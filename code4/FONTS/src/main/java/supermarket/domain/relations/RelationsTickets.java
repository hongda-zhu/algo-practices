package supermarket.domain.relations;

import supermarket.domain.controllers.ProductController;
import supermarket.tuples.ProductInfo;

import java.util.*;

/**
 * Class representing the relations between products using tickets.
 * @author Dídac Dalmases Valcárcel
 */
public class RelationsTickets extends Relations {
    private final List<HashSet<Integer>> ticketList;
    /**
     * Initializes the relationsTickets class, giving the filePaths and the domainController.
     * @param filePaths FilePaths of the tickets to be read.
     * @param productController Instance of ProductController.
     */
    public RelationsTickets(String[] filePaths, ProductController productController) {
        super(productController);
        ticketList = new ArrayList<>();
        TicketParser parser = new TicketParser(this);
        //call TicketParser for each filePath, and get ArrayList<int> in return for each one
        if (filePaths != null) {
            for (String filePath : filePaths) {
                HashSet<Integer> ticket = parser.readTicket(filePath);
                if (ticket != null){
                    ticketList.add(ticket);
                }
            }
        }
        calculateRelations();
    }

    /**
     * Calculates relations using all tickets.
     */
    private void calculateRelations() {
        for(HashSet<Integer> ticket : ticketList) {
            processTicket(ticket);
        }
    }

    /**
     * Updates relations between products of the same ticket.
     * @param ticket TreeSet of products of the ticket.
     */
    private void processTicket(HashSet<Integer> ticket) {
        ArrayList<Integer> productList = new ArrayList<>(ticket);
        int n = productList.size();
        for (int i = 0; i < n; i++) {
            int prod1 = productList.get(i);
            for (int j = i + 1; j < n; j++) {
                int prod2 = productList.get(j);
                addToRelation(prod1, prod2);
                addToRelation(prod2, prod1); //to keep symmetry
            }
        }
    }

    /**
     * Updates relation between two products, calculating a newValue (previous (or 0) + 1).
     * @param p1 Barcode of product1.
     * @param p2 Barcode of product2.
     */
    private void addToRelation(int p1, int p2) {
        relations.putIfAbsent(p1, new LinkedHashMap<>());
        //we get the map of the product, and put a value on it's p2 relation slot
        float newValue = relations.get(p1).getOrDefault(p2,0.f) + 1.f;
        modify(p1,p2,newValue);
    }

    /**
     * Returns info of the product with the specified barcode, or null if it is not defined.
     * @param barcode   Barcode of the product.
     * @return          ProductInfo of the defined product, or null if it does not exist.
     */
    public ProductInfo getProductInfo(int barcode) {
        if (!productController.existsProduct(barcode)) return null;
        return productController.getProductInfo(barcode);
    }
}
