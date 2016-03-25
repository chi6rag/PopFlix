package net.chiragaggarwal.android.popflix.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.Nullable;

import java.text.ParseException;

public class MoviesProvider extends ContentProvider {
    private static final int MOVIES_ENDPOINT = 0;
    private static final int MOVIE_ENDPOINT = 1;
    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        addMoviesUri();
        addMovieUri();
    }

    private SQLiteOpenHelper databaseHelper;

    @Override
    public boolean onCreate() {
        this.databaseHelper = DatabaseHelper.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        int matchCode = uriMatcher.match(uri);
        if (matchCode == MOVIES_ENDPOINT)
            return PopFlixContract.MoviesEntry.buildMoviesMimeType();
        if (matchCode == MOVIE_ENDPOINT)
            return PopFlixContract.MoviesEntry.buildMovieMimeType();
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues movieContentValues) {
        int matchCode = uriMatcher.match(uri);
        try {
            if (matchCode == MOVIES_ENDPOINT) {
                SQLiteDatabase database = databaseHelper.getWritableDatabase();
                Long id = MoviesGateway.getInstance(database).insertIfFavorite(movieContentValues);
                return PopFlixContract.MoviesEntry.buildMovieUri(id);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private static void addMoviesUri() {
        MoviesProvider.uriMatcher.addURI(PopFlixContract.MoviesEntry.PROVIDER_AUTHORITY,
                PopFlixContract.MoviesEntry.MOVIE_PATH, MOVIE_ENDPOINT);
    }

    private static void addMovieUri() {
        MoviesProvider.uriMatcher.addURI(PopFlixContract.MoviesEntry.PROVIDER_AUTHORITY,
                PopFlixContract.MoviesEntry.MOVIES_PATH, MOVIES_ENDPOINT);
    }
}
