package project.cloudstorage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage.s3")
public record StorageS3Properties(
        String endpoint,
        String accessKey,
        String secretKey,
        String bucket
)
{}
