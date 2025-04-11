package supermarket.tests;

import org.junit.Test;
import static org.junit.Assert.*;
import supermarket.domain.algorithm.AbstractAlgorithm;
import supermarket.domain.algorithm.AlgorithmFactory;
import supermarket.tuples.AlgorithmType;

import java.util.List;

public class AlgorithmFactoryTest {
    // Valid test matrices
    private static final float[][] VALID_MATRIX = new float[][]{{0, 1}, {1, 0}};
    private static final float[][] VALID_MATRIX_3X3 = new float[][]{
            {0, 1, 2},
            {1, 0, 3},
            {2, 3, 0}
    };

    // Invalid test matrices
    private static final float[][] INVALID_MATRIX_NEGATIVE = new float[][]{{0, -1}, {1, 0}};
    private static final float[][] INVALID_MATRIX_NON_SQUARE = new float[][]{{0, 1}, {1, 0, 2}};
    private static final float[][] INVALID_MATRIX_EMPTY = new float[][]{};

    @Test
    public void testCreateAlgorithmForEachType() {
        for (AlgorithmType type : AlgorithmType.values()) {
            AbstractAlgorithm algorithm = AlgorithmFactory.createAlgorithm(type, VALID_MATRIX);
            assertNotNull(
                    String.format("Created algorithm should not be null for type %s", type),
                    algorithm
            );
            assertEquals(
                    String.format("Created algorithm should be of type %s", type),
                    type,
                    algorithm.getType()
            );
        }
    }

    @Test(expected = NullPointerException.class)
    public void testCreateAlgorithmWithNullMatrix() {
        AlgorithmFactory.createAlgorithm(AlgorithmType.GREEDY, null);
    }

    @Test
    public void testCreateAllAlgorithms() {
        List<AbstractAlgorithm> algorithms = AlgorithmFactory.createAllAlgorithms(VALID_MATRIX);
        assertEquals(
                "Number of created algorithms should match number of algorithm types",
                AlgorithmType.values().length,
                algorithms.size()
        );
    }

    @Test
    public void testCreateAllAlgorithmsWithDifferentMatrices() {
        List<AbstractAlgorithm> algorithms2x2 = AlgorithmFactory.createAllAlgorithms(VALID_MATRIX);
        List<AbstractAlgorithm> algorithms3x3 = AlgorithmFactory.createAllAlgorithms(VALID_MATRIX_3X3);

        assertEquals(
                "Number of algorithms should be same regardless of matrix size",
                algorithms2x2.size(),
                algorithms3x3.size()
        );
    }

    @Test(expected = NullPointerException.class)
    public void testCreateAllAlgorithmsWithNullMatrix() {
        AlgorithmFactory.createAllAlgorithms(null);
    }

    @Test
    public void testGetAllUsableAlgorithmsWithValidMatrix() {
        List<AbstractAlgorithm> usableAlgorithms = AlgorithmFactory.getAllUsableAlgorithms(VALID_MATRIX);

        // All algorithms should be usable with a valid matrix
        assertEquals(
                "All algorithms should be usable with valid matrix",
                AlgorithmType.values().length,
                usableAlgorithms.size()
        );

        for (AbstractAlgorithm algorithm : usableAlgorithms) {
            assertTrue(
                    String.format("Algorithm %s should be usable with valid matrix", algorithm.getType()),
                    algorithm.canUseAlgorithm()
            );
        }
    }

    @Test
    public void testGetAllUsableAlgorithmsWithInvalidMatrix() {
        // Test with negative values
        List<AbstractAlgorithm> usableAlgorithmsNegative =
                AlgorithmFactory.getAllUsableAlgorithms(INVALID_MATRIX_NEGATIVE);
        assertTrue(
                "No algorithms should be usable with negative values",
                usableAlgorithmsNegative.isEmpty()
        );

        // Test with non-square matrix
        List<AbstractAlgorithm> usableAlgorithmsNonSquare =
                AlgorithmFactory.getAllUsableAlgorithms(INVALID_MATRIX_NON_SQUARE);
        assertTrue(
                "No algorithms should be usable with non-square matrix",
                usableAlgorithmsNonSquare.isEmpty()
        );

        // Test with empty matrix
        List<AbstractAlgorithm> usableAlgorithmsEmpty =
                AlgorithmFactory.getAllUsableAlgorithms(INVALID_MATRIX_EMPTY);
        assertTrue(
                "No algorithms should be usable with empty matrix",
                usableAlgorithmsEmpty.isEmpty()
        );
    }

    @Test
    public void testGetAllUsableAlgorithmsResultConsistency() {
        List<AbstractAlgorithm> allAlgorithms = AlgorithmFactory.createAllAlgorithms(VALID_MATRIX);
        List<AbstractAlgorithm> usableAlgorithms = AlgorithmFactory.getAllUsableAlgorithms(VALID_MATRIX);

        for (AbstractAlgorithm algorithm : allAlgorithms) {
            boolean isUsable = algorithm.canUseAlgorithm();
            boolean isInUsableList = usableAlgorithms.stream()
                    .map(AbstractAlgorithm::getType)
                    .anyMatch(type -> type.equals(algorithm.getType()));

            assertEquals(
                    String.format(
                            "Algorithm %s usability status (%b) should match its presence in usable list (%b)",
                            algorithm.getType(),
                            isUsable,
                            isInUsableList
                    ),
                    isUsable,
                    isInUsableList
            );
        }
    }

    @Test(expected = NullPointerException.class)
    public void testGetAllUsableAlgorithmsWithNullMatrix() {
        AlgorithmFactory.getAllUsableAlgorithms(null);
    }
}
