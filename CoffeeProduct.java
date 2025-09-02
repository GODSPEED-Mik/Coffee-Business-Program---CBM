package Micow.ProjectC.Micow_Cashier;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class CoffeeProduct implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String category;
    private double totalCost;   // total value of current stock
    private double unitSize;    // size of one unit (g, ml, etc.)
    private String unit;
    private double stock;       // number of units in stock (can be fractional)
    private double minStock;    // threshold for low stock
    private String imagePath;
    private LocalDateTime lastUpdated;
    private Map<String, Double> ingredients;

    public CoffeeProduct(String name, String category, double totalCost, double unitSize,
                         String unit, double stock, double minStock, String imagePath) {
        this.name = name;
        this.category = category;
        this.totalCost = totalCost;
        this.unitSize = unitSize;
        this.unit = unit;
        this.stock = stock;
        this.minStock = minStock;
        this.imagePath = imagePath;
        this.lastUpdated = LocalDateTime.now();
        this.ingredients = new HashMap<>();
    }

    // Default constructor for serialization
    public CoffeeProduct() {
        this.ingredients = new HashMap<>();
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getTotalCost() { return totalCost; }
    public double getUnitSize() { return unitSize; }
    public String getUnit() { return unit; }
    public double getStock() { return stock; }
    public double getMinStock() { return minStock; }
    public String getImagePath() { return imagePath; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public Map<String, Double> getIngredients() { return new HashMap<>(ingredients); }

    // Setters
    public void setName(String name) { this.name = name; touch(); }
    public void setCategory(String category) { this.category = category; touch(); }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; touch(); }
    public void setUnitSize(double unitSize) { this.unitSize = unitSize; touch(); }
    public void setUnit(String unit) { this.unit = unit; touch(); }
    public void setStock(double stock) { this.stock = stock; touch(); }
    public void setMinStock(double minStock) { this.minStock = minStock; touch(); }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; touch(); }

    private void touch() { this.lastUpdated = LocalDateTime.now(); }

    // Business methods
    public double getPhysicalStock() { return stock * unitSize; }
    public double getCostPerUnit() { return stock == 0 ? 0 : totalCost / stock; }

    public void reduceStock(double physicalAmount) {
        if (physicalAmount <= 0) return;

        double currentPhysical = getPhysicalStock();
        if (physicalAmount > currentPhysical) {
            stock = 0;
            totalCost = 0;
        } else {
            double costPerPhysical = (currentPhysical == 0) ? 0 : totalCost / currentPhysical;
            double costDeduct = physicalAmount * costPerPhysical;
            double fraction = physicalAmount / unitSize;
            stock -= fraction;
            totalCost -= costDeduct;

            if (stock < 0) stock = 0;
            if (totalCost < 0) totalCost = 0;
        }
        touch();
    }

    public void addStock(double physicalAmount, double addedCost) {
        if (physicalAmount <= 0) return;
        double fraction = physicalAmount / unitSize;
        stock += fraction;
        totalCost += addedCost;
        touch();
    }

    public String getStockStatus() {
        if (stock == 0) return "Out of Stock";
        if (stock <= minStock) return "Low Stock";
        return "In Stock";
    }

    // Helpers
    public boolean isLowStock() { return stock <= minStock && stock > 0; }
    public boolean isOutOfStock() { return stock == 0; }

    @Override
    public String toString() {
        return String.format("%s (%s) - Stock: %.2f, Cost/Unit: ₱%.2f",
                name, category, stock, getCostPerUnit());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CoffeeProduct)) return false;
        CoffeeProduct other = (CoffeeProduct) obj;
        return name != null && name.equals(other.name);
    }

    @Override
    public int hashCode() { return name != null ? name.hashCode() : 0; }
}
