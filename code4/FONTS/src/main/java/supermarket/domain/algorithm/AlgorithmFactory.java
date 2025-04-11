package supermarket.domain.algorithm;

import supermarket.tuples.AlgorithmType;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory class to create algorithms given an AlgorithmType and a distance matrix.
 * @author Pau Martí Biosca
 */
public class AlgorithmFactory {

    /**
     * Creates an algorithm of the specified type with the given distance matrix.
     *
     * @param algorithmType  The type of algorithm to create.
     * @param distanceMatrix The matrix representing the distances or costs between pairs of elements.
     * @return An instance of the specified algorithm type.
     */
    public static AbstractAlgorithm createAlgorithm(AlgorithmType algorithmType, float[][] distanceMatrix) {
        switch (algorithmType) {
            case GREEDY -> {
                return new GreedyAlgorithm(distanceMatrix);
            }
            case KRUSKAL_APPROX -> {
                return new KruskalApproxAlgorithm(distanceMatrix);
            }
            case SIMULATED_ANNEALING -> {
                return new SimulatedAnnealingAlgorithm(distanceMatrix);
            }
            default -> throw new RuntimeException("Asked to create algorithm class with invalid AlgorithmType: " + algorithmType);
        }
    }

    /**
     * Creates all available algorithms with the given distance matrix.
     *
     * @param distanceMatrix The matrix representing the distances or costs between pairs of elements.
     * @return A list of all available algorithms.
     */
    public static List<AbstractAlgorithm> createAllAlgorithms(float[][] distanceMatrix) {
        ArrayList<AbstractAlgorithm> algorithms = new ArrayList<>();
        for (AlgorithmType type : AlgorithmType.values()) algorithms.add(createAlgorithm(type, distanceMatrix));
        return algorithms;
    }

    /**
     * Creates all available algorithms with the given distance matrix and removes the ones that can't be used.
     *
     * @param distanceMatrix The matrix representing the distances or costs between pairs of elements.
     * @return A list of all available algorithms that can be used.
     */
    public static List<AbstractAlgorithm> getAllUsableAlgorithms(float[][] distanceMatrix) {
        List<AbstractAlgorithm> algorithms = createAllAlgorithms(distanceMatrix);
        algorithms.removeIf(algorithm -> !algorithm.canUseAlgorithm());
        return algorithms;
    }
}
