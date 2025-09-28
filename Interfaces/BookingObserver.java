// Create new file: src/Interfaces/BookingObserver.java
package Interfaces;

import CoreClasses.Booking;

public interface BookingObserver {
    void onBookingConfirmed(Booking booking);
}