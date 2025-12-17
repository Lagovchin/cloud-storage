package project.cloudstorage.storage;

import org.springframework.stereotype.Component;

@Component
public class PathNormalizer {

    public String normalizeDirectory(String directoryPath) {
        if (directoryPath == null || directoryPath.isBlank()) {
            return "";
        }

        String normalized = directoryPath.trim().replace("\\", "/");

        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        if (normalized.isBlank()) {
            return "";
        }

        if (!normalized.endsWith("/")) {
            normalized = normalized + "/";
        }

        String[] parts = normalized.split("/");
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            if (part.isBlank() || ".".equals(part) || "..".equals(part)) {
                throw new IllegalArgumentException("Invalid directory path: " + directoryPath);
            }
        }

        return normalized;
    }


    public String normalizeRelativeName(String originalName) {

        String normalized = originalName.replace("\\", "/");

        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        String[] parts = normalized.split("/");

        for (String part : parts) {
            if (part.isBlank() || ".".equals(part) || "..".equals(part)) {
                throw new IllegalArgumentException("Invalid file name: " + originalName);
            }
        }

        return normalized;
    }

    public String userRootPrefix(long userId) {
        return "user-" + userId + "-files/";
    }

    public String removeTrailingSlash(String value) {
        if (value == null) return null;
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    public String parentDirectory(String relativeKey) {
        int lastSlashIndex = relativeKey.lastIndexOf("/");
        if (lastSlashIndex < 0) {
            return "";
        }
        return relativeKey.substring(0, lastSlashIndex + 1);
    }

    public String fileName(String relativeKey) {
        int lastSlashIndex = relativeKey.lastIndexOf("/");
        if (lastSlashIndex < 0) {
            return relativeKey;
        }
        return relativeKey.substring(lastSlashIndex + 1);
    }

    public String normalizePath(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            throw new IllegalArgumentException("Path is missing");
        }

        String normalized = rawPath.replace("\\", "/").trim();

        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Path is missing");
        }

        return normalizeRelativeName(normalized);
    }

    public String normalizeFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("Path is missing");
        }

        String normalized = filePath.trim().replace("\\", "/");

        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        if (normalized.isBlank() || normalized.endsWith("/")) {
            throw new IllegalArgumentException("Invalid file path: " + filePath);
        }

        String[] parts = normalized.split("/");
        for (String part : parts) {
            if (part.isBlank() || ".".equals(part) || "..".equals(part)) {
                throw new IllegalArgumentException("Invalid file path: " + filePath);
            }
        }

        return normalized;
    }

}
