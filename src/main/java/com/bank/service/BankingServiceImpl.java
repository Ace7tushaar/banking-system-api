package com.bank.service;

import com.bank.dao.AccountDAO;
import com.bank.exception.AccountNotFoundException;
import com.bank.exception.InsufficientBalanceException;
import com.bank.exception.InvalidTransactionAmountException;
import com.bank.model.Account;
import java.sql.SQLException;

public class BankingServiceImpl {
    private AccountDAO accountDAO = new AccountDAO();

    public void deposit(String accountNumber, double amount) 
            throws SQLException, AccountNotFoundException, InvalidTransactionAmountException {
        
        if (amount <= 0) {
            throw new InvalidTransactionAmountException("Deposit amount must be greater than zero.");
        }

        Account account = accountDAO.getAccount(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException("Account " + accountNumber + " does not exist.");
        }

        double newBalance = account.getBalance() + amount;
        accountDAO.updateBalance(accountNumber, newBalance);
        accountDAO.logTransaction(accountNumber, "DEPOSIT", amount);
        System.out.println("Success! Deposited $" + amount + ". New Balance: $" + newBalance);
    }

    public void withdraw(String accountNumber, double amount) 
            throws SQLException, AccountNotFoundException, InsufficientBalanceException, InvalidTransactionAmountException {
        
        if (amount <= 0) {
            throw new InvalidTransactionAmountException("Withdrawal amount must be greater than zero.");
        }

        Account account = accountDAO.getAccount(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException("Account " + accountNumber + " does not exist.");
        }

        if (account.getBalance() < amount) {
            throw new InsufficientBalanceException("Transaction Failed: Low balance ($" + account.getBalance() + ")");
        }

        double newBalance = account.getBalance() - amount;
        accountDAO.updateBalance(accountNumber, newBalance);
        accountDAO.logTransaction(accountNumber, "WITHDRAWAL", amount);
        System.out.println("Success! Withdrew $" + amount + ". Remaining Balance: $" + newBalance);
    }

    public void transfer(String senderAcc, String receiverAcc, double amount) 
            throws Exception {
        
        if (amount <= 0) {
            throw new InvalidTransactionAmountException("Transfer amount must be greater than zero.");
        }

        Account sender = accountDAO.getAccount(senderAcc);
        if (sender == null) {
            throw new AccountNotFoundException("Sender account " + senderAcc + " does not exist.");
        }

        Account receiver = accountDAO.getAccount(receiverAcc);
        if (receiver == null) {
            throw new AccountNotFoundException("Receiver account " + receiverAcc + " does not exist.");
        }

        if (sender.getBalance() < amount) {
            throw new InsufficientBalanceException("Transaction Failed: Low balance ($" + sender.getBalance() + ")");
        }

        boolean success = accountDAO.transferMoney(senderAcc, receiverAcc, amount);
        
        if (!success) {
            throw new SQLException("Transfer failed due to a database error. All changes rolled back.");
        }
    }
}