package supermarket.exceptions;

public class ExceptionShelfNotExists extends RuntimeException {
    public ExceptionShelfNotExists(int shelfId, String storeName) {
        super("The shelf " + shelfId + " does not exist in the store " + storeName + ".");
    }
}
