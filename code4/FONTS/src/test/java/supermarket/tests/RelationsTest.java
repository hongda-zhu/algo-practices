package supermarket.tests;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import supermarket.domain.controllers.ProductController;
import supermarket.domain.relations.Relations;
import supermarket.tuples.ProductInfo;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class RelationsTest {
    private Relations relations;
    private ArrayList<Integer> barcodes;
    private float[][] relationMatrix;
    private ProductController mockProductController;

    @Before
    public void setUp() {
        mockProductController = Mockito.mock(ProductController.class);
        barcodes = new ArrayList<>();
        int n_PRODUCTS = 5;
        for (int i = 0; i < n_PRODUCTS; i++) {
            barcodes.add(i);
            ProductInfo productInfo = new ProductInfo(i, "p" + i, 1.0f, "normal");
            when(mockProductController.existsProduct(i)).thenReturn(true);
            when(mockProductController.getProductInfo(i)).thenReturn(productInfo);
        }

        relationMatrix = new float[][]{
                {0, 0.5f, 0.3f, 0.5f, 0.1f},
                {0.5f, 0, 0.4f, 0.3f, 0.45f},
                {0.3f, 0.4f, 0, 0.5f, 0.5f},
                {0.5f, 0.3f, 0.5f, 0, 0.2f},
                {0.1f, 0.45f, 0.5f, 0.2f, 0}
        };

        relations = new Relations(mockProductController, relationMatrix, barcodes);
    }

    @Test
    public void testGetRelationMatrixWithArrayList() {
        float[][] resultMatrix = relations.getRelationMatrix(barcodes);

        // Verify matrix values
        for (int i = 0; i < barcodes.size(); i++) {
            for (int j = 0; j < barcodes.size(); j++) {
                if (i != j) {
                    assertEquals("Incorrect value of relationMatrix using an arrayList",
                            relationMatrix[i][j], resultMatrix[i][j], 1e-6);
                } else {
                    assertEquals("Diagonal value has to be 0", 0, resultMatrix[i][j], 1e-6);
                }
            }
        }
    }

    @Test
    public void testGetRelationMatrixWithSet() {
        TreeSet<Integer> barcodeSet = new TreeSet<>(barcodes);
        float[][] resultMatrix = relations.getRelationMatrix(barcodeSet);

        // Verify matrix values
        for (int i = 0; i < barcodes.size(); i++) {
            for (int j = 0; j < barcodes.size(); j++) {
                if (i != j) {
                    assertEquals("Incorrect value of relationMatrix using a set",
                            relationMatrix[i][j], resultMatrix[i][j], 1e-6);
                } else {
                    assertEquals("Diagonal value has to be 0", 0, resultMatrix[i][j], 1e-6);
                }
            }
        }
    }

    @Test
    public void testModifyRelation() {
        // Test modifying existing relation
        relations.modify(0, 1, 0.75f);
        float[][] resultMatrix = relations.getRelationMatrix(barcodes);
        assertEquals("Modified relation not updated correctly", 0.75f, resultMatrix[0][1], 1e-6);
        assertEquals("Symmetric relation not updated correctly", 0.75f, resultMatrix[1][0], 1e-6);

        // Test adding new relation
        relations.modify(0, 5, 0.8f);
        barcodes.add(5);
        resultMatrix = relations.getRelationMatrix(barcodes);
        assertEquals("New relation not added correctly", 0.8f, resultMatrix[0][5], 1e-6);
        assertEquals("New symmetric relation not added correctly", 0.8f, resultMatrix[5][0], 1e-6);
    }

    @Test
    public void testDeleteRelation() {
        // Delete a product and verify its relations are removed
        relations.delete(2);
        float[][] resultMatrix = relations.getRelationMatrix(new ArrayList<>(Arrays.asList(0, 1, 3, 4)));

        // Verify size of matrix after deletion
        assertEquals("Matrix size incorrect after deletion", 4, resultMatrix.length);

        // Verify relations with deleted product are removed
        for (int i = 0; i < resultMatrix.length; i++) {
            for (int j = 0; j < resultMatrix[i].length; j++) {
                if (i != j) {
                    int originalI = i >= 2 ? i + 1 : i;
                    int originalJ = j >= 2 ? j + 1 : j;
                    float expectedValue = relationMatrix[originalI][originalJ];
                    assertEquals("Incorrect relation value after deletion",
                            expectedValue, resultMatrix[i][j], 1e-6);
                }
            }
        }
    }

    @Test
    public void testReadMatrix() {
        // Create new matrix with different values
        float[][] newMatrix = new float[][]{
                {0, 0.9f, 0.8f},
                {0.9f, 0, 0.7f},
                {0.8f, 0.7f, 0}
        };
        ArrayList<Integer> newBarcodes = new ArrayList<>(Arrays.asList(10, 11, 12));

        // Read new matrix
        relations.readMatrix(newMatrix, newBarcodes);
        float[][] resultMatrix = relations.getRelationMatrix(newBarcodes);

        // Verify new matrix values
        for (int i = 0; i < newMatrix.length; i++) {
            for (int j = 0; j < newMatrix[i].length; j++) {
                assertEquals("Matrix not read correctly",
                        newMatrix[i][j], resultMatrix[i][j], 1e-6);
            }
        }
    }

    @Test
    public void testEdgeCases() {
        // Test with empty barcodes list
        ArrayList<Integer> emptyBarcodes = new ArrayList<>();
        float[][] emptyMatrix = new float[0][0];
        Relations emptyRelations = new Relations(mockProductController, emptyMatrix, emptyBarcodes);
        float[][] resultMatrix = emptyRelations.getRelationMatrix(emptyBarcodes);
        assertEquals("Empty matrix should have 0 rows", 0, resultMatrix.length);

        // Test modifying relation with non-existent product
        relations.modify(100, 101, 0.5f);
        ArrayList<Integer> extendedBarcodes = new ArrayList<>(barcodes);
        extendedBarcodes.add(100);
        extendedBarcodes.add(101);
        resultMatrix = relations.getRelationMatrix(extendedBarcodes);
        assertEquals("Non-existent product relation not added correctly",
                0.5f, resultMatrix[5][6], 1e-6);
    }
}
