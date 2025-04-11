package supermarket.tests;

import org.junit.Before;
import org.junit.Test;

import supermarket.domain.controllers.StoreController;
import supermarket.domain.supermarket.Store;

import supermarket.tuples.ProductInfo;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class StoreControllerTest {

    private StoreController storeController;
    private Store storeMock;

    @Before
    public void setUp() {
        storeController = new StoreController();
        storeMock = mock(Store.class);
    }

    @Test
    public void testCreateStore() {
        StoreController storeController = new StoreController();
        storeController.createStore("store1");
        assertTrue(storeController.exists("store1"));
    }

    @Test
    public void testCreateStoreAlreadyExistsException() {
        String storeName = "store1";
        storeController.createStore(storeName);
        Exception exception = assertThrows(RuntimeException.class, () -> storeController.createStore(storeName));
        String expectedMessage = "Store with name " + storeName + " already exists";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testStoreExists() {
        storeController.createStore("store1");
        assertTrue(storeController.exists("store1"));
        assertFalse(storeController.exists("store2"));
    }

    @Test
    public void testDeleteStore() {
        storeController.createStore("store1");
        storeController.deleteStore("store1");
        assertFalse(storeController.exists("store1"));
    }

    @Test
    public void testDeleteStoreNotExistsException() {
        String storeName = "store1";
        Exception exception = assertThrows(RuntimeException.class, () -> storeController.deleteStore(storeName));
        String expectedMessage = "The store " + storeName + " does not exist";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testChangeStoreName() {
        storeController.createStore("store1");
        storeController.changeStoreName("store1", "store2");
        assertFalse(storeController.exists("store1"));
        assertTrue(storeController.exists("store2"));
    }

    @Test
    public void testChangeStoreNameNotExistsException() {
        String storeName = "store1";
        Exception exception = assertThrows(RuntimeException.class, () -> storeController.changeStoreName(storeName, "store2"));
        String expectedMessage = "The store " + storeName + " does not exist";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testChangeStoreNameAlreadyUsedException() {
        String storeName2 = "store2";
        storeController.createStore("store1");
        storeController.createStore(storeName2);
        Exception exception = assertThrows(RuntimeException.class, () -> storeController.changeStoreName("store1", storeName2));
        String expectedMessage = "A store with the name " + storeName2 + " already exists";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testAddShelf() {
        int shelfId = 1;
        storeController.createStore("store1");
        storeController.addShelf("store1", shelfId, "Normal", 10);
        assertTrue(storeController.getShelfIds("store1").contains(1));
    }

    @Test
    public void testAddShelfStoreNotExistsException() {
        String storeName = "store1";
        Exception exception = assertThrows(RuntimeException.class, () -> storeController.addShelf(storeName, 1, "Normal", 10));
        String expectedMessage = "The store " + storeName + " does not exist";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testRemoveShelf() {
        storeController.createStore("store1");
        storeController.addShelf("store1", 1, "Normal", 10);
        storeController.removeShelf("store1", 1);
        assertFalse(storeController.getShelfIds("store1").contains(1));
    }

    @Test
    public void testRemoveShelfStoreNotExistsException() {
        String storeName = "store1";
        Exception exception = assertThrows(RuntimeException.class, () -> storeController.removeShelf(storeName, 1));
        String expectedMessage = "The store " + storeName + " does not exist";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testPlaceProduct() {
        storeController.createStore("store1");
        storeController.addShelf("store1", 1, "Normal", 10);
        storeController.addOfferedProduct("store1", 123);
        ProductInfo productInfo = new ProductInfo(123, "p1", 1, "Normal");
        storeController.placeProduct("store1", 1, productInfo, 5);
        assertTrue(storeController.getStoredProducts("store1", 1).contains(123));
    }

    @Test
    public void testPlaceProductStoreNotExistsException() {
        String storeName = "store1";
        ProductInfo productInfo = new ProductInfo(123, "p1", 1, "Normal");
        Exception exception = assertThrows(RuntimeException.class, () -> storeController.placeProduct(storeName, 1, productInfo, 5));
        String expectedMessage = "The store " + storeName + " does not exist";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testWithdrawProduct() {
        storeController.createStore("store1");
        storeController.addShelf("store1", 1, "Normal", 10);
        storeController.addOfferedProduct("store1", 123);
        ProductInfo productInfo = new ProductInfo(123, "p1", 1, "Normal");
        storeController.placeProduct("store1", 1, productInfo, 5);
        storeController.withdrawProduct("store1", 1, 123);
        assertFalse(storeController.getStoredProducts("store1", 1).contains(123));
    }

    @Test
    public void testWithdrawProductStoreNotExistsException() {
        String storeName = "store1";
        Exception exception = assertThrows(RuntimeException.class, () -> storeController.withdrawProduct(storeName, 1, 123));
        String expectedMessage = "The store " + storeName + " does not exist";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testWithdrawProductNotContainsProductException() {
        String storeName = "store1";
        storeController.createStore(storeName);
        int shelfId = 1;
        int productBarcode = 123;
        storeController.addShelf(storeName, shelfId, "Normal", 10);
        when(storeMock.containsProduct(shelfId, productBarcode)).thenReturn(false);
        Exception exception = assertThrows(RuntimeException.class, () -> storeController.withdrawProduct(storeName, 1, productBarcode));
        String expectedMessage = "The shelf " + shelfId + " (store: " + storeName + ") does not contain the product " + productBarcode + "\n";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testAddOfferedProduct() {
        storeController.createStore("store1");
        storeController.addOfferedProduct("store1", 123);
        assertTrue(storeController.getOfferedProducts("store1").contains(123));
    }

    @Test
    public void testAddOfferedProductStoreNotExistsException() {
        String storeName = "store1";
        Exception exception = assertThrows(RuntimeException.class, () -> storeController.addOfferedProduct(storeName, 123));
        String expectedMessage = "The store " + storeName + " does not exist";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testDiscardOfferedProduct() {
        storeController.createStore("store1");
        storeController.addOfferedProduct("store1", 123);
        storeController.discardOfferedProduct("store1", 123);
        assertFalse(storeController.getOfferedProducts("store1").contains(123));
    }

    @Test
    public void testDiscardOfferedProductStoreNotExistsException() {
        String storeName = "store1";
        Exception exception = assertThrows(RuntimeException.class, () -> storeController.discardOfferedProduct(storeName, 123));
        String expectedMessage = "The store " + storeName + " does not exist";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testDiscardFormAll() {
        storeController.createStore("store1");
        storeController.createStore("store2");
        storeController.createStore("store3");
        storeController.addOfferedProduct("store1", 123);
        storeController.addOfferedProduct("store2", 123);
        storeController.addOfferedProduct("store3", 123);
        when(storeMock.productIsOffered(123)).thenReturn(true);
        storeController.discardProductFromAll(123);
        assertFalse(storeController.getOfferedProducts("store1").contains(123));
        assertFalse(storeController.getOfferedProducts("store2").contains(123));
        assertFalse(storeController.getOfferedProducts("store3").contains(123));
    }

    @Test
    public void testSwapPosition() {
        storeController.createStore("store1");
        storeController.addShelf("store1", 1, "Normal", 10);
        storeController.addOfferedProduct("store1", 123);
        storeController.addOfferedProduct("store1", 124);
        ProductInfo productInfo1 = new ProductInfo(123, "p1", 1, "Normal");
        ProductInfo productInfo2 = new ProductInfo(124, "p2", 1, "Normal");
        storeController.placeProduct("store1", 1, productInfo1, 1);
        storeController.placeProduct("store1", 1, productInfo2, 5);
        storeController.swapPositions("store1", 1, 1, 5);
        assertEquals(124, storeController.getStoredProducts("store1", 1).get(1).intValue());
        assertEquals(123, storeController.getStoredProducts("store1", 1).get(5).intValue());
    }

    @Test
    public void testSwapPositionStoreNotExistsException() {
        String storeName = "store1";
        Exception exception = assertThrows(RuntimeException.class, () -> storeController.swapPositions(storeName, 1, 1, 5));
        String expectedMessage = "The store " + storeName + " does not exist";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testPlaceProducts() {
        storeController.createStore("store1");
        storeController.addShelf("store1", 1, "Normal", 10);
        storeController.addOfferedProduct("store1", 123);
        storeController.addOfferedProduct("store1", 124);
        ProductInfo prodInfo1 = new ProductInfo(123, "p1", 2, "Normal");
        ProductInfo prodInfo2 = new ProductInfo(124, "p2", 2, "Normal");
        ProductInfo[] products = {prodInfo1, prodInfo2};
        storeController.placeProducts("store1", 1, products);
        assertTrue(storeController.getStoredProducts("store1", 1).contains(123));
        assertTrue(storeController.getStoredProducts("store1", 1).contains(124));
    }

    @Test
    public void testPlaceProductsStoreNotExistsException() {
        String storeName = "store1";
        ProductInfo prodInfo1 = new ProductInfo(123, "p1", 2, "Normal");
        ProductInfo prodInfo2 = new ProductInfo(124, "p2", 2, "Normal");
        ProductInfo[] products = {prodInfo1, prodInfo2};
        Exception exception = assertThrows(RuntimeException.class, () -> storeController.placeProducts(storeName, 1, products));
        String expectedMessage = "The store " + storeName + " does not exist";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testModifyShelfSizeStoreNotExistsException() {
        String storeName = "store1";
        Exception exception = assertThrows(RuntimeException.class, () -> storeController.modifyShelfSize(storeName, 1, 10));
        String expectedMessage = "The store " + storeName + " does not exist";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testProductIsPlaced() {
        storeController.createStore("store1");
        storeController.addShelf("store1", 1, "Normal", 10);
        storeController.addOfferedProduct("store1", 123);
        ProductInfo productInfo = new ProductInfo(123, "p1", 1, "Normal");
        storeController.placeProduct("store1", 1, productInfo, 5);
        when(storeMock.productIsPlaced(123)).thenReturn(true);
        assertTrue(storeController.productIsPlaced("store1", 123));
    }

    @Test
    public void testProductIsPlacedStoreNotExistsException() {
        String storeName = "store1";
        Exception exception = assertThrows(RuntimeException.class, () -> storeController.productIsPlaced(storeName, 123));
        String expectedMessage = "The store " + storeName + " does not exist";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testProductIsPlacedInAny() {
        storeController.createStore("store1");
        storeController.addShelf("store1", 1, "Normal", 10);
        storeController.addOfferedProduct("store1", 123);
        ProductInfo productInfo = new ProductInfo(123, "p1", 1, "Normal");
        storeController.placeProduct("store1", 1, productInfo, 5);
        when(storeMock.productIsPlaced(123)).thenReturn(true);
        assertTrue(storeController.productIsPlacedInAny(123));
        when(storeMock.productIsPlaced(124)).thenReturn(false);
        //assertFalse(storeController.productIsPlacedInAny(124));
    }

    @Test
    public void testContainsProduct() {
        storeController.createStore("store1");
        storeController.addShelf("store1", 1, "Normal", 10);
        storeController.addOfferedProduct("store1", 123);
        ProductInfo productInfo = new ProductInfo(123, "p1", 1, "Normal");
        storeController.placeProduct("store1", 1, productInfo, 5);
        when(storeMock.containsProduct(1, 123)).thenReturn(true);
        assertTrue(storeController.containsProducts("store1", 1));
    }

    @Test
    public void testContainsProductStoreNotExistsException() {
        String storeName = "store1";
        Exception exception = assertThrows(RuntimeException.class, () -> storeController.containsProducts(storeName, 1));
        String expectedMessage = "The store " + storeName + " does not exist";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }
}
