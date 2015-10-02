package ru.chipenable.popularmovies.client.provider;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by Pashgan on 10.09.2015.
 */
public class MovieDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "movies.db";
    public static final int DATABASE_VERSION = 14;

    private static MovieDbHelper sDatabase;

    public static synchronized MovieDbHelper getInstance(Context context) {
        if (sDatabase == null) {
            sDatabase = new MovieDbHelper(context.getApplicationContext());
        }
        return sDatabase;
    }

    private MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createMovieTable = "CREATE TABLE " + MovieContract.MovieTable.TABLE_NAME + " (" +
                MovieContract.MovieTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieContract.MovieTable.COL_MOVIE_ID + " LONG UNIQUE NOT NULL, " +
                MovieContract.MovieTable.COL_TITLE        + " TEXT, " +
                MovieContract.MovieTable.COL_POSTER_PATH  + " TEXT, " +
                MovieContract.MovieTable.COL_POSTER_LOCAL_PATH + " TEXT, " +
                MovieContract.MovieTable.COL_RELEASE_DATE + " TEXT, " +
                MovieContract.MovieTable.COL_DURATION + " INTEGER, " +
                MovieContract.MovieTable.COL_RATING   + " REAL, "   +
                MovieContract.MovieTable.COL_SYNOPSIS + " TEXT, "   +
                MovieContract.MovieTable.COL_FAVORITE + " INTEGER " +
                ");";

        db.execSQL(createMovieTable);

        String createTrailerTable = "CREATE TABLE " + MovieContract.TrailerTable.TABLE_NAME + " (" +
                MovieContract.TrailerTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieContract.TrailerTable.COL_MOVIE_ID + " LONG NOT NULL, " +
                MovieContract.TrailerTable.COL_TITLE + " TEXT, " +
                MovieContract.TrailerTable.COL_KEY + " TEXT " +
                ");";

        db.execSQL(createTrailerTable);

        String createReviewTable = "CREATE TABLE " + MovieContract.ReviewTable.TABLE_NAME + " (" +
                MovieContract.ReviewTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieContract.ReviewTable.COL_MOVIE_ID + " LONG NOT NULL, " +
                MovieContract.ReviewTable.COL_AUTHOR + " TEXT, " +
                MovieContract.ReviewTable.COL_CONTENT + " TEXT " +
                ");";

        db.execSQL(createReviewTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String deleteTable = "DROP TABLE IF EXISTS ";
        db.execSQL(deleteTable + MovieContract.MovieTable.TABLE_NAME);
        db.execSQL(deleteTable + MovieContract.TrailerTable.TABLE_NAME);
        db.execSQL(deleteTable + MovieContract.ReviewTable.TABLE_NAME);
        onCreate(db);
    }


}
