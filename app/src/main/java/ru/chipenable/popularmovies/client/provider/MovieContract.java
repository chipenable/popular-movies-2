package ru.chipenable.popularmovies.client.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Pashgan on 10.09.2015.
 */
public final class MovieContract  {

    public static final String CONTENT_AUTHORITY = "ru.chipenable.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIE = "movie";
    public static final String PATH_TRAILER = "trailer";
    public static final String PATH_REVIEW = "review";


    public MovieContract(){}

    public static abstract class MovieTable implements BaseColumns{

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String TABLE_NAME = "movie_table";
        public static final String COL_MOVIE_ID = "movie_id";
        public static final String COL_TITLE = "title";
        public static final String COL_SYNOPSIS = "synopsis";
        public static final String COL_RELEASE_DATE = "release_date";
        public static final String COL_DURATION = "duration";
        public static final String COL_RATING = "rating";
        public static final String COL_POSTER_PATH = "poster_path";
        public static final String COL_POSTER_LOCAL_PATH = "poster_loc_path";
        public static final String COL_FAVORITE = "favorite";

        public static Uri buildUri(long id) {
            return Uri.withAppendedPath(CONTENT_URI, Long.toString(id));
        }
    }

    public static abstract class TrailerTable implements BaseColumns{

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILER).build();

        public static final String TABLE_NAME = "trailer_table";
        public static final String COL_MOVIE_ID = "movie_id";
        public static final String COL_TITLE = "title";
        public static final String COL_KEY = "key";

        public static Uri buildUri(long id) {
            return Uri.withAppendedPath(CONTENT_URI, Long.toString(id));
        }
    }

    public static abstract class ReviewTable implements BaseColumns{

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEW).build();

        public static final String TABLE_NAME = "review_table";
        public static final String COL_MOVIE_ID = "movie_id";
        public static final String COL_AUTHOR = "author";
        public static final String COL_CONTENT = "content";

        public static Uri buildUri(long id) {
            return Uri.withAppendedPath(CONTENT_URI, Long.toString(id));
        }
    }

}
