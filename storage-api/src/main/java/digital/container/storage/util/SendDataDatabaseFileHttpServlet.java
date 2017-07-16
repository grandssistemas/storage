package digital.container.storage.util;


import digital.container.storage.domain.model.file.DatabaseFile;
import digital.container.storage.domain.model.file.DatabaseFilePart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SendDataDatabaseFileHttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(SendDataDatabaseFileHttpServlet.class);
    private SendDataDatabaseFileHttpServlet() {}

    public static void send(DatabaseFile databaseFile, HttpServletResponse httpServletResponse) {
        httpServletResponse.reset();

        if(databaseFile.getContentType().contains("pdf")) {
            httpServletResponse.setHeader("Content-disposition","attachment;filename="+databaseFile.getName());
        }

        httpServletResponse.setContentType(MediaType.parseMediaType(databaseFile.getContentType()).getType());
        httpServletResponse.setContentLength(Integer.parseInt(databaseFile.getSize().toString()));

        try (ServletOutputStream fos = httpServletResponse.getOutputStream()) {
            for (DatabaseFilePart databaseFilePart : databaseFile.getParts()) {
                fos.write(databaseFilePart.getRawBytes());
            }
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }

    }
}
