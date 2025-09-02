package Micow.ProjectC.Micow_Cashier;

import javax.swing.*;

import Micow.ProjectC.Micow_Cashier.IngredientUsageFrame.SavedProduct;

import java.awt.*;
import java.util.List;

public class OurProduct extends JFrame {
    private InventoryManager inventoryManager;
    private List<IngredientUsageFrame.SavedProduct> savedProducts;

    public OurProduct(InventoryManager inventoryManager, List<IngredientUsageFrame.SavedProduct> savedProducts) {
        this.inventoryManager = inventoryManager;
        this.savedProducts = savedProducts;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Products Gallery");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIConstants.CREAM);
        JLabel label = new JLabel("Products Gallery (Placeholder)", SwingConstants.CENTER);
        label.setFont(UIConstants.HEADER_FONT);
        panel.add(label, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.setBackground(UIConstants.COFFEE_BROWN);
        closeButton.setForeground(Color.WHITE);
        closeButton.setFont(UIConstants.HEADER_FONT);
        closeButton.addActionListener(e -> dispose());
        panel.add(closeButton, BorderLayout.SOUTH);

        add(panel);
    }

    public void updateSavedProducts(List<IngredientUsageFrame.SavedProduct> savedProducts) {
        this.savedProducts = savedProducts;
        // Placeholder: Update UI with saved products
    }
}