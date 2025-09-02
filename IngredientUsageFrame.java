package Micow.ProjectC.Micow_Cashier;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Enhanced Recipe Cost Calculator with improved error handling, 
 * better user experience, and optimized performance.
 */
public class IngredientUsageFrame extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(IngredientUsageFrame.class.getName());
    
    // Enhanced Constants
    private static final Color CREAM = UIConstants.CREAM;
    private static final Color COFFEE_BROWN = UIConstants.COFFEE_BROWN;
    private static final Color EDITABLE_CELL_COLOR = new Color(255, 255, 224);
    private static final Color ALTERNATE_ROW_COLOR = new Color(255, 248, 220);
    private static final Color PROFIT_COLOR = new Color(0, 120, 0);
    private static final Color WARNING_COLOR = new Color(255, 165, 0);
    private static final Color ERROR_COLOR = new Color(220, 20, 60);
    private static final Color BUTTON_HOVER = UIConstants.BUTTON_HOVER;
    private static final Color ACCENT_ORANGE = UIConstants.ACCENT_ORANGE;
    
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("₱#,##0.00");
    private static final DecimalFormat QUANTITY_FORMAT = new DecimalFormat("#,##0.##");
    private static final double DEFAULT_PROFIT_MARGIN = 0.30;
    private static final Dimension FRAME_SIZE = new Dimension(800, 600);
    private static final Dimension IMAGE_PANEL_SIZE = new Dimension(150, 220);
    private static final Dimension TABLE_SIZE = new Dimension(450, 250);
    
    // Core components
    private final List<SavedProduct> savedProducts = new ArrayList<>();
    private final List<CoffeeProduct> products;
    private final Inventory parentInventory;
    private final RecipeManager recipeManager;
    private final InventoryManager inventoryManager;
    
    // Static registry for cross-window communication
    private static final List<OurProduct> openOurProductWindows = Collections.synchronizedList(new ArrayList<>());
    
    // UI Components
    private DefaultTableModel savedProductsModel;
    private JTable savedProductsTable;
    private DefaultTableModel tableModel;
    private JTable ingredientTable;
    private JComboBox<String> recipeSelector;
    private JLabel productNameLabel;
    private JLabel productCostLabel;
    private JLabel profitLabel;
    private JLabel stockWarningLabel;
    private JLabel imageLabel;
    private JProgressBar preparationProgress;
    
    // State management
    private String currentRecipe = "Caramel Macchiato";
    private boolean hasUnsavedChanges = false;

    public IngredientUsageFrame(Inventory parentInventory, InventoryManager inventoryManager) {
        LOGGER.info("Initializing IngredientUsageFrame");
        
        this.parentInventory = parentInventory;
        this.inventoryManager = inventoryManager;
        this.products = inventoryManager.getProducts();
        this.recipeManager = new RecipeManager();
        
        try {
            SwingUtilities.invokeLater(this::initializeUI);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize IngredientUsageFrame", e);
            showErrorDialog("Initialization Error", "Failed to initialize Recipe Calculator: " + e.getMessage());
        }
    }

    private void initializeUI() {
        try {
            initializeComponents();
            setupUI();
            loadDefaultRecipe();
            refreshSavedProductsUI();
            LOGGER.info("IngredientUsageFrame initialized successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "UI initialization failed", e);
            showErrorDialog("UI Error", "Failed to setup user interface: " + e.getMessage());
        }
    }

    // Public API methods
    public List<SavedProduct> getSavedProducts() {
        return Collections.unmodifiableList(savedProducts);
    }

    public static void registerOurProductWindow(OurProduct window) {
        openOurProductWindows.add(window);
        LOGGER.info("Registered OurProduct window. Total open: " + openOurProductWindows.size());
    }

    public static void unregisterOurProductWindow(OurProduct window) {
        openOurProductWindows.remove(window);
        LOGGER.info("Unregistered OurProduct window. Total open: " + openOurProductWindows.size());
    }

    private void notifyOurProductWindows() {
        SwingUtilities.invokeLater(() -> {
            synchronized (openOurProductWindows) {
                openOurProductWindows.removeIf(window -> {
                    try {
                        if (window.isDisplayable()) {
                            window.updateSavedProducts(getSavedProducts());
                            return false;
                        }
                        return true;
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Error notifying OurProduct window", e);
                        return true;
                    }
                });
            }
        });
    }

    private void initializeComponents() {
        this.tableModel = createTableModel();
        this.ingredientTable = createTable();
        this.recipeSelector = createRecipeSelector();
        this.productNameLabel = createProductNameLabel();
        this.productCostLabel = createCostLabel();
        this.profitLabel = createProfitLabel();
        this.stockWarningLabel = createStockWarningLabel();
        this.imageLabel = createImageLabel();
        this.preparationProgress = createProgressBar();
    }

    private void setupUI() {
        configureFrame();
        layoutComponents();
        setupEventHandlers();
        setupKeyBindings();
    }

    private void configureFrame() {
        setTitle("Recipe Cost Calculator - Enhanced");
        setSize(FRAME_SIZE);
        setLocationRelativeTo(parentInventory);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(CREAM);
        
        // Enhanced window closing with unsaved changes check
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClosing();
            }
            
            @Override
            public void windowClosed(WindowEvent e) {
                LOGGER.info("IngredientUsageFrame closed");
            }
        });
    }

    private void handleWindowClosing() {
        if (hasUnsavedChanges) {
            int result = JOptionPane.showConfirmDialog(this,
                "You have unsaved changes. Do you want to save before closing?",
                "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION);
            
            switch (result) {
                case JOptionPane.YES_OPTION:
                    saveCurrentRecipe();
                    dispose();
                    break;
                case JOptionPane.NO_OPTION:
                    dispose();
                    break;
                // Cancel option - do nothing, keep window open
            }
        } else {
            dispose();
        }
    }

    private void layoutComponents() {
        getContentPane().add(createHeaderPanel(), BorderLayout.NORTH);
        getContentPane().add(createMainPanel(), BorderLayout.CENTER);
        getContentPane().add(createFooterPanel(), BorderLayout.SOUTH);
        getContentPane().add(createSavedProductsPanel(), BorderLayout.EAST);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(10, 10));
        header.setBackground(CREAM);
        header.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectorPanel.setBackground(CREAM);
        selectorPanel.add(new JLabel("Recipe: "));
        selectorPanel.add(recipeSelector);
        
        header.add(selectorPanel, BorderLayout.NORTH);
        header.add(productNameLabel, BorderLayout.CENTER);
        header.add(stockWarningLabel, BorderLayout.SOUTH);
        
        return header;
    }

    private JPanel createMainPanel() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBackground(CREAM);
        main.setBorder(new EmptyBorder(0, 10, 0, 10));
        
        main.add(createImagePanel(), BorderLayout.WEST);
        main.add(createTablePanel(), BorderLayout.CENTER);
        
        return main;
    }

    private JPanel createImagePanel() {
        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.Y_AXIS));
        imagePanel.setBackground(CREAM);
        imagePanel.setPreferredSize(IMAGE_PANEL_SIZE);
        imagePanel.setBorder(BorderFactory.createTitledBorder("Recipe Image"));

        imagePanel.add(Box.createVerticalGlue());
        imagePanel.add(imageLabel);
        imagePanel.add(Box.createVerticalStrut(10));
        
        JLabel recipeInfo = createRecipeInfoLabel();
        imagePanel.add(recipeInfo);
        imagePanel.add(Box.createVerticalStrut(10));
        imagePanel.add(preparationProgress);
        imagePanel.add(Box.createVerticalGlue());

        return imagePanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(0, 10));
        tablePanel.setBackground(CREAM);

        JLabel tableHeader = createTableHeader();
        tablePanel.add(tableHeader, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(ingredientTable);
        scrollPane.setPreferredSize(TABLE_SIZE);
        scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Add ingredient controls
        JPanel controlsPanel = createIngredientControlsPanel();
        tablePanel.add(controlsPanel, BorderLayout.SOUTH);

        return tablePanel;
    }

    private JPanel createIngredientControlsPanel() {
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.setBackground(CREAM);
        
        JButton addIngredientBtn = new JButton("Add Ingredient");
        addIngredientBtn.addActionListener(e -> addCustomIngredient());
        
        JButton removeIngredientBtn = new JButton("Remove Selected");
        removeIngredientBtn.addActionListener(e -> removeSelectedIngredient());
        
        controls.add(addIngredientBtn);
        controls.add(removeIngredientBtn);
        
        return controls;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout(10, 10));
        footer.setBackground(CREAM);
        footer.setBorder(new EmptyBorder(10, 10, 10, 10));

        footer.add(createCostDisplayPanel(), BorderLayout.CENTER);
        footer.add(createActionButtonsPanel(), BorderLayout.SOUTH);

        return footer;
    }

    private JPanel createCostDisplayPanel() {
        JPanel costPanel = new JPanel();
        costPanel.setBackground(CREAM);
        costPanel.setLayout(new BoxLayout(costPanel, BoxLayout.Y_AXIS));
        costPanel.setBorder(BorderFactory.createTitledBorder("Cost Analysis"));

        productCostLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        profitLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        costPanel.add(productCostLabel);
        costPanel.add(Box.createVerticalStrut(5));
        costPanel.add(profitLabel);

        return costPanel;
    }

    private JPanel createActionButtonsPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 3, 5, 5));
        buttonPanel.setBackground(CREAM);

        ButtonConfig[] configs = {
            new ButtonConfig("Calculate & Process", new Color(34, 139, 34), this::processRecipe),
            new ButtonConfig("Clear All", new Color(255, 140, 0), this::clearAllAmounts),
            new ButtonConfig("Save Recipe", new Color(70, 130, 180), this::saveCurrentRecipe),
            new ButtonConfig("Quick Fill", new Color(147, 112, 219), this::quickFillStandardRecipe),
            new ButtonConfig("Products Gallery", new Color(191, 144, 0), this::openProductsGallery),
            new ButtonConfig("Export Recipe", new Color(60, 179, 113), this::exportRecipe)
        };

        for (ButtonConfig config : configs) {
            buttonPanel.add(createStyledButton(config));
        }

        return buttonPanel;
    }

    private JButton createStyledButton(ButtonConfig config) {
        JButton button = new JButton(config.text);
        button.setBackground(config.color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 12, 8, 12));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 35));

        // Enhanced hover effects
        button.addMouseListener(new MouseAdapter() {
            private final Color originalColor = config.color;
            
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(originalColor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalColor);
            }
        });

        button.addActionListener(e -> {
            try {
                config.action.run();
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error executing button action: " + config.text, ex);
                showErrorDialog("Action Error", "Failed to execute " + config.text + ": " + ex.getMessage());
            }
        });
        
        return button;
    }

    private JPanel createSavedProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(280, FRAME_SIZE.height));
        panel.setBackground(CREAM);
        panel.setBorder(BorderFactory.createTitledBorder("Saved Recipes"));
        
        String[] columns = {"Image", "Name", "Cost", "Status"};
        savedProductsModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return switch (column) {
                    case 0 -> ImageIcon.class;
                    case 2 -> Double.class;
                    default -> String.class;
                };
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        savedProductsTable = new JTable(savedProductsModel);
        savedProductsTable.setRowHeight(50);
        savedProductsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        savedProductsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        savedProductsTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        savedProductsTable.getColumnModel().getColumn(3).setPreferredWidth(60);
        
        savedProductsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = savedProductsTable.getSelectedRow();
                    if (row >= 0) {
                        viewSavedRecipeDetails(row);
                    }
                }
            }
        });
        
        panel.add(new JScrollPane(savedProductsTable), BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        buttonPanel.setBackground(CREAM);
        
        JButton[] buttons = {
            new JButton("Load"),
            new JButton("Delete"),
            new JButton("Duplicate"),
            new JButton("Export All")
        };
        
        buttons[0].addActionListener(e -> loadSavedRecipe());
        buttons[1].addActionListener(e -> deleteSavedRecipe());
        buttons[2].addActionListener(e -> duplicateSavedRecipe());
        buttons[3].addActionListener(e -> exportAllRecipes());
        
        for (JButton btn : buttons) {
            buttonPanel.add(btn);
        }
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    // Component creation methods
    private DefaultTableModel createTableModel() {
        String[] columnNames = {"Ingredient", "Amount", "Unit", "Cost/Unit", "Total Cost", "Stock", "Status"};
        return new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Only amount column is editable
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 1, 3, 4, 5 -> Double.class; // Numeric columns
                    default -> String.class;
                };
            }
        };
    }

    private JTable createTable() {
        JTable table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(35);
        table.setDefaultRenderer(Object.class, new EnhancedTableCellRenderer());
        
        // Set column widths
        TableColumn[] columns = {
            table.getColumnModel().getColumn(0), // Ingredient
            table.getColumnModel().getColumn(1), // Amount
            table.getColumnModel().getColumn(2), // Unit
            table.getColumnModel().getColumn(3), // Cost/Unit
            table.getColumnModel().getColumn(4), // Total Cost
            table.getColumnModel().getColumn(5), // Stock
            table.getColumnModel().getColumn(6)  // Status
        };
        
        int[] widths = {120, 80, 60, 80, 90, 80, 80};
        for (int i = 0; i < columns.length; i++) {
            columns[i].setPreferredWidth(widths[i]);
        }
        
        return table;
    }

    private JComboBox<String> createRecipeSelector() {
        JComboBox<String> selector = new JComboBox<>(recipeManager.getRecipeNames());
        selector.setFont(new Font("Arial", Font.PLAIN, 14));
        selector.setPreferredSize(new Dimension(200, 25));
        return selector;
    }

    private JLabel createProductNameLabel() {
        JLabel label = new JLabel(currentRecipe, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 28));
        label.setForeground(COFFEE_BROWN);
        return label;
    }

    private JLabel createCostLabel() {
        JLabel label = new JLabel("Product Cost: " + CURRENCY_FORMAT.format(0.0));
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setForeground(COFFEE_BROWN);
        return label;
    }

    private JLabel createProfitLabel() {
        JLabel label = new JLabel("Suggested Price: ₱0.00 (30% margin)");
        label.setFont(new Font("Arial", Font.ITALIC, 14));
        label.setForeground(PROFIT_COLOR);
        return label;
    }

    private JLabel createStockWarningLabel() {
        JLabel label = new JLabel(" ");
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JProgressBar createProgressBar() {
        JProgressBar progress = new JProgressBar(0, 100);
        progress.setStringPainted(true);
        progress.setString("Ready");
        progress.setPreferredSize(new Dimension(120, 20));
        return progress;
    }

    private JLabel createImageLabel() {
        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createDashedBorder(COFFEE_BROWN, 2, 5, 5, true));
        label.setBackground(Color.WHITE);
        label.setOpaque(true);
        label.setPreferredSize(new Dimension(120, 120));

        // Enhanced drag and drop
        new DropTarget(label, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                handleImageDrop(dtde, label);
            }
        });

        return label;
    }

    private void handleImageDrop(DropTargetDropEvent dtde, JLabel label) {
        try {
            dtde.acceptDrop(DnDConstants.ACTION_COPY);
            List<File> droppedFiles = (List<File>) dtde.getTransferable()
                    .getTransferData(DataFlavor.javaFileListFlavor);
            
            if (!droppedFiles.isEmpty()) {
                File file = droppedFiles.get(0);
                String fileName = file.getName().toLowerCase();
                
                if (isValidImageFile(fileName)) {
                    loadImageFromFile(file, label);
                    hasUnsavedChanges = true;
                } else {
                    showWarningDialog("Invalid File", "Please drop a valid image file (JPEG, JPG, PNG, GIF)");
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error handling image drop", ex);
            showErrorDialog("Image Error", "Failed to load image: " + ex.getMessage());
        }
    }

    private boolean isValidImageFile(String fileName) {
        return fileName.endsWith(".jpeg") || fileName.endsWith(".jpg") || 
               fileName.endsWith(".png") || fileName.endsWith(".gif");
    }

    private void loadImageFromFile(File file, JLabel label) {
        try {
            ImageIcon image = new ImageIcon(file.getAbsolutePath());
            if (image.getImage() != null && image.getIconWidth() > 0) {
                Image scaledImage = image.getImage().getScaledInstance(110, 110, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(scaledImage));
                label.setText("");
                LOGGER.info("Image loaded successfully: " + file.getName());
            } else {
                throw new RuntimeException("Invalid or corrupted image file");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load image from file: " + file.getName(), e);
            throw e;
        }
    }

    private JLabel createRecipeInfoLabel() {
        JLabel info = new JLabel("<html><center><small>Premium Recipe<br/>Est. 2-3 mins<br/>Difficulty: Medium</small></center></html>");
        info.setFont(new Font("Arial", Font.PLAIN, 10));
        info.setForeground(COFFEE_BROWN);
        info.setAlignmentX(Component.CENTER_ALIGNMENT);
        return info;
    }

    private JLabel createTableHeader() {
        JLabel header = new JLabel("INGREDIENTS & COST BREAKDOWN", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 16));
        header.setForeground(COFFEE_BROWN);
        return header;
    }

    private void setupEventHandlers() {
        recipeSelector.addActionListener(e -> {
            String selected = (String) recipeSelector.getSelectedItem();
            if (selected != null && !selected.equals(currentRecipe)) {
                loadRecipe(selected);
            }
        });
        
        tableModel.addTableModelListener(e -> {
            if (e.getColumn() == 1) { // Amount column changed
                updateRowCost(e.getFirstRow());
                hasUnsavedChanges = true;
            }
        });

        // Enhanced table selection listener
        ingredientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateStockWarning();
            }
        });
    }

    private void setupKeyBindings() {
        // Ctrl+S for save
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "save");
        getRootPane().getActionMap().put("save", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCurrentRecipe();
            }
        });

        // Ctrl+N for new/clear
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), "clear");
        getRootPane().getActionMap().put("clear", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearAllAmounts();
            }
        });
    }

    // Enhanced business logic methods
    private void processRecipe() {
        if (!validateRecipeForProcessing()) {
            return;
        }

        // Start processing with progress indication
        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                processRecipeIngredients();
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                if (!chunks.isEmpty()) {
                    preparationProgress.setValue(chunks.get(chunks.size() - 1));
                }
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    preparationProgress.setString("Completed");
                    preparationProgress.setValue(100);
                    showSuccessDialog("Recipe processed successfully!\nInventory has been updated.");
                    
                    // Reset progress after delay
                    Timer timer = new Timer(2000, e -> {
                        preparationProgress.setValue(0);
                        preparationProgress.setString("Ready");
                    });
                    timer.setRepeats(false);
                    timer.start();
                    
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Recipe processing failed", e);
                    preparationProgress.setString("Failed");
                    showErrorDialog("Processing Error", "Failed to process recipe: " + e.getMessage());
                }
            }
        };

        worker.execute();
    }

    private boolean validateRecipeForProcessing() {
        if (tableModel.getRowCount() == 0) {
            showWarningDialog("No Recipe", "Please add ingredients to the recipe before processing.");
            return false;
        }

        // Check if any ingredients have amounts > 0
        boolean hasIngredients = false;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            double amount = parseTableValue(tableModel.getValueAt(i, 1));
            if (amount > 0) {
                hasIngredients = true;
                break;
            }
        }

        if (!hasIngredients) {
            showWarningDialog("No Amounts", "Please enter amounts for ingredients before processing.");
            return false;
        }

        return checkIngredientAvailability();
    }

    private void processRecipeIngredients() throws Exception {
        int totalIngredients = tableModel.getRowCount();
        int processed = 0;

        for (int i = 0; i < totalIngredients; i++) {
            String ingredientName = (String) tableModel.getValueAt(i, 0);
            double requiredAmount = parseTableValue(tableModel.getValueAt(i, 1));

            if (requiredAmount > 0) {
                CoffeeProduct product = findProduct(ingredientName);
                if (product != null) {
                    double currentStock = product.getStock();
                    if (currentStock >= requiredAmount) {
                        // Deduct stock
                        product.setStock((int) (currentStock - requiredAmount));
                        LOGGER.info("Deducted " + requiredAmount + " from " + ingredientName);
                        
                        // Update table status
                        SwingUtilities.invokeLater(() -> {
                            tableModel.setValueAt("Used", i, 6);
                            tableModel.setValueAt(currentStock - requiredAmount, i, 5);
                        });
                    } else {
                        throw new RuntimeException("Insufficient stock for " + ingredientName + 
                                                 ". Required: " + requiredAmount + ", Available: " + currentStock);
                    }
                }
            }

            processed++;
            final int progress = (processed * 100) / totalIngredients;
            SwingUtilities.invokeLater(() -> preparationProgress.setValue(progress));

            // Simulate processing time
            Thread.sleep(200);
        }
    }

    private void saveCurrentRecipe() {
        try {
            if (!validateRecipeForSaving()) {
                return;
            }

            double totalCost = calculateCurrentTotalCost();
            Map<String, Double> ingredientsMap = extractIngredientsMap();

            ImageIcon image = (imageLabel.getIcon() instanceof ImageIcon) ? 
                            (ImageIcon) imageLabel.getIcon() : null;

            SavedProduct product = new SavedProduct(currentRecipe, totalCost, image, ingredientsMap);
            savedProducts.add(product);

            refreshSavedProductsUI();
            notifyOurProductWindows();
            hasUnsavedChanges = false;

            showSuccessDialog("Recipe '" + currentRecipe + "' saved successfully!\n" +
                            "Total Cost: " + CURRENCY_FORMAT.format(totalCost) + "\n" +
                            "Ingredients: " + ingredientsMap.size());

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to save recipe", e);
            showErrorDialog("Save Error", "Failed to save recipe: " + e.getMessage());
        }
    }

    private boolean validateRecipeForSaving() {
        if (currentRecipe == null || currentRecipe.trim().isEmpty()) {
            showWarningDialog("No Recipe Name", "Please select or enter a recipe name.");
            return false;
        }

        if (tableModel.getRowCount() == 0) {
            showWarningDialog("No Ingredients", "Please add ingredients before saving.");
            return false;
        }

        // Check if recipe already exists
        boolean exists = savedProducts.stream()
                .anyMatch(p -> p.name.equalsIgnoreCase(currentRecipe));
        
        if (exists) {
            int result = JOptionPane.showConfirmDialog(this,
                "A recipe with this name already exists. Overwrite?",
                "Recipe Exists", JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                savedProducts.removeIf(p -> p.name.equalsIgnoreCase(currentRecipe));
                return true;
            }
            return false;
        }

        return true;
    }

    private double calculateCurrentTotalCost() {
        double totalCost = 0.0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object value = tableModel.getValueAt(i, 4);
            if (value instanceof Number) {
                totalCost += ((Number) value).doubleValue();
            }
        }
        return totalCost;
    }

    private Map<String, Double> extractIngredientsMap() {
        Map<String, Double> ingredientsMap = new HashMap<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String ingredientName = (String) tableModel.getValueAt(i, 0);
            double amount = parseTableValue(tableModel.getValueAt(i, 1));
            if (amount > 0) {
                ingredientsMap.put(ingredientName, amount);
            }
        }
        return ingredientsMap;
    }

    private void openProductsGallery() {
        try {
            LOGGER.info("Opening Products Gallery");
            OurProduct productWindow = new OurProduct(inventoryManager, getSavedProducts());
            registerOurProductWindow(productWindow);
            productWindow.setVisible(true);
            
            productWindow.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    unregisterOurProductWindow(productWindow);
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to open Products Gallery", e);
            showErrorDialog("Gallery Error", "Failed to open Products Gallery: " + e.getMessage());
        }
    }

    private void exportRecipe() {
        if (tableModel.getRowCount() == 0) {
            showWarningDialog("No Recipe", "Please load a recipe before exporting.");
            return;
        }

        try {
            StringBuilder export = new StringBuilder();
            export.append("Recipe: ").append(currentRecipe).append("\n");
            export.append("Total Cost: ").append(CURRENCY_FORMAT.format(calculateCurrentTotalCost())).append("\n\n");
            export.append("Ingredients:\n");
            export.append("============\n");

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String name = (String) tableModel.getValueAt(i, 0);
                double amount = parseTableValue(tableModel.getValueAt(i, 1));
                String unit = (String) tableModel.getValueAt(i, 2);
                double cost = parseTableValue(tableModel.getValueAt(i, 4));

                export.append(String.format("• %s: %s %s (Cost: %s)\n",
                    name, QUANTITY_FORMAT.format(amount), unit, CURRENCY_FORMAT.format(cost)));
            }

            // Show export dialog
            JTextArea textArea = new JTextArea(export.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 400));
            
            JOptionPane.showMessageDialog(this, scrollPane, 
                "Recipe Export - " + currentRecipe, JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Export failed", e);
            showErrorDialog("Export Error", "Failed to export recipe: " + e.getMessage());
        }
    }

    private void deleteSavedRecipe() {
        int selectedRow = savedProductsTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < savedProducts.size()) {
            SavedProduct product = savedProducts.get(selectedRow);
            int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete '" + product.name + "'?",
                "Delete Recipe", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            
            if (result == JOptionPane.YES_OPTION) {
                savedProducts.remove(selectedRow);
                refreshSavedProductsUI();
                notifyOurProductWindows();
                showSuccessDialog("Recipe '" + product.name + "' deleted successfully.");
            }
        } else {
            showWarningDialog("No Selection", "Please select a recipe to delete.");
        }
    }

    private void duplicateSavedRecipe() {
        int selectedRow = savedProductsTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < savedProducts.size()) {
            SavedProduct original = savedProducts.get(selectedRow);
            String newName = JOptionPane.showInputDialog(this, 
                "Enter name for the duplicate recipe:", 
                original.name + " (Copy)");
            
            if (newName != null && !newName.trim().isEmpty()) {
                SavedProduct duplicate = new SavedProduct(
                    newName.trim(), 
                    original.cost, 
                    original.image, 
                    new HashMap<>(original.Ingredients)
                );
                savedProducts.add(duplicate);
                refreshSavedProductsUI();
                notifyOurProductWindows();
                showSuccessDialog("Recipe duplicated as '" + newName + "'");
            }
        } else {
            showWarningDialog("No Selection", "Please select a recipe to duplicate.");
        }
    }

    private void exportAllRecipes() {
        if (savedProducts.isEmpty()) {
            showWarningDialog("No Recipes", "No saved recipes to export.");
            return;
        }

        try {
            StringBuilder export = new StringBuilder();
            export.append("ALL SAVED RECIPES\n");
            export.append("=================\n\n");

            for (int i = 0; i < savedProducts.size(); i++) {
                SavedProduct product = savedProducts.get(i);
                export.append(String.format("%d. %s\n", i + 1, product.name));
                export.append("   Cost: ").append(CURRENCY_FORMAT.format(product.cost)).append("\n");
                export.append("   Ingredients: ");
                
                if (product.Ingredients.isEmpty()) {
                    export.append("None specified\n");
                } else {
                    export.append("\n");
                    for (Map.Entry<String, Double> entry : product.Ingredients.entrySet()) {
                        export.append(String.format("   • %s: %s\n", 
                            entry.getKey(), QUANTITY_FORMAT.format(entry.getValue())));
                    }
                }
                export.append("\n");
            }

            JTextArea textArea = new JTextArea(export.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600, 500));
            
            JOptionPane.showMessageDialog(this, scrollPane, 
                "All Recipes Export", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Export all failed", e);
            showErrorDialog("Export Error", "Failed to export recipes: " + e.getMessage());
        }
    }

    private void loadSavedRecipe() {
        int selectedRow = savedProductsTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < savedProducts.size()) {
            SavedProduct product = savedProducts.get(selectedRow);
            
            if (hasUnsavedChanges) {
                int result = JOptionPane.showConfirmDialog(this,
                    "You have unsaved changes. Load recipe anyway?",
                    "Unsaved Changes", JOptionPane.YES_NO_OPTION);
                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            loadRecipeFromSavedProduct(product);
            hasUnsavedChanges = false;
        } else {
            showWarningDialog("No Selection", "Please select a recipe to load.");
        }
    }

    private void viewSavedRecipeDetails(int row) {
        if (row >= 0 && row < savedProducts.size()) {
            SavedProduct product = savedProducts.get(row);
            StringBuilder details = new StringBuilder();
            details.append("Recipe: ").append(product.name).append("\n");
            details.append("Total Cost: ").append(CURRENCY_FORMAT.format(product.cost)).append("\n");
            
            double suggestedPrice = product.cost * (1 + DEFAULT_PROFIT_MARGIN);
            details.append("Suggested Price: ").append(CURRENCY_FORMAT.format(suggestedPrice)).append("\n\n");
            
            details.append("Ingredients:\n");
            details.append("============\n");
            
            if (product.Ingredients.isEmpty()) {
                details.append("No ingredients specified.\n");
            } else {
                for (Map.Entry<String, Double> entry : product.Ingredients.entrySet()) {
                    CoffeeProduct coffeeProduct = findProduct(entry.getKey());
                    String unit = coffeeProduct != null ? getPhysicalUnit(coffeeProduct.getUnit()) : "units";
                    
                    details.append(String.format("• %s: %s %s\n",
                        entry.getKey(),
                        QUANTITY_FORMAT.format(entry.getValue()),
                        unit));
                }
            }
            
            JTextArea textArea = new JTextArea(details.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 300));
            
            JOptionPane.showMessageDialog(this, scrollPane, 
                "Recipe Details - " + product.name, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void loadRecipeFromSavedProduct(SavedProduct product) {
        currentRecipe = product.name;
        productNameLabel.setText(product.name);
        
        // Update recipe selector if the recipe exists in the dropdown
        for (int i = 0; i < recipeSelector.getItemCount(); i++) {
            if (recipeSelector.getItemAt(i).equals(product.name)) {
                recipeSelector.setSelectedIndex(i);
                break;
            }
        }
        
        tableModel.setRowCount(0);
        
        for (Map.Entry<String, Double> entry : product.Ingredients.entrySet()) {
            String ingredientName = entry.getKey();
            double amount = entry.getValue();
            
            CoffeeProduct coffeeProduct = findProduct(ingredientName);
            String unit = coffeeProduct != null ? getPhysicalUnit(coffeeProduct.getUnit()) : "ml/g";
            double costPerUnit = coffeeProduct != null ? coffeeProduct.getCostPerUnit() : 0.0;
            double totalCost = amount * costPerUnit;
            double stock = coffeeProduct != null ? coffeeProduct.getStock() : 0.0;
            String status = (stock >= amount) ? "Available" : "Low Stock";
            
            tableModel.addRow(new Object[]{
                ingredientName, amount, unit, costPerUnit, totalCost, stock, status
            });
        }
        
        if (product.image != null) {
            imageLabel.setIcon(product.image);
            imageLabel.setText("");
        } else {
            imageLabel.setIcon(null);
            imageLabel.setText("<html><center>☕<br/><small>No Image</small></center></html>");
        }
        
        calculateTotalCost();
        updateStockWarning();
    }

    private void refreshSavedProductsUI() {
        savedProductsModel.setRowCount(0);
        for (SavedProduct p : savedProducts) {
            ImageIcon icon = null;
            if (p.image != null) {
                Image scaled = p.image.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                icon = new ImageIcon(scaled);
            }
            
            // Determine status based on ingredient availability
            String status = "Available";
            if (!p.Ingredients.isEmpty()) {
                boolean hasLowStock = false;
                for (Map.Entry<String, Double> entry : p.Ingredients.entrySet()) {
                    CoffeeProduct product = findProduct(entry.getKey());
                    if (product == null || product.getStock() < entry.getValue()) {
                        hasLowStock = true;
                        break;
                    }
                }
                status = hasLowStock ? "Low Stock" : "Available";
            }
            
            savedProductsModel.addRow(new Object[] {icon, p.name, p.cost, status});
        }
    }

    private void addCustomIngredient() {
        String[] availableIngredients = products.stream()
                .map(CoffeeProduct::getName)
                .toArray(String[]::new);

        String selected = (String) JOptionPane.showInputDialog(this,
            "Select an ingredient to add:",
            "Add Ingredient",
            JOptionPane.QUESTION_MESSAGE,
            null,
            availableIngredients,
            availableIngredients.length > 0 ? availableIngredients[0] : null);

        if (selected != null) {
            // Check if ingredient already exists
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (selected.equals(tableModel.getValueAt(i, 0))) {
                    showWarningDialog("Duplicate Ingredient", 
                        "This ingredient is already in the recipe.");
                    return;
                }
            }

            CoffeeProduct product = findProduct(selected);
            if (product != null) {
                String unit = getPhysicalUnit(product.getUnit());
                double costPerUnit = product.getCostPerUnit();
                double stock = product.getStock();
                
                tableModel.addRow(new Object[]{
                    selected, 0.0, unit, costPerUnit, 0.0, stock, "Available"
                });
                
                hasUnsavedChanges = true;
            }
        }
    }

    private void removeSelectedIngredient() {
        int selectedRow = ingredientTable.getSelectedRow();
        if (selectedRow >= 0) {
            String ingredientName = (String) tableModel.getValueAt(selectedRow, 0);
            int result = JOptionPane.showConfirmDialog(this,
                "Remove '" + ingredientName + "' from the recipe?",
                "Remove Ingredient", JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                tableModel.removeRow(selectedRow);
                calculateTotalCost();
                hasUnsavedChanges = true;
            }
        } else {
            showWarningDialog("No Selection", "Please select an ingredient to remove.");
        }
    }

    private void loadDefaultRecipe() {
        loadRecipe(currentRecipe);
    }

    private void loadRecipe(String recipeName) {
        if (recipeName == null) return;
        
        if (hasUnsavedChanges) {
            int result = JOptionPane.showConfirmDialog(this,
                "You have unsaved changes. Load new recipe anyway?",
                "Unsaved Changes", JOptionPane.YES_NO_OPTION);
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        currentRecipe = recipeName;
        productNameLabel.setText(recipeName);
        
        tableModel.setRowCount(0);
        
        Recipe recipe = recipeManager.getRecipe(recipeName);
        if (recipe != null) {
            for (RecipeIngredient ingredient : recipe.getIngredients()) {
                addIngredientToTable(ingredient);
            }
        }
        
        updateImage(recipeName);
        calculateTotalCost();
        updateStockWarning();
        hasUnsavedChanges = false;
    }

    private void addIngredientToTable(RecipeIngredient ingredient) {
        CoffeeProduct product = findProduct(ingredient.getName());
        String unit = product != null ? getPhysicalUnit(product.getUnit()) : "ml/g";
        double costPerUnit = product != null ? product.getCostPerUnit() : 0.0;
        double totalCost = ingredient.getAmount() * costPerUnit;
        double stock = product != null ? product.getStock() : 0.0;
        String status = (product != null && stock >= ingredient.getAmount()) ? "Available" : "Low Stock";
        
        tableModel.addRow(new Object[]{
            ingredient.getName(),
            ingredient.getAmount(),
            unit,
            costPerUnit,
            totalCost,
            stock,
            status
        });
    }

    private void updateRowCost(int row) {
        if (row < 0 || row >= tableModel.getRowCount()) return;
        
        try {
            double amount = parseTableValue(tableModel.getValueAt(row, 1));
            String ingredientName = (String) tableModel.getValueAt(row, 0);
            CoffeeProduct product = findProduct(ingredientName);
            
            double costPerUnit = product != null ? product.getCostPerUnit() : 0.0;
            double totalCost = amount * costPerUnit;
            double stock = product != null ? product.getStock() : 0.0;
            String status = (stock >= amount) ? "Available" : "Low Stock";
            
            tableModel.setValueAt(costPerUnit, row, 3);
            tableModel.setValueAt(totalCost, row, 4);
            tableModel.setValueAt(stock, row, 5);
            tableModel.setValueAt(status, row, 6);
            
            calculateTotalCost();
            updateStockWarning();
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid amount entered for row " + row, e);
            showWarningDialog("Input Error", "Please enter a valid number for the amount.");
        }
    }

    private void calculateTotalCost() {
        double totalCost = 0.0;
        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object value = tableModel.getValueAt(i, 4);
            if (value instanceof Number) {
                totalCost += ((Number) value).doubleValue();
            }
        }
        
        updateCostDisplay(totalCost);
    }

    private void updateCostDisplay(double totalCost) {
        productCostLabel.setText("Product Cost: " + CURRENCY_FORMAT.format(totalCost));
        
        double suggestedPrice = totalCost * (1 + DEFAULT_PROFIT_MARGIN);
        profitLabel.setText(String.format("Suggested Price: %s (%.0f%% margin)",
            CURRENCY_FORMAT.format(suggestedPrice), DEFAULT_PROFIT_MARGIN * 100));
    }

    private void updateStockWarning() {
        boolean hasLowStock = false;
        int lowStockCount = 0;
        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String status = (String) tableModel.getValueAt(i, 6);
            if ("Low Stock".equals(status)) {
                hasLowStock = true;
                lowStockCount++;
            }
        }
        
        if (hasLowStock) {
            stockWarningLabel.setText("⚠️ " + lowStockCount + " ingredient(s) have low stock!");
            stockWarningLabel.setForeground(WARNING_COLOR);
        } else if (tableModel.getRowCount() > 0) {
            stockWarningLabel.setText("✅ All ingredients available");
            stockWarningLabel.setForeground(PROFIT_COLOR);
        } else {
            stockWarningLabel.setText(" ");
        }
    }

    private void clearAllAmounts() {
        if (tableModel.getRowCount() == 0) {
            showWarningDialog("No Recipe", "No recipe loaded to clear.");
            return;
        }

        int result = JOptionPane.showConfirmDialog(this,
            "Clear all ingredient amounts?", "Clear Recipe", 
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                tableModel.setValueAt(0.0, i, 1);
                tableModel.setValueAt(0.0, i, 4);
                tableModel.setValueAt("Available", i, 6);
            }
            calculateTotalCost();
            updateStockWarning();
            hasUnsavedChanges = true;
        }
    }

    private void quickFillStandardRecipe() {
        String selectedRecipe = (String) recipeSelector.getSelectedItem();
        if (selectedRecipe != null) {
            loadRecipe(selectedRecipe);
        }
    }

    // Utility methods
    private CoffeeProduct findProduct(String name) {
        return products.stream()
                .filter(product -> product.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private String getPhysicalUnit(String unit) {
        if (unit == null || unit.isBlank()) return "ml/g";
        return switch (unit.toLowerCase()) {
            case "l", "liter", "liters" -> "ml";
            case "kg", "kilogram", "kilograms" -> "g";
            case "oz", "ounce", "ounces" -> "g";
            case "lb", "pound", "pounds" -> "g";
            default -> unit;
        };
    }

    private double parseTableValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String str && !str.isBlank()) {
            try {
                return Double.parseDouble(str.replace(",", "").trim());
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    private void updateImage(String recipeName) {
        try {
            ImageIcon image = loadImage("/images/" + recipeName.toLowerCase().replace(" ", "_") + ".png");
            if (image != null) {
                Image scaledImage = image.getImage().getScaledInstance(110, 110, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaledImage));
                imageLabel.setText("");
            } else {
                setDefaultImagePlaceholder();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error loading image for recipe: " + recipeName, e);
            setDefaultImagePlaceholder();
        }
    }

    private void setDefaultImagePlaceholder() {
        imageLabel.setIcon(null);
        imageLabel.setText("<html><center>☕<br/><small>Drop image here<br/>or no image</small></center></html>");
    }

    private ImageIcon loadImage(String path) {
        try {
            java.net.URL imgURL = getClass().getResource(path);
            if (imgURL != null) {
                return new ImageIcon(imgURL);
            } else {
                LOGGER.fine("Image resource not found: " + path);
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error loading image resource: " + path, e);
            return null;
        }
    }

    private boolean checkIngredientAvailability() {
        List<String> unavailableIngredients = new ArrayList<>();
        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String ingredientName = (String) tableModel.getValueAt(i, 0);
            double amount = parseTableValue(tableModel.getValueAt(i, 1));
            
            if (amount > 0) {
                CoffeeProduct product = findProduct(ingredientName);
                if (product == null) {
                    unavailableIngredients.add(ingredientName + " (not found)");
                } else if (product.getStock() < amount) {
                    unavailableIngredients.add(ingredientName + 
                        " (need: " + QUANTITY_FORMAT.format(amount) + 
                        ", have: " + QUANTITY_FORMAT.format(product.getStock()) + ")");
                }
            }
        }
        
        if (!unavailableIngredients.isEmpty()) {
            StringBuilder message = new StringBuilder("Cannot process recipe. Insufficient stock:\n\n");
            for (String item : unavailableIngredients) {
                message.append("• ").append(item).append("\n");
            }
            
            showErrorDialog("Stock Shortage", message.toString());
            return false;
        }
        
        return true;
    }

    // Dialog helper methods
    private void showSuccessDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarningDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
    }

    private void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    // Enhanced inner classes
    private static class ButtonConfig {
        final String text;
        final Color color;
        final Runnable action;

        ButtonConfig(String text, Color color, Runnable action) {
            this.text = text;
            this.color = color;
            this.action = action;
        }
    }

    public static class SavedProduct {
        public String name;
        public double cost;
        public ImageIcon image;
        public Map<String, Double> Ingredients;

        public SavedProduct(String name, double cost, ImageIcon image, Map<String, Double> ingredients) {
            this.name = name != null ? name : "Unnamed Recipe";
            this.cost = cost;
            this.image = image;
            this.Ingredients = ingredients != null ? new HashMap<>(ingredients) : new HashMap<>();
        }

        @Override
        public String toString() {
            return name + " (" + CURRENCY_FORMAT.format(cost) + ")";
        }
    }

    private class EnhancedTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                // Alternate row colors
                component.setBackground(row % 2 == 0 ? ALTERNATE_ROW_COLOR : Color.WHITE);
                
                // Highlight editable column
                if (column == 1) {
                    component.setBackground(EDITABLE_CELL_COLOR);
                }
                
                // Color code status column
                if (column == 6 && value instanceof String) {
                    String status = (String) value;
                    switch (status) {
                        case "Low Stock" -> component.setBackground(new Color(255, 240, 240));
                        case "Available" -> component.setBackground(new Color(240, 255, 240));
                        case "Used" -> component.setBackground(new Color(240, 240, 255));
                    }
                }
            }

            // Text alignment
            setHorizontalAlignment(switch (column) {
                case 1, 3, 4, 5 -> CENTER; // Numeric columns
                case 6 -> CENTER; // Status column
                default -> LEFT;
            });

            // Format numeric values
            if (value instanceof Number) {
                switch (column) {
                    case 3, 4 -> setText(CURRENCY_FORMAT.format(value)); // Cost columns
                    case 1, 5 -> setText(QUANTITY_FORMAT.format(value)); // Quantity columns
                }
            }

            return component;
        }
    }

    private static class Recipe {
        private final String name;
        private final RecipeIngredient[] ingredients;

        Recipe(String name, RecipeIngredient[] ingredients) {
            this.name = name;
            this.ingredients = ingredients != null ? ingredients : new RecipeIngredient[0];
        }

        public String getName() { return name; }
        public RecipeIngredient[] getIngredients() { return ingredients; }
    }

    private static class RecipeIngredient {
        private final String name;
        private final double amount;

        RecipeIngredient(String name, double amount) {
            this.name = name;
            this.amount = Math.max(0, amount); // Ensure non-negative
        }

        public String getName() { return name; }
        public double getAmount() { return amount; }
    }

    private static class RecipeManager {
        private final Map<String, Recipe> recipes;

        RecipeManager() {
            this.recipes = new HashMap<>();
            initializeRecipes();
        }

        private void initializeRecipes() {
            recipes.put("Caramel Macchiato", new Recipe("Caramel Macchiato", new RecipeIngredient[]{
                new RecipeIngredient("Milk", 200),
                new RecipeIngredient("Espresso", 30),
                new RecipeIngredient("Caramel Syrup", 15),
                new RecipeIngredient("Sugar", 5),
                new RecipeIngredient("Whipped Cream", 20)
            }));
            
            recipes.put("Caffè Latte", new Recipe("Caffè Latte", new RecipeIngredient[]{
                new RecipeIngredient("Milk", 220),
                new RecipeIngredient("Espresso", 30),
                new RecipeIngredient("Sugar", 5)
            }));
            
            recipes.put("Cappuccino", new Recipe("Cappuccino", new RecipeIngredient[]{
                new RecipeIngredient("Milk", 150),
                new RecipeIngredient("Espresso", 30),
                new RecipeIngredient("Sugar", 5),
                new RecipeIngredient("Foamed Milk", 50)
            }));
            
            recipes.put("Americano", new Recipe("Americano", new RecipeIngredient[]{
                new RecipeIngredient("Espresso", 60),
                new RecipeIngredient("Hot Water", 150),
                new RecipeIngredient("Sugar", 3)
            }));
            
            recipes.put("Mocha", new Recipe("Mocha", new RecipeIngredient[]{
                new RecipeIngredient("Milk", 180),
                new RecipeIngredient("Espresso", 30),
                new RecipeIngredient("Chocolate Syrup", 20),
                new RecipeIngredient("Sugar", 5),
                new RecipeIngredient("Whipped Cream", 15)
            }));
            
            recipes.put("Frappuccino", new Recipe("Frappuccino", new RecipeIngredient[]{
                new RecipeIngredient("Cold Milk", 200),
                new RecipeIngredient("Espresso", 30),
                new RecipeIngredient("Ice", 100),
                new RecipeIngredient("Sugar", 10),
                new RecipeIngredient("Whipped Cream", 25)
            }));
            
            recipes.put("Flat White", new Recipe("Flat White", new RecipeIngredient[]{
                new RecipeIngredient("Milk", 160),
                new RecipeIngredient("Espresso", 60),
                new RecipeIngredient("Sugar", 3)
            }));
            
            recipes.put("Iced Coffee", new Recipe("Iced Coffee", new RecipeIngredient[]{
                new RecipeIngredient("Cold Brew Coffee", 200),
                new RecipeIngredient("Ice", 150),
                new RecipeIngredient("Milk", 50),
                new RecipeIngredient("Sugar", 8)
            }));
            
            recipes.put("Espresso Romano", new Recipe("Espresso Romano", new RecipeIngredient[]{
                new RecipeIngredient("Espresso", 30),
                new RecipeIngredient("Lemon Zest", 1),
                new RecipeIngredient("Sugar", 2)
            }));
            
            recipes.put("Turkish Coffee", new Recipe("Turkish Coffee", new RecipeIngredient[]{
                new RecipeIngredient("Finely Ground Coffee", 10),
                new RecipeIngredient("Water", 100),
                new RecipeIngredient("Sugar", 5)
            }));
            
            recipes.put("Vienna Coffee", new Recipe("Vienna Coffee", new RecipeIngredient[]{
                new RecipeIngredient("Strong Coffee", 150),
                new RecipeIngredient("Whipped Cream", 30),
                new RecipeIngredient("Cocoa Powder", 2),
                new RecipeIngredient("Sugar", 5)
            }));

            LOGGER.info("Initialized " + recipes.size() + " default recipes");
        }
       
        public Recipe getRecipe(String name) {
            return recipes.get(name);
        }

        public String[] getRecipeNames() {
            return recipes.keySet().toArray(new String[0]);
        }
        
        public void addCustomRecipe(Recipe recipe) {
            if (recipe != null && recipe.getName() != null) {
                recipes.put(recipe.getName(), recipe);
                LOGGER.info("Added custom recipe: " + recipe.getName());
            }
        }
        
        public boolean removeRecipe(String name) {
            Recipe removed = recipes.remove(name);
            if (removed != null) {
                LOGGER.info("Removed recipe: " + name);
                return true;
            }
            return false;
        }
        
        public int getRecipeCount() {
            return recipes.size();
        }
    }
}