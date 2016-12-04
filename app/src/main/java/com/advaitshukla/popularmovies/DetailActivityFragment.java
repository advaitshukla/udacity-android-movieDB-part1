package com.advaitshukla.popularmovies;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.advaitshukla.popularmovies.Model.Movie;
import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    ImageView posterView;
    TextView titleView;
    TextView releaseDateView;
    TextView ratingView;
    TextView plotView;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        posterView = (ImageView) rootView.findViewById(R.id.detail_poster);
        titleView = (TextView) rootView.findViewById(R.id.detail_title);
        releaseDateView = (TextView) rootView.findViewById(R.id.detail_release_date);
        ratingView = (TextView) rootView.findViewById(R.id.detail_rating);
        plotView = (TextView) rootView.findViewById(R.id.detail_plot);

        populateViews();

        return rootView;
    }

    private void populateViews() {
        Bundle bundle = getActivity().getIntent().getExtras();
        Movie movie = (Movie) bundle.getSerializable(getContext().getString(R.string.detail_page_intent_key));

        String imageURL = movie.getURL();
        String title = movie.getTitle();
        String releaseDate = movie.getReleaseDate();
        Double rating = new Double(movie.getRating());
        String plot = movie.getOverview();

        Picasso.with(getActivity()).load(imageURL).into(posterView);
        titleView.setText(getContext().getString(R.string.detail_page_title, title));
        releaseDateView.setText(getContext().getString(R.string.detail_page_release_date, releaseDate));
        ratingView.setText(getContext().getString(R.string.detail_page_rating, rating));
        plotView.setText(getContext().getString(R.string.detail_page_synopsis, plot));
    }
}
