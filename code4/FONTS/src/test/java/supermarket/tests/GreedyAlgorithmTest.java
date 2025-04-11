package supermarket.tests;

import org.junit.Test;
import supermarket.domain.algorithm.AbstractAlgorithm;
import supermarket.domain.algorithm.GreedyAlgorithm;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class GreedyAlgorithmTest {
        @Test
        public void testCanUseAlgorithmNegative() {
            AbstractAlgorithm greedyAlgorithm;
            // Non-square matrices is tested on Abstract Algorithm
            float[][] matrixNegative = {
                    {-0.1f, -1.2f},
                    {1.2f, 0.0f},
            };
            greedyAlgorithm = new GreedyAlgorithm(matrixNegative);
            assertFalse("Expected canUseAlgorithm() to return false for matrix with negative values", greedyAlgorithm.canUseAlgorithm());        }

        @Test
        public void testCanUseAlgorithmAsymmetric() { // Meaning that between x and y there is A relation value and with y and x there is B value ( != A )
            AbstractAlgorithm greedyAlgorithm;
            float[][] matrixNonSymmetric = {
                    {0f, 1.2f},
                    {1f, 0.0f},
            };
            greedyAlgorithm = new GreedyAlgorithm(matrixNonSymmetric);
            assertTrue("Expected canUseAlgorithm() to return true for non-symmetric matrix", greedyAlgorithm.canUseAlgorithm());
        }

        @Test
        public void testEmptyMatrix() {
            AbstractAlgorithm greedyAlgorithm;
            float[][] emptyMatrix = {};
            greedyAlgorithm = new GreedyAlgorithm(emptyMatrix);
            int[] result = greedyAlgorithm.calculateDistribution();
            assertEquals("Expected result length to be 0 for empty matrix", 0, result.length);
        }


        @Test
        public void testMediumMatrix() {
            GreedyAlgorithm greedyAlgorithm;
            float[][] matrixGreedy = {
                    {0.0f, 2.3f, 1.1f, 3.5f, 4.6f, 5.7f, 6.8f},
                    {2.3f, 0.0f, 3.2f, 2.6f, 1.5f, 4.9f, 7.1f},
                    {1.1f, 3.2f, 0.0f, 4.1f, 5.4f, 6.2f, 2.8f},
                    {3.5f, 2.6f, 4.1f, 0.0f, 3.3f, 1.8f, 5.5f},
                    {4.6f, 1.5f, 5.4f, 3.3f, 0.0f, 2.4f, 6.7f},
                    {5.7f, 4.9f, 6.2f, 1.8f, 2.4f, 0.0f, 4.4f},
                    {6.8f, 7.1f, 2.8f, 5.5f, 6.7f, 4.4f, 0.0f}
            };
            greedyAlgorithm = new GreedyAlgorithm(matrixGreedy);
            int[] result = greedyAlgorithm.calculateDistribution();
            int[] expectedResult = new int[]{0, 2, 6, 5, 3, 1, 4};  // Since all algorithms minimize the cost (then we invert it)
            assertArrayEquals("Expected specific distribution for medium matrix", expectedResult, result);
        }

        @Test
        public void testLargeMatrix() {
            GreedyAlgorithm greedyAlgorithm;
            float[][] largeMatrix = {
                    {0.0f, 1.2f, 3.4f, 5.6f, 7.8f, 9.1f, 2.3f, 4.5f, 6.7f, 8.9f, 1.1f},
                    {1.2f, 0.0f, 2.2f, 3.3f, 4.4f, 5.5f, 6.6f, 7.7f, 8.8f, 9.9f, 0.9f},
                    {3.4f, 2.2f, 0.0f, 4.1f, 1.3f, 6.8f, 7.4f, 2.9f, 5.5f, 3.3f, 4.7f},
                    {5.6f, 3.3f, 4.1f, 0.0f, 2.4f, 5.2f, 1.8f, 7.6f, 6.1f, 2.7f, 3.5f},
                    {7.8f, 4.4f, 1.3f, 2.4f, 0.0f, 3.9f, 4.8f, 5.1f, 2.6f, 1.7f, 9.0f},
                    {9.1f, 5.5f, 6.8f, 5.2f, 3.9f, 0.0f, 8.4f, 2.1f, 4.9f, 6.3f, 7.5f},
                    {2.3f, 6.6f, 7.4f, 1.8f, 4.8f, 8.4f, 0.0f, 9.7f, 3.2f, 5.8f, 6.9f},
                    {4.5f, 7.7f, 2.9f, 7.6f, 5.1f, 2.1f, 9.7f, 0.0f, 1.4f, 8.6f, 2.8f},
                    {6.7f, 8.8f, 5.5f, 6.1f, 2.6f, 4.9f, 3.2f, 1.4f, 0.0f, 7.3f, 5.6f},
                    {8.9f, 9.9f, 3.3f, 2.7f, 1.7f, 6.3f, 5.8f, 8.6f, 7.3f, 0.0f, 9.4f},
                    {1.1f, 0.9f, 4.7f, 3.5f, 9.0f, 7.5f, 6.9f, 2.8f, 5.6f, 9.4f, 0.0f}
            };
            greedyAlgorithm = new GreedyAlgorithm(largeMatrix);
            int[] result = greedyAlgorithm.calculateDistribution();
            int[] expectedResult = {0, 10, 1, 2, 4, 9, 3, 6, 8, 7, 5};
            assertArrayEquals("Expected specific distribution for given large matrix",expectedResult, result);
        }

    @Test
    public void testLargeMatrixAllStarts() {
        GreedyAlgorithm greedyAlgorithm;
        float[][] largeMatrix = {
                {0.0f, 1.2f, 3.4f, 5.6f, 7.8f, 9.1f, 2.3f, 4.5f, 6.7f, 8.9f, 1.1f},
                {1.2f, 0.0f, 2.2f, 3.3f, 4.4f, 5.5f, 6.6f, 7.7f, 8.8f, 9.9f, 0.9f},
                {3.4f, 2.2f, 0.0f, 4.1f, 1.3f, 6.8f, 7.4f, 2.9f, 5.5f, 3.3f, 4.7f},
                {5.6f, 3.3f, 4.1f, 0.0f, 2.4f, 5.2f, 1.8f, 7.6f, 6.1f, 2.7f, 3.5f},
                {7.8f, 4.4f, 1.3f, 2.4f, 0.0f, 3.9f, 4.8f, 5.1f, 2.6f, 1.7f, 9.0f},
                {9.1f, 5.5f, 6.8f, 5.2f, 3.9f, 0.0f, 8.4f, 2.1f, 4.9f, 6.3f, 7.5f},
                {2.3f, 6.6f, 7.4f, 1.8f, 4.8f, 8.4f, 0.0f, 9.7f, 3.2f, 5.8f, 6.9f},
                {4.5f, 7.7f, 2.9f, 7.6f, 5.1f, 2.1f, 9.7f, 0.0f, 1.4f, 8.6f, 2.8f},
                {6.7f, 8.8f, 5.5f, 6.1f, 2.6f, 4.9f, 3.2f, 1.4f, 0.0f, 7.3f, 5.6f},
                {8.9f, 9.9f, 3.3f, 2.7f, 1.7f, 6.3f, 5.8f, 8.6f, 7.3f, 0.0f, 9.4f},
                {1.1f, 0.9f, 4.7f, 3.5f, 9.0f, 7.5f, 6.9f, 2.8f, 5.6f, 9.4f, 0.0f}
        };
        greedyAlgorithm = new GreedyAlgorithm(largeMatrix);
        List<String> params;
        params = new ArrayList<>();
        params.add("true");
        greedyAlgorithm.setParameters(params);

        int[] initialSolution = {0, 4, 7, 6, 2, 1, 8, 3, 10, 5, 9};  // Random initial solution
        double initialCost = greedyAlgorithm.calculateCycleCost(initialSolution);
        int[] result = greedyAlgorithm.calculateDistribution();
        double finalCost = greedyAlgorithm.calculateCycleCost(result);

        assertTrue("Expected final cost to be better than initial cost", finalCost < initialCost);
    }

        @Test
        public void testSingleElementMatrix() {
            GreedyAlgorithm greedyAlgorithm;
            float[][] singleElementMatrix = {
                    {1.f}
            };
            greedyAlgorithm = new GreedyAlgorithm(singleElementMatrix);
            int[] result = greedyAlgorithm.calculateDistribution();
            int[] expectedResult = {0};
            assertArrayEquals("Expected single-element matrix to return {0} as distribution", expectedResult, result);
        }

        @Test
        public void testSameValuesMatrix() {
            GreedyAlgorithm greedyAlgorithm;
            float[][] sameValuesMatrix = {
                    {0.f,1.f,1.f,1.f,1.f},
                    {1.f,0.f,1.f,1.f,1.f},
                    {1.f,1.f,0.f,1.f,1.f},
                    {1.f,1.f,1.f,0.f,1.f},
                    {1.f,1.f,1.f,1.f,0.f},
            };
            greedyAlgorithm = new GreedyAlgorithm(sameValuesMatrix);
            int[] result = greedyAlgorithm.calculateDistribution();
            int[] expectedResult = {0,1,2,3,4};         // Result could be any combination, but since we start from node 0 we get an "ordered" result
            assertArrayEquals("Expected specific distribution for given matrix with equal values",expectedResult, result);
        }

        @Test
        public void testSparseMatrix() {
            GreedyAlgorithm greedyAlgorithm;
            float[][] sparseMatrix = {
                    {0.0f, 0.0f, 3.0f, 0.0f},
                    {0.0f, 0.0f, 0.0f, 1.0f},
                    {3.0f, 0.0f, 0.0f, 0.0f},
                    {0.0f, 1.0f, 0.0f, 0.0f}
            };
            greedyAlgorithm = new GreedyAlgorithm(sparseMatrix);
            int[] result = greedyAlgorithm.calculateDistribution();
            int[] expectedOrder = new int[]{0, 1, 2, 3};
            assertArrayEquals("Expected specific distribution for sparse matrix", expectedOrder, result);
        }
}
