package com.ceihtel.chivalry.exceptions;

public class SoldierNotFoundException extends RuntimeException {
    public SoldierNotFoundException(String message) {
        super(message);
    }
}
