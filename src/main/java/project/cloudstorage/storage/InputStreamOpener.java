package project.cloudstorage.storage;

import java.io.IOException;
import java.io.InputStream;

@FunctionalInterface
public interface InputStreamOpener {

    InputStream open() throws IOException;
}
