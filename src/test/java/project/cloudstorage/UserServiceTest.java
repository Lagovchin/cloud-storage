package project.cloudstorage;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import project.cloudstorage.config.TestPostgresContainer;
import project.cloudstorage.dto.SignUpRequestDto;
import project.cloudstorage.user.User;
import project.cloudstorage.exception.UserAlreadyExistException;
import project.cloudstorage.user.UserRepository;
import project.cloudstorage.user.UserService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceTest {

    @BeforeAll
    static void beforeAll() {
        TestPostgresContainer.getInstance().start();
    }

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Test
    void testSuccessfulRegistration() {
        SignUpRequestDto req = new SignUpRequestDto();
        req.setUsername("john");
        req.setPassword("pass123");

        User saved = userService.register(req);

        assertNotNull(saved.getId());
        assertEquals("john", saved.getUsername());
        assertNotEquals("pass123", saved.getPassword());
        assertTrue(saved.getPassword().startsWith("$2"));
    }

    @Test
    void testDuplicateUsernameThrowsException() {
        SignUpRequestDto req = new SignUpRequestDto();
        req.setUsername("mike");
        req.setPassword("qwerty");

        userService.register(req);

        assertThrows(
                UserAlreadyExistException.class,
                () -> userService.register(req)
        );
    }

    @Test
    void successfulSignIn() {
        // given — пользователь существует
        userService.register(new SignUpRequestDto("alex", "12345"));

        // when — пробуем залогиниться
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("alex", "12345")
        );

        // then
        assertTrue(auth.isAuthenticated());
        assertEquals("alex", auth.getName());
    }

    @Test
    void wrongPasswordThrowsException() {
        userService.register(new SignUpRequestDto("bob", "secret"));

        assertThrows(
                BadCredentialsException.class,
                () -> authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken("bob", "wrong")
                )
        );
    }

    @Test
    void unknownUserThrowsException() {
        assertThrows(
                BadCredentialsException.class,
                () -> authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken("ghost", "123")
                )
        );
    }
}

