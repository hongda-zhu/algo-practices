package supermarket.tests;

import org.junit.Test;
import supermarket.domain.algorithm.AbstractAlgorithm;
import supermarket.domain.algorithm.SimulatedAnnealingAlgorithm;
import supermarket.tuples.AlgorithmParameter;
import supermarket.tuples.AlgorithmType;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class SimulatedAnnealingAlgorithmTest {

    @Test
    public void testCanUseAlgorithmValidMatrix() {
        AbstractAlgorithm simulatedAnnealingAlgorithm;
        float[][] matrixValid = {
                {0.0f, 1.2f, 3.4f},
                {1.2f, 0.0f, 2.1f},
                {3.4f, 2.1f, 0.0f}
        };
        simulatedAnnealingAlgorithm = new SimulatedAnnealingAlgorithm(matrixValid);
        assertTrue("Algorithm should accept valid matrix", simulatedAnnealingAlgorithm.canUseAlgorithm());
    }

    @Test
    public void testCanUseAlgorithmNegative() {
        AbstractAlgorithm simulatedAnnealingAlgorithm;
        //non-square matrices is tested on Abstract Algorithm
        float[][] matrixNegative = {
                {-0.1f, 1.2f},
                {1.2f, 0.0f},
        };
        simulatedAnnealingAlgorithm = new SimulatedAnnealingAlgorithm(matrixNegative);
        assertFalse("Algorithm should reject matrix with negative values", simulatedAnnealingAlgorithm.canUseAlgorithm());
    }

    @Test
    public void testCanUseAlgorithmAsymmetric() {
        AbstractAlgorithm simulatedAnnealingAlgorithm;
        float[][] matrixNonSymmetric = {
                {0f, 1.2f},
                {1f, 0.0f},
        };
        simulatedAnnealingAlgorithm = new SimulatedAnnealingAlgorithm(matrixNonSymmetric);
        assertTrue("Algorithm should accept asymmetric matrix with zero diagonal", simulatedAnnealingAlgorithm.canUseAlgorithm());
    }

    @Test
    public void testGetAvailableParameters() {
        SimulatedAnnealingAlgorithm algorithm = new SimulatedAnnealingAlgorithm(new float[][]{{0}});
        ArrayList<AlgorithmParameter> params = algorithm.getAvailableParameters();
        assertNotNull("Parameters list should not be null", params);
        assertTrue("Should have at least 3 parameters", params.size() >= 3);
    }

    @Test
    public void testGetType() {
        SimulatedAnnealingAlgorithm algorithm = new SimulatedAnnealingAlgorithm(new float[][]{{0}});
        assertEquals("Algorithm type should be SIMULATED_ANNEALING",
                AlgorithmType.SIMULATED_ANNEALING, algorithm.getType());
    }

    @Test
    public void testGenerateInitialSolution() {
        float[][] matrixValid = {
                {0.0f, 1.2f, 3.4f, 2.1f},
                {1.2f, 0.0f, 2.1f, 3.4f},
                {3.4f, 2.1f, 0.0f, 1.2f},
                {2.1f, 3.4f, 1.2f, 0.0f}
        };
        SimulatedAnnealingAlgorithm simulatedAnnealingAlgorithm = new SimulatedAnnealingAlgorithm(matrixValid);
        ArrayList<Integer> solution = simulatedAnnealingAlgorithm.generateInitialSolution();
        assertEquals("Initial solution size should match matrix dimension", 4, solution.size());
        assertTrue("Initial solution should contain element 0", solution.contains(0));
        assertTrue("Initial solution should contain element 1", solution.contains(1));
        assertTrue("Initial solution should contain element 2", solution.contains(2));
        assertTrue("Initial solution should contain element 3", solution.contains(3));
    }

    @Test
    public void testCalculateCost() {
        float[][] matrixValid = {
                {0.0f, 1.2f, 3.4f, 2.1f},
                {1.2f, 0.0f, 2.1f, 3.4f},
                {3.4f, 2.1f, 0.0f, 1.2f},
                {2.1f, 3.4f, 1.2f, 0.0f}
        };
        SimulatedAnnealingAlgorithm simulatedAnnealingAlgorithm = new SimulatedAnnealingAlgorithm(matrixValid);
        int[] solution = {0, 1, 2, 3};
        double cost = simulatedAnnealingAlgorithm.calculateCycleCost(solution);
        assertEquals("Cost calculation should match expected cycle cost", 1.2 + 2.1 + 1.2 + 2.1, cost, 1e-6);
    }

    @Test
    public void testCalculateDistribution() {
        float[][] matrixValid = {
                {0.0f, 1.2f, 3.4f, 2.1f, 4.5f},
                {1.2f, 0.0f, 2.1f, 3.4f, 5.6f},
                {3.4f, 2.1f, 0.0f, 1.2f, 6.7f},
                {2.1f, 3.4f, 1.2f, 0.0f, 7.8f},
                {4.5f, 5.6f, 6.7f, 7.8f, 0.0f}
        };
        SimulatedAnnealingAlgorithm simulatedAnnealingAlgorithm = new SimulatedAnnealingAlgorithm(matrixValid);
        int[] finalSolution = simulatedAnnealingAlgorithm.calculateDistribution();
        assertNotNull("Final distribution should not be null", finalSolution);
        assertEquals("Final distribution length should match matrix dimension", matrixValid.length, finalSolution.length);
    }

    @Test
    public void testMinimizeCost() {
        float[][] matrixValid = {
                {0.0f, 1.2f, 3.4f, 2.1f, 4.5f},
                {1.2f, 0.0f, 2.1f, 3.4f, 5.6f},
                {3.4f, 2.1f, 0.0f, 1.2f, 6.7f},
                {2.1f, 3.4f, 1.2f, 0.0f, 7.8f},
                {4.5f, 5.6f, 6.7f, 7.8f, 0.0f}
        };
        SimulatedAnnealingAlgorithm simulatedAnnealingAlgorithm = new SimulatedAnnealingAlgorithm(matrixValid);
        int[] initialSolution = {0, 2, 1, 4, 3};
        double initialCost = simulatedAnnealingAlgorithm.calculateCycleCost(initialSolution);
        int[] finalSolution = simulatedAnnealingAlgorithm.calculateDistribution();
        double finalCost = simulatedAnnealingAlgorithm.calculateCycleCost(finalSolution);
        assertTrue("Final cost should be less than initial cost", finalCost < initialCost);
    }

    @Test
    public void testSolutionValidity() {
        float[][] matrix = {{0.0f, 1.0f}, {1.0f, 0.0f}};
        SimulatedAnnealingAlgorithm algorithm = new SimulatedAnnealingAlgorithm(matrix);
        int[] solution = algorithm.calculateDistribution();
        // Check if solution contains all indices exactly once
        boolean[] used = new boolean[matrix.length];
        for (int index : solution) {
            assertFalse("Each index should appear only once", used[index]);
            used[index] = true;
        }
        // Verify all indices were used
        for (boolean isUsed : used) {
            assertTrue("All indices should be used", isUsed);
        }
    }
}