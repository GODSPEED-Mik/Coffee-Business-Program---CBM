package Micow.ProjectC.Micow_Cashier;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class POSWindow extends JFrame {
    private InventoryManager inventoryManager;
    private TransactionManager txManager;
    private DefaultTableModel cartModel;
    private JLabel totalLabel;
    private JTextField qtyField;
    private JList<CoffeeProduct> productList;
    private DefaultListModel<CoffeeProduct> productListModel;
    private DecimalFormat currencyFormat = new DecimalFormat("₱#,##0.00");
    private List<CartItem> cartItems = new ArrayList<>();
    
    // Modern Color Scheme
    private static final Color PRIMARY_COLOR = new Color(79, 70, 229);      // Indigo
    private static final Color SECONDARY_COLOR = new Color(99, 102, 241);   // Light Indigo
    private static final Color ACCENT_COLOR = new Color(34, 197, 94);       // Green
    private static final Color DANGER_COLOR = new Color(239, 68, 68);       // Red
    private static final Color BACKGROUND_COLOR = new Color(248, 250, 252); // Light Gray
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(30, 41, 59);        // Dark Gray
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);   // Medium Gray

    // Inner class for cart items
    private static class CartItem {
        CoffeeProduct product;
        double quantity;
        
        CartItem(CoffeeProduct product, double quantity) {
            this.product = product;
            this.quantity = quantity;
        }
        
        double getSubtotal() {
            return product.getCostPerUnit() * quantity;
        }
    }

    public POSWindow(InventoryManager invMgr, TransactionManager txMgr) {
        super("Micow Coffee POS System");
        this.inventoryManager = invMgr;
        this.txManager = txMgr;
        
        setupLookAndFeel();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Add window icon
        setIconImage(createIcon());
    }
    
    private void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());
            
            // Customize UI defaults
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private Image createIcon() {
        // Create a simple coffee cup icon
        BufferedImage icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = icon.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(PRIMARY_COLOR);
        g2.fillRoundRect(8, 8, 16, 16, 4, 4);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(10, 10, 12, 12, 2, 2);
        g2.dispose();
        return icon;
    }

    private void initializeComponents() {
        // Initialize product list
        productListModel = new DefaultListModel<>();
        for (CoffeeProduct product : inventoryManager.getProducts()) {
            productListModel.addElement(product);
        }
        
        productList = new JList<>(productListModel);
        productList.setCellRenderer(new ProductListCellRenderer());
        productList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productList.setFixedCellHeight(80);
        
        // Initialize cart table
        String[] columns = {"Product", "Quantity", "Unit Price", "Subtotal", "Actions"};
        cartModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only actions column is editable
            }
        };
        
        // Initialize other components
        qtyField = new JTextField("1", 5);
        totalLabel = new JLabel("Total: ₱0.00");
        totalLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        totalLabel.setForeground(PRIMARY_COLOR);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Create header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Create main content panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Left panel - Products
        JPanel leftPanel = createProductPanel();
        
        // Right panel - Cart and controls
        JPanel rightPanel = createCartPanel();
        
        // Split pane for responsive layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(400);
        splitPane.setOneTouchExpandable(true);
        splitPane.setBorder(null);
        splitPane.setBackground(BACKGROUND_COLOR);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        JLabel titleLabel = new JLabel("Micow Coffee POS System");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel dateTimeLabel = new JLabel(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm")));
        dateTimeLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        dateTimeLabel.setForeground(Color.WHITE);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(PRIMARY_COLOR);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(dateTimeLabel, BorderLayout.EAST);
        
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        return headerPanel;
    }
    
    private JPanel createProductPanel() {
        JPanel productPanel = new JPanel(new BorderLayout());
        productPanel.setBackground(CARD_COLOR);
        productPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        // Products header
        JLabel productsLabel = new JLabel("☕ Available Products");
        productsLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        productsLabel.setForeground(TEXT_PRIMARY);
        productsLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        // Product list in scroll pane
        JScrollPane productScrollPane = new JScrollPane(productList);
        productScrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        productScrollPane.getViewport().setBackground(Color.WHITE);
        
        // Add to cart controls
        JPanel addToCartPanel = createAddToCartPanel();
        
        productPanel.add(productsLabel, BorderLayout.NORTH);
        productPanel.add(productScrollPane, BorderLayout.CENTER);
        productPanel.add(addToCartPanel, BorderLayout.SOUTH);
        
        return productPanel;
    }
    
    private JPanel createAddToCartPanel() {
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        addPanel.setBackground(CARD_COLOR);
        
        JLabel qtyLabel = new JLabel("Quantity:");
        qtyLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        qtyLabel.setForeground(TEXT_PRIMARY);
        
        qtyField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        qtyField.setPreferredSize(new Dimension(80, 35));
        qtyField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        
        JButton addButton = createStyledButton("Add to Cart", ACCENT_COLOR);
        addButton.setPreferredSize(new Dimension(120, 35));
        
        addPanel.add(qtyLabel);
        addPanel.add(qtyField);
        addPanel.add(addButton);
        
        return addPanel;
    }
    
    private JPanel createCartPanel() {
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setBackground(CARD_COLOR);
        cartPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        // Cart header
        JLabel cartLabel = new JLabel("🛒 Shopping Cart");
        cartLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        cartLabel.setForeground(TEXT_PRIMARY);
        cartLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        // Cart table
        JTable cartTable = new JTable(cartModel);
        cartTable.setRowHeight(50);
        cartTable.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        cartTable.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        cartTable.getTableHeader().setBackground(BACKGROUND_COLOR);
        cartTable.getTableHeader().setForeground(TEXT_PRIMARY);
        
        // Custom cell renderer for better appearance
        cartTable.setDefaultRenderer(Object.class, new CartTableCellRenderer());
        
        JScrollPane cartScrollPane = new JScrollPane(cartTable);
        cartScrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        cartScrollPane.getViewport().setBackground(Color.WHITE);
        
        // Bottom panel with total and checkout
        JPanel bottomPanel = createBottomPanel();
        
        cartPanel.add(cartLabel, BorderLayout.NORTH);
        cartPanel.add(cartScrollPane, BorderLayout.CENTER);
        cartPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        return cartPanel;
    }
    
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(CARD_COLOR);
        bottomPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        // Total panel
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.setBackground(CARD_COLOR);
        totalPanel.add(totalLabel);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(CARD_COLOR);
        
        JButton clearButton = createStyledButton("Clear Cart", DANGER_COLOR);
        JButton checkoutButton = createStyledButton("Checkout", PRIMARY_COLOR);
        
        clearButton.setPreferredSize(new Dimension(120, 40));
        checkoutButton.setPreferredSize(new Dimension(120, 40));
        
        buttonPanel.add(clearButton);
        buttonPanel.add(checkoutButton);
        
        bottomPanel.add(totalPanel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return bottomPanel;
    }
    
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        button.setForeground(Color.BLACK);
        button.setBackground(backgroundColor);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
            }
        });
        
        return button;
    }
    
    private void setupEventHandlers() {
        // Add to cart button
        for (Component comp : getAllComponents(this)) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                if ("Add to Cart".equals(btn.getText())) {
                    btn.addActionListener(this::addToCart);
                } else if ("Checkout".equals(btn.getText())) {
                    btn.addActionListener(this::checkout);
                } else if ("Clear Cart".equals(btn.getText())) {
                    btn.addActionListener(this::clearCart);
                }
            }
        }
        
        // Double-click on product to add
        productList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    addToCart(null);
                }
            }
        });
        
        // Enter key in quantity field
        qtyField.addActionListener(this::addToCart);
    }
    
    private java.util.List<Component> getAllComponents(Container container) {
        java.util.List<Component> components = new ArrayList<>();
        for (Component comp : container.getComponents()) {
            components.add(comp);
            if (comp instanceof Container) {
                components.addAll(getAllComponents((Container) comp));
            }
        }
        return components;
    }
    
    private void addToCart(ActionEvent e) {
        CoffeeProduct selected = productList.getSelectedValue();
        if (selected == null) {
            showMessage("Please select a product first.", "No Product Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            double quantity = Double.parseDouble(qtyField.getText().trim());
            if (quantity <= 0) {
                showMessage("Please enter a valid quantity.", "Invalid Quantity", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (quantity > selected.getStock()) {
                showMessage("Insufficient stock! Available: " + selected.getStock(), "Stock Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Check if product already in cart
            boolean found = false;
            for (int i = 0; i < cartItems.size(); i++) {
                CartItem item = cartItems.get(i);
                if (item.product.getName().equals(selected.getName())) {
                    item.quantity += quantity;
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                cartItems.add(new CartItem(selected, quantity));
            }
            
            updateCartDisplay();
            qtyField.setText("1");
            qtyField.requestFocus();
            
        } catch (NumberFormatException ex) {
            showMessage("Please enter a valid number for quantity.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateCartDisplay() {
        cartModel.setRowCount(0);
        double total = 0;
        
        for (CartItem item : cartItems) {
            double subtotal = item.getSubtotal();
            total += subtotal;
            
            Object[] row = {
                item.product.getName(),
                String.format("%.1f", item.quantity),
                currencyFormat.format(item.product.getCostPerUnit()),
                currencyFormat.format(subtotal),
                "Remove"
            };
            cartModel.addRow(row);
        }
        
        totalLabel.setText("Total: " + currencyFormat.format(total));
    }
    
    private void clearCart(ActionEvent e) {
        if (cartItems.isEmpty()) return;
        
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to clear the cart?",
            "Clear Cart",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            cartItems.clear();
            updateCartDisplay();
        }
    }
    
    private void checkout(ActionEvent e) {
        if (cartItems.isEmpty()) {
            showMessage("Cart is empty! Please add items before checkout.", "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Create transaction
        Transaction tx = new Transaction();
        tx.setTimestamp(LocalDateTime.now());
        
        double total = 0;
        StringBuilder receipt = new StringBuilder();
        receipt.append("MICOW COFFEE RECEIPT\n");
        receipt.append("===================\n");
        receipt.append(String.format("Date: %s\n\n", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))));
        
        for (CartItem item : cartItems) {
            double subtotal = item.getSubtotal();
            total += subtotal;
            
            // Add to transaction
            tx.addItem(new TransactionItem(-1, item.product.getName(), item.quantity, item.product.getCostPerUnit()));
            
            // Add to receipt
            receipt.append(String.format("%-20s %6.1f x %8s = %10s\n",
                item.product.getName(),
                item.quantity,
                currencyFormat.format(item.product.getCostPerUnit()),
                currencyFormat.format(subtotal)
            ));
            
            // Update stock
            item.product.reduceStock(item.quantity);
        }
        
        tx.setTotal(total);
        txManager.addTransaction(tx);
        inventoryManager.saveInventory();
        
        receipt.append("\n===================\n");
        receipt.append(String.format("TOTAL: %s\n", currencyFormat.format(total)));
        receipt.append("===================\n");
        receipt.append("Thank you for your business!");
        
        // Show receipt
        JTextArea receiptArea = new JTextArea(receipt.toString());
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        receiptArea.setEditable(false);
        
        JScrollPane scrollPane = new JScrollPane(receiptArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Transaction Complete", JOptionPane.INFORMATION_MESSAGE);
        
        // Clear cart
        cartItems.clear();
        updateCartDisplay();
        
        // Refresh product list to show updated stock
        productList.repaint();
    }
    
    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }
    
    // Custom cell renderer for product list
    private class ProductListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            
            if (value instanceof CoffeeProduct) {
                CoffeeProduct product = (CoffeeProduct) value;
                
                JPanel panel = new JPanel(new BorderLayout());
                panel.setBorder(new EmptyBorder(10, 15, 10, 15));
                
                if (isSelected) {
                    panel.setBackground(SECONDARY_COLOR);
                } else {
                    panel.setBackground(index % 2 == 0 ? Color.WHITE : BACKGROUND_COLOR);
                }
                
                JLabel nameLabel = new JLabel(product.getName());
                nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
                nameLabel.setForeground(isSelected ? Color.WHITE : TEXT_PRIMARY);
                
                JLabel priceLabel = new JLabel(currencyFormat.format(product.getCostPerUnit()));
                priceLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
                priceLabel.setForeground(isSelected ? Color.WHITE : ACCENT_COLOR);
                
                JLabel stockLabel = new JLabel("Stock: " + (int)product.getStock());
                stockLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
                stockLabel.setForeground(isSelected ? Color.WHITE : TEXT_SECONDARY);
                
                JPanel rightPanel = new JPanel(new BorderLayout());
                rightPanel.setOpaque(false);
                rightPanel.add(priceLabel, BorderLayout.NORTH);
                rightPanel.add(stockLabel, BorderLayout.SOUTH);
                
                panel.add(nameLabel, BorderLayout.WEST);
                panel.add(rightPanel, BorderLayout.EAST);
                
                return panel;
            }
            
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
    
    // Custom cell renderer for cart table
    private class CartTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (column == 4) { // Actions column
                JButton removeButton = new JButton("Remove");
                removeButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
                removeButton.setForeground(Color.WHITE);
                removeButton.setBackground(DANGER_COLOR);
                removeButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                removeButton.setFocusPainted(false);
                
                removeButton.addActionListener(e -> {
                    if (row < cartItems.size()) {
                        cartItems.remove(row);
                        updateCartDisplay();
                    }
                });
                
                return removeButton;
            }
            
            // Alternate row colors
            if (!isSelected) {
                comp.setBackground(row % 2 == 0 ? Color.WHITE : BACKGROUND_COLOR);
            }
            
            setHorizontalAlignment(column == 1 || column == 2 || column == 3 ? SwingConstants.RIGHT : SwingConstants.LEFT);
            
            return comp;
        }
    }
}