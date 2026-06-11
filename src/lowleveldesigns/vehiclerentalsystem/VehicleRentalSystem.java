package lowleveldesigns.vehiclerentalsystem;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/*
enums
* */
enum RentalStatus {
    RESERVED,
    ACTIVE,
    COMPLETED,
    CANCELLED;
}

enum LicenseType {
    STANDARD,
    MOTORCYCLE,
    COMMERCIAL;
}

enum VehicleType {
    CAR,
    BIKE,
    TRUCK;
}

/*
classes
* */
/*
Customer
Vehicle (interface)
Car
Bike
Truck
VehicleFactory
Rental
VehicleFilter
VehicleRentalSystem
* */

/*
Customer
    knows:
        customerId
        name
        email
        phoneNo
        Set<LicenseType>
    does:
        addLicenseType(LicenseType)
        removeLicenseType(LicenseType)
* */
class Customer {
    private String customerId;
    private String name;
    private String email;
    private String phoneNo;
    private Set<LicenseType> licenseTypes;

    public Customer(
            String customerId,
            String name,
            String email,
            String phoneNo,
            Set<LicenseType> licenseTypes
    ) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.phoneNo = phoneNo;
        this.licenseTypes = licenseTypes;
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

    public Set<LicenseType> getLicenseTypes() {
        return licenseTypes;
    }

    public void addLicenseType(LicenseType licenseType) {
        licenseTypes.add(licenseType);
    }

    public void removeLicenseType(LicenseType licenseType) {
        licenseTypes.remove(licenseType);
    }
}

/*
Vehicle
    knows:
    does:
        isAvailable() -> boolean
        setAvailability(boolean)
        getVehicleType() -> VehicleType
        getLicensePlate() -> String
        getNoOfSeats() -> int
        getDailyRate() -> double
        getLicenseRequired() -> LicenseType
* */
interface Vehicle {
    boolean isAvailable();
    void setAvailability(boolean availability);
    VehicleType getVehicleType();
    int getNoOfSeats();
    void setLicensePlate(String licensePlate);
    String getLicensePlate();
    double getDailyRate();
    LicenseType getLicenseType();
}

/*
Car
    knows:
        noOfSeats
        dailyRate
        licensePlate
        VehicleType
        LicenseType
        available
    does:
        isAvailable() -> boolean
        setAvailability(boolean)
        getVehicleType() -> VehicleType
        getLicensePlate() -> String
        getNoOfSeats() -> int
        getDailyRate() -> double
        getLicenseRequired() -> LicenseType
* */
class Car implements Vehicle {
    private int noOfSeats;
    private double dailyRate;
    private String licensePlate;
    private VehicleType vehicleType;
    private LicenseType licenseType;
    private boolean available;

    public Car(
            int noOfSeats,
            double dailyRate,
            VehicleType vehicleType,
            LicenseType licenseType
    ) {
        this.noOfSeats = noOfSeats;
        this.dailyRate = dailyRate;
        this.vehicleType = vehicleType;
        this.licenseType = licenseType;
        this.available = true;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public void setAvailability(boolean available) {
        this.available = available;
    }

    @Override
    public VehicleType getVehicleType() {
        return vehicleType;
    }

    @Override
    public int getNoOfSeats() {
        return noOfSeats;
    }

    @Override
    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    @Override
    public String getLicensePlate() {
        return licensePlate;
    }

    @Override
    public double getDailyRate() {
        return dailyRate;
    }

    @Override
    public LicenseType getLicenseType() {
        return licenseType;
    }
}

/*
Bike
    knows:
        noOfSeats
        dailyRate
        licensePlate
        VehicleType
        LicenseType
        available
    does:
        isAvailable() -> boolean
        setAvailability(boolean)
        getVehicleType() -> VehicleType
        getLicensePlate() -> String
        getNoOfSeats() -> int
        getDailyRate() -> double
        getLicenseRequired() -> LicenseType
* */
class Bike implements Vehicle {
    private int noOfSeats;
    private double dailyRate;
    private String licensePlate;
    private VehicleType vehicleType;
    private LicenseType licenseType;
    private boolean available;

    public Bike(
            int noOfSeats,
            double dailyRate,
            VehicleType vehicleType,
            LicenseType licenseType
    ) {
        this.noOfSeats = noOfSeats;
        this.dailyRate = dailyRate;
        this.vehicleType = vehicleType;
        this.licenseType = licenseType;
        this.available = true;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public void setAvailability(boolean available) {
        this.available = available;
    }

    @Override
    public VehicleType getVehicleType() {
        return vehicleType;
    }

    @Override
    public int getNoOfSeats() {
        return noOfSeats;
    }

    @Override
    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    @Override
    public String getLicensePlate() {
        return licensePlate;
    }

    @Override
    public double getDailyRate() {
        return dailyRate;
    }

    @Override
    public LicenseType getLicenseType() {
        return licenseType;
    }
}

/*
Truck
    knows:
        noOfSeats
        dailyRate
        licensePlate
        VehicleType
        LicenseType
        cargoCapacity
        available
    does:
        isAvailable() -> boolean
        setAvailability(boolean)
        getVehicleType() -> VehicleType
        getLicensePlate() -> String
        getNoOfSeats() -> int
        getDailyRate() -> double
        getLicenseRequired() -> LicenseType
        getCargoCapacity() -> int
* */
class Truck implements Vehicle {
    private int noOfSeats;
    private double dailyRate;
    private String licensePlate;
    private VehicleType vehicleType;
    private LicenseType licenseType;
    private boolean available;
    private int cargoCapacity;

    public Truck(
            int noOfSeats,
            double dailyRate,
            VehicleType vehicleType,
            LicenseType licenseType
    ) {
        this.noOfSeats = noOfSeats;
        this.dailyRate = dailyRate;
        this.vehicleType = vehicleType;
        this.licenseType = licenseType;
        this.available = true;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public void setAvailability(boolean available) {
        this.available = available;
    }

    @Override
    public VehicleType getVehicleType() {
        return vehicleType;
    }

    @Override
    public int getNoOfSeats() {
        return noOfSeats;
    }

    @Override
    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }


    @Override
    public String getLicensePlate() {
        return licensePlate;
    }

    @Override
    public double getDailyRate() {
        return dailyRate;
    }

    @Override
    public LicenseType getLicenseType() {
        return licenseType;
    }

    public void setCargoCapacity(int cargoCapacity) {
        this.cargoCapacity = cargoCapacity;
    }

    public int getCargoCapacity() {
        return cargoCapacity;
    }
}

/*
VehicleFactory
    knows:
    does:
        getVehicle(VehicleType) -> Vehicle
* */
class VehicleFactory {
    public static Vehicle getVehicle(VehicleType vehicleType) {
        return switch (vehicleType) {
            case CAR -> new Car(4, 10.00, VehicleType.CAR, LicenseType.STANDARD);
            case BIKE -> new Bike(2, 12.00, VehicleType.BIKE, LicenseType.MOTORCYCLE);
            case TRUCK -> new Truck(2, 15.00, VehicleType.TRUCK, LicenseType.COMMERCIAL);
        };
    }
}

/*
Rental
    knows:
        Customer
        Vehicle
        startTime
        endTime
        totalCharge
        RentStatus
    does:
        updateStatus(RentStatus)
        calculateTotalCharge() -> double
* */
class Rental {
    private Customer customer;
    private Vehicle vehicle;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double totalCharge;
    private RentalStatus rentalStatus;

    public Rental(
            Customer customer,
            Vehicle vehicle,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        this.customer = customer;
        this.vehicle = vehicle;
        this.startTime = startTime;
        this.endTime = endTime;
        this.rentalStatus = RentalStatus.RESERVED;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public double getTotalCharge() {
        return totalCharge;
    }

    public RentalStatus getRentalStatus() {
        return rentalStatus;
    }

    public void updateStatus(RentalStatus rentalStatus) {
        this.rentalStatus = rentalStatus;
    }

    public double calculateTotalCharge() {
        long days = Duration.between(startTime, endTime).toDays();
        if(days == 0) days = 1;

        double totalCharge = days * vehicle.getDailyRate();
        if(LocalDateTime.now().isAfter(endTime)){
            long lateDays = Duration.between(endTime, LocalDateTime.now()).toDays() + 1;
            totalCharge += lateDays * vehicle.getDailyRate() * 1.5;
        }

        this.totalCharge = totalCharge;

        return totalCharge;
    }
}

/*
VehicleFilter
    knows:
        VehicleType
        LicenseType
        dailyRate

        VehicleFilterBuilder
            knows:
                VehicleType
                LicenseType
                dailyRate
            does:
                withVehicleType(VehicleType) -> VehicleFilterBuilder
                withLicenseType(LicenseType) -> VehicleFilterBuilder
                withDailyRate(double) -> VehicleFilterBuilder
                build() -> VehicleFilter

         does:
            apply(List<Vehicle>) -> List<Vehicle>
* */
class VehicleFilter {
    private VehicleType vehicleType;
    private LicenseType licenseType;
    private double dailyRate;

    private VehicleFilter(VehicleFilterBuilder builder) {
        this.vehicleType = builder.vehicleType;
        this.licenseType = builder.licenseType;
        this.dailyRate = builder.dailyRate;
    }

    public List<Vehicle> apply(List<Vehicle> vehicles) {
        return vehicles.stream()
                .filter(v -> (vehicleType == null || vehicleType == v.getVehicleType()))
                .filter(v -> (licenseType == null || licenseType.equals(v.getLicenseType())))
                .filter(v -> (dailyRate == 0.0 || dailyRate >= v.getDailyRate()))
                .toList();
    }

    public static class VehicleFilterBuilder {
        private VehicleType vehicleType;
        private LicenseType licenseType;
        private double dailyRate;

        public VehicleFilterBuilder() {
            dailyRate = 0.0;
        }

        public VehicleFilterBuilder withVehicleType(VehicleType vehicleType) {
            this.vehicleType = vehicleType;
            return this;
        }

        public VehicleFilterBuilder withLicenseType(LicenseType licenseType) {
            this.licenseType = licenseType;
            return this;
        }

        public VehicleFilterBuilder withDailyRate(double dailyRate) {
            this.dailyRate = dailyRate;
            return this;
        }

        public VehicleFilter build() {
            return new VehicleFilter(this);
        }
    }
}

/*
VehicleRentalSystem
    knows:
        List<Customer>
        List<Vehicle>
        List<Rental>
        VehicleFactory
    does:
        addCustomer(Customer)
        removeCustomer(Customer)
        addVehicle(Vehicle)
        removeVehicle(Vehicle)
        searchAvailableVehicles(VehicleFilter) -> List<Vehicle>
        reserveVehicle(Customer, Vehicle) -> Rental
        pickupVehicle(Customer, Rental)
        returnVehicle(Customer, Rental) -> double
        cancelBooking(Customer, Rental)
* */

public class VehicleRentalSystem {
    private List<Customer> customers;
    private List<Vehicle> vehicles;
    private List<Rental> rentals;
    private VehicleFactory vehicleFactory;

    public VehicleRentalSystem(VehicleFactory vehicleFactory) {
        this.customers = new ArrayList<>();
        this.vehicles = new ArrayList<>();
        this.rentals = new ArrayList<>();
        this.vehicleFactory = vehicleFactory;
    }

    public void addCustomer(Customer customer) {
        customers.add(customer);
    }

    public void removeCustomer(Customer customer) {
        customers.remove(customer);
    }

    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
    }

    public void removeVehicle(Vehicle vehicle) {
        vehicles.remove(vehicle);
    }

    public List<Vehicle> searchAvailableVehicles(VehicleFilter filter) {
        return filter.apply(vehicles);
    }

    public Rental reserveVehicle(Customer customer, Vehicle vehicle, LocalDateTime startTime, LocalDateTime endTime) {
        if(!customer.getLicenseTypes().contains(vehicle.getLicenseType())) return null;

        for(Rental rental: rentals) {
            if(rental.getVehicle().getLicensePlate().equals(vehicle.getLicensePlate())
                || (startTime.isAfter(rental.getStartTime()) && startTime.isBefore(rental.getEndTime()))
                    || (endTime.isAfter(rental.getStartTime()) && endTime.isBefore(rental.getEndTime()))
            ) {
                return null;
            }
        }
        Rental rental = new Rental(customer, vehicle, startTime, endTime);
        rentals.add(rental);
        rental.updateStatus(RentalStatus.RESERVED);

        return rental;
    }

    public void pickupVehicle(Customer customer, Rental rental) {
        if(rental.getCustomer().getCustomerId().equals(customer.getCustomerId()) && LocalDateTime.now().isAfter(rental.getStartTime()) && LocalDateTime.now().isBefore(rental.getEndTime())) {
            rental.updateStatus(RentalStatus.ACTIVE);
            rental.getVehicle().setAvailability(false);
        }
    }

    public double returnVehicle(Customer customer, Rental rental) {
        double charge = rental.calculateTotalCharge();
        rental.updateStatus(RentalStatus.COMPLETED);
        rental.getVehicle().setAvailability(true);
        return charge;
    }

    public void cancelBooking(Customer customer, Rental rental) {
        if(rental.getCustomer().getCustomerId().equals(customer.getCustomerId()) && rental.getRentalStatus() == RentalStatus.RESERVED) {
            rental.updateStatus(RentalStatus.CANCELLED);
        }
    }
}

/*

Customer has-a List of LicenseType

Car is-a Vehicle
Car has-a VehicleType
Car has-a LicenseType

Bike is-a Vehicle
Bike has-a VehicleType
Bike has-a LicenseType

Truck is-a Vehicle
Truck has-a VehicleType
Truck has-a LicenseType

Rental has-a Customer
Rental has-a RentalStatus

VehicleRentalSystem has-a List of Customer
VehicleRentalSystem has-a List of Vehicle
VehicleRentalSystem has-a List of Rental
VehicleRentalSystem has-a VehicleFactory

* */