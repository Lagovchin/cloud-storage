package project.cloudstorage.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponseDto {
    @Schema(description = "Username", example = "user_1")
    private String username;
}
