package digital.container.storage.util;


import digital.container.storage.domain.model.file.amazon.AmazonS3File;
import digital.container.storage.domain.model.file.local.LocalFile;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SendDataAmazonS3FileHttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(SendDataAmazonS3FileHttpServlet.class);
    public static void send(AmazonS3File s3File,
                            File file,
                            HttpServletResponse httpServletResponse,
                            Boolean download) {
        httpServletResponse.reset();
        if(s3File.getContentType().contains("pdf") || download) {
            httpServletResponse.setHeader("Content-disposition","attachment;filename="+file.getName());
        } else {
            httpServletResponse.setHeader("Content-disposition","filename="+file.getName());
        }
        httpServletResponse.setContentType(MediaType.parseMediaType(s3File.getContentType()).getType());
        httpServletResponse.setContentLength(Integer.parseInt(s3File.getSize().toString()));

        try(FileInputStream fis = new FileInputStream(file);
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
        } finally {
            try {
                FileUtils.deleteDirectory(new File(file.getParent()));
            } catch (IOException e) {
                e.printStackTrace();
            }
//            Paths.get(file.getPath()).toFile().delete();
//            file.delete();
        }

    }
}
