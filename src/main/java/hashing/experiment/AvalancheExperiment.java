package hashing.experiment;

import hashing.HashFunction;
import hashing.HashFunctionV01;
import hashing.experiment.support.HashDifferenceCalculator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Random;

/**
 * Implements experiment 6 by measuring hash differences after changing one ASCII character in each input.
 */
public final class AvalancheExperiment {

    private static final Path RESULTS_FILE = Path.of("data", "results", "v0.1", "avalanche.csv");
    private static final Path HISTOGRAM_FILE = Path.of("data", "results", "v0.1", "avalanche-histogram.csv");

    private static final long RANDOM_SEED = 20260924L;
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int[] INPUT_LENGTHS = { 10, 100, 500, 1000 };
    private static final int PAIRS_PER_LENGTH = 25_000;
    private static final int HASH_BIT_COUNT = 256;
    private static final int HASH_HEX_COUNT = 64;

    private final HashFunction hashFunction;

    public AvalancheExperiment(HashFunction hashFunction) {
        this.hashFunction = hashFunction;
    }

    public static void main(String[] args) throws IOException {
        AvalancheExperiment experiment = new AvalancheExperiment(new HashFunctionV01());

        experiment.run();
        System.out.printf("Avalanche experiment completed. Results: %s%n", RESULTS_FILE);
    }

    public void run() throws IOException {
        Random random = new Random(RANDOM_SEED);
        Statistics combinedStatistics = new Statistics();
        StringBuilder resultsCsv = new StringBuilder("version,input_length,pair_count,min_bit_percent,max_bit_percent,"
                + "mean_bit_percent,min_hex_percent,max_hex_percent," + "mean_hex_percent,seed,alphabet\n");

        for (int inputLength : INPUT_LENGTHS) {
            Statistics statistics = checkLength(inputLength, random, combinedStatistics);
            appendResult(resultsCsv, Integer.toString(inputLength), statistics);
        }

        writeResults(resultsCsv, combinedStatistics);
    }

    private Statistics checkLength(int inputLength, Random random, Statistics combinedStatistics) {
        Statistics statistics = new Statistics();

        for (int pair = 0; pair < PAIRS_PER_LENGTH; pair++) {
            byte[] originalInput = randomAscii(inputLength, random);
            byte[] changedInput = originalInput.clone();
            int changedPosition = random.nextInt(inputLength);

            byte replacement;
            do {
                replacement = (byte) ALPHABET.charAt(random.nextInt(ALPHABET.length()));
            } while (replacement == originalInput[changedPosition]);

            changedInput[changedPosition] = replacement;

            String originalHash = hashFunction.hash(originalInput);
            String changedHash = hashFunction.hash(changedInput);
            int differingBits = HashDifferenceCalculator.countDifferingBits(originalHash, changedHash);
            int differingHexCharacters = HashDifferenceCalculator.countDifferingHexCharacters(originalHash,
                    changedHash);

            statistics.add(differingBits, differingHexCharacters);
            combinedStatistics.add(differingBits, differingHexCharacters);
        }

        return statistics;
    }

    private static byte[] randomAscii(int length, Random random) {
        byte[] input = new byte[length];

        for (int index = 0; index < length; index++) {
            input[index] = (byte) ALPHABET.charAt(random.nextInt(ALPHABET.length()));
        }

        return input;
    }

    private static void writeResults(StringBuilder resultsCsv, Statistics combinedStatistics) throws IOException {
        StringBuilder histogramCsv = new StringBuilder("version,input_length,differing_bits,bit_percent,frequency\n");

        appendResult(resultsCsv, "all", combinedStatistics);
        appendHistogram(histogramCsv, combinedStatistics);

        Files.createDirectories(RESULTS_FILE.getParent());
        Files.writeString(RESULTS_FILE, resultsCsv, StandardCharsets.UTF_8);
        Files.writeString(HISTOGRAM_FILE, histogramCsv, StandardCharsets.UTF_8);
    }

    private static void appendResult(StringBuilder csv, String inputLength, Statistics statistics) {
        csv.append(String.format(Locale.ROOT, "v0.1,%s,%d,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%d,%s%n", inputLength,
                statistics.count, bitPercent(statistics.minimumBits), bitPercent(statistics.maximumBits),
                bitPercent(statistics.bitSum / statistics.count), hexPercent(statistics.minimumHexCharacters),
                hexPercent(statistics.maximumHexCharacters), hexPercent(statistics.hexSum / statistics.count),
                RANDOM_SEED, ALPHABET));
    }

    private static void appendHistogram(StringBuilder csv, Statistics statistics) {
        for (int differingBits = 0; differingBits <= HASH_BIT_COUNT; differingBits++) {
            csv.append(String.format(Locale.ROOT, "v0.1,all,%d,%.4f,%d%n", differingBits, bitPercent(differingBits),
                    statistics.bitFrequencies[differingBits]));
        }
    }

    private static double bitPercent(double differingBits) {
        return 100.0 * differingBits / HASH_BIT_COUNT;
    }

    private static double hexPercent(double differingCharacters) {
        return 100.0 * differingCharacters / HASH_HEX_COUNT;
    }

    private static final class Statistics {

        private int count;
        private int minimumBits = HASH_BIT_COUNT;
        private int maximumBits;
        private int minimumHexCharacters = HASH_HEX_COUNT;
        private int maximumHexCharacters;
        private double bitSum;
        private double hexSum;
        private final int[] bitFrequencies = new int[HASH_BIT_COUNT + 1];

        private void add(int differingBits, int differingHexCharacters) {
            count++;
            minimumBits = Math.min(minimumBits, differingBits);
            maximumBits = Math.max(maximumBits, differingBits);
            minimumHexCharacters = Math.min(minimumHexCharacters, differingHexCharacters);
            maximumHexCharacters = Math.max(maximumHexCharacters, differingHexCharacters);
            bitSum += differingBits;
            hexSum += differingHexCharacters;
            bitFrequencies[differingBits]++;
        }
    }

}
