package metro.ExoticStamp.modules.user.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import metro.ExoticStamp.modules.user.application.UserCommandService;
import metro.ExoticStamp.modules.user.application.UserQueryService;
import metro.ExoticStamp.modules.user.presentation.dto.request.CreateUserRequest;
import metro.ExoticStamp.modules.user.presentation.dto.request.UpdateUserRequest;
import metro.ExoticStamp.modules.user.presentation.dto.response.UserResponse;
import metro.ExoticStamp.modules.user.presentation.mapper.UserPresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User")
public class UserController {

    private final UserCommandService commandService;
    private final UserQueryService queryService;

    @GetMapping("/{id}")
    @Operation(summary = "Get user by id")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(UserPresentationMapper.toResponse(queryService.getById(id)));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user")
    public ResponseEntity<UserResponse> getMe(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                UserPresentationMapper.toResponse(queryService.getByUsername(principal.getUsername())));
    }

    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody CreateUserRequest req) {
        UserResponse response = UserPresentationMapper.toResponse(
                commandService.createUser(UserPresentationMapper.toCreateCommand(req)));
        return ResponseEntity.status(201)
                .body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a user")
    public ResponseEntity<UserResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest req) {
        return ResponseEntity.ok(UserPresentationMapper.toResponse(commandService.updateUser(
                UserPresentationMapper.toUpdateCommand(id, req))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a user")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        commandService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
