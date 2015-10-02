package ru.chipenable.popularmovies.client.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Pashgan on 11.09.2015.
 */
public class MovieProvider extends ContentProvider {

    private static final String TAG = "MovieProvider";

    private MovieDbHelper mMovieDbHelper;
    private static final UriMatcher sUriMatcher;

    private static final int ALL_MOVIES = 1;
    private static final int MOVIE = 2;
    private static final int ALL_TRAILERS = 3;
    private static final int TRAILER = 4;
    private static final int ALL_REVIEWS = 5;
    private static final int REVIEW = 6;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE, ALL_MOVIES);
        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE + "/#", MOVIE);
        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_TRAILER, ALL_TRAILERS);
        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_TRAILER + "/#", TRAILER);
        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_REVIEW, ALL_REVIEWS);
        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_REVIEW + "/#", REVIEW);
    }

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate");
        mMovieDbHelper = MovieDbHelper.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "query function");
        final SQLiteDatabase db = mMovieDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor retCursor = null;

        switch (match){
            case ALL_MOVIES:{
                Log.d(TAG, "get list movie");
                retCursor = db.query(MovieContract.MovieTable.TABLE_NAME, null, null, null,
                        null, null, sortOrder);
                break;
            }

            case MOVIE:{
                Log.d(TAG, "get movie");
                String id = uri.getLastPathSegment();

                retCursor = db.query(MovieContract.MovieTable.TABLE_NAME,
                        null,
                        MovieContract.MovieTable.COL_MOVIE_ID + "=?",
                        new String[] {id},
                        null,
                        null,
                        sortOrder);
                break;
            }

            case TRAILER:{
                Log.d(TAG, "get trailers");
                String id = uri.getLastPathSegment();

                retCursor = db.query(MovieContract.TrailerTable.TABLE_NAME,
                        null,
                        MovieContract.TrailerTable.COL_MOVIE_ID + "=?",
                        new String[] {id},
                        null,
                        null,
                        sortOrder);
                break;
            }

            case REVIEW:{
                Log.d(TAG, "get reviews");
                String id = uri.getLastPathSegment();

                retCursor = db.query(MovieContract.ReviewTable.TABLE_NAME,
                        null,
                        MovieContract.ReviewTable.COL_MOVIE_ID + "=?",
                        new String[] {id},
                        null,
                        null,
                        sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }

        if (retCursor != null) {
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "insert function");
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);

        Uri returnUri = null;
        long id = 0;
        long movieId = 0;

        switch (match){
            case ALL_MOVIES: {
                Log.d(TAG, "insert movie");
                id = db.insert(MovieContract.MovieTable.TABLE_NAME, null, values);
                movieId = values.getAsLong(MovieContract.MovieTable.COL_MOVIE_ID);
                returnUri = MovieContract.MovieTable.buildUri(movieId);
                break;
            }

            case ALL_TRAILERS:{
                Log.d(TAG, "insert trailer");
                id = db.insert(MovieContract.TrailerTable.TABLE_NAME, null, values);
                movieId = values.getAsLong(MovieContract.TrailerTable.COL_MOVIE_ID);
                returnUri = MovieContract.TrailerTable.buildUri(movieId);
                break;
            }

            case ALL_REVIEWS:{
                Log.d(TAG, "insert reviews");
                id = db.insert(MovieContract.ReviewTable.TABLE_NAME, null, values);
                movieId = values.getAsLong(MovieContract.ReviewTable.COL_MOVIE_ID);
                returnUri = MovieContract.ReviewTable.buildUri(movieId);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (id <= 0){
            throw new android.database.SQLException("Failed to insert row into " + uri);
        }

        if (returnUri != null) {
            Log.d(TAG, returnUri.toString());
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete function");
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int rowsDeleted = 0;

        switch(match){
            case ALL_MOVIES: {
                Log.d(TAG, "delete all movies");
                rowsDeleted = db.delete(MovieContract.MovieTable.TABLE_NAME, null, null);
                break;
            }

            case MOVIE: {
                Log.d(TAG, "delete certain movie");
                String[] selArgs = new String[]{uri.getLastPathSegment()};
                rowsDeleted = db.delete(MovieContract.MovieTable.TABLE_NAME,
                        MovieContract.MovieTable.COL_MOVIE_ID + "=?",
                        selArgs);
                break;
            }

            case ALL_TRAILERS: {
                Log.d(TAG, "delete all trailers");
                rowsDeleted = db.delete(MovieContract.TrailerTable.TABLE_NAME, null, null);
                break;
            }

            case TRAILER: {
                Log.d(TAG, "delete certain trailers");
                String[] selArgs = new String[] {uri.getLastPathSegment()};
                rowsDeleted = db.delete(MovieContract.TrailerTable.TABLE_NAME,
                        MovieContract.TrailerTable.COL_MOVIE_ID + "=?",
                        selArgs);
                break;
            }

            case ALL_REVIEWS:{
                Log.d(TAG, "delete all reviews");
                rowsDeleted = db.delete(MovieContract.ReviewTable.TABLE_NAME, null, null);
                break;
            }

            case REVIEW: {
                Log.d(TAG, "delete certain reviews");
                String[] selArgs = new String[] {uri.getLastPathSegment()};
                rowsDeleted = db.delete(MovieContract.ReviewTable.TABLE_NAME,
                        MovieContract.ReviewTable.COL_MOVIE_ID + "=?",
                        selArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        Log.d(TAG, "rowsDeleted = " + Integer.toString(rowsDeleted));
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        //this method isn't used

        return 0;
    }
}
