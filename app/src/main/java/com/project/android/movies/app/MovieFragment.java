package com.project.android.movies.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieFragment extends Fragment {

    private final String LOG_TAG = MovieFragment.class.getSimpleName();

    private ImageAdapter mMovieAdapter;

    public MovieFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mMovieAdapter = new ImageAdapter(
                // the current context (this fragment's parent activity)
                getActivity());

        GridView gridview = (GridView) rootView.findViewById(R.id.gridview);
        gridview.setAdapter(mMovieAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);

                Movie movie = (Movie) mMovieAdapter.getItem(position);

                String title = movie.getTitle();
                detailIntent.putExtra(getString(R.string.bean_movie_title), title);

                String releaseDate = movie.getReleaseDate();
                detailIntent.putExtra(getString(R.string.bean_movie_release_date), releaseDate);

                Bitmap poster = movie.getBitmap();
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                poster.compress(Bitmap.CompressFormat.PNG, 50, bs);
                detailIntent.putExtra(getString(R.string.bean_movie_poster), bs.toByteArray());

                String voteAverate = new Double(movie.getVoteAverage()).toString();
                detailIntent.putExtra(getString(R.string.bean_movie_vote_average), voteAverate);

                String plotSynopsis = movie.getPlotSynopsis();
                detailIntent.putExtra(getString(R.string.bean_movie_plot_synopsis), plotSynopsis);

                startActivity(detailIntent);
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.moviefragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        refresh();
    }

    private void refresh() {
        FetchMovieTask task = new FetchMovieTask();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortOrder = preferences.getString(getString(R.string.pref_sort_order_key), getString(R.string.pref_sort_order_popular));
//        Log.v(LOG_TAG, "Preference Sort Order: " + sortOrder);
        task.execute(sortOrder);
    }


    public class ImageAdapter extends BaseAdapter {

        private final String LOG_TAG = ImageAdapter.class.getSimpleName();

        private Context mContext;

        private Movie [] mMovies;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return (mMovies ==  null) ? 0 : mMovies.length;
        }

        public Object getItem(int position) {
            return (mMovies == null) ? new Movie(0,"","","","",0d) : mMovies[position];
        }

        public long getItemId(int position) {
            return 0;
        }

        public void clearMovies() {
            mMovies = null;
        }

        public void addAll(Movie [] movies) {
            mMovies = movies;
        }

        public Movie [] getMovies() {
            return (mMovies == null) ? new Movie [0] : mMovies;
        }

        public Bitmap getPoster(int position) {
            return (mMovies == null) ? Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444) : mMovies[position].getBitmap();
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

//            Log.d(LOG_TAG, "SET IMAGE BITMAP IN GRID BY ADAPTER");

            imageView.setImageBitmap(getPoster(position));

            return imageView;
        }
    }

    public class Movie {
        private final String LOG_TAG = Movie.class.getSimpleName();
        private int mID;
        private String mTitle;
        private String mReleaseDate;
        private String mPosterPath;
        private String mPlotSynopsis;
        private double mVoteAverage;
        private Bitmap mBitmap;
        public Movie(int id, String title, String releaseDate, String posterPath, String plotSynopsis, double voteAverage) {
            mID = id;
            mTitle = title;
            mReleaseDate = releaseDate;
            mPosterPath = posterPath;
            mPlotSynopsis = plotSynopsis;
            mVoteAverage = voteAverage;
        }
        public int getID() { return mID; }
        public String getTitle() { return mTitle; }
        public String getReleaseDate() { return mReleaseDate; }
        public String getPosterPath() { return mPosterPath; }
        public String getPlotSynopsis() { return mPlotSynopsis; }
        public double getVoteAverage() { return mVoteAverage; }
        public String getPosterUri() {
            final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/w45";
            final String API_KEY = "api_key";

            Uri uri = Uri.parse(POSTER_BASE_URL).buildUpon() // Uri.Builder
                    .appendPath(getPosterPath().substring(1))
                    .appendQueryParameter(API_KEY, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();

//            Log.v(LOG_TAG, "Built URI " + uri.toString());
            return uri.toString();

        }
        public Bitmap getBitmap() {
            return (mBitmap == null) ? Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444) : mBitmap;
        }
        public void setBitmap(Bitmap bitmap) {
            mBitmap = bitmap;
        }
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ID: ").append(mID).append(", ")
                    .append("Title: ").append(mTitle).append(", ")
                    .append("Release Date: ").append(mReleaseDate).append(", ")
                    .append("Poster Path: ").append(mPosterPath).append(", ")
                    .append("Plot Synopsis: ").append(mPlotSynopsis).append(", ")
                    .append("Vote Average: ").append(Double.toString(mVoteAverage));
            return builder.toString();
        }
    }

    public class FetchMovieTask extends AsyncTask<String, Void, Movie[]> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        protected Movie[] doInBackground(String... sortOrder) {
//            Log.d(LOG_TAG,"LOAD POSTER ADDRESSES");
            return getMovieJson(sortOrder[0]);
        }

        protected void onPostExecute(Movie[] result) {
            if (result != null) {
                mMovieAdapter.clearMovies();
                mMovieAdapter.addAll(result);
                new FetchPosterTask().execute();
            }
        }

        private Movie[] getMovieJson(String sortOrder) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            try {
                final String FORECAST_BASE_URL = "http://api.themoviedb.org/3/movie";
                final String API_KEY = "api_key";

                Uri uri = Uri.parse(FORECAST_BASE_URL).buildUpon() // Uri.Builder
                        .appendPath(sortOrder)
                        .appendQueryParameter(API_KEY, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(uri.toString());

//                Log.v(LOG_TAG, "Built URI " + uri.toString());

                // Create the request to the movie db, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                // Nothing to do.
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();

//                Log.v(LOG_TAG, "Movie JSON string: " + movieJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attempting
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the movie.
            return null;

        }

        /**
         * Take the String representing the movie in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private Movie[] getMovieDataFromJson(String movieJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String TMDB_LIST = "results";
            final String TMDB_ID = "id";
            final String TMDB_TITLE = "title";
            final String TMDB_RELEASE_DATE = "release_date";
            final String TMDB_POSTER_PATH = "poster_path";
            final String TMDB_VOTE_AVERAGE = "vote_average";
            final String TMDB_PLOT_SYNOPSIS = "overview";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(TMDB_LIST);

            // TMDB returns movies based upon the sort order requested.

            Movie [] movies = new Movie [movieArray.length()];
            for(int i = 0; i < movieArray.length(); i++) {
                // Get the JSON object representing the movie
                JSONObject jsonMovie = movieArray.getJSONObject(i);

                int id = jsonMovie.getInt(TMDB_ID);
                String title = jsonMovie.getString(TMDB_TITLE);
                String releaseDate = jsonMovie.getString(TMDB_RELEASE_DATE);
                String posterPath = jsonMovie.getString(TMDB_POSTER_PATH);
                String plotSynopsis = jsonMovie.getString(TMDB_PLOT_SYNOPSIS);
                double voteAverage = jsonMovie.getDouble(TMDB_VOTE_AVERAGE);
                Movie movie = new Movie(id,title,releaseDate,posterPath,plotSynopsis,voteAverage);
                movies[i] = movie;
            }

//            for (Movie entry : movies) {
//                Log.v(LOG_TAG, "Movie entry: " + entry.toString());
//            }

            return movies;

        }

    }

    public class FetchPosterTask extends AsyncTask<Void, Void, Void> {

        private final String LOG_TAG = FetchPosterTask.class.getSimpleName();

        protected Void doInBackground(Void... params) {
            for(Movie movie:mMovieAdapter.getMovies()){
                String uri = movie.getPosterUri();
                try {
                    InputStream in = new java.net.URL(uri).openStream();
                    movie.setBitmap(BitmapFactory.decodeStream(in));
//                    Log.d(LOG_TAG,"LOAD A BITMAP");
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            mMovieAdapter.notifyDataSetChanged();
        }

    }

}
