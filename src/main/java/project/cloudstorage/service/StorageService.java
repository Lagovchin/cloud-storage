package project.cloudstorage.service;

import org.springframework.web.multipart.MultipartFile;
import project.cloudstorage.dto.ResourceInfo;
import project.cloudstorage.util.DownloadContent;

import java.util.List;

public interface StorageService {

    List<ResourceInfo> listDirectory(long userId, String path);

    List<ResourceInfo> upload(long userId, String targetDirPath, List<MultipartFile> files);

    DownloadContent download(long userId, String path);

    void delete(long userId, String path);

    ResourceInfo move(long userId, String from, String to);

    ResourceInfo getInfo(long userId, String path);

    ResourceInfo createDirectory(long userId, String path);

    List<ResourceInfo> search(long userId, String query);
}
