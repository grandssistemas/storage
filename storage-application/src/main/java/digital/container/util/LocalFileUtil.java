package digital.container.util;


import digital.container.storage.domain.model.file.FileType;
import io.gumga.core.GumgaThreadScope;

import java.time.LocalDate;

public class LocalFileUtil {

    private LocalFileUtil() {}

    public static final String DIRECTORY_PATH = System.getProperty("user.home") + "/storage-files";

    public static String getRelativePathFileANYTHING(String containerKey) {
        LocalDate today = LocalDate.now();
        StringBuilder sb = new StringBuilder();
        sb.append(GumgaThreadScope.organizationCode.get());
        sb.append('/' + processContainerKey(containerKey));
        sb.append("/ANYTHING/"+ today.getYear());
        sb.append('/' + today.getMonth().toString());
        return sb.toString();
    }

    public static String getRelativePathFileTAXDOCUMENT(String containerKey, Integer year, String month, FileType type, String movement) {
        StringBuilder sb = new StringBuilder();
        sb.append(GumgaThreadScope.organizationCode.get());
        sb.append('/' + processContainerKey(containerKey));
        sb.append("/TAX-DOCUMENT/" + year);
        sb.append('/' + month);
        sb.append('/' + type.toString());
        sb.append('/' + movement);
        return sb.toString();
    }

    private static String processContainerKey(String containerKey) {
        String key = containerKey.substring(0, 8);
        return key + '/' + containerKey;
    }
}
