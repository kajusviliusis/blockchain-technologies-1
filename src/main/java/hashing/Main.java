package hashing;

import java.io.IOException;
import java.nio.file.Path;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            printUsage();
            return;
        }

        HashFunction hashFunction = new HashFunctionV01();

        try {
            byte[] input = loadInput(args[0], args[1]);
            System.out.println(hashFunction.hash(input));
        } catch (IOException exception) {
            System.err.println("Could not read file: " + exception.getMessage());
        } catch (IllegalArgumentException exception) {
            System.err.println(exception.getMessage());
            printUsage();
        }
    }

    private static byte[] loadInput(String mode, String value) throws IOException {
        return switch (mode) {
            case "text" -> InputLoader.fromText(value);
            case "file" -> InputLoader.fromFile(Path.of(value));
            default -> throw new IllegalArgumentException("Unknown input mode: " + mode);
        };
    }

    private static void printUsage() {
        System.err.println("Usage: <text|file> <value>");
    }
}