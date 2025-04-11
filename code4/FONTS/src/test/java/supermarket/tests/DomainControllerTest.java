package supermarket.tests;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import supermarket.domain.controllers.DomainController;
import supermarket.domain.controllers.ProductController;
import supermarket.domain.controllers.StoreController;
import supermarket.domain.relations.Relations;
import supermarket.exceptions.ExceptionProductNotExistsWithOption;
import supermarket.tuples.AlgorithmType;
import supermarket.tuples.ProductInfo;

import javax.management.relation.Relation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class DomainControllerTest {
    private DomainController domainController;
    private ProductController mockProductController;
    private StoreController mockStoreController;
    private Relations mockRelations;
    final private int existingProductId = 111;
    final private int nonexistingProductId = 999;
    final private String existingStore = "Suma";
    final private String unexistingStore = "Caprabo";

    //ONLY FUNCTIONS WITH SIGNIFICANT LOGIC HAVE BEEN TESTED

    @Before
    public void setUp() throws Exception {
        mockProductController = Mockito.mock(ProductController.class);
        mockStoreController = Mockito.mock(StoreController.class);
        mockRelations = Mockito.mock(Relations.class);
        when(mockProductController.existsProduct(nonexistingProductId)).thenReturn(false);
        when(mockProductController.existsProduct(existingProductId)).thenReturn(true);
        when(mockStoreController.exists(existingStore)).thenReturn(true);
        when(mockStoreController.exists(unexistingStore)).thenReturn(false);
        domainController = new DomainController(mockProductController, mockStoreController, mockRelations);
    }

    @Test
    public void placeUnexistingProduct() {
        assertThrows("Error! Non-existing product is placed",ExceptionProductNotExistsWithOption.class,
                () -> domainController.placeProduct(existingStore,0,nonexistingProductId,0));
    }

    @Test
    public void placeExistingProduct() {
        try{
            domainController.placeProduct(existingStore,0,existingProductId,0);
        } catch (Exception e) {
            fail("Fail! Exception thrown for existing product!");
        }
    }

    @Test
    public void withdrawNonExistingProduct() {
        assertThrows("Error! Non-existing product is withdrawn",RuntimeException.class,
                () -> domainController.withdrawProduct(existingStore,0,nonexistingProductId));
    }

    @Test
    public void getPlaceableProductsExistingStore(){
        HashSet<Integer> placeable = new HashSet<Integer>();
        placeable.add(existingProductId);
        when(mockStoreController.getPlaceableProducts(existingStore)).thenReturn(placeable);
        when(mockStoreController.getShelfType(existingStore,0)).thenReturn("Normal");
        float price = 3;
        ProductInfo productInfo = new ProductInfo(existingProductId,"dummy",price,"Normal");
        when(mockProductController.getProductInfo(existingProductId)).thenReturn(productInfo);
        when(mockStoreController.getStoredProducts(existingStore,0)).thenReturn(new ArrayList<>());

        assertEquals("Error! The returned set is not the expected.",placeable,domainController.getPlaceableProductOfShelf(existingStore,0));
    }

    @Test
    public void getAvailableAlgorithmsReturnsSet(){
        //Mocking of getPlaceableProducts
        HashSet<Integer> placeable = new HashSet<Integer>();
        placeable.add(existingProductId);
        when(mockStoreController.getPlaceableProducts(existingStore)).thenReturn(placeable);
        when(mockStoreController.getShelfType(existingStore,0)).thenReturn("Normal");
        float price = 3;
        ProductInfo productInfo = new ProductInfo(existingProductId,"dummy",price,"Normal");
        when(mockProductController.getProductInfo(existingProductId)).thenReturn(productInfo);
        when(mockStoreController.getStoredProducts(existingStore,0)).thenReturn(new ArrayList<>());
        float[][] relations = new float[][]{{0.0f}};
        when(mockRelations.getRelationMatrix(placeable)).thenReturn(relations);
        Set<AlgorithmType> availableAlgorithms = new HashSet<>();
        assertTrue("Errror! Set returned doesn't match the expected type.",
                domainController.getAvailableAlgorithms(existingStore, 0) != null);
    }
}
