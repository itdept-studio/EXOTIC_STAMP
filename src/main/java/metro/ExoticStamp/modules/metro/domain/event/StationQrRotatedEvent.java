package metro.ExoticStamp.modules.metro.domain.event;

public record StationQrRotatedEvent(Integer stationId, String oldQrToken, String newQrToken) {}
