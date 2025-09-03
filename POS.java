package Micow.ProjectC.Micow_Cashier;

public class POS {
    public static void main(String[] args) {
        // Create Inventory & Transaction managers
        InventoryManager inventoryManager = new InventoryManager();
        TransactionManager transactionManager = new TransactionManager();

        // Load inventory (if you have saved inventory data)
        inventoryManager.loadInventory();

        // Start POS Window
        javax.swing.SwingUtilities.invokeLater(() -> {
            POSWindow posWindow = new POSWindow(inventoryManager, transactionManager);
            posWindow.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
            posWindow.setVisible(true);
        });
    }
}
