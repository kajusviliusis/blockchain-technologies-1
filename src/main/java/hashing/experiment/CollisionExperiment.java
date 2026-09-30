package hashing.experiment;

import hashing.HashFunction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Implements experiment 5 by searching random and structured inputs for hash collisions.
 */
public final class CollisionExperiment {

    private static final Path RESULTS_FILE = Path.of("data", "results", "v0.2", "collisions.csv");
    private static final Path EXAMPLES_FILE = Path.of("data", "results", "v0.2", "collision-examples.csv");

    private static final long RANDOM_SEED = 20260924L;
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int[] INPUT_LENGTHS = { 10, 100, 500, 1000 };
    private static final int PAIR_COUNT = 100_000;
    private static final int MAX_EXAMPLES = 10;

    private final HashFunction hashFunction;

    public CollisionExperiment(HashFunction hashFunction) {
        this.hashFunction = hashFunction;
    }

    public void run() throws IOException {
        Random random = new Random(RANDOM_SEED);
        List<CollisionExample> examples = new ArrayList<>();
        List<CollisionSummary> summaries = new ArrayList<>();

        for (int inputLength : INPUT_LENGTHS) {
            summaries.add(checkRandomInputs(inputLength, random, examples));
        }

        summaries.add(checkStructuredInputs(examples));
        writeResults(summaries, examples);
    }

    private CollisionSummary checkRandomInputs(int inputLength, Random random, List<CollisionExample> examples) {
        Set<String> distinctInputs = new HashSet<>();
        Map<String, String> firstInputByHash = new HashMap<>();
        Set<String> collisionHashes = new HashSet<>();
        int pairwiseCollisionCount = 0;

        for (int pair = 0; pair < PAIR_COUNT; pair++) {
            String firstInput = randomAscii(inputLength, random);
            String secondInput;

            do {
                secondInput = randomAscii(inputLength, random);
            } while (secondInput.equals(firstInput));

            String firstHash = hashFunction.hash(firstInput.getBytes(StandardCharsets.US_ASCII));
            String secondHash = hashFunction.hash(secondInput.getBytes(StandardCharsets.US_ASCII));

            if (firstHash.equals(secondHash)) {
                pairwiseCollisionCount++;
            }

            inspectInput(inputLength, firstInput, firstHash, distinctInputs, firstInputByHash, collisionHashes,
                    examples);
            inspectInput(inputLength, secondInput, secondHash, distinctInputs, firstInputByHash, collisionHashes,
                    examples);
        }

        return new CollisionSummary("random", inputLength, PAIR_COUNT, distinctInputs.size(), pairwiseCollisionCount,
                collisionHashes.size());
    }

    private CollisionSummary checkStructuredInputs(List<CollisionExample> examples) {
        String[][] pairs = { { "aabbccddeeff", "fedcbafedcba" }, { "aaaaaaaaaaaa", "abababababab" } };

        Set<String> distinctInputs = new HashSet<>();
        Map<String, String> firstInputByHash = new HashMap<>();
        Set<String> collisionHashes = new HashSet<>();
        int pairwiseCollisionCount = 0;

        for (String[] pair : pairs) {
            String firstHash = hashFunction.hash(pair[0].getBytes(StandardCharsets.US_ASCII));
            String secondHash = hashFunction.hash(pair[1].getBytes(StandardCharsets.US_ASCII));

            if (firstHash.equals(secondHash)) {
                pairwiseCollisionCount++;
            }

            inspectInput(pair[0].length(), pair[0], firstHash, distinctInputs, firstInputByHash, collisionHashes,
                    examples);
            inspectInput(pair[1].length(), pair[1], secondHash, distinctInputs, firstInputByHash, collisionHashes,
                    examples);
        }

        return new CollisionSummary("structured", 12, pairs.length, distinctInputs.size(), pairwiseCollisionCount,
                collisionHashes.size());
    }

    // Checks one distinct input against hashes already seen in the dataset.
    private static void inspectInput(int inputLength, String input, String hash, Set<String> distinctInputs,
            Map<String, String> firstInputByHash, Set<String> collisionHashes, List<CollisionExample> examples) {
        if (!distinctInputs.add(input)) {
            return;
        }

        String previousInput = firstInputByHash.putIfAbsent(hash, input);
        if (previousInput != null && collisionHashes.add(hash) && examples.size() < MAX_EXAMPLES) {
            examples.add(new CollisionExample(inputLength, hash, previousInput, input));
        }
    }

    private static String randomAscii(int length, Random random) {
        StringBuilder input = new StringBuilder(length);

        for (int index = 0; index < length; index++) {
            input.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }

        return input.toString();
    }

    private static void writeResults(List<CollisionSummary> summaries, List<CollisionExample> examples)
            throws IOException {
        StringBuilder summaryCsv = new StringBuilder("version,dataset,input_length,pair_count,distinct_input_count,"
                + "pairwise_collision_count,collision_group_count,seed,alphabet\n");

        for (CollisionSummary summary : summaries) {
            summaryCsv.append("v0.2,").append(summary.dataset()).append(',').append(summary.inputLength()).append(',')
                    .append(summary.pairCount()).append(',').append(summary.distinctInputCount()).append(',')
                    .append(summary.pairwiseCollisionCount()).append(',').append(summary.collisionGroupCount())
                    .append(',').append(summary.dataset().equals("random") ? RANDOM_SEED : "").append(',')
                    .append(summary.dataset().equals("random") ? ALPHABET : "").append('\n');
        }

        StringBuilder examplesCsv = new StringBuilder("version,input_length,hash,first_input,second_input\n");

        for (CollisionExample example : examples) {
            examplesCsv.append("v0.2,").append(example.inputLength()).append(',').append(example.hash()).append(',')
                    .append(example.firstInput()).append(',').append(example.secondInput()).append('\n');
        }

        Files.createDirectories(RESULTS_FILE.getParent());
        Files.writeString(RESULTS_FILE, summaryCsv, StandardCharsets.UTF_8);
        Files.writeString(EXAMPLES_FILE, examplesCsv, StandardCharsets.UTF_8);
    }

    private record CollisionSummary(String dataset, int inputLength, int pairCount, int distinctInputCount,
            int pairwiseCollisionCount, int collisionGroupCount) {
    }

    private record CollisionExample(int inputLength, String hash, String firstInput, String secondInput) {
    }
}
