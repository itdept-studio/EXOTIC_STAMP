package metro.ExoticStamp.modules.collection.domain.repository;

import metro.ExoticStamp.common.model.PageResult;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampaignRepository {

    Optional<Campaign> findById(UUID id);

    Campaign save(Campaign campaign);

    boolean existsDefaultByLineId(UUID lineId);

    Optional<Campaign> findDefaultByLineId(UUID lineId);

    List<Campaign> findAllActiveDefaults();

    boolean existsByCode(String code);

    PageResult<Campaign> findAllPaged(int page, int size);

    List<Campaign> findByLineIdOrderByCodeAsc(UUID lineId);
}

