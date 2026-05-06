package lowleveldesigns.librarymanagementsystem;

/*

Reservation
    knows:
        Member
        Book
        reservationDate
        status (PENDING, FULFILLED)
    does:
        nothing (date carrier)

BorrowRecord
    knows:
        member
        bookCopy
        borrowDate
        returnDate (null until returned)
    does:
        getDueDate(): LocalDateTime
        calculateLateFee(): double

BookCopy
    knows:
        copyId
        status
    does:
        getStatus(): BorrowStatus
        setStatus(BorrowStatus)

Book
    knows:
        title
        author
        ISBN
        List<BookCopy>
    does:
        isAvailable(): boolean
        addCopy(BookCopy)
        removeCopy(BookCopy)

Member
    knows:
        name
        email
        phoneNumber
    does:
        nothing (data carrier)

LibraryManagementSystem
    knows:
        List<Member>
        List<Book>
        List<BorrowRecord>
        List<Reservation>
    does:
        addMember(Member)
        removeMember(Member)
        borrowBook(Member, Book): BookRecord
        returnBook(Member, BookRecord)
        reserveBook(Member, Book)
        addRecord(Member, BookCopy, borrowDate) -> private
        updateRecord(BookRecord, returnDate): double -> private
        getRecords(Member): List<BookRecord>


Book has-a List<BookCopy>                         (one to many)

BorrowRecord has-a Member                         (one to one)
BorrowRecord has-a BookCopy                       (one to one)

Reservation has-a Member                          (one to one)
Reservation has-a Book                            (one to one)

LibraryManagementSystem has-a List<Book>          (one to many)
LibraryManagementSystem has-a List<Member>        (one to many)
LibraryManagementSystem has-a List<BorrowRecord>  (one to many)
LibraryManagementSystem has-a List<Reservation>   (one to many)

* */

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

enum ReservationStatus {
    PENDING,
    FULFILLED
}

enum BorrowStatus {
    AVAILABLE,
    BORROWED
}

class Reservation {
    private Member member;
    private Book book;
    private LocalDateTime reservationDate;
    private ReservationStatus status;

    public Reservation(Member member, Book book, LocalDateTime reservationDate) {
        this.member = member;
        this.book = book;
        this.reservationDate = reservationDate;
        this.status = ReservationStatus.PENDING;
    }

    public LocalDateTime getReservationDate() {
        return reservationDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public Book getBook() {
        return book;
    }

    public Member getMember() {
        return member;
    }
}

class BorrowRecord {
    private Member member;
    private BookCopy bookCopy;
    private LocalDateTime borrowDate;
    private LocalDateTime returnDate;
    private final int MAX_FREE_DAYS = 14;
    private final int LATE_FEE_PER_DAY = 10;

    public BorrowRecord(Member member, BookCopy bookCopy, LocalDateTime borrowDate) {
        this.member = member;
        this.bookCopy = bookCopy;
        this.borrowDate = LocalDateTime.now();
    }

    public Member getMember() {
        return member;
    }

    public BookCopy getBookCopy() {
        return bookCopy;
    }

    public LocalDateTime getBorrowDate() {
        return borrowDate;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public LocalDateTime getDueDate() {
        return borrowDate.plusDays(MAX_FREE_DAYS);
    }

    public double calculateLateFee() {
        long calendarDays = ChronoUnit.DAYS.between(getDueDate().toLocalDate(), LocalDateTime.now().toLocalDate());

        return calendarDays > 0 ? calendarDays * LATE_FEE_PER_DAY : 0;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }
}

class Member {
    private String memberId;
    private String name;
    private String email;
    private int phoneNumber;

    public Member(String memberId, String name, String email, int phoneNumber) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getMemberId() {
        return memberId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member member)) return false;
        return memberId.equals(member.memberId);
    }

    @Override
    public int hashCode() {
        return memberId.hashCode();
    }
}

class BookCopy {
    private String copyId;
    private BorrowStatus status;
    private Book book;

    public BookCopy(String copyId, Book book) {
        this.copyId = copyId;
        this.status = BorrowStatus.AVAILABLE;
        this.book = book;
    }

    public BorrowStatus getStatus() {
        return status;
    }

    public void setStatus(BorrowStatus status) {
        this.status = status;
    }

    public String getCopyId() {
        return copyId;
    }

    public Book getBook() {
        return book;
    }
}

class Book {
    private String title;
    private String author;
    private String ISBN;
    private List<BookCopy> bookCopies;

    public Book(String title, String author, String ISBN, List<BookCopy> bookCopies) {
        this.title = title;
        this.author = author;
        this.ISBN = ISBN;
        this.bookCopies = bookCopies;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getISBN() {
        return ISBN;
    }

    public boolean isAvailable() {
        for(BookCopy bookCopy: bookCopies) {
            if(bookCopy.getStatus() == BorrowStatus.AVAILABLE) {
                return true;
            }
        }

        return false;
    }

    public void addCopy(BookCopy bookCopy) {
        bookCopies.add(bookCopy);
    }

    public void removeCopy(BookCopy bookCopy) {
        bookCopies.remove(bookCopy);
    }

    public BookCopy borrowBook() {
        for(BookCopy bookCopy: bookCopies) {
            if(bookCopy.getStatus().equals(BorrowStatus.AVAILABLE)) {
                bookCopy.setStatus(BorrowStatus.BORROWED);

                return bookCopy;
            }
        }

        return null;
    }
}

public class LibraryManagementSystem {
    private List<Member> members;
    private List<Book> books;
    private List<BorrowRecord> borrowRecords;
    private List<Reservation> reservations;

    public LibraryManagementSystem() {
        members = new ArrayList<>();
        books = new ArrayList<>();
        borrowRecords = new ArrayList<>();
        reservations = new ArrayList<>();
    }

    public void addMember(Member member) {
        members.add(member);
    }

    public void removeMember(Member member) {
        members.remove(member);
    }

    public void addBook(Book book) {
        books.add(book);
    }

    public void removeBook(Book book) {
        books.remove(book);
    }

    public BorrowRecord borrowBook(Member member, Book book) {
        // 1. Check how many copies a member has borrowed (Max 3 are allowed)
        // or if the current book type is already borrowed
        int borrowedCount = 0;
        for(BorrowRecord record: borrowRecords) {
            if(record.getMember().equals(member) && record.getReturnDate() == null) {
                borrowedCount++;
                if(borrowedCount == 3) {
                    return null;
                }
            }
            if(record.getBookCopy().getBook().getISBN().equals(book.getISBN())
                    && record.getReturnDate() == null) {
                return null;
            }
        }

        // 2. Find an available copy and return BorrowRecord
        if(book.isAvailable()) {
            BookCopy bookCopy = book.borrowBook();

            BorrowRecord newBorrowRecord = new BorrowRecord(member, bookCopy, LocalDateTime.now());

            borrowRecords.add(newBorrowRecord);

            return newBorrowRecord;
        }

        return null;
    }

    public void returnBook(Member member, BorrowRecord borrowRecord) {
        BorrowRecord newBorrowRecord = null;

        for(BorrowRecord br: borrowRecords) {
            if(br.equals(borrowRecord)) {
                br.setReturnDate(LocalDateTime.now());
                borrowRecord.getBookCopy().setStatus(BorrowStatus.AVAILABLE);
                double lateFee = borrowRecord.calculateLateFee();

                Book book = borrowRecord.getBookCopy().getBook();
                Reservation reservation = reservations.stream()
                        .filter(r -> r.getBook().equals(book) && r.getStatus() == ReservationStatus.PENDING)
                        .min(Comparator.comparing(Reservation::getReservationDate))
                        .orElse(null);
                if(reservation != null) {
                    reservation.setStatus(ReservationStatus.FULFILLED);

                    BookCopy bookCopy = book.borrowBook();
                    newBorrowRecord = new BorrowRecord(reservation.getMember(), bookCopy, LocalDateTime.now());
                }
            }
        }

        if(newBorrowRecord != null) {
            borrowRecords.add(newBorrowRecord);
        }
    }

    public void reserveBook(Member member, Book book) {
        Reservation reservation = new Reservation(member, book, LocalDateTime.now());
        reservations.add(reservation);
    }

    public boolean hasActiveBorrow(Member member, Book book) {
        return borrowRecords.stream()
                .anyMatch(r -> r.getMember().equals(member)
                            && r.getBookCopy().getBook().equals(book)
                            && r.getReturnDate() == null);
    }

    public static void main(String[] args) {
        // Setup
        Book book = new Book("The Pragmatic Programmer", "David Thomas", "978-0135957059", new ArrayList<>());
        BookCopy copy1 = new BookCopy("COPY-001", book);
        book.addCopy(copy1);

        Member alice = new Member("M001", "Alice", "alice@email.com", 123456789);
        Member bob = new Member("M002", "Bob", "bob@email.com", 987654321);

        LibraryManagementSystem lms = new LibraryManagementSystem();
        lms.addMember(alice);
        lms.addMember(bob);
        lms.addBook(book);

        // Alice borrows the book
        BorrowRecord aliceRecord = lms.borrowBook(alice, book);
        System.out.println("Alice borrowed: " + (aliceRecord != null ? "success" : "failed"));

        // Bob tries to borrow the same book — should fail, no copies available
        BorrowRecord bobRecord = lms.borrowBook(bob, book);
        System.out.println("Bob borrowed: " + (bobRecord != null ? "success" : "failed"));

        // Bob reserves the book
        lms.reserveBook(bob, book);
        System.out.println("Bob reserved the book");

        // Alice returns the book — should trigger Bob's reservation
        lms.returnBook(alice, aliceRecord);
        System.out.println("Alice returned the book");

        // Check Bob now has an active borrow record
        System.out.println("Bob's reservation fulfilled: " + (lms.hasActiveBorrow(bob, book) ? "yes" : "no"));
    }
}
