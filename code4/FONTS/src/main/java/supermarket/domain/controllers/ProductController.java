package supermarket.domain.controllers;

import supermarket.domain.supermarket.Product;
import supermarket.tuples.ProductInfo;

import java.util.*;

/**
 * ProductController class is responsible for managing products.
 * It can define, delete, modify and get information about products.
 * @author Dídac Dalmases Valcárcel
 */
public class ProductController {
    private final Map<Integer, Product> products;
    public ProductController() {
        products = new HashMap<>();
    }

    /**
     * Returns number of products defined.
     * @return Number of products defined.
     */
    public int getNProducts() { return products.size(); }

    /**
     * Checks if product exists.
     * @param barcode Barcode of the product to be checked.
     * @return Returns true if product exists, false if it doesn't.
     */
    public boolean existsProduct(int barcode) {
        return products.containsKey(barcode);
    }

    /**
     * Gets product from its barcode, and throws an error if it doesn't exist.
     * @param barcode The barcode of the desired product.
     * @return Returns the product with the given barcode.
     */
    private Product getProductFromBarcode(int barcode) {
        if (!products.containsKey(barcode)) throw new RuntimeException("The product with this barcode " + barcode + " does not exist");
        return products.get(barcode); //we return it either if it exists or not, if it doesn't exist it will return null
    }

    /**
     * Gets product info from its barcode, and throws an error if it doesn't exist.
     * @param barcode The barcode of the desired product.
     * @return Returns the product info with the given barcode.
     */
    public ProductInfo getProductInfo(int barcode) {
        return getProductFromBarcode(barcode).toProductInfo();
    }

    /**
     * Defines and creates new product, so it can be used.
     *
     * @param barcode Identifier of the product.
     * @param name    Name of the product.
     * @param price   Price of the product.
     * @param shelfType Shelf type which this product belongs to.
     */
    public void defineProduct(int barcode, String name, float price, String shelfType) {
        if (existsProduct(barcode)) {
            throw new RuntimeException("The product with this barcode " + barcode + " already exists");
        }
        Product newProduct = new Product(barcode, name, price, shelfType);
        products.put(barcode, newProduct);
    }

    /**
     * Deletes the product and discards it from all stores.
     * @param barcode Identifier of the product to be deleted.
     */
    public void deleteProduct(int barcode) {
        if (products.remove(barcode) == null) {
            throw new RuntimeException("The product with this barcode " + barcode + " does not exist");
        }
    }

    /**
     * Returns a sorted set with all defined barcodes (barcodes of all defined products).
     * @return  Ordered set of all defined barcodes.
     */
    public Set<Integer> getDefinedBarcodes() {
        return new TreeSet<>(products.keySet());
    }

    /**
     * Modifies the price of the specified product
     * @param barcode Identifier of the product to be shown.
     * @param price Price to be set for the product.
     */
    public void modifyPriceProduct(int barcode, float price) {
        Product product = getProductFromBarcode(barcode);
        if (product != null) product.setPrice(price);
    }

    /**
     * Modifies the price of the specified product
     * @param barcode Identifier of the product to be shown.
     * @param shelfType ShelfType to be set for the product.
     */
    public void modifyShelfTypeProduct(int barcode, String shelfType) {
        Product product = getProductFromBarcode(barcode);
        if (product != null) product.setShelfType(shelfType);
    }

    /**
     * Returns the ShelfType of the specified product.
     * @param barcode Identifier of the product to be shown.
     * @return Returns ShelfType of the product with barcode.
     */
    public String getShelfType(int barcode) {
        Product product = getProductFromBarcode(barcode);
        return product.getShelfType();
    }

    /**
     * Returns the name of the specified product.
     * @param barcode Identifier of the product to be shown.
     * @return Returns the name of the product with the specified barcode.
     */
    public String getName(int barcode) {
        Product product = getProductFromBarcode(barcode);
        return product.getName();
    }

    /**
     * Returns the price of the specified product.
     * @param barcode Identifier of the product to be shown.
     * @return Returns the price of the product with the specified barcode.
     */
    public float getPrice(int barcode) {
        Product product = getProductFromBarcode(barcode);
        return product.getPrice();
    }
}
