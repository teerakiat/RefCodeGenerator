package th.co.itmx.otp.exception;

public class InvalidFormatException extends Exception {
    public InvalidFormatException(String invalidKeyErrorMessage) {
        super(invalidKeyErrorMessage);
    }
}
