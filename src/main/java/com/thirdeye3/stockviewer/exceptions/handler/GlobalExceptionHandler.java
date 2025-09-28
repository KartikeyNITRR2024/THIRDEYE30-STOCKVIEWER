package com.thirdeye3.stockviewer.exceptions.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.thirdeye3.stockviewer.dtos.Response;
import com.thirdeye3.stockviewer.exceptions.InvalidMachineException;
import com.thirdeye3.stockviewer.exceptions.MessageBrokerException;
import com.thirdeye3.stockviewer.exceptions.MessengerFetchException;
import com.thirdeye3.stockviewer.exceptions.PropertyFetchException;
import com.thirdeye3.stockviewer.exceptions.StockException;
import com.thirdeye3.stockviewer.exceptions.ThresholdFetchException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidMachineException.class)
    public ResponseEntity<Response<Void>> handleInvalidMachine(InvalidMachineException ex) {
        Response<Void> response = new Response<>(
                false,
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(ThresholdFetchException.class)
    public ResponseEntity<Response<Void>> handleThresholdFetch(ThresholdFetchException ex) {
        Response<Void> response = new Response<>(
                false,
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                ex.getMessage(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    
    @ExceptionHandler(MessengerFetchException.class)
    public ResponseEntity<Response<Void>> handleMessengerFetch(MessengerFetchException ex) {
        Response<Void> response = new Response<>(
                false,
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                ex.getMessage(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(PropertyFetchException.class)
    public ResponseEntity<Response<Void>> handlePropertyFetch(PropertyFetchException ex) {
        Response<Void> response = new Response<>(
                false,
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                ex.getMessage(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    @ExceptionHandler(StockException.class)
    public ResponseEntity<Response<Void>> handleStockFetch(StockException ex) {
        Response<Void> response = new Response<>(
                false,
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                ex.getMessage(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    
    
    
    
    @ExceptionHandler(MessageBrokerException.class)
    public ResponseEntity<Response<Void>> handleInvalidTopicException(MessageBrokerException ex) {
        Response<Void> response = new Response<>(
                false,
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    
    

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleGeneric(Exception ex) {
        Response<Void> response = new Response<>(
                false,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Unexpected error occurred: " + ex.getMessage(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
