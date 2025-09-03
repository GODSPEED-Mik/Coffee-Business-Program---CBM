package Micow.ProjectC.Micow_Cashier;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Transaction {
    private long id;
    private LocalDateTime timestamp;
    private List<TransactionItem> items = new ArrayList<>();
    private double total;
    private String paymentMethod;
    private String cashier;

    public Transaction() {
        this.timestamp = LocalDateTime.now();
    }
    // getters/setters

    public void addItem(TransactionItem item){
        items.add(item);
        recalcTotal();
    }
    public void recalcTotal(){
        total = items.stream().mapToDouble(TransactionItem::getSubtotal).sum();
    }
}
