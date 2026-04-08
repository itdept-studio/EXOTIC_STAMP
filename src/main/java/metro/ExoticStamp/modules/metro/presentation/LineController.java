package metro.ExoticStamp.modules.metro.presentation;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.modules.metro.application.LineCommandService;
import metro.ExoticStamp.modules.metro.application.LineQueryService;
import metro.ExoticStamp.modules.metro.presentation.dto.request.CreateLineRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.ToggleStatusRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.UpdateLineRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.response.LineDetailResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.LineResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lines")
@RequiredArgsConstructor
@Tag(name = "Lines")
public class LineController {

    private final LineQueryService lineQueryService;
    private final LineCommandService lineCommandService;

    @GetMapping
    @Operation(summary = "List metro lines")
    public ResponseEntity<ApiResponse<List<LineResponse>>> getAllLines(
            @RequestParam(defaultValue = "true") boolean activeOnly
    ) {
        return ResponseEntity.ok(ApiResponse.ok(lineQueryService.getAllLines(activeOnly)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get metro line with station summaries")
    public ResponseEntity<ApiResponse<LineDetailResponse>> getLineById(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "true") boolean stationsActiveOnly
    ) {
        return ResponseEntity.ok(ApiResponse.ok(lineQueryService.getLineDetail(id, stationsActiveOnly)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a metro line")
    public ResponseEntity<ApiResponse<LineResponse>> createLine(@Valid @RequestBody CreateLineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(lineCommandService.createLine(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a metro line")
    public ResponseEntity<ApiResponse<LineResponse>> updateLine(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLineRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(lineCommandService.updateLine(id, request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle metro line status")
    public ResponseEntity<ApiResponse<LineResponse>> toggleLineStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ToggleStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(lineCommandService.toggleLineStatus(id, request)));
    }
}



