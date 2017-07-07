package digital.container.util;

import digital.container.storage.domain.model.file.LocalFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class SaveLocalFile {

    private static final Logger LOG = LoggerFactory.getLogger(SaveLocalFile.class);

    private SaveLocalFile(){}

    public static void saveFile(File folder, String fileName, InputStream inputStream) {
        try(FileOutputStream fos = new FileOutputStream(new File(folder, fileName));
            InputStream is = inputStream) {

            while (is.available() > 0) {
                byte buffer[] = new byte[is.available() > LocalFile.BUFFER_SIZE ? LocalFile.BUFFER_SIZE : is.available()];
                is.read(buffer);
                fos.write(buffer);
            }

        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }
}
