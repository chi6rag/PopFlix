package net.chiragaggarwal.android.popflix.presentation;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.chiragaggarwal.android.popflix.NetworkUtilities;
import net.chiragaggarwal.android.popflix.R;
import net.chiragaggarwal.android.popflix.data.MoviesProviderService;
import net.chiragaggarwal.android.popflix.models.Callback;
import net.chiragaggarwal.android.popflix.models.Error;
import net.chiragaggarwal.android.popflix.models.ImageSize;
import net.chiragaggarwal.android.popflix.models.Movie;
import net.chiragaggarwal.android.popflix.models.MoviesPreference;
import net.chiragaggarwal.android.popflix.models.Reviews;
import net.chiragaggarwal.android.popflix.models.Video;
import net.chiragaggarwal.android.popflix.models.Videos;
import net.chiragaggarwal.android.popflix.network.FetchReviewsTask;
import net.chiragaggarwal.android.popflix.network.FetchVideosTask;
import net.chiragaggarwal.android.popflix.presentation.common.ListUtilities;
import net.chiragaggarwal.android.popflix.presentation.common.MovieDetailViewModel;
import net.chiragaggarwal.android.popflix.presentation.common.MovieDetailsPresenter;
import net.chiragaggarwal.android.popflix.presentation.common.MovieDetailsView;

import static android.widget.AdapterView.OnItemClickListener;

public class DetailsFragment extends Fragment implements MovieDetailsView {
    private static final String DIVIDED_BY_TEN = " / 10";
    private static final String LOG_TAG = "popflix.detailsfragment";
    private static final String MIME_TYPE_TEXT_PLAIN = "text/plain";

    private TextView movieName;
    private ImageView moviePoster;
    private TextView movieYear;
    private TextView movieOverview;
    private TextView movieAverage;
    private ListView listVideos;
    private ProgressBar videoLoadingProgressBar;
    private TextView textVideoErrorMessage;
    private MovieVideosAdapter movieVideosAdapter;
    private ProgressBar progressbarReviews;
    private ListView listReviews;
    private TextView textReviewsErrorMessage;
    private MovieReviewsAdapter movieReviewsAdapter;
    private ShareActionProvider shareActionProvider;
    private Movie movie;
    private Button buttonToggleFavorite;
    private MovieDetailViewModel movieDetailViewModel;
    private MovieDetailsPresenter movieDetailsPresenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie, container, false);
        setHasOptionsMenu(true);
        this.movie = fetchMovieFromArguments();
        this.movieDetailViewModel = new MovieDetailViewModel(this.movie);
        MoviesProviderService moviesProviderService = new MoviesProviderService(getContext());
        this.movieDetailsPresenter = new MovieDetailsPresenter(this, moviesProviderService);
        initializeViews(view);
        showDetailsFor(this.movie);
        loadVideosFor(this.movie);
        loadReviewsFor(this.movie);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_details, menu);
        MenuItem shareActionItem = menu.findItem(R.id.action_share);
        this.shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareActionItem);
        setDefaultShareAction();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                break;
        }
        return false;
    }

    @Override
    public void onSaveFavoriteMovie() {
        initializeStates();
    }

    @Override
    public void onDeleteFavoriteMovie() {
        initializeStates();
    }

    private Movie fetchMovieFromArguments() {
        return getArguments().getParcelable(Movie.TAG);
    }

    private void initializeViews(View view) {
        this.movieName = ((TextView) view.findViewById(R.id.movie_name));
        this.moviePoster = ((ImageView) view.findViewById(R.id.movie_poster));
        this.movieYear = ((TextView) view.findViewById(R.id.movie_year));
        this.movieOverview = ((TextView) view.findViewById(R.id.movie_overview));
        this.movieAverage = ((TextView) view.findViewById(R.id.movie_vote_average));
        this.listVideos = (ListView) view.findViewById(R.id.list_videos);
        this.videoLoadingProgressBar = (ProgressBar) view.findViewById(R.id.progressbar_videos);
        this.textVideoErrorMessage = ((TextView) view.findViewById(R.id.text_video_error_message));
        this.listReviews = ((ListView) view.findViewById(R.id.list_reviews));
        this.progressbarReviews = ((ProgressBar) view.findViewById(R.id.progressbar_reviews));
        this.textReviewsErrorMessage = ((TextView) view.findViewById(R.id.text_reviews_error_message));
        this.buttonToggleFavorite = (Button) view.findViewById(R.id.button_toggle_favorite);
        initializeStates();
        setEventListeners();
    }

    private void showDetailsFor(Movie movie) {
        this.movieName.setText(movie.originalTitle);
        showPoster(movie);
        this.movieYear.setText(movie.yearString());
        this.movieOverview.setText(movie.overview);
        this.movieAverage.setText(movie.voteAverage + DIVIDED_BY_TEN);
    }

    private void loadVideosFor(Movie movie) {
        startVideoLoadingProgressBar();
        Context context = getContext();
        new FetchVideosTask(movie.idString(),
                context,
                new NetworkUtilities(context),
                new Callback<Videos, Error>() {

                    @Override
                    public void onSuccess(Videos videos) {
                        stopVideoLoadingProgressBar();
                        if (videos.any()) {
                            showVideos(videos);
                            OnItemClickListener videoListOnItemClickListener = buildOnItemClickListenerForVideosList();
                            setOnItemClickListenerForVideosList(videoListOnItemClickListener);
                        } else
                            showNoVideosError();
                    }

                    @Override
                    public void onFailure(Error error) {
                        stopVideoLoadingProgressBar();
                        showVideoLoadingFailureError();
                    }

                    @Override
                    public void onUnexpectedFailure() {
                        Log.e(LOG_TAG, "Fetching Videos - Unexpected Failure");
                    }
                }).execute();
    }

    private void loadReviewsFor(Movie movie) {
        startReviewsLoadingProgressBar();
        Context context = getContext();
        new FetchReviewsTask(movie.idString(),
                context,
                new NetworkUtilities(context),
                new Callback<Reviews, Error>() {

                    @Override
                    public void onSuccess(Reviews reviews) {
                        stopReviewsLoadingProgressBar();
                        if (reviews.any()) showReviews(reviews);
                        else showNoReviewsError();
                    }

                    @Override
                    public void onFailure(Error error) {
                        stopReviewsLoadingProgressBar();
                        showReviewsLoadingFailureError();
                    }

                    @Override
                    public void onUnexpectedFailure() {
                        Log.e(LOG_TAG, "Fetching Reviews - Unexpected Failure");
                    }
                }).execute();
    }

    private void initializeStates() {
        String favoriteToggleText = movieDetailViewModel.favoriteToggleText();
        this.buttonToggleFavorite.setText(favoriteToggleText);
    }

    private void setEventListeners() {
        setOnClickListenerForFavoriteToggle();
    }

    private void setOnClickListenerForFavoriteToggle() {
        this.buttonToggleFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFavorite();
            }
        });
    }

    private void toggleFavorite() {
        movieDetailViewModel.toggleFavorite();
        movieDetailsPresenter.toggleFavorite(movie);
        MoviesPreference.getInstance(getContext()).setRefreshRequired();
    }

    private void setDefaultShareAction() {
        if (this.shareActionProvider == null) return;
        Intent intent = buildDefaultShareIntent();
        if (this.shareActionProvider != null) this.shareActionProvider.setShareIntent(intent);
    }

    private Intent buildDefaultShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, this.movie.originalTitle);
        intent.setType(MIME_TYPE_TEXT_PLAIN);
        return intent;
    }

    private void showPoster(Movie movie) {
        Picasso.with(getContext()).
                load(movie.imageUrlString(getContext(), ImageSize.MEDIUM))
                .placeholder(R.drawable.popflix_placeholder_medium)
                .into(this.moviePoster);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void startVideoLoadingProgressBar() {
        this.videoLoadingProgressBar.setVisibility(ProgressBar.VISIBLE);
        this.videoLoadingProgressBar.animate().start();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void stopVideoLoadingProgressBar() {
        this.videoLoadingProgressBar.animate().cancel();
        this.videoLoadingProgressBar.setVisibility(ProgressBar.GONE);
    }

    private void showVideos(Videos videos) {
        this.movieVideosAdapter = new MovieVideosAdapter(videos, getContext());
        this.listVideos.setVisibility(ListView.VISIBLE);
        this.listVideos.setAdapter(movieVideosAdapter);
        new ListUtilities(listVideos).setHeightToSumOfHeightsOfElements();
        setFirstVideoUrlStringShareAction(videos);
    }

    private void setOnItemClickListenerForVideosList(OnItemClickListener onItemClickListenerForVideosList) {
        this.listVideos.setOnItemClickListener(onItemClickListenerForVideosList);
    }

    private void showNoVideosError() {
        this.textVideoErrorMessage.setVisibility(TextView.VISIBLE);
        this.textVideoErrorMessage.setText(getString(R.string.error_videos_not_available));
    }

    private OnItemClickListener buildOnItemClickListenerForVideosList() {
        return new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Video video = movieVideosAdapter.getItem(position);
                Intent viewVideoIntent = new Intent(Intent.ACTION_VIEW, video.getYouTubeUri());
                if (viewVideoIntent.resolveActivity(getContext().getPackageManager()) != null)
                    startActivity(viewVideoIntent);
            }
        };
    }

    private void showVideoLoadingFailureError() {
        this.textVideoErrorMessage.setVisibility(TextView.VISIBLE);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void startReviewsLoadingProgressBar() {
        this.progressbarReviews.setVisibility(ProgressBar.VISIBLE);
        this.progressbarReviews.animate().start();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void stopReviewsLoadingProgressBar() {
        this.progressbarReviews.animate().cancel();
        this.progressbarReviews.setVisibility(ProgressBar.INVISIBLE);
    }

    private void showReviews(Reviews reviews) {
        this.movieReviewsAdapter = new MovieReviewsAdapter(reviews, getContext());
        this.listReviews.setVisibility(ListView.VISIBLE);
        this.listReviews.setAdapter(movieReviewsAdapter);
        new ListUtilities(this.listReviews).setHeightToSumOfHeightsOfElements();
    }

    private void showNoReviewsError() {
        this.textReviewsErrorMessage.setVisibility(TextView.VISIBLE);
        this.textReviewsErrorMessage.setText(getString(R.string.error_reviews_not_available));
    }

    private void showReviewsLoadingFailureError() {
        this.textReviewsErrorMessage.setVisibility(TextView.VISIBLE);
    }

    private void setFirstVideoUrlStringShareAction(Videos videos) {
        String firstVideoUrlString = videos.getYouTubeUrlStringForFirstVideo();
        Intent intent = buildFirstVideoUrlStringShareIntent(firstVideoUrlString);
        if (this.shareActionProvider != null) this.shareActionProvider.setShareIntent(intent);
    }

    private Intent buildFirstVideoUrlStringShareIntent(String firstVideoUrlString) {
        Intent firstVideoUrlStringShareIntent = new Intent(Intent.ACTION_SEND);
        firstVideoUrlStringShareIntent.putExtra(Intent.EXTRA_TEXT, firstVideoUrlString);
        firstVideoUrlStringShareIntent.setType(MIME_TYPE_TEXT_PLAIN);
        return firstVideoUrlStringShareIntent;
    }
}
