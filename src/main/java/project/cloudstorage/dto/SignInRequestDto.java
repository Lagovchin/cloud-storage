package project.cloudstorage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignInRequestDto {
    @Schema(description = "Username", example = "user_1")
    @NotBlank(message = "Username is required")
    @Size(min = 5, max = 20, message = "Username length must be between 5 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9]+[a-zA-Z_0-9]*[a-zA-Z0-9]+$",
            message = "Username must start and end with a letter or digit and contain only letters, digits or underscore")
    private String username;

    @Schema(description = "Password", example = "password")
    @NotBlank(message = "Password is required")
    @Size(min = 5, max = 20, message = "Password length must be between 5 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*(),.?\":{}|<>\\[\\]/`~+=-_';]*$",
            message = "Password contains invalid characters")
    private String password;
}
