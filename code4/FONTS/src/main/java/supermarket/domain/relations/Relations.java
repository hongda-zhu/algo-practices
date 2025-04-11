package supermarket.domain.relations;

import supermarket.domain.controllers.ProductController;

import java.util.*;

/**
 * Class representing the relations between products.
 * @author Rubén Palà Vacas
 */
public class Relations {

    final ProductController productController;

    /**
     * Map representing the relations between products. Key integer corresponds to the product id.
     * Second map corresponds to the products it is related, and the value of this relation.
     */
    final LinkedHashMap<Integer, LinkedHashMap<Integer,Float>> relations;

    /**
     * Constructor of Relations class to use for the tickets version.
     * @param productController Product controller of the relation.
     */
    protected Relations(ProductController productController) { //only used for relationsTickets
        this.productController = productController;
        this.relations = new LinkedHashMap<>();
    }

    /**
     * Constructor of Relations class to use for the manual version.
     * @param productController Product controller of the relation.
     * @param relationMatrix Matrix to read.
     * @param barcodes Barcodes that the user has introduced with a certain order.
     */
    public Relations(ProductController productController, float[][] relationMatrix, ArrayList<Integer> barcodes) {
        this.productController = productController;
        relations = new LinkedHashMap<>();
        readMatrix(relationMatrix,barcodes);
    }

    /**
     * Fills relationMatrix with values from the relation map.
     * @param barcodes Order to follow in the matrix (barcodes[0] goes to matrix[0][...].
     * @return Returns matrix with the correct values.
     */
    public float[][] getRelationMatrix(ArrayList<Integer> barcodes){
        float[][] matrix = new float[barcodes.size()][barcodes.size()];
        int xBarcode;
        for (int i = 0; i < barcodes.size(); i++){
            xBarcode = barcodes.get(i);
            for (int j = 0; j < barcodes.size(); j++){
                //[i] -> relations of the first barcode
                //[j] -> the barcode which is related to the first barcode
                if (i != j) {
                    matrix[i][j] = relations.get(xBarcode).getOrDefault(barcodes.get(j), 0.0f);
                }
                else matrix[i][j] = 0.0f;
            }
        }
        return matrix;
    }

    /**
     * Gets relationMatrix.
     * @param barcodes Order to follow from the user.
     * @return Returns the correct matrix with values.
     */
    public float[][] getRelationMatrix(Set<Integer> barcodes){
        return getRelationMatrix(new ArrayList<>(barcodes));
    }

    /**
     * Reads matrix and puts values into relations map.
     * @param relationMatrix Matrix with the relations values.
     * @param barcodes Barcodes of the products with relations.
     */
    public void readMatrix(float[][] relationMatrix, ArrayList<Integer> barcodes) {
        relations.clear();
        for (int i = 0; i < barcodes.size(); i++) {
            LinkedHashMap<Integer,Float> innerRelations = new LinkedHashMap<>();
            for (int j = 0; j < barcodes.size(); j++) {
                if (i != j) innerRelations.put(barcodes.get(j),relationMatrix[i][j]);
            }
            relations.put(barcodes.get(i),innerRelations);
        }
    }

    /**
     * Modifies relation between two products
     * @param barcode1 First product.
     * @param barcode2 Second product.
     * @param newValue New value into the relation.
     */
    public void modify(int barcode1, int barcode2, float newValue) {
        relations.computeIfAbsent(barcode1, _ -> new LinkedHashMap<>());
        relations.get(barcode1).put(barcode2,newValue);
        relations.computeIfAbsent(barcode2, _ -> new LinkedHashMap<>());
        relations.get(barcode2).put(barcode1,newValue);
    }

    /**
     * Deletes relations with a given barcode.
     * @param barcode Barcode of the product.
     */
    public void delete(int barcode){
        relations.remove(barcode);
        for ( Map.Entry<Integer, LinkedHashMap<Integer, Float>> entry : relations.entrySet()) {
            LinkedHashMap<Integer, Float> innerRelations = entry.getValue();
            innerRelations.remove(barcode);
        }
    }
}
