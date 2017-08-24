package digital.container.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;


public class XMLUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLUtil.class);

    private XMLUtil() {}

    public static String getXml(MultipartFile multipartFile) {
        try(InputStream inputStream = multipartFile.getInputStream()) {
            String xml = IOUtils.toString(inputStream, "UTF8");

            return xml;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return "";
    }
}
