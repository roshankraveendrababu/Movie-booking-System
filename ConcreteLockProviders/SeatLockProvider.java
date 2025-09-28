package ConcreteLockProviders;

import CoreClasses.Seat;
import CoreClasses.SeatLock;
import CoreClasses.Show;
import CoreClasses.User;
import Interfaces.ISeatLockProvider;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SeatLockProvider implements ISeatLockProvider {

    private final Integer lockTimeout;
    // Stores a dedicated manager object for each show, which contains both the seats and the lock.
    private final Map<Show, ShowLockManager> locks;
    private final ScheduledExecutorService scheduler;

    /**
     * Helper inner class to hold both the seat locks and the ReadWriteLock for a single show.
     */
    private static class ShowLockManager {
        private final ReadWriteLock lock = new ReentrantReadWriteLock();
        private final Map<Seat, SeatLock> seatLocks = new ConcurrentHashMap<>();
    }

    public SeatLockProvider(Integer lockTimeout) {
        this.locks = new ConcurrentHashMap<>();
        this.lockTimeout = lockTimeout;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Starts the background task to periodically clean up expired locks.
     */
    public void startLockCleanup() {
        // Schedule the cleanup task to run every 5 seconds, after a 5-second delay.
        scheduler.scheduleAtFixedRate(this::cleanupExpiredLocks, 5, 5, TimeUnit.SECONDS);
        System.out.println("Background seat lock cleanup started (checks every 5s).");
    }

    /**
     * Gracefully shuts down the background thread.
     */
    public void shutdown() {
        scheduler.shutdown();
        System.out.println("Background seat lock cleanup stopped.");
    }

    /**
     * The core logic for the background task. This is a WRITE operation.
     */
    private void cleanupExpiredLocks() {
        for (ShowLockManager manager : locks.values()) {
            manager.lock.writeLock().lock(); // Get the exclusive write lock
            try {
                manager.seatLocks.entrySet().removeIf(lockEntry -> lockEntry.getValue().isLockExpired());
            } finally {
                manager.lock.writeLock().unlock(); // Always release the lock
            }
        }
    }

    @Override
    public void lockSeats(final Show show, final List<Seat> seats, final User user) throws Exception {
        ShowLockManager manager = locks.computeIfAbsent(show, s -> new ShowLockManager());

        manager.lock.writeLock().lock(); // Get the exclusive WRITE lock since we are modifying the map
        try {
            for (Seat seat : seats) {
                if (manager.seatLocks.containsKey(seat)) {
                    throw new Exception("Seat " + seat.getSeatId() + " is already locked.");
                }
            }
            Date now = new Date();
            for (Seat seat : seats) {
                SeatLock lock = new SeatLock(seat, show, lockTimeout, now, user);
                manager.seatLocks.put(seat, lock);
            }
        } finally {
            manager.lock.writeLock().unlock(); // ALWAYS release the lock in a finally block
        }
    }

    @Override
    public void unlockSeats(final Show show, final List<Seat> seats, final User user) {
        ShowLockManager manager = locks.get(show);
        if (manager == null) return;

        manager.lock.writeLock().lock(); // Get the exclusive WRITE lock
        try {
            for (Seat seat : seats) {
                SeatLock lock = manager.seatLocks.get(seat);
                if (lock != null && lock.getLockedBy().equals(user)) {
                    manager.seatLocks.remove(seat);
                }
            }
        } finally {
            manager.lock.writeLock().unlock();
        }
    }

    @Override
    public boolean validateLock(final Show show, final Seat seat, final User user) {
        ShowLockManager manager = locks.get(show);
        if (manager == null) return false;

        manager.lock.readLock().lock(); // Get a shared READ lock, as we are not modifying data
        try {
            SeatLock lock = manager.seatLocks.get(seat);
            return lock != null && !lock.isLockExpired() && lock.getLockedBy().equals(user);
        } finally {
            manager.lock.readLock().unlock();
        }
    }

    @Override
    public List<Seat> getLockedSeats(final Show show) {
        ShowLockManager manager = locks.get(show);
        if (manager == null) {
            return Collections.emptyList();
        }

        manager.lock.readLock().lock(); // Get a shared READ lock
        try {
            // Return a new list to ensure thread safety outside this method
            return new ArrayList<>(manager.seatLocks.keySet());
        } finally {
            manager.lock.readLock().unlock();
        }
    }
}