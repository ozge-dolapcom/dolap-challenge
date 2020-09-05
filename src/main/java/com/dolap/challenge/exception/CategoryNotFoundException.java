package com.dolap.challenge.exception;

public class CategoryNotFoundException extends RuntimeException{

    public static final String CATEGORY_NOT_FOUND_EXCEPTION_MESSAGE_KEY = "com.dolap.challenge.exception.CategoryNotFoundException.message";

    /**
     * Constructs a {@link RuntimeException} with the provided message
     *
     * @param message is the message that's set as a cause and later could be retrieved by
     *                the method {@link CategoryNotFoundException#getMessage()}
     */
    public CategoryNotFoundException(String message){
        super(message);
    }
}
