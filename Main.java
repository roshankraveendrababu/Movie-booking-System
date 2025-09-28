import CommonEnum.PaymentMethod;
import CommonEnum.SeatCategory;
import ConcreteLockProviders.SeatLockProvider;
import Controllers.*;
import CoreClasses.*;
import Factories.PaymentStrategyFactory;
import Interfaces.BookingObserver;
import Interfaces.PaymentStrategy;
import Observers.AnalyticsUpdateObserver;
import Observers.EmailNotificationObserver;
import Services.*;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Main {
    // Static fields to be accessible by all helper methods
    private static MovieController movieController;
    private static TheatreController theatreController;
    private static ShowController showController;
    private static BookingController bookingController;
    private static PaymentController paymentController;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        SeatLockProvider seatLockProvider = null;
        try {
            seatLockProvider = initializeSystem();
            setupInitialData(); // Create a richer set of sample data

            User currentUser = new User("Cinephile Charlie", "charlie@cinema.com");
            boolean running = true;
            while (running) {
                showMainMenu();
                try {
                    int choice = Integer.parseInt(scanner.nextLine());
                    switch (choice) {
                        case 1:
                            handleListMovies();
                            break;
                        case 2:
                            handleBookingProcess(currentUser);
                            break;
                        case 3:
                            handleConcurrencyTest();
                            break;
                        case 4:
                            handleAbandonedLockTest();
                            break;
                        case 0:
                            running = false;
                            break;
                        default:
                            System.out.println("Invalid choice. Please try again.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number.");
                } catch (Exception e) {
                    System.err.println("An error occurred: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("A critical error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (seatLockProvider != null) {
                System.out.println("\nApplication is closing. Shutting down background services...");
                seatLockProvider.shutdown();
            }
            scanner.close();
        }
    }

    // Wires up all the application components
    private static SeatLockProvider initializeSystem() {
        // Services
        MovieService movieService = new MovieService();
        TheatreService theatreService = new TheatreService();
        ShowService showService = new ShowService();
        SeatLockProvider seatLockProvider = new SeatLockProvider(15); // 15-second lock for easier testing
        seatLockProvider.startLockCleanup();
        BookingService bookingService = new BookingService(seatLockProvider);
        PaymentService paymentService = new PaymentService(bookingService);
        SeatAvailabilityService seatAvailabilityService = new SeatAvailabilityService(bookingService, seatLockProvider);

        // Observers
        bookingService.addObserver(new EmailNotificationObserver());
        bookingService.addObserver(new AnalyticsUpdateObserver());

        // Controllers
        movieController = new MovieController(movieService);
        theatreController = new TheatreController(theatreService);
        showController = new ShowController(seatAvailabilityService, showService, theatreService, movieService);
        bookingController = new BookingController(showService, bookingService, theatreService);
        paymentController = new PaymentController(paymentService);

        return seatLockProvider;
    }

    // Sets up a realistic dataset of movies, theatres, and shows
    private static void setupInitialData() throws Exception {
        System.out.println("--- Setting up sample movies and shows for today ---");
        // Create movies
        int movie1Id = movieController.createMovie("Inception", 150);
        int movie2Id = movieController.createMovie("The Dark Knight", 152);

        // Create Theatre 1: Gemini Multiplex
        int theatre1Id = theatreController.createTheatre("Gemini Multiplex");
        int screen1Id = theatreController.createScreenInTheatre("Screen 1", theatre1Id);
        for (int i = 1; i <= 30; i++) { // 30 seats
            theatreController.createSeatInScreen((i-1)/10 + 1, SeatCategory.GOLD, screen1Id);
        }
        showController.createShow(movie1Id, screen1Id, new Date(), 150); // Inception in Screen 1

        // Create Theatre 2: Anna Cinemas
        int theatre2Id = theatreController.createTheatre("Anna Cinemas");
        int screen2Id = theatreController.createScreenInTheatre("IMAX", theatre2Id);
        for (int i = 1; i <= 50; i++) { // 50 seats
            theatreController.createSeatInScreen((i-1)/10 + 1, SeatCategory.PLATINUM, screen2Id);
        }
        showController.createShow(movie1Id, screen2Id, new Date(), 150); // Inception in IMAX
        showController.createShow(movie2Id, screen2Id, new Date(), 152); // Dark Knight in IMAX
    }

    private static void showMainMenu() {
        System.out.println("\n===== BOOK MY TICKET =====");
        System.out.println("1. List All Movies");
        System.out.println("2. Book a Ticket");
        System.out.println("3. Simulate Race Condition");
        System.out.println("4. Simulate Abandoned Lock");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }

    private static void handleListMovies() {
        System.out.println("\n--- Movies Currently Playing ---");
        // This line gets the list of movies from the controller
        List<Movie> movies = movieController.getAllMovies();

        // This loop prints the details for each movie in the list
        for (Movie movie : movies) {
            System.out.println("ID: " + movie.getMovieId() + ", Title: " + movie.getMovieName() + ", Duration: " + movie.getMovieDuration() + " mins");
        }
    }

    // Orchestrates the entire multi-step booking flow
    private static void handleBookingProcess(User user) throws Exception {
        // Step 1: Select a Movie
        Movie selectedMovie = selectMovie();
        if (selectedMovie == null) return;

        // Step 2: Select a Show (Theatre and Time)
        Show selectedShow = selectShow(selectedMovie);
        if (selectedShow == null) return;

        // Step 3: Select Seats
        System.out.println("\n--- Booking tickets for '" + selectedMovie.getMovieName() + "' at " + selectedShow.getScreen().getTheatre().getTheatreName() + " ---");
        displaySeats(selectedShow);
        System.out.print("Enter the seat numbers you want to book (e.g., 5,6,7): ");
        String seatsInput = scanner.nextLine();
        List<Integer> seatIdsToBook = Arrays.stream(seatsInput.split(","))
                .map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
        if (seatIdsToBook.isEmpty()) {
            System.out.println("No seats selected.");
            return;
        }

        // Step 4: Create Booking and Process Payment
        String bookingId = bookingController.createBooking(user, selectedShow.getId(), seatIdsToBook);
        System.out.println("Seats temporarily locked. Booking ID: " + bookingId + ". Please complete payment.");

        System.out.println("\n--- Choose Payment Method ---");
        System.out.println("1. Debit Card (Simulates Success)");
        System.out.println("2. UPI (Simulates Failure)");
        System.out.print("Enter payment choice: ");
        int paymentChoice = Integer.parseInt(scanner.nextLine());

        PaymentMethod method = (paymentChoice == 1) ? PaymentMethod.DEBIT_CARD : PaymentMethod.UPI;
        PaymentStrategy strategy = PaymentStrategyFactory.getPaymentStrategy(method);
        paymentController.processPayment(bookingId, user, strategy);
    }

    // Helper for the user to select a movie from a list
    private static Movie selectMovie() throws Exception {
        handleListMovies();
        System.out.print("Enter the ID of the movie you want to book: ");
        int movieId = Integer.parseInt(scanner.nextLine());
        return movieController.getMovie(movieId);
    }

    // Helper for the user to select a show for a given movie
    private static Show selectShow(Movie movie) throws Exception {
        System.out.println("\n--- Available Shows for '" + movie.getMovieName() + "' ---");
        List<Show> shows = showController.getShowsByMovie(movie);
        if (shows.isEmpty()) {
            System.out.println("Sorry, no shows available for this movie.");
            return null;
        }
        for (Show show : shows) {
            System.out.println("Show ID: " + show.getId() + ", Theatre: " + show.getScreen().getTheatre().getTheatreName() + " (" + show.getScreen().getScreenName() + "), Time: " + show.getStartTime());
        }
        System.out.print("Enter the Show ID you want to book: ");
        int showId = Integer.parseInt(scanner.nextLine());
        return showController.getShow(showId);
    }

    // Displays a rich seat layout with categories and prices
    private static void displaySeats(Show show) throws Exception {
        List<Integer> availableSeatIds = showController.getAvailableSeats(show.getId());
        List<Seat> allSeats = show.getScreen().getSeats();
        Map<SeatCategory, Integer> prices = Map.of(SeatCategory.SILVER, 150, SeatCategory.GOLD, 250, SeatCategory.PLATINUM, 400);

        System.out.println("\n--- Screen Layout (Price per seat) ---");
        for (int i = 0; i < allSeats.size(); i++) {
            Seat seat = allSeats.get(i);
            String seatDisplay;
            if (availableSeatIds.contains(seat.getSeatId())) {
                seatDisplay = String.format("[%c:%2d ₹%d]", seat.getSeatCategory().toString().charAt(0), seat.getSeatId(), prices.get(seat.getSeatCategory()));
            } else {
                seatDisplay = " [---XX---] ";
            }
            System.out.print(seatDisplay);
            if ((i + 1) % 10 == 0) {
                System.out.println();
            }
        }
        System.out.println("\n-----------------------------------------------------");
    }

    // Interactive concurrency test
    private static void handleConcurrencyTest() throws Exception {
        System.out.println("\n--- Concurrency Stress Test ---");
        Show show = selectShow(selectMovie());
        if (show == null) return;

        System.out.print("Enter a seat number for all users to compete for: ");
        int seatToBook = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter number of concurrent users (threads) to simulate: ");
        int numThreads = Integer.parseInt(scanner.nextLine());

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        for (int i = 1; i <= numThreads; i++) {
            final int userId = i;
            executor.submit(() -> {
                try {
                    User user = new User("TestUser " + userId, "test" + userId + "@example.com");
                    Thread.sleep(new Random().nextInt(100));
                    System.out.println(Thread.currentThread().getName() + " attempting to book seat #" + seatToBook);
                    bookingController.createBooking(user, show.getId(), Arrays.asList(seatToBook));
                    System.out.println("✅ ✅ ✅ " + Thread.currentThread().getName() + " SUCCEEDED!");
                } catch (Exception e) {
                    System.out.println("❌ " + Thread.currentThread().getName() + " FAILED: " + e.getMessage());
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(15, TimeUnit.SECONDS);
        System.out.println("\n--- Concurrency Test Finished ---");
    }

    // Interactive abandoned lock test
    private static void handleAbandonedLockTest() throws Exception {
        System.out.println("\n--- Abandoned Lock Simulation (15s timeout) ---");
        Show show = selectShow(selectMovie());
        if (show == null) return;

        displaySeats(show);
        System.out.print("Enter seat numbers to lock and then abandon (e.g., 25,26): ");
        String seatsInput = scanner.nextLine();
        List<Integer> seatIdsToBook = Arrays.stream(seatsInput.split(","))
                .map(String::trim).map(Integer::parseInt).collect(Collectors.toList());

        User abandoningUser = new User("Forgetful Fred", "fred@example.com");
        System.out.println(abandoningUser.getUserName() + " is locking seats " + seatIdsToBook + "...");
        bookingController.createBooking(abandoningUser, show.getId(), seatIdsToBook);
        System.out.println("Seats locked. Now showing seat status:");
        displaySeats(show);

        System.out.println("\n" + abandoningUser.getUserName() + " has abandoned the session.");
        System.out.println("Waiting for 25 seconds for the background thread to clean up the lock...");
        TimeUnit.SECONDS.sleep(25);

        System.out.println("\nWait finished. Checking seat status again:");
        displaySeats(show);
        System.out.println("✅ Test complete. The abandoned seats should be available again.");
    }
}