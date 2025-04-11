package supermarket.domain.controllers;

import supermarket.domain.supermarket.Store;
import supermarket.exceptions.ExceptionStoreNotExists;
import supermarket.tuples.ProductInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages multiple stores in the supermarket domain.
 * The StoreController class allows creating, deleting, and managing stores,
 * as well as handling shelves and products within those stores.
 */
public class StoreController {
    private final Map<String, Store> stores;

    /**
     * Initializes a new StoreController with an empty list of stores.
     */
    public StoreController() {
        stores = new HashMap<>();
    }

    /**
     * Validates if a store exists.
     *
     * @param storeName The name of the store to check.
     * @return true if the store exists; false otherwise.
     */
    public boolean exists(String storeName) {
        return stores.containsKey(storeName);
    }

    /**
     * Creates a new store with the given name.
     *
     * @param storeName The name of the store to create.
     */
    public void createStore(String storeName) {
        if (exists(storeName)) {
            throw new RuntimeException("Store with name " + storeName + " already exists");
        }
        stores.put(storeName, new Store(storeName));
    }

    /**
     * Deletes a store by its name.
     *
     * @param storeName The name of the store to delete.
     */
    public void deleteStore(String storeName) {
        if (!exists(storeName)) {
           throw new ExceptionStoreNotExists(storeName);
        }
        stores.remove(storeName);
    }

    /**
     * Retrieves the IDs of all shelves in a specific store.
     *
     * @param storeName The name of the store.
     * @return A set of shelf IDs, or null if the store does not exist.
     */
    public Set<Integer> getShelfIds(String storeName) {
        if (!exists(storeName)) {
            throw new ExceptionStoreNotExists(storeName);
        }
        return stores.get(storeName).getShelfIds();
    }

    /**
     * Retrieves the type of shelf by its ID in a specific store.
     *
     * @param storeName The name of the store.
     * @param shelfId   The ID of the shelf to retrieve.
     * @return The type of the shelf, or null if the store or shelf does not exist.
     */
    public String getShelfType(String storeName, int shelfId) {
        if (!exists(storeName)) {
            throw new ExceptionStoreNotExists(storeName);
        }

        return stores.get(storeName).getShelf(shelfId).getShelfType();
    }

    /**
     * Retrieves the barcodes of all products currently offered in a specific store.
     *
     * @param storeName The name of the store.
     * @return A set of offered product barcodes, or null if the store does not exist.
     */
    public Set<Integer> getOfferedProducts(String storeName) {
        if (!exists(storeName)) {
            throw new ExceptionStoreNotExists(storeName);
        }
        return stores.get(storeName).getOfferedProductsBarcodes();
    }

    /**
     * Retrieves the barcodes of all products currently placeable (offered and not placed in a shelf) in a specific store.
     *
     * @param storeName The name of the store.
     * @return A set of placeable product barcodes, or null if the store does not exist.
     */
    public Set<Integer> getPlaceableProducts(String storeName) {
        if (!exists(storeName)) {
            throw new ExceptionStoreNotExists(storeName);
        }
        return stores.get(storeName).getPlaceableProductsBarcodes();
    }

    /**
     * Retrieves the names of all stores managed by the controller.
     *
     * @return A set of store names.
     */
    public Set<String> getStoreNames() {
        return stores.keySet();
    }

    /**
     * Changes the name of an existing store.
     *
     * @param storeName     The current name of the store.
     * @param newStoreName  The new name for the store.
     */
    public void changeStoreName(String storeName, String newStoreName) {
        if (!exists(storeName)) {
           throw new ExceptionStoreNotExists(storeName);
        }
        if (exists(newStoreName)) {
            throw new RuntimeException("A store with the name " + newStoreName + " already exists");
        }
        Store store = stores.get(storeName);
        store.setName(newStoreName);
        stores.remove(storeName);
        stores.put(newStoreName, store);
    }

    /**
     * Checks if a specific product is offered by a specific store.
     *
     * @param storeName     The name of the store.
     * @param productBarcode The barcode of the product to check.
     * @return true if the product is offered; false otherwise.
     */
    public boolean storeOffersProduct(String storeName, int productBarcode) {
        if (!exists(storeName)) {
            throw new ExceptionStoreNotExists(storeName);
        }
        return stores.get(storeName).productIsOffered(productBarcode);
    }

    /**
     * Adds a new shelf to a specific store.
     *
     * @param storeName   The name of the store to add the shelf to.
     * @param shelfId     The ID of the shelf to add.
     * @param shelfType   The type of the shelf.
     * @param size        The size of the shelf.
     */
    public void addShelf(String storeName, int shelfId, String shelfType, int size) {
        if (!exists(storeName)) {
            throw new ExceptionStoreNotExists(storeName);
        }
        stores.get(storeName).addShelf(shelfId, shelfType, size);
    }

    /**
     * Clears all products from a shelf (makes the shelf empty).
     *
     * @param storeName The name of the store.
     * @param shelfId   The ID of the shelf to clear all their products.
     */
    public void clearShelf(String storeName, int shelfId) {
        if (!exists(storeName)) {
            throw new ExceptionStoreNotExists(storeName);
        }
        stores.get(storeName).clearShelf(shelfId);
    }

    /**
     * Removes a shelf from a specific store by its ID.
     *
     * @param storeName The name of the store.
     * @param shelfId   The ID of the shelf to remove.
     */
    public void removeShelf(String storeName, int shelfId) {
        if (!exists(storeName)) {
            throw new ExceptionStoreNotExists(storeName);
        }
        stores.get(storeName).removeShelf(shelfId);
    }

    /**
     * Retrieves the stored products on a specific shelf in a store.
     *
     * @param storeName The name of the store.
     * @param shelfId   The ID of the shelf to check.
     * @return A list representing the distribution of products, or null if the store or shelf does not exist.
     */
    public List<Integer> getStoredProducts(String storeName, int shelfId) {
        if (!exists(storeName)) {
            throw new ExceptionStoreNotExists(storeName);
        }
        return stores.get(storeName).getStoredProducts(shelfId);
    }

    /**
     * Places a product on a specific shelf at a given position in a store.
     *
     * @param storeName        The name of the store.
     * @param shelfId          The ID of the shelf where the product will be placed.
     * @param productInfo      The info of the product to place.
     * @param shelfPosition    The position on the shelf where the product will be placed.
     */
    public void placeProduct(String storeName, int shelfId, ProductInfo productInfo, int shelfPosition) {
        if (!exists(storeName)) {
            throw new ExceptionStoreNotExists(storeName);
        }
        stores.get(storeName).placeProduct(shelfId, productInfo, shelfPosition);
    }

    /**
     * Withdraws a product from a specific shelf in a store.
     *
     * @param storeName     The name of the store.
     * @param shelfId       The ID of the shelf from which the product will be withdrawn.
     * @param productBarcode The barcode of the product to withdraw.
     */
    public void withdrawProduct(String storeName, int shelfId, int productBarcode) {
        if (!exists(storeName)) {
            throw new ExceptionStoreNotExists(storeName);
        }

        if (!stores.get(storeName).containsProduct(shelfId, productBarcode)) {
            throw new RuntimeException("The shelf " + shelfId + " (store: " + storeName + ") does not contain the product " + productBarcode + "\n");
        }
        stores.get(storeName).withdrawProduct(shelfId, productBarcode);
    }

    /**
     * Adds a product to the set of offered products in a specific store.
     *
     * @param storeName     The name of the store.
     * @param productBarcode The barcode of the product to add.
     */
    public void addOfferedProduct(String storeName, int productBarcode) {
        if (!exists(storeName)) {
            throw new ExceptionStoreNotExists(storeName);
        }
        stores.get(storeName).addOfferedProduct(productBarcode);
    }

    /**
     * Discards a product from the set of offered products in a specific store.
     *
     * @param storeName     The name of the store.
     * @param productBarcode The barcode of the product to discard.
     */
    public void discardOfferedProduct(String storeName, int productBarcode) {
        if (!exists(storeName)) {
            throw new ExceptionStoreNotExists(storeName);
        }
        stores.get(storeName).discardOfferedProduct(productBarcode);
    }

    /**
     * Discards a product from all stores in the system.
     *
     * @param productBarcode The barcode of the product to discard.
     */
    public void discardProductFromAll(int productBarcode) {
        for (Store store : stores.values()) {
            if(store.productIsOffered(productBarcode)) {
                store.discardOfferedProduct(productBarcode);
            }
        }
    }

    /**
     * Swaps the positions of products on a specific shelf in a store.
     *
     * @param storeName The name of the store.
     * @param shelfId   The ID of the shelf where the products will be swapped.
     * @param position1 The first position to swap.
     * @param position2 The second position to swap.
     */
    public void swapPositions(String storeName, int shelfId, int position1, int position2) {
        if (!exists(storeName)) {
            throw new ExceptionStoreNotExists(storeName);
        }
        stores.get(storeName).swapPositions(shelfId, position1, position2);
    }

    /**
     * Places multiple products on a specific shelf in a store.
     *
     * @param storeName         The name of the store.
     * @param shelfId           The ID of the shelf where the products will be placed.
     * @param productsOfShelf   The products to place.
     */
    public void placeProducts(String storeName, Integer shelfId, ProductInfo[] productsOfShelf) {
        if (!exists(storeName)) {
            throw new ExceptionStoreNotExists(storeName);
        }
        stores.get(storeName).placeProducts(shelfId,productsOfShelf);
    }

    /**
     * Modifies the shelf size of a specific shelf in a store.
     *
     * @param storeName The name of the store.
     * @param shelfId   The ID of the shelf to modify.
     * @param shelfSize new size of the shelf.
     */
    public void modifyShelfSize(String storeName, int shelfId, int shelfSize) {
        if (!exists(storeName)) {
            throw new ExceptionStoreNotExists(storeName);
        }
        stores.get(storeName).modifyShelfSize(shelfId,shelfSize);
    }

    /**
     * Checks if a product is already placed in any shelf within the store.
     *
     * @param productBarcode The barcode of the product to check.
     * @return true if the product is placed in any shelf; false otherwise.
     */
    public boolean productIsPlaced(String storeName, int productBarcode) {
        if (!exists(storeName)) {
            throw new ExceptionStoreNotExists(storeName);
        }
        return stores.get(storeName).productIsPlaced(productBarcode);
    }

    /**
     * Checks if a product is already placed in any store.
     *
     * @param barcode The barcode of the product to check.
     * @return true if the product is placed in any store; false otherwise.
     */
    public boolean productIsPlacedInAny(int barcode) {
        for(Store store : stores.values()) {
            if(store.productIsPlaced(barcode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a boolean indicating if the shelf contains any products.
     * @param storeName Name of the store that offers products.
     * @param shelfId ID of the shelf that will get the products stored in
     * @return True if the shelf contains any products, false otherwise.
     */
    public boolean containsProducts(String storeName, int shelfId) {
        if (!exists(storeName)) {
            throw new ExceptionStoreNotExists(storeName);
        }
        return stores.get(storeName).containsProducts(shelfId);
    }
}
