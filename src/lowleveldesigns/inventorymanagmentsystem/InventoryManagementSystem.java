package lowleveldesigns.inventorymanagmentsystem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

/*
enum
* */
enum OrderStatus {
    IN_PROGRESS,
    FULFILLED,
    REJECTED,
    CANCELLED;
}

/*
classes
* */
/*
Product
NotificationService
Order
Warehouse
InventoryManagementSystem
* */

/*
Product
    knows:
        productId
        productName
        productCategory
        description
        minThreshold
        maxQuantity
    does:
        nothing (data carrier)
* */
class Product {
    private String productId;
    private String productName;
    private String productCategory;
    private String description;
    private int minThreshold;
    private int maxQuantity;

    public Product(
            String productId,
            String productName,
            String productCategory,
            String description,
            int minThreshold,
            int maxQuantity
    ) {
        this.productId = productId;
        this.productName = productName;
        this.productCategory = productCategory;
        this.description = description;
        this.minThreshold = minThreshold;
        this.maxQuantity = maxQuantity;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public String getDescription() {
        return description;
    }

    public int getMinThreshold() {
        return minThreshold;
    }

    public int getMaxQuantity() {
        return maxQuantity;
    }
}

/*
NotificationService
    knows:
    does:
        notify(Product)
* */
class NotificationService {
    public void notify(Product product) {
        System.out.println("Product: " + product.getProductName() + " is in low stock.");
    }
}

/*
Order
    knows:
        orderId
        Map<Product, Integer>
        OrderStatus
        timestamp
    does:
        updateStatus()
* */
class Order {
    private String orderId;
    private Map<Product, Integer> productQuantitiesOrdered;
    private OrderStatus orderStatus;
    private LocalDateTime timestamp;

    public Order(String orderId, Map<Product, Integer> productQuantitiesOrdered) {
        this.orderId = orderId;
        this.productQuantitiesOrdered = productQuantitiesOrdered;
        this.orderStatus = OrderStatus.IN_PROGRESS;
        this.timestamp = LocalDateTime.now();
    }

    public String getOrderId() {
        return orderId;
    }

    public Map<Product, Integer> getProductQuantitiesOrdered() {
        return productQuantitiesOrdered;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void updateStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}

/*
Warehouse
    knows:
        warehouseId
        warehouseName
        warehouseLocation
        Map<Product, Integer>
        List<Order>
        NotificationService
        ReentrantLock
    does:
        addProduct(Product)
        removeProduct(Product)
        incrementProductQuantity(Product, quantity)
        decrementProductQuantity(Product, quantity)
        placeOrder(Map<Product, Integer>) -> Order
        fulfillOrder(Order)
        cancelOrder(Order)
        checkAndNotify(Product)
* */
class Warehouse {
    private String warehouseId;
    private String warehouseName;
    private String warehouseLocation;
    private Map<Product, Integer> currProductQuantities;
    private List<Order> orders;
    private NotificationService notificationService;
    private final ReentrantLock lock = new ReentrantLock();

    public Warehouse(
            String warehouseId,
            String warehouseName,
            String warehouseLocation,
            Map<Product, Integer> initialProductQuantities,
            NotificationService notificationService
    ) {
        this.warehouseId = warehouseId;
        this.warehouseName = warehouseName;
        this.warehouseLocation = warehouseLocation;
        this.currProductQuantities = initialProductQuantities;
        this.orders = new ArrayList<>();
        this.notificationService = notificationService;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public String getWarehouseLocation() {
        return warehouseLocation;
    }

    public int getCurrProductQuantity(Product product) {
        if(!currProductQuantities.containsKey(product)) throw new RuntimeException();
        return currProductQuantities.get(product);
    }

    public void addProduct(Product product) {
        currProductQuantities.putIfAbsent(product, 0);
    }

    public void removeProduct(Product product) {
        currProductQuantities.remove(product);
    }

    public void incrementProductQuantity(Product product, int quantity) {
        lock.lock();
        try {
            if(!currProductQuantities.containsKey(product)) {
                addProduct(product);
            }
            int currQuantity = currProductQuantities.get(product);
            int finalQuantity = Math.min(product.getMaxQuantity(), currQuantity + quantity);
            currProductQuantities.put(product, finalQuantity);
        } finally {
            lock.unlock();
        }
    }

    public void decrementProductQuantity(Product product, int quantity) {
        lock.lock();
        try {
            if(!currProductQuantities.containsKey(product)) return;

            int currQuantity = currProductQuantities.get(product);
            int finalQuantity = Math.max(0, currQuantity - quantity);
            currProductQuantities.put(product, finalQuantity);
            checkAndNotify(product);
        } finally {
            lock.unlock();
        }
    }

    public Order placeOrder(Map<Product, Integer> productQuantitiesOrdered) {
        lock.lock();
        try {
            // 1. Check whether each product quantity ordered can be fulfilled
            for(Map.Entry<Product, Integer> entry: productQuantitiesOrdered.entrySet()) {
                Product currProduct = entry.getKey();
                int quantityOrdered = entry.getValue();
                if(!currProductQuantities.containsKey(currProduct) || quantityOrdered > currProductQuantities.get(currProduct)) {
                    return null;
                }
            }
            // 2. Decrement the quantity of each product
            for(Map.Entry<Product, Integer> entry: productQuantitiesOrdered.entrySet()) {
                Product currProduct = entry.getKey();
                int quantityOrdered = entry.getValue();
                int currQuantity = currProductQuantities.get(currProduct);
                int finalQuantity = Math.max(0, currQuantity - quantityOrdered);
                currProductQuantities.put(currProduct, finalQuantity);
                checkAndNotify(currProduct);
            }
            // 3. Create a new order and add it to the orders list
            Order order = new Order(LocalDateTime.now().toString(), productQuantitiesOrdered);
            orders.add(order);
            // 4. Return the order
            return order;
        } finally {
            lock.unlock();
        }
    }

    public void fulfillOrder(Order order) {
        if(!orders.contains(order) || order.getOrderStatus() != OrderStatus.IN_PROGRESS) return;
        order.updateStatus(OrderStatus.FULFILLED);
    }

    public void cancelOrder(Order order) {
        if(!orders.contains(order) || order.getOrderStatus() != OrderStatus.IN_PROGRESS) return;

        lock.lock();
        try {
            for(Map.Entry<Product, Integer> entry: order.getProductQuantitiesOrdered().entrySet()) {
                Product currProduct = entry.getKey();
                int quantityOrdered = entry.getValue();
                int finalQuantity = currProductQuantities.getOrDefault(currProduct, 0) + quantityOrdered;
                currProductQuantities.put(currProduct, finalQuantity);
            }
            order.updateStatus(OrderStatus.CANCELLED);
        } finally {
            lock.unlock();
        }
    }

    private void checkAndNotify(Product product) {
        if(currProductQuantities.get(product) < product.getMinThreshold()) {
            notificationService.notify(product);
        }
    }
}

/*
InventoryManagementSystem
    knows:
        List<Warehouse>
    does:
        addWarehouse(Warehouse)
        removeWarehouse(Warehouse)
        addProduct(Warehouse, Product, quantity)
        removeProduct(Warehouse, Product, quantity)
        placeOrder(Warehouse, Map<Product, Integer>) -> Order
        fulfillOrder(Warehouse, Order)
        cancelOrder(Warehouse, Order)
* */
public class InventoryManagementSystem {
    private List<Warehouse> warehouses;

    public InventoryManagementSystem() {
        this.warehouses = new ArrayList<>();
    }

    public void addWarehouse(Warehouse warehouse) {
        warehouses.add(warehouse);
    }

    public void removeWarehouse(Warehouse warehouse) {
        warehouses.remove(warehouse);
    }

    public void addProduct(Warehouse warehouse, Product product, int quantity) {
        warehouse.incrementProductQuantity(product, quantity);
    }

    public void removeProduct(Warehouse warehouse, Product product, int quantity) {
        warehouse.decrementProductQuantity(product, quantity);
    }

    public Order placeOrder(Warehouse warehouse, Map<Product, Integer> productQuantitiesOrdered) {
        return warehouse.placeOrder(productQuantitiesOrdered);
    }

    public void fulfillOrder(Warehouse warehouse, Order order) {
        warehouse.fulfillOrder(order);
    }

    public void cancelOrder(Warehouse warehouse, Order order) {
        warehouse.cancelOrder(order);
    }
}

/*

Order has-a Map of Product -> quantity ordered
Order has-a OrderStatus

Warehouse has-a Map of Product -> current quantity
Warehouse has-a List of Order
Warehouse has-a NotificationService

InventoryManagementSystem has-a List of Warehouse

* */