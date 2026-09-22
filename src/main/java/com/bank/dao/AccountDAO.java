package com.bank.dao;

import com.bank.model.Account;
import com.bank.model.SavingsAccount;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class AccountDAO {

    public Account getAccount(String accountNumber) throws SQLException {
        String query = "SELECT * FROM accounts WHERE account_number = ?";
        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("holder_name");
                double balance = rs.getDouble("balance");
                return new SavingsAccount(accountNumber, name, balance);
            }
        }
        return null;
    }

    public void updateBalance(String accountNumber, double newBalance) throws SQLException {
        String query = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, newBalance);
            stmt.setString(2, accountNumber);
            stmt.executeUpdate();
        }
    }

    public void logTransaction(String accountNumber, String type, double amount) throws SQLException {
        String query = "INSERT INTO transactions (account_number, type, amount) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, accountNumber);
            stmt.setString(2, type);
            stmt.setDouble(3, amount);
            stmt.executeUpdate();
        }
    }

    // ==========================================
    // STEP 1 UPGRADE: TRANSACTION CONTROL METHOD
    // ==========================================
    public boolean transferMoney(String senderAcc, String receiverAcc, double amount) {
        String deductQuery = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
        String addQuery = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
        String logQuery = "INSERT INTO transactions (account_number, type, amount) VALUES (?, ?, ?)";

        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();

            // 1. TURN OFF AUTO-COMMIT (Start Transaction)
            conn.setAutoCommit(false);

            // Step A: Deduct money from sender
            try (PreparedStatement deductStmt = conn.prepareStatement(deductQuery)) {
                deductStmt.setDouble(1, amount);
                deductStmt.setString(2, senderAcc);
                int rowsAffected = deductStmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Sender account not found.");
                }
            }

            // Step B: Add money to receiver
            try (PreparedStatement addStmt = conn.prepareStatement(addQuery)) {
                addStmt.setDouble(1, amount);
                addStmt.setString(2, receiverAcc);
                int rowsAffected = addStmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Receiver account not found.");
                }
            }

            // Step C: Log transaction for Sender
            try (PreparedStatement logStmt1 = conn.prepareStatement(logQuery)) {
                logStmt1.setString(1, senderAcc);
                logStmt1.setString(2, "TRANSFER_OUT");
                logStmt1.setDouble(3, amount);
                logStmt1.executeUpdate();
            }

            // Step D: Log transaction for Receiver
            try (PreparedStatement logStmt2 = conn.prepareStatement(logQuery)) {
                logStmt2.setString(1, receiverAcc);
                logStmt2.setString(2, "TRANSFER_IN");
                logStmt2.setDouble(3, amount);
                logStmt2.executeUpdate();
            }

            // 2. COMMIT ALL CHANGES PERMANENTLY (Only reached if all steps succeed)
            conn.commit();
            System.out.println("Transfer successful!");
            return true;

        } catch (SQLException e) {
            System.err.println("Transaction failed: " + e.getMessage());
            
            // 3. ROLLBACK EVERYTHING IF ANY STEP FAILED
            if (conn != null) {
                try {
                    System.err.println("Rolling back all database changes...");
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            return false;

        } finally {
            // Restore default state and close connection
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // ==========================================
    // STEP 3 UPGRADE: CSV AUDIT LOG EXPORTER
    // ==========================================
    public void exportAuditLogsToCSV(String accountNumber, String filePath) throws SQLException, IOException {
        String query = "SELECT * FROM transactions WHERE account_number = ? ORDER BY timestamp DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                // Write CSV Header
                writer.write("TransactionID,AccountNumber,Type,Amount,Timestamp");
                writer.newLine();

                boolean hasData = false;
                while (rs.next()) {
                    hasData = true;
                    String line = String.format("%d,%s,%s,%.2f,%s",
                            rs.getInt("id"),
                            rs.getString("account_number"),
                            rs.getString("type"),
                            rs.getDouble("amount"),
                            rs.getTimestamp("timestamp")
                    );
                    writer.write(line);
                    writer.newLine();
                }

                if (!hasData) {
                    System.out.println("No transaction history found for account: " + accountNumber);
                } else {
                    System.out.println("Audit log exported successfully to: " + filePath);
                }
            }
        }
    }
}