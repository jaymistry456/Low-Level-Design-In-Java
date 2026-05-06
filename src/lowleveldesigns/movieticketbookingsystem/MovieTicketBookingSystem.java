package lowleveldesigns.movieticketbookingsystem;

/*
SeatType (enum):
    STANDARD(10)
    RECLINER(20)
    VIP(30)
BookingStatus (enum):
    PENDING
    CONFIRMED
    EXPIRED

Customer:
    knows:
        customerId
        customerName
        customerEmail
        customerPhoneNumber
    does:
        nothing (data carrier)
Movie
    knows:
        movieId
        movieName
        movieLength
    does:
        nothing (data carrier)
Seat
    knows:
        seatId
        SeatType
        SeatStatus
    does:
        getSeatPrice() -> double
        isAvailable() -> boolean
        lock()
        unlock()
        book()
Screen
    knows:
        screenNumber
        List<Seat>
    does:
        getAvailableSeats() -> List<Seat>
Showtime
    knows:
        showtimeId
        Movie
        Screen
        startTime
        endTime
    does:
        nothing (data carrier)
Booking
    knows:
        Customer
        Showtime
        List<Seat>
        bookingStatus
        lockExpiry
    does:
        isExpired() -> boolean
        confirm()
        expire()
MovieTicketBookingSystem
    knows:
        List<Customer>
        List<Movie>
        List<Screen>
        List<Showtime>
        List<Booking>
    does:
        addCustomer(Customer)
        removeCustomer(Customer)
        addMovie(Movie)
        removeMovie(Movie)
        addScreen(Screen)
        removeScreen(Screen)
        addShowtime(Showtime)
        removeShowtime(Showtime)
        bookSeat(Customer, Showtime, List<Seat>) -> Booking
*/


/*

MovieTicketBookingSystem has-a List of Customers
MovieTicketBookingSystem has-a List of Movies
MovieTicketBookingSystem has-a List of Screens
MovieTicketBookingSystem has-a List of Showtimes
MovieTicketBookingSystem has-a List of Bookings

Booking has-a Customer, a Showtime and a List of Seats

Showtime has-a Movie and a Screen

Screen has-a List of Seats


* */

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

enum SeatType {
    STANDARD(10),
    RECLINER(20),
    VIP(30);

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
    BOOKED
}

enum BookingStatus {
    PENDING,
    CONFIRMED,
    EXPIRED
}

class Customer {
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhoneNumber;

    public Customer(String customerId, String customerName, String customerEmail, String customerPhoneNumber) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhoneNumber = customerPhoneNumber;
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

    public String getCustomerPhoneNumber() {
        return customerPhoneNumber;
    }
}

class Movie {
    private String movieId;
    private String movieName;
    private double movieLength;

    public Movie(String movieId, String movieName, double movieLength) {
        this.movieId = movieId;
        this.movieName = movieName;
        this.movieLength = movieLength;
    }

    public String getMovieId() {
        return movieId;
    }

    public String getMovieName() {
        return movieName;
    }

    public double getMovieLength() {
        return movieLength;
    }
}

class Seat {
    private String seatId;
    private SeatType seatType;
    private SeatStatus seatStatus;

    private final ReentrantLock lock = new ReentrantLock();

    public Seat(String seatId, SeatType seatType) {
        this.seatId = seatId;
        this.seatType = seatType;
        this.seatStatus = SeatStatus.AVAILABLE;
    }

    public String getSeatId() {
        return seatId;
    }

    public SeatType getSeatType() {
        return seatType;
    }

    public SeatStatus getSeatStatus() {
        return seatStatus;
    }

    public double getSeatPrice() {
        return seatType.getPrice();
    }

    public boolean isAvailable() {
        return seatStatus == SeatStatus.AVAILABLE;
    }

    public boolean tryLock() {
        lock.lock();

        try {
            if(isAvailable()) {
                lockSeat();
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    private void lockSeat() {
        seatStatus = SeatStatus.LOCKED;
    }

    public void unlockSeat() {
        seatStatus = SeatStatus.AVAILABLE;
    }

    public void bookSeat() {
        seatStatus = SeatStatus.BOOKED;
    }
}

class Screen {
    private int screenNumber;
    private List<Seat> seats;

    public Screen(int screenNumber, List<Seat> seats) {
        this.screenNumber = screenNumber;
        this.seats = seats;
    }

    public int getScreenNumber() {
        return screenNumber;
    }

    public List<Seat> getAvailableSeats() {
        return seats.stream()
                .filter(Seat::isAvailable)
                .toList();
    }
}

class Showtime {
    private String showtimeId;
    private Movie movie;
    private Screen screen;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Showtime(String showtimeId, Movie movie, Screen screen, LocalDateTime startTime, LocalDateTime endTime) {
        this.showtimeId = showtimeId;
        this.movie = movie;
        this.screen = screen;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getShowtimeId() {
        return showtimeId;
    }

    public Movie getMovie() {
        return movie;
    }

    public Screen getScreen() {
        return screen;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
}

class Booking {
    private Customer customer;
    private Showtime showtime;
    private List<Seat> seats;
    private BookingStatus bookingStatus;
    private LocalDateTime lockExpiry;

    public Booking(Customer customer, Showtime showtime, List<Seat> seats) {
        this.customer = customer;
        this.showtime = showtime;
        this.seats = seats;
        this.bookingStatus = BookingStatus.PENDING;
        this.lockExpiry = LocalDateTime.now().plusMinutes(10);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(lockExpiry);
    }

    public void confirm() {
        if(!isExpired()) {
            seats.forEach(Seat::bookSeat);
            bookingStatus = BookingStatus.CONFIRMED;
        }
        else {
            expire();
        }
    }

    public void expire() {
        seats.forEach(Seat::unlockSeat);
        bookingStatus = BookingStatus.EXPIRED;
    }
}

public class MovieTicketBookingSystem {
    private List<Customer> customers;
    private List<Movie> movies;
    private List<Screen> screens;
    private List<Showtime> showtimes;
    private List<Booking> bookings;

    public MovieTicketBookingSystem() {
        this.customers = new ArrayList<>();
        this.movies = new ArrayList<>();
        this.screens = new ArrayList<>();
        this.showtimes = new ArrayList<>();
        this.bookings = new ArrayList<>();
    }

    public void addCustomer(Customer customer) {
        customers.add(customer);
    }

    public void removeCustomer(Customer customer) {
        customers.remove(customer);
    }

    public void addMovie(Movie movie) {
        movies.add(movie);
    }

    public void removeMovie(Movie movie) {
        movies.remove(movie);
    }

    public void addScreen(Screen screen) {
        screens.add(screen);
    }

    public void removeScreen(Screen screen) {
        screens.remove(screen);
    }

    public void addShowtime(Showtime showtime) {
        showtimes.add(showtime);
    }

    public void removeShowtime(Showtime showtime) {
        showtimes.remove(showtime);
    }

    public Booking bookSeat(Customer customer, Showtime showtime, List<Seat> seats) {
        for(int i = 0; i < seats.size(); i++) {
            if(!seats.get(i).tryLock()) {
                for(int j = 0; j < i; j++) {
                    seats.get(j).unlockSeat();
                }
                return null;
            }
        }

        Booking booking = new Booking(customer, showtime, seats);
        bookings.add(booking);

        return booking;
    }
}
