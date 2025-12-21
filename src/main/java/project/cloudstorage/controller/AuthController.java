package project.cloudstorage.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.cloudstorage.api.AuthApi;
import project.cloudstorage.dto.SignInRequestDto;
import project.cloudstorage.dto.SignUpRequestDto;
import project.cloudstorage.service.AuthenticationService;
import project.cloudstorage.user.UserResponseDto;
import project.cloudstorage.user.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @PostMapping("/sign-in")
    @Override
    public ResponseEntity<UserResponseDto> signIn(@RequestBody @Valid SignInRequestDto signInRequestDto,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response
    ) {
        Authentication auth = authenticationService.authenticate(signInRequestDto.getUsername(),
                signInRequestDto.getPassword(),
                request, response);

        return ResponseEntity.ok(new UserResponseDto(auth.getName()));
    }

    @PostMapping("/sign-up")
    @Override
    public ResponseEntity<UserResponseDto> signUp(@RequestBody @Valid SignUpRequestDto signUpRequestDto,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response
    ) {

        userService.register(signUpRequestDto);

        Authentication auth = authenticationService.authenticate(signUpRequestDto.getUsername(),
                signUpRequestDto.getPassword(),
                request, response);

        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponseDto(auth.getName()));
    }

    @PostMapping("/sign-out")
    @Override
    public ResponseEntity<Void> signOut(HttpServletRequest request, HttpServletResponse response) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        new SecurityContextLogoutHandler().logout(request, response, authentication);

        return ResponseEntity.noContent().build();
    }
}
