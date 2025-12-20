package project.cloudstorage.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.cloudstorage.api.UserApi;
import project.cloudstorage.config.SecurityUser;

@RestController
@RequestMapping("/api/user/me")
public class UserController implements UserApi {

    @GetMapping
    @Override
    public ResponseEntity<UserResponseDto> me(@AuthenticationPrincipal SecurityUser user) {
        return ResponseEntity.ok(new UserResponseDto(user.getUsername()));
    }
}
