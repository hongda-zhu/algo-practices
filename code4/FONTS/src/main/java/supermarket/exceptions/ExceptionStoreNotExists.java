package supermarket.exceptions;

public class ExceptionStoreNotExists extends RuntimeException {
    public ExceptionStoreNotExists(String storeName) {
        super("The store " + storeName + " does not exist");
    }
}
