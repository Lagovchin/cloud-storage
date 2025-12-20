package project.cloudstorage.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import project.cloudstorage.config.SecurityUser;
import project.cloudstorage.dto.ResourceInfo;

import java.util.List;

@Tag(name = "Directory", description = "Directory operations")
public interface DirectoryApi {

    @Operation(summary = "List directory", description = "List resources in a directory (non-recursive)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Directory contents"),
            @ApiResponse(responseCode = "400", description = "Invalid path"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Directory not found"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    ResponseEntity<List<ResourceInfo>> list(String path, SecurityUser user);

    @Operation(summary = "Create directory", description = "Create an empty directory")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Directory created"),
            @ApiResponse(responseCode = "400", description = "Invalid path"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Parent directory not found"),
            @ApiResponse(responseCode = "409", description = "Directory already exists"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    ResponseEntity<ResourceInfo> createDirectory(String path, SecurityUser user);
}
