package digital.container.util;


import java.time.Instant;
import java.util.UUID;

public class GenerateHash {

    private GenerateHash() {}

    public static String generate() {
        return Instant.now().getEpochSecond()+ UUID.randomUUID().toString().replaceAll("-", "");
    }
}
