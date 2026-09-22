package com.bank;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Account {

    @Id
    private String accountNumber;
    private String accountHolder;
    private double balance;
    private String status;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Transaction> transactions = new ArrayList<>();

    public Account() {}

    public Account(String accountNumber, String accountHolder, double balance, String status) {
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.balance = balance;
        this.status = status;
    }

    // Getters and Setters
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getAccountHolder() { return accountHolder; }
    public void setAccountHolder(String accountHolder) { this.accountHolder = accountHolder; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }

    // Business Logic
    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
            this.transactions.add(new Transaction("DEPOSIT", amount, this.balance));
        }
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && this.balance >= amount) {
            this.balance -= amount;
            this.transactions.add(new Transaction("WITHDRAWAL", amount, this.balance));
            return true;
        }
        return false;
    }
}