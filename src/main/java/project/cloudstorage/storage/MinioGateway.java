package project.cloudstorage.storage;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import project.cloudstorage.config.StorageS3Properties;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

@Component
@RequiredArgsConstructor
public class MinioGateway {

    private final MinioClient minioClient;
    private final StorageS3Properties properties;

    public String bucket() {
        return properties.bucket();
    }

    public List<Item> listDirectory(String prefix) {
        String bucket = bucket();

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(prefix)
                        .delimiter("/")
                        .recursive(false)
                        .build()
        );

        List<Item> items = new ArrayList<>();

        for (Result<Item> result : results) {
            try {
                items.add(result.get());
            } catch (Exception e) {
                throw new RuntimeException("Failed to list objects from MinIO (prefix=" + prefix + ")", e);
            }
        }

        return items;
    }

    public boolean exists(String objectKey) {
        String bucket = bucket();

        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
            return true;
        } catch (ErrorResponseException e) {
            String code = (e.errorResponse() != null) ? e.errorResponse().code() : null;

            if ("NoSuchKey".equals(code) || "NoSuchObject".equals(code)) {
                return false;
            }
            throw new RuntimeException("Failed to stat object: " + objectKey, e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to stat object: " + objectKey, e);
        }
    }

    public void put(String objectKey, InputStream inputStream, long size, String contentType) {
        String bucket = bucket();

        String ct = (contentType == null || contentType.isBlank())
                ? "application/octet-stream"
                : contentType;

        try {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(inputStream, size, -1)
                    .contentType(ct)
                    .build();

            minioClient.putObject(args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to put object to MinIO: " + objectKey, e);
        }
    }

    public List<Item> listRecursive(String prefix) {
        String bucket = bucket();

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(prefix)
                        .recursive(true)
                        .build()
        );

        List<Item> items = new ArrayList<>();
        for (Result<Item> result : results) {
            try {
                items.add(result.get());
            } catch (Exception e) {
                throw new RuntimeException("Failed to list objects from MinIO (prefix=" + prefix + ")", e);
            }
        }
        return items;
    }

    public InputStream get(String objectKey) {
        String bucket = bucket();
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to get object: " + objectKey, e);
        }
    }

    public StatObjectResponse stat(String objectKey) {
        String bucket = bucket();
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to stat object: " + objectKey, e);
        }
    }

    public void remove(String objectKey) {
        String bucket = bucket();
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove object: " + objectKey, e);
        }
    }

    public void removeAll(List<String> objectKeys) {
        if (objectKeys == null || objectKeys.isEmpty()) {
            return;
        }

        try {
            List<DeleteObject> toDelete = objectKeys.stream()
                    .map(DeleteObject::new)
                    .toList();
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(bucket())
                            .objects(toDelete)
                            .build()
            );

            for (Result<DeleteError> result : results) {
                DeleteError deleteError = result.get();
                throw new RuntimeException("MinIO delete error: " + deleteError.objectName() + " - " + deleteError.message());
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove objects batch", e);
        }
    }

    public void copy(String sourceObjectKey, String targetObjectKey) {
        String bucket = bucket();
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucket)
                            .object(targetObjectKey)
                            .source(
                                    CopySource.builder()
                                            .bucket(bucket)
                                            .object(sourceObjectKey)
                                            .build()
                            )
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy object: " + sourceObjectKey + " -> " + targetObjectKey, e);
        }
    }

    public boolean hasAnyObject(String prefix) {
        String bucket = bucket();

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(prefix)
                        .recursive(false)
                        .build()
        );

        for (Result<Item> result : results) {
            try {
                result.get();
                return true;
            } catch (Exception e) {
                throw new RuntimeException("Failed to check objects in MinIO (prefix=" + prefix + ")", e);
            }
        }
        return false;
    }

    public void putEmptyObject(String objectKey) {
        String bucket = bucket();

        byte[] empty = new byte[0];
        ByteArrayInputStream inputStream = new ByteArrayInputStream(empty);

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(inputStream, 0, -1)
                            .contentType("application/octet-stream")
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to create empty object in MinIO (key=" + objectKey + ")", e);
        }
    }

    public OptionalLong tryGetObjectSize(String objectKey) {
        String bucket = bucket();

        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
            return OptionalLong.of(stat.size());
        } catch (ErrorResponseException e) {
            String code = e.errorResponse() != null ? e.errorResponse().code() : null;
            if ("NoSuchKey".equals(code) || "NoSuchObject".equals(code)) {
                return OptionalLong.empty();
            }
            throw new RuntimeException("Failed to stat object in MinIO (key=" + objectKey + ")", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to stat object in MinIO (key=" + objectKey + ")", e);
        }
    }

}
