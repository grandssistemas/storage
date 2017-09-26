package digital.container.util;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    private ZipUtil() {}

    public static void zipFolder(File file, ZipOutputStream out, String name) throws Exception {
        File[] files = file.listFiles();
        byte[] tmpBuf = new byte[1024];

        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                if(StringUtils.isEmpty(name)) {
                    zipFolder(files[i], out, files[i].getName() + '/');
                } else {
                    zipFolder(files[i], out, name + files[i].getName() + '/');
                }

                continue;
            }
            String fileName = name + files[i].getName();
            FileInputStream in = new FileInputStream(files[i].getAbsolutePath());
//            System.out.println(" Adding: " + fileName);
            out.putNextEntry(new ZipEntry(fileName));
            int len;
            while ((len = in.read(tmpBuf)) > 0) {
                out.write(tmpBuf, 0, len);
            }
            out.closeEntry();
            in.close();
        }
    }

}
