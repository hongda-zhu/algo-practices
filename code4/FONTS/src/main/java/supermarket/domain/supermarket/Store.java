package supermarket.domain.supermarket;

import supermarket.exceptions.ExceptionShelfNotExists;
import supermarket.tuples.ProductInfo;

import java.util.*;

/**
 * Represents a store in the supermarket domain.
 * The Store class allows management of shelves and products within the store.
 *
 * @author Eric Medina León
 */
public class Store {
    String name;
    final Map<Integer,Shelf> shelves;
    final Set<Integer> offeredProductsBarcodes;
    final Set<Integer> placeableProductsBarcodes;

    /**
     * Creates a store with the given name.
     *
     * @param name Unique name associated with the store.
     */
    public Store(String name) {
        this.name = name;
        this.shelves = new HashMap<>();
        offeredProductsBarcodes = new HashSet<>();
        placeableProductsBarcodes = new HashSet<>();
    }

    /**
     * Retrieves the name of the store.
     *
     * @return The name of the store.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves a shelf by its ID.
     *
     * @param shelfId The ID of the shelf to retrieve.
     * @return The Shelf object associated with the given ID, or null if not found.
     */
    public Shelf getShelf(int shelfId) {
        return shelves.get(shelfId);
    }

    /**
     * Retrieves the IDs of all shelves in the store.
     *
     * @return A set containing the IDs of all shelves.
     */
    public Set<Integer> getShelfIds() {
        return shelves.keySet();
    }

    /**
     * Retrieves the barcodes of all products currently offered in the store.
     *
     * @return A set containing the barcodes of offered products.
     */
    public Set<Integer> getOfferedProductsBarcodes() {
        return offeredProductsBarcodes;
    }

    /**
     * Retrieves the barcodes of all products currently placeable (offered and not yet placed in a shelf) in the store.
     *
     * @return A set containing the barcodes of placeable products.
     */
    public Set<Integer> getPlaceableProductsBarcodes() {
        return placeableProductsBarcodes;
    }

    /**
     * Retrieves the list of product positions on a specific shelf.
     *
     * @param shelfId The ID of the shelf to retrieve the product distribution from.
     * @return A list representing the product positions on the shelf.
     */
    public List<Integer> getStoredProducts(int shelfId) {
        return shelves.get(shelfId).getStoredProducts();
    }

    /**
     * Sets the name of the store.
     *
     * @param name The new name for the store.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Checks if a shelf exists in the store.
     *
     * @param shelfId The ID of the shelf to check.
     * @return true if the shelf exists, false otherwise.
     */
    public boolean shelfExists(int shelfId) {
        return shelves.containsKey(shelfId);
    }

    /**
     * Checks if a product is offered by the store.
     *
     * @param productBarcode The barcode of the product to check.
     * @return true if the product is offered, false otherwise.
     */
    public boolean productIsOffered(int productBarcode) {
        return offeredProductsBarcodes.contains(productBarcode);
    }

    /**
     * Checks if a product is already placed in any shelf within the store.
     *
     * @param productBarcode The barcode of the product to check.
     * @return true if the product is placed in any shelf; false otherwise.
     */
    public boolean productIsPlaced(int productBarcode) {
        return !placeableProductsBarcodes.contains(productBarcode);
    }

    /**
     * Adds a new shelf to the store.
     *
     * @param shelfId   The ID of the shelf to add.
     * @param shelfType The type of the shelf.
     * @param size      The size of the shelf.
     */
    public void addShelf(int shelfId, String shelfType, int size) {
        if (shelves.containsKey(shelfId)) {
            throw new RuntimeException("The shelf with ID " + shelfId + " already exists in the store " + name);
        }
        Shelf newShelf = new Shelf(shelfId, shelfType, size);
        shelves.put(shelfId, newShelf);
    }

    /**
     * Withdraws all products from a shelf.
     * @param shelfId The ID of the shelf to clear all products.
     */
    public void clearShelf(int shelfId) {
        Shelf shelf = shelves.get(shelfId);
        placeableProductsBarcodes.addAll(shelf.clearShelf());
    }

    /**
     * Removes a shelf from the store by its ID.
     *
     * @param shelfId The ID of the shelf to remove.
     */
    public void removeShelf(int shelfId) {
        if (shelves.get(shelfId) == null) {
            throw new ExceptionShelfNotExists(shelfId,name);
        }
        clearShelf(shelfId);
        shelves.remove(shelfId);
    }

    /**
     * Checks if a specific shelf contains the given product.
     *
     * @param shelfId       The ID of the shelf to check.
     * @param productBarcode The barcode of the product to check.
     * @return true if the shelf contains the product, false otherwise.
     */
    public boolean containsProduct(int shelfId, int productBarcode) {
        Shelf shelf = shelves.get(shelfId);
        return shelf != null && shelf.containsProduct(productBarcode);
    }

    /**
     * Checks if a specific position on a shelf is free.
     *
     * @param shelfId       The ID of the shelf to check.
     * @param shelfPosition The position on the shelf to check.
     * @return true if the position is free, false otherwise.
     */
    public boolean isPositionFree(int shelfId, int shelfPosition) {
        Shelf shelf = shelves.get(shelfId);
        return shelf != null && shelf.isPositionFree(shelfPosition);
    }

    /**
     * Places a product on a specified shelf at a given position. It will replace any product that was present in that location before.
     *
     * @param shelfId           The ID of the shelf where the product will be placed.
     * @param productInfo       The info of the product to place.
     * @param shelfPosition     The position on the shelf where the product will be placed.
     */
    public void placeProduct(int shelfId, ProductInfo productInfo, int shelfPosition) {
        if(!productIsOffered(productInfo.barcode())) {
            throw new RuntimeException("The store " + name + " does not offer the product " + productInfo + ".\n");
        }
        if(productIsPlaced(productInfo.barcode())) {
            throw new RuntimeException("The product " + productInfo + " is already placed in the store.\n");
        }
        Shelf shelf = shelves.get(shelfId);
        if(shelf == null) {
            throw new ExceptionShelfNotExists(shelfId,name);
        }
        Integer removedBarcode = shelf.placeProduct(productInfo, shelfPosition);
        if (removedBarcode != null) placeableProductsBarcodes.add(removedBarcode);
        placeableProductsBarcodes.remove(productInfo.barcode());
    }

    /**
     * Takes out a product from a specified shelf (if present).
     *
     * @param shelfId        The ID of the shelf from which the product will be taken out.
     * @param productBarcode  The barcode of the product to take out.
     */
    public void withdrawProduct(int shelfId, int productBarcode) {
        Shelf shelf = shelves.get(shelfId);
        if (shelf == null) {
            throw new ExceptionShelfNotExists(shelfId,name);
        }
        shelf.withdrawProduct(productBarcode);
        placeableProductsBarcodes.add(productBarcode);
    }

    /**
     * Adds a product to the set of offered products.
     *
     * @param productBarcode The barcode of the product to add.
     */
    public void addOfferedProduct(int productBarcode) {
        if(productIsOffered(productBarcode)) {
            throw new RuntimeException("The product " + productBarcode + " is already offered in the store " + name);
        }
        offeredProductsBarcodes.add(productBarcode);
        placeableProductsBarcodes.add(productBarcode);
    }

    /**
     * Discards a product from the set of offered products.
     * If the product is found on any shelf, it will also be removed from the shelf.
     *
     * @param productBarcode The barcode of the product to discard.
     */
    public void discardOfferedProduct(int productBarcode) {
        if(!productIsOffered(productBarcode)) throw new RuntimeException("The product " + productBarcode + " is not offered in the store " + name);
        offeredProductsBarcodes.remove(productBarcode);
        placeableProductsBarcodes.remove(productBarcode);
        for (Shelf shelf : shelves.values()) {
            if (shelf.containsProduct(productBarcode)) {
                shelf.withdrawProduct(productBarcode);
            }
        }
    }

    /**
     * Swaps the products at two specified positions on a shelf.
     *
     * @param shelfId    The ID of the shelf where the products will be swapped.
     * @param position1  The position of the first product.
     * @param position2  The position of the second product.
     */
    public void swapPositions(int shelfId, int position1, int position2) {
        if(shelves.get(shelfId) == null) {
            throw new ExceptionShelfNotExists(shelfId,name);
        }
        shelves.get(shelfId).swapPositions(position1, position2);
    }

    /**
     * Places a list of products on a specified shelf (from position 0 to the size of productsOfShelf). This will override all shelf products at the placed positions.
     * If the array size is larger than the shelf size, it will ignore the ones in the end, and show a warning.
     *
     * @param shelfId      The ID of the shelf where the product will be placed.
     * @param productInfos The info of the products to place.
     */
    public void placeProducts(Integer shelfId, ProductInfo[] productInfos) {
        int length = productInfos.length;
        int shelfSize = shelves.get(shelfId).size;
        if (length > shelfSize) {
            System.err.println("Trying to place " + length + " products to the shelf with id " + shelfId + ", which only has size of " + shelfSize + ". Ignoring additional products.");
            length = shelfSize;
        }
        for (int i = 0; i < length; ++i) {
            if (productInfos[i] != null) placeProduct(shelfId, productInfos[i], i);
        }
    }

    /**
     * Modifies the size of a shelf. If the new size is smaller than the current size, the products that are removed will be added to the set of placeable products.
     *
     * @param shelfId    The ID of the shelf to modify.
     * @param shelfSize  The new size of the shelf.
     */
    public void modifyShelfSize(int shelfId, int shelfSize) {
        if(shelves.get(shelfId) == null) {
            throw new ExceptionShelfNotExists(shelfId,name);
        }
        List<Integer> removedProducts = shelves.get(shelfId).setShelfSize(shelfSize);
        placeableProductsBarcodes.addAll(removedProducts);
    }

    /**
     * Returns a boolean indicating if the shelf contains any products.
     * @param shelfId ID of the shelf that will get the products stored in
     * @return True if the shelf contains any products, false otherwise.
     */
    public boolean containsProducts(int shelfId) {
        if (shelves.get(shelfId) == null) {
            throw new ExceptionShelfNotExists(shelfId, name);
        }
        return shelves.get(shelfId).containsProducts();
    }
}
