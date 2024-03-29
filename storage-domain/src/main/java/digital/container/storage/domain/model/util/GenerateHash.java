package digital.container.storage.domain.model.util;

import java.time.Instant;
import java.util.UUID;

public class GenerateHash {

    private GenerateHash() {}

    public static String generateLocalFile() {
        return generateHash() + "LF";
    }
    public static String generateAmazonS3() {
        return generateHash() + "S3";
    }
    public static String generateDatabaseFile() {
        return generateHash() + "DF";
    }
    public static String generateDownload() {
        return generateHash() + "DL";
    }

    private static String generateHash() {

        return Instant.now().toEpochMilli() + UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }

}
