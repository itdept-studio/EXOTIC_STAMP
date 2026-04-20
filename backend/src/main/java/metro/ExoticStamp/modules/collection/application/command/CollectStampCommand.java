package metro.ExoticStamp.modules.collection.application.command;

import metro.ExoticStamp.modules.collection.domain.model.CollectMethod;

import java.math.BigDecimal;
import java.util.UUID;

public record CollectStampCommand(
        UUID userId,
        UUID idempotencyKey,
        String nfcTagId,
        String qrToken,
        UUID campaignId,
        String deviceFingerprint,
        BigDecimal latitude,
        BigDecimal longitude,
        CollectMethod collectMethod
) {}

