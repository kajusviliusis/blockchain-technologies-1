package hashing.experiment;

import hashing.HashFunction;
import hashing.HashFunctionV01;
import hashing.InputLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Implements experiment 2 by verifying hash length, hexadecimal format, and
 * matching hashes for identical text and file bytes.
 */
public final class CorrectnessExperiment {

        private static final Path INPUT_DIRECTORY = Path.of("data", "inputs");
        private static final Path OUTPUT_FILE = Path.of(
                        "data", "results", "v0.1", "correctness.csv");

        private final HashFunction hashFunction;

        public CorrectnessExperiment(HashFunction hashFunction) {
                this.hashFunction = hashFunction;
        }

        public static void main(String[] args) throws IOException {
                CorrectnessExperiment experiment = new CorrectnessExperiment(
                                new HashFunctionV01());

                int caseCount = experiment.run();
                System.out.printf(
                                "Correctness experiment passed for %d cases. Results: %s%n",
                                caseCount,
                                OUTPUT_FILE);
        }

        public int run() throws IOException {
                StringBuilder csv = new StringBuilder(
                                "case_name,input_mode,byte_count,hash_length,is_lowercase_hex,hash\n");

                addResult(csv, "empty", "file", readFile("empty.txt"));
                addResult(csv, "one_byte_a", "file", readFile("one-byte-a.txt"));
                addResult(csv, "one_byte_b", "file", readFile("one-byte-b.txt"));
                addResult(csv, "utf8_non_ascii", "file", readFile("utf8-non-ascii.txt"));

                byte[] textBytes = InputLoader.fromText("identical text");
                byte[] fileBytes = readFile("text-without-newline.txt");

                if (!Arrays.equals(textBytes, fileBytes)) {
                        throw new IllegalStateException(
                                        "The text and file inputs do not contain identical bytes.");
                }

                String textHash = addResult(csv, "identical_text", "text", textBytes);
                String fileHash = addResult(csv, "identical_text", "file", fileBytes);
                if (!textHash.equals(fileHash)) {
                        throw new IllegalStateException(
                                        "Identical text and file bytes produced different hashes.");
                }

                Files.createDirectories(OUTPUT_FILE.getParent());
                Files.writeString(OUTPUT_FILE, csv, StandardCharsets.UTF_8);
                return 6;
        }

        private static byte[] readFile(String fileName) throws IOException {
                return InputLoader.fromFile(INPUT_DIRECTORY.resolve(fileName));
        }

        private String addResult(
                        StringBuilder csv,
                        String caseName,
                        String inputMode,
                        byte[] input) {
                String hash = hashFunction.hash(input);
                boolean isLowercaseHex = hash.matches("[0-9a-f]+");

                if (hash.length() != 64 || !isLowercaseHex) {
                        throw new IllegalStateException(
                                        "Invalid hash format for case: " + caseName);
                }

                csv.append(caseName).append(',')
                                .append(inputMode).append(',')
                                .append(input.length).append(',')
                                .append(hash.length()).append(',')
                                .append(isLowercaseHex).append(',')
                                .append(hash).append('\n');

                return hash;
        }
}
