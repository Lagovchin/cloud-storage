package project.cloudstorage.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import project.cloudstorage.dto.SignInRequestDto;
import project.cloudstorage.dto.SignUpRequestDto;
import project.cloudstorage.user.UserResponseDto;

@Tag(name = "Auth", description = "Registration, authentication, and logout")
public interface AuthApi {

    @Operation(summary = "Sign in", description = "Authenticate user and create session")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    ResponseEntity<UserResponseDto> signIn(@RequestBody SignInRequestDto signInRequestDto,
                                           HttpServletRequest request,
                                           HttpServletResponse response);

    @Operation(summary = "Sign up", description = "Register user and create session")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Username already taken"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    ResponseEntity<UserResponseDto> signUp(@RequestBody SignUpRequestDto signUpRequestDto,
                                           HttpServletRequest request,
                                           HttpServletResponse response);

    @Operation(summary = "Sign out", description = "Invalidate current session")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Signed out"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    ResponseEntity<Void> signOut(HttpServletRequest request, HttpServletResponse response);
}
