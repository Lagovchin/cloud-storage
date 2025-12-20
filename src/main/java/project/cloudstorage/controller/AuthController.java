package project.cloudstorage.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.cloudstorage.api.AuthApi;
import project.cloudstorage.dto.SignInRequestDto;
import project.cloudstorage.dto.SignUpRequestDto;
import project.cloudstorage.user.UserResponseDto;
import project.cloudstorage.user.User;
import project.cloudstorage.user.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    @PostMapping("/sign-in")
    @Override
    public ResponseEntity<UserResponseDto> signIn(@RequestBody @Valid SignInRequestDto signInRequestDto,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response
    ) {

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(signInRequestDto.getUsername(), signInRequestDto.getPassword());

        Authentication auth = authenticationManager.authenticate(authToken);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        return ResponseEntity.ok(new UserResponseDto(signInRequestDto.getUsername()));
    }

    @PostMapping("/sign-up")
    @Override
    public ResponseEntity<UserResponseDto> signUp(@RequestBody @Valid SignUpRequestDto signUpRequestDto,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response
    ) {

        User user = userService.register(signUpRequestDto);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(signUpRequestDto.getUsername(), signUpRequestDto.getPassword());

        Authentication auth = authenticationManager.authenticate(authToken);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, request, response);

        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponseDto(user.getUsername()));
    }

    @PostMapping("/sign-out")
    @Override
    public ResponseEntity<Void> signOut(HttpServletRequest request, HttpServletResponse response) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        new SecurityContextLogoutHandler().logout(request, response, authentication);

        return ResponseEntity.noContent().build();
    }
}
