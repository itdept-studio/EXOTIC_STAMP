package metro.ExoticStamp.modules.reward.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.reward.application.service.AdminRewardCommandService;
import metro.ExoticStamp.modules.reward.application.service.AdminRewardQueryService;
import metro.ExoticStamp.modules.reward.presentation.mapper.RewardPresentationMapper;
import metro.ExoticStamp.modules.reward.presentation.request.CreateMilestoneRequest;
import metro.ExoticStamp.modules.reward.presentation.request.UpdateMilestoneRequest;
import metro.ExoticStamp.modules.reward.presentation.response.MilestoneResponse;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/milestones")
@RequiredArgsConstructor
@Tag(name = "Admin Milestones")
public class AdminMilestoneController {

    private final AdminRewardQueryService adminRewardQueryService;
    private final AdminRewardCommandService adminRewardCommandService;
    private final RewardPresentationMapper presentationMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List milestones", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PageResponse<MilestoneResponse>>> list(
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size
    ) {
        int s = size != null ? size : 0;
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toMilestonePage(adminRewardQueryService.listMilestones(activeOnly, page, s))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get milestone", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<MilestoneResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toMilestoneResponse(adminRewardQueryService.getMilestone(id))));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create milestone", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<MilestoneResponse>> create(@Valid @RequestBody CreateMilestoneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                presentationMapper.toMilestoneResponse(
                        adminRewardCommandService.createMilestone(presentationMapper.toCreateMilestoneCommand(request)))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update milestone", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<MilestoneResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMilestoneRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toMilestoneResponse(
                        adminRewardCommandService.updateMilestone(presentationMapper.toUpdateMilestoneCommand(id, request)))));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate milestone", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<MilestoneResponse>> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toMilestoneResponse(adminRewardCommandService.activateMilestone(id))));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate milestone", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<MilestoneResponse>> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toMilestoneResponse(adminRewardCommandService.deactivateMilestone(id))));
    }
}
