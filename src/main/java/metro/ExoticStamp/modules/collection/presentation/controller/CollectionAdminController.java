package metro.ExoticStamp.modules.collection.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.model.PageResult;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.modules.collection.application.command.AdminCreateCampaignCommand;
import metro.ExoticStamp.modules.collection.application.command.AdminCreateStampDesignCommand;
import metro.ExoticStamp.modules.collection.application.command.AdminUpdateCampaignCommand;
import metro.ExoticStamp.modules.collection.application.command.AdminUpdateStampDesignCommand;
import metro.ExoticStamp.modules.collection.application.service.CollectionAdminCommandService;
import metro.ExoticStamp.modules.collection.application.service.CollectionAdminQueryService;
import metro.ExoticStamp.modules.collection.application.view.AdminCampaignView;
import metro.ExoticStamp.modules.collection.application.view.AdminStampDesignView;
import metro.ExoticStamp.modules.collection.application.view.CollectionAdminStatsView;
import metro.ExoticStamp.modules.collection.presentation.dto.CreateCampaignRequest;
import metro.ExoticStamp.modules.collection.presentation.dto.CreateStampDesignRequest;
import metro.ExoticStamp.modules.collection.presentation.dto.UpdateCampaignRequest;
import metro.ExoticStamp.modules.collection.presentation.dto.UpdateStampDesignRequest;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/collections")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Collection Admin", description = "Campaigns, stamp designs, and collection operations")
@SecurityRequirement(name = "bearerAuth")
public class CollectionAdminController {

    private final CollectionAdminCommandService collectionAdminCommandService;
    private final CollectionAdminQueryService collectionAdminQueryService;

    @GetMapping("/stats")
    @Operation(summary = "Aggregate collection statistics")
    public ResponseEntity<ApiResponse<CollectionAdminStatsView>> stats() {
        return ResponseEntity.ok(ApiResponse.ok(collectionAdminQueryService.getStats()));
    }

    @GetMapping("/campaigns")
    @Operation(summary = "List campaigns (paginated)")
    public ResponseEntity<ApiResponse<PageResult<AdminCampaignView>>> listCampaigns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(collectionAdminQueryService.listCampaigns(page, size)));
    }

    @GetMapping("/campaigns/{id}")
    @Operation(summary = "Get campaign by id")
    public ResponseEntity<ApiResponse<AdminCampaignView>> getCampaign(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(collectionAdminQueryService.getCampaign(id)));
    }

    @PostMapping("/campaigns")
    @Operation(summary = "Create campaign")
    public ResponseEntity<ApiResponse<AdminCampaignView>> createCampaign(@Valid @RequestBody CreateCampaignRequest request) {
        AdminCreateCampaignCommand cmd = new AdminCreateCampaignCommand(
                request.getLineId(),
                request.getPartnerId(),
                request.getCode(),
                request.getName(),
                request.getDescription(),
                request.getBannerUrl(),
                request.getStartDate(),
                request.getEndDate(),
                request.isActive(),
                request.isDefaultCampaign()
        );
        AdminCampaignView view = collectionAdminCommandService.createCampaign(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(view));
    }

    @PutMapping("/campaigns/{id}")
    @Operation(summary = "Update campaign")
    public ResponseEntity<ApiResponse<AdminCampaignView>> updateCampaign(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCampaignRequest request
    ) {
        AdminUpdateCampaignCommand cmd = new AdminUpdateCampaignCommand(
                id,
                request.getPartnerId(),
                request.getCode(),
                request.getName(),
                request.getDescription(),
                request.getBannerUrl(),
                request.getStartDate(),
                request.getEndDate(),
                request.isActive(),
                request.isDefaultCampaign()
        );
        return ResponseEntity.ok(ApiResponse.ok(collectionAdminCommandService.updateCampaign(cmd)));
    }

    @PatchMapping("/campaigns/{id}/activate")
    @Operation(summary = "Activate campaign")
    public ResponseEntity<ApiResponse<AdminCampaignView>> activateCampaign(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(collectionAdminCommandService.activateCampaign(id)));
    }

    @PatchMapping("/campaigns/{id}/deactivate")
    @Operation(summary = "Deactivate campaign")
    public ResponseEntity<ApiResponse<AdminCampaignView>> deactivateCampaign(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(collectionAdminCommandService.deactivateCampaign(id)));
    }

    @PostMapping("/campaigns/{campaignId}/stations/{stationId}")
    @Operation(summary = "Assign station to campaign")
    public ResponseEntity<ApiResponse<Void>> assignStation(
            @PathVariable UUID campaignId,
            @PathVariable UUID stationId
    ) {
        collectionAdminCommandService.assignStationToCampaign(campaignId, stationId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/campaigns/{campaignId}/stations/{stationId}")
    @Operation(summary = "Remove station from campaign")
    public ResponseEntity<ApiResponse<Void>> removeStation(
            @PathVariable UUID campaignId,
            @PathVariable UUID stationId
    ) {
        collectionAdminCommandService.removeStationFromCampaign(campaignId, stationId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/campaigns/{campaignId}/stamp-designs")
    @Operation(summary = "List stamp designs for a campaign")
    public ResponseEntity<ApiResponse<List<AdminStampDesignView>>> listStampDesigns(@PathVariable UUID campaignId) {
        return ResponseEntity.ok(ApiResponse.ok(collectionAdminQueryService.listStampDesignsForCampaign(campaignId)));
    }

    @GetMapping("/stamp-designs/{id}")
    @Operation(summary = "Get stamp design by id")
    public ResponseEntity<ApiResponse<AdminStampDesignView>> getStampDesign(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(collectionAdminQueryService.getStampDesign(id)));
    }

    @PostMapping("/stamp-designs")
    @Operation(summary = "Create stamp design")
    public ResponseEntity<ApiResponse<AdminStampDesignView>> createStampDesign(@Valid @RequestBody CreateStampDesignRequest request) {
        AdminCreateStampDesignCommand cmd = new AdminCreateStampDesignCommand(
                request.getStationId(),
                request.getCampaignId(),
                request.getName(),
                request.getArtworkUrl(),
                request.getAnimationUrl(),
                request.getSoundUrl(),
                request.isLimited(),
                request.isActive()
        );
        AdminStampDesignView view = collectionAdminCommandService.createStampDesign(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(view));
    }

    @PutMapping("/stamp-designs/{id}")
    @Operation(summary = "Update stamp design")
    public ResponseEntity<ApiResponse<AdminStampDesignView>> updateStampDesign(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStampDesignRequest request
    ) {
        AdminUpdateStampDesignCommand cmd = new AdminUpdateStampDesignCommand(
                id,
                request.getStationId(),
                request.getCampaignId(),
                request.getName(),
                request.getArtworkUrl(),
                request.getAnimationUrl(),
                request.getSoundUrl(),
                request.isLimited(),
                request.isActive()
        );
        return ResponseEntity.ok(ApiResponse.ok(collectionAdminCommandService.updateStampDesign(cmd)));
    }

    @PatchMapping("/stamp-designs/{id}/activate")
    @Operation(summary = "Activate stamp design")
    public ResponseEntity<ApiResponse<AdminStampDesignView>> activateStampDesign(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(collectionAdminCommandService.activateStampDesign(id)));
    }

    @PatchMapping("/stamp-designs/{id}/deactivate")
    @Operation(summary = "Deactivate stamp design")
    public ResponseEntity<ApiResponse<AdminStampDesignView>> deactivateStampDesign(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(collectionAdminCommandService.deactivateStampDesign(id)));
    }
}
