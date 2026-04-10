package metro.ExoticStamp.modules.collection.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Collection module settings (idempotency window, query limits).
 */
@Data
@Validated
@ConfigurationProperties(prefix = "collection")
public class CollectionProperties {

    /** How long a client idempotency key is honored for duplicate detection. */
    @NotNull
    private Duration idempotencyWindow = Duration.ofHours(1);

    /** Max page size for history and stamp lists. */
    private int maxPageSize = 50;

    /** Default page size for paginated collection reads. */
    private int defaultPageSize = 20;
}
