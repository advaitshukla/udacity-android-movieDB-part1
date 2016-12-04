package com.advaitshukla.popularmovies;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.advaitshukla.popularmovies.Model.Movie;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class GridFragment extends Fragment {

    MovieAdapter adapter;
    Context context;
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    SharedPreferences prefs;

    public GridFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        context = getContext();
        adapter = new MovieAdapter(
                getActivity(),
                R.layout.grid_item_poster,
                new ArrayList<Movie>());

        GridView gridView = (GridView) view.findViewById(R.id.maingridview);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = adapter.getItem(position);
                Intent intent = new Intent(getContext(), DetailActivity.class);

                Bundle bundle = new Bundle();
                bundle.putSerializable(getContext().getString(R.string.detail_page_intent_key), movie);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        updateMovies(getString(R.string.url_build_top_rated));

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
                String sortOption = sp.getString(key, getString(R.string.sorting_preference_top_rated));
                updateMovies(sortOption);
            }
        };

        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private void updateMovies(String sortOption) {
        fetchMovieTask movieTask = new fetchMovieTask();
        if(sortOption.equals(getString(R.string.sorting_preference_top_rated))){
            movieTask.execute(getString(R.string.url_build_top_rated));
        } else {
            movieTask.execute(getString(R.string.url_build_most_popular));
        }
    }

    public class fetchMovieTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        private ArrayList<Movie> getMovieDataFromJson(String movieJsonStr) throws JSONException {

            ArrayList<Movie> retList = new ArrayList<>();

            final String MDB_RESULT = context.getString(R.string.url_parse_result);
            final String MDB_POSTER_PATH = context.getString(R.string.url_parse_poster_path);
            final String MDB_TITLE = context.getString(R.string.url_parse_title);
            final String MDB_PLOT = context.getString(R.string.url_parse_overview);
            final String MDB_RATING = context.getString(R.string.url_parse_rating);
            final String MDB_RELEASE_DATE = context.getString(R.string.url_parse_release_date);

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(MDB_RESULT);

            for(int i = 0; i < movieArray.length(); i++) {
                JSONObject JSONMovie = movieArray.getJSONObject(i);
                String moviePath = JSONMovie.getString(MDB_POSTER_PATH);
                String movieTitle = JSONMovie.getString(MDB_TITLE);
                String moviePlot = JSONMovie.getString(MDB_PLOT);
                String movieRelease = JSONMovie.getString(MDB_RELEASE_DATE);
                double movieRating = JSONMovie.getDouble(MDB_RATING);
                String posterPath = context.getString(R.string.url_parse_poster_base_url, moviePath);
                retList.add(new Movie(movieTitle, posterPath, movieRating, moviePlot, movieRelease));
            }

            return retList;
        }

        private boolean isOnline() {
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnectedOrConnecting();
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {
            if(!isOnline()) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String movieTaskJsonStr = null;

            try {
                final String MOVIEDB_BASE_URL = getString(R.string.url_build_base_url);
                String MOVIEDB_SORT_TYPE;
                if(params[0].toString().equals(getString(R.string.url_build_most_popular))) {
                    MOVIEDB_SORT_TYPE = getString(R.string.url_build_most_popular);
                } else {
                    MOVIEDB_SORT_TYPE = getString(R.string.url_build_top_rated);
                }
                final String MOVIEDB_API_KEY = getString(R.string.url_build_api_key);
                final String MOVIEDB_LANGUAGE= getString(R.string.url_build_language);

                Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                        .appendPath(MOVIEDB_SORT_TYPE)
                        .appendQueryParameter(MOVIEDB_API_KEY, getString(R.string.API_KEY))
                        .appendQueryParameter(MOVIEDB_LANGUAGE, getString(R.string.url_build_en_us))
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if(inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                movieTaskJsonStr = buffer.toString();


            } catch (Exception E) {
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        //log some stuff
                    }
                }
            }

            try {
                return getMovieDataFromJson(movieTaskJsonStr);
            } catch (JSONException e) {
                //log error
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> result) {
            if(result != null) {
                adapter.clear();
                for(Movie movie: result) {
                    adapter.add(movie);
                }
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.no_network_text)
                        .setTitle(R.string.no_network_title)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();
            }
        }
    }

    public class MovieAdapter extends ArrayAdapter<Movie> {
        Context context;

        public MovieAdapter(Context context, int resource, ArrayList<Movie> objects) {
            super(context, resource, objects);
            this.context = context;
        }

        public class ViewHolder {
            ImageView poster;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            Movie movie = getItem(position);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            if(convertView == null) {
                convertView = inflater.inflate(R.layout.grid_item_poster, null);
                holder = new ViewHolder();
                holder.poster = (ImageView) convertView.findViewById(R.id.grid_item_poster_imageview);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            //String URLstr = "http://image.tmdb.org/t/p/w185/" + movie.getURL();
            String URLstr = movie.getURL();
            Picasso.with(context).load(URLstr).into(holder.poster);

            return convertView;
        }
    }
}
