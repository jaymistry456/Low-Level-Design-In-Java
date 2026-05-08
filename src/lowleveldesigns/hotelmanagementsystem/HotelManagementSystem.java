package lowleveldesigns.hotelmanagementsystem;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/*
enums
* */
enum RoomStatus {
    AVAILABLE,
    BOOKED,
    UNDER_MAINTAINENCE;
}

enum BookingStatus {
    CONFIRMED,
    CANCELLED;
}

enum RoomType {
    SINGLE(100),
    DOUBLE(200),
    SUITE(300);

    private final double price;

    RoomType(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }
}

/*
classes
* */
// Customer
// Room
// Booking
// PricingCalculator
// HotelSearchStrategy
// Hotel
// HotelManagementSystem


/*
Customer
    knows:
        customerId
        customerName
        customerEmail
        customerPhoneNo
        customerCreditCardInfo
    does:
        nothing (data carrier)
* */
class Customer {
    private String customerId;
    private String customerName;
    private String customerEmail;
    private int customerPhoneNo;
    private String customerCreditCardInfo;

    public Customer(
            String customerId,
            String customerName,
            String customerEmail,
            int customerPhoneNo,
            String customerCreditCardInfo
    ) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhoneNo = customerPhoneNo;
        this.customerCreditCardInfo = customerCreditCardInfo;
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

    public int getCustomerPhoneNo() {
        return customerPhoneNo;
    }

    public String getCustomerCreditCardInfo() {
        return customerCreditCardInfo;
    }
}

/*
Room
    knows:
        roomNo
        RoomType
        RoomStatus
        lock
    does:
        updateStatus(status)
        tryBook()
        release()
* */
class Room {
    private String roomNo;
    private RoomType roomType;
    private RoomStatus roomStatus;

    private final ReentrantLock lock = new ReentrantLock();

    public Room(String roomNo, RoomType roomType) {
        this.roomNo = roomNo;
        this.roomType = roomType;
        this.roomStatus = RoomStatus.AVAILABLE;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public RoomStatus getRoomStatus() {
        return roomStatus;
    }

    public void updateStatus(RoomStatus roomStatus) {
        this.roomStatus = roomStatus;
    }

    public boolean tryBook() {
        lock.lock();
        try {
            if(getRoomStatus() != RoomStatus.AVAILABLE) {
                return false;
            }
            updateStatus(RoomStatus.BOOKED);
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void release() {
        updateStatus(RoomStatus.AVAILABLE);
    }
}

/*
Booking
    knows:
        bookingId
        Customer
        Hotel
        Room
        amount
        startTime
        endTime
        BookingStatus
    does:
        updateStatus(BookingStatus)
* */
class Booking {
    private String bookingId;
    private Customer customer;
    private Hotel hotel;
    private Room room;
    private double amount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatus bookingStatus;

    public Booking(
            String bookingId,
            Customer customer,
            Hotel hotel,
            Room room,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        this.bookingId = bookingId;
        this.customer = customer;
        this.hotel = hotel;
        this.room = room;
        this.startTime = startTime;
        this.endTime = endTime;
        this.bookingStatus = BookingStatus.CONFIRMED;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getBookingId() {
        return bookingId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public Room getRoom() {
        return room;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public void updateStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
    }
}

/*
Hotel
    knows:
        hotelId
        hotelName
        city
        List<Room>
        noOfRatings
        rating
    does:
        addRoom(Room)
        removeRoom(Room)
        rateHotel(int rating)
* */
class Hotel {
    private String hotelId;
    private String hotelName;
    private String city;
    private List<Room> rooms;
    private int noOfRatings;
    private double rating;

    public Hotel(String hotelId, String hotelName, String city) {
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.city = city;
        this.rooms = new ArrayList<>();
        this.noOfRatings = 0;
        this.rating = 0;
    }

    public String getHotelId() {
        return hotelId;
    }

    public String getHotelName() {
        return hotelName;
    }

    public String getCity() {
        return city;
    }

    public double getRating() {
        return rating;
    }

    public void addRoom(Room room) {
        this.rooms.add(room);
    }

    public void removeRoom(Room room) {
        this.rooms.remove(room);
    }

    public void rateHotel(int newRating) {
        rating = (rating * noOfRatings + newRating) / (noOfRatings + 1);
        noOfRatings++;
    }

    public boolean hasRoomTypeAvailable(RoomType roomType) {
        for(Room room: rooms) {
            if(room.getRoomType() == roomType && room.getRoomStatus() == RoomStatus.AVAILABLE) {
                return true;
            }
        }
        return false;
    }

}

/*
PricingCalculator
    knows:
        nothing
    does:
        calculate(Booking) -> double
* */
class PricingCalculator {
    public double calculate(Booking booking) {
        long nights = ChronoUnit.DAYS.between(
                booking.getStartTime().toLocalDate(),
                booking.getEndTime().toLocalDate()
        );
        double amount = booking.getRoom().getRoomType().getPrice() * nights;
        booking.setAmount(amount);
        return amount;
    }
}

/*
HotelSearchStrategy
    knows:
        List<Hotel>
    does:
        searchHotel(city, RoomType) -> List<Hotel>
* */
class HotelSearchStrategy {
    private List<Hotel> hotels;

    public HotelSearchStrategy(List<Hotel> hotels) {
        this.hotels = hotels;
    }

    public List<Hotel> searchHotel(String city, RoomType roomType) {
        return hotels
                .stream()
                .filter(hotel -> hotel.getCity().equals(city) && hotel.hasRoomTypeAvailable(roomType))
                .toList();
    }
}

/*
HotelManagementSystem
    knows:
        List<Customer>
        List<Hotel>
        List<Booking>
        PricingCalculator
        HotelSearchStrategy
    does:
        addCustomer(Customer)
        removeCustomer(Customer)
        addHotel(Hotel)
        removeHotel(Hotel)
        searchHotel(city, RoomType, pricePerRoom) -> List<Hotel>
        book(Customer, Hotel, Room, startTime, endTime) -> Booking
        cancelBooking(Booking)
* */
public class HotelManagementSystem {
    private List<Customer> customers;
    private List<Hotel> hotels;
    private List<Booking> bookings;
    private PricingCalculator pricingCalculator;
    private HotelSearchStrategy strategy;

    public HotelManagementSystem(PricingCalculator pricingCalculator, HotelSearchStrategy strategy) {
        this.customers = new ArrayList<>();
        this.hotels = new ArrayList<>();
        this.bookings = new ArrayList<>();
        this.pricingCalculator = pricingCalculator;
        this.strategy = strategy;
    }

    public void addCustomer(Customer customer) {
        this.customers.add(customer);
    }

    public void removeCustomer(Customer customer) {
        this.customers.remove(customer);
    }

    public void addHotel(Hotel hotel) {
        hotels.add(hotel);
    }

    public void removeHotel(Hotel hotel) {
        hotels.remove(hotel);
    }

    public List<Hotel> searchHotel(String city, RoomType roomType) {
        return strategy.searchHotel(city, roomType);
    }

    public Booking book(Customer customer, Hotel hotel, Room room, LocalDateTime startTime, LocalDateTime endTime) {
        if(room.tryBook()) {
            Booking newBooking = new Booking("ID" + LocalDateTime.now(), customer, hotel, room, startTime, endTime);
            pricingCalculator.calculate(newBooking);
            bookings.add(newBooking);
            return newBooking;
        }
        return null;
    }

    public void cancelBooking(Booking booking) {
        booking.updateStatus(BookingStatus.CANCELLED);
        booking.getRoom().release();
    }
}


/*

Booking has-a Customer and a Room

HotelSearchStrategy has-a List of Hotels

Hotel has-a List of Rooms

HotelManagementSystem has-a List of Customers
HotelManagementSystem has-a List of Hotels
HotelManagementSystem has-a List of Booking
HotelManagementSystem has-a PricingCalculator
HotelManagementSystem has-a HotelSearchStrategy

* */


