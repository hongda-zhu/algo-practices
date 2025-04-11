package supermarket.tests;

import org.junit.Before;
import org.junit.Test;
import supermarket.domain.algorithm.KruskalApproxAlgorithm;
import supermarket.tuples.AlgorithmType;
import supermarket.tuples.AlgorithmParameter;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class KruskalApproxAlgorithmTest {
    private static final float[][] MATRIX_2X2 = {
            {0.0f, 1.0f},
            {1.0f, 0.0f}
    };
    private static final float[][] MATRIX_STANDARD_TEST = {
            {0.0f, 29.0f, 82.0f, 46.0f},
            {29.0f, 0.0f, 55.0f, 46.0f},
            {82.0f, 55.0f, 0.0f, 68.0f},
            {46.0f, 46.0f, 68.0f, 0.0f}
    };

    private KruskalApproxAlgorithm algorithm;

    @Before
    public void setUp() {
        algorithm = new KruskalApproxAlgorithm(MATRIX_2X2);
    }

    @Test
    public void testGetType() {
        assertEquals(
                "Algorithm type should be KRUSKAL_APPROX",
                AlgorithmType.KRUSKAL_APPROX,
                algorithm.getType()
        );
    }

    @Test
    public void testAvailableParameters() {
        List<AlgorithmParameter> params = algorithm.getAvailableParameters();
        assertFalse(
                "Algorithm should have available parameters",
                params.isEmpty()
        );

        AlgorithmParameter param = params.getFirst();
        assertEquals(
                "Parameter name should match expected",
                "Edge repetition elimination type",
                param.paramName()
        );
    }

    @Test
    public void testSetValidParameters() {
        List<String> validParams = List.of("FirstStartingNode");
        algorithm.setParameters(new ArrayList<>(validParams));

        // Test that the parameter was accepted by checking the result consistency
        int[] result1 = algorithm.calculateDistribution();
        algorithm.setParameters(new ArrayList<>(validParams));
        int[] result2 = algorithm.calculateDistribution();

        assertArrayEquals(
                "Results should be consistent with same parameters",
                result1,
                result2
        );
    }

    @Test
    public void testSetInvalidParameters() {
        List<String> invalidParams = List.of("InvalidParam");
        algorithm.setParameters(new ArrayList<>(invalidParams));

        // Should still produce valid results with default parameter
        int[] result = algorithm.calculateDistribution();
        assertNotNull("Should produce result even with invalid parameter", result);
        assertEquals(
                "Result length should match matrix size",
                MATRIX_2X2.length,
                result.length
        );
    }

    @Test
    public void testCanUseAlgorithmWithValidMatrix() {
        float[][] MATRIX_3X3 = {
                {0.0f, 1.0f, 2.0f},
                {1.0f, 0.0f, 2.0f},
                {2.0f, 2.0f, 0.0f}
        };
        assertTrue(
                "Should accept valid symmetric matrix satisfying triangle inequality",
                new KruskalApproxAlgorithm(MATRIX_3X3).canUseAlgorithm()
        );
    }

    @Test
    public void testCanUseAlgorithmWithAsymmetricMatrix() {
        float[][] MATRIX_ASYMMETRIC = {
                {0.0f, 1.2f, 2.0f},
                {1.0f, 0.0f, 3.0f},
                {2.0f, 3.0f, 0.0f}
        };
        assertTrue(
                "Should accept asymmetric matrix satisfying triangle inequality",
                new KruskalApproxAlgorithm(MATRIX_ASYMMETRIC).canUseAlgorithm()
        );
    }

    @Test
    public void testCanUseAlgorithmWithTriangleInequalityViolation() {
        float[][] MATRIX_TRIANGLE_INEQUALITY_VIOLATION = {
                {0.0f, 10.0f, 1.0f},
                {10.0f, 0.0f, 1.0f},
                {1.0f, 1.0f, 0.0f}
        };
        assertFalse(
                "Should reject matrix violating triangle inequality",
                new KruskalApproxAlgorithm(MATRIX_TRIANGLE_INEQUALITY_VIOLATION).canUseAlgorithm()
        );
    }

    @Test
    public void testCalculateDistributionBasicProperties() {
        int[] distribution = algorithm.calculateDistribution();

        assertNotNull("Distribution should not be null", distribution);
        assertEquals(
                "Distribution length should match matrix size",
                MATRIX_2X2.length,
                distribution.length
        );

        boolean[] visited = new boolean[MATRIX_2X2.length];
        for (int vertex : distribution) {
            visited[vertex] = true;
        }

        // Check if all vertices were visited
        for (boolean wasVisited : visited) {
            assertTrue("All vertices should be visited", wasVisited);
        }
    }

    @Test
    public void testAllEliminationTypes() {
        KruskalApproxAlgorithm alg = new KruskalApproxAlgorithm(MATRIX_STANDARD_TEST);

        List<Float> costs = new ArrayList<>();

        for (String elimType : KruskalApproxAlgorithm.elimTypes) {
            alg.setParameters(new ArrayList<>(List.of(elimType)));
            int[] solution = alg.calculateDistribution();
            costs.add(alg.calculateCycleCost(solution));

            // Verify solution properties
            assertNotNull(
                    String.format("Solution should not be null for elimination type %s", elimType),
                    solution
            );
            assertEquals(
                    String.format("Solution length should match matrix size for elimination type %s", elimType),
                    MATRIX_STANDARD_TEST.length,
                    solution.length
            );
        }

        // Verify that FastBestStartingNode and BestStartingNode are not worse than FirstStartingNode
        assertTrue(
                "BestStartingNode should not be worse than FirstStartingNode",
                costs.get(1) <= costs.get(0) + 1e-5
        );
        assertTrue(
                "FastBestStartingNode should not be worse than FirstStartingNode",
                costs.get(2) <= costs.get(0) + 1e-5
        );
    }

    @Test
    public void testSolutionStability() {
        // Test if multiple runs with same parameters produce same results
        KruskalApproxAlgorithm alg = new KruskalApproxAlgorithm(MATRIX_STANDARD_TEST);
        alg.setParameters(new ArrayList<>(List.of("FirstStartingNode")));

        int[] solution1 = alg.calculateDistribution();
        float cost1 = alg.calculateCycleCost(solution1);

        int[] solution2 = alg.calculateDistribution();
        float cost2 = alg.calculateCycleCost(solution2);

        assertEquals(
                "Multiple runs should produce same cost with same parameters",
                cost1,
                cost2,
                1e-6
        );
    }

    @Test(expected = NullPointerException.class)
    public void testNullMatrix() {
        new KruskalApproxAlgorithm(null);
    }
}
