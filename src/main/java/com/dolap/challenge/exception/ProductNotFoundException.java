package com.dolap.challenge.exception;

public class ProductNotFoundException extends RuntimeException{

    public static final String PRODUCT_NOT_FOUND_EXCEPTION_MESSAGE_KEY = "com.dolap.challenge.exception.ProductNotFoundException.message";

    /**
     * Constructs a {@link RuntimeException} with the provided message
     *
     * @param message is the message that's set as a cause and later could be retrieved by
     *                the method {@link ProductNotFoundException#getMessage()}
     */
    public ProductNotFoundException(String message){
        super(message);
    }
}
