package lowleveldesigns.pizzaorderingsystem;

/*
enums
* */

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

enum OrderType {
    PICKUP,
    DELIVERY;
}

enum OrderStatus {
    PLACED,
    PREPARING,
    OUT_FOR_DELIVERY,
    READY_FOR_PICKUP,
    DELIVERED,
    PICKUP_UP,
    CANCELLED;
}

enum PizzaSize {
    SMALL(8),
    MEDIUM(12),
    LARGE(16);

    private final double price;

    PizzaSize(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }
}

enum CrustType {
    THIN(0),
    THICK(1),
    STUFFED(2);

    private final double price;

    CrustType(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }
}

enum SauceType {
    TOMATO,
    PESTO,
    BBQ;
}

enum Topping {
    CHEESE(1.5),
    MUSHROOMS(1.5),
    PEPPERS(1.5),
    OLIVES(1.5),
    CORN(1.5),
    JALAPENO(1.5),
    ONIONS(1.5),
    TOMATO(1.5);

    private final double price;

    Topping(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }
}

/*
classes
* */
/*
Customer
Pizza (contains PizzaBuilder)
Order
PizzaOrderingSystem
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
Pizza
    knows:
        PizzaSize
        CrustType
        SauceType
        List<Topping>
        PizzaBuilder
            knows:
                PizzaSize
                CrustType
                SauceType
                List<Topping>   // optional
            does:
                withTopping(Topping) -> PizzaBuilder
                build() -> Pizza
    does:
        getPrice() -> double
* */
class Pizza {
    private PizzaSize size;
    private CrustType crustType;
    private SauceType sauceType;
    private List<Topping> toppings;   // optional

    private Pizza(PizzaBuilder builder) {
        this.size = builder.size;
        this.crustType = builder.crustType;
        this.sauceType = builder.sauceType;
        this.toppings = builder.toppings;
    }

    public static class PizzaBuilder {
        private PizzaSize size;
        private CrustType crustType;
        private SauceType sauceType;
        private List<Topping> toppings;   // optional

        public PizzaBuilder(PizzaSize size, CrustType crustType, SauceType sauceType) {
            this.size = size;
            this.crustType = crustType;
            this.sauceType = sauceType;
            this.toppings = new ArrayList<>();
        }

        public PizzaBuilder withTopping(Topping topping) {
            this.toppings.add(topping);
            return this;
        }

        public Pizza build() {
            return new Pizza(this);
        }
    }

    public double getPrice() {
        double price = 0;
        price += size.getPrice();
        price += crustType.getPrice();
        for(Topping topping: toppings) {
            price += topping.getPrice();
        }

        return price;
    }
}

/*
Order
    knows:
        orderId
        Customer
        List<Pizza>
        OrderType
        OrderStatus
        orderDateAndTime
    does:
        addPizza(Pizza)
        removePizza(Pizza)
        getTotal() -> double
        placeOrder()
        preparing()
        outForDelivery()
        delivered()
        readyForPickup()
        pickedUp()
        cancelOrder()
* */
class Order {
    private String orderId;
    private Customer customer;
    private List<Pizza> pizzas;
    private OrderType orderType;
    private OrderStatus orderStatus;
    private LocalDateTime orderDateAndTime;

    public Order(String orderId, Customer customer, OrderType orderType) {
        this.orderId = orderId;
        this.customer = customer;
        this.pizzas = new ArrayList<>();
        this.orderType = orderType;
    }

    public void addPizza(Pizza pizza) {
        pizzas.add(pizza);
    }

    public void removePizza(Pizza pizza) {
        pizzas.remove(pizza);
    }

    public double getTotal() {
        double total = 0;

        for(Pizza pizza: pizzas) {
            total += pizza.getPrice();
        }

        return total;
    }

    public void placeOrder() {
        this.orderStatus = OrderStatus.PLACED;
        this.orderDateAndTime = LocalDateTime.now();
    }

    public void preparing() {
        if(orderStatus != OrderStatus.PLACED) return;

        orderStatus = OrderStatus.PREPARING;
    }

    public void outForDelivery() {
        if(orderType != OrderType.DELIVERY || orderStatus != OrderStatus.PREPARING) return;

        orderStatus = OrderStatus.OUT_FOR_DELIVERY;
    }

    public void delivered() {
        if(orderType != OrderType.DELIVERY || orderStatus != OrderStatus.OUT_FOR_DELIVERY) return;

        orderStatus = OrderStatus.DELIVERED;
    }

    public void readyForPickup() {
        if(orderType != OrderType.PICKUP || orderStatus != OrderStatus.PREPARING) return;

        orderStatus = OrderStatus.READY_FOR_PICKUP;
    }

    public void pickedUp() {
        if(orderType != OrderType.PICKUP || orderStatus != OrderStatus.READY_FOR_PICKUP) return;

        orderStatus = OrderStatus.PICKUP_UP;
    }

    public void cancelOrder() {
        if(orderStatus != OrderStatus.PLACED) return;

        orderStatus = OrderStatus.CANCELLED;
    }
}

/*
PizzaOrderingSystem
    knows:
        List<Order>
    does:
        createOrder(orderId, Customer, OrderType) -> Order
        addPizza(Order, Pizza)
        removePizza(Order, Pizza)
        getOrderTotal(Order) -> double
        placeOrder(Order)
        preparing(Order)
        outForDelivery(Order)
        delivered(Order)
        readyForPickup(Order)
        pickedUp(Order)
        cancelOrder(Order)
* */
public class PizzaOrderingSystem {
    private List<Order> orders;

    public PizzaOrderingSystem() {
        this.orders = new ArrayList<>();
    }

    public Order createOrder(String orderId, Customer customer, OrderType orderType) {
        Order newOrder = new Order(orderId, customer, orderType);
        orders.add(newOrder);
        return newOrder;
    }

    public void addPizza(Order order, Pizza pizza) {
        order.addPizza(pizza);
    }

    public void removePizza(Order order, Pizza pizza) {
        order.removePizza(pizza);
    }

    public double getOrderTotal(Order order) {
        return order.getTotal();
    }

    public void placeOrder(Order order) {
        order.placeOrder();
    }

    public void preparing(Order order) {
        order.preparing();
    }

    public void outForDelivery(Order order) {
        order.outForDelivery();
    }

    public void delivered(Order order) {
        order.delivered();
    }

    public void readyForPickup(Order order) {
        order.readyForPickup();
    }

    public void pickedUp(Order order) {
        order.pickedUp();
    }

    public void cancelOrder(Order order) {
        order.cancelOrder();
    }
}

/*

Pizza has-a PizzaSize
Pizza has-a CrustType
Pizza has-a SauceType
Pizza has-a List of Topping

Order has-a Customer
Order has-a List of Pizza
Order has-a OrderType
Order has-a OrderStatus

PizzaOrderingSystem has-a List of Order

* */