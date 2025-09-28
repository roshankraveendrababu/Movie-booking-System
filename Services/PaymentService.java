package Services;

import CommonEnum.PaymentStatus;
import CoreClasses.Booking;
import CoreClasses.User;
import Interfaces.PaymentStrategy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PaymentService {

    private final Map<Booking, Integer> bookingFailures;
    private final BookingService bookingService;

    public PaymentService(BookingService bookingService) {
        this.bookingFailures = new ConcurrentHashMap<>();
        this.bookingService = bookingService;
    }

    public void processPayment(final String bookingId, final User user, PaymentStrategy paymentStrategy) throws Exception {
        PaymentStatus status = paymentStrategy.processPayment();

        if (status == PaymentStatus.SUCCESS) {
            bookingService.confirmBooking(bookingService.getBooking(bookingId), user);
        } else {
            processPaymentFailed(bookingId, user, status);
        }
    }

    public void processPaymentFailed(final String bookingId, final User user, PaymentStatus status) throws Exception {
        Booking booking = bookingService.getBooking(bookingId);
        if (!booking.getUser().equals(user)) {
            throw new Exception("Only the booking owner can report payment failure.");
        }

        bookingFailures.put(booking, bookingFailures.getOrDefault(booking, 0) + 1);
        System.out.println("Payment failed for Booking ID: " + bookingId + " with status: " + status);

        // Important: Unlock the seats since the payment failed!
        bookingService.releaseSeatLocks(booking);
    }
}