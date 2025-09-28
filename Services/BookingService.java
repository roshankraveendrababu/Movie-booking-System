package Services;

import CoreClasses.Booking;
import CoreClasses.Seat;
import CoreClasses.Show;
import CoreClasses.User;
import Interfaces.BookingObserver;
import Interfaces.ISeatLockProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BookingService {

    // Stores all bookings made across shows (key = booking ID)
    private final Map<String, Booking> showBookings;
    private final ISeatLockProvider seatLockProvider;
    private final AtomicInteger bookingIdCounter = new AtomicInteger(1);

    // NEW: List to hold all registered observers
    private final List<BookingObserver> observers = new ArrayList<>();

    public BookingService(ISeatLockProvider seatLockProvider) {
        this.seatLockProvider = seatLockProvider;
        this.showBookings = new ConcurrentHashMap<>();
    }

    // NEW: Methods to manage observers
    public void addObserver(BookingObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(BookingObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(Booking booking) {
        for (BookingObserver observer : observers) {
            observer.onBookingConfirmed(booking);
        }
    }

    public Booking getBooking(final String bookingId) throws Exception {
        if (!showBookings.containsKey(bookingId)) {
            throw new Exception("No Booking exists for the ID : " + bookingId);
        }
        return showBookings.get(bookingId);
    }

    public List<Booking> getAllBookings(final Show show) {
        List<Booking> response = new ArrayList<>();
        for (Booking booking : showBookings.values()) {
            if (booking.getShow().equals(show)) {
                response.add(booking);
            }
        }
        return response;
    }

    public Booking createBooking(final User user, final Show show, final List<Seat> seats) throws Exception {
        if (isAnySeatAlreadyBooked(show, seats)) {
            throw new Exception("Seat Already Booked");
        }
        seatLockProvider.lockSeats(show, seats, user);
        final String bookingId = String.valueOf(bookingIdCounter.getAndIncrement());
        final Booking newBooking = new Booking(bookingId, show, user, seats);
        showBookings.put(bookingId, newBooking);
        return newBooking;
    }

    public List<Seat> getBookedSeats(final Show show) {
        return getAllBookings(show).stream()
                .filter(Booking::isConfirmed)
                .map(Booking::getSeatsBooked)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public void confirmBooking(final Booking booking, final User user) throws Exception {
        if (!booking.getUser().equals(user)) {
            throw new Exception("Cannot confirm a booking made by another user");
        }
        for (Seat seat : booking.getSeatsBooked()) {
            if (!seatLockProvider.validateLock(booking.getShow(), seat, user)) {
                throw new Exception("Acquired Lock is either invalid or has Expired");
            }
        }
        booking.confirmBooking();

        // NEW: Notify all observers that the booking is confirmed!
        System.out.println("Booking " + booking.getId() + " confirmed. Notifying observers...");
        notifyObservers(booking);
    }

    // NEW: Method to release locks if payment fails.
    public void releaseSeatLocks(Booking booking) {
        seatLockProvider.unlockSeats(booking.getShow(), booking.getSeatsBooked(), booking.getUser());
        System.out.println("Seats for failed Booking ID " + booking.getId() + " have been unlocked.");
    }

    private boolean isAnySeatAlreadyBooked(final Show show, final List<Seat> seats) {
        final List<Seat> bookedSeats = getBookedSeats(show);
        for (Seat seat : seats) {
            if (bookedSeats.contains(seat)) {
                return true;
            }
        }
        return false;
    }
}