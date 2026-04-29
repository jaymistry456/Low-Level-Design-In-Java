package lowleveldesigns;


/*
PricingCalculator
    knows:
        hourlyRate
    does:
        calculate(Ticket, exitTime): double

Ticket
    knows:
        vehicle
        spot
        entryTime
    does:
        nothing (data carrier)

Vehicle
    knows:
        licencePlate
        VehicleType
    does:
        nothing (data carrier)

SpotAssignmentStrategy
    knows:
        List<ParkingFloor>
    does:
        findSpot(VehicleType): ParkingSpot

ParkingSpot
    knows:
        SpotType
        isOccupied
    does:
        reserve()
        release()

ParkingFloor
    knows:
        List<ParkingSpot>
        floorNumber
    does:
        findAvailableSpot(SpotType): ParkingSpot

ParkingLot
    knows:
        List<ParkingFloor>
        List<Ticket>
        SpotAssignmentStrategy
        PricingCalculator
    does:
        parkVehicle(Vehicle): Ticket
        unparkVehicle(Ticket): double
* */


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

enum VehicleType {
    MOTORCYCLE,
    CAR,
    TRUCK
}

enum SpotType {
    SMALL,
    MEDIUM,
    LARGE
}

class Ticket {
    private Vehicle vehicle;
    private ParkingSpot parkingSpot;
    private LocalDateTime entryTime;

    public Ticket(Vehicle vehicle, ParkingSpot parkingSpot) {
        this.vehicle = vehicle;
        this.parkingSpot = parkingSpot;
        this.entryTime = LocalDateTime.now();
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ParkingSpot getParkingSpot() {
        return parkingSpot;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }
}

class PricingCalculator {
    private final double hourlyRate;

    public PricingCalculator(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public double calculate(Ticket ticket, LocalDateTime exitTime) {
        long hours = Math.max(1, Duration.between(ticket.getEntryTime(), exitTime).toHours());

        return hours * hourlyRate;
    }
}

class Vehicle {
    private String licensePlate;
    private VehicleType vehicleType;

    public Vehicle(String licensePlate, VehicleType vehicleType) {
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
    }

    public String getLicencePlate() {
        return licensePlate;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }
}

class ParkingSpot {
    private SpotType spotType;
    private boolean isOccupied;

    public ParkingSpot(SpotType spotType) {
        this.spotType = spotType;
        isOccupied = false;
    }

    public SpotType getSpotType() {
        return spotType;
    }

    public boolean isAvailable() {
        return !isOccupied;
    }

    public void reserve() {
        isOccupied = true;
    }

    public void release() {
        isOccupied = false;
    }
}

class SpotAssignmentStrategy {
    private List<ParkingFloor> parkingFloors;
    private Map<VehicleType, SpotType> map;


    public SpotAssignmentStrategy(List<ParkingFloor> parkingFloors) {
        this.parkingFloors = parkingFloors;

        map = new HashMap<>();
        map.put(VehicleType.MOTORCYCLE, SpotType.SMALL);
        map.put(VehicleType.CAR, SpotType.MEDIUM);
        map.put(VehicleType.TRUCK, SpotType.LARGE);
    }

    public ParkingSpot findSpot(VehicleType vehicleType) {
        for(ParkingFloor parkingFloor: parkingFloors) {
            ParkingSpot parkingSpot = parkingFloor.findAvailableSpot(map.get(vehicleType));

            if(parkingSpot != null) {
                return parkingSpot;
            }
        }

        return null;
    }
}

class ParkingFloor {
    private int floorNumber;
    private List<ParkingSpot> parkingSpots;

    public ParkingFloor(int floorNumber, List<ParkingSpot> parkingSpots) {
        this.floorNumber = floorNumber;
        this.parkingSpots = parkingSpots;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public ParkingSpot findAvailableSpot(SpotType spotType) {
        for(ParkingSpot spot: parkingSpots) {
            if(spot.getSpotType() == spotType && spot.isAvailable()) {
                spot.reserve();
                return spot;
            }
        }

        return null;
    }
}

class ParkingLot {
    private List<ParkingFloor> parkingFloors;
    private SpotAssignmentStrategy spotAssignmentStrategy;
    private PricingCalculator pricingCalculator;
    private List<Ticket> tickets = new ArrayList<>();   // internal state, NOT injected

    public ParkingLot(List<ParkingFloor> parkingFloors, SpotAssignmentStrategy spotAssignmentStrategy, PricingCalculator pricingCalculator) {
        this.parkingFloors = parkingFloors;
        this.spotAssignmentStrategy = spotAssignmentStrategy;
        this.pricingCalculator = pricingCalculator;
    }

    public Ticket parkVehicle(Vehicle vehicle) {
        ParkingSpot parkingSpot = spotAssignmentStrategy.findSpot(vehicle.getVehicleType());

        if(parkingSpot == null) {
            return null;
        }
        Ticket ticket = new Ticket(vehicle, parkingSpot);
        tickets.add(ticket);

        return ticket;
    }

    public double unparkVehicle(Ticket ticket) {
        if(!tickets.contains(ticket)) {
            throw new IllegalArgumentException("Invalid ticket");
        }

        ticket.getParkingSpot().release();
        tickets.remove(ticket);
        return pricingCalculator.calculate(ticket, LocalDateTime.now());
    }

    public static void main(String[] args) {
        List<ParkingFloor> parkingFloors = new ArrayList<>();
        for(int i = 1; i <= 10; i++) {
            List<ParkingSpot> parkingSpots = new ArrayList<>();
            for(int j = 0; j < 10; j++) {
                parkingSpots.add(new ParkingSpot(SpotType.SMALL));
                parkingSpots.add(new ParkingSpot(SpotType.MEDIUM));
                parkingSpots.add(new ParkingSpot(SpotType.LARGE));
            }
            parkingFloors.add(new ParkingFloor(i, parkingSpots));
        }
        SpotAssignmentStrategy spotAssignmentStrategy = new SpotAssignmentStrategy(parkingFloors);
        PricingCalculator pricingCalculator = new PricingCalculator(10);

        ParkingLot parkingLot = new ParkingLot(parkingFloors, spotAssignmentStrategy, pricingCalculator);

        Vehicle car = new Vehicle("1234", VehicleType.CAR);
        System.out.println("Parking car with license plate: " + car.getLicencePlate());
        Ticket ticket = parkingLot.parkVehicle(car);
        double cost = parkingLot.unparkVehicle(ticket);
        System.out.println("Unparking car, cost: " + cost);
    }
}

