package project.cloudstorage.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.cloudstorage.config.SecurityUser;
import project.cloudstorage.dto.UserResponseDto;

@RestController
@RequestMapping("/api/user/me")
public class UserController {

    @GetMapping
    public ResponseEntity<UserResponseDto> me(@AuthenticationPrincipal SecurityUser user) {
        return ResponseEntity.ok(new UserResponseDto(user.getUsername()));
    }
}
