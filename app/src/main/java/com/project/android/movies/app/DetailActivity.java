package com.project.android.movies.app;
/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment {

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();

        private static final String MOVIE_SHARE_HASHTAG = "#MovieApp";

        private String mMovieStr;

        public DetailFragment() {
            this.setHasOptionsMenu(true);
            mMovieStr = "";
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            Intent intent = getActivity().getIntent();

            if(intent!=null && intent.hasExtra(getString(R.string.bean_movie_title))) {
                String title = intent.getStringExtra(getString(R.string.bean_movie_title));
                mMovieStr += title + "\n";
                TextView detailView = (TextView) rootView.findViewById(R.id.detail_title);
                detailView.setText(title);
            }

            if(intent!=null && intent.hasExtra(getString(R.string.bean_movie_release_date))) {
                String releaseDate = intent.getStringExtra(getString(R.string.bean_movie_release_date));
                mMovieStr += releaseDate + "\n";
                TextView detailView = (TextView) rootView.findViewById(R.id.detail_release_date);
                detailView.setText(releaseDate);
            }

            if(intent!=null && intent.hasExtra(getString(R.string.bean_movie_poster))) {
                Bitmap poster = BitmapFactory.decodeByteArray(
                        intent.getByteArrayExtra(getString(R.string.bean_movie_poster)),
                        0,
                        intent.getByteArrayExtra(getString(R.string.bean_movie_poster)).length);
                ImageView detailView = (ImageView) rootView.findViewById(R.id.detail_poster);
                detailView.setImageBitmap(poster);
            }

            if(intent!=null && intent.hasExtra(getString(R.string.bean_movie_vote_average))) {
                String voteAverage = intent.getStringExtra(getString(R.string.bean_movie_vote_average));
                mMovieStr += voteAverage + "\n";
                TextView detailView = (TextView) rootView.findViewById(R.id.detail_vote_average);
                detailView.setText(voteAverage);
            }

            if(intent!=null && intent.hasExtra(getString(R.string.bean_movie_plot_synopsis))) {
                String plotSynopsis = intent.getStringExtra(getString(R.string.bean_movie_plot_synopsis));
                mMovieStr += plotSynopsis + "\n";
                TextView detailView = (TextView) rootView.findViewById(R.id.detail_plot_synopsis);
                detailView.setText(plotSynopsis);
            }

            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.detailfragment, menu);
            MenuItem item = menu.findItem(R.id.action_share);
            ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
            if(mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareIntent());
            } else {
                Log.d(LOG_TAG, "Share action provider is null?");
            }

        }

        private Intent createShareIntent() {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mMovieStr + "\n" + MOVIE_SHARE_HASHTAG);
            return shareIntent;
        }
    }
}