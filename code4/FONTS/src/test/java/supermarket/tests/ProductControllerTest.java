package supermarket.tests;

import org.junit.Before;
import org.junit.Test;
import supermarket.domain.controllers.ProductController;
import supermarket.domain.supermarket.Product;
import supermarket.tuples.ProductInfo;

import java.util.HashMap;
import java.util.Set;

import static org.junit.Assert.*;


public class ProductControllerTest {
    final int NPRODUCTS = 6;
    ProductController productController;
    HashMap<Integer, Product> testProducts;

    @Before
    public void setUp() {
        productController = new ProductController();
        testProducts = new HashMap<>();
        for (int i = 0; i < NPRODUCTS; i++) {
            String name = "p" + i;
            productController.defineProduct(i,name,1.f,"normal");
            testProducts.put(i,new Product(i,name,1.f,"normal"));
        }
    }

    @Test
    public void testExistsProduct() {
        for (int i = 0; i < NPRODUCTS; i++)
            assertTrue("Product with barcode " + i + " should exist", productController.existsProduct(i));
        for (int i = NPRODUCTS; i < 2*NPRODUCTS; i++)
            assertFalse("Product with barcode " + i + " should not exist", productController.existsProduct(i));
    }

    @Test
    public void testGetProductInfo() {
        for (int i = 0; i < NPRODUCTS; i++) {
            Product expectedProduct = testProducts.get(i);
            ProductInfo resultProduct = productController.getProductInfo(i);
            assertEquals("Barcode should match for product " + i, expectedProduct.getBarcode(), resultProduct.barcode());
            assertEquals("Product name should match for product " + i, expectedProduct.getName(), resultProduct.name());
            assertEquals("Product price should match for product " + i, expectedProduct.getPrice(), resultProduct.price(), 0.f);
            assertEquals("Shelf type should match for product " + i, expectedProduct.getShelfType(), resultProduct.shelfType());
        }
        for (int i = NPRODUCTS; i < 2*NPRODUCTS; i++) {
            try {
                productController.getProductInfo(i);
                fail("Expected exception for non-existent product with barcode " + i);
            }
            catch (Exception e) {
                assertTrue("Exception should be thrown for non-existent product", true);
            }
        }
    }

    @Test
    public void testNegativePrice() {
        try {
            productController.defineProduct(10,"p01",-1.f,"normal");
            fail("Price can't be negative for product p01");
        }
        catch (Exception e) {
            assertTrue("Exception should be thrown for negative price", e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testExistingProduct() {
        int expectedN = productController.getNProducts();
        try {
            productController.defineProduct(0,"p0",1.f,"normal");
        }
        catch (Exception e) {
            int resultN = productController.getNProducts();
            assertEquals("Number of products should remain the same after trying to define an existing product", expectedN, resultN);
        }
    }

    @Test
    public void testDeleteProductExistent() {
        productController.deleteProduct(0);
        assertFalse("Product with barcode 0 should no longer exist", productController.existsProduct(0));
    }

    @Test
    public void testDeleteProductNonExistent() {
        try {
            productController.deleteProduct(10);
            fail("Expected exception for trying to delete non-existent product with barcode 10");
        }
        catch (Exception e) {
            assertTrue("Exception should be thrown for deleting non-existent product", true);
        }
    }

    @Test
    public void testAddAndDelete()  {
        productController.defineProduct(10,"p10",1.f,"normal");
        assertTrue("Product with barcode 10 should be added", productController.existsProduct(10));
        productController.deleteProduct(10);
        assertFalse("Product with barcode 10 should be deleted", productController.existsProduct(10));
    }

    @Test
    public void testAddWithNegative() {
        try {
            productController.defineProduct(-1, "p10", 1.f, "normal");
            fail("Barcode can't be negative for product p10");
        }
        catch (Exception e) {
            assertTrue("Exception should be thrown for negative barcode", e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testGetDefinedBarcodes() {
        Set<Integer> result = productController.getDefinedBarcodes();
        for (Integer resultBarcode : result) {
            assertTrue("Product with barcode " + resultBarcode + " should be in testProducts", testProducts.containsKey(resultBarcode));
        }
    }

    @Test
    public void testGetDefinedBarcodesAfterAdding() {
        Set<Integer> result = productController.getDefinedBarcodes();
        for (Integer resultBarcode : result) {
            assertTrue("Product with barcode " + resultBarcode + " should be in testProducts", testProducts.containsKey(resultBarcode));
        }
    }

    @Test
    public void testModifyPrice() {
        productController.modifyPriceProduct(0,5.f);
        float resultPrice = productController.getPrice(0);
        assertEquals("Product price for barcode 0 should be modified to 5.0", 5.f, resultPrice, 0.0);
    }

    @Test
    public void testModifyPriceNegative() {
        try {
            productController.modifyPriceProduct(0,-5.f);
            fail("Price can't be negative for product with barcode 0");
        }
        catch (Exception e) {
            assertTrue("Exception should be thrown for negative price", e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testModifyPriceNonExistent() {
        try {
            productController.modifyPriceProduct(10, 0.f);
            fail("Expected exception for modifying price of non-existent product with barcode 10");
        }
        catch (Exception e) {
            assertTrue("Exception should be thrown for modifying price of non-existent product", true);
        }
    }

    @Test
    public void testModifyShelfType() {
        String expectedShelf = "different";
        productController.modifyShelfTypeProduct(0, expectedShelf);
        assertEquals("Shelf type for product with barcode 0 should be modified to 'different'", expectedShelf, productController.getShelfType(0));
    }

    @Test
    public void testModifyShelfNonExistent() {
        try {
            productController.modifyShelfTypeProduct(10, "different");
            fail("Expected exception for modifying shelf type of non-existent product with barcode 10");
        }
        catch (Exception e) {
            assertTrue("Exception should be thrown for modifying shelf type of non-existent product", true);
        }
    }
}
