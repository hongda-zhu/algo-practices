package supermarket.tests;

import org.junit.Before;
import org.junit.Test;
import supermarket.domain.supermarket.Store;
import supermarket.tuples.ProductInfo;
import supermarket.domain.supermarket.Shelf;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

public class StoreTest {

    private Store store;
    private Shelf shelfMock;

    @Before
    public void setUp() {
        store = new Store("Store1");
        shelfMock = mock(Shelf.class);
    }

    @Test
    public void testSetName() {
        store.setName("Store2");
        assertEquals("Store2", store.getName());
    }

    @Test
    public void testAddShelf() {
        store.addShelf(1, "Normal", 10);
        assertNotNull(store.getShelf(1));
        assertTrue(store.shelfExists(1));
    }

    @Test
    public void testAddShelfAlreadyExistsException() {
        int shelfId = 1;
        store.addShelf(shelfId, "Normal", 10);
        Exception exception = assertThrows(RuntimeException.class, () -> store.addShelf(shelfId, "Normal", 10));
        String expectedMessage = "The shelf with ID " + shelfId + " already exists in the store Store1";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testRemoveShelf() {
        store.addShelf(1, "Normal", 10);
        assertTrue(store.shelfExists(1));
        store.removeShelf(1);
        assertFalse(store.shelfExists(1));
    }

    @Test
    public void testRemoveShelfNotExistsException() {
        int shelfId = 1;
        Exception exception = assertThrows(RuntimeException.class, () -> store.removeShelf(shelfId));
        String expectedMessage = "The shelf " + shelfId + " does not exist in the store Store1.";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testShelfExists() {
        store.addShelf(1, "Normal", 10);
        assertTrue(store.shelfExists(1));
        assertFalse(store.shelfExists(2));
    }

    @Test
    public void testPlaceProduct() {
        assertNotNull(shelfMock);
        ProductInfo productInfo = new ProductInfo(123, "p1", 2, "Normal");
        store.addShelf(1, "Normal", 10);
        store.addOfferedProduct(123);
        when(shelfMock.placeProduct(productInfo, 1)).thenReturn(124);
        store.placeProduct(1, productInfo, 1);
        assertTrue(store.getShelf(1).getStoredProducts().contains(123));
    }

    @Test
    public void testPlaceProductNotOfferedException() {
        store.addShelf(1, "Normal", 10);
        ProductInfo productInfo = new ProductInfo(123, "p1", 2, "Normal");
        Exception exception = assertThrows(RuntimeException.class, () -> store.placeProduct(1, productInfo, 1));
        String expectedMessage = "The store Store1 does not offer the product " + productInfo + ".\n";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testPlaceProductAlreadyPlacedException() {
        store.addShelf(1, "Normal", 10);
        int barcode = 123;
        store.addOfferedProduct(barcode);
        ProductInfo productInfo = new ProductInfo(barcode, "p1", 2, "Normal");
        store.placeProduct(1, productInfo, 1);
        Exception exception = assertThrows(RuntimeException.class, () -> store.placeProduct(1, productInfo, 1));
        String expectedMessage = "The product " + productInfo + " is already placed in the store.\n";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testPlaceProductShelfNotExistsException() {
        int barcode = 123;
        int shelfId = 1;
        store.addOfferedProduct(barcode);
        ProductInfo productInfo = new ProductInfo(barcode, "p1", 2, "Normal");
        Exception exception = assertThrows(RuntimeException.class, () -> store.placeProduct(shelfId, productInfo, 1));
        String expectedMessage = "The shelf " + shelfId + " does not exist in the store Store1.";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testWithdrawProduct() {
        int shelfId = 1;
        int barcode = 123;
        store.addShelf(shelfId, "Normal", 10);
        store.addOfferedProduct(barcode);
        store.placeProduct(shelfId, new ProductInfo(barcode, "p1", 2, "Normal"), 1);
        store.withdrawProduct(shelfId, barcode);
        assertFalse(store.getShelf(shelfId).getStoredProducts().contains(barcode));
    }

    @Test
    public void testWithdrawProductShelfNotExistsException() {
        int shelfId = 1;
        int barcode = 123;
        store.addOfferedProduct(barcode);
        Exception exception = assertThrows(RuntimeException.class, () -> store.withdrawProduct(shelfId, barcode));
        String expectedMessage = "The shelf " + shelfId + " does not exist in the store Store1.";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testAddOfferedProduct() {
        store.addOfferedProduct(123);
        assertTrue(store.productIsOffered(123));
    }

    @Test
    public void testAddOfferedProductAlreadyOfferedException() {
        int productBarcode = 123;
        store.addOfferedProduct(productBarcode);
        Exception exception = assertThrows(RuntimeException.class, () -> store.addOfferedProduct(productBarcode));
        String expectedMessage = "The product " + productBarcode + " is already offered in the store Store1";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testDiscardOfferedProduct() {
        store.addOfferedProduct(123);
        assertTrue(store.productIsOffered(123));
        store.discardOfferedProduct(123);
        assertFalse(store.productIsOffered(123));
    }

    @Test
    public void testDiscardOfferedProductNotOfferedException() {
        int productBarcode = 123;
        Exception exception = assertThrows(RuntimeException.class, () -> store.discardOfferedProduct(productBarcode));
        String expectedMessage = "The product " + productBarcode + " is not offered in the store Store1";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testProductIsOffered() {
        store.addOfferedProduct(123);
        assertTrue(store.productIsOffered(123));
        assertFalse(store.productIsOffered(124));
    }

    @Test
    public void testProductIsPlaced() {
        store.addShelf(1, "Normal", 10);
        int barcode = 123;
        store.addOfferedProduct(barcode);
        store.placeProduct(1, new ProductInfo(barcode, "p1", 2, "Normal"), 1);
        assertTrue(store.productIsPlaced(barcode));
    }

    @Test
    public void testContainsProduct() {
        int shelfId = 1;
        store.addShelf(shelfId, "Normal", 10);
        int barcode = 123;
        store.addOfferedProduct(barcode);
        store.placeProduct(shelfId, new ProductInfo(barcode, "p1", 2, "Normal"), 1);
        when(shelfMock.containsProduct(barcode)).thenReturn(true);
        assertTrue(store.containsProduct(shelfId, barcode));
    }

    @Test
    public void testPlaceableProducts() {
        store.addOfferedProduct(123);
        store.addOfferedProduct(124);
        assertTrue(store.getPlaceableProductsBarcodes().contains(124));

        ProductInfo productInfo = new ProductInfo(123, "p1", 2, "a");
        store.addShelf(1, "a", 10);
        when(shelfMock.placeProduct(productInfo, 1)).thenReturn(null);
        store.placeProduct(1, productInfo, 1);
        assertFalse(store.getPlaceableProductsBarcodes().contains(123));

        store.withdrawProduct(1, 123);
        assertTrue(store.getPlaceableProductsBarcodes().contains(123));

        store.discardOfferedProduct(123);
        assertFalse(store.getPlaceableProductsBarcodes().contains(123));
    }

    @Test
    public void testSwapPositions() {
        store.addShelf(1, "Normal", 10);
        store.addOfferedProduct(123);
        store.addOfferedProduct(124);
        int position1 = 1;
        int position2 = 3;
        store.placeProduct(1, new ProductInfo(123, "p1", 2, "Normal"), position1);
        store.placeProduct(1, new ProductInfo(124, "p2", 2, "Normal"), position2);
        store.swapPositions(1, position1, position2);
        assertEquals(124, store.getShelf(1).getStoredProducts().get(position1).intValue());
        assertEquals(123, store.getShelf(1).getStoredProducts().get(position2).intValue());
    }

    @Test
    public void testSwapPositionsShelfNotExistsException() {
        int shelfId = 1;
        int position1 = 1;
        int position2 = 3;
        Exception exception = assertThrows(RuntimeException.class, () -> store.swapPositions(shelfId, position1, position2));
        String expectedMessage = "The shelf " + shelfId + " does not exist in the store Store1.";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testPlaceProducts() {
        store.addShelf(1, "Normal", 10);
        int barcode1 = 123;
        int barcode2 = 124;
        store.addOfferedProduct(barcode1);
        store.addOfferedProduct(barcode2);
        ProductInfo prodInfo1 = new ProductInfo(barcode1, "p1", 2, "Normal");
        ProductInfo prodInfo2 = new ProductInfo(barcode2, "p2", 2, "Normal");
        ProductInfo[] products = {prodInfo1, prodInfo2};
        store.placeProducts(1, products);
        assertTrue(store.getShelf(1).getStoredProducts().contains(barcode1));
        assertTrue(store.getShelf(1).getStoredProducts().contains(barcode2));
    }

    @Test
    public void testPlaceProductsSizeError() {
        int shelfId = 1;
        int shelfSize = 1;
        store.addShelf(shelfId, "Normal", shelfSize);
        int barcode1 = 123;
        int barcode2 = 124;
        store.addOfferedProduct(barcode1);
        store.addOfferedProduct(barcode2);
        ProductInfo prodInfo1 = new ProductInfo(barcode1, "p1", 2, "Normal");
        ProductInfo prodInfo2 = new ProductInfo(barcode2, "p2", 2, "Normal");
        ProductInfo[] products = {prodInfo1, prodInfo2};
        int length = products.length;

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        store.placeProducts(shelfId, products);
        String expectedMessage = "Trying to place " + length + " products to the shelf with id " + shelfId
                + ", which only has size of " + shelfSize + ". Ignoring additional products.";
        String actualMessage = errContent.toString().trim();
        assertTrue(actualMessage.contains(expectedMessage));
    }


    @Test
    public void testModifyShelfSize() {
        store.addShelf(1, "Normal", 10);
        store.addOfferedProduct(123);
        store.placeProduct(1, new ProductInfo(123, "p1", 2, "Normal"), 8);
        List<Integer> removedProducts = new ArrayList<>();
        removedProducts.add(123);
        when(shelfMock.setShelfSize(2)).thenReturn(removedProducts);
        int newSize = 5;
        store.modifyShelfSize(1, newSize);
        assertEquals(newSize, store.getShelf(1).getSize());
        assertTrue(store.getPlaceableProductsBarcodes().contains(123));
    }

    @Test
    public void testModifyShelfSizeNotExistsException() {
        int shelfId = 1;
        int newSize = 20;
        Exception exception = assertThrows(RuntimeException.class, () -> store.modifyShelfSize(shelfId, newSize));
        String expectedMessage = "The shelf " + shelfId + " does not exist in the store Store1.";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testContainsProducts() {
        store.addShelf(1, "Normal", 10);
        int barcode = 123;
        store.addOfferedProduct(barcode);
        ProductInfo prodInfo = new ProductInfo(barcode, "p1", 2, "Normal");
        when(shelfMock.containsProduct(barcode)).thenReturn(true);
        store.placeProduct(1, prodInfo, 1);
        assertTrue(store.containsProducts(1));
    }

    @Test
    public void testContainsProductsShelfNotExistsException() {
        int shelfId = 1;
        Exception exception = assertThrows(RuntimeException.class, () -> store.containsProducts(shelfId));
        String expectedMessage = "The shelf " + shelfId + " does not exist in the store Store1.";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }
}
