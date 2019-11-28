package cpen221.mp3.cache;

public class NotPresentException extends Exception {
    public NotPresentException(String errorMessage) {
        super(errorMessage);
    }
}
