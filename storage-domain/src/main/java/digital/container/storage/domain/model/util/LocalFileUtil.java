package digital.container.storage.domain.model.util;


import digital.container.storage.domain.model.file.FileType;
import io.gumga.core.GumgaThreadScope;

import java.time.LocalDate;

public final class LocalFileUtil {

    protected LocalFileUtil() {}

    public static final String DIRECTORY_PATH = System.getProperty("storage.localfile");

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

    public static String getRelativePathFileTAXDOCUMENTCanceled(String containerKey, Integer year, String month, FileType type, String movement) {
        StringBuilder sb = new StringBuilder();
        sb.append(GumgaThreadScope.organizationCode.get());
        sb.append('/' + processContainerKey(containerKey));
        sb.append("/TAX-DOCUMENT/" + year);
        sb.append('/' + month);
        sb.append('/' + type.toString());
        sb.append("/CANCELED");
        sb.append('/' + movement);
        return sb.toString();
//        return getRelativePathFileTAXDOCUMENT(containerKey, year, month, type, movement) + "/CANCELED";
    }

    public static String getRelativePathFileTAXDOCUMENTDisable(String containerKey, Integer year, String month, FileType type) {
        StringBuilder sb = new StringBuilder();
        sb.append(GumgaThreadScope.organizationCode.get());
        sb.append('/' + processContainerKey(containerKey));
        sb.append("/TAX-DOCUMENT/" + year);
        sb.append('/' + month);
        sb.append('/' + type.toString());
        sb.append("/DISABLE");
        return sb.toString();
    }

    public static String getRelativePathFileTAXDOCUMENTLetterCorrection(String containerKey, Integer year, String month, FileType type) {
        StringBuilder sb = new StringBuilder();
        sb.append(GumgaThreadScope.organizationCode.get());
        sb.append('/' + processContainerKey(containerKey));
        sb.append("/TAX-DOCUMENT/" + year);
        sb.append('/' + month);
        sb.append('/' + type.toString());
        sb.append("/LETTER-CORRECTION");
        return sb.toString();
    }

    private static String processContainerKey(String containerKey) {
        String key = containerKey.substring(0, 8);
        return key + '/' + containerKey;
    }
}
