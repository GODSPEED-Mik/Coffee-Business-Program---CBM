// DataPersistence.java - Missing implementation
package Micow.ProjectC.Micow_Cashier;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

/**
 * Handles data persistence for the coffee shop management system
 */
public class DataPersistence {
    private static final String INVENTORY_FILE = "coffee_inventory.json";
    private static final String RECIPES_FILE = "coffee_recipes.json";
    private static final String SETTINGS_FILE = "app_settings.json";
    private final Gson gson;

    public DataPersistence() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
    }

    // Inventory persistence
    public void saveInventory(List<CoffeeProduct> products) {
        try (FileWriter writer = new FileWriter(INVENTORY_FILE)) {
            gson.toJson(products, writer);
        } catch (IOException e) {
            System.err.println("Error saving inventory: " + e.getMessage());
        }
    }

    public List<CoffeeProduct> loadInventory() {
        try (FileReader reader = new FileReader(INVENTORY_FILE)) {
            Type listType = new TypeToken<List<CoffeeProduct>>(){}.getType();
            List<CoffeeProduct> products = gson.fromJson(reader, listType);
            return products != null ? products : new ArrayList<>();
        } catch (IOException e) {
            System.out.println("No existing inventory file found. Starting fresh.");
            return new ArrayList<>();
        }
    }

    // Recipe persistence
    public void saveRecipes(Map<String, Recipe> recipes) {
        try (FileWriter writer = new FileWriter(RECIPES_FILE)) {
            gson.toJson(recipes, writer);
        } catch (IOException e) {
            System.err.println("Error saving recipes: " + e.getMessage());
        }
    }

    public Map<String, Recipe> loadRecipes() {
        try (FileReader reader = new FileReader(RECIPES_FILE)) {
            Type mapType = new TypeToken<Map<String, Recipe>>(){}.getType();
            Map<String, Recipe> recipes = gson.fromJson(reader, mapType);
            return recipes != null ? recipes : new HashMap<>();
        } catch (IOException e) {
            System.out.println("No existing recipes file found. Loading defaults.");
            return new HashMap<>();
        }
    }

    // Settings persistence
    public void saveSettings(AppSettings settings) {
        try (FileWriter writer = new FileWriter(SETTINGS_FILE)) {
            gson.toJson(settings, writer);
        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }

    public AppSettings loadSettings() {
        try (FileReader reader = new FileReader(SETTINGS_FILE)) {
            AppSettings settings = gson.fromJson(reader, AppSettings.class);
            return settings != null ? settings : new AppSettings();
        } catch (IOException e) {
            return new AppSettings();
        }
    }

    // Custom adapter for LocalDateTime
    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            return LocalDateTime.parse(json.getAsString());
        }
    }
}

// AppSettings.java - Configuration management
class AppSettings {
    private double defaultProfitMargin = 0.30;
    private String defaultCurrency = "₱";
    private boolean darkMode = false;
    private boolean autoSave = true;
    private int lowStockThreshold = 10;
    private String backupDirectory = "./backups/";

    // Getters and setters
    public double getDefaultProfitMargin() { return defaultProfitMargin; }
    public void setDefaultProfitMargin(double defaultProfitMargin) { this.defaultProfitMargin = defaultProfitMargin; }
    
    public String getDefaultCurrency() { return defaultCurrency; }
    public void setDefaultCurrency(String defaultCurrency) { this.defaultCurrency = defaultCurrency; }
    
    public boolean isDarkMode() { return darkMode; }
    public void setDarkMode(boolean darkMode) { this.darkMode = darkMode; }
    
    public boolean isAutoSave() { return autoSave; }
    public void setAutoSave(boolean autoSave) { this.autoSave = autoSave; }
    
    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }
    
    public String getBackupDirectory() { return backupDirectory; }
    public void setBackupDirectory(String backupDirectory) { this.backupDirectory = backupDirectory; }
}

// Enhanced Recipe Management System
class Recipe {
    private String name;
    private String category;
    private String description;
    private int preparationTime; // minutes
    private String difficulty;
    private List<RecipeIngredient> ingredients;
    private String instructions;
    private String imagePath;
    private LocalDateTime createdDate;
    private LocalDateTime lastModified;
    private int timesUsed;

    public Recipe() {
        this.ingredients = new ArrayList<>();
        this.createdDate = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
        this.timesUsed = 0;
    }

    public Recipe(String name, String category) {
        this();
        this.name = name;
        this.category = category;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name; 
        this.lastModified = LocalDateTime.now();
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { 
        this.category = category;
        this.lastModified = LocalDateTime.now();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description;
        this.lastModified = LocalDateTime.now();
    }

    public int getPreparationTime() { return preparationTime; }
    public void setPreparationTime(int preparationTime) { 
        this.preparationTime = preparationTime;
        this.lastModified = LocalDateTime.now();
    }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { 
        this.difficulty = difficulty;
        this.lastModified = LocalDateTime.now();
    }

    public List<RecipeIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<RecipeIngredient> ingredients) { 
        this.ingredients = ingredients;
        this.lastModified = LocalDateTime.now();
    }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { 
        this.instructions = instructions;
        this.lastModified = LocalDateTime.now();
    }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { 
        this.imagePath = imagePath;
        this.lastModified = LocalDateTime.now();
    }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public LocalDateTime getLastModified() { return lastModified; }
    
    public int getTimesUsed() { return timesUsed; }
    public void incrementUsage() { 
        this.timesUsed++;
        this.lastModified = LocalDateTime.now();
    }

    public void addIngredient(RecipeIngredient ingredient) {
        this.ingredients.add(ingredient);
        this.lastModified = LocalDateTime.now();
    }

    public void removeIngredient(RecipeIngredient ingredient) {
        this.ingredients.remove(ingredient);
        this.lastModified = LocalDateTime.now();
    }

    public double calculateTotalCost(List<CoffeeProduct> inventory) {
        return ingredients.stream()
                .mapToDouble(ingredient -> {
                    CoffeeProduct product = inventory.stream()
                            .filter(p -> p.getName().equalsIgnoreCase(ingredient.getName()))
                            .findFirst()
                            .orElse(null);
                    return product != null ? ingredient.getAmount() * product.getCostPerUnit() : 0.0;
                })
                .sum();
    }
}

class RecipeIngredient {
    private String name;
    private double amount;
    private String unit;
    private boolean optional;
    private String notes;

    public RecipeIngredient() {}

    public RecipeIngredient(String name, double amount, String unit) {
        this.name = name;
        this.amount = amount;
        this.unit = unit;
        this.optional = false;
    }

    public RecipeIngredient(String name, double amount, String unit, boolean optional) {
        this(name, amount, unit);
        this.optional = optional;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public boolean isOptional() { return optional; }
    public void setOptional(boolean optional) { this.optional = optional; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

// Enhanced Recipe Manager
class EnhancedRecipeManager {
    private Map<String, Recipe> recipes;
    private DataPersistence dataPersistence;
    private List<String> categories;

    public EnhancedRecipeManager() {
        this.dataPersistence = new DataPersistence();
        this.recipes = dataPersistence.loadRecipes();
        this.categories = Arrays.asList("Hot Drinks", "Cold Drinks", "Specialty", "Seasonal", "Desserts");
        
        if (recipes.isEmpty()) {
            initializeDefaultRecipes();
        }
    }

    private void initializeDefaultRecipes() {
        // Create default recipes
        Recipe caramelMacchiato = new Recipe("Caramel Macchiato", "Hot Drinks");
        caramelMacchiato.setDescription("Rich espresso with steamed milk and caramel");
        caramelMacchiato.setPreparationTime(5);
        caramelMacchiato.setDifficulty("Medium");
        caramelMacchiato.addIngredient(new RecipeIngredient("Milk", 200, "ml"));
        caramelMacchiato.addIngredient(new RecipeIngredient("Espresso", 30, "ml"));
        caramelMacchiato.addIngredient(new RecipeIngredient("Caramel Syrup", 15, "ml"));
        caramelMacchiato.addIngredient(new RecipeIngredient("Sugar", 5, "g", true));
        caramelMacchiato.addIngredient(new RecipeIngredient("Whipped Cream", 20, "ml", true));

        Recipe latte = new Recipe("Caffe Latte", "Hot Drinks");
        latte.setDescription("Classic espresso with steamed milk");
        latte.setPreparationTime(3);
        latte.setDifficulty("Easy");
        latte.addIngredient(new RecipeIngredient("Milk", 220, "ml"));
        latte.addIngredient(new RecipeIngredient("Espresso", 30, "ml"));
        latte.addIngredient(new RecipeIngredient("Sugar", 5, "g", true));

        Recipe cappuccino = new Recipe("Cappuccino", "Hot Drinks");
        cappuccino.setDescription("Equal parts espresso, steamed milk, and milk foam");
        cappuccino.setPreparationTime(4);
        cappuccino.setDifficulty("Medium");
        cappuccino.addIngredient(new RecipeIngredient("Milk", 150, "ml"));
        cappuccino.addIngredient(new RecipeIngredient("Espresso", 30, "ml"));
        cappuccino.addIngredient(new RecipeIngredient("Sugar", 5, "g", true));
        cappuccino.addIngredient(new RecipeIngredient("Foamed Milk", 50, "ml"));

        recipes.put(caramelMacchiato.getName(), caramelMacchiato);
        recipes.put(latte.getName(), latte);
        recipes.put(cappuccino.getName(), cappuccino);
        
        saveRecipes();
    }

    public void addRecipe(Recipe recipe) {
        recipes.put(recipe.getName(), recipe);
        saveRecipes();
    }

    public void removeRecipe(String name) {
        recipes.remove(name);
        saveRecipes();
    }

    public void updateRecipe(String oldName, Recipe updatedRecipe) {
        recipes.remove(oldName);
        recipes.put(updatedRecipe.getName(), updatedRecipe);
        saveRecipes();
    }

    public Recipe getRecipe(String name) {
        return recipes.get(name);
    }

    public Set<String> getRecipeNames() {
        return recipes.keySet();
    }

    public List<Recipe> getRecipesByCategory(String category) {
        return recipes.values().stream()
                .filter(recipe -> category.equals(recipe.getCategory()))
                .sorted((r1, r2) -> r1.getName().compareToIgnoreCase(r2.getName()))
                .toList();
    }

    public List<Recipe> searchRecipes(String searchTerm) {
        String term = searchTerm.toLowerCase();
        return recipes.values().stream()
                .filter(recipe -> 
                    recipe.getName().toLowerCase().contains(term) ||
                    (recipe.getDescription() != null && recipe.getDescription().toLowerCase().contains(term)) ||
                    recipe.getIngredients().stream().anyMatch(ing -> ing.getName().toLowerCase().contains(term)))
                .toList();
    }

    public List<Recipe> getPopularRecipes(int limit) {
        return recipes.values().stream()
                .sorted((r1, r2) -> Integer.compare(r2.getTimesUsed(), r1.getTimesUsed()))
                .limit(limit)
                .toList();
    }

    public List<String> getCategories() {
        return new ArrayList<>(categories);
    }

    public void addCategory(String category) {
        if (!categories.contains(category)) {
            categories.add(category);
        }
    }

    private void saveRecipes() {
        dataPersistence.saveRecipes(recipes);
    }

    public Map<String, Recipe> getAllRecipes() {
        return new HashMap<>(recipes);
    }
}

// Validation utilities
class ValidationUtils {
    public static class ValidationResult {
        private boolean valid;
        private String errorMessage;

        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
    }

    public static ValidationResult validateProduct(CoffeeProduct product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            return new ValidationResult(false, "Product name cannot be empty");
        }
        if (product.getTotalCost() < 0) {
            return new ValidationResult(false, "Total cost cannot be negative");
        }
        if (product.getUnitSize() <= 0) {
            return new ValidationResult(false, "Unit size must be positive");
        }
        if (product.getStock() < 0) {
            return new ValidationResult(false, "Stock cannot be negative");
        }
        if (product.getMinStock() < 0) {
            return new ValidationResult(false, "Minimum stock cannot be negative");
        }
        return new ValidationResult(true, null);
    }

    public static ValidationResult validateRecipe(Recipe recipe) {
        if (recipe.getName() == null || recipe.getName().trim().isEmpty()) {
            return new ValidationResult(false, "Recipe name cannot be empty");
        }
        if (recipe.getIngredients().isEmpty()) {
            return new ValidationResult(false, "Recipe must have at least one ingredient");
        }
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            if (ingredient.getName() == null || ingredient.getName().trim().isEmpty()) {
                return new ValidationResult(false, "Ingredient name cannot be empty");
            }
            if (ingredient.getAmount() <= 0) {
                return new ValidationResult(false, "Ingredient amount must be positive");
            }
        }
        return new ValidationResult(true, null);
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public static boolean isValidPhoneNumber(String phone) {
        return phone != null && phone.matches("^[+]?[0-9\\s\\-()]{7,15}$");
    }
}

// Backup and Export utilities
class BackupManager {
    private DataPersistence dataPersistence;
    private String backupDirectory;

    public BackupManager(String backupDirectory) {
        this.dataPersistence = new DataPersistence();
        this.backupDirectory = backupDirectory;
        createBackupDirectory();
    }

    private void createBackupDirectory() {
        File dir = new File(backupDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public void createBackup() {
        String timestamp = LocalDateTime.now().toString().replaceAll(":", "-");
        String backupPath = backupDirectory + "backup_" + timestamp + "/";
        
        File backupDir = new File(backupPath);
        backupDir.mkdirs();

        try {
            // Copy current data files to backup directory
            copyFile("coffee_inventory.json", backupPath + "coffee_inventory.json");
            copyFile("coffee_recipes.json", backupPath + "coffee_recipes.json");
            copyFile("app_settings.json", backupPath + "app_settings.json");
        } catch (IOException e) {
            System.err.println("Error creating backup: " + e.getMessage());
        }
    }

    private void copyFile(String source, String destination) throws IOException {
        File sourceFile = new File(source);
        File destFile = new File(destination);
        
        if (sourceFile.exists()) {
            try (FileInputStream fis = new FileInputStream(sourceFile);
                 FileOutputStream fos = new FileOutputStream(destFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
        }
    }

    public List<String> getAvailableBackups() {
        File backupDir = new File(backupDirectory);
        File[] backupFolders = backupDir.listFiles(File::isDirectory);
        List<String> backups = new ArrayList<>();
        
        if (backupFolders != null) {
            for (File folder : backupFolders) {
                if (folder.getName().startsWith("backup_")) {
                    backups.add(folder.getName());
                }
            }
        }
        
        backups.sort(Collections.reverseOrder()); // Most recent first
        return backups;
    }
}

// Usage Analytics
class UsageAnalytics {
    private Map<String, Integer> recipeUsageCount;
    private Map<String, Double> ingredientUsageAmount;
    private LocalDateTime lastReset;

    public UsageAnalytics() {
        this.recipeUsageCount = new HashMap<>();
        this.ingredientUsageAmount = new HashMap<>();
        this.lastReset = LocalDateTime.now();
    }

    public void recordRecipeUsage(String recipeName) {
        recipeUsageCount.merge(recipeName, 1, Integer::sum);
    }

    public void recordIngredientUsage(String ingredientName, double amount) {
        ingredientUsageAmount.merge(ingredientName, amount, Double::sum);
    }

    public Map<String, Integer> getRecipeUsageCount() {
        return new HashMap<>(recipeUsageCount);
    }

    public Map<String, Double> getIngredientUsageAmount() {
        return new HashMap<>(ingredientUsageAmount);
    }

    public List<Map.Entry<String, Integer>> getTopRecipes(int limit) {
        return recipeUsageCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .toList();
    }

    public void resetAnalytics() {
        recipeUsageCount.clear();
        ingredientUsageAmount.clear();
        lastReset = LocalDateTime.now();
    }

    public LocalDateTime getLastReset() {
        return lastReset;
    }
}