package lowleveldesigns.airlinereservationsystem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/*
enums
* */
enum SeatType {
    ECONOMY(10),
    BUSINESS(20),
    FIRST_CLASS(30);

    private final double price;

    SeatType(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }
}

enum SeatStatus {
    AVAILABLE,
    LOCKED,
    BOOKED;
}

enum FlightStatus {
    SCHEDULED,
    BOARDING,
    IN_FLIGHT,
    LANDED,
    CANCELLED;
}

enum BookingStatus {
    IN_PROGRESS,
    BOOKED,
    CANCELLED,
    EXPIRED;
}

/*
classes
* */
/*
Customer
Passenger
Seat
Flight
FlightFilter
Booking
AirlineReservationSystem
*/

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

    public String phoneNo() {
        return phoneNo;
    }
}

/*
Passenger
    knows:
        name
        passportNo
        dateOfBirth
    does:
        nothing (data carrier)
* */
record Passenger(String name, String passportNo, LocalDate dateOfBirth) {}

/*
Seat
    knows:
        seatId
        SeatType
        SeatStatus
        ReentrantLock
    does:
        updateStatus(SeatStatus)
        book()
        cancel()
* */
class Seat {
    private String seatNo;
    private SeatType seatType;
    private SeatStatus seatStatus;
    private LocalDateTime lockExpiry;
    private final ReentrantLock lock = new ReentrantLock();

    public Seat(String seatNo, SeatType seatType) {
        this.seatNo = seatNo;
        this.seatType = seatType;
        this.seatStatus = SeatStatus.AVAILABLE;
    }

    public void updateStatus(SeatStatus seatStatus) {
        this.seatStatus = seatStatus;
    }

    private boolean isLockExpired() {
        return lockExpiry != null && LocalDateTime.now().isAfter(lockExpiry);
    }

    public boolean book() {
        lock.lock();
        try {
            if(seatStatus == SeatStatus.AVAILABLE || isLockExpired()) {
                seatStatus = SeatStatus.LOCKED;
                lockExpiry = LocalDateTime.now().plusMinutes(15);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public boolean cancel() {
        if(seatStatus == SeatStatus.BOOKED) {
            seatStatus = SeatStatus.AVAILABLE;
            lockExpiry = null;
            return true;
        }
        return false;
    }

    public String getSeatNo() {
        return seatNo;
    }

    public SeatType getSeatType() {
        return seatType;
    }

    public SeatStatus getSeatStatus() {
        return seatStatus;
    }
}

/*
Flight
    knows:
        flightNo
        flightName
        airline
        List<Seat>
        date
        startTime
        endTime
        source
        destination
        noOfStops
        FlightStatus
    does:
        updateStatus(FlightStatus)
        bookSeat(Seat) -> boolean
        cancelSeat(Seat) -> boolean
* */
class Flight {
    private String flightNo;
    private String flightName;
    private String airline;
    private List<Seat> seats;
    private LocalDate date;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String source;
    private String destination;
    private int noOfStops;
    private FlightStatus flightStatus;

    public Flight(
            String flightNo,
            String flightName,
            String airline,
            LocalDate date,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String source,
            String destination,
            int noOfStops
    ) {
        this.flightNo = flightNo;
        this.flightName = flightName;
        this.airline = airline;
        this.seats = new ArrayList<>();
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.source = source;
        this.destination = destination;
        this.noOfStops = noOfStops;
        this.flightStatus = FlightStatus.SCHEDULED;
    }

    public String getFlightNo() {
        return flightNo;
    }

    public String getFlightName() {
        return flightName;
    }

    public String getAirline() {
        return airline;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public int getNoOfStops() {
        return noOfStops;
    }

    public FlightStatus getFlightStatus() {
        return flightStatus;
    }

    public void updateStatus(FlightStatus flightStatus) {
        this.flightStatus = flightStatus;
    }

    public boolean isWithinPrice(double price) {
        for(Seat seat: seats) {
            if(seat.getSeatType().getPrice() <= price) {
                return true;
            }
        }
        return false;
    }

    public boolean bookSeat(Seat seat) {
        if(flightStatus != FlightStatus.SCHEDULED) return false;
        if(!seats.contains(seat)) return false;
        return seat.book();
    }

    public boolean is24HoursBeforeDepartureTime() {
        return LocalDateTime.now().isBefore(startTime.minusHours(24));
    }

    public boolean cancelSeat(Seat seat) {
        if(!seats.contains(seat)) return false;
        if(!is24HoursBeforeDepartureTime()) return false;
        return seat.cancel();
    }
}

/*
FlightFilter
    knows:
        source
        destination
        LocalDate
        price
        noOfStops
        airline
        apply(List<Flight>) -> List<Flight>
        FlightFilterBuilder:
            knows:
                source
                destination
                LocalDate
                price
                noOfStops
                airline
            does:
                withPrice(double) -> FlightFilterBuilder
                withNoOfStops(int) -> FlightFilterBuilder
                withAirline(String) -> FlightFilterBuilder
                build() -> FlightFilter
* */
class FlightFilter {
    private String source;
    private String destination;
    private LocalDate date;
    private Double price;
    private Integer noOfStops;
    private String airline;

    private FlightFilter(FlightFilterBuilder builder) {
        this.source = builder.source;
        this.destination = builder.destination;
        this.date = builder.date;
        this.price = builder.price;
        this.noOfStops = builder.noOfStops;
        this.airline = builder.airline;
    }

    public List<Flight> apply(List<Flight> flights) {
        return flights.stream()
                .filter(f -> source.equals(f.getSource()))
                .filter(f -> destination.equals(f.getDestination()))
                .filter(f -> date.isEqual(f.getDate()))
                .filter(f -> (price == null || f.isWithinPrice(price)))
                .filter(f -> (noOfStops == null || noOfStops >= f.getNoOfStops()))
                .filter(f -> (airline == null || airline.equals(f.getAirline())))
                .toList();
    }

    public static class FlightFilterBuilder {
        private String source;
        private String destination;
        private LocalDate date;
        private Double price;
        private Integer noOfStops;
        private String airline;

        public FlightFilterBuilder(String source, String destination, LocalDate date) {
            this.source = source;
            this.destination = destination;
            this.date = date;
        }

        public FlightFilterBuilder withPrice(Double price) {
            this.price = price;
            return this;
        }

        public FlightFilterBuilder withNoOfStops(Integer noOfStops) {
            this.noOfStops = noOfStops;
            return this;
        }

        public FlightFilterBuilder withAirline(String airline) {
            this.airline = airline;
            return this;
        }

        public FlightFilter build() {
            return new FlightFilter(this);
        }
    }
}

/*
Booking
    knows:
        bookingId
        Customer
        Passenger
        Flight
        Seat
        BookingStatus
        timestamp
    does:
        updateStatus(BookingStatus)
* */
class Booking {
    private Customer customer;
    private Passenger passenger;
    private Flight flight;
    private Seat seat;
    private BookingStatus bookingStatus;
    private LocalDateTime timestamp;

    public Booking(
            Customer customer,
            Passenger passenger,
            Flight flight,
            Seat seat
    ) {
        this.customer = customer;
        this.passenger = passenger;
        this.flight = flight;
        this.seat = seat;
        this.bookingStatus = BookingStatus.IN_PROGRESS;
        this.timestamp = LocalDateTime.now();
    }

    public Customer getCustomer() {
        return customer;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public Flight getFlight() {
        return flight;
    }

    public Seat getSeat() {
        return seat;
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void updateStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public boolean cancel() {
        if(flight.cancelSeat(seat)) {
            bookingStatus = BookingStatus.CANCELLED;
            return true;
        }
        return false;
    }
}

/*
AirlineReservationSystem
    knows:
        List<Customer>
        List<Flight>
        List<Booking>
    does:
        addCustomer(Customer)
        removeCustomer(Customer)
        addFlight(Flight)
        removeFlight(Flight)
        searchFlights(FlightFilter) -> List<Flight>
        book(Customer, Passenger, Flight, Seat) -> boolean
        cancel(Customer, Booking) -> boolean
* */

public class AirlineReservationSystem {
    private List<Customer> customers;
    private List<Flight> flights;
    private List<Booking> bookings;

    public AirlineReservationSystem() {
        this.customers = new ArrayList<>();
        this.flights = new ArrayList<>();
        this.bookings = new ArrayList<>();
    }

    public void addCustomer(Customer customer) {
        customers.add(customer);
    }

    public void removeCustomer(Customer customer) {
        customers.remove(customer);
    }

    public void addFlight(Flight flight) {
        flights.add(flight);
    }

    public void removeFlight(Flight flight) {
        flights.remove(flight);
    }

    public List<Flight> searchFlights(FlightFilter flightFilter) {
        return flightFilter.apply(flights);
    }

    public boolean book(Customer customer, Passenger passenger, Flight flight, Seat seat) {
        if(flight.bookSeat(seat)) {
            Booking booking = new Booking(customer, passenger, flight, seat);
            booking.updateStatus(BookingStatus.BOOKED);
            bookings.add(booking);
            return true;
        }
        return false;
    }

    public boolean cancel(Customer customer, Booking booking) {
        return booking.cancel();
    }
}

/*

Seat has-a SeatStatus

Flight has-a List of Seat
Flight has-a FlightStatus

FlightFilter has-a FlightFilterBuilder

Booking has-a Passenger
Booking has-a Flight
Booking has-a Seat
Booking has-a BookingStatus

AirlineReservationSystem has-a List of Customers
AirlineReservationSystem has-a List of Flights
AirlineReservationSystem has-a List of Bookings

* */