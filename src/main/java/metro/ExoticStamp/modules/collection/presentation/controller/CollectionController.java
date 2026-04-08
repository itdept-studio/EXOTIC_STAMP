package metro.ExoticStamp.modules.collection.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.modules.collection.application.command.CollectStampCommand;
import metro.ExoticStamp.modules.collection.application.mapper.UserStampAppMapper;
import metro.ExoticStamp.modules.collection.application.service.CollectionCommandService;
import metro.ExoticStamp.modules.collection.application.service.CollectionQueryService;
import metro.ExoticStamp.modules.collection.presentation.request.CollectStampRequest;
import metro.ExoticStamp.modules.collection.presentation.response.ProgressResponse;
import metro.ExoticStamp.modules.collection.presentation.response.StampBookResponse;
import metro.ExoticStamp.modules.collection.presentation.response.StampCollectResponse;
import metro.ExoticStamp.modules.collection.presentation.response.UserStampResponse;
import metro.ExoticStamp.modules.user.domain.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/collections")
@RequiredArgsConstructor
@Tag(name = "Collections")
public class CollectionController {

    private final CollectionCommandService commandService;
    private final CollectionQueryService queryService;
    private final UserStampAppMapper userStampAppMapper;

    @PostMapping("/scan")
    @Operation(summary = "Scan station (NFC or QR) and collect stamp", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<StampCollectResponse>> scanAndCollect(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CollectStampRequest req
    ) {
        UUID userId = extractUserId(principal);
        boolean hasNfc = req.getNfcTagId() != null && !req.getNfcTagId().isBlank();
        boolean hasQr = req.getQrToken() != null && !req.getQrToken().isBlank();
        if (!hasNfc && !hasQr) {
            throw new IllegalArgumentException("Either nfcTagId or qrToken is required");
        }
        if (hasNfc && hasQr) {
            throw new IllegalArgumentException("Provide only one of nfcTagId or qrToken");
        }

        CollectStampCommand cmd = new CollectStampCommand(
                userId,
                req.getIdempotencyKey(),
                req.getNfcTagId(),
                req.getQrToken(),
                req.getCampaignId(),
                req.getDeviceFingerprint(),
                req.getLatitude(),
                req.getLongitude(),
                userStampAppMapper.resolveCollectMethod(req.getNfcTagId(), req.getQrToken())
        );

        StampCollectResponse res = commandService.collectStamp(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(res));
    }

    @GetMapping("/me/stamps")
    @Operation(summary = "View my stamps (by line/campaign)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<UserStampResponse>>> myStamps(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam UUID lineId,
            @RequestParam(required = false) UUID campaignId
    ) {
        UUID userId = extractUserId(principal);
        return ResponseEntity.ok(ApiResponse.ok(queryService.getMyStamps(userId, lineId, campaignId)));
    }

    @GetMapping("/me/progress")
    @Operation(summary = "View my progress (% collected per line)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ProgressResponse>> myProgress(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam UUID lineId,
            @RequestParam(required = false) UUID campaignId
    ) {
        UUID userId = extractUserId(principal);
        return ResponseEntity.ok(ApiResponse.ok(queryService.getMyProgress(userId, lineId, campaignId)));
    }

    @GetMapping("/me/history")
    @Operation(summary = "View my recent stamp collection history", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<UserStampResponse>>> myHistory(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "20") int limit
    ) {
        UUID userId = extractUserId(principal);
        return ResponseEntity.ok(ApiResponse.ok(queryService.getMyHistory(userId, limit)));
    }

    @GetMapping("/me/book")
    @Operation(summary = "View my stamp book (grid of stations)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<StampBookResponse>> myStampBook(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam UUID lineId,
            @RequestParam(required = false) UUID campaignId
    ) {
        UUID userId = extractUserId(principal);
        return ResponseEntity.ok(ApiResponse.ok(queryService.getStampBook(userId, lineId, campaignId)));
    }

    private UUID extractUserId(UserDetails principal) {
        if (principal == null) {
            throw new IllegalStateException("Missing principal");
        }
        if (principal instanceof User u) {
            return u.getId();
        }
        throw new IllegalStateException("Unsupported principal type: " + principal.getClass().getName());
    }
}

