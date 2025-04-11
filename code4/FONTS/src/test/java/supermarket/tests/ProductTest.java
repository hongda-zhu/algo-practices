package supermarket.tests;

import org.junit.Test;
import supermarket.domain.supermarket.Product;

import static org.junit.Assert.*;

public class ProductTest {

    @Test
    public void testProductCreation() {
        Product p = new Product(1,"banana",1.f,"normal");
        assertEquals("Expected product name to be 'banana'", "banana", p.getName());
        assertEquals("Expected product price to be 1.0", 1.f, p.getPrice(), 0.0);
        assertEquals("Expected product barcode to be 1", 1, p.getBarcode());
        assertEquals("Expected shelf type to be 'normal'", "normal", p.getShelfType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativePrice() {
        new Product(1,"banana",-1.f,"normal");
    }

    @Test
    public void testSetPrice() {
        Product p = new Product(1,"banana",1.f,"normal");
        p.setPrice(2.f);
        assertEquals("Expected product price to be updated to 2.0", 2.f, p.getPrice(), 0.0);
    }

    @Test
    public void testSetPriceNegative() {
        Product p = new Product(1,"banana",1.f,"normal");
        try {
            p.setPrice(-1.0f);
            fail("Expected IllegalArgumentException for negative price");
        } catch (IllegalArgumentException e) {
            assertEquals("Expected exception message for negative price", "Price cannot be negative", e.getMessage());
        }
    }

    @Test
    public void testSetShelfType() {
        Product p = new Product(1,"banana",1.f,"normal");
        p.setShelfType("fruitOnly");
        assertEquals("Expected shelf type to be updated to 'fruitOnly'", "fruitOnly", p.getShelfType());
    }

}
