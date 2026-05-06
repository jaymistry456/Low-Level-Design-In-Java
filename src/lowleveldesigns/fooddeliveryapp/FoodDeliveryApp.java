package lowleveldesigns.fooddeliveryapp;

/*
enums
OrderStatus
*/

/*
Customer
Agent
Location
Order
MenuItem
Restaurant
AgentMatchingStrategy
PricingCalculator
FoodDeliveryApp
* */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/*
Enums
* */
enum OrderStatus {
    ORDER_PLACED,
    PREPARING,
    PICKEP_UP,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED;
}

/*
Location
    knows:
        latitude
        longitude
    does:
        distanceTo() -> double
* */
record Location(double latitude, double longitude) {
    public double distanceTo(Location other) {
        return Math.sqrt(
                Math.pow(this.latitude - other.latitude, 2) +
                Math.pow(this.longitude - other.longitude, 2)
        );
    }
}

/*
Customer
    knows:
        customerId
        customerName
        customerEmail
        customerPhoneNumber
        customerCreditCardInfo
        Location
    does:
        nothing (data carrier)
* */

class Customer {
    private String customerId;
    private String name;
    private String email;
    private String phoneNumber;
    private String creditCardInfo;
    private Location address;

    public Customer(String customerId, String name, String email, String phoneNumber, String creditCardInfo, Location address) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.creditCardInfo = creditCardInfo;
        this.address = address;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCreditCardInfo() {
        return creditCardInfo;
    }

    public Location getAddress() {
        return address;
    }
}

/*
Agent
    knows:
        agentId
        agentName
        agentEmail
        agentPhoneNumber
        agentRating
        agentLocation
        isAvailable
    does:
        available()
        busy()
        setRating(int rating)
 */
class Agent {
    private String agentId;
    private String name;
    private String email;
    private int phoneNumber;
    private double rating;
    private int noOfRatings;
    private Location location;
    private boolean isAvailable;

    public Agent(String agentId, String name, String email, int phoneNumber, Location location) {
        this.agentId = agentId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.rating = 0;
        this.noOfRatings = 0;
        this.location = location;
        isAvailable = true;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getPhoneNumber() {
        return phoneNumber;
    }

    public double getRating() {
        return rating;
    }

    public Location getLocation() {
        return location;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void available() {
        this.isAvailable = true;
    }

    public void busy() {
        this.isAvailable = false;
    }

    public void setRating(int newRating) {
        rating = (rating * noOfRatings + newRating) / (noOfRatings + 1);
        noOfRatings++;
    }
}

/*
MenuItem
    knows:
        itemId
        itemName
        itemPrice
    does:
        nothing (data carrier)
* */
class MenuItem {
    private String itemId;
    private String itemName;
    private double itemPrice;

    public MenuItem(String itemId, String itemName, double itemPrice) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
    }

    public String getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public double getItemPrice() {
        return itemPrice;
    }
}

/*
Restaurant
    knows:
        restaurantId
        restaurantName
        Location
        phoneNumber
        List<MenuItem>
        rating
        noOfRatings
    does:
        setRating(rating)
* */
class Restaurant {
    private String restaurantId;
    private String restaurantName;
    private Location address;
    private int phoneNumber;
    private List<MenuItem> menuItems;
    private double rating;
    private int noOfRatings;

    public Restaurant(String restaurantId, String restaurantName, Location address, int phoneNumber, List<MenuItem> menuItems) {
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.menuItems = menuItems;
        this.rating = 0;
        this.noOfRatings = 0;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public Location getAddress() {
        return address;
    }

    public int getPhoneNumber() {
        return phoneNumber;
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(int newRating) {
        rating = (rating * noOfRatings + newRating) / (noOfRatings + 1);
        noOfRatings++;
    }
}

/*
Order
    knows:
        Customer
        Agent
        Restaurant
        List<MenuItem>
        OrderStatus
        amount
        lock
    does:
        tryAccept(Agent)
        updateStatus(OrderStatus)
        getStatus() -> OrderStatus
* */
class Order {
    private Customer customer;
    private Agent agent;
    private Restaurant restaurant;
    private List<MenuItem> items;
    private OrderStatus status;
    private double amount;

    private final ReentrantLock lock = new ReentrantLock();

    public Order(Customer customer, Restaurant restaurant, List<MenuItem> items) {
        this.customer = customer;
        this.restaurant = restaurant;
        this.items = items;
        this.status = OrderStatus.ORDER_PLACED;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Agent getAgent() {
        return agent;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public double getAmount() {
        return amount;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public boolean tryAccept(Agent agent) {
        lock.lock();
        try {
            if(status == OrderStatus.PREPARING) {
                this.agent = agent;
                status = OrderStatus.PICKEP_UP;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public void updateStatus(OrderStatus status) {
        this.status = status;
    }

    public OrderStatus getStatus() {
        return status;
    }
}

/*
AgentMatchingStrategy
    knows:
        List<Agent>
    does:
        findNearbyAgents(Location, searchRadius) -> List<Agent>
* */
class AgentMatchingStrategy {
    private List<Agent> agents;

    public AgentMatchingStrategy(List<Agent> agents) {
        this.agents = agents;
    }

    public List<Agent> findNearbyAgents(Location address, int searchRadius) {
        return agents.stream()
                .filter(
                        agent -> agent.isAvailable() &&
                                        agent.getLocation().distanceTo(address) <= searchRadius)
                .toList();
    }
}

/*
PricingCalculator
    knows:
        deliveryFee (flat)
    does:
        calculate(List<MenuItem>) -> double
* */
class PricingCalculator {
    private final double deliveryFee = 5;

    public double calculate(List<MenuItem> menuItems) {
        double result = 0;
        for(MenuItem item: menuItems) {
            result += item.getItemPrice();
        }

        return result + deliveryFee;
    }
}

/*
FoodDeliveryApp
    knows:
        List<Customer>
        List<Agent>
        List<Order>
        List<Restaurant>
        AgentMatchingStrategy
        PricingCalculator
    does:
        addCustomer(Customer)
        removeCustomer(Customer)
        addAgent(Agent)
        removeAgent(Agent)
        addRestaurant(Restaurant)
        removeRestaurant(Restaurant)
        showRestaurants(Location address) -> List<Restaurant>
        placeOrder(Customer, Restaurant, List<MenuItem>)
        cancelOrder(Order)
        acceptOrder(Agent, Order)
        completeOrder(Order)
        rateAgent(Agent, rating)
        rateRestaurant(Restaurant, rating)
* */
class FoodDeliveryApp {
    private List<Customer> customers;
    private List<Agent> agents;
    private List<Order> orders;
    private List<Restaurant> restaurants;
    private AgentMatchingStrategy strategy;
    private PricingCalculator pricingCalculator;

    public FoodDeliveryApp(AgentMatchingStrategy strategy, PricingCalculator pricingCalculator) {
        this.customers = new ArrayList<>();
        this.agents = new ArrayList<>();
        this.orders = new ArrayList<>();
        this.restaurants = new ArrayList<>();
        this.strategy = strategy;
        this.pricingCalculator = pricingCalculator;
    }

    public void addCustomer(Customer customer) {
        customers.add(customer);
    }

    public void removeCustomer(Customer customer) {
        customers.remove(customer);
    }

    public void addAgent(Agent agent) {
        agents.add(agent);
    }

    public void removeAgent(Agent agent) {
        agents.remove(agent);
    }

    public void addRestaurant(Restaurant restaurant) {
        restaurants.add(restaurant);
    }

    public void removeRestaurant(Restaurant restaurant) {
        restaurants.remove(restaurant);
    }

    public List<Restaurant> showNearbyRestaurants(Customer customer, int searchRadius) {
        return restaurants.stream()
                .filter(restaurant ->
                            restaurant.getAddress().distanceTo(customer.getAddress()) <= searchRadius
                        )
                .toList();
    }

    public void placeOrder(Customer customer, Restaurant restaurant, List<MenuItem> items) {
        Order newOrder = new Order(customer, restaurant, items);
        newOrder.setAmount(pricingCalculator.calculate(items));
        strategy.findNearbyAgents(customer.getAddress(), 5);

        newOrder.updateStatus(OrderStatus.PREPARING);

        orders.add(newOrder);
    }

    public void cancelOrder(Order order) {
        if(order.getStatus() == OrderStatus.PREPARING) {
            order.updateStatus(OrderStatus.CANCELLED);
        }
    }

    public void acceptOrder(Order order, Agent agent) {
        if(order.tryAccept(agent)) {
            agent.busy();
        }
    }

    public void completeOrder(Order order) {
        order.updateStatus(OrderStatus.DELIVERED);
        order.getAgent().available();
    }

    public void rateAgent(Agent agent, int rating) {
        agent.setRating(rating);
    }

    public void rateRestaurant(Restaurant restaurant, int rating) {
        restaurant.setRating(rating);
    }

}


/*

Customer has-a Location

Restaurant has-a List of MenuItem and a Location

Order has-a Customer, an Agent and a Restaurant

AgentMatchingStrategy has-a List of Agent

FoodDeliveryApp has-a List of Customer
FoodDeliveryApp has-a List of Agent
FoodDeliveryApp has-a List of Order
FoodDeliveryApp has-a List of Restaurant
FoodDeliveryApp has-a AgentMatchingStrategy
FoodDeliveryApp has-a PricingCalculator

* */
