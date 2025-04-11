package supermarket.tests;

import org.junit.Test;
import supermarket.domain.algorithm.AbstractAlgorithm;
import supermarket.tuples.AlgorithmType;
import supermarket.tuples.AlgorithmParameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class AbstractAlgorithmTest {
    static class EmptyAlgorithm extends AbstractAlgorithm {
        public EmptyAlgorithm(float[][] distanceMatrix) {
            super(distanceMatrix);
        }

        @Override
        public AlgorithmType getType() {
            return null;
        }

        @Override
        public int[] calculateDistribution() {
            return new int[0];
        }
    }

    @Test
    public void testCalculateCycleCost() {
        float[][] relations = new float[][] {
                {0, 1, 2},
                {1, 0, 4},
                {1, 10, 0}
        };
        EmptyAlgorithm algorithm = new EmptyAlgorithm(relations);

        int[] cycle = new int[]{0,1,2};
        assertEquals("Cycle cost for path 0->1->2->0 should be 6 (1+4+1)",
                6, algorithm.calculateCycleCost(cycle), 1e-5);

        int[] cycle2 = new int[]{0,2,1};
        assertEquals("Cycle cost for path 0->2->1->0 should be 13 (2+10+1)",
                13, algorithm.calculateCycleCost(cycle2), 1e-5);
    }

    @Test
    public void testCalculateCycleCostSingleNode() {
        float[][] relations = new float[][] {{0}};
        EmptyAlgorithm algorithm = new EmptyAlgorithm(relations);
        int[] cycle = new int[]{0};
        assertEquals("Cycle cost for single node should be 0",
                0, algorithm.calculateCycleCost(cycle), 1e-5);
    }

    @Test
    public void testCalculateCycleCostTwoNodes() {
        float[][] relations = new float[][] {
                {0, 5},
                {5, 0}
        };
        EmptyAlgorithm algorithm = new EmptyAlgorithm(relations);
        int[] cycle = new int[]{0,1};
        assertEquals("Cycle cost for two nodes should be sum of both paths",
                10, algorithm.calculateCycleCost(cycle), 1e-5);
    }

    @Test
    public void testSetParameters() {
        float[][] relations = new float[][] {{0}};
        EmptyAlgorithm algorithm = new EmptyAlgorithm(relations);

        // Test with empty list
        algorithm.setParameters(new ArrayList<>());
        assertTrue("Algorithm should accept empty parameter list",
                algorithm.getAvailableParameters().isEmpty());

        // Test with null
        algorithm.setParameters(null);
        assertTrue("Algorithm should handle null parameters gracefully",
                algorithm.getAvailableParameters().isEmpty());

        // Test with some parameters
        List<String> params = Arrays.asList("param1", "param2");
        algorithm.setParameters(params);
        assertTrue("Default implementation should ignore parameters",
                algorithm.getAvailableParameters().isEmpty());
    }

    @Test
    public void testCanUseAlgorithmValidMatrix() {
        float[][] validMatrix = new float[][] {
                {0, 1, 2},
                {1, 0, 4},
                {1, 10, 0}
        };
        EmptyAlgorithm algorithm = new EmptyAlgorithm(validMatrix);
        assertTrue("Valid square matrix with non-negative values should be accepted",
                algorithm.canUseAlgorithm());
    }

    @Test
    public void testCanUseAlgorithmNonSquareMatrix() {
        float[][] invalidMatrix = new float[][] {
                {0, 1},
                {1, 0, 4},
                {1, 10, 0}
        };
        EmptyAlgorithm algorithm = new EmptyAlgorithm(invalidMatrix);
        assertFalse("Non-square matrix should be rejected",
                algorithm.canUseAlgorithm());
    }

    @Test
    public void testCanUseAlgorithmNegativeMatrix() {
        float[][] invalidMatrix = new float[][] {
                {0, 1, 2},
                {1, 0, 4},
                {-1, 10, 0}
        };
        EmptyAlgorithm algorithm = new EmptyAlgorithm(invalidMatrix);
        assertFalse("Matrix with negative values should be rejected",
                algorithm.canUseAlgorithm());
    }

    @Test
    public void testCanUseAlgorithmEmptyMatrix() {
        float[][] emptyMatrix = new float[][] {};
        EmptyAlgorithm algorithm = new EmptyAlgorithm(emptyMatrix);
        assertFalse("Empty matrix should be rejected",
                algorithm.canUseAlgorithm());
    }

    @Test
    public void testCanUseAlgorithmSingleElementMatrix() {
        float[][] singleElementMatrix = new float[][] {{0}};
        EmptyAlgorithm algorithm = new EmptyAlgorithm(singleElementMatrix);
        assertTrue("Single element matrix with zero should be accepted",
                algorithm.canUseAlgorithm());
    }

    @Test
    public void testCanUseAlgorithmNullMatrix() {
        assertThrows("Null matrix should throw NullPointerException",
                NullPointerException.class,
                () -> new EmptyAlgorithm(null));
    }

    @Test
    public void testGetAvailableParameters() {
        float[][] matrix = new float[][] {{0}};
        EmptyAlgorithm algorithm = new EmptyAlgorithm(matrix);
        ArrayList<AlgorithmParameter> params = algorithm.getAvailableParameters();
        assertNotNull("Available parameters should never be null", params);
        assertTrue("Default implementation should return empty parameter list",
                params.isEmpty());
    }
}