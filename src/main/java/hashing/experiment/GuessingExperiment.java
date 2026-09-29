package hashing.experiment;

import hashing.HashFunction;
import hashing.HashFunctionV01;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Implements experiment 7 by searching all four-digit candidates with and without a public salt.
 */
public final class GuessingExperiment {

    private static final Path RESULTS_FILE = Path.of("data", "results", "v0.1", "guessing.csv");

    private static final String TARGET_INPUT = "0427";
    private static final int CANDIDATE_COUNT = 10_000;
    private static final long SALT_SEED = 20260924L;
    private static final int SALT_LENGTH = 16;

    private final HashFunction hashFunction;

    public GuessingExperiment(HashFunction hashFunction) {
        this.hashFunction = hashFunction;
    }

    public static void main(String[] args) throws IOException {
        GuessingExperiment experiment = new GuessingExperiment(new HashFunctionV01());

        experiment.run();
        System.out.printf("Guessing experiment completed. Results: %s%n", RESULTS_FILE);
    }

    public void run() throws IOException {
        List<String> candidates = createCandidates();
        byte[] targetBytes = TARGET_INPUT.getBytes(StandardCharsets.US_ASCII);

        String unsaltedTargetHash = hashFunction.hash(targetBytes);
        SearchResult unsaltedResult = search("no_salt", unsaltedTargetHash, candidates, new byte[0]);

        byte[] salt = createSalt();
        String saltedTargetHash = hashFunction.hash(append(targetBytes, salt));
        SearchResult saltedResult = search("public_salt", saltedTargetHash, candidates, salt);

        writeResults(List.of(unsaltedResult, saltedResult));
    }

    private SearchResult search(String mode, String targetHash, List<String> candidates, byte[] salt) {
        List<String> matches = new ArrayList<>();
        int attemptCount = 0;

        long start = System.nanoTime();
        for (String candidate : candidates) {
            byte[] candidateBytes = candidate.getBytes(StandardCharsets.US_ASCII);
            byte[] hashInput = salt.length == 0 ? candidateBytes : append(candidateBytes, salt);
            String candidateHash = hashFunction.hash(hashInput);
            attemptCount++;

            if (candidateHash.equals(targetHash)) {
                matches.add(candidate);
            }
        }
        long elapsedNanoseconds = System.nanoTime() - start;

        return new SearchResult(mode, targetHash, HexFormat.of().formatHex(salt), candidates.size(), attemptCount,
                matches, elapsedNanoseconds);
    }

    private static List<String> createCandidates() {
        List<String> candidates = new ArrayList<>(CANDIDATE_COUNT);

        for (int number = 0; number < CANDIDATE_COUNT; number++) {
            candidates.add(String.format(Locale.ROOT, "%04d", number));
        }

        return candidates;
    }

    private static byte[] createSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new Random(SALT_SEED).nextBytes(salt);
        return salt;
    }

    // Appends the salt as raw bytes, not hex.
    private static byte[] append(byte[] input, byte[] salt) {
        byte[] combined = Arrays.copyOf(input, input.length + salt.length);
        System.arraycopy(salt, 0, combined, input.length, salt.length);
        return combined;
    }

    private static void writeResults(List<SearchResult> results) throws IOException {
        StringBuilder csv = new StringBuilder("version,mode,target_hash,salt_hex,candidate_count,attempt_count,"
                + "match_count,matches,elapsed_ns\n");

        for (SearchResult result : results) {
            csv.append("v0.1,").append(result.mode()).append(',').append(result.targetHash()).append(',')
                    .append(result.saltHex()).append(',').append(result.candidateCount()).append(',')
                    .append(result.attemptCount()).append(',').append(result.matches().size()).append(',')
                    .append(String.join(";", result.matches())).append(',').append(result.elapsedNanoseconds())
                    .append('\n');
        }

        Files.createDirectories(RESULTS_FILE.getParent());
        Files.writeString(RESULTS_FILE, csv, StandardCharsets.UTF_8);
    }

    private record SearchResult(String mode, String targetHash, String saltHex, int candidateCount, int attemptCount,
            List<String> matches, long elapsedNanoseconds) {
    }
}
