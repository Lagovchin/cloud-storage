package project.cloudstorage.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import project.cloudstorage.config.SecurityUser;
import project.cloudstorage.dto.ResourceInfo;

import java.util.List;

@Tag(name = "Resource", description = "File and resource operations")
public interface ResourceApi {

    @Operation(summary = "Upload resource(s)", description = "Upload files or folders via multipart")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Resources uploaded"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "409", description = "Resource already exists"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    ResponseEntity<List<ResourceInfo>> upload(String path, List<MultipartFile> files, SecurityUser user);

    @Operation(summary = "Download resource", description = "Download a file or directory (directory as zip)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resource downloaded"),
            @ApiResponse(responseCode = "400", description = "Invalid path"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    ResponseEntity<StreamingResponseBody> download(String path, SecurityUser user);

    @Operation(summary = "Delete resource", description = "Delete a file or directory")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Resource deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid path"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    void delete(String path, SecurityUser user);

    @Operation(summary = "Move or rename resource", description = "Move or rename a file or directory")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resource moved"),
            @ApiResponse(responseCode = "400", description = "Invalid path"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "409", description = "Target already exists"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    ResponseEntity<ResourceInfo> move(String from, String to, SecurityUser user);

    @Operation(summary = "Get resource info", description = "Return resource metadata")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resource info"),
            @ApiResponse(responseCode = "400", description = "Invalid path"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    ResponseEntity<ResourceInfo> getInfo(String path, SecurityUser user);

    @Operation(summary = "Search resources", description = "Search resources by query")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results"),
            @ApiResponse(responseCode = "400", description = "Invalid query"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    ResponseEntity<List<ResourceInfo>> search(String query, SecurityUser user);
}
