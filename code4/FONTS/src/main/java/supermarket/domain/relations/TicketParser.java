package supermarket.domain.relations;

import supermarket.tuples.ProductInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;

/**
 * Class to parse tickets.
 * @author Dídac Dalmases Valcárcel
 */
public class TicketParser {
    /**
     TICKET EXAMPLE
     You have to follow the next syntax:    barcode productName
                                            barcode productName
                                            barcode productName
                                            barcode productName
                                                  ...

     Basic example:                         1 p1
                                            2 p2
                                            3 p3
                                            4 p4

        For each common apparition between to products with the same barcode in two different tickets, their relation value increases.
     */


    final private RelationsTickets relationsTickets;

    /**
     * Initializes ticketParser class.
     * @param relationsTickets Instance of relationsTickets to use.
     */
    public TicketParser(RelationsTickets relationsTickets) {
        this.relationsTickets = relationsTickets;
    }

    /**
     * Reads products from a text file.
     * @param filePath Path of the text file to be read.
     * @return Returns all the products that appear on the text file if it is correctly formatted.
     */
    public HashSet<Integer> readTicket(String filePath) {
        HashSet<Integer> barcodes = new HashSet<>();
        int barcode;
        try (BufferedReader reader = new BufferedReader(new FileReader(Paths.get(filePath).toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] information = line.split(" ");
                try {
                    barcode = (Integer.parseInt(information[0]));
                    ProductInfo productInfo = relationsTickets.getProductInfo(barcode);
                    if (productInfo == null) {
                        throw new RuntimeException("Ticket product not defined: " + barcode);
                    }
                    if (!productInfo.name().equals(information[1])) {
                        throw new RuntimeException("Ticket product with barcode " + barcode + " doesn't have name " + information[1]);
                    }
                    barcodes.add(barcode);
                }
                catch (NumberFormatException e) {
                    System.err.println("Invalid format of barcode: " + information[0]);
                    return null;
                }
            }
            return barcodes;
        }
        catch (IOException e) {
            System.err.println("Error reading file: " + filePath + ". " + e);
            return null;
        }
    }

}
