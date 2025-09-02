	package Micow.ProjectC.Micow_Cashier;

import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

class UIConstants {
    public static final Color COFFEE_BROWN = new Color(101, 67, 33);
    public static final Color LIGHT_COFFEE = new Color(205, 175, 149);
    public static final Color CREAM = new Color(255, 248, 220);
    public static final Color ACCENT_ORANGE = new Color(210, 105, 30);
    public static final Color BUTTON_HOVER = new Color(255, 140, 0);
    public static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 24);
    public static final Font TABLE_FONT = new Font("Arial", Font.PLAIN, 12);
    public static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 14);
    public static final int[] COLUMN_WIDTHS = {80, 150, 100, 100, 100, 80, 100, 80};
}

class InventoryManager {
    private List<CoffeeProduct> products;
    private DataPersistence dataPersistence;

    public InventoryManager() {
        products = new ArrayList<>();
        try {
            dataPersistence = new DataPersistence();
            loadInventory();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error initializing DataPersistence: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addProduct(CoffeeProduct product) {
        products.add(product);
        saveInventory();
    }

    public void removeProduct(int index) {
        products.remove(index);
        saveInventory();
    }

    public void updateProduct(int index, CoffeeProduct updatedProduct) {
        products.set(index, updatedProduct);
        saveInventory();
    }

    public void saveInventory() {
        try {
            dataPersistence.saveInventory(products);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error saving inventory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadInventory() {
        try {
            List<CoffeeProduct> loadedProducts = dataPersistence.loadInventory();
            if (loadedProducts != null) {
                products.clear();
                products.addAll(loadedProducts);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error loading inventory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public double getTotalInventoryValue() {
        return products.stream().mapToDouble(CoffeeProduct::getTotalCost).sum();
    }

    public long getLowStockCount() {
        return products.stream().filter(p -> p.getStock() <= p.getMinStock()).count();
    }

    public List<CoffeeProduct> getProducts() {
        return products;
    }
}

public class Inventory extends JFrame {
    private InventoryManager inventoryManager;
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel totalValueLabel;
    private JLabel lowStockWarningLabel;

    public Inventory(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
        initializeUI();
        loadSampleData();
    }

    public Inventory() {
        this(new InventoryManager());
    }

    private void initializeUI() {
        setTitle("☕ Coffee Shop Inventory Tracker by Mico Abutin");
        setSize(1218, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIConstants.CREAM);
        JPanel headerPanel = createHeaderPanel();
        createInventoryTable();
        JPanel buttonPanel = createButtonPanel();
        JPanel statusPanel = createStatusPanel();
        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().add(headerPanel, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        getContentPane().add(statusPanel, BorderLayout.EAST);
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        // Set window icon
        try {
            ImageIcon icon = UIUtils.loadImageIcon(UIUtils.LOGO_PATH, 32, 32);
            if (icon != null) {
                setIconImage(icon.getImage());
            }
        } catch (Exception e) {
            System.err.println("Error loading window icon: " + e.getMessage());
        }
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIConstants.COFFEE_BROWN);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Logo
        JLabel logoLabel = new JLabel();
        ImageIcon logoIcon = UIUtils.loadImageIcon(UIUtils.LOGO_PATH, 50, 50);
        if (logoIcon != null) {
            logoLabel.setIcon(logoIcon);
        } else {
            logoLabel.setText("☕ Micow");
            logoLabel.setFont(new Font("Arial", Font.BOLD, 20));
            logoLabel.setForeground(Color.WHITE);
        }
        panel.add(logoLabel, BorderLayout.WEST);

        // Title
        JLabel titleLabel = new JLabel("Coffee Shop Inventory");
        titleLabel.setFont(UIConstants.TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, BorderLayout.CENTER);

        // Date
        JLabel dateLabel = new JLabel(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        dateLabel.setFont(UIConstants.TABLE_FONT);
        dateLabel.setForeground(UIConstants.LIGHT_COFFEE);
        panel.add(dateLabel, BorderLayout.EAST);

        return panel;
    }

    private void createInventoryTable() {
        String[] columns = {"Image", "Product Name", "Category", "Total Cost", "Unit Size", "Stock Qty", "Cost/Unit", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0) return ImageIcon.class;
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(70);
        table.setBackground(Color.WHITE);
        table.setGridColor(UIConstants.LIGHT_COFFEE);
        table.setFont(UIConstants.TABLE_FONT);
        table.setSelectionBackground(UIConstants.LIGHT_COFFEE);
        table.setSelectionForeground(UIConstants.COFFEE_BROWN);
        JTableHeader header = table.getTableHeader();
        header.setBackground(UIConstants.COFFEE_BROWN);
        header.setForeground(Color.BLACK);
        header.setFont(UIConstants.HEADER_FONT);
        for (int i = 0; i < columns.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(UIConstants.COLUMN_WIDTHS[i]);
        }
        table.getColumnModel().getColumn(7).setCellRenderer(new StatusCellRenderer());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0 && e.getClickCount() == 2) {
                    CoffeeProduct selected = inventoryManager.getProducts().get(row);
                    showProductDetails(selected);
                }
            }
        });
    }
    

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));
        panel.setBackground(UIConstants.CREAM);

        JButton addButton = createStyledButton("Add Product", " + ");
        JButton editButton = createStyledButton("Edit Product", "*");
        JButton deleteButton = createStyledButton("Delete Product", "/");
        JButton usageButton = createStyledButton("Record Usage", "'");
        JButton restockButton = createStyledButton("Restock", "📦");
        JButton ingredientUsageButton = createStyledButton("Ingredient Usage", "☕");
        JButton returnButton = createStyledButton("Return", "↩️");

        addButton.addActionListener(e -> new ProductDialog(this, "Add Coffee Product", null).setVisible(true));
        editButton.addActionListener(e -> editSelectedProduct());
        deleteButton.addActionListener(e -> deleteSelectedProduct());
        usageButton.addActionListener(e -> showUsageDialog());
        restockButton.addActionListener(e -> showRestockDialog());
        ingredientUsageButton.addActionListener(e -> {
            try {
                System.out.println("Attempting to open IngredientUsageFrame at " + new java.util.Date());
                SwingUtilities.invokeLater(() -> {
                    try {
                        IngredientUsageFrame frame = new IngredientUsageFrame(this, inventoryManager);
                        frame.setVisible(true);
                        System.out.println("IngredientUsageFrame opened successfully");
                    } catch (Exception ex) {
                        System.err.println("Error in invokeLater for IngredientUsageFrame: " + ex.getMessage());
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Error opening Ingredient Usage: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (Exception ex) {
                System.err.println("Error opening IngredientUsageFrame: " + ex.getMessage());
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error opening Ingredient Usage: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        returnButton.addActionListener(e -> onReturnButtonClicked());

        panel.add(addButton);
        panel.add(editButton);
        panel.add(deleteButton);
        panel.add(usageButton);
        panel.add(restockButton);
        panel.add(ingredientUsageButton);
        panel.add(returnButton);

        return panel;
    }

    private JButton createStyledButton(String text, String emoji) {
        JButton button = new JButton(emoji + " " + text);
        button.setBackground(UIConstants.COFFEE_BROWN);
        button.setForeground(Color.WHITE);
        button.setFont(UIConstants.HEADER_FONT);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 24, 12, 24));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(160, 40));
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            protected void paintButtonPressed(Graphics g, AbstractButton b) {
                g.setColor(UIConstants.ACCENT_ORANGE.darker());
                g.fillRoundRect(0, 0, b.getWidth(), b.getHeight(), 20, 20);
            }
        });

        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0, 50), 1),
            BorderFactory.createEmptyBorder(12, 24, 12, 24)
        ));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(UIConstants.BUTTON_HOVER);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 0, 0, 100), 1),
                    BorderFactory.createEmptyBorder(12, 24, 12, 24)
                ));
                button.setLocation(button.getX(), button.getY() - 2);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(UIConstants.COFFEE_BROWN);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 0, 0, 50), 1),
                    BorderFactory.createEmptyBorder(12, 24, 12, 24)
                ));
                button.setLocation(button.getX(), button.getY() + 2);
            }
        });

        return button;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UIConstants.CREAM);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UIConstants.COFFEE_BROWN, 2),
                "Dashboard",
                0, 0,
                UIConstants.HEADER_FONT,
                UIConstants.COFFEE_BROWN));
        panel.setPreferredSize(new Dimension(200, 0));
        totalValueLabel = new JLabel("Total Inventory: ₱0.00");
        lowStockWarningLabel = new JLabel("Low Stock Items: 0");
        JLabel[] labels = {totalValueLabel, lowStockWarningLabel};
        for (JLabel label : labels) {
            label.setFont(UIConstants.TABLE_FONT);
            label.setBorder(new EmptyBorder(5, 10, 5, 10));
            panel.add(label);
        }
        return panel;
    }

    private class ProductDialog extends JDialog {
        private JTextField nameField;
        private JComboBox<String> categoryBox;
        private JTextField costField;
        private JTextField unitSizeField;
        private JComboBox<String> unitBox;
        private JTextField stockField;
        private JTextField minStockField;
        private JLabel imageLabel;
        private String selectedImagePath;

        public ProductDialog(Frame parent, String title, CoffeeProduct product) {
            super(parent, title, true);
            setSize(500, 600);
            setLocationRelativeTo(parent);
            buildUI(product);
        }
        
        

        private void buildUI(CoffeeProduct product) {
            JPanel mainPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            nameField = new JTextField(product != null ? product.getName() : "", 20);
            String[] categories = {"Coffee Beans", "Milk Products", "Syrups", "Pastries", "Equipment", "Other"};
            categoryBox = new JComboBox<>(categories);
            if (product != null) categoryBox.setSelectedItem(product.getCategory());
            costField = new JTextField(product != null ? String.valueOf(product.getTotalCost()) : "", 20);
            unitSizeField = new JTextField(product != null ? String.valueOf(product.getUnitSize()) : "", 20);
            String[] units = {"g", "ml", "kg", "L", "pcs", "bags"};
            unitBox = new JComboBox<>(units);
            if (product != null) unitBox.setSelectedItem(product.getUnit());
            stockField = new JTextField(product != null ? String.valueOf(product.getStock()) : "", 20);
            minStockField = new JTextField(product != null ? String.valueOf(product.getMinStock()) : "", 20);

            imageLabel = new JLabel("Drop image here or click to browse", SwingConstants.CENTER);
            imageLabel.setPreferredSize(new Dimension(200, 150));
            imageLabel.setBorder(BorderFactory.createDashedBorder(UIConstants.COFFEE_BROWN, 2, 5, 5, true));
            imageLabel.setBackground(Color.WHITE);
            imageLabel.setOpaque(true);
            selectedImagePath = product != null ? product.getImagePath() : null;
            if (selectedImagePath != null) setImagePreview(imageLabel, selectedImagePath);

            new DropTarget(imageLabel, new java.awt.dnd.DropTargetAdapter() {
                @Override
                public void drop(java.awt.dnd.DropTargetDropEvent dtde) {
                    try {
                        dtde.acceptDrop(java.awt.dnd.DnDConstants.ACTION_COPY);
                        List<File> droppedFiles = (List<File>) dtde.getTransferable()
                                .getTransferData(java.awt.datatransfer.DataFlavor.javaFileListFlavor);
                        if (!droppedFiles.isEmpty()) {
                            File file = droppedFiles.get(0);
                            if (isImageFile(file)) {
                                selectedImagePath = file.getAbsolutePath();
                                setImagePreview(imageLabel, selectedImagePath);
                            } else {
                                JOptionPane.showMessageDialog(ProductDialog.this, "Please select an image file!");
                            }
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(ProductDialog.this, "Error loading image!");
                        ex.printStackTrace();
                    }
                }
            });

            imageLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                            "Image files", "jpg", "jpeg", "png", "gif"));
                    if (fileChooser.showOpenDialog(ProductDialog.this) == JFileChooser.APPROVE_OPTION) {
                        selectedImagePath = fileChooser.getSelectedFile().getAbsolutePath();
                        setImagePreview(imageLabel, selectedImagePath);
                    }
                }
            });

            gbc.gridx = 0; gbc.gridy = 0;
            mainPanel.add(new JLabel("Product Name:"), gbc);
            gbc.gridx = 1;
            mainPanel.add(nameField, gbc);
            gbc.gridx = 0; gbc.gridy = 1;
            mainPanel.add(new JLabel("Category:"), gbc);
            gbc.gridx = 1;
            mainPanel.add(categoryBox, gbc);
            gbc.gridx = 0; gbc.gridy = 2;
            mainPanel.add(new JLabel("Total Cost (₱):"), gbc);
            gbc.gridx = 1;
            mainPanel.add(costField, gbc);
            gbc.gridx = 0; gbc.gridy = 3;
            mainPanel.add(new JLabel("Unit Size:"), gbc);
            gbc.gridx = 1;
            mainPanel.add(unitSizeField, gbc);
            gbc.gridx = 0; gbc.gridy = 4;
            mainPanel.add(new JLabel("Unit:"), gbc);
            gbc.gridx = 1;
            mainPanel.add(unitBox, gbc);
            gbc.gridx = 0; gbc.gridy = 5;
            mainPanel.add(new JLabel("Stock Quantity:"), gbc);
            gbc.gridx = 1;
            mainPanel.add(stockField, gbc);
            gbc.gridx = 0; gbc.gridy = 6;
            mainPanel.add(new JLabel("Min Stock Level:"), gbc);
            gbc.gridx = 1;
            mainPanel.add(minStockField, gbc);
            gbc.gridx = 0; gbc.gridy = 7;
            gbc.gridwidth = 2;
            mainPanel.add(new JLabel("Product Image:"), gbc);
            gbc.gridy = 8;
            mainPanel.add(imageLabel, gbc);

            JPanel buttonPanel = new JPanel();
            JButton saveButton = new JButton("Save Product");
            JButton cancelButton = new JButton("Cancel");
            saveButton.addActionListener(e -> saveProduct(product));
            cancelButton.addActionListener(e -> dispose());
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            gbc.gridy = 9;
            mainPanel.add(buttonPanel, gbc);

            add(mainPanel);
        }

        private void saveProduct(CoffeeProduct product) {
            try {
                String name = nameField.getText().trim();
                double cost = Double.parseDouble(costField.getText());
                double unitSize = Double.parseDouble(unitSizeField.getText());
                double stock = Double.parseDouble(stockField.getText());
                double minStock = Double.parseDouble(minStockField.getText());

                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter a product name!");
                    return;
                }
                if (cost < 0 || unitSize <= 0 || stock < 0 || minStock < 0) {
                    JOptionPane.showMessageDialog(this, "Values cannot be negative or zero (except stock)!");
                    return;
                }
                if (minStock > stock) {
                    JOptionPane.showMessageDialog(this, "Minimum stock cannot exceed current stock!");
                    return;
                }

                CoffeeProduct newProduct = new CoffeeProduct(name, (String) categoryBox.getSelectedItem(),
                        cost, unitSize, (String) unitBox.getSelectedItem(), stock, minStock, selectedImagePath);
                if (product == null) {
                    inventoryManager.addProduct(newProduct);
                } else {
                    int index = inventoryManager.getProducts().indexOf(product);
                    inventoryManager.updateProduct(index, newProduct);
                }
                refreshTable();
                updateDashboard();
                dispose();
                JOptionPane.showMessageDialog(Inventory.this, "Product saved successfully!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers!");
                ex.printStackTrace();
            }
        }
    }

    private void editSelectedProduct() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product to edit!");
            return;
        }
        CoffeeProduct product = inventoryManager.getProducts().get(selectedRow);
        new ProductDialog(this, "Edit Coffee Product", product).setVisible(true);
    }

    private void deleteSelectedProduct() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete!");
            return;
        }
        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this product?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            inventoryManager.removeProduct(selectedRow);
            refreshTable();
            updateDashboard();
        }
    }

    private void showUsageDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product to record usage!");
            return;
        }
        CoffeeProduct product = inventoryManager.getProducts().get(selectedRow);
        String usage = JOptionPane.showInputDialog(this,
                "Enter quantity used for " + product.getName() + " (in " + product.getUnit() + "):");
        try {
            double usageAmount = Double.parseDouble(usage);
            if (usageAmount < 0) {
                JOptionPane.showMessageDialog(this, "Usage quantity cannot be negative!");
                return;
            }
            product.reduceStock(usageAmount);
            refreshTable();
            updateDashboard();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number!");
            ex.printStackTrace();
        }
    }

    private void showRestockDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product to restock!");
            return;
        }
        CoffeeProduct product = inventoryManager.getProducts().get(selectedRow);
        String restock = JOptionPane.showInputDialog(this,
                "Enter restock quantity for " + product.getName() + " (in " + product.getUnit() + "):");
        try {
            double restockAmount = Double.parseDouble(restock);
            if (restockAmount < 0) {
                JOptionPane.showMessageDialog(this, "Restock quantity cannot be negative!");
                return;
            }
            String costStr = JOptionPane.showInputDialog(this,
                    "Enter cost for the restocked amount (₱):");
            double addedCost = Double.parseDouble(costStr);
            if (addedCost < 0) {
                JOptionPane.showMessageDialog(this, "Cost cannot be negative!");
                return;
            }
            product.addStock(restockAmount, addedCost);
            refreshTable();
            updateDashboard();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number!");
            ex.printStackTrace();
        }
    }

    private void showProductDetails(CoffeeProduct product) {
        JDialog dialog = new JDialog(this, product.getName() + " - Details", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new BorderLayout());
        if (product.getImagePath() != null) {
            ImageIcon icon = new ImageIcon(product.getImagePath());
            Image scaled = icon.getImage().getScaledInstance(300, 200, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaled));
            panel.add(imageLabel, BorderLayout.NORTH);
        }
        StringBuilder details = new StringBuilder("<html><body style='padding: 10px; font-family: Arial;'>");
        details.append("<h3>").append(product.getName()).append("</h3>");
        details.append("<p><b>Category:</b> ").append(product.getCategory()).append("</p>");
        details.append("<p><b>Total Cost:</b> ₱").append(String.format("%.2f", product.getTotalCost())).append("</p>");
        details.append("<p><b>Unit Size:</b> ").append(product.getUnitSize()).append(" ").append(product.getUnit()).append("</p>");
        details.append("<p><b>Stock:</b> ").append(String.format("%.2f", product.getStock())).append("</p>");
        details.append("<p><b>Min Stock:</b> ").append(String.format("%.2f", product.getMinStock())).append("</p>");
        details.append("<p><b>Cost per Unit:</b> ₱").append(String.format("%.2f", product.getCostPerUnit())).append("</p>");
        details.append("<p><b>Status:</b> ").append(product.getStockStatus()).append("</p>");
        details.append("</body></html>");
        JLabel detailsLabel = new JLabel(details.toString());
        panel.add(detailsLabel, BorderLayout.CENTER);
        dialog.getContentPane().add(panel);
        dialog.setVisible(true);
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        for (CoffeeProduct product : inventoryManager.getProducts()) {
            ImageIcon icon = null;
            if (product.getImagePath() != null && new File(product.getImagePath()).exists()) {
                ImageIcon originalIcon = new ImageIcon(product.getImagePath());
                Image scaledImage = originalIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                icon = new ImageIcon(scaledImage);
            }
            tableModel.addRow(new Object[]{
                    icon,
                    product.getName(),
                    product.getCategory(),
                    "₱" + String.format("%.2f", product.getTotalCost()),
                    product.getUnitSize() + " " + product.getUnit(),
                    String.format("%.2f", product.getStock()),
                    "₱" + String.format("%.2f", product.getCostPerUnit()),
                    product.getStockStatus()
            });
        }
    }

    void updateDashboard() {
        double totalValue = inventoryManager.getTotalInventoryValue();
        long lowStockCount = inventoryManager.getLowStockCount();
        totalValueLabel.setText("Total Inventory: ₱" + String.format("%.2f", totalValue));
        lowStockWarningLabel.setText("Low Stock Items: " + lowStockCount);
        lowStockWarningLabel.setForeground(lowStockCount > 0 ? Color.RED : Color.BLACK);
    }

    private void setImagePreview(JLabel label, String imagePath) {
        if (imagePath != null && new File(imagePath).exists()) {
            ImageIcon icon = new ImageIcon(imagePath);
            Image scaled = icon.getImage().getScaledInstance(200, 150, Image.SCALE_SMOOTH);
            label.setText("");
            label.setIcon(new ImageIcon(scaled));
        }
    }

    private void onReturnButtonClicked() {
        this.dispose();
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                name.endsWith(".png") || name.endsWith(".gif");
    }

    private void loadSampleData() {
        refreshTable();
        updateDashboard();
    }

    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                      boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                String status = value.toString();
                switch (status) {
                    case "Low Stock":
                        c.setBackground(new Color(255, 200, 200));
                        c.setForeground(Color.RED);
                        break;
                    case "Out of Stock":
                        c.setBackground(new Color(255, 150, 150));
                        c.setForeground(Color.RED);
                        break;
                    case "In Stock":
                        c.setBackground(new Color(200, 255, 200));
                        c.setForeground(new Color(0, 120, 0));
                        break;
                    default:
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                }
            }
            setHorizontalAlignment(SwingConstants.CENTER);
            return c;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new Inventory().setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error initializing UI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}

