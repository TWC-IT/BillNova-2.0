package com.thewizecompany.billnova.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, String resourceValue) {
        super(resource + " not found with value: " + resourceValue);
    }
}
