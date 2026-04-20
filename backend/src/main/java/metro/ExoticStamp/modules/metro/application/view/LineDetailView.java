package metro.ExoticStamp.modules.metro.application.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineDetailView {
    private UUID id;
    private String code;
    private String name;
    private String color;
    private Integer totalStations;
    private boolean isActive;
    private List<StationView> stations;
}
