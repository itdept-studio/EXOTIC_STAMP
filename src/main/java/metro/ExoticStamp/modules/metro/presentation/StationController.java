package metro.ExoticStamp.modules.metro.presentation;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.modules.metro.application.StationCommandService;
import metro.ExoticStamp.modules.metro.application.StationQueryService;
import metro.ExoticStamp.modules.metro.presentation.dto.request.CreateStationRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.RotateQrTokenRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.UpdateStationRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationDetailResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationImageUploadResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationStatsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stations")
@RequiredArgsConstructor
@Tag(name = "Stations")
public class StationController {

    private final StationQueryService stationQueryService;
    private final StationCommandService stationCommandService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get station scan statistics")
    public ResponseEntity<ApiResponse<List<StationStatsResponse>>> stationStats() {
        return ResponseEntity.ok(ApiResponse.ok(stationQueryService.stationStats()));
    }

    @GetMapping
    @Operation(summary = "List stations")
    public ResponseEntity<ApiResponse<List<StationResponse>>> listStations(
            @RequestParam(required = false) UUID lineId,
            @RequestParam(defaultValue = "true") boolean activeOnly
    ) {
        return ResponseEntity.ok(ApiResponse.ok(stationQueryService.listStations(lineId, activeOnly)));
    }

    @GetMapping("/nfc/{nfcTagId}")
    @Operation(summary = "Resolve station by NFC tag")
    public ResponseEntity<ApiResponse<StationDetailResponse>> resolveStationByNfc(@PathVariable String nfcTagId) {
        return ResponseEntity.ok(ApiResponse.ok(stationQueryService.resolveStationByNfc(nfcTagId)));
    }

    @GetMapping("/qr/{qrToken}")
    @Operation(summary = "Resolve station by QR token")
    public ResponseEntity<ApiResponse<StationDetailResponse>> resolveStationByQr(@PathVariable String qrToken) {
        return ResponseEntity.ok(ApiResponse.ok(stationQueryService.resolveStationByQr(qrToken)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get station detail")
    public ResponseEntity<ApiResponse<StationDetailResponse>> getStationById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(stationQueryService.getStationDetailById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a station")
    public ResponseEntity<ApiResponse<StationDetailResponse>> createStation(@Valid @RequestBody CreateStationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(stationCommandService.createStation(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update station details")
    public ResponseEntity<ApiResponse<StationDetailResponse>> updateStation(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(stationCommandService.updateStation(id, request)));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate station")
    public ResponseEntity<ApiResponse<StationDetailResponse>> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(stationCommandService.activateStation(id)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate station")
    public ResponseEntity<ApiResponse<StationDetailResponse>> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(stationCommandService.deactivateStation(id)));
    }

    @PatchMapping("/{id}/rotate-qr")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Rotate station QR token")
    public ResponseEntity<ApiResponse<StationDetailResponse>> rotateQr(
            @PathVariable UUID id,
            @Valid @RequestBody RotateQrTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(stationCommandService.rotateQrToken(id, request)));
    }

    @PatchMapping("/{id}/collector-count")
    @PreAuthorize("hasAuthority('INTERNAL')")
    @Operation(summary = "Increment collector count (internal)")
    public ResponseEntity<ApiResponse<Void>> incrementCollectorCount(@PathVariable UUID id) {
        stationCommandService.incrementCollectorCount(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete station")
    public ResponseEntity<Void> softDeleteStation(@PathVariable UUID id) {
        stationCommandService.softDeleteStation(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Upload station image")
    public ResponseEntity<ApiResponse<StationImageUploadResponse>> uploadStationImage(
            @PathVariable UUID id,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.ok(stationCommandService.uploadStationImage(id, file)));
    }
}



