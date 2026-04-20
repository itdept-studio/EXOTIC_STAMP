package metro.ExoticStamp.modules.metro.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.common.exceptions.storage.InvalidFileException;
import metro.ExoticStamp.infra.storage.FileValidator;
import metro.ExoticStamp.infra.storage.StorageService;
import metro.ExoticStamp.modules.metro.application.command.CreateStationCommand;
import metro.ExoticStamp.modules.metro.application.command.RotateStationQrTokenCommand;
import metro.ExoticStamp.modules.metro.application.command.UpdateStationCommand;
import metro.ExoticStamp.modules.metro.application.mapper.MetroAppMapper;
import metro.ExoticStamp.modules.metro.application.port.StationCachePort;
import metro.ExoticStamp.modules.metro.application.view.StationDetailView;
import metro.ExoticStamp.modules.metro.application.view.StationImageUploadView;
import metro.ExoticStamp.modules.metro.domain.event.StationActivatedEvent;
import metro.ExoticStamp.modules.metro.domain.event.StationDeactivatedEvent;
import metro.ExoticStamp.modules.metro.domain.event.StationQrRotatedEvent;
import metro.ExoticStamp.modules.metro.domain.exception.DuplicateNfcTagException;
import metro.ExoticStamp.modules.metro.domain.exception.DuplicateQrTokenException;
import metro.ExoticStamp.modules.metro.domain.exception.DuplicateStationCodeException;
import metro.ExoticStamp.modules.metro.domain.exception.DuplicateStationSequenceException;
import metro.ExoticStamp.modules.metro.domain.exception.LineNotFoundException;
import metro.ExoticStamp.modules.metro.domain.exception.StationInactiveException;
import metro.ExoticStamp.modules.metro.domain.exception.StationNotFoundException;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import metro.ExoticStamp.modules.rbac.application.support.RbacTransactionCallbacks;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StationCommandService {

    private final LineRepository lineRepository;
    private final StationRepository stationRepository;
    private final StationCachePort stationCachePort;
    private final MetroAppMapper mapper;
    private final StorageService storageService;
    private final FileValidator fileValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public StationDetailView createStation(CreateStationCommand command) {
        UUID lineId = command.getLineId();
        lineRepository.findById(lineId).orElseThrow(() -> new LineNotFoundException(lineId));
        validateSequence(lineId, command.getSequence(), null);
        validateNewStationCodes(lineId, command.getCode(), command.getNfcTagId(), command.getQrCodeToken());

        LocalDateTime now = LocalDateTime.now();
        boolean active = Boolean.TRUE.equals(command.getIsActive());
        Station station = Station.builder()
                .lineId(lineId)
                .code(command.getCode().trim())
                .name(command.getName().trim())
                .sequence(command.getSequence())
                .description(command.getDescription())
                .historicalInfo(command.getHistoricalInfo())
                .latitude(command.getLatitude())
                .longitude(command.getLongitude())
                .nfcTagId(blankToNull(command.getNfcTagId()))
                .qrCodeToken(blankToNull(command.getQrCodeToken()))
                .collectorCount(0)
                .isActive(active)
                .createdAt(now)
                .build();
        Station saved = stationRepository.save(station);
        if (active) {
            bumpLineTotalStations(lineId, 1);
        }
        return mapper.toStationDetailView(saved, true);
    }

    @Transactional
    public StationDetailView updateStation(UpdateStationCommand command) {
        UUID stationId = command.getStationId();
        Station station = stationRepository.findById(stationId).orElseThrow(() -> new StationNotFoundException(stationId));
        String oldNfc = station.getNfcTagId();
        String oldQr = station.getQrCodeToken();
        boolean wasActive = Boolean.TRUE.equals(station.getIsActive());

        if (command.getCode() != null && !command.getCode().isBlank()) {
            String code = command.getCode().trim();
            if (!code.equals(station.getCode()) && stationRepository.existsByCodeAndIdNot(code, stationId)) {
                throw new DuplicateStationCodeException(code, station.getLineId());
            }
            station.setCode(code);
        }
        if (command.getName() != null && !command.getName().isBlank()) {
            station.setName(command.getName().trim());
        }
        if (command.getSequence() != null) {
            validateSequence(station.getLineId(), command.getSequence(), stationId);
            station.setSequence(command.getSequence());
        }
        if (command.getDescription() != null) {
            station.setDescription(command.getDescription());
        }
        if (command.getHistoricalInfo() != null) {
            station.setHistoricalInfo(command.getHistoricalInfo());
        }
        if (command.getLatitude() != null) {
            station.setLatitude(command.getLatitude());
        }
        if (command.getLongitude() != null) {
            station.setLongitude(command.getLongitude());
        }
        if (command.getNfcTagId() != null) {
            String nfc = blankToNull(command.getNfcTagId());
            if (nfc != null && !Objects.equals(nfc, station.getNfcTagId())
                    && stationRepository.existsByNfcTagIdAndIdNot(nfc, stationId)) {
                throw new DuplicateNfcTagException(nfc);
            }
            station.setNfcTagId(nfc);
        }
        if (command.getQrCodeToken() != null) {
            String qr = blankToNull(command.getQrCodeToken());
            if (qr != null && !Objects.equals(qr, station.getQrCodeToken())
                    && stationRepository.existsByQrCodeTokenAndIdNot(qr, stationId)) {
                throw new DuplicateQrTokenException(qr);
            }
            station.setQrCodeToken(qr);
        }
        if (command.getIsActive() != null) {
            boolean nowActive = Boolean.TRUE.equals(command.getIsActive());
            if (wasActive != nowActive) {
                station.setIsActive(nowActive);
                if (nowActive) {
                    bumpLineTotalStations(station.getLineId(), 1);
                    RbacTransactionCallbacks.afterCommit(
                            () -> eventPublisher.publishEvent(new StationActivatedEvent(station.getId())));
                } else {
                    bumpLineTotalStations(station.getLineId(), -1);
                    RbacTransactionCallbacks.afterCommit(
                            () -> eventPublisher.publishEvent(new StationDeactivatedEvent(station.getId())));
                }
            }
        }

        station.setUpdatedAt(LocalDateTime.now());
        Station saved = stationRepository.save(station);
        evictStationCaches(saved, oldNfc, oldQr);
        return mapper.toStationDetailView(saved, true);
    }

    @Transactional
    public StationDetailView activateStation(UUID stationId) {
        Station station = stationRepository.findById(stationId).orElseThrow(() -> new StationNotFoundException(stationId));
        if (Boolean.TRUE.equals(station.getIsActive())) {
            return mapper.toStationDetailView(station, true);
        }
        station.setIsActive(true);
        station.setUpdatedAt(LocalDateTime.now());
        Station saved = stationRepository.save(station);
        bumpLineTotalStations(saved.getLineId(), 1);
        evictAllStationCaches(saved);
        RbacTransactionCallbacks.afterCommit(
                () -> eventPublisher.publishEvent(new StationActivatedEvent(saved.getId())));
        return mapper.toStationDetailView(saved, true);
    }

    @Transactional
    public StationDetailView deactivateStation(UUID stationId) {
        Station station = stationRepository.findById(stationId).orElseThrow(() -> new StationNotFoundException(stationId));
        if (!Boolean.TRUE.equals(station.getIsActive())) {
            return mapper.toStationDetailView(station, true);
        }
        station.setIsActive(false);
        station.setUpdatedAt(LocalDateTime.now());
        Station saved = stationRepository.save(station);
        bumpLineTotalStations(saved.getLineId(), -1);
        evictAllStationCaches(saved);
        RbacTransactionCallbacks.afterCommit(
                () -> eventPublisher.publishEvent(new StationDeactivatedEvent(saved.getId())));
        return mapper.toStationDetailView(saved, true);
    }

    @Transactional
    public StationDetailView rotateQrToken(RotateStationQrTokenCommand command) {
        UUID stationId = command.getStationId();
        Station station = stationRepository.findById(stationId).orElseThrow(() -> new StationNotFoundException(stationId));
        String newQr = command.getQrCodeToken().trim();
        if (stationRepository.existsByQrCodeTokenAndIdNot(newQr, stationId)) {
            throw new DuplicateQrTokenException(newQr);
        }
        String oldQr = station.getQrCodeToken();
        station.setQrCodeToken(newQr);
        station.setUpdatedAt(LocalDateTime.now());
        Station saved = stationRepository.save(station);
        RbacTransactionCallbacks.afterCommit(
                () -> {
                    if (oldQr != null) {
                        stationCachePort.evictByQrToken(oldQr);
                    }
                    stationCachePort.evictDetailByStationId(saved.getId());
                    eventPublisher.publishEvent(new StationQrRotatedEvent(saved.getId(), oldQr, newQr));
                });
        return mapper.toStationDetailView(saved, true);
    }

    @Transactional
    public void incrementCollectorCount(UUID stationId) {
        Station station = stationRepository.findById(stationId).orElseThrow(() -> new StationNotFoundException(stationId));
        if (!Boolean.TRUE.equals(station.getIsActive())) {
            log.warn("[Metro] reject collectorCount increment for inactive stationId={} principal={}",
                    stationId, currentPrincipalName());
            throw new StationInactiveException(stationId);
        }
        log.info("[Metro] increment collectorCount stationId={} principal={}", stationId, currentPrincipalName());
        int current = station.getCollectorCount() == null ? 0 : station.getCollectorCount();
        final int next;
        try {
            next = Math.addExact(current, 1);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("collectorCount overflow for station: " + stationId);
        }
        station.setCollectorCount(next);
        station.setUpdatedAt(LocalDateTime.now());
        stationRepository.save(station);
        try {
            stationCachePort.evictDetailByStationId(stationId);
            if (station.getNfcTagId() != null) {
                stationCachePort.evictByNfcTagId(station.getNfcTagId());
            }
            if (station.getQrCodeToken() != null) {
                stationCachePort.evictByQrToken(station.getQrCodeToken());
            }
        } catch (Exception e) {
            log.warn("[StationCommand] cache eviction after collector increment failed: {}", e.getMessage());
        }
    }

    @Transactional
    public void softDeleteStation(UUID stationId) {
        Station station = stationRepository.findById(stationId).orElseThrow(() -> new StationNotFoundException(stationId));
        if (Boolean.FALSE.equals(station.getIsActive())) {
            return;
        }
        bumpLineTotalStations(station.getLineId(), -1);
        station.setIsActive(false);
        station.setUpdatedAt(LocalDateTime.now());
        stationRepository.save(station);
        evictAllStationCaches(station);
        RbacTransactionCallbacks.afterCommit(
                () -> eventPublisher.publishEvent(new StationDeactivatedEvent(station.getId())));
    }

    @Transactional
    public StationImageUploadView uploadStationImage(UUID stationId, MultipartFile file) {
        if (file == null) {
            throw new InvalidFileException("File is required");
        }
        fileValidator.validate(file);
        Station station = stationRepository.findById(stationId).orElseThrow(() -> new StationNotFoundException(stationId));
        String oldUrl = station.getImageUrl();
        if (oldUrl != null && !oldUrl.isBlank()) {
            try {
                storageService.delete(oldUrl);
            } catch (Exception e) {
                log.warn("[StationCommand] best-effort delete of old image failed stationId={} err={}", stationId, e.getMessage());
            }
        }
        String folder = "metro/stations/" + stationId;
        String url = storageService.upload(file, folder);
        station.setImageUrl(url);
        station.setUpdatedAt(LocalDateTime.now());
        stationRepository.save(station);
        evictAllStationCaches(station);
        return new StationImageUploadView(url);
    }

    private void validateSequence(UUID lineId, Integer sequence, UUID stationId) {
        boolean exists = stationId == null
                ? stationRepository.existsByLineIdAndSequence(lineId, sequence)
                : stationRepository.existsByLineIdAndSequenceAndIdNot(lineId, sequence, stationId);
        if (exists) {
            throw new DuplicateStationSequenceException(lineId, sequence);
        }
    }

    private void validateNewStationCodes(UUID lineId, String code, String nfcTagId, String qrCodeToken) {
        if (stationRepository.existsByCode(code)) {
            throw new DuplicateStationCodeException(code, lineId);
        }
        String nfc = blankToNull(nfcTagId);
        if (nfc != null && stationRepository.existsByNfcTagId(nfc)) {
            throw new DuplicateNfcTagException(nfc);
        }
        String qr = blankToNull(qrCodeToken);
        if (qr != null && stationRepository.existsByQrCodeToken(qr)) {
            throw new DuplicateQrTokenException(qr);
        }
    }

    private void bumpLineTotalStations(UUID lineId, int delta) {
        Line line = lineRepository.findById(lineId).orElseThrow(() -> new LineNotFoundException(lineId));
        int current = line.getTotalStations() == null ? 0 : line.getTotalStations();
        line.setTotalStations(Math.max(0, current + delta));
        line.setUpdatedAt(LocalDateTime.now());
        lineRepository.save(line);
    }

    private void evictAllStationCaches(Station station) {
        stationCachePort.evictDetailByStationId(station.getId());
        if (station.getNfcTagId() != null) {
            stationCachePort.evictByNfcTagId(station.getNfcTagId());
        }
        if (station.getQrCodeToken() != null) {
            stationCachePort.evictByQrToken(station.getQrCodeToken());
        }
    }

    private void evictStationCaches(Station station, String oldNfc, String oldQr) {
        stationCachePort.evictDetailByStationId(station.getId());
        if (oldNfc != null && !oldNfc.equals(station.getNfcTagId())) {
            stationCachePort.evictByNfcTagId(oldNfc);
        }
        if (oldQr != null && !oldQr.equals(station.getQrCodeToken())) {
            stationCachePort.evictByQrToken(oldQr);
        }
        if (station.getNfcTagId() != null) {
            stationCachePort.evictByNfcTagId(station.getNfcTagId());
        }
        if (station.getQrCodeToken() != null) {
            stationCachePort.evictByQrToken(station.getQrCodeToken());
        }
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String currentPrincipalName() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth == null ? "anonymous" : auth.getName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
