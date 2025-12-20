package project.cloudstorage.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceInfo {
    @Schema(description = "Parent directory path", example = "folder1/folder2/")
    private String path;
    @Schema(description = "Resource name", example = "file.txt")
    private String name;
    @Schema(description = "File size in bytes, missing for directories", example = "123")
    private Long size;
    @Schema(description = "Resource type", example = "FILE")
    private ResourceType type;

}
