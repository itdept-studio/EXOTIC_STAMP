package metro.ExoticStamp.modules.collection.domain.factory;

import metro.ExoticStamp.modules.collection.domain.model.Campaign;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Shared construction of default-per-line campaigns (bootstrap + line-created events).
 */
public final class DefaultCampaignFactory {

    private DefaultCampaignFactory() {}

    public static String defaultCampaignCode(UUID lineId) {
        String compact = lineId.toString().replace("-", "");
        return ("DEF-" + compact).substring(0, 30);
    }

    public static Campaign createDefaultForLine(UUID lineId, String lineName, String lineCode) {
        LocalDateTime now = LocalDateTime.now();
        return Campaign.builder()
                .lineId(lineId)
                .isDefault(true)
                .isActive(true)
                .code(defaultCampaignCode(lineId))
                .name("Default campaign: " + lineName)
                .description("Auto-created default campaign for line " + lineCode)
                .startDate(now)
                .endDate(now.plusYears(50))
                .createdAt(now)
                .build();
    }
}
