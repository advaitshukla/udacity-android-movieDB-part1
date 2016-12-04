package com.advaitshukla.popularmovies.Model;

import java.io.Serializable;

/**
 * Created by advaits on 11/13/16.
 */
public class Movie implements Serializable {
    private String URL;
    private String title;
    private double rating;
    private String overview;
    private String releaseDate;

    public Movie(String movieTitle, String movieImagePath, double rating, String overview, String releaseDate){
        this.URL = movieImagePath;
        this.title = movieTitle;
        this.rating = rating;
        this.overview = overview;
        this.releaseDate = releaseDate;
    }

    public String getURL() {
        return URL;
    }

    public String getTitle() {
        return title;
    }

    public double getRating() {
        return rating;
    }

    public String getOverview() {
        return overview;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

}
