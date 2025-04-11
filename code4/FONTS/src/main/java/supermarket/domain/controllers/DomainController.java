package supermarket.domain.controllers;

import supermarket.domain.algorithm.AbstractAlgorithm;
import supermarket.domain.algorithm.AlgorithmFactory;
import supermarket.domain.algorithm.GraphUtils;
import supermarket.domain.relations.Relations;
import supermarket.domain.relations.RelationsTickets;
import supermarket.exceptions.ExceptionProductNotExistsWithOption;
import supermarket.tuples.AlgorithmType;
import supermarket.tuples.ProductInfo;

import java.util.*;

/**
 * DomainController class is the main controller of the system.
 * It manages the products, stores, relations between products and the supermarket distributions.
 * @author Rubén Palà Vacas
 */
public class DomainController {
    /**
     * Current relations between the products of the system.
     */
    Relations currentRelations;
    /**
     * Controller of the existing products in the system.
     */
    final ProductController ctrlProduct;
    /**
     * Controller of the existing stores in the system.
     */
    final StoreController ctrlStore;


    /**
     * Constructor for the DomainController. Initializes empty controllers and an empty basic relations.
     */
    public DomainController() {
        ctrlProduct = new ProductController();
        ctrlStore = new StoreController();
        float[][] initRelations = new float[][]{{0.0f}};
        currentRelations = new Relations(ctrlProduct, initRelations, new ArrayList<>());
    }

    /**
     * Constructor for the DomainController. Initializes controllers with the ones given and relations with the one given.
     * @param ctrlProduct Product controller to be used.
     * @param ctrlStore Store Controller to be used.
     * @param relations Relations to be used.
     */
    public DomainController(ProductController ctrlProduct, StoreController ctrlStore, Relations relations) {
        this.ctrlProduct = ctrlProduct;
        this.ctrlStore = ctrlStore;
        this.currentRelations = relations;
    }

    /**
     * Creates a product with the given barcode, name, price and shelf type.
     *
     * @param barcode   Unique barcode associated with the product.
     * @param name      Unique name associated with the product.
     * @param price     Price of the product.
     * @param shelfType Type of shelf that the product can be stored in.
     */
    public void defineProduct(int barcode, String name, float price, String shelfType) {
        ctrlProduct.defineProduct(barcode, name, price, shelfType);
    }

    /**
     * Deletes the product with the given barcode.
     *
     * @param barcode Barcode identifying the product.
     */
    public void deleteProduct(int barcode) {
        discardProductFromAll(barcode);
        ctrlProduct.deleteProduct(barcode);
        currentRelations.delete(barcode);
    }

    /**
     * Modifies the price of the given product
     * @param barcode Barcode identifying the product.
     * @param price New price of the product.
     */
    public void modifyPriceProduct(int barcode, float price) {
        ctrlProduct.modifyPriceProduct(barcode,price);
    }

    /**
     * Modifies the shelf type of the given product.
     * @param barcode Barcode identifying the product
     * @param shelfType New shelf type of the product.
     */
    public void modifyShelfTypeProduct(int barcode, String shelfType) {
        ctrlProduct.modifyShelfTypeProduct(barcode, shelfType);
    }

    /**
     * Returns the information of the given product.
     * @param barcode Barcode identifying the product.
     */
    public ProductInfo getProductInfo(int barcode) {
        return ctrlProduct.getProductInfo(barcode);
    }

    /**
     * Checks if the given product exists.
     * @param barcode Barcode identifying the product.
     * @return Returns true if the product exists, false if else.
     */
    public boolean existsProduct(int barcode) {
        return ctrlProduct.existsProduct(barcode);
    }

    /**
     * Gets the relations matrix from the current relation class.
     *
     * @return Matrix representing the product relations.
     */
    public float[][] getProductRelations() {
        return currentRelations.getRelationMatrix(getDefinedProductsBarcodes());
    }

    /**
     * Updates the currents relations to a basic relations with the current matrix.
     * @param relationMatrix Matrix representing the relations following the order of defined barcodes.
     */
    public void readRelationMatrix(float[][] relationMatrix) {
        currentRelations = new Relations(ctrlProduct, relationMatrix ,new ArrayList<>(getDefinedProductsBarcodes()));
    }

    /**
     * Updates the currents relations to a relations tickets with the given file path
     * @param filePaths String vector containing the filePaths of the tickets.
     */
    public void readRelationTickets(String[] filePaths){
        currentRelations = new RelationsTickets(filePaths,ctrlProduct);
    }

    /**
     * Modifies the value of the relation between two products.
     *
     * @param barcode1 Barcode identifying product 1.
     * @param barcode2 Barcode identifying product 2.
     * @param newValue New value of the relation between the products.
     */
    public void modifyRelations(int barcode1, int barcode2, float newValue) {
        currentRelations.modify(barcode1, barcode2, newValue);
    }

    /**
     * Places a product in a position of a shelf inside a given store.
     * @param storeName      Name of the store.
     * @param shelfId        Identifier of the shelf.
     * @param productInfo    Info of the product to be placed.
     * @param position       Position of the product inside the shelf.
     */
    public void placeProduct(String storeName, int shelfId, ProductInfo productInfo, int position) {
        ctrlStore.placeProduct(storeName, shelfId, productInfo, position);
    }

    /**
     * Places a product in a position of a shelf inside a given store.
     *
     * @param storeName      Name of the store.
     * @param shelfId        Identifier of the shelf.
     * @param productBarcode Barcode identifying the product to be placed.
     * @param position       Position of the product inside the shelf.
     */
    public void placeProduct(String storeName, int shelfId, int productBarcode, int position) {
        if (!ctrlProduct.existsProduct(productBarcode)) {
            throw new ExceptionProductNotExistsWithOption(productBarcode);
        }
        placeProduct(storeName, shelfId, ctrlProduct.getProductInfo(productBarcode), position);
    }

    /**
     * Withdraws a product from a given shelf.
     *
     * @param productBarcode Barcode identifying the product to be withdrawn.
     * @param storeName      Name of the store.
     * @param shelfId        Identifier of the shelf.
     */
    public void withdrawProduct(String storeName, int shelfId, int productBarcode) {
        if(!ctrlStore.productIsPlaced(storeName,productBarcode)){
            throw new RuntimeException("Product is not placed in store " + storeName + ".");
        }
        ctrlStore.withdrawProduct(storeName, shelfId, productBarcode);
    }

    /**
     * Swaps the content of the two given positions inside a shelf.
     *
     * @param position1 First position to be swapped.
     * @param position2 Second position to be swapped.
     * @param shelfId   Identifier of the shelf.
     */
    public void swapPositions(String storeName, int shelfId, int position1, int position2) {
        ctrlStore.swapPositions(storeName, shelfId, position1, position2);
    }

    /**
     * Exports the current distribution of the store.
     */
    public void exportDistribution() {
        //TODO
    }

    /**
     * Imports a preset distribution for the given store.
     */
    public void importDistribution() {
        //TODO
    }

    /**
     * Creates a store with the given attributes.
     *
     * @param name            Name of the store.
     * @param offeredProducts Set containing the barcodes of the initial offered products.
     * @param shelfSizes      Sizes of the shelves with the same identifier as the first key.
     * @param shelfTypes      Types of the shelves with the same identifier as the first key.
     */
    public void createStore(String name, Set<Integer> offeredProducts, HashMap<Integer, Integer> shelfSizes, HashMap<Integer, String> shelfTypes) {
        ctrlStore.createStore(name);
        for(Integer barcode : offeredProducts) ctrlStore.addOfferedProduct(name,barcode);
        for(Integer i : shelfSizes.keySet()){
            addShelf(name,i, shelfTypes.get(i), shelfSizes.get(i));
        }
    }

    /**
     * Adds a shelf with the given attribute to the indicated store.
     * @param storeName Name of the store.
     * @param shelfId Identifier of the shelf.
     * @param shelfType Type of shelf.
     * @param size Size of the shelf.
     */
    public void addShelf( String storeName, int shelfId, String shelfType, int size ) {
        ctrlStore.addShelf(storeName,shelfId,shelfType,size);
    }

    /**
     * Removes a shelf from the given store.
     * @param storeName Name of the store.
     * @param shelfId Identifier of the shelf.
     */
    public void removeShelf( String storeName, int shelfId ) {
        ctrlStore.removeShelf(storeName,shelfId);
    }

    /**
     * Deletes the store with the given name.
     *
     * @param name Name of the store.
     */
    public void deleteStore(String name) {
        ctrlStore.deleteStore(name);
    }

    /**
     * Changes the name of the given store.
     * @param oldStoreName Name of the store to change.
     * @param newStoreName New name of the store.
     */
    public void modifyStoreName(String oldStoreName, String newStoreName) {
        ctrlStore.changeStoreName(oldStoreName, newStoreName);
    }

    /**
     * Offers the product in the given store.
     *
     * @param storeName      Name of the store.
     * @param productBarcode Barcode identifying the product to be offered.
     */
    public void addOfferedProduct(String storeName, int productBarcode) {
        ctrlStore.addOfferedProduct(storeName, productBarcode);
    }

    /**
     * Discard the product in the given store.
     *
     * @param storeName      Name of the store.
     * @param productBarcode Barcode identifying the product to be discarded.
     */
    public void discardOfferedProduct(String storeName, int productBarcode) {
        ctrlStore.discardOfferedProduct(storeName, productBarcode);
    }

    /**
     * Discard the product in all the existing stores, if present.
     * @param productBarcode Barcode identifying the product to be discarded.
     */
    public void discardProductFromAll(int productBarcode) {
        ctrlStore.discardProductFromAll(productBarcode);
    }

    /**
     * Gets the offered products of the selected store.
     * @param storeName Name of the store.
     * @return Returns a set with the barcodes of the offered products in the store.
     */
    public Set<Integer> getOfferedProductsBarcodes(String storeName) {
        return ctrlStore.getOfferedProducts(storeName);
    }

    /**
     * Gets the barcodes of the defined products in the system.
     *
     * @return Returns a set with the barcodes of the defined products in the system.
     */
    public Set<Integer> getDefinedProductsBarcodes(){
        return ctrlProduct.getDefinedBarcodes();
    }

    /**
     * Gets the names of the stores in the system
     * @return Returns a set with the names of the stores in the system.
     */
    public Set<String> getStoreNames(){
        return ctrlStore.getStoreNames();
    }

    /**
     * Gets the ids of the shelves in the given store.
     * @param storeName Name of the store.
     * @return Returns a set with the shelves ids of the given store.
     */
    public Set<Integer> getShelfIds(String storeName) {
        return ctrlStore.getShelfIds(storeName);
    }

    /**
     * Returns a boolean indicating if the given store exists.
     * @param storeName Name of the store.
     * @return Returns true if the store exists, false if else.
     */
    public boolean existsStore(String storeName) {
        return ctrlStore.exists(storeName);
    }

    /**
     * Returns all products placed in a specific shelf (what product each position contains)
     * @param storeName Name of the store that contains the shelf.
     * @param shelfId   ID of the shelf.
     * @return          Ordered list of barcodes (or null if position does not contain any product).
     */
    public List<Integer> getStoredProducts(String storeName, int shelfId) {
        return ctrlStore.getStoredProducts(storeName, shelfId);
    }

    /**
     * Returns all product barcodes that can currently be placed in a specific shelf (including those already placed),
     * which corresponds to all placeable products of the store that have the same shelf type as the shelf + the products already placed on that shelf.
     * @param storeName Name of the store that contains the shelf.
     * @param shelfId   ID of the shelf.
     * @return          Set of barcodes of all products that can currently be placed on the specified shelf.
     */
    public Set<Integer> getPlaceableProductOfShelf(String storeName, int shelfId) {
        Set<Integer> placeableProducts = ctrlStore.getPlaceableProducts(storeName);
        String shelfType = ctrlStore.getShelfType(storeName, shelfId);
        placeableProducts.removeIf(barcode -> !ctrlProduct.getProductInfo(barcode).shelfType().equals(shelfType));
        for (Integer barcode : ctrlStore.getStoredProducts(storeName, shelfId)) {
            if (barcode != null) placeableProducts.add(barcode);
        }
        return placeableProducts;
    }

    /**
     * Gets a set of all available algorithms (that can be applied to any shelf of the store) given the offered products in a certain store.
     * @param storeName Name of the store that offers products.
     * @param shelfId ID of the shelf that will get the products stored in
     * @return Returns a set of AlgorithmType that can be used to calculate a distribution.
     */
    public Set<AlgorithmType> getAvailableAlgorithms(String storeName, int shelfId) {
        float[][] relationMatrix = GraphUtils.invertMatrixValues(currentRelations.getRelationMatrix(getPlaceableProductOfShelf(storeName, shelfId)));
        List<AbstractAlgorithm> algorithms = AlgorithmFactory.getAllUsableAlgorithms(relationMatrix);
        Set<AlgorithmType> availableAlgorithms = new HashSet<>();
        for (AbstractAlgorithm algorithm : algorithms) availableAlgorithms.add(algorithm.getType());
        return availableAlgorithms;
    }

    private int[] getDistributionShelf(String storeName, int shelfId, AlgorithmType algorithmType, List<String> algorithmParameters) {
        float[][] distanceMatrix = GraphUtils.invertMatrixValues(currentRelations.getRelationMatrix(getPlaceableProductOfShelf(storeName, shelfId))); //TODO allow more ways to convert from relation matrix to distance matrix
        AbstractAlgorithm algorithm = AlgorithmFactory.createAlgorithm(algorithmType, distanceMatrix);
        algorithm.setParameters(algorithmParameters);
        return algorithm.calculateDistribution(); //TODO be able to decide what to do if amount of products is different from shelf size (default: ignore extra products)
    }

    /**
     * Calculates a distribution of products for the specified shelf using the specified algorithm with given parameters.
     * @param storeName             Name of the store that contains the shelf.
     * @param shelfId               ID of the shelf that will get filled with products.
     * @param algorithmType         Type of algorithm to use to calculate distribution with most relation possible.
     * @param algorithmParameters   Specific parameters for the chosen algorithm type.
     */
    public void calculateDistributionShelf(String storeName, int shelfId, AlgorithmType algorithmType, List<String> algorithmParameters) {
        ctrlStore.clearShelf(storeName, shelfId);
        int[] productsOfShelf = getDistributionShelf(storeName, shelfId, algorithmType, new ArrayList<>(algorithmParameters));
        ArrayList<Integer> arrayPlaceableProducts = new ArrayList<>(getPlaceableProductOfShelf(storeName, shelfId));
        ProductInfo[] productInfosOfShelf = new ProductInfo[productsOfShelf.length];
        for (int i = 0; i < productsOfShelf.length; ++i) {
            productInfosOfShelf[i] = ctrlProduct.getProductInfo(arrayPlaceableProducts.get(productsOfShelf[i]));
        }
        ctrlStore.placeProducts(storeName,shelfId,productInfosOfShelf);
    }

    /**
     * Calculates a distribution of products for all shelves of a store using the specified algorithm with given parameters.
     * @param storeName             Name of the store.
     * @param algorithmType         Type of algorithm to use to calculate distribution with most relation possible.
     * @param algorithmParameters   Specific parameters for the chosen algorithm type.
     */
    public void calculateDistribution(String storeName, AlgorithmType algorithmType, List<String> algorithmParameters) {
        Set<Integer> shelfIds = ctrlStore.getShelfIds(storeName);
        for (int shelfId : shelfIds) ctrlStore.clearShelf(storeName, shelfId); //clear all shelves before starting to calculate distribution one by one.
        for (int shelfId : shelfIds) {
            calculateDistributionShelf(storeName, shelfId, algorithmType, new ArrayList<>(algorithmParameters));
        }
    }

    /**
     * Modifies shelf size of the specified shelf. If a smaller size is selected, some products might be withdrawn from the shelf.
     * @param storeName Name of the store that contains the shelf.
     * @param shelfId   ID of the shelf that will change size.
     * @param shelfSize New size of the shelf.
     */
    public void modifyStoreShelfSize(String storeName, int shelfId, int shelfSize) {
        ctrlStore.modifyShelfSize(storeName,shelfId,shelfSize);
    }

    /**
     * Returns a boolean indicating if the product is placed in any of the stores.
     * @param barcode Barcode identifying the product.
     * @return A boolean indicating if the product is placed in any store.
     */
    public boolean productIsPlaced(int barcode) {
        return ctrlStore.productIsPlacedInAny(barcode);
    }

    /**
     * Returns a boolean indicating if the shelf contains any products.
     * @param storeName Name of the store that offers products.
     * @param shelfId ID of the shelf that will get the products stored in
     * @return True if the shelf contains any products, false otherwise.
     */
    public boolean containsProducts(String storeName, int shelfId) {
        return ctrlStore.containsProducts(storeName, shelfId);
    }
}
