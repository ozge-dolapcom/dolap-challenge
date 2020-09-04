package com.dolap.challenge.exception;

public class OutOfStockException extends RuntimeException{

    public static final String OUT_OF_STOCK_EXCEPTION_MESSAGE_KEY = "com.dolap.challenge.exception.OutOfStockException.message";

    /**
     * Constructs a {@link RuntimeException} with the provided message
     *
     * @param message is the message that's set as a cause and later could be retrieved by
     *                the method {@link OutOfStockException#getMessage()}
     */
    public OutOfStockException(String message) {
        super(message);
    }
}
