package project.cloudstorage.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import project.cloudstorage.api.ResourceApi;
import project.cloudstorage.config.SecurityUser;
import project.cloudstorage.util.DownloadContent;
import project.cloudstorage.dto.ResourceInfo;
import project.cloudstorage.service.StorageService;

import java.util.List;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController implements ResourceApi {

    private final StorageService storageService;

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public ResponseEntity<List<ResourceInfo>> upload(@RequestParam(value = "path", required = false) String path,
                                                     @RequestPart(name = "object") List<MultipartFile> files,
                                                     @AuthenticationPrincipal SecurityUser user
    ) {
        List<ResourceInfo> uploaded = storageService.upload(user.getId(), path, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(uploaded);
    }

    @GetMapping(
            value = "/download",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @Override
    public ResponseEntity<StreamingResponseBody> download(@RequestParam String path,
                                                          @AuthenticationPrincipal SecurityUser user
    ) {
        DownloadContent content = storageService.download(user.getId(), path);
        StreamingResponseBody body = content::writeTo;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(content.fileName())
                .build());

        return ResponseEntity.ok().headers(headers).body(body);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void delete(@RequestParam String path, @AuthenticationPrincipal SecurityUser user) {
        storageService.delete(user.getId(), path);
    }

    @GetMapping("/move")
    @Override
    public ResponseEntity<ResourceInfo> move(@RequestParam String from,
                                             @RequestParam String to,
                                             @AuthenticationPrincipal SecurityUser user
    ) {
        return ResponseEntity.ok(storageService.move(user.getId(), from, to));
    }

    @GetMapping
    @Override
    public ResponseEntity<ResourceInfo> getInfo(@RequestParam String path, @AuthenticationPrincipal SecurityUser user) {
        return ResponseEntity.ok(storageService.getInfo(user.getId(), path));
    }

    @GetMapping("/search")
    @Override
    public ResponseEntity<List<ResourceInfo>> search(@RequestParam String query,
                                                     @AuthenticationPrincipal SecurityUser user
    ) {
        return ResponseEntity.ok(storageService.search(user.getId(), query));
    }
}
