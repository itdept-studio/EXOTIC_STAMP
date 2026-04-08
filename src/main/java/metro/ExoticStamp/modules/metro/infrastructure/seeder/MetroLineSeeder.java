package metro.ExoticStamp.modules.metro.infrastructure.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Idempotent reference seed for metro M1 + 14 stations (shared kernel).
 */
@Component
@Order(50)
@RequiredArgsConstructor
@Slf4j
public class MetroLineSeeder implements CommandLineRunner {

    private static final String LINE_CODE = "M1";

    private final LineRepository lineRepository;
    private final StationRepository stationRepository;

    @Override
    public void run(String... args) {
        LocalDateTime now = LocalDateTime.now();
        Line line;
        if (lineRepository.existsByCode(LINE_CODE)) {
            line = lineRepository.findByCode(LINE_CODE).orElseThrow();
            log.debug("[MetroLineSeeder] line {} already exists (id={})", LINE_CODE, line.getId());
        } else {
            line = lineRepository.save(Line.builder()
                    .code(LINE_CODE)
                    .name("Line M1")
                    .color("#E4002B")
                    .totalStations(0)
                    .isActive(true)
                    .createdAt(now)
                    .build());
            log.info("[MetroLineSeeder] inserted line {}", LINE_CODE);
        }

        int inserted = 0;
        int skipped = 0;
        List<StationSeed> seeds = stationSeeds();
        for (StationSeed s : seeds) {
            if (stationRepository.existsByCode(s.code())) {
                skipped++;
                continue;
            }
            Station st = Station.builder()
                    .lineId(line.getId())
                    .code(s.code())
                    .name(s.name())
                    .sequence(s.sequence())
                    .description(s.description())
                    .historicalInfo(null)
                    .latitude(s.lat())
                    .longitude(s.lon())
                    .nfcTagId(s.nfc())
                    .qrCodeToken(s.qr())
                    .collectorCount(0)
                    .isActive(true)
                    .createdAt(now)
                    .build();
            stationRepository.save(st);
            inserted++;
        }

        int activeOnLine = (int) stationRepository.findAllByLineIdAndIsActive(line.getId(), true).size();
        line.setTotalStations(activeOnLine);
        line.setUpdatedAt(now);
        lineRepository.save(line);

        log.info("[MetroLineSeeder] summary line={} stationsInserted={} stationsSkippedAlready={} totalActiveOnLine={}",
                LINE_CODE, inserted, skipped, activeOnLine);
    }

    private static List<StationSeed> stationSeeds() {
        List<StationSeed> list = new ArrayList<>();
        String[] names = {
                "Ga Ngã Tư Sở", "Ga Hàng Xanh", "Ga Cầu Giấy", "Ga Kim Mã", "Ga Lê Đại Hành",
                "Ga Thành Công", "Ga Cát Linh", "Ga Yên Hòa", "Ga Phạm Hùng", "Ga Mỹ Đình",
                "Ga Đình Công", "Ga Phùng Khoang", "Ga Hà Đông", "Ga Yên Nghĩa"
        };
        BigDecimal baseLat = new BigDecimal("21.0285");
        BigDecimal baseLon = new BigDecimal("105.8542");
        for (int i = 0; i < 14; i++) {
            int seq = i + 1;
            String code = String.format("M1-S%02d", seq);
            BigDecimal lat = baseLat.add(new BigDecimal("0.001").multiply(BigDecimal.valueOf(i)));
            BigDecimal lon = baseLon.add(new BigDecimal("0.001").multiply(BigDecimal.valueOf(i)));
            list.add(new StationSeed(
                    code,
                    names[i],
                    seq,
                    "Seeded station " + seq,
                    lat,
                    lon,
                    "M1-NFC-" + String.format("%03d", seq),
                    "M1-QR-" + String.format("%03d", seq)
            ));
        }
        return list;
    }

    private record StationSeed(
            String code,
            String name,
            int sequence,
            String description,
            BigDecimal lat,
            BigDecimal lon,
            String nfc,
            String qr
    ) {}
}
