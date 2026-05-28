package lowleveldesigns.onlineauctionsystem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/*
enums
* */
enum AuctionState {
    ACTIVE,
    SOLD,
    EXPIRED,
    CANCELLED;
}

/*
classes
* */
/*
User
Bid
Auction
AuctionScheduler
OnlineAuctionSystem
* */

/*
User
    knows:
        userId
        name
        email
        phoneNo
    does:
        nothing (data carrier)
* */
class User {
    private String userId;
    private String name;
    private String email;
    private String phoneNo;

    public User (String userId, String name, String email, String phoneNo) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phoneNo = phoneNo;
    }

    public String getUserId () {
        return userId;
    }

    public String getNamae () {
        return name;
    }

    public String getEmail () {
        return email;
    }

    public String getPhoneNo () {
        return phoneNo;
    }
}

/*
Bid
    knows:
        User
        bidValue
        timestamp
    does:
        nothing (data carrier)
* */
class Bid {
    private User bidder;
    private double bidValue;
    private LocalDateTime timestamp;

    public Bid (User bidder, double bidValue) {
        this.bidder = bidder;
        this.bidValue = bidValue;
        this.timestamp = LocalDateTime.now();
    }

    public User getBidder () {
        return bidder;
    }

    public double getBidValue () {
        return bidValue;
    }

    public LocalDateTime getTimestamp () {
        return timestamp;
    }
}

/*
Auction
    knows:
        auctionId
        User (seller)
        itemName
        itemDescription
        startingPrice
        endTime
        AuctionState
        PriorityQueue<Bid>
        ReentrantLock
    does:
        isExpired() -> boolean
        getHighestBid() -> Bid
        bid(User, price)
        close()
        cancel()
* */
class Auction {
    private String auctionId;
    private User seller;
    private String itemName;
    private String itemDescription;
    private double startingPrice;
    private LocalDateTime endTime;
    private AuctionState auctionState;
    private PriorityQueue<Bid> bids;
    private ReentrantLock lock = new ReentrantLock();

    public Auction (
            String auctionId,
            User seller,
            String itemName,
            String itemDescription,
            double startingPrice,
            LocalDateTime endTime
    ) {
        this.auctionId = auctionId;
        this.seller = seller;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.startingPrice = startingPrice;
        this.endTime = endTime;
        this.auctionState = AuctionState.ACTIVE;
        this.bids = new PriorityQueue<>((a, b) -> {
            if (a.getBidValue() != b.getBidValue()) {
                return Double.compare(b.getBidValue(), a.getBidValue());
            }
            return a.getTimestamp().compareTo(b.getTimestamp());
        });   // Sorted by bidValue desc, timestamp asc
    }

    public String getAuctionId() {
        return auctionId;
    }

    public User getSeller() {
        return seller;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public AuctionState getAuctionState() {
        return auctionState;
    }

    public boolean isExpired () {
        return LocalDateTime.now().isAfter(endTime);
    }

    public Bid getHighestBid () {
        if (!bids.isEmpty()) {
            return bids.peek();
        }
        return null;
    }

    public void bid (User bidder, double price) {
        lock.lock();
        try {
            if (auctionState != AuctionState.ACTIVE || isExpired()) return;
            Bid currHighestBid = getHighestBid();
            if (currHighestBid == null || currHighestBid.getBidValue() < price) {
                bids.offer(new Bid(bidder, price));
            }
        } finally {
            lock.unlock();
        }
    }

    public void close () {
        lock.lock();
        try {
            if (auctionState != AuctionState.ACTIVE) return;
            if (bids.isEmpty()) {
                auctionState = AuctionState.EXPIRED;
            } else {
                auctionState = AuctionState.SOLD;
            }
        } finally {
            lock.unlock();
        }
    }

    public void cancel () {
        lock.lock();
        try {
            if (bids.isEmpty()) {
                auctionState = AuctionState.CANCELLED;
            }
        } finally {
            lock.unlock();
        }
    }
}

/*
AuctionScheduler
    knows:
        SchedulerExecutorService
    does:
        executeTask()
        start()
        stop()
* */
class AuctionScheduler {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private List<Auction> auctions;

    public AuctionScheduler (List<Auction> auctions) {
        this.auctions = auctions;
    }

    public void executeTask () {
        try {
            for (Auction auction: auctions) {
                if (auction.getAuctionState() == AuctionState.ACTIVE
                        && LocalDateTime.now().isAfter(auction.getEndTime())
                ) {
                    auction.close();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void start () {
        scheduler.scheduleAtFixedRate(this::executeTask, 0, 1, TimeUnit.MINUTES);
    }

    public void stop () {
        scheduler.shutdown();
    }
}

/*
OnlineAuctionSystem
    knows:
        List<User>
        List<Auction>
    does:
        addUser(User)
        removeUser(User)
        createAuction(Auction)
        bid(User, price, Auction)
        cancel(Auction)
* */

public class OnlineAuctionSystem {
    private List<User> users;
    private List<Auction> auctions;
    private AuctionScheduler scheduler;

    public OnlineAuctionSystem (AuctionScheduler scheduler) {
        this.users = new ArrayList<>();
        this.auctions = new ArrayList<>();
        this.scheduler = scheduler;
        this.scheduler.start();
    }

    public void addUser (User user) {
        users.add(user);
    }

    public void removeUser (User user) {
        users.remove(user);
    }

    public void createAuction (Auction auction) {
        auctions.add(auction);
    }

    public void bid (User bidder, double price, Auction auction) {
        auction.bid(bidder, price);
    }

    public void cancel (User user, Auction auction) {
        if (auction.getSeller().getUserId().equals(user.getUserId())) {
            auction.cancel();
        }
    }
}

/*

Bid has-a User (bidder)

Auction has-a User (seller)
Auction has-a AuctionState
Auction has-a List of Bids

AuctionScheduler has-a List of Bids

OnlineAuctionSystem has-a List of Users
OnlineAuctionSystem has-a List of Auctions

* */