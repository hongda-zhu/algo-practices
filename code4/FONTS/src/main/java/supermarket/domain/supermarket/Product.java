package supermarket.domain.supermarket;

import supermarket.tuples.ProductInfo;

/**
 * Represents a product type in the supermarket domain.
 * @autor Dídac Dalmases Valcarcel
 */
public class Product {
    int barcode;
    String name;
    float price;
    String shelfType;

    /**
     * Constructor for Product class.
     * @param barcode Barcode of the product.
     * @param name Name of the product.
     * @param price Price of the product.
     * @param shelfType ShelfType of the product.
     */
    public Product(int barcode, String name, float price, String shelfType) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.barcode = barcode;
        if (barcode < 0) {
            throw new IllegalArgumentException("Barcode cannot be negative");
        }
        this.name = name;
        this.price = price;
        this.shelfType = shelfType;
    }

    /**
     * Sets price of the product.
     * @param price Price to be set.
     */
    public void setPrice(float price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = price;
    }

    /**
     * Sets shelfType of the product.
     * @param shelfType ShelfType to be set.
     */
    public void setShelfType(String shelfType) {
        this.shelfType = shelfType;
    }

    /**
     * Gets barcode of the product.
     * @return Returns barcode of the product.
     */
    public int getBarcode() {
        return barcode;
    }

    /**
     * Gets name of the product.
     * @return Returns name of the product.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets price of the product.
     * @return Returns price of the product.
     */
    public float getPrice() {
        return price;
    }

    /**
     * Gets shelfType of the product.
     * @return Returns shelfType of the product.
     */
    public String getShelfType() {
        return shelfType;
    }

    /**
     * Creates a ProductInfo record with the information of the product.
     * @return new ProductInfo with values matching the product characteristics.
     */
    public ProductInfo toProductInfo() {
        return new ProductInfo(barcode, name, price, shelfType);
    }
}
