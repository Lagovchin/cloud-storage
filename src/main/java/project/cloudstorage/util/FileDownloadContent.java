package project.cloudstorage.util;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@RequiredArgsConstructor
public class FileDownloadContent implements DownloadContent {

    private final String fileName;
    private final long contentLength;
    private final InputStreamOpener opener;

    @Override
    public String fileName() {
        return fileName;
    }

    @Override
    public Long contentLength() {
        return contentLength;
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        try (InputStream inputStream = opener.open()) {
            inputStream.transferTo(outputStream);
        }   catch (Exception e) {
                throw new IOException("Failed to stream file: " + fileName, e);
        }
    }
}
