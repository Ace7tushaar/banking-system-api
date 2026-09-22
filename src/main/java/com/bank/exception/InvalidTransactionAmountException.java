package com.bank.exception;

public class InvalidTransactionAmountException extends Exception {
    public InvalidTransactionAmountException(String message) {
        super(message);
    }
}