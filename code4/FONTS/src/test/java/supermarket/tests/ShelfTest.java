package supermarket.tests;

import org.junit.Before;
import org.junit.Test;
import supermarket.domain.supermarket.Shelf;
import supermarket.tuples.ProductInfo;

import java.util.List;

import static org.junit.Assert.*;

public class ShelfTest {

    private Shelf shelf;
    private static final String NORMAL_SHELF_TYPE = "Normal";
    private static final int INITIAL_SHELF_SIZE = 10;
    private static final int SHELF_ID = 1;

    @Before
    public void setUp() {
        shelf = new Shelf(SHELF_ID, NORMAL_SHELF_TYPE, INITIAL_SHELF_SIZE);
    }

    @Test
    public void testInitialShelfState() {
        assertEquals("Shelf ID should match the constructor value", SHELF_ID, shelf.getId());
        assertEquals("Initial shelf size should match the constructor value", INITIAL_SHELF_SIZE, shelf.getSize());
        assertEquals("Shelf type should match the constructor value", NORMAL_SHELF_TYPE, shelf.getShelfType());
        assertEquals("Stored products list should match the initial size", INITIAL_SHELF_SIZE, shelf.getStoredProducts().size());
    }

    @Test
    public void testIncrementShelfSize() {
        int newSize = 20;
        List<Integer> removedProducts = shelf.setShelfSize(newSize);
        assertEquals("Shelf size should be updated to new larger size", newSize, shelf.getSize());
        assertEquals("No products should be removed when expanding shelf", 0, removedProducts.size());
        assertEquals("Stored products list should match the new size", newSize, shelf.getStoredProducts().size());
    }

    @Test
    public void testDecrementShelfSize() {
        // Place some products first
        shelf.placeProduct(new ProductInfo(123, "p1", 2, NORMAL_SHELF_TYPE), 8);
        shelf.placeProduct(new ProductInfo(124, "p2", 2, NORMAL_SHELF_TYPE), 9);

        int newSize = 5;
        List<Integer> removedProducts = shelf.setShelfSize(newSize);
        assertEquals("Shelf size should be updated to new smaller size", newSize, shelf.getSize());
        assertEquals("Two products should have been removed", 2, removedProducts.size());
        assertEquals("Stored products list should match the new size", newSize, shelf.getStoredProducts().size());
    }

    @Test
    public void testPlaceProduct() {
        ProductInfo product = new ProductInfo(123, "p1", 2, NORMAL_SHELF_TYPE);
        Integer previousProduct = shelf.placeProduct(product, 1);
        assertNull("Previous product should be null for empty position", previousProduct);
        assertEquals("Product should be placed at the specified position", product.barcode(), shelf.getStoredProducts().get(1).intValue());
    }

    @Test
    public void testPlaceProductReplacement() {
        ProductInfo product1 = new ProductInfo(123, "p1", 2, NORMAL_SHELF_TYPE);
        ProductInfo product2 = new ProductInfo(124, "p2", 2, NORMAL_SHELF_TYPE);
        shelf.placeProduct(product1, 1);
        Integer replacedBarcode = shelf.placeProduct(product2, 1);
        assertEquals("Replaced product barcode should match first product", product1.barcode(), replacedBarcode.intValue());
    }

    @Test
    public void testPlaceProductShelfTypeException() {
        Shelf fridgeShelf = new Shelf(1, "Fridge", 10);
        ProductInfo product = new ProductInfo(123, "p1", 2, NORMAL_SHELF_TYPE);

        Exception exception = assertThrows("Should throw exception for mismatched shelf type",
                RuntimeException.class, () -> fridgeShelf.placeProduct(product, 1));

        String expectedMessage = "The shelf type of product " + NORMAL_SHELF_TYPE + " does not match shelf type Fridge";
        assertTrue("Exception message should indicate shelf type mismatch",
                exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testPlaceProductDuplicateException() {
        ProductInfo product = new ProductInfo(123, "p1", 2, NORMAL_SHELF_TYPE);
        shelf.placeProduct(product, 1);

        Exception exception = assertThrows("Should throw exception for duplicate product placement",
                RuntimeException.class, () -> shelf.placeProduct(product, 2));

        String expectedMessage = "The product " + product + " is already placed.";
        assertTrue("Exception message should indicate duplicate product",
                exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testWithdrawProduct() {
        int barcode = 123;
        ProductInfo product = new ProductInfo(barcode, "p1", 2, NORMAL_SHELF_TYPE);
        shelf.placeProduct(product, 1);
        shelf.withdrawProduct(barcode);
        assertNull("Position should be null after withdrawal", shelf.getStoredProducts().get(1));
        assertFalse("Product should not be present after withdrawal", shelf.containsProduct(barcode));
    }

    @Test
    public void testWithdrawNonexistentProduct() {
        int barcode = 123;
        shelf.withdrawProduct(barcode);
        assertFalse("Non-existent product withdrawal should not affect shelf", shelf.containsProduct(barcode));
    }

    @Test
    public void testSwapPositions() {
        ProductInfo product1 = new ProductInfo(123, "p1", 2, NORMAL_SHELF_TYPE);
        ProductInfo product2 = new ProductInfo(124, "p2", 2, NORMAL_SHELF_TYPE);
        int position1 = 1;
        int position2 = 5;

        shelf.placeProduct(product1, position1);
        shelf.placeProduct(product2, position2);
        shelf.swapPositions(position1, position2);

        assertEquals("First position should contain second product", product2.barcode(),
                shelf.getStoredProducts().get(position1).intValue());
        assertEquals("Second position should contain first product", product1.barcode(),
                shelf.getStoredProducts().get(position2).intValue());
    }

    @Test
    public void testSwapPositionsWithEmpty() {
        ProductInfo product = new ProductInfo(123, "p1", 2, NORMAL_SHELF_TYPE);
        int position1 = 1;
        int position2 = 5;

        shelf.placeProduct(product, position1);
        shelf.swapPositions(position1, position2);

        assertNull("First position should be empty after swap",
                shelf.getStoredProducts().get(position1));
        assertEquals("Second position should contain the product", product.barcode(),
                shelf.getStoredProducts().get(position2).intValue());
    }

    @Test
    public void testSwapPositionsInvalidPositions() {
        assertThrows("Should throw exception for negative position",
                RuntimeException.class, () -> shelf.swapPositions(-1, 5));

        assertThrows("Should throw exception for position beyond size",
                RuntimeException.class, () -> shelf.swapPositions(1, INITIAL_SHELF_SIZE + 1));
    }

    @Test
    public void testClearShelf() {
        ProductInfo product1 = new ProductInfo(123, "p1", 2, NORMAL_SHELF_TYPE);
        ProductInfo product2 = new ProductInfo(124, "p2", 2, NORMAL_SHELF_TYPE);

        shelf.placeProduct(product1, 0);
        shelf.placeProduct(product2, 1);

        List<Integer> removedProducts = shelf.clearShelf();

        assertEquals("Should remove all products", 2, removedProducts.size());
        assertTrue("Should contain first product", removedProducts.contains(product1.barcode()));
        assertTrue("Should contain second product", removedProducts.contains(product2.barcode()));
        assertFalse("Shelf should not contain any products after clearing", shelf.containsProducts());
    }

    @Test
    public void testSetShelfType() {
        ProductInfo product = new ProductInfo(123, "p1", 2, NORMAL_SHELF_TYPE);
        shelf.placeProduct(product, 0);

        List<Integer> removedProducts = shelf.setShelfType("Fridge");

        assertEquals("Should remove one product", 1, removedProducts.size());
        assertEquals("New shelf type should be set", "Fridge", shelf.getShelfType());
        assertFalse("Products should be removed after type change", shelf.containsProducts());
    }

    @Test
    public void testIsPositionFree() {
        ProductInfo product = new ProductInfo(123, "p1", 2, NORMAL_SHELF_TYPE);
        int position = 1;

        assertTrue("Position should be free initially", shelf.isPositionFree(position));
        shelf.placeProduct(product, position);
        assertFalse("Position should not be free after product placement", shelf.isPositionFree(position));
        assertFalse("Position beyond size should not be free", shelf.isPositionFree(INITIAL_SHELF_SIZE + 1));
    }
}
