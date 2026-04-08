package metro.ExoticStamp.modules.user.application.mapper;

import metro.ExoticStamp.modules.user.application.command.CreateUserCommand;
import metro.ExoticStamp.modules.user.application.command.UpdateUserCommand;
import metro.ExoticStamp.modules.user.domain.model.User;
import metro.ExoticStamp.modules.user.presentation.dto.request.CreateUserRequest;
import metro.ExoticStamp.modules.user.presentation.dto.request.UpdateUserRequest;
import metro.ExoticStamp.modules.user.presentation.dto.response.UserResponse;

import java.util.UUID;

// Static utility — không cần @Component
public final class UserAppMapper {

    private UserAppMapper() {}

    public static CreateUserCommand toCreateCommand(CreateUserRequest req) {
        return CreateUserCommand.builder()
                .firstname(req.getFirstname())
                .lastname(req.getLastname())
                .username(req.getUsername())
                .email(req.getEmail())
                .phoneNumber(req.getPhoneNumber())
                .password(req.getPassword())
                .dob(req.getDob())
                .gender(req.isGender()).build();
    }

    public static UpdateUserCommand toUpdateCommand(UUID id, UpdateUserRequest req) {
        return UpdateUserCommand.builder()
                .id(id)
                .firstname(req.getFirstname())
                .lastname(req.getLastname())
                .bio(req.getBio())
                .avatarUrl(req.getAvatarUrl())
                .gender(req.getGender())
                .dob(req.getDob())
                .build();
    }

    public static UserResponse toResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .firstname(u.getFirstname())
                .lastname(u.getLastname())
                .username(u.getUsername())
                .email(u.getEmail())
                .phoneNumber(u.getPhoneNumber())
                .dob(u.getDob())
                .gender(u.isGender())
                .bio(u.getBio())
                .avatarUrl(u.getAvatarUrl())
                .status(u.getStatus() != null ? u.getStatus().name() : null)
                .createdAt(u.getCreatedAt())
                .build();
    }
}