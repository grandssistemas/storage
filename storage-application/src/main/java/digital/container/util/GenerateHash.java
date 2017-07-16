package digital.container.util;


import java.time.Instant;
import java.util.UUID;

public class GenerateHash {

    private GenerateHash() {}

    public static String generateLocalFile() {
        return generateHash() + "LF";
    }

    public static String generateDatabaseFile() {
        return generateHash() + "DF";
    }

    private static String generateHash() {
        return Instant.now().getEpochSecond()+ UUID.randomUUID().toString().replaceAll("-", "");
    }
}
