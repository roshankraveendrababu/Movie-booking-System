package Controllers;

import CoreClasses.Movie;
import CoreClasses.Screen;
import CoreClasses.Seat;
import CoreClasses.Show;
import Services.MovieService;
import Services.SeatAvailabilityService;
import Services.ShowService;
import Services.TheatreService;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ShowController {

    private final SeatAvailabilityService seatAvailabilityService;
    private final ShowService showService;
    private final TheatreService theatreService;
    private final MovieService movieService;

    public ShowController(SeatAvailabilityService seatAvailabilityService, ShowService showService,
                          TheatreService theatreService, MovieService movieService) {
        this.seatAvailabilityService = seatAvailabilityService;
        this.showService = showService;
        this.theatreService = theatreService;
        this.movieService = movieService;
    }

    // NEW METHOD: Add this to your file
    public Show getShow(final int showId) throws Exception {
        return showService.getShow(showId);
    }

    public int createShow(final int movieId, final int screenId, final Date startTime,
                          final Integer durationInSeconds) throws Exception{
        final Screen screen = theatreService.getScreen(screenId);
        final Movie movie = movieService.getMovie(movieId);
        return showService.createShow(movie, screen, startTime, durationInSeconds).getId();
    }

    public List<Integer> getAvailableSeats(final int showId) throws Exception{
        final Show show = showService.getShow(showId);
        final List<Seat> availableSeats = seatAvailabilityService.getAvailableSeats(show);
        return availableSeats.stream().map(Seat::getSeatId).collect(Collectors.toList());
    }

    public List<Show> getShowsByMovie(Movie movie) {
        return showService.getShowsByMovie(movie);
    }
}