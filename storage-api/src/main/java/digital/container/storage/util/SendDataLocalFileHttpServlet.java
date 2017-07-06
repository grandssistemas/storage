package digital.container.storage.util;


import digital.container.storage.domain.model.LocalFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

public class SendDataLocalFileHttpServlet {
    private static final String DIRECTORY_PATH = System.getProperty("user.home") + "/storage-files";
    private static final Logger LOG = LoggerFactory.getLogger(SendDataDatabaseFileHttpServlet.class);

    private SendDataLocalFileHttpServlet() {}

    public static void send(LocalFile file, HttpServletResponse httpServletResponse) {
        httpServletResponse.reset();
        httpServletResponse.setContentType(MediaType.parseMediaType(file.getContentType()).getType());
        httpServletResponse.setContentLength(Integer.parseInt(file.getSize().toString()));

        try(FileInputStream fis = new FileInputStream(new File(DIRECTORY_PATH + "/" + file.getRelativePath()));
            ServletOutputStream fos = httpServletResponse.getOutputStream();) {

            while (fis.available() > 0) {
                byte buffer[] = new byte[fis.available() > LocalFile.BUFFER_SIZE ? LocalFile.BUFFER_SIZE : fis.available()];
                int nBytes = fis.read(buffer);
                fos.write(buffer);
            }
        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }

    }

}
