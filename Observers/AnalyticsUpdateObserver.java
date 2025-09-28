// Create new file: src/Observers/AnalyticsUpdateObserver.java
package Observers;

import CoreClasses.Booking;
import Interfaces.BookingObserver;

public class AnalyticsUpdateObserver implements BookingObserver {
    @Override
    public void onBookingConfirmed(Booking booking) {
        // In a real system, you'd send this data to an analytics service
        System.out.println("--> [ANALYTICS OBSERVER]: Updating analytics for Show ID: " +
                booking.getShow().getId() + ". Seats booked: " + booking.getSeatsBooked().size());
    }
}