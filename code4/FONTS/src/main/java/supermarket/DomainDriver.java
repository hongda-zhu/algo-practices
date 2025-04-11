package supermarket;

import supermarket.domain.algorithm.AlgorithmFactory;
import supermarket.domain.controllers.DomainController;
import supermarket.exceptions.ExceptionIncorrectNumOfAttributes;
import supermarket.exceptions.ExceptionProductNotExistsWithOption;
import supermarket.exceptions.ExceptionStoreNotExists;
import supermarket.tuples.AlgorithmParameter;
import supermarket.tuples.AlgorithmType;
import supermarket.tuples.ProductInfo;

import java.io.*;
import java.util.*;

/**
 * This class is the main driver of the application. It is responsible for handling the user input and
 * executing the commands that the user inputs. It uses the DomainController to interact with the domain
 * layer of the application.
 * @author Rubén Palà Vacas
 */
public class DomainDriver {

    static DomainController domainController = new DomainController();
    static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    static HashMap<String,String> menus = new HashMap<>();
    static String command;
    static boolean readingFile;
    public static String readLine() throws IOException {
        String line = reader.readLine();
        if (readingFile) {
            if (line == null) {
                readingFile = false;
                System.out.println("Finished reading from file");
                reader = new BufferedReader(new InputStreamReader(System.in));
                line = reader.readLine();
            }
            else {
                System.out.println(">" + line);
            }
        }
        return line;
    }

    public static void main(String[] args) throws IOException {
        initializeMenus();
        boolean successful;
        printMenu("welcome");
        readingFile = false;
        while ((command = readLine()) != null) {
            boolean menu = false;
            try{
                String input;
                String[] parts;
                successful = true;
                command = command.toLowerCase();
                command = command.stripTrailing();
                switch (command) {
                    /* MENUS */
                    case "-1":
                        printMenu("main");
                        menu = true;
                        break;

                    case "0":
                        printMenu("product");
                        menu = true;
                        break;

                    case "1":
                        printMenu("store");
                        menu = true;
                        break;

                    case "2":
                        printMenu("relations");
                        menu = true;
                        break;

                    case "help":
                        printMenu("help");
                        menu = true;
                        break;

                    case "exit":
                        System.exit(0);
                        break;

                    case "execute file":
                        System.out.println("Insert the file path that will be loaded to process commands (current directory: " + System.getProperty("user.dir") + "): ");
                        String filePath = readLine();
                        reader = new BufferedReader(new FileReader(filePath));
                        readingFile = true;
                        break;

                    case "add product":
                        addProduct();
                        break;

                    case "delete product":
                        System.out.println("Insert the [barcode] of the product: ");
                        input = readLine();
                        int barcode = Integer.parseInt(input.strip());
                        if(domainController.productIsPlaced(barcode)){
                            System.out.println("Product is placed in one or many stores. Would you like still delete the product? (Y/N)");
                            String answer = readLine();
                            if (!isAnswerYes(answer)){
                                break;
                            }
                        }
                        domainController.deleteProduct(barcode);
                        break;

                    case "show product":
                        System.out.println("Insert the [barcode] of the product: ");
                        input = readLine();
                        barcode = Integer.parseInt(input.strip());
                        showProduct(barcode);
                        break;

                    case "showall products":
                        Set<Integer> barcodes = domainController.getDefinedProductsBarcodes();
                        if(barcodes.isEmpty()){
                            throw new RuntimeException("No products to be shown.");
                        }
                        for( Integer code : barcodes ) {
                            showProduct(code);
                        }
                        break;

                    case "mod product price":
                        System.out.println("Insert the [barcode] and the new [price] separated with a space: ");
                        input = readLine();
                        parts = input.split(" ");

                        if (parts.length != 2) {
                            throw new ExceptionIncorrectNumOfAttributes(2);
                        }

                        barcode = Integer.parseInt(parts[0]);
                        float price = Float.parseFloat(parts[1]);
                        domainController.modifyPriceProduct(barcode, price);
                        break;

                    case "mod product shelftype":
                        System.out.println("Insert the [barcode] of the product and its new [shelfType] separated with a space: ");
                        input = readLine();

                        parts = input.split(" ");

                        if (parts.length != 2) {
                            throw new ExceptionIncorrectNumOfAttributes(2);
                        }

                        barcode = Integer.parseInt(parts[0]);
                        String shelfType = parts[1];
                        domainController.modifyShelfTypeProduct(barcode, shelfType);
                        break;

                    case "add store":
                        addStore();
                        break;

                    case "delete store":
                        System.out.print("Insert the [name] of the store: ");
                        String storeName = readLine();
                        domainController.deleteStore(storeName.strip());
                        break;

                    case "show store":
                        System.out.print("Insert the [name] of the store: ");
                        storeName = readLine();
                        showStore(storeName.strip());
                        break;

                    case "showall stores":
                        Set<String> storeNames = domainController.getStoreNames();
                        if (storeNames.isEmpty()) System.err.println("No stores available.");
                        else {
                            System.out.println("The following stores are available:");
                            for (String store : storeNames) {
                                showStore(store);
                                System.out.println("------------------------------------------------------------");
                            }
                        }
                        break;

                    case "offer product":
                        System.out.println("""
                                Insert the [name] of the store and the [barcode] of the product to be offered\s
                                separated with a space:""");
                        input = readLine();
                        parts = input.split(" ");

                        if (parts.length != 2) {
                            throw new ExceptionIncorrectNumOfAttributes(2);
                        }

                        storeName = parts[0];
                        barcode = Integer.parseInt(parts[1]);
                        if (!domainController.existsProduct(barcode)) {
                            throw new ExceptionProductNotExistsWithOption(barcode);
                        }
                        domainController.addOfferedProduct(storeName, barcode);
                        break;

                    case "discard product":
                        System.out.println("""
                                Insert the [name] of the store and the [barcode] of the product to be discarded\s
                                separated with a space:""");
                        input = readLine();
                        parts = input.split(" ");

                        if (parts.length != 2) {
                            throw new ExceptionIncorrectNumOfAttributes(2);
                        }

                        storeName = parts[0];
                        barcode = Integer.parseInt(parts[1]);
                        if (!domainController.existsProduct(barcode)) {
                            System.err.println("Product does not exist");
                            return;
                        }
                        domainController.discardOfferedProduct(storeName, barcode);
                        break;

                    case "mod store name":
                        System.out.println("Insert the [name] of the store and its [newName] separated with a space:");
                        input = readLine();
                        parts = input.split(" ");

                        if (parts.length != 2) {
                            throw new ExceptionIncorrectNumOfAttributes(2);
                        }
                        String oldStoreName = parts[0];
                        String newStoreName = parts[1];

                        domainController.modifyStoreName(oldStoreName, newStoreName);
                        break;

                    case "mod shelf size":
                        System.out.println("Insert the [name] of the store, the [id] of the shelf and the [newSize] separated with a space:");
                        input = readLine();
                        parts = input.split(" ");

                        if (parts.length != 3) {
                            throw new ExceptionIncorrectNumOfAttributes(3);
                        }
                        storeName = parts[0];
                        int shelfId = Integer.parseInt(parts[1]);
                        int shelfSize = Integer.parseInt(parts[2]);
                        domainController.modifyStoreShelfSize(storeName,shelfId,shelfSize);
                        break;

                    case "show offered products":
                        System.out.print("Insert the [name] of the store: ");
                        storeName = readLine();
                        barcodes = domainController.getOfferedProductsBarcodes(storeName);
                        System.out.println("The following barcodes are offered on the store:");
                        for (Integer code : barcodes) {
                            System.out.println("    -" + code);
                        }
                        break;

                    case "add shelf":
                        System.out.print("Insert the [name] of the store: ");
                        storeName = readLine();
                        System.out.println("Now, insert the [id] [type] [size] of the shelf in that order and separated by spaces:");
                        input = readLine();

                        parts = input.split(" ");

                        if (parts.length != 3) {
                            throw new ExceptionIncorrectNumOfAttributes(3);
                        }

                        shelfId = Integer.parseInt(parts[0]);
                        shelfType = parts[1];
                        int size = Integer.parseInt(parts[2]);

                        domainController.addShelf(storeName, shelfId, shelfType, size);
                        break;

                    case "delete shelf":
                        System.out.println("Insert the [name] of the store and the [id] of the shelf, separated by spaces: ");
                        input = readLine();

                        parts = input.split(" ");

                        if (parts.length != 2) {
                            throw new ExceptionIncorrectNumOfAttributes(2);
                        }

                        storeName = parts[0];
                        shelfId = Integer.parseInt(parts[1]);
                        if(domainController.containsProducts(storeName,shelfId)){
                            System.out.println("Shelf has products placed. Would you like still delete the shelf? (Y/N)");
                            String answer = readLine();
                            if (!isAnswerYes(answer)){
                                break;
                            }
                        }
                        domainController.removeShelf(storeName, shelfId);
                        break;

                    case "place product":
                        System.out.println("Insert the [name] of the store, [id] of the shelf, [barcode] and [position] of product, all separated by spaces: ");
                        input = readLine();

                        parts = input.split(" ");

                        if (parts.length != 4) {
                            throw new ExceptionIncorrectNumOfAttributes(4);
                        }

                        storeName = parts[0];
                        shelfId = Integer.parseInt(parts[1]);
                        barcode = Integer.parseInt(parts[2]);
                        int position = Integer.parseInt(parts[3]);
                        domainController.placeProduct(storeName, shelfId, barcode, position);
                        break;

                    case "withdraw product":
                        System.out.print("Insert the [name] of the store, [id] of the shelf, [barcode] of product, all separated by spaces: ");
                        input = readLine();

                        parts = input.split(" ");

                        if (parts.length != 3) {
                            throw new ExceptionIncorrectNumOfAttributes(3);
                        }

                        storeName = parts[0];
                        shelfId = Integer.parseInt(parts[1]);
                        barcode = Integer.parseInt(parts[2]);
                        domainController.withdrawProduct(storeName, shelfId, barcode);
                        break;

                    case "show placed products shelf":
                        System.out.println("Insert the [name] of the store and the [id] of the shelf, all separated by spaces: ");
                        input = readLine();

                        parts = input.split(" ");

                        if (parts.length != 2) {
                            throw new ExceptionIncorrectNumOfAttributes(2);
                        }

                        storeName = parts[0];
                        shelfId = Integer.parseInt(parts[1]);
                        List<Integer> placedProducts = domainController.getStoredProducts(storeName, shelfId);
                        if (placedProducts != null) {
                            System.out.println("The following product barcodes are stored in shelf " + shelfId + " of store " + storeName + ":");
                            int placedPosition = 0;
                            for (Integer i : placedProducts) {
                                if (i != null) System.out.println(" [" + placedPosition + "] - " + i);
                                else System.out.println(" [" + placedPosition + "] - {empty slot}");
                                placedPosition++;
                            }
                        }

                        break;

                    case "swap product positions":
                        System.out.println("Insert the [name] of the store, [id] of the shelf, [position1] and [position2] of the products, all separated by spaces: ");
                        input = readLine();

                        parts = input.split(" ");

                        if (parts.length != 4) {
                            throw new ExceptionIncorrectNumOfAttributes(4);
                        }

                        storeName = parts[0];
                        shelfId = Integer.parseInt(parts[1]);
                        int position1 = Integer.parseInt(parts[2]);
                        int position2 = Integer.parseInt(parts[3]);

                        domainController.swapPositions(storeName, shelfId, position1, position2);
                        break;

                    case "read relation matrix":
                        readRelationMatrix();
                        break;

                    case "read tickets":
                        System.out.print("Do you want to read all files from a directory? (y/n): ");
                        if (isAnswerYes(readLine())) {
                            System.out.println("Insert the path to the directory containing the ticket files:");
                            File folder = new File(readLine());
                            File[] files = folder.listFiles();
                            if (files == null) {
                                throw new RuntimeException("Invalid directory given as input");
                            }
                            String[] fileNames = new String[files.length];
                            for (int i = 0; i < files.length; ++i) {
                                fileNames[i] = files[i].getAbsolutePath();
                            }

                            System.out.println("Detected files:");
                            for (String f : fileNames) System.out.println(f);
                            domainController.readRelationTickets(fileNames);
                        }
                        else {
                            System.out.print("How many tickets do you want to read? ");
                            int nTickets = Integer.parseInt(readLine());
                            String[] paths = new String[nTickets];
                            System.out.println("Insert each path followed by an enter:");
                            int i = 0;
                            while(i < nTickets){
                                input = readLine();
                                paths[i] = input;
                                ++i;
                            }
                            domainController.readRelationTickets(paths);
                        }
                        break;

                    case "show relation matrix":
                        float[][] relations = domainController.getProductRelations();
                        if (relations == null || relations.length == 0) {
                            throw new RuntimeException("No relations found.");
                        } else {
                            System.out.println("The following relation matrix is saved: ");
                            for (float[] relation : relations) {
                                for (float v : relation) {
                                    System.out.print(" " + v);
                                }
                                System.out.println();
                            }
                        }
                        break;

                    case "modify relation":
                        System.out.print("Insert the [barcode1] and [barcode2] of the products and the [newValue] of the relation, all separated by spaces: ");
                        input = readLine();

                        parts = input.split(" ");

                        if (parts.length != 3) {
                            throw new ExceptionIncorrectNumOfAttributes(3);
                        }

                        int barcode1 = Integer.parseInt(parts[0]);
                        int barcode2 = Integer.parseInt(parts[1]);
                        float newValue = Float.parseFloat(parts[2]);

                        domainController.modifyRelations(barcode1, barcode2, newValue);
                        System.out.println("New relation saved successfully.");
                        break;

                    case "calculate distribution":
                        calculateDistribution();
                        break;

                    default:
                        System.out.println("Command not found. Type 'help' to pop up the help menu.");
                        successful = false;
                        break;
                }
                if(!menu && successful) System.out.println("Finalized execution of command '" + command + "'. Returning to main menu...");
            } catch (NumberFormatException e) {
                System.err.println("Error! Incorrect format. Please introduce the expected format.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }  catch (ExceptionProductNotExistsWithOption e){
                System.err.println("Error! " + e.getMessage() + " Would you like to add the product? (Y/N)");
                String input = reader.readLine();
                if(input.equalsIgnoreCase("Y") || input.equalsIgnoreCase("yes")){
                    addProduct(e.getBarcode());
                    System.out.println("Finalized execution of command 'add product'. Returning to main menu...");
                }
            } catch (RuntimeException e) {
                System.err.println("Error! " + e.getMessage());
            }
            System.err.flush();
            if(!menu) printMenu("main");
        }
    }

    public static boolean isAnswerYes(String answer) {
        return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
    }

    public static void initializeMenus(){
        menus.put("welcome", """
                                    +------------------------------------------------------------------------------+
                                    | WELCOME MENU | Type exit to end execution.                                   |
                                    +------------------------------------------------------------------------------+
                                    |               WELCOME TO THE SUPERMARKET DISTRIBUTION MANAGER!               |
                                    |                                                                              |
                                    |        To see available commands, select one of the following menus          |
                                    |                       inserting a number and then enter.                     |
                                    |          Inside each menu, you can see the possible commands shown.          |
                                    |               Commands can still be executed from any window.                |
                                    |                                                                              |
                                    |                             [ 0 ] Products                                   |
                                    |                             [ 1 ] Stores                                     |
                                    |                             [ 2 ] Relations                                  |
                                    |                                                                              |
                                    |                  To execute a test file, type 'execute file'.                |
                                    +------------------------------------------------------------------------------+
                                    """);
        menus.put("main", """
                                    +------------------------------------------------------------------------------+
                                    | MAIN MENU | Type exit to end execution.                                      |
                                    +------------------------------------------------------------------------------+
                                    |                 Select one of the menus or execute a command.                |
                                    |                                                                              |
                                    |                             [ 0 ] Products                                   |
                                    |                             [ 1 ] Stores                                     |
                                    |                             [ 2 ] Relations                                  |
                                    |                                                                              |
                                    |                  To execute a test file, type 'execute file'.                |
                                    +------------------------------------------------------------------------------+
                                    """);
        menus.put("product","""
                                    +------------------------------------------------------------------------------+
                                    | PRODUCT MENU | Type -1 to return to main menu. Type exit to end execution.   |
                                    +------------------------------------------------------------------------------+
                                    |                       Execute any of the shown commands.                     |
                                    |                                                                              |
                                    |   add product                - Adds a products to the system.                |
                                    |   delete product             - Deletes a product from the system.            |
                                    |   mod product price          - Modifies the price of a product.              |
                                    |   mod product shelftype      - Modifies the shelfType of a product.          |
                                    |   show product               - Shows the attributes of a given product.      |
                                    |   showall products           - Shows the attributes of all products.         |
                                    |                                                                              |
                                    +------------------------------------------------------------------------------+
                                    """);
        menus.put("store", """
                                    +------------------------------------------------------------------------------+
                                    | STORE MENU  | Type -1 to return to main menu. Type exit to end execution.    |
                                    +------------------------------------------------------------------------------+
                                    |                       Execute any of the shown commands.                     |
                                    |                                                                              |
                                    |   add shelf                  - Adds a shelf to a store.                      |
                                    |   add store                  - Adds a store to the system.                   |
                                    |   calculate distribution     - Calculates distribution with an algorithm.    |
                                    |   delete shelf               - Deletes a shelf from a store.                 |
                                    |   delete store               - Deletes a store from the system.              |
                                    |   discard product            - Discards an offered product from a store.     |
                                    |   mod store name             - Modifies the name of a store.                 |
                                    |   offer product              - Offers a products inside a store.             |
                                    |   place product              - Places a product inside a shelf of a store.   |
                                    |   show offered products      - Shows the offered products of a store.        |
                                    |   show store                 - Shows the attributes of a given store.        |
                                    |   showall stores             - Shows the attributes of all stores.           |
                                    |   swap product positions     - Swaps two positions inside a shelf of a store.|
                                    |   withdraw product           - Withdraws a product from a shelf of a store.  |
                                    |                                                                              |
                                    +------------------------------------------------------------------------------+
                                    """);
        menus.put("relations", """
                                    +------------------------------------------------------------------------------+
                                    | RELATIONS MENU | Type -1 to return to main menu. Type exit to end execution. |
                                    +------------------------------------------------------------------------------+
                                    |                       Execute any of the shown commands.                     |
                                    |                                                                              |
                                    |   modify relation            - Modifies the relation between two products.   |
                                    |   read relation matrix       - Reads product relation matrix (NxN).          |
                                    |   read tickets               _ Reads tickets and calculates relation matrix. |
                                    |   show relation matrix       - Shows the currently saved relation matrix.    |
                                    |                                                                              |
                                    +------------------------------------------------------------------------------+
                                    """);
        menus.put("help", """
                                    +------------------------------------------------------------------------------+
                                    | HELP MENU  | Type -1 to return to main menu. Type exit to end execution.     |
                                    +------------------------------------------------------------------------------+
                                    |  Execute any of the available commands by typing them and pressing enter.    |
                                    |               Then, follow the instructions of the command.                  |
                                    |       Menus are only informative. Check them to see available commands.      |
                                    |           You can still execute any command anywhere in the program.         |
                                    +------------------------------------------------------------------------------+
                                    """);
    }

    public static void printMenu(String menu){
        System.out.print(menus.get(menu));
    }

    public static void addStore() throws IOException {
        System.out.println("Insert the [name] of the store and the desired [number] of shelves:");
        String input = readLine();
        String[] parts = input.split(" ");

        if( parts.length != 2){
            throw new RuntimeException("Please insert 2 attributes.");
        }
        String storeName = parts[0];
        int nShelves = Integer.parseInt(parts[1]);
        if(domainController.existsStore(storeName)) {
            throw new RuntimeException("Store " + storeName + "already exists");
        }

        System.out.println( nShelves + " shelves will be created. Insert the attributes of each shelf in the following format: [size] [shelfType]");
        HashMap<Integer, Integer> shelfSizes = new HashMap<>();
        HashMap<Integer, String> shelfTypes = new HashMap<>();

        for(int i = 0; i < nShelves; i++) {
            System.out.print("     -shelf " + i + ": ");
            input = readLine();
            parts = input.split(" ");
            if( parts.length != 2){
                throw new ExceptionIncorrectNumOfAttributes(2);
            }
            shelfTypes.put(i,parts[1]);
            shelfSizes.put(i,Integer.parseInt(parts[0]));

        }

        System.out.println("Would you like to offer any products? (Y/N)");
        Set<Integer> offeredProducts = new HashSet<>();
        String answer = readLine();
        if (isAnswerYes(answer)){
            System.out.print("Now, insert [barcode] of each product. To end insertion, type -1:\n" +
                    "  -");
            int product = Integer.parseInt(readLine());
            while(product != -1){
                if(!domainController.existsProduct(product)) {
                    System.err.println("  Product does not exist.");
                }
                else if (offeredProducts.contains(product)) {
                    System.err.println("  Product already offered.");
                }
                else{
                    offeredProducts.add(product);
                    System.out.println("  Successfully offered product [" + product +"]");
                }
                System.out.print("  -");
                product = Integer.parseInt(readLine());
            }
        }

        domainController.createStore(storeName,offeredProducts,shelfSizes,shelfTypes);
    }

    public static void addProduct() throws IOException {
        System.out.println("""
                                        Insert the attributes of the product in the following order, separated with a space: \s
                                        [barcode] [name] [price] [shelfType]""");
        String input = readLine();

        String[] parts = input.split(" ");

        if( parts.length != 4){
            throw new ExceptionIncorrectNumOfAttributes(4);
        }

        int barcode = Integer.parseInt(parts[0]);
        String name = parts[1];
        float price = Float.parseFloat(parts[2]);
        String shelfType = parts[3];

        domainController.defineProduct(barcode,name,price,shelfType);

        if(domainController.getDefinedProductsBarcodes().size()>1){
            defineRelations(barcode);
        }
    }

    public static void addProduct(int barcode) throws IOException {
        System.out.print("Insert the attributes of the product with barcode [" + barcode + "] in the following order, separated with a space:\n[name] [price] [shelfType]\n" + barcode + " ");
        String input = readLine();

        String[] parts = input.split(" ");

        if( parts.length != 3){
            throw new ExceptionIncorrectNumOfAttributes(4);
        }

        String name = parts[0];
        float price = Float.parseFloat(parts[1]);
        String shelfType = parts[2];

        domainController.defineProduct(barcode,name,price,shelfType);

        if(domainController.getDefinedProductsBarcodes().size()>1){
            defineRelations(barcode);
        }
    }

    public static void defineRelations(int barcode) throws IOException {
        System.out.println("More than one product is defined in the system. Would you like to introduce the relations with the other products? (Y/N)");
        if(isAnswerYes(readLine())){
            System.out.println("Please introduce the value of the relation with each product, or 0 if unknown:");
            Set<Integer> definedBarcodes = domainController.getDefinedProductsBarcodes();
            definedBarcodes.remove(barcode);
            for( Integer barcodeProd : definedBarcodes){
                System.out.print(" [Product " + barcodeProd + "]: ");
                float newValue = Float.parseFloat(readLine());
                domainController.modifyRelations(barcode,barcodeProd,newValue);
                domainController.modifyRelations(barcodeProd,barcode,newValue);
            }
        }
        else{
            System.out.println("Relations with other products will be initialized as 0.");
            Set<Integer> definedBarcodes = domainController.getDefinedProductsBarcodes();
            for( Integer barcodeProd : definedBarcodes){
                domainController.modifyRelations(barcode,barcodeProd,0);
                domainController.modifyRelations(barcodeProd,barcode,0);
            }
        }
    }

    public static void showProduct(int barcode) {
        ProductInfo productInfo = domainController.getProductInfo(barcode);
        System.out.println(productInfo);
    }

    public static void showShelf(String storeName, int shelfId) {
        System.out.println("Placed products of shelf " + shelfId + ": ");
        List<Integer> placedProducts = domainController.getStoredProducts(storeName, shelfId);
        for (Integer barcode : placedProducts) {
            if (barcode == null) System.out.print("[/] ");
            else System.out.print("[" + barcode + "] ");
        }
        System.out.println();
    }

    public static void showStore(String storeName) {
        if(!domainController.existsStore(storeName)){
            throw new ExceptionStoreNotExists(storeName);
        }

        System.out.println("Store Information:");
        System.out.println("   - Store name: " + storeName);
        System.out.println("   - Offered products: ");
        Set<Integer> offeredProds = domainController.getOfferedProductsBarcodes(storeName);
        if (!offeredProds.isEmpty()) {
            for (Integer barcode : offeredProds) {
                showProduct(barcode);
            }
        }
        Set<Integer> shelfIds = domainController.getShelfIds(storeName);
        System.out.println();
        System.out.println("   - Shelves: ");
        if (!shelfIds.isEmpty()) {
            for (Integer id : shelfIds) {
                showShelf(storeName, id);
            }
        }
    }

    private static void readRelationMatrix() throws IOException {
        System.out.println("The following order of barcodes, both in column and row, will be assumed:");
        Set<Integer> definedProducts = domainController.getDefinedProductsBarcodes();
        System.out.print(" ");
        for (Integer idProd : definedProducts) System.out.print(idProd + " ");
        System.out.print("\n");
        System.out.println("Now, insert an n x n matrix representing the value of the relations. Separate each value with a space.\n " +
                "Note that the diagonal will be ignored: ");
        System.out.print(" ");
        for (int i = 0; i < definedProducts.size(); i++) {
            System.out.print("- ");
        }
        System.out.print("\n" + " ");
        float[][] newRelations = new float[definedProducts.size()][definedProducts.size()];
        String input;
        String[] parts;
        for (int i = 0; i < definedProducts.size(); i++) {
            input = readLine();
            parts = input.split(" ");
            if (parts.length != definedProducts.size()) {
                throw new RuntimeException("Please insert " + definedProducts.size() + " values.");
            }
            for (int j = 0; j < definedProducts.size(); j++) {
                newRelations[i][j] = Float.parseFloat(parts[j]);
            }
            System.out.print(" ");
        }
        domainController.readRelationMatrix(newRelations);
    }

    private static void calculateDistribution() throws IOException {
        System.out.print("Insert the [name] of the store: ");
        String storeName = readLine();
        if(!domainController.existsStore(storeName)){
            throw new ExceptionStoreNotExists(storeName);
        }
        if(domainController.getOfferedProductsBarcodes(storeName).isEmpty()){
            throw new RuntimeException("There are no offered products for the store: " + storeName + ".");
        }
        Set<AlgorithmType> availableAlgorithms;
        System.out.print("Do you want to calculate a distribution for only one shelf? ");
        boolean onlyOneShelf = isAnswerYes(readLine());
        int shelfId = -1;
        if (onlyOneShelf) {
            System.out.print("Insert the [id] of the shelf: ");
            shelfId = Integer.parseInt(readLine());
            if(!domainController.existsStore(storeName)){
                throw new ExceptionStoreNotExists(storeName);
            }
            availableAlgorithms = domainController.getAvailableAlgorithms(storeName, shelfId);
        }
        else {
            Integer[] shelves = domainController.getShelfIds(storeName).toArray(new Integer[]{});
            if (shelves.length == 0) {
                throw new RuntimeException("Can't calculate a distribution on a store with no shelves");
            }
            //set intersection of each set of available algorithms for each shelf (resulting ones will be compatible with all of them)
            availableAlgorithms = domainController.getAvailableAlgorithms(storeName, shelves[0]);
            for (int i = 1; i < shelves.length; ++i) availableAlgorithms.retainAll(domainController.getAvailableAlgorithms(storeName, shelves[i]));
        }
        if (availableAlgorithms.isEmpty()) {
            throw new RuntimeException("There are no available algorithms to calculate the distribution for the chosen store (+shelf).");
        }

        System.out.println("Please select one of the following algorithms (type the exact string): " + availableAlgorithms);
        AlgorithmType algorithmType = AlgorithmType.valueOf(readLine());
        if (availableAlgorithms.contains(algorithmType)) {
            List<AlgorithmParameter> availableParameters = AlgorithmFactory.createAlgorithm(algorithmType, new float[][]{}).getAvailableParameters();
            List<String> chosenParameters = new ArrayList<>();
            if (!availableParameters.isEmpty()) {
                System.out.println("For each available parameter, specify their desired value (if invalid input, default value will be chosen for each of them):");
                for (AlgorithmParameter availableParameter : availableParameters) {
                    System.out.println("Name: " + availableParameter.paramName());
                    System.out.println("Description: " + availableParameter.paramDescription());
                    System.out.println("Type of value: " + availableParameter.paramType());
                    System.out.print("Chosen value: ");
                    String paramValue = readLine();
                    chosenParameters.add(paramValue);
                }
            }
            if (onlyOneShelf) {
                domainController.calculateDistributionShelf(storeName, shelfId, algorithmType, chosenParameters);
                System.out.println("Finished calculating the distribution.");
                showShelf(storeName, shelfId);
            } else {
                domainController.calculateDistribution(storeName, algorithmType, chosenParameters);
                System.out.println("Finished calculating the distribution.");
                for (int id : domainController.getShelfIds(storeName))
                    showShelf(storeName, id);
            }
        }
        else throw new RuntimeException("This algorithm is not available with this distribution");
    }
}
