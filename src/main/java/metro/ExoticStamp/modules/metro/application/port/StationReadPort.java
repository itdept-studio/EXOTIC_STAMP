package metro.ExoticStamp.modules.metro.application.port;

import metro.ExoticStamp.modules.metro.application.view.MetroStationView;

import java.util.List;
import java.util.UUID;

public interface StationReadPort {

    MetroStationView resolveStationViewByNfc(String nfcTagId);

    MetroStationView resolveStationViewByQr(String qrToken);

    MetroStationView getStationViewById(UUID stationId);

    List<MetroStationView> listActiveStationsByLineId(UUID lineId);
}
