package com.ceihtel.chivalry.exceptions;

public class SoldierAlreadyExistsException extends RuntimeException {
    public SoldierAlreadyExistsException(String message) {
        super(message);
    }
}
