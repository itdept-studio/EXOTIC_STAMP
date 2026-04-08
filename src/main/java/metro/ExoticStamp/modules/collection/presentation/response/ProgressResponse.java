package metro.ExoticStamp.modules.collection.presentation.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressResponse {
    private UUID lineId;
    private long collected;
    private long total;
    private int percentage;
}

