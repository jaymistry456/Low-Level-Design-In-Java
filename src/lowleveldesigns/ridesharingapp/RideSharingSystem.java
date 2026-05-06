package lowleveldesigns.ridesharingapp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

// enums
// RideType
enum RideType {
    ECONOMY(10),
    PREMIUM(15),
    SUV(20);

    private final double price;

    RideType(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }
}

// RideStatus
enum RideStatus {
    SEARCHING,
    ACCEPTED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

// Location
/*
knows:
    latitude
    longitude
does:
    distanceTo() -> double
* */
record Location (double latitude, double longitude) {
    public double distanceTo(Location other) {
        return Math.sqrt(
                Math.pow(this.latitude - other.latitude, 2) +
                Math.pow(this.longitude - other.longitude, 2)
        );
    }
}

// Customers
/*
knows:
    customerId
    customerName
    customerEmail
    customerPhoneNumber
    customerCreditCardInfo
does:
    nothing (data carrier)
* */
class Customer {
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhoneEmail;
    private int customerPhoneNumber;
    private int CreditCardInfor;

    public Customer(String customerId, String customerName, String customerEmail, String customerPhoneEmail, int customerPhoneNumber, int creditCardInfor) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhoneEmail = customerPhoneEmail;
        this.customerPhoneNumber = customerPhoneNumber;
        CreditCardInfor = creditCardInfor;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getCustomerPhoneEmail() {
        return customerPhoneEmail;
    }

    public int getCustomerPhoneNumber() {
        return customerPhoneNumber;
    }

    public int getCreditCardInfor() {
        return CreditCardInfor;
    }
}

// Driver
/*
knows:
    driverId
    driverName
    driverEmail
    driverPhoneNumber
    driverRating
    driverLocation
does:
    nothing (data carrier)
* */
class Driver {
    private String driverId;
    private String driverName;
    private String driverEmail;
    private String driverPhoneNumber;
    private double riderRating;
    private int noOfRatings;
    private Location driverLocation;
    private boolean isAvailable;

    public Driver(String driverId, String driverName, String driverEmail, String driverPhoneNumber, double riderRating, Location driverLocation) {
        this.driverId = driverId;
        this.driverName = driverName;
        this.driverEmail = driverEmail;
        this.driverPhoneNumber = driverPhoneNumber;
        this.riderRating = riderRating;
        this.noOfRatings = 0;
        this.driverLocation = driverLocation;
        this.isAvailable = true;
    }

    public String getDriverId() {
        return driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getDriverEmail() {
        return driverEmail;
    }

    public String getDriverPhoneNumber() {
        return driverPhoneNumber;
    }

    public double getRiderRating() {
        return riderRating;
    }

    public Location getDriverLocation() {
        return driverLocation;
    }

    public boolean isAvailable() {
        return this.isAvailable;
    }


    public void setRiderRating(double newRating) {
        this.riderRating = (riderRating * noOfRatings + newRating) / (noOfRatings + 1);
        noOfRatings++;
    }

    public void setDriverLocation(Location driverLocation) {
        this.driverLocation = driverLocation;
    }

    public void available() {
        isAvailable = true;
    }

    public void busy() {
        isAvailable = false;
    }
}



// Ride
/*
knows:
    Customer
    Driver
    RideType
    RideStatus
    sourceLocation
    destinationLocation
    price
does:
    updateStatus(RideStatus)
    getStatus() -> RideStatus
    getDistance() -> double
* */
class Ride {
    private Customer customer;
    private Driver driver;
    private RideType rideType;
    private RideStatus rideStatus;
    private Location srcLocation;
    private Location destLocation;
    private double price;

    private final ReentrantLock lock = new ReentrantLock();

    public Ride(Customer customer, RideType rideType, Location srcLocation, Location destLocation) {
        this.customer = customer;
        this.rideType = rideType;
        this.rideStatus = RideStatus.SEARCHING;
        this.srcLocation = srcLocation;
        this.destLocation = destLocation;
    }

    public boolean tryAccept(Driver driver) {
        lock.lock();
        try {
            if(rideStatus == RideStatus.SEARCHING) {
                this.driver = driver;
                this.rideStatus = RideStatus.ACCEPTED;
                return true;
            }
            return false;
        } finally {
          lock.unlock();
        }
    }

    public Customer getCustomer() {
        return customer;
    }

    public Driver getDriver() {
        return driver;
    }

    public RideType getRideType() {
        return rideType;
    }

    public RideStatus getRideStatus() {
        return rideStatus;
    }

    public Location getSrcLocation() {
        return srcLocation;
    }

    public Location getDestLocation() {
        return destLocation;
    }

    public double getPrice() {
        return price;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public void setRideType(RideType rideType) {
        this.rideType = rideType;
    }

    public void updateStatus(RideStatus rideStatus) {
        this.rideStatus = rideStatus;
    }

    public void setSrcLocation(Location srcLocation) {
        this.srcLocation = srcLocation;
    }

    public void setDestLocation(Location destLocation) {
        this.destLocation = destLocation;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}

// RideMatchingStrategy
/*
knows:
    List<Driver>
does:
    findNearbyDrivers(sourceLocation, searchRadius) -> List<Driver>
* */
class RideMatchingStrategy {
    private List<Driver> drivers;

    public RideMatchingStrategy(List<Driver> drivers) {
        this.drivers = drivers;
    }

    public List<Driver> findNearbyDrivers(Location srcLocation, double searchRadius) {
        return drivers.stream()
                .filter(
                        driver ->
                                driver.getDriverLocation().distanceTo(srcLocation) <= searchRadius
                                && driver.isAvailable()
                ).toList();
    }
}

// Pricing Calculator
/*
knows:
    nothing
does:
    calculate(distance, RideType) -> double
* */
class PricingCalculator {
    public double calculate(double distance, RideType rideType) {
        return rideType.getPrice() * distance;
    }
}

// RideSharingSystem
/*
knows:
    List<Customer>
    List<Driver>
    List<Ride>
    RideMatchingStrategy
    PricingCalculator
does:
    addCustomer(Customer)
    removeCustomer(Customer)
    addDriver(Driver)
    removeDriver(Driver)
    bookRide(Customer, source, destination)
    cancelRide(Ride)
    acceptRide(Driver, Ride)
    completeRide(Ride)
    rateDriver(Customer, Driver, rating)
* */
class RideSharingSystem {
    private List<Customer> customers;
    private List<Driver> drivers;
    private List<Ride> rides;
    private RideMatchingStrategy rideMatchingStrategy;
    private PricingCalculator pricingCalculator;

    private final double SEARCH_RADIUS = 10;

    public RideSharingSystem(RideMatchingStrategy rideMatchingStrategy, PricingCalculator pricingCalculator) {
        this.customers = new ArrayList<>();
        this.drivers = new ArrayList<>();
        this.rides = new ArrayList<>();
        this.rideMatchingStrategy = rideMatchingStrategy;
        this.pricingCalculator = pricingCalculator;
    }

    public void addCustomer(Customer customer) {
        customers.add(customer);
    }

    public void removeCustomer(Customer customer) {
        customers.remove(customer);
    }

    public void addDriver(Driver driver) {
        drivers.add(driver);
    }

    public void removeDriver(Driver driver) {
        drivers.remove(driver);
    }

    public Ride bookRide(Customer customer, RideType rideType, Location srcLocation, Location destLocation) {
        List<Driver> drivers = rideMatchingStrategy.findNearbyDrivers(srcLocation, SEARCH_RADIUS);
        if(drivers.isEmpty()) return null;

        Ride newRide = new Ride(customer,rideType, srcLocation, destLocation);
        rides.add(newRide);

        return newRide;
    }

    public void cancelRide(Ride ride) {
        ride.updateStatus(RideStatus.CANCELLED);
    }

    public void acceptRide(Driver driver, Ride ride) {
        if(ride.tryAccept(driver)) {
            driver.busy();
        }
    }

    public void completeRide(Ride ride) {
        ride.updateStatus(RideStatus.COMPLETED);
        double price = pricingCalculator.calculate(ride.getDestLocation().distanceTo(ride.getSrcLocation()), ride.getRideType());
        ride.setPrice(price);
        ride.getDriver().available();
    }

    public void rateDriver(Driver driver, int rating) {
        driver.setRiderRating(rating);
    }
}

/*
RideSharingSystem has-a List of Customers
RideSharingSystem has-a List of Drivers
RideSharingSystem has-a List of Rides
RideSharingSystem has-a List of RideMatchingStrategy
RideSharingSystem has-a List of PricingCalculator

RideMatchingStrategy has-a List of Drivers

Ride has-a Customer and a Driver
* */
