package lowleveldesigns.foodorderingwithdiscounts;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
enums
* */
enum OrderStatus {
    PLACED,
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED;
}

/*
classes
* */
/*
Customer
FoodItem
(interface) PriceComponent
BaseOrder (implements PriceComponent)
(abstract) DiscountDecorator (implements PriceComponent)
PercentageDiscount (extends DiscountDecorator)
FlatDiscount (extends DiscountDecorator)
SeasonalDiscount (extends DiscountDecorator)
Order
FoodOrderingWithDiscounts
* */

/*
Customer
    knows:
        customerId
        name
        email
        phoneNo
    does:
        nothing (data carrier)
* */
class Customer {
    private String customerId;
    private String name;
    private String email;
    private String phoneNo;

    public Customer(String customerId, String name, String email, String phoneNo) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.phoneNo = phoneNo;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNo() {
        return phoneNo;
    }
}

/*
FoodItem
    knows:
        itemId
        name
        price
    does:
        nothing (data carrier)
* */
class FoodItem {
    private String itemId;
    private String name;
    private double price;

    public FoodItem(String itemId, String name, double price) {
        this.itemId = itemId;
        this.name = name;
        this.price = price;
    }

    public String getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }
}

/*
PriceComponent (interface)
    knows:
    does:
        getPrice() -> double
* */
interface PriceComponent {
    double getPrice();
}

/*
BaseOrder implements PriceComponent
    knows:
        List<FoodItem>
    does:
        getPrice() -> double
        getFoodItems() -> List<FoodItem>
* */
class BaseOrder implements PriceComponent {
    private List<FoodItem> foodItems;

    public BaseOrder(List<FoodItem> foodItems) {
        this.foodItems = foodItems;
    }

    @Override
    public double getPrice() {
        double result = 0;

        for(FoodItem foodItem: foodItems) {
            result += foodItem.getPrice();
        }

        return result;
    }

    public List<FoodItem> getFoodItems() {
        return foodItems;
    }
}

/*
abstract DiscountDecorator implements PriceComponent
    knows:
        PriceComponent next
    does:
        abstract getPrice() -> double
        getNext() -> PriceComponent
* */
abstract class DiscountDecorator implements PriceComponent {
    private PriceComponent next;

    public DiscountDecorator(PriceComponent next) {
        this.next = next;
    }

    @Override
    abstract public double getPrice();

    public PriceComponent getNext() {
        return next;
    }
}

/*
PercentageDiscount extends DiscountDecorator
    knows:
        percentageAmount
    does:
        getPrice() -> double
* */
class PercentageDiscount extends DiscountDecorator {
    private double percentageAmount;

    public PercentageDiscount(PriceComponent next, double percentageAmount) {
        super(next);
        this.percentageAmount = percentageAmount;
    }

    @Override
    public double getPrice() {
        double price = super.getNext().getPrice();
        return price - (price * percentageAmount / 100);
    }
}

/*
FlatDiscount extends DiscountDecorator
    knows:
        flatAmount
    does:
        getPrice() -> double
* */
class FlatDiscount extends DiscountDecorator {
    private double flatAmount;

    public FlatDiscount(PriceComponent next, double flatAmount) {
        super(next);
        this.flatAmount = flatAmount;
    }

    @Override
    public double getPrice() {
        double price = super.getNext().getPrice();
        return Math.max(0, price - flatAmount);
    }
}

/*
SeasonalDiscount extends DiscountDecorator
    knows:
        seasonalAmount
        startTime
        endTime
    does:
        isDiscountValid() -> boolean
        getPrice() -> double
* */
class SeasonalDiscount extends DiscountDecorator {
    private double seasonalAmount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public SeasonalDiscount(
            PriceComponent next,
            double seasonalAmount,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        super(next);
        this.seasonalAmount = seasonalAmount;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean isDiscountValid() {
        LocalDateTime currTime = LocalDateTime.now();
        return currTime.isAfter(startTime) && currTime.isBefore(endTime);
    }

    @Override
    public double getPrice() {
        double price = super.getNext().getPrice();
        if(isDiscountValid()) {
            return Math.max(0, price - seasonalAmount);
        }
        return price;
    }
}

/*
Order
    knows:
        orderId
        Customer
        List<FoodItem>
        PriceComponent
        OrderStatus
        timestamp
    does:
        getFinalPrice() -> double
        updateStatus(OrderStatus)
* */
class Order {
    private String orderId;
    private Customer customer;
    private List<FoodItem> foodItems;
    private PriceComponent priceComponent;
    private OrderStatus orderStatus;
    private LocalDateTime timestamp;

    public Order(
            String orderId,
            Customer customer,
            List<FoodItem> foodItems,
            PriceComponent priceComponent
    ) {
        this.orderId = orderId;
        this.customer = customer;
        this.foodItems = foodItems;
        this.priceComponent = priceComponent;
        this.orderStatus = OrderStatus.PLACED;
        this.timestamp = LocalDateTime.now();
    }

    public double getFinalPrice() {
        return priceComponent.getPrice();
    }

    public void updateStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderId() {
        return orderId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public List<FoodItem> getFoodItems() {
        return foodItems;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

/*
FoodOrderingWithDiscounts
    knows:
        List<Customer>
        List<FoodItem>
        List<Order>
    does:
        addCustomer(Customer)
        removeCustomer(Customer)
        addFoodItem(FoodItem)
        removeFoodItem(FoodItem)
        placeOrder(orderId, Customer, List<FoodItem>, PriceComponent) -> Order
        preparing(Order)
        outForDelivery(Order)
        delivered(Order)
        cancelOrder(Customer, Order)
* */
public class FoodOrderingWithDiscounts {
    private List<Customer> customers;
    private List<FoodItem> foodItems;
    private List<Order> orders;

    public FoodOrderingWithDiscounts() {
        this.customers = new ArrayList<>();
        this.foodItems = new ArrayList<>();
        this.orders = new ArrayList<>();
    }

    public void addCustomer(Customer customer) {
        customers.add(customer);
    }

    public void removeCustomer(Customer customer) {
        customers.remove(customer);
    }

    public void addFoodItem(FoodItem foodItem) {
        foodItems.add(foodItem);
    }

    public void removeFoodItem(FoodItem foodItem) {
        foodItems.remove(foodItem);
    }

    public Order placeOrder(String orderId, Customer customer, List<FoodItem> foodItems, PriceComponent priceComponent) {
        Order order = new Order(orderId, customer, foodItems, priceComponent);
        orders.add(order);
        return order;
    }

    public void preparing(Order order) {
        if(!orders.contains(order) || order.getOrderStatus() != OrderStatus.PLACED) return;
        order.updateStatus(OrderStatus.PREPARING);
    }

    public void outForDelivery(Order order) {
        if(!orders.contains(order) || order.getOrderStatus() != OrderStatus.PREPARING) return;
        order.updateStatus(OrderStatus.OUT_FOR_DELIVERY);
    }

    public void delivered(Order order) {
        if(!orders.contains(order) || order.getOrderStatus() != OrderStatus.OUT_FOR_DELIVERY) return;
        order.updateStatus(OrderStatus.DELIVERED);
    }

    public void cancelOrder(Customer customer, Order order) {
        if(!orders.contains(order) ||
                !order.getCustomer().getCustomerId().equals(customer.getCustomerId()) ||
                order.getOrderStatus() != OrderStatus.PLACED
        ) {
            return;
        }
        order.updateStatus(OrderStatus.CANCELLED);
    }
}

/*

BaseOrder is-a PriceComponent
BaseOrder has-a List of FoodItem

DiscountDecorator is-a PriceComponent
DiscountDecorator has-a PriceComponent next

PercentageDiscount is-a DiscountDecorator
FlatDiscount is-a DiscountDecorator
SeasonalDiscount is-a DiscountDecorator

Order has-a Customer
Order has-a List of FoodItem
Order has-a PriceComponent
Order has-a OrderStatus

FoodOrderingWithDiscounts has-a List of Customer
FoodOrderingWithDiscounts has-a List of FoodItem
FoodOrderingWithDiscounts has-a List of Order

* */