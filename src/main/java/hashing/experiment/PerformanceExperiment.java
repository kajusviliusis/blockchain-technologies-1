package hashing.experiment;

import hashing.HashFunction;
import hashing.HashFunctionV01;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Implements experiment 4 by measuring hash time for file prefixes containing
 * 1, 2, 4, 8, ... lines and the complete file.
 */
public final class PerformanceExperiment {

    private static final Path INPUT_FILE = Path.of(
            "data", "inputs", "konstitucija.txt");
    private static final Path OUTPUT_FILE = Path.of(
            "data", "results", "v0.1", "performance.csv");

    private static final int WARMUP_COUNT = 2;
    private static final int MEASUREMENT_COUNT = 5;

    private final HashFunction hashFunction;

    public PerformanceExperiment(HashFunction hashFunction) {
        this.hashFunction = hashFunction;
    }

    public static void main(String[] args) throws IOException {
        PerformanceExperiment experiment = new PerformanceExperiment(
                new HashFunctionV01());

        int inputCount = experiment.run();
        System.out.printf(
                "Performance experiment completed for %d input sizes. Results: %s%n",
                inputCount,
                OUTPUT_FILE);
    }

    public int run() throws IOException {
        byte[] completeFile = Files.readAllBytes(INPUT_FILE);
        List<InputSample> inputs = createInputs(completeFile);

        warmUp(inputs);

        StringBuilder csv = new StringBuilder(
                "line_count,byte_count,measurement_1_ns,measurement_2_ns,"
                        + "measurement_3_ns,measurement_4_ns,measurement_5_ns,"
                        + "mean_ns,min_ns,max_ns,checksum\n");

        for (InputSample input : inputs) {
            measure(input, csv);
        }

        Files.createDirectories(OUTPUT_FILE.getParent());
        Files.writeString(OUTPUT_FILE, csv, StandardCharsets.UTF_8);
        return inputs.size();
    }

    private void warmUp(List<InputSample> inputs) {
        for (InputSample input : inputs) {
            for (int call = 0; call < WARMUP_COUNT; call++) {
                hashFunction.hash(input.bytes());
            }
        }
    }

    private void measure(InputSample input, StringBuilder csv) {
        long[] measurements = new long[MEASUREMENT_COUNT];
        int checksum = 0;

        for (int measurement = 0; measurement < MEASUREMENT_COUNT; measurement++) {
            long start = System.nanoTime();
            String hash = hashFunction.hash(input.bytes());
            long elapsed = System.nanoTime() - start;

            measurements[measurement] = elapsed;
            checksum += hash.hashCode();
        }

        long sum = 0;
        long minimum = measurements[0];
        long maximum = measurements[0];

        for (long measurement : measurements) {
            sum += measurement;
            minimum = Math.min(minimum, measurement);
            maximum = Math.max(maximum, measurement);
        }

        double mean = (double) sum / MEASUREMENT_COUNT;

        csv.append(String.format(
                Locale.ROOT,
                "%d,%d,%d,%d,%d,%d,%d,%.2f,%d,%d,%d%n",
                input.lineCount(),
                input.bytes().length,
                measurements[0],
                measurements[1],
                measurements[2],
                measurements[3],
                measurements[4],
                mean,
                minimum,
                maximum,
                checksum));
    }

    private static List<InputSample> createInputs(byte[] completeFile) {
        List<Integer> lineEndPositions = findLineEndPositions(completeFile);
        List<InputSample> inputs = new ArrayList<>();
        int totalLineCount = lineEndPositions.size();

        for (int lineCount = 1; lineCount < totalLineCount; lineCount *= 2) {
            int byteCount = lineEndPositions.get(lineCount - 1);
            inputs.add(new InputSample(
                    lineCount,
                    Arrays.copyOf(completeFile, byteCount)));
        }

        inputs.add(new InputSample(totalLineCount, completeFile));
        return inputs;
    }

    private static List<Integer> findLineEndPositions(byte[] bytes) {
        List<Integer> positions = new ArrayList<>();

        for (int index = 0; index < bytes.length; index++) {
            if (bytes[index] == '\r') {
                if (index + 1 < bytes.length && bytes[index + 1] == '\n') {
                    index++;
                }
                positions.add(index + 1);
            } else if (bytes[index] == '\n') {
                positions.add(index + 1);
            }
        }

        if (positions.isEmpty() || positions.getLast() < bytes.length) {
            positions.add(bytes.length);
        }

        return positions;
    }

    private record InputSample(int lineCount, byte[] bytes) {
    }
}
