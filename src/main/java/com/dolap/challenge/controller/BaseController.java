package com.dolap.challenge.controller;

import com.dolap.challenge.exception.ApiExceptionResponse;
import com.dolap.challenge.exception.OutOfStockException;
import com.dolap.challenge.exception.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

/**
 * Defines the base controller of the API where all other controllers will extend
 * It includes a simple exception handling logic to display proper JSON responses to the clients
 */
public class BaseController {

    /**
     * Method that catches all the exceptions and constructs a proper response
     * to the clients
     *
     * @param exception is the exception raised by the app
     * @return a proper response
     */
    @ExceptionHandler
    public ResponseEntity<Object> handleException(Exception exception) {
        ApiExceptionResponse apiExceptionResponse = new ApiExceptionResponse();
        apiExceptionResponse.setTimestamp(LocalDateTime.now());
        apiExceptionResponse.setStatus(statusCodeForException(exception));
        apiExceptionResponse.setMessage(messageForException(exception));

        return new ResponseEntity<>(apiExceptionResponse, apiExceptionResponse.getStatus());
    }

    /**
     * Constructs the display message for the exception
     *
     * @param exception that's raised by the app
     * @return the display message for the exception
     */
    private String messageForException(Exception exception) {
        if(exception instanceof MethodArgumentNotValidException){
            BindingResult bindingResult = ((MethodArgumentNotValidException)exception).getBindingResult();
            if(bindingResult.getErrorCount() > 0){
                return bindingResult.getAllErrors().get(0).getDefaultMessage();
            }
        }
        return exception.getMessage();
    }

    /**
     * Constructs the HttpStatus code for the response
     *
     * @param exception that's raised by the app
     * @return HttpStatus
     */
    private HttpStatus statusCodeForException(Exception exception) {
        if (exception instanceof OutOfStockException
                || exception instanceof ProductNotFoundException
                || exception instanceof MethodArgumentNotValidException) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
