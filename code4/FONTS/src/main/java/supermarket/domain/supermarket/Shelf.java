package supermarket.domain.supermarket;

import supermarket.tuples.ProductInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a shelf in a store.
 * Each shelf holds products at specific positions and can be resized as needed.
 *
 * @author Eric Medina León
 */
public class Shelf {
    final int id;
    int size;
    String shelfType;

    /**
     * List of stored product barcodes, indexed with shelf position. If a position is null, it does not contain a product.
     */
    final List<Integer> storedProducts;

    /**
     * Constructs a new Shelf with the specified ID, type, and size.
     *
     * @param id        The unique identifier for the shelf.
     * @param shelfType The type or category of the shelf.
     * @param size      The initial size of the shelf (number of product positions).
     */
    public Shelf(int id, String shelfType, int size) {
        this.id = id;
        this.size = size;
        this.shelfType = shelfType;
        this.storedProducts = new ArrayList<>();
        for (int i = 0; i < size; ++i) storedProducts.add(null);
    }

    /**
     * Retrieves the ID of the shelf.
     *
     * @return The shelf ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Retrieves the current size of the shelf.
     *
     * @return The size of the shelf (number of positions).
     */
    public int getSize() {
        return size;
    }

    /**
     * Retrieves the type of the shelf.
     *
     * @return The type of the shelf.
     */
    public String getShelfType() {
        return shelfType;
    }

    /**
     * Retrieves the list of product positions on the shelf.
     * Each position corresponds to a product barcode or null if the position is empty.
     *
     * @return A list of product barcodes, representing the positions on the shelf.
     */
    public List<Integer> getStoredProducts() {
        return storedProducts;
    }

    /**
     * Clears the shelf. All products will be withdrawn from the shelf, and returned as a list of barcodes.
     *
     * @return List of barcodes of all products removed (the shelf will end up empty)
     */
    public List<Integer> clearShelf() {
        List<Integer> removedBarcodes = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            if (storedProducts.get(i) != null) {
                removedBarcodes.add(storedProducts.get(i));
                storedProducts.set(i, null);
            }
        }
        return removedBarcodes;
    }

    /**
     * Sets a new type for the shelf. All products will be withdrawn from the shelf, and returned as a list of barcodes.
     *
     * @param shelfType The new type for the shelf.
     * @return List of barcodes of all products removed (the shelf will end up empty)
     */
    public List<Integer> setShelfType(String shelfType) {
        this.shelfType = shelfType;
        return clearShelf();
    }

    /**
     * Adjusts the size of the shelf.
     * If the new size is larger, the shelf is expanded.
     * If the new size is smaller, the shelf is reduced, and any products at positions beyond the new size are removed.
     *
     * @param newSize The new size for the shelf.
     * @return Returns list of removed barcodes (in case the shelf shrunk in size and removed products at the end
     */
    public List<Integer> setShelfSize(int newSize) {
        List<Integer> removedBarcodes = new ArrayList<>();
        if (newSize < size) {
            for (int i = 0; i < size - newSize; ++i) {
                if (storedProducts.getLast() != null) removedBarcodes.add(storedProducts.getLast());
                storedProducts.removeLast();
            }
        }
        else {
            for (int i = 0; i < newSize - size; ++i) {
                storedProducts.add(null);
            }
        }
        this.size = newSize;
        return removedBarcodes;
    }

    /**
     * Places a product at the specified position on the shelf.
     *
     * @param productInfo   The info of the product to place.
     * @param position      The position on the shelf to place the product.
     * @return              The barcode of the product that was removed (previous product in that position, null if none).
     */
    public Integer placeProduct(ProductInfo productInfo, int position) {
        if(!shelfType.equals(productInfo.shelfType())) {
            throw new RuntimeException("The shelf type of product " + productInfo.shelfType() + " does not match shelf type " + shelfType);
        }
        if(storedProducts.contains(productInfo.barcode())) {
            throw new RuntimeException("The product " + productInfo + " is already placed.");
        }
        Integer removedBarcode = storedProducts.get(position);
        storedProducts.set(position, productInfo.barcode());
        return removedBarcode;
    }

    /**
     * Withdraws the product with the given barcode if present.
     * The position will be cleared and the product removed from the set of stored products.
     *
     * @param productBarcode The barcode of the product to withdraw.
     */
    public void withdrawProduct(int productBarcode) {
        if (storedProducts.contains(productBarcode)) {
            storedProducts.set(storedProducts.indexOf(productBarcode), null);
        }
    }

    /**
     * Swaps the products at two specified positions on the shelf. Throws an error if one position is out of bounds
     *
     * @param position1 The first position.
     * @param position2 The second position.
     */
    public void swapPositions(int position1, int position2) {
        if (position1 < 0 || position1 >= size) {
            throw new RuntimeException("First position to swap is invalid: " + position1);
        }
        if (position2 < 0 || position2 >= size) {
            throw new RuntimeException("Second position to swap is invalid: " + position2);
        }
        Integer productBarcode1 = storedProducts.get(position1);
        if (storedProducts.get(position2) != null) storedProducts.set(position1, storedProducts.get(position2));
        else storedProducts.set(position1, null);

        storedProducts.set(position2, productBarcode1);
    }

    /**
     * Checks if a specific product is stored on the shelf.
     *
     * @param productBarcode The barcode of the product to check.
     * @return true if the product is stored on the shelf, false otherwise.
     */
    public boolean containsProduct(int productBarcode) {
        return storedProducts.contains(productBarcode);
    }

    /**
     * Checks if a specific position on the shelf is free (i.e., does not contain a product).
     *
     * @param shelfPosition The position to check.
     * @return true if the position is free, false otherwise.
     */
    public boolean isPositionFree(int shelfPosition) {
        return shelfPosition < size && storedProducts.get(shelfPosition) == null;
    }

    /**
     * Returns a boolean indicating if the shelf contains any products.
     * @return True if the shelf contains any products, false otherwise.
     */
    public boolean containsProducts() {
        for (Integer barcode : storedProducts) if (barcode != null) return true;
        return false;
    }
}
