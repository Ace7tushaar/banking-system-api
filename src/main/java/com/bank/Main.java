package com.bank;

import com.bank.exception.AccountNotFoundException;
import com.bank.exception.InsufficientBalanceException;
import com.bank.exception.InvalidTransactionAmountException;
import com.bank.service.BankingServiceImpl;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        BankingServiceImpl service = new BankingServiceImpl();
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("====================================");
        System.out.println("   CONSOLE BANKING SYSTEM STARTED   ");
        System.out.println("====================================");

        while (running) {
            System.out.println("\nSelect an Option:");
            System.out.println("1. Deposit Money");
            System.out.println("2. Withdraw Money");
            System.out.println("3. Transfer Money (ACID Transaction)");
            System.out.println("4. Exit");
            System.out.print("Enter choice (1-4): ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline after choice input

            if (choice == 4) {
                running = false;
                System.out.println("Exiting Console Banking System. Goodbye!");
                break;
            }

            try {
                switch (choice) {
                    case 1:
                        System.out.print("Enter Account Number (e.g. ACC1001): ");
                        String depAcc = scanner.nextLine();
                        System.out.print("Enter Amount: ");
                        double depAmount = scanner.nextDouble();
                        scanner.nextLine(); // Consume newline after amount
                        service.deposit(depAcc, depAmount);
                        break;

                    case 2:
                        System.out.print("Enter Account Number (e.g. ACC1001): ");
                        String withAcc = scanner.nextLine();
                        System.out.print("Enter Amount: ");
                        double withAmount = scanner.nextDouble();
                        scanner.nextLine(); // Consume newline after amount
                        service.withdraw(withAcc, withAmount);
                        break;

                    case 3:
                        System.out.print("Enter Sender Account Number: ");
                        String senderAcc = scanner.nextLine();
                        System.out.print("Enter Receiver Account Number: ");
                        String receiverAcc = scanner.nextLine();
                        System.out.print("Enter Transfer Amount: ");
                        double transferAmount = scanner.nextDouble();
                        scanner.nextLine(); // Consume newline after amount

                        service.transfer(senderAcc, receiverAcc, transferAmount);
                        break;

                    default:
                        System.out.println("Invalid choice. Please enter 1, 2, 3, or 4.");
                }
            } catch (AccountNotFoundException e) {
                System.err.println("Account Error: " + e.getMessage());
            } catch (InsufficientBalanceException e) {
                System.err.println("Business Logic Error: " + e.getMessage());
            } catch (InvalidTransactionAmountException e) {
                System.err.println("Input Error: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Database/System Error: " + e.getMessage());
            }
        }
        scanner.close();
    }
}