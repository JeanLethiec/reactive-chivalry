package com.ceihtel.chivalry.controllers;

import com.ceihtel.chivalry.exceptions.SoldierNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ChivalryControllerAdvice {
    @ExceptionHandler(SoldierNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> onError(SoldierNotFoundException e) {
        return Map.of("error", e.getMessage());
    }
}
