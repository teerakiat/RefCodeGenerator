package th.co.itmx.util;

public class InvalidFormatException extends Exception {
    public InvalidFormatException(String invalidKeyErrorMessage) {
        super(invalidKeyErrorMessage);
    }
}
