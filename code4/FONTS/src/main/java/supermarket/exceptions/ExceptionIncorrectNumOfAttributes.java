package supermarket.exceptions;

public class ExceptionIncorrectNumOfAttributes extends RuntimeException {
    public ExceptionIncorrectNumOfAttributes(int numOfAttributes) {
        super("Please insert " + numOfAttributes +" attributes.");
    }
}
