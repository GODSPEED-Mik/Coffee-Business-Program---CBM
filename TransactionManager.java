package Micow.ProjectC.Micow_Cashier;

import java.util.ArrayList;
import java.util.List;

public class TransactionManager {
    private List<Transaction> transactions;
    private TransactionPersistence persistence;

    // ✅ Default constructor (no argument)
    public TransactionManager() {
        this("transactions.dat"); // Use a default file name
    }

    // Existing constructor
    public TransactionManager(String filePath) {
        persistence = new TransactionPersistence(filePath);
        transactions = persistence.loadTransactions();
    }

    public void addTransaction(Transaction tx) {
        transactions.add(tx);
        persistence.saveTransactions(transactions);
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}
