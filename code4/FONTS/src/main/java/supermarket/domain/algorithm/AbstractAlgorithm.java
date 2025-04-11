package supermarket.domain.algorithm;

import supermarket.tuples.AlgorithmParameter;
import supermarket.tuples.AlgorithmType;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for implementations for algorithms that calculate distributions
 * given a distance matrix (must be a square matrix with non-negative values to be able to execute any algorithm).
 * @author Pau Martí Biosca
 */
public abstract class AbstractAlgorithm {
    final int n;
    final float[][] distanceMatrix;

    /**
     * Constructor for AbstractAlgorithm.
     * Initializes the algorithm with a given distance matrix.
     * @param distanceMatrix The matrix representing the distances or costs between pairs of elements.
     */
    public AbstractAlgorithm(float[][] distanceMatrix) {
        this.distanceMatrix = distanceMatrix;
        n = distanceMatrix.length;
    }

    /**
     * Returns the type of algorithm
     * @return AlgorithmType that corresponds to the implemented algorithm.
     */
    public abstract AlgorithmType getType();

    /**
     * Calculates the cost of a cycle given a list of vertices.
     * @param vertices List of vertices that form a cycle.
     * @return Cost of the cycle.
     */
    public float calculateCycleCost(int[] vertices) {
        float result = 0;
        for (int i = 1; i < vertices.length; ++i) {
            result += distanceMatrix[vertices[i-1]][vertices[i]];
        }
        result += distanceMatrix[vertices[vertices.length-1]][vertices[0]];
        return result;
    }

    /**
     * Gives all available parameters for an algorithm (things that will change how the algorithm behaves)
     * For example, criteria for adapting Kruskal eulerian path output to a node order.
     * @return List of algorithm parameters, which contain name, description and list of possible values.
     */
    public ArrayList<AlgorithmParameter> getAvailableParameters() {
        return new ArrayList<>();
    }

    /**
     * Sets all parameters to the specified value (passing invalid value on any parameter will make the algorithm use the default behaviour)
     * @param parameters Array of strings corresponding to the answer got by "getAvailableParameters".
     *                   The array must be of the same size, and each position must have a string of the list given, for each parameter (or null).
     *                   This array will have all used elements removed (from the start), so inheritance is easier to implement.
     */
    public void setParameters(List<String> parameters) {

    }

    /**
     * Checks if the relationMatrix data is a valid input for the implemented algorithm.
     * @return If the implemented algorithm can be used with the given relationMatrix data.
     */
    public boolean canUseAlgorithm() {
        if (n <= 0) return false;
        for (int i = 0; i < n; ++i) {
            if (distanceMatrix[i].length != n) return false; //not a n*n matrix
            for (int j = 0; j < n; ++j) {
                if (distanceMatrix[i][j] < 0) return false; //negative values are invalid
            }
        }
        return true;
    }

    /**
     * Calculates a distribution using the relationMatrix that minimizes distance.
     * Distribution consists of an array of indices, which correspond to the positions of the relationMatrix.
     * @return Array of indices of size relationMatrix, circular path.
     */
    public abstract int[] calculateDistribution();
    
}
