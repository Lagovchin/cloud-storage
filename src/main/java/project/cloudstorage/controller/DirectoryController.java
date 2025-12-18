package project.cloudstorage.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import project.cloudstorage.config.SecurityUser;
import project.cloudstorage.dto.ResourceInfo;
import project.cloudstorage.service.StorageService;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final StorageService storageService;

    @GetMapping
    public ResponseEntity<List<ResourceInfo>> list(@RequestParam(required = false) String path,
                                                   @AuthenticationPrincipal SecurityUser user
    ) {
        return ResponseEntity.ok(storageService.listDirectory(user.getId(), path));
    }

    @PostMapping
    public ResponseEntity<ResourceInfo> createDirectory(@RequestParam String path,
                                                        @AuthenticationPrincipal SecurityUser user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(storageService.createDirectory(user.getId(), path));
    }
}
