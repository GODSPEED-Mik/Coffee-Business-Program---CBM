package Micow.ProjectC.Micow_Cashier;

public class TransactionItem {
    private long id;
    private long productId;
    private String productName;
    private double qty;
    private double unitPrice;

    public TransactionItem(long productId, String productName, double qty, double unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.qty = qty;
        this.unitPrice = unitPrice;
    }
    public double getSubtotal(){ return qty * unitPrice; }
    // getters/setters...
}
