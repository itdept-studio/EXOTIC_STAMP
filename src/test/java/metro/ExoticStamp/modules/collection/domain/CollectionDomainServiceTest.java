package metro.ExoticStamp.modules.collection.domain;

import metro.ExoticStamp.modules.collection.domain.exception.IdempotencyKeyConflictException;
import metro.ExoticStamp.modules.collection.domain.exception.StampAlreadyCollectedException;
import metro.ExoticStamp.modules.collection.domain.model.CollectMethod;
import metro.ExoticStamp.modules.collection.domain.model.UserStamp;
import metro.ExoticStamp.modules.collection.domain.repository.UserStampRepository;
import metro.ExoticStamp.modules.collection.domain.service.CollectionDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionDomainServiceTest {

    private static final UUID U1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID U2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID STATION = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID CAMPAIGN = UUID.fromString("00000000-0000-0000-0000-000000000020");

    @Mock
    private UserStampRepository userStampRepository;

    private CollectionDomainService service;

    @BeforeEach
    void setUp() {
        service = new CollectionDomainService(userStampRepository);
    }

    @Test
    void assertNotAlreadyCollected_passes() {
        when(userStampRepository.existsByUserIdAndStationIdAndCampaignId(U1, STATION, CAMPAIGN)).thenReturn(false);
        assertDoesNotThrow(() -> service.assertNotAlreadyCollected(U1, STATION, CAMPAIGN));
    }

    @Test
    void assertNotAlreadyCollected_throws() {
        when(userStampRepository.existsByUserIdAndStationIdAndCampaignId(U1, STATION, CAMPAIGN)).thenReturn(true);
        assertThrows(StampAlreadyCollectedException.class, () -> service.assertNotAlreadyCollected(U1, STATION, CAMPAIGN));
    }

    @Test
    void resolveIdempotent_empty() {
        when(userStampRepository.findFirstByIdempotencyKeyAndCollectedAtAfterOrderByCollectedAtDesc("k", LocalDateTime.MIN))
                .thenReturn(Optional.empty());
        assertTrue(service.resolveIdempotentStamp("k", U1, LocalDateTime.MIN).isEmpty());
    }

    @Test
    void resolveIdempotent_sameUser_returns() {
        UserStamp us = UserStamp.builder()
                .userId(U1)
                .stationId(STATION)
                .campaignId(CAMPAIGN)
                .stampDesignId(UUID.randomUUID())
                .collectedAt(LocalDateTime.now())
                .gpsVerified(false)
                .collectMethod(CollectMethod.NFC)
                .deviceFingerprint("1234567890")
                .idempotencyKey("k")
                .createdAt(LocalDateTime.now())
                .build();
        when(userStampRepository.findFirstByIdempotencyKeyAndCollectedAtAfterOrderByCollectedAtDesc("k", LocalDateTime.MIN))
                .thenReturn(Optional.of(us));
        assertSame(us, service.resolveIdempotentStamp("k", U1, LocalDateTime.MIN).orElseThrow());
    }

    @Test
    void resolveIdempotent_otherUser_conflict() {
        UserStamp us = UserStamp.builder()
                .userId(U2)
                .stationId(STATION)
                .campaignId(CAMPAIGN)
                .stampDesignId(UUID.randomUUID())
                .collectedAt(LocalDateTime.now())
                .gpsVerified(false)
                .collectMethod(CollectMethod.NFC)
                .deviceFingerprint("1234567890")
                .idempotencyKey("k")
                .createdAt(LocalDateTime.now())
                .build();
        when(userStampRepository.findFirstByIdempotencyKeyAndCollectedAtAfterOrderByCollectedAtDesc("k", LocalDateTime.MIN))
                .thenReturn(Optional.of(us));
        assertThrows(IdempotencyKeyConflictException.class, () -> service.resolveIdempotentStamp("k", U1, LocalDateTime.MIN));
    }
}
