package metro.ExoticStamp.modules.collection.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.collection.application.command.AdminCreateCampaignCommand;
import metro.ExoticStamp.modules.collection.application.command.AdminCreateStampDesignCommand;
import metro.ExoticStamp.modules.collection.application.command.AdminUpdateCampaignCommand;
import metro.ExoticStamp.modules.collection.application.command.AdminUpdateStampDesignCommand;
import metro.ExoticStamp.modules.collection.application.mapper.CollectionAdminMapper;
import metro.ExoticStamp.modules.collection.application.view.AdminCampaignView;
import metro.ExoticStamp.modules.collection.application.view.AdminStampDesignView;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignNotFoundException;
import metro.ExoticStamp.modules.collection.domain.exception.InvalidRequestException;
import metro.ExoticStamp.modules.collection.domain.exception.InvalidStationException;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignStationRepository;
import metro.ExoticStamp.modules.collection.domain.repository.StampDesignRepository;
import metro.ExoticStamp.modules.metro.application.port.LineReadPort;
import metro.ExoticStamp.modules.metro.application.port.StationReadPort;
import metro.ExoticStamp.modules.metro.application.view.MetroStationView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CollectionAdminCommandService {

    private final CampaignRepository campaignRepository;
    private final CampaignStationRepository campaignStationRepository;
    private final StampDesignRepository stampDesignRepository;
    private final LineReadPort lineReadPort;
    private final StationReadPort stationReadPort;
    private final CollectionAdminMapper collectionAdminMapper;
    private final Clock clock;

    @Transactional
    public AdminCampaignView createCampaign(AdminCreateCampaignCommand cmd) {
        lineReadPort.getLineById(cmd.lineId());
        String code = cmd.code().trim();
        if (campaignRepository.existsByCode(code)) {
            throw new InvalidRequestException("Campaign code already exists");
        }
        if (cmd.defaultCampaign() && campaignRepository.existsDefaultByLineId(cmd.lineId())) {
            throw new InvalidRequestException("Default campaign already exists for this line");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        Campaign campaign = Campaign.builder()
                .lineId(cmd.lineId())
                .partnerId(cmd.partnerId())
                .code(code)
                .name(cmd.name().trim())
                .description(cmd.description())
                .bannerUrl(cmd.bannerUrl())
                .startDate(cmd.startDate())
                .endDate(cmd.endDate())
                .isActive(cmd.active())
                .isDefault(cmd.defaultCampaign())
                .createdAt(now)
                .build();
        Campaign saved = campaignRepository.save(campaign);
        return collectionAdminMapper.toCampaignView(saved);
    }

    @Transactional
    public AdminCampaignView updateCampaign(AdminUpdateCampaignCommand cmd) {
        Campaign campaign = campaignRepository.findById(cmd.id()).orElseThrow(() -> new CampaignNotFoundException(cmd.id()));
        String code = cmd.code().trim();
        if (!code.equals(campaign.getCode()) && campaignRepository.existsByCode(code)) {
            throw new InvalidRequestException("Campaign code already exists");
        }
        if (cmd.defaultCampaign() && !campaign.isDefault() && campaign.getLineId() != null
                && campaignRepository.existsDefaultByLineId(campaign.getLineId())) {
            throw new InvalidRequestException("Default campaign already exists for this line");
        }
        campaign.setPartnerId(cmd.partnerId());
        campaign.setCode(code);
        campaign.setName(cmd.name().trim());
        campaign.setDescription(cmd.description());
        campaign.setBannerUrl(cmd.bannerUrl());
        campaign.setStartDate(cmd.startDate());
        campaign.setEndDate(cmd.endDate());
        campaign.setActive(cmd.active());
        campaign.setDefault(cmd.defaultCampaign());
        Campaign saved = campaignRepository.save(campaign);
        return collectionAdminMapper.toCampaignView(saved);
    }

    @Transactional
    public AdminCampaignView activateCampaign(UUID id) {
        Campaign campaign = campaignRepository.findById(id).orElseThrow(() -> new CampaignNotFoundException(id));
        campaign.setActive(true);
        return collectionAdminMapper.toCampaignView(campaignRepository.save(campaign));
    }

    @Transactional
    public AdminCampaignView deactivateCampaign(UUID id) {
        Campaign campaign = campaignRepository.findById(id).orElseThrow(() -> new CampaignNotFoundException(id));
        campaign.setActive(false);
        return collectionAdminMapper.toCampaignView(campaignRepository.save(campaign));
    }

    @Transactional
    public void assignStationToCampaign(UUID campaignId, UUID stationId) {
        Campaign campaign = campaignRepository.findById(campaignId).orElseThrow(() -> new CampaignNotFoundException(campaignId));
        MetroStationView station = stationReadPort.getStationViewById(stationId);
        if (campaign.getLineId() == null || !campaign.getLineId().equals(station.lineId())) {
            throw new InvalidRequestException("Station is not on the campaign line");
        }
        if (!station.active()) {
            throw new InvalidStationException(stationId, "Station is inactive");
        }
        campaignStationRepository.assign(campaignId, stationId);
    }

    @Transactional
    public void removeStationFromCampaign(UUID campaignId, UUID stationId) {
        if (campaignRepository.findById(campaignId).isEmpty()) {
            throw new CampaignNotFoundException(campaignId);
        }
        campaignStationRepository.remove(campaignId, stationId);
    }

    @Transactional
    public AdminStampDesignView createStampDesign(AdminCreateStampDesignCommand cmd) {
        validateStampDesignRefs(cmd.campaignId(), cmd.stationId());
        LocalDateTime now = LocalDateTime.now(clock);
        StampDesign entity = StampDesign.builder()
                .stationId(cmd.stationId())
                .campaignId(cmd.campaignId())
                .name(cmd.name().trim())
                .artworkUrl(cmd.artworkUrl().trim())
                .animationUrl(cmd.animationUrl())
                .soundUrl(cmd.soundUrl())
                .isLimited(cmd.limited())
                .isActive(cmd.active())
                .createdAt(now)
                .build();
        StampDesign saved = stampDesignRepository.save(entity);
        return collectionAdminMapper.toStampDesignView(saved);
    }

    @Transactional
    public AdminStampDesignView updateStampDesign(AdminUpdateStampDesignCommand cmd) {
        StampDesign entity = stampDesignRepository.findById(cmd.id()).orElseThrow(() -> new InvalidRequestException("Stamp design not found: " + cmd.id()));
        validateStampDesignRefs(cmd.campaignId(), cmd.stationId());
        entity.setStationId(cmd.stationId());
        entity.setCampaignId(cmd.campaignId());
        entity.setName(cmd.name().trim());
        entity.setArtworkUrl(cmd.artworkUrl().trim());
        entity.setAnimationUrl(cmd.animationUrl());
        entity.setSoundUrl(cmd.soundUrl());
        entity.setLimited(cmd.limited());
        entity.setActive(cmd.active());
        StampDesign saved = stampDesignRepository.save(entity);
        return collectionAdminMapper.toStampDesignView(saved);
    }

    @Transactional
    public AdminStampDesignView activateStampDesign(UUID id) {
        StampDesign entity = stampDesignRepository.findById(id).orElseThrow(() -> new InvalidRequestException("Stamp design not found: " + id));
        entity.setActive(true);
        return collectionAdminMapper.toStampDesignView(stampDesignRepository.save(entity));
    }

    @Transactional
    public AdminStampDesignView deactivateStampDesign(UUID id) {
        StampDesign entity = stampDesignRepository.findById(id).orElseThrow(() -> new InvalidRequestException("Stamp design not found: " + id));
        entity.setActive(false);
        return collectionAdminMapper.toStampDesignView(stampDesignRepository.save(entity));
    }

    private void validateStampDesignRefs(UUID campaignId, UUID stationId) {
        MetroStationView station = null;
        if (stationId != null) {
            station = stationReadPort.getStationViewById(stationId);
            if (!station.active()) {
                throw new InvalidStationException(stationId, "Station is inactive");
            }
        }
        if (campaignId != null) {
            Campaign campaign = campaignRepository.findById(campaignId).orElseThrow(() -> new CampaignNotFoundException(campaignId));
            if (station != null && campaign.getLineId() != null) {
                if (!campaign.getLineId().equals(station.lineId())) {
                    throw new InvalidRequestException("Station does not belong to campaign line");
                }
            }
        }
    }
}
