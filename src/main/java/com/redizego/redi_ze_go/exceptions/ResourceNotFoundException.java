package com.redizego.redi_ze_go.exceptions;

public class ResourceNotFoundException extends  RuntimeException {
    public ResourceNotFoundException() {
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
