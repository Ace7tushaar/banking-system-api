package com.bank;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // Seed default account data into the H2 database on application startup
    @PostConstruct
    public void initDatabase() {
        if (!accountRepository.existsById("ACC1001")) {
            accountRepository.save(new Account("ACC1001", "Tushar", 5000.00, "Active"));
        }
    }

    public Account getAccount(String accountNumber) {
        return accountRepository.findById(accountNumber).orElse(null);
    }

    public Account deposit(String accountNumber, double amount) {
        Account account = getAccount(accountNumber);
        if (account != null) {
            account.deposit(amount);
            return accountRepository.save(account);
        }
        return null;
    }

    public Account withdraw(String accountNumber, double amount) {
        Account account = getAccount(accountNumber);
        if (account != null && account.withdraw(amount)) {
            return accountRepository.save(account);
        }
        return null;
    }

    public List<Transaction> getTransactions(String accountNumber) {
        Account account = getAccount(accountNumber);
        if (account != null) {
            return account.getTransactions();
        }
        return null;
    }
}