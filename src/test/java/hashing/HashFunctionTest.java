package hashing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the basic requirements of the hash function.
 * <p>
 * The tests cover parts of the following assignment experiments:
 * </p>
 * <ul>
 * <li>Experiment 1: empty input and UTF-8 input with non-ASCII characters.</li>
 * <li>Experiment 2: hash length, hex format, and matching text/file input.</li>
 * <li>Experiment 3: deterministic results and no state left between calls.</li>
 * </ul>
 * <p>
 * The full experiments are implemented separately because they also require generated input files, separate program
 * runs, and result collection.
 * </p>
 */
class HashFunctionTest {

    private static final byte[] INPUT_A = "first input".getBytes(StandardCharsets.UTF_8);
    private static final byte[] INPUT_B = "second input".getBytes(StandardCharsets.UTF_8);

    private final HashFunction hashFunction = new HashFunctionV01();

    @TempDir
    Path temporaryDirectory;

    @Test
    void emptyInputReturnsHashWithoutThrowing() {
        String hash = assertDoesNotThrow(() -> hashFunction.hash(new byte[0]));

        assertEquals(64, hash.length());
    }

    @Test
    void outputHasExpectedFormat() {
        String hash = hashFunction.hash(INPUT_A);

        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]{64}"));
    }

    @Test
    void sameBytesProduceSameHashRepeatedly() {
        String expected = hashFunction.hash(INPUT_A);

        for (int repetition = 0; repetition < 5; repetition++) {
            assertEquals(expected, hashFunction.hash(INPUT_A));
        }
    }

    @Test
    void aBASequenceProducesEqualResultsForBothACalls() {
        String firstAHash = hashFunction.hash(INPUT_A);
        hashFunction.hash(INPUT_B);
        String secondAHash = hashFunction.hash(INPUT_A);

        assertEquals(firstAHash, secondAHash);
    }

    @Test
    void reusedInstanceMatchesFreshInstances() {
        List<byte[]> inputs = List.of(new byte[0], INPUT_A, INPUT_B, "third input".getBytes(StandardCharsets.UTF_8));

        for (byte[] input : inputs) {
            String reusedInstanceHash = hashFunction.hash(input);
            String freshInstanceHash = new HashFunctionV01().hash(input);

            assertEquals(freshInstanceHash, reusedInstanceHash);
        }
    }

    @Test
    void acceptsNonAsciiUtf8Input() {
        byte[] utf8Input = "Lietuvių kalba: ąčęėįšųūž".getBytes(StandardCharsets.UTF_8);

        String hash = assertDoesNotThrow(() -> hashFunction.hash(utf8Input));

        assertTrue(hash.matches("[0-9a-f]{64}"));
    }

    @Test
    void textBytesAndFileBytesProduceSameHash() throws IOException {
        String text = "Identical UTF-8 bytes: Žąsis";
        Path inputFile = temporaryDirectory.resolve("input.txt");
        Files.writeString(inputFile, text, StandardCharsets.UTF_8);

        byte[] textBytes = InputLoader.fromText(text);
        byte[] fileBytes = InputLoader.fromFile(inputFile);

        assertEquals(hashFunction.hash(textBytes), hashFunction.hash(fileBytes));
    }

}
