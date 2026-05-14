package lowleveldesigns.stockexchangesystem;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/*
enums
* */
enum OrderStatus {
    PENDING,
    EXECUTED,
    CANCELLED;
}

enum OrderType {
    MARKET,
    LIMIT;
}

enum OrderSide {
    BUY,
    SELL;
}

/*
classes
* */
/*
Trader
Stock
OrderBook
Order
TradeRecord
NotificationService
StockExchangeSystem
* */

/*
Trader
    knows:
        traderId
        name
        email
        phoneNo
    does:
        nothing (date carrier)
* */
class Trader {
    private String traderId;
    private String name;
    private String email;
    private int phoneNo;

    public Trader(String traderId, String name, String email, int phoneNo) {
        this.traderId = traderId;
        this.name = name;
        this.email = email;
        this.phoneNo = phoneNo;
    }

    public String getTraderId() {
        return traderId;
    }

    public String getName() {
        return name;
    }

    public String email() {
        return email;
    }

    public int getPhoneNo() {
        return phoneNo;
    }
}

/*
Stock
    knows:
        symbol
        name
    does:
        nothing (date carrier)
* */
class Stock {
    private String symbol;
    private String name;

    public Stock(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }
}

/*
Order
    knows:
        orderId
        Trader
        OrderSide
        OrderType
        Stock
        quantity
        price
        timestamp
        OrderStatus
    does:
        updateStatus(OrderStatus)
* */
class Order {
    private String orderId;
    private Trader trader;
    private OrderSide orderSide;
    private OrderType orderType;
    private Stock stock;
    private int quantity;
    private Double price;
    private LocalDateTime timestamp;
    private OrderStatus orderStatus;

    public Order(
            String orderId,
            Trader trader,
            OrderSide orderSide,
            OrderType orderType,
            Stock stock,
            int quantity,
            Double price
    ) {
        this.orderId = orderId;
        this.trader = trader;
        this.orderSide = orderSide;
        this.orderType = orderType;
        this.stock = stock;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = LocalDateTime.now();
        this.orderStatus = OrderStatus.PENDING;
    }

    public String getOrderId() {
        return orderId;
    }

    public Trader getTrader() {
        return trader;
    }

    public OrderSide getOrderSide() {
        return orderSide;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public Stock getStock() {
        return stock;
    }

    public int getQuantity() {
        return quantity;
    }

    public Double getPrice() {
        return price;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void updateStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}

/*
OrderBook
    knows:
        PriorityQueue buyOrders (desc by price, asc by timestamp)
        PriorityQueue sellOrders (asc by price, asc by timestamp)
        Reentrant lock
    does:
        addOrder(Order)
        cancelOrder(Order)
        matchOrders() -> List<TradeRecord>
* */
class OrderBook {
    private PriorityQueue<Order> buyOrders;
    private PriorityQueue<Order> sellOrders;
    private final ReentrantLock lock = new ReentrantLock();

    public OrderBook() {
        this.buyOrders = new PriorityQueue<>((a, b) -> {
            if(!a.getPrice().equals(b.getPrice())) {
                return Double.compare(b.getPrice(), a.getPrice());
            }
            return a.getTimestamp().compareTo(b.getTimestamp());
        });

        this.sellOrders = new PriorityQueue<>((a, b) -> {
            if(!a.getPrice().equals(b.getPrice())) {
                return Double.compare(a.getPrice(), b.getPrice());
            }
            return a.getTimestamp().compareTo(b.getTimestamp());
        });
    }

    public List<TradeRecord> addOrder(Order order) {
        lock.lock();
        try {
            if(order.getOrderSide() == OrderSide.BUY) {
                buyOrders.add(order);
            } else {
                sellOrders.add(order);
            }
            return matchOrders();
        } finally {
            lock.unlock();
        }
    }

    public void cancelOrder(Order order) {
        lock.lock();
        try {
            if(order.getOrderSide() == OrderSide.BUY) {
                buyOrders.remove(order);
            } else {
                sellOrders.remove(order);
            }
            order.updateStatus(OrderStatus.CANCELLED);
        } finally {
            lock.unlock();
        }
    }

    public List<TradeRecord> matchOrders() {
        List<TradeRecord> records = new ArrayList<>();
        while(!buyOrders.isEmpty() && !sellOrders.isEmpty()) {
            if(buyOrders.peek().getPrice() < sellOrders.peek().getPrice()) {
                break;
            }
            Order buyOrder = buyOrders.poll();
            Order sellOrder = sellOrders.poll();

            int minQuantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());
            TradeRecord tradeRecord = new TradeRecord(buyOrder.getTrader(), sellOrder.getTrader(), buyOrder.getStock(), minQuantity, sellOrder.getPrice());
            records.add(tradeRecord);
            buyOrder.setQuantity(buyOrder.getQuantity() - minQuantity);
            if(buyOrder.getQuantity() == 0) {
                buyOrder.updateStatus(OrderStatus.EXECUTED);
            } else {
                buyOrders.offer(buyOrder);
            }
            sellOrder.setQuantity(sellOrder.getQuantity() - minQuantity);
            if(sellOrder.getQuantity() == 0) {
                sellOrder.updateStatus(OrderStatus.EXECUTED);
            } else {
                sellOrders.offer(sellOrder);
            }
        }

        return records;
    }
}


/*
TradeRecord
    knows:
        Trader buyer
        Trader seller
        Stock
        quantity
        price
        timestamp
    does:
        nothing (date carrier)
* */
class TradeRecord {
    private Trader buyer;
    private Trader seller;
    private Stock stock;
    private int quantity;
    private Double price;
    private LocalDateTime timestamp;

    public TradeRecord(Trader buyer, Trader seller, Stock stock, int quantity, Double price) {
        this.buyer = buyer;
        this.seller = seller;
        this.stock = stock;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = LocalDateTime.now();
    }

    public Trader getBuyer() {
        return buyer;
    }

    public Trader getSeller() {
        return seller;
    }

    public Stock getStock() {
        return stock;
    }

    public int getQuantity() {
        return quantity;
    }

    public Double getPrice() {
        return price;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

/*
NotificationService
    knows:
        nothing
    does:
        notify(TradeRecord)
* */
class NotificationService {
    public void notify(TradeRecord record) {
        System.out.println("Notification sent to Buyer: " + record.getBuyer().getName() + " and Seller: " + record.getSeller().getName() + " at timestamp: " + LocalDateTime.now());
    }
}

/*
StockExchangeSystem
    knows:
        List<Trader>
        List<Stock>
        Map<String, OrderBook>
        List<TradeRecord>
        NotificationService
    does:
        addTrader(Trader)
        removeTreader(Trader)
        addStock(Stock)
        removeStock(Stock)
        placeOrder(Order)
        cancelOrder(Order)
* */
public class StockExchangeSystem {
    private List<Trader> traders;
    private List<Stock> stocks;
    private List<TradeRecord> tradeRecords;
    private Map<String, OrderBook> map; // Stock symbol -> OrderBook
    private NotificationService notificationService;

    public StockExchangeSystem(NotificationService notificationService) {
        this.traders = new ArrayList<>();
        this.stocks = new ArrayList<>();
        this.tradeRecords = new ArrayList<>();
        this.map = new HashMap<>();
        this.notificationService = notificationService;
    }

    public void addTrader(Trader trader) {
        this.traders.add(trader);
    }

    public void removeTrader(Trader trader) {
        this.traders.remove(trader);
    }

    public void addStock(Stock stock) {
        this.stocks.add(stock);
    }

    public void removeStock(Stock stock) {
        this.stocks.remove(stock);
    }

    public void placeOrder(Order order) {
        if (order.getOrderType() == OrderType.MARKET) {
            if (!map.containsKey(order.getStock().getSymbol())) {
                order.updateStatus(OrderStatus.CANCELLED);
                return;
            }
            OrderBook orderBook = map.get(order.getStock().getSymbol());
            List<TradeRecord> records = orderBook.addOrder(order);
            for(TradeRecord record: records) {
                tradeRecords.add(record);
                notificationService.notify(record);
            }
        } else {
            if(!map.containsKey(order.getStock().getSymbol())) {
                map.put(order.getStock().getSymbol(), new OrderBook());
            }
            OrderBook orderBook = map.get(order.getStock().getSymbol());
            List<TradeRecord> records = orderBook.addOrder(order);
            for(TradeRecord record: records) {
                tradeRecords.add(record);
                notificationService.notify(record);
            }
        }
    }

    public void cancelOrder(Order order) {
        OrderBook orderBook = map.get(order.getStock().getSymbol());
        if(orderBook != null) {
            orderBook.cancelOrder(order);
        }
    }
}

/*

Order has-a Trader
Order has-a Stock
Order has-a OrderStatus
Order has-a OrderSide
Order has-a OrderType

OrderBook has-a PriorityQueue of Orders (two of them)

TradeRecord has-a Trader buyer and a Trader seller

StockExchangeSystem has-a List of Trader
StockExchangeSystem has-a List of Stock
StockExchangeSystem has-a Map<Stock, OrderBook>
StockExchangeSystem has-a TradeRecord
StockExchangeSystem has-a NotificationService

* */

