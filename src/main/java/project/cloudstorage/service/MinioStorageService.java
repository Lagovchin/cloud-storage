package project.cloudstorage.service;

import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.cloudstorage.dto.ResourceInfo;
import project.cloudstorage.dto.ResourceType;
import project.cloudstorage.exception.ResourceAlreadyExistsException;
import project.cloudstorage.exception.ResourceNotFoundException;
import project.cloudstorage.util.*;

import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MinioStorageService implements StorageService {

    private final MinioGateway minio;
    private final PathNormalizer path;

    @Override
    public List<ResourceInfo> listDirectory(long userId, String directoryPath) {

        String normalizedDirectoryPath = path.normalizeDirectory(directoryPath);
        String userRootPrefix = path.userRootPrefix(userId);

        String prefix = userRootPrefix + normalizedDirectoryPath;

        List<ResourceInfo> result = new ArrayList<>();

        List<Item> items = minio.listDirectory(prefix);

        for (Item item : items) {
            String objectKey = item.objectName();

            if (objectKey.equals(prefix)) {
                continue;
            }

            String relativeName = objectKey.substring(prefix.length());
            boolean looksLikeDirMarker = relativeName.endsWith("/");

            if (item.isDir() || looksLikeDirMarker) {
                String directoryName = path.removeTrailingSlash(relativeName) + "/";

                result.add(new ResourceInfo(
                        normalizedDirectoryPath,
                        directoryName,
                        null,
                        ResourceType.DIRECTORY
                ));
            } else {
                result.add(new ResourceInfo(
                        normalizedDirectoryPath,
                        relativeName,
                        item.size(),
                        ResourceType.FILE
                ));
            }
        }

        return result;
    }

    @Override
    public List<ResourceInfo> upload(long userId, String targetDirectoryPath, List<MultipartFile> files) {

        String normalizedTargetDirectory = path.normalizeDirectory(targetDirectoryPath);
        String userRootPrefix = path.userRootPrefix(userId);

        List<ResourceInfo> uploadedResources = new ArrayList<>();

        for (MultipartFile multipartFile : files) {
            String originalFileName = multipartFile.getOriginalFilename();

            if (originalFileName == null || originalFileName.isBlank()) {
                throw new IllegalArgumentException("File original name is empty");
            }

            String normalizedRelativeName = path.normalizeRelativeName(originalFileName);
            String objectKey = userRootPrefix + normalizedTargetDirectory + normalizedRelativeName;

            if (minio.exists(objectKey)) {
                // ТЗ: 409 если уже существует
                throw new ResourceAlreadyExistsException("Resource already exists: " + normalizedRelativeName);
            }

            try (InputStream inputStream = multipartFile.getInputStream()) {
                minio.put(objectKey, inputStream, multipartFile.getSize(), multipartFile.getContentType());
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload file: " + originalFileName, e);
            }

            String relativeKeyInsideUserRoot = normalizedTargetDirectory + normalizedRelativeName;

            String parentPath = path.parentDirectory(relativeKeyInsideUserRoot);
            String name = path.fileName(relativeKeyInsideUserRoot);

            uploadedResources.add(new ResourceInfo(
                    parentPath,
                    name,
                    multipartFile.getSize(),
                    ResourceType.FILE
            ));
        }

        return uploadedResources;
    }

    @Override
    public DownloadContent download(long userId, String rawPath) {
        String normalizedPath = path.normalizePath(rawPath);
        String userRootPrefix = path.userRootPrefix(userId);
        String fileKey = userRootPrefix + normalizedPath;

        if (minio.exists(fileKey)) {
            String fileName = path.fileName(normalizedPath);
            long size = minio.stat(fileKey).size();

            return new FileDownloadContent(
                    fileName,
                    size,
                    () -> minio.get(fileKey)
            );
        }

        String dirPrefix = userRootPrefix + normalizedPath + "/";

        List<Item> items = minio.listRecursive(dirPrefix);

        List<String> objectKeys = new ArrayList<>();
        for (Item item : items) {
            if (item.isDir()) {
                continue;
            }
            String key = item.objectName();
            if (key.equals(dirPrefix)) {
                continue;
            }
            objectKeys.add(key);
        }

        boolean directoryExists = minio.exists(dirPrefix) || minio.hasAnyObject(dirPrefix);

        if (objectKeys.isEmpty() && !directoryExists) {
            throw new ResourceNotFoundException("Resource not found: " + rawPath);
        }

        String zipName = path.fileName(normalizedPath) + ".zip";
        return new ZipDownloadContent(zipName, dirPrefix, objectKeys, minio);
    }

    @Override
    public void delete(long userId, String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            throw new IllegalArgumentException("Path is empty");
        }

        String userRootPrefix = path.userRootPrefix(userId);
        boolean isDirectory = rawPath.endsWith("/");

        if (isDirectory) {
            String normalizedDir = path.normalizeDirectory(rawPath);

            if (normalizedDir.isBlank()) {
                throw new IllegalArgumentException("Cannot delete root directory");
            }

            String prefix = userRootPrefix + normalizedDir;

            List<Item> items = minio.listRecursive(prefix);

            List<String> keysToDelete = new ArrayList<>();
            for (Item item : items) {
                if (item.isDir()) {
                    continue;
                }
                keysToDelete.add(item.objectName());
            }

            if (keysToDelete.isEmpty()) {
                throw new ResourceNotFoundException("Resource not found: " + rawPath);
            }

            minio.removeAll(keysToDelete);
            return;
        }

        String normalizedPath = path.normalizePath(rawPath);
        String objectKey = userRootPrefix + normalizedPath;

        if (!minio.exists(objectKey)) {
            throw new ResourceNotFoundException("Resource not found: " + rawPath);
        }

        minio.remove(objectKey);
    }

    @Override
    public ResourceInfo move(long userId, String from, String to) {
        if (from == null || from.isBlank()) {
            throw new IllegalArgumentException("Parameter 'from' is required");
        }
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Parameter 'to' is required");
        }

        boolean fromIsDirectory = from.endsWith("/");
        boolean toIsDirectory = to.endsWith("/");

        if (fromIsDirectory != toIsDirectory) {
            throw new IllegalArgumentException("Cannot move directory to file or file to directory");
        }

        String userPrefix = path.userRootPrefix(userId);

        if (!fromIsDirectory) {
            String normalizedFrom = path.normalizePath(from);
            String normalizedTo = path.normalizePath(to);

            String sourceKey = userPrefix + normalizedFrom;
            String targetKey = userPrefix + normalizedTo;

            if (!minio.exists(sourceKey)) {
                throw new ResourceNotFoundException("Resource not found: " + from);
            }

            if (minio.exists(targetKey)) {
                throw new ResourceAlreadyExistsException("Target already exists: " + to);
            }

            minio.copy(sourceKey, targetKey);
            minio.remove(sourceKey);

            String parent = path.parentDirectory(normalizedTo);
            String name = path.fileName(normalizedTo);
            long size = minio.stat(targetKey).size();

            return new ResourceInfo(parent, name, size, ResourceType.FILE);
        }

        String normalizedFromDir = path.normalizeDirectory(from);
        String normalizedToDir = path.normalizeDirectory(to);

        if (normalizedFromDir.isBlank()) {
            throw new IllegalArgumentException("Root directory cannot be moved");
        }

        if (normalizedToDir.startsWith(normalizedFromDir)) {
            throw new IllegalArgumentException("Cannot move directory into itself");
        }

        String sourcePrefix = userPrefix + normalizedFromDir;
        String targetPrefix = userPrefix + normalizedToDir;

        List<Item> sourceItems = minio.listRecursive(sourcePrefix);
        if (sourceItems.isEmpty()) {
            throw new ResourceNotFoundException("Resource not found: " + from);
        }

        List<Item> targetItems = minio.listRecursive(targetPrefix);
        if (!targetItems.isEmpty()) {
            throw new ResourceAlreadyExistsException("Target already exists: " + to);
        }

        List<String> sourceKeys = new ArrayList<>();
        for (Item item : sourceItems) {
            if (item.isDir()) {
                continue;
            }
            sourceKeys.add(item.objectName());
        }

        for (String key : sourceKeys) {
            String relative = key.substring(sourcePrefix.length());
            String targetKey = targetPrefix + relative;
            minio.copy(key, targetKey);
        }

        minio.removeAll(sourceKeys);

        String dirWithoutSlash = path.removeTrailingSlash(normalizedToDir);
        String parent = path.parentDirectory(dirWithoutSlash);
        String name = path.fileName(dirWithoutSlash) + "/";

        return new ResourceInfo(parent, name, null, ResourceType.DIRECTORY);
    }

    @Override
    public ResourceInfo getInfo(long userId, String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            throw new IllegalArgumentException("Path is missing");
        }

        boolean isDirectory = rawPath.endsWith("/");

        String userRoot = path.userRootPrefix(userId);

        if (isDirectory) {
            String directory = path.normalizeDirectory(rawPath);

            if (directory.isBlank()) {
                throw new IllegalArgumentException("Invalid directory path: " + rawPath);
            }

            String dirKey = userRoot + directory;

            if (!minio.exists(dirKey) && !minio.hasAnyObject(dirKey)) {
                throw new ResourceNotFoundException("Resource not found: " + directory);
            }

            String withoutTrailingSlash = path.removeTrailingSlash(directory);
            String parentDir = path.parentDirectory(withoutTrailingSlash);
            String name = path.fileName(withoutTrailingSlash) + "/";

            return new ResourceInfo(parentDir, name, null, ResourceType.DIRECTORY);
        }

        String file = path.normalizeFile(rawPath);
        String fileKey = userRoot + file;

        OptionalLong sizeOpt = minio.tryGetObjectSize(fileKey);
        if (sizeOpt.isEmpty()) {
            throw new ResourceNotFoundException("Resource not found: " + file);
        }

        String parentDir = path.parentDirectory(file);
        String name = path.fileName(file);

        return new ResourceInfo(parentDir, name, sizeOpt.getAsLong(), ResourceType.FILE);
    }


    @Override
    public ResourceInfo createDirectory(long userId, String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            throw new IllegalArgumentException("Path is missing");
        }

        String directory = path.normalizeDirectory(rawPath);

        if (directory.isBlank()) {
            throw new IllegalArgumentException("Invalid directory path: " + rawPath);
        }

        String userRoot = path.userRootPrefix(userId);
        String dirKey = userRoot + directory;

        if (minio.exists(dirKey) || minio.hasAnyObject(dirKey)) {
            throw new ResourceAlreadyExistsException("Directory already exists: " + directory);
        }

        String withoutTrailingSlash = path.removeTrailingSlash(directory);
        String parentDir = path.parentDirectory(withoutTrailingSlash);

        if (!parentDir.isBlank()) {
            String parentKey = userRoot + parentDir;
            if (!minio.exists(parentKey) && !minio.hasAnyObject(parentKey)) {
                throw new ResourceNotFoundException("Parent directory does not exist: " + parentDir);
            }
        }

        minio.putEmptyObject(dirKey);

        String name = path.fileName(withoutTrailingSlash) + "/";
        return new ResourceInfo(parentDir, name, null, ResourceType.DIRECTORY);
    }

    @Override
    public List<ResourceInfo> search(long userId, String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Invalid search query");
        }

        String q = query.trim().toLowerCase(Locale.ROOT);

        String userRoot = path.userRootPrefix(userId);

        List<Item> items = minio.listRecursive(userRoot);

        LinkedHashMap<String, ResourceInfo> unique = new LinkedHashMap<>();

        for (Item item : items) {
            String objectName = item.objectName();

            if (!objectName.startsWith(userRoot)) {
                continue;
            }

            String relative = objectName.substring(userRoot.length());

            if (relative.isBlank()) {
                continue;
            }

            boolean isDirectoryMarker = relative.endsWith("/");

            String nameSource = isDirectoryMarker
                    ? path.removeTrailingSlash(relative)
                    : relative;

            String name = path.fileName(nameSource).toLowerCase(Locale.ROOT);

            if (!name.contains(q)) {
                continue;
            }

            ResourceInfo info;
            if (isDirectoryMarker) {
                String withoutSlash = path.removeTrailingSlash(relative);
                String parentDir = path.parentDirectory(withoutSlash);
                String dirName = path.fileName(withoutSlash) + "/";

                info = new ResourceInfo(parentDir, dirName, null, ResourceType.DIRECTORY);
            } else {
                String parentDir = path.parentDirectory(relative);
                String fileName = path.fileName(relative);

                info = new ResourceInfo(parentDir, fileName, item.size(), ResourceType.FILE);
            }

            String key = info.getType() + "|" + info.getPath() + "|" + info.getName();
            unique.putIfAbsent(key, info);
        }

        return new ArrayList<>(unique.values());
    }

}
