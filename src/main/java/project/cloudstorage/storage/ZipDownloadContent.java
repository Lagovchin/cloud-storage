package project.cloudstorage.storage;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RequiredArgsConstructor
public class ZipDownloadContent implements DownloadContent {

    private final String zipName;
    private final String basePrefix;
    private final List<String> objectKeys;
    private final MinioGateway minio;


    @Override
    public String fileName() {
        return zipName;
    }

    @Override
    public Long contentLength() {
        return null;
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(outputStream)) {
            for (String objectKey : objectKeys) {
                String entryName = objectKey.substring(basePrefix.length());

                if (entryName.isBlank() || entryName.endsWith("/")) {
                    continue;
                }

                zip.putNextEntry(new ZipEntry(entryName));

                try (InputStream inputStream = minio.get(objectKey)) {
                    inputStream.transferTo(zip);
                }
                zip.closeEntry();
            }
            zip.finish();
        } catch (Exception e) {
            throw new IOException("Failed to build zip: " + zipName, e);
        }

    }
}
