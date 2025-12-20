package project.cloudstorage.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import project.cloudstorage.config.SecurityUser;
import project.cloudstorage.user.UserResponseDto;

@Tag(name = "User", description = "Current user")
public interface UserApi {

    @Operation(summary = "Get current user", description = "Returns the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current user"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    ResponseEntity<UserResponseDto> me(SecurityUser user);
}