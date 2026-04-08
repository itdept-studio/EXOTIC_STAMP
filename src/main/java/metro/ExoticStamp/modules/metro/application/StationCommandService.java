package metro.ExoticStamp.modules.metro.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.common.exceptions.storage.InvalidFileException;
import metro.ExoticStamp.infra.storage.FileValidator;
import metro.ExoticStamp.infra.storage.StorageService;
import metro.ExoticStamp.modules.metro.application.mapper.MetroAppMapper;
import metro.ExoticStamp.modules.metro.application.port.StationCachePort;
import metro.ExoticStamp.modules.metro.domain.event.StationActivatedEvent;
import metro.ExoticStamp.modules.metro.domain.event.StationDeactivatedEvent;
import metro.ExoticStamp.modules.metro.domain.event.StationQrRotatedEvent;
import metro.ExoticStamp.modules.metro.domain.exception.DuplicateNfcTagException;
import metro.ExoticStamp.modules.metro.domain.exception.DuplicateQrTokenException;
import metro.ExoticStamp.modules.metro.domain.exception.DuplicateStationCodeException;
import metro.ExoticStamp.modules.metro.domain.exception.DuplicateStationSequenceException;
import metro.ExoticStamp.modules.metro.domain.exception.LineNotFoundException;
import metro.ExoticStamp.modules.metro.domain.exception.StationNotFoundException;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import metro.ExoticStamp.modules.rbac.application.support.RbacTransactionCallbacks;
import metro.ExoticStamp.modules.metro.presentation.dto.request.CreateStationRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.RotateQrTokenRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.UpdateStationRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationDetailResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationImageUploadResponse;
import org.springframework.context.ApplicationEventPublisher;
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
    public StationDetailResponse createStation(CreateStationRequest req) {
        UUID lineId = req.getLineId();
        lineRepository.findById(lineId).orElseThrow(() -> new LineNotFoundException(lineId));
        validateSequence(lineId, req.getSequence(), null);
        validateNewStationCodes(lineId, req.getCode(), req.getNfcTagId(), req.getQrCodeToken());

        LocalDateTime now = LocalDateTime.now();
        boolean active = Boolean.TRUE.equals(req.getIsActive());
        Station station = Station.builder()
                .lineId(lineId)
                .code(req.getCode().trim())
                .name(req.getName().trim())
                .sequence(req.getSequence())
                .description(req.getDescription())
                .historicalInfo(req.getHistoricalInfo())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .nfcTagId(blankToNull(req.getNfcTagId()))
                .qrCodeToken(blankToNull(req.getQrCodeToken()))
                .collectorCount(0)
                .isActive(active)
                .createdAt(now)
                .build();
        Station saved = stationRepository.save(station);
        if (active) {
            bumpLineTotalStations(lineId, 1);
        }
        return mapper.toStationDetail(saved, true);
    }

    @Transactional
    public StationDetailResponse updateStation(UUID stationId, UpdateStationRequest req) {
        Station station = stationRepository.findById(stationId).orElseThrow(() -> new StationNotFoundException(stationId));
        String oldNfc = station.getNfcTagId();
        String oldQr = station.getQrCodeToken();
        boolean wasActive = Boolean.TRUE.equals(station.getIsActive());

        if (req.getCode() != null && !req.getCode().isBlank()) {
            String code = req.getCode().trim();
            if (!code.equals(station.getCode()) && stationRepository.existsByCodeAndIdNot(code, stationId)) {
                throw new DuplicateStationCodeException(code, station.getLineId());
            }
            station.setCode(code);
        }
        if (req.getName() != null && !req.getName().isBlank()) {
            station.setName(req.getName().trim());
        }
        if (req.getSequence() != null) {
            validateSequence(station.getLineId(), req.getSequence(), stationId);
            station.setSequence(req.getSequence());
        }
        if (req.getDescription() != null) {
            station.setDescription(req.getDescription());
        }
        if (req.getHistoricalInfo() != null) {
            station.setHistoricalInfo(req.getHistoricalInfo());
        }
        if (req.getLatitude() != null) {
            station.setLatitude(req.getLatitude());
        }
        if (req.getLongitude() != null) {
            station.setLongitude(req.getLongitude());
        }
        if (req.getNfcTagId() != null) {
            String nfc = blankToNull(req.getNfcTagId());
            if (nfc != null && !Objects.equals(nfc, station.getNfcTagId())) {
                if (stationRepository.existsByNfcTagIdAndIdNot(nfc, stationId)) {
                    throw new DuplicateNfcTagException(nfc);
                }
            }
            station.setNfcTagId(nfc);
        }
        if (req.getQrCodeToken() != null) {
            String qr = blankToNull(req.getQrCodeToken());
            if (qr != null && !Objects.equals(qr, station.getQrCodeToken())) {
                if (stationRepository.existsByQrCodeTokenAndIdNot(qr, stationId)) {
                    throw new DuplicateQrTokenException(qr);
                }
            }
            station.setQrCodeToken(qr);
        }
        if (req.getIsActive() != null) {
            boolean nowActive = Boolean.TRUE.equals(req.getIsActive());
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
        return mapper.toStationDetail(saved, true);
    }

    @Transactional
    public StationDetailResponse activateStation(UUID stationId) {
        Station station = stationRepository.findById(stationId).orElseThrow(() -> new StationNotFoundException(stationId));
        if (Boolean.TRUE.equals(station.getIsActive())) {
            return mapper.toStationDetail(station, true);
        }
        station.setIsActive(true);
        station.setUpdatedAt(LocalDateTime.now());
        Station saved = stationRepository.save(station);
        bumpLineTotalStations(saved.getLineId(), 1);
        evictAllStationCaches(saved);
        RbacTransactionCallbacks.afterCommit(
                () -> eventPublisher.publishEvent(new StationActivatedEvent(saved.getId())));
        return mapper.toStationDetail(saved, true);
    }

    @Transactional
    public StationDetailResponse deactivateStation(UUID stationId) {
        Station station = stationRepository.findById(stationId).orElseThrow(() -> new StationNotFoundException(stationId));
        if (!Boolean.TRUE.equals(station.getIsActive())) {
            return mapper.toStationDetail(station, true);
        }
        station.setIsActive(false);
        station.setUpdatedAt(LocalDateTime.now());
        Station saved = stationRepository.save(station);
        bumpLineTotalStations(saved.getLineId(), -1);
        evictAllStationCaches(saved);
        RbacTransactionCallbacks.afterCommit(
                () -> eventPublisher.publishEvent(new StationDeactivatedEvent(saved.getId())));
        return mapper.toStationDetail(saved, true);
    }

    @Transactional
    public StationDetailResponse rotateQrToken(UUID stationId, RotateQrTokenRequest req) {
        Station station = stationRepository.findById(stationId).orElseThrow(() -> new StationNotFoundException(stationId));
        String newQr = req.getQrCodeToken().trim();
        if (stationRepository.existsByQrCodeTokenAndIdNot(newQr, stationId)) {
            throw new DuplicateQrTokenException(newQr);
        }
        String oldQr = station.getQrCodeToken();
        station.setQrCodeToken(newQr);
        station.setUpdatedAt(LocalDateTime.now());
        Station saved = stationRepository.save(station);
        if (oldQr != null) {
            stationCachePort.evictByQrToken(oldQr);
        }
        stationCachePort.evictDetailByStationId(saved.getId());
        RbacTransactionCallbacks.afterCommit(
                () -> eventPublisher.publishEvent(new StationQrRotatedEvent(saved.getId(), oldQr, newQr)));
        return mapper.toStationDetail(saved, true);
    }

    @Transactional
    public void incrementCollectorCount(UUID stationId) {
        Station station = stationRepository.findById(stationId).orElseThrow(() -> new StationNotFoundException(stationId));
        int next = (station.getCollectorCount() == null ? 0 : station.getCollectorCount()) + 1;
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
    public StationImageUploadResponse uploadStationImage(UUID stationId, MultipartFile file) {
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
        return new StationImageUploadResponse(url);
    }

    private void validateSequence(UUID lineId, Integer sequence, UUID excludeStationId) {
        if (sequence == null) {
            return;
        }
        boolean dup = excludeStationId == null
                ? stationRepository.existsByLineIdAndSequence(lineId, sequence)
                : stationRepository.existsByLineIdAndSequenceAndIdNot(lineId, sequence, excludeStationId);
        if (dup) {
            throw new DuplicateStationSequenceException(lineId, sequence);
        }
    }

    private void validateNewStationCodes(UUID lineId, String code, String nfcTagId, String qrCodeToken) {
        if (stationRepository.existsByCode(code.trim())) {
            throw new DuplicateStationCodeException(code.trim(), lineId);
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
        int next = Math.max(0, line.getTotalStations() + delta);
        line.setTotalStations(next);
        line.setUpdatedAt(LocalDateTime.now());
        lineRepository.save(line);
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }

    private void evictStationCaches(Station saved, String oldNfc, String oldQr) {
        try {
            stationCachePort.evictDetailByStationId(saved.getId());
            if (oldNfc != null) {
                stationCachePort.evictByNfcTagId(oldNfc);
            }
            if (saved.getNfcTagId() != null) {
                stationCachePort.evictByNfcTagId(saved.getNfcTagId());
            }
            if (oldQr != null) {
                stationCachePort.evictByQrToken(oldQr);
            }
            if (saved.getQrCodeToken() != null) {
                stationCachePort.evictByQrToken(saved.getQrCodeToken());
            }
        } catch (Exception e) {
            log.warn("[StationCommand] cache eviction failed after station update: {}", e.getMessage());
        }
    }

    private void evictAllStationCaches(Station station) {
        try {
            stationCachePort.evictDetailByStationId(station.getId());
            if (station.getNfcTagId() != null) {
                stationCachePort.evictByNfcTagId(station.getNfcTagId());
            }
            if (station.getQrCodeToken() != null) {
                stationCachePort.evictByQrToken(station.getQrCodeToken());
            }
        } catch (Exception e) {
            log.warn("[StationCommand] cache eviction failed: {}", e.getMessage());
        }
    }
}
