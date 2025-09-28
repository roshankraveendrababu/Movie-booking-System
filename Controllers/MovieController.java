package Controllers;


import CoreClasses.Movie;
import Services.MovieService;

import java.util.List;

public class MovieController {

    // Reference to the MovieService which contains the business logic related to movies
    private final MovieService movieService;

    // Constructor to initialize the MovieService dependency
    public MovieController(final MovieService movieService) {
        this.movieService = movieService;
    }

    public int createMovie(final String movieName, final int durationInMinutes) {
        return movieService.createMovie(movieName, durationInMinutes).getMovieId();
    }

    public Movie getMovie(final int movieId) throws Exception {
        return movieService.getMovie(movieId);
    }

    public List<Movie> getAllMovies() {
        return movieService.getAllMovies();
    }
}
