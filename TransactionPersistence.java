package Micow.ProjectC.Micow_Cashier;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionPersistence {
    private final File file;

    public TransactionPersistence(String path) {
        this.file = new File(path);
    }

    @SuppressWarnings("unchecked")
    public List<Transaction> loadTransactions() {
        if (!file.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<Transaction>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void saveTransactions(List<Transaction> transactions) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(transactions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
