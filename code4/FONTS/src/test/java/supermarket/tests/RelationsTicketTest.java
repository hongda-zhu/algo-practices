package supermarket.tests;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import supermarket.domain.controllers.ProductController;
import supermarket.domain.relations.RelationsTickets;

import java.util.TreeSet;

import static org.junit.Assert.*;

public class RelationsTicketTest {
    private RelationsTickets relationsTickets;
    private final int N_PRODUCTS = 4;
    private ProductController productControllerMock;

    @Before
    public void setUp() {
        productControllerMock = Mockito.mock(ProductController.class); // Needed to initialize RelationsTickets
        String testDataFolder = "src/test/resources/";
        String[] filePaths = {
                testDataFolder + "ticket1.txt",
                testDataFolder + "ticket2.txt",
                testDataFolder + "ticket3.txt"
        };
        for (int i = 1; i <= N_PRODUCTS; i++) {
            Mockito.when(productControllerMock.getProductInfo(i)).thenReturn(new supermarket.tuples.ProductInfo(i, "p" + i, i, "normal"));
            Mockito.when(productControllerMock.existsProduct(i)).thenReturn(true);  // Mock product existence from 1 to N
        }

        relationsTickets = new RelationsTickets(filePaths, productControllerMock);
    }

    @Test
    public void testInitializationWithEmptyFilePaths() {
        RelationsTickets emptyRelations = new RelationsTickets(new String[]{}, productControllerMock);
        assertNotNull("RelationsTickets should not be null when initialized with empty file paths", emptyRelations);
    }

    @Test
    public void testRelationValues() {
        TreeSet<Integer> barcodes = new TreeSet<>();
        for (int i = 1; i <= N_PRODUCTS; i++) barcodes.add(i);
        float[][] matrix = relationsTickets.getRelationMatrix(barcodes);
        assertTrue("Relation value between p2 and p3 should be greater than p2 and p4", matrix[1][2] > matrix[1][3]);
        assertTrue("Relation value between p2 and p3 should be greater than p3 and p4", matrix[1][2] > matrix[2][3]);
    }

    @Test
    public void testSymmetry() {
        TreeSet<Integer> barcodes = new TreeSet<>();
        for (int i = 1; i <= N_PRODUCTS; i++) barcodes.add(i);
        float[][] matrix = relationsTickets.getRelationMatrix(barcodes);
        // Verify symmetry
        for (int i = 0; i < N_PRODUCTS; i++) {
            for (int j = 0; j < N_PRODUCTS; j++) {
                assertEquals("Matrix should be symmetric at position [" + i + "][" + j + "]",
                        matrix[i][j], matrix[j][i], 0);
            }
        }
    }

    @Test
    public void testEmptyBarcodes() {
        TreeSet<Integer> emptyBarcodes = new TreeSet<>();
        float[][] matrix = relationsTickets.getRelationMatrix(emptyBarcodes);
        assertEquals("Matrix should be empty when there are no barcodes", 0, matrix.length);
    }

    @Test
    public void testSingleBarcode() {
        TreeSet<Integer> singleBarcode = new TreeSet<>();
        singleBarcode.add(1);
        float[][] matrix = relationsTickets.getRelationMatrix(singleBarcode);
        assertEquals("Matrix should have only 1 row/column for a single barcode", 1, matrix.length);
        assertEquals("Diagonal value for single barcode should be 0", 0.0f, matrix[0][0], 0f);
    }
}
