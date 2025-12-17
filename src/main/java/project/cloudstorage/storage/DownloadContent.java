package project.cloudstorage.storage;

import java.io.IOException;
import java.io.OutputStream;

public interface DownloadContent {

    String fileName();

    Long contentLength();

    void writeTo(OutputStream outputStream) throws IOException;
}
