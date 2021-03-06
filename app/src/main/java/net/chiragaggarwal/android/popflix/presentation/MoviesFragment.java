package net.chiragaggarwal.android.popflix.presentation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import net.chiragaggarwal.android.popflix.NetworkUtilities;
import net.chiragaggarwal.android.popflix.R;
import net.chiragaggarwal.android.popflix.data.MoviesProviderService;
import net.chiragaggarwal.android.popflix.models.Error;
import net.chiragaggarwal.android.popflix.models.Movie;
import net.chiragaggarwal.android.popflix.models.Movies;
import net.chiragaggarwal.android.popflix.network.MoviesService;
import net.chiragaggarwal.android.popflix.presentation.common.MoviesPresenter;
import net.chiragaggarwal.android.popflix.presentation.common.MoviesView;

import java.text.ParseException;

public class MoviesFragment extends Fragment implements MoviesView {
    private static final String LOG_TAG = "popflix.movies_fragment";
    private GridView moviesGrid;
    private MoviesAdapter moviesAdapter;
    private OnMovieSelectedListener onMovieSelectedListener;
    private MoviesPresenter moviesPresenter;

    public void refresh() {
        this.moviesAdapter.clear();
        fetchMovies(this.moviesPresenter);
    }

    public interface OnMovieSelectedListener {
        void onMovieSelected(Movie movie);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onMovieSelectedListener = ((OnMovieSelectedListener) getActivity());
        } catch (ClassCastException exception) {
            throw new ClassCastException(getActivity().toString() + " must implement OnMovieSelectedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movies, container, false);
        onMovieSelectedListener = ((OnMovieSelectedListener) getActivity());
        setHasOptionsMenu(true);
        initializeViews(view);

        NetworkUtilities networkUtilities = new NetworkUtilities(getContext());
        MoviesService moviesService = new MoviesService(getContext(), networkUtilities);
        MoviesProviderService moviesProviderService = new MoviesProviderService(getContext());
        this.moviesPresenter = new MoviesPresenter(this, moviesService, moviesProviderService);

        return view;
    }

    @Override
    public void onMoviesLoaded(Movies movies) {
        this.moviesAdapter.populateMovies(movies);
        setOnItemClickListenerForMovieGrid();
    }

    @Override
    public void onError(Error error) {
        showErrorDialog(error);
    }

    @Override
    public void onUnexpectedError() {
        Log.e(LOG_TAG, "Oops! Something went wrong!");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_movies, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.movies_action_settings:
                launchSettings();
                break;
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Movies movies = this.moviesAdapter.getMovies();
        outState.putParcelable(Movies.TAG, movies);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) {
            fetchMovies(moviesPresenter);
            return;
        }
        Movies movies = (Movies) savedInstanceState.get(Movies.TAG);
        onMoviesLoaded(movies);
    }

    private void initializeViews(View view) {
        this.moviesGrid = ((GridView) view.findViewById(R.id.movies_grid));
        this.moviesAdapter = new MoviesAdapter(getContext(), new Movies());
        this.moviesGrid.setAdapter(this.moviesAdapter);
    }

    private void fetchMovies(MoviesPresenter moviesPresenter) {
        try {
            moviesPresenter.fetchMovies(sortOrder());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void setOnItemClickListenerForMovieGrid() {
        this.moviesGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = moviesAdapter.getItem(position);
                onMovieSelectedListener.onMovieSelected(movie);
            }
        });
    }

    private void launchSettings() {
        Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private String sortOrder() {
        String sortOrderPreferenceKey = getString(R.string.preference_sort_order_key);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getString(sortOrderPreferenceKey, "");
    }

    private void showErrorDialog(Error error) {
        new AlertDialog.Builder(getContext())
                .setTitle(error.statusCode.toString())
                .setMessage(error.statusMessage)
                .create()
                .show();
    }
}
