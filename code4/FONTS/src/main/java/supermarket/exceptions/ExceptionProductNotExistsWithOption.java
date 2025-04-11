package supermarket.exceptions;

public class ExceptionProductNotExistsWithOption extends RuntimeException {
    final int barcode;
    public ExceptionProductNotExistsWithOption(Integer barcode) {
        super("Product with barcode " + barcode + " does not exist.");
        this.barcode = barcode;
    }

    public int getBarcode() {
        return barcode;
    }
}
