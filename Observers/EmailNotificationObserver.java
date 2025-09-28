// Create new file: src/Observers/EmailNotificationObserver.java
package Observers;

import CoreClasses.Booking;
import Interfaces.BookingObserver;

public class EmailNotificationObserver implements BookingObserver {
    @Override
    public void onBookingConfirmed(Booking booking) {
        // In a real system, you'd integrate with an email API
        System.out.println("--> [EMAIL OBSERVER]: Sending confirmation email to " +
                booking.getUser().getUserEmail() + " for Booking ID: " + booking.getId());
    }
}