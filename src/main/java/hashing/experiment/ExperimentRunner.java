package hashing.experiment;

import hashing.HashFunction;
import hashing.HashFunctionV02;

import java.io.IOException;

/** Runs all hash-function experiments. */
public final class ExperimentRunner {

    private ExperimentRunner() {
    }

    public static void main(String[] args) throws IOException {
        HashFunction hashFunction = new HashFunctionV02();

        new PerformanceExperiment(hashFunction).run();
        new CollisionExperiment(hashFunction).run();
        new AvalancheExperiment(hashFunction).run();
        new GuessingExperiment(hashFunction).run();

        System.out.println("All experiments completed.");
    }
}
