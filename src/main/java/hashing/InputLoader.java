package hashing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class InputLoader {

    private InputLoader() {
    }

    public static byte[] fromText(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] fromFile(Path path) throws IOException {
        return Files.readAllBytes(path);
    }
}