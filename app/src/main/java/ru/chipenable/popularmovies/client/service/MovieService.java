package ru.chipenable.popularmovies.client.service;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import retrofit.RestAdapter;
import ru.chipenable.popularmovies.connection.ConnectionStateReceiver;
import ru.chipenable.popularmovies.model.AllMovieDetails;
import ru.chipenable.popularmovies.model.moviedetail.MovieDetail;
import ru.chipenable.popularmovies.model.movielist.MovieList;
import ru.chipenable.popularmovies.model.movielist.Result;
import ru.chipenable.popularmovies.model.reviews.ReviewResult;
import ru.chipenable.popularmovies.model.reviews.Reviews;
import ru.chipenable.popularmovies.model.trailers.TrailerResult;
import ru.chipenable.popularmovies.model.trailers.Trailers;
import ru.chipenable.popularmovies.client.provider.MovieContract;
import ru.chipenable.popularmovies.utils.Utils;

/*
Using IntentService isn't good choice to create a REST client because you can't control its
queue of tasks and it makes some problems (for example you can't cancel old requests).
But I've found it too late to fix my mistake.
* */

public class MovieService extends IntentService {

    private static final String TAG = "MovieService";

    public static final int GET_ALL_MOVIE_DETAILS = 1;
    public static final int GET_MOVIE_LIST = 2;
    public static final int SAVE_MOVIE = 3;
    public static final int DELETE_MOVIE = 4;

    private static final String SERVICE_COM = "service_com";
    private static final String TYPE_SORT = "type_sort";
    private static final String PAGE = "page";
    private static final String MOVIE_ID = "movie_id";
    private static final String MESSENGER   = "messenger";
    private static final String SERVICE_EXCEPTION = "exception";
    private static final String MOVIE_DETAILS = "movie_details";
    private static final String MOVIE_TRAILERS = "movie_trailers";
    private static final String MOVIE_REVIEWS = "movie_reviews";


    private static Intent makeIntent(Context context, int com, long id, Handler handler){
        Intent intent = new Intent(context, MovieService.class);
        intent.putExtra(SERVICE_COM, com);
        intent.putExtra(MOVIE_ID, id);
        intent.putExtra(MESSENGER, new Messenger(handler));
        return intent;
    }

    public static void getMovieList(Context context, String sortPar, int page, Handler handler){
        Intent intent = makeIntent(context, GET_MOVIE_LIST, 0, handler);
        intent.putExtra(TYPE_SORT, sortPar);
        intent.putExtra(PAGE, page);
        context.startService(intent);
    }

    public static void getAllMovieDetails(Context context, long id, Handler handler){
        Intent intent = makeIntent(context, GET_ALL_MOVIE_DETAILS, id, handler);
        context.startService(intent);
    }

    public static void saveMovie(Context context, long id, AllMovieDetails allMovieDetails, Handler handler){
        Intent intent = makeIntent(context, SAVE_MOVIE, id, handler);

        ContentValues details = new ContentValues();
        details.put(MovieContract.MovieTable.COL_MOVIE_ID, id);
        details.put(MovieContract.MovieTable.COL_TITLE, allMovieDetails.getMovieDetail().getTitle());
        details.put(MovieContract.MovieTable.COL_POSTER_PATH, allMovieDetails.getMovieDetail().getPosterPath());
        details.put(MovieContract.MovieTable.COL_RELEASE_DATE, allMovieDetails.getMovieDetail().getReleaseDate());
        details.put(MovieContract.MovieTable.COL_RATING, allMovieDetails.getMovieDetail().getVoteAverage());
        details.put(MovieContract.MovieTable.COL_DURATION, allMovieDetails.getMovieDetail().getRuntime());
        details.put(MovieContract.MovieTable.COL_SYNOPSIS, allMovieDetails.getMovieDetail().getOverview());
        int flag = (allMovieDetails.getFavoriteFlag())? 1 : 0;
        details.put(MovieContract.MovieTable.COL_FAVORITE, flag);
        intent.putExtra(MOVIE_DETAILS, details);

        ArrayList<ContentValues> trailers = new ArrayList<>();
        List<TrailerResult> listTrailers = allMovieDetails.getTrailers().getResults();
        for(TrailerResult tr: listTrailers){
            ContentValues cv = new ContentValues();
            cv.put(MovieContract.TrailerTable.COL_MOVIE_ID, id);
            cv.put(MovieContract.TrailerTable.COL_TITLE, tr.getName());
            cv.put(MovieContract.TrailerTable.COL_KEY, tr.getKey());
            trailers.add(cv);
        }
        intent.putExtra(MOVIE_TRAILERS, trailers);

        ArrayList<ContentValues> reviews = new ArrayList<>();
        List<ReviewResult> listReviews = allMovieDetails.getReviews().getResults();
        for(ReviewResult rr: listReviews){
            ContentValues cv = new ContentValues();
            cv.put(MovieContract.ReviewTable.COL_MOVIE_ID, id);
            cv.put(MovieContract.ReviewTable.COL_AUTHOR, rr.getAuthor());
            cv.put(MovieContract.ReviewTable.COL_CONTENT, rr.getContent());
            reviews.add(cv);
        }
        intent.putExtra(MOVIE_REVIEWS, reviews);

        context.startService(intent);
    }

    public static void deleteMovie(Context context, long id, Handler handler){
        Intent intent = makeIntent(context, DELETE_MOVIE, id, handler);
        context.startService(intent);
    }

    /***************************************************************************/

    public MovieService() {
        super("MovieService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int com = intent.getIntExtra(SERVICE_COM, 0);
        if (com == 0){
            return;
        }

        boolean connectionState = ConnectionStateReceiver.checkConnection(this);

        MovieClient client = new RestAdapter.Builder()
                    .setEndpoint(MovieClient.ENDPOINT)
                    .build()
                    .create(MovieClient.class);

        Message msg = Message.obtain();
        msg.obj = null;
        msg.arg1 = com;
        msg.arg2 = -1;
        Exception exception = null;

        switch(com){
            case SAVE_MOVIE: {
                Log.d(TAG, "save movie");

                //preparation of data to save
                ContentValues details = intent.getParcelableExtra(MOVIE_DETAILS);
                ArrayList<ContentValues> trailerList = intent.getParcelableArrayListExtra(MOVIE_TRAILERS);
                ArrayList<ContentValues> reviewList = intent.getParcelableArrayListExtra(MOVIE_REVIEWS);
                long id = intent.getLongExtra(MOVIE_ID, 0);
                Uri uri = MovieContract.MovieTable.buildUri(id);

                //downloading of a movie poster
                String imageUrl = MovieClient.BASE_IMG_PATH + details.get(MovieContract.MovieTable.COL_POSTER_PATH);
                Uri result = Utils.downloadImage(this, imageUrl, Long.toString(id) + ".jpg");
                if (result != null){
                    details.put(MovieContract.MovieTable.COL_POSTER_LOCAL_PATH, result.toString());
                }

                //check the database if it has the same data or not
                ContentResolver resolver = getApplicationContext().getContentResolver();
                Cursor cursor = resolver.query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    //update existing data?

                }
                else{
                    //insert movie details, trailers and reviews in database
                    resolver.insert(MovieContract.MovieTable.CONTENT_URI, details);
                    for(ContentValues cv: trailerList) {
                        resolver.insert(MovieContract.TrailerTable.CONTENT_URI, cv);
                    }
                    for(ContentValues cv: reviewList){
                        resolver.insert(MovieContract.ReviewTable.CONTENT_URI, cv);
                    }
                }

                if (cursor != null){
                    cursor.close();
                }
                msg.arg2 = 0;
                break;
            }

            case DELETE_MOVIE: {
                Log.d(TAG, "delete movie");
                long id = intent.getLongExtra(MOVIE_ID, 0);

                //delete movie data from the database
                Uri uri = MovieContract.MovieTable.buildUri(id);
                ContentResolver resolver = getApplicationContext().getContentResolver();
                int movieRows = resolver.delete(uri, null, null);
                Log.d(TAG, "uri: " + uri.toString() + " rowDeleted: " + Integer.toString(movieRows));

                uri = MovieContract.TrailerTable.buildUri(id);
                int trailerRows = resolver.delete(uri, null, null);
                Log.d(TAG, "uri: " + uri.toString() + " rowDeleted: " + Integer.toString(trailerRows));

                uri = MovieContract.ReviewTable.buildUri(id);
                int reviewRows = resolver.delete(uri, null, null);
                Log.d(TAG, "uri: " + uri.toString() + " rowDeleted: " + Integer.toString(reviewRows));

                //delete an image from the gallery
                String imageName = Long.toString(id) + ".jpg";
                Utils.deleteFile(this, imageName);

                if(movieRows != 0){
                    msg.arg2 = 0;
                }
                break;
            }

            case GET_MOVIE_LIST:{
                Log.d(TAG, "get movie list");
                try{
                    String typeSort = intent.getStringExtra(TYPE_SORT);
                    int page = intent.getIntExtra(PAGE, 1);

                    if (typeSort.equals(MovieClient.FAVORITE)){

                        ContentResolver resolver = getApplicationContext().getContentResolver();
                        long id = intent.getLongExtra(MOVIE_ID, 0);
                        Uri uri = MovieContract.MovieTable.CONTENT_URI;
                        Cursor cursor = resolver.query(uri, null, null, null, null);

                        if (cursor != null && cursor.moveToFirst()){
                            MovieList movieList = new MovieList();
                            for(int i = 0; i < cursor.getCount(); i++){
                                Result movie = new Result();
                                int colIndex = cursor.getColumnIndex(MovieContract.MovieTable.COL_MOVIE_ID);
                                movie.setId(cursor.getLong(colIndex));

                                colIndex = cursor.getColumnIndex(MovieContract.MovieTable.COL_POSTER_PATH);
                                movie.setPosterPath(cursor.getString(colIndex));

                                colIndex = cursor.getColumnIndex(MovieContract.MovieTable.COL_POSTER_LOCAL_PATH);
                                movie.setPosterLocalPath(cursor.getString(colIndex));

                                movieList.addResult(movie);
                                cursor.moveToNext();
                            }
                            movieList.setPage(1);
                            movieList.setTotalPages(1);
                            msg.obj = movieList;
                            cursor.close();
                        }
                    }
                    else {
                        if (connectionState) {
                            Log.d(TAG, "send request to web service");
                            msg.obj = client.getMovieList(typeSort, page);
                        }
                    }
                    msg.arg2 = page;
                }
                catch(Exception e){
                    exception = e;
                }
                break;
            }

            case GET_ALL_MOVIE_DETAILS: {
                Log.d(TAG, "get all movie details");
                try {
                    long id = intent.getLongExtra(MOVIE_ID, 0);
                    Uri uri = MovieContract.MovieTable.buildUri(id);
                    ContentResolver resolver = getApplicationContext().getContentResolver();
                    Cursor detailCursor = resolver.query(uri, null, null, null, null);

                    if (detailCursor != null && detailCursor.moveToFirst()) {

                        uri = MovieContract.TrailerTable.buildUri(id);
                        Cursor trailerCursor = resolver.query(uri, null, null, null, null);

                        uri = MovieContract.ReviewTable.buildUri(id);
                        Cursor reviewCursor = resolver.query(uri, null, null, null, null);

                        msg.obj = cursorToMovieDetails(detailCursor, trailerCursor, reviewCursor);
                        msg.arg2 = 0;

                        detailCursor.close();
                        if (trailerCursor != null){
                            trailerCursor.close();
                        }
                        if (reviewCursor != null){
                            reviewCursor.close();
                        }
                    }
                    else {
                        if (connectionState) {
                            MovieDetail movieDetail = client.getMovieDetails(id);
                            Trailers trailers = client.getMovieTrailers(id);
                            Reviews reviews = client.getReviews(id);
                            msg.obj = new AllMovieDetails(movieDetail, trailers, reviews, false);
                            msg.arg2 = 0;
                        }
                    }

                }
                catch (Exception e) {
                    exception = e;
                }
                break;
            }

            default:
                msg.arg1 = 0;
                break;
        }

        if (exception != null) {
            Log.d(TAG, "exception: " + exception.getMessage());
        }

        Messenger messenger = intent.getParcelableExtra(MESSENGER);
        if (messenger != null) {
            try {
                Bundle bundle = new Bundle();
                bundle.putSerializable(SERVICE_EXCEPTION, exception);
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    public static Exception getException(Message msg){
        Bundle bundle = msg.getData();
        Exception exception = (Exception) bundle.getSerializable(SERVICE_EXCEPTION);
        return exception;
    }


    private AllMovieDetails cursorToMovieDetails(Cursor detailCursor, Cursor trailerCursor, Cursor reviewCursor){
        MovieDetail movieDetail = new MovieDetail();

        int colIndex = detailCursor.getColumnIndex(MovieContract.MovieTable.COL_MOVIE_ID);
        long id = detailCursor.getLong(colIndex);
        movieDetail.setId(id);

        colIndex = detailCursor.getColumnIndex(MovieContract.MovieTable.COL_TITLE);
        String title = detailCursor.getString(colIndex);
        movieDetail.setTitle(title);

        colIndex = detailCursor.getColumnIndex(MovieContract.MovieTable.COL_POSTER_PATH);
        String posterPath = detailCursor.getString(colIndex);
        movieDetail.setPosterPath(posterPath);

        colIndex = detailCursor.getColumnIndex(MovieContract.MovieTable.COL_POSTER_LOCAL_PATH);
        String posterLocalPath = detailCursor.getString(colIndex);
        movieDetail.setPosterLocalPath(posterLocalPath);

        colIndex = detailCursor.getColumnIndex(MovieContract.MovieTable.COL_RELEASE_DATE);
        String releaseDate = detailCursor.getString(colIndex);
        movieDetail.setReleaseDate(releaseDate);

        colIndex = detailCursor.getColumnIndex(MovieContract.MovieTable.COL_DURATION);
        int duration = detailCursor.getInt(colIndex);
        movieDetail.setRuntime(duration);

        colIndex = detailCursor.getColumnIndex(MovieContract.MovieTable.COL_RATING);
        double rating = detailCursor.getDouble(colIndex);
        movieDetail.setVoteAverage(rating);

        colIndex = detailCursor.getColumnIndex(MovieContract.MovieTable.COL_SYNOPSIS);
        String synopsis = detailCursor.getString(colIndex);
        movieDetail.setOverview(synopsis);

        colIndex  = detailCursor.getColumnIndex(MovieContract.MovieTable.COL_FAVORITE);
        boolean favorite = !(detailCursor.getInt(colIndex) == 0);


        List<TrailerResult> trailerList = new ArrayList<>();
        trailerCursor.moveToFirst();
        for(int i = 0; i < trailerCursor.getCount(); i++){
            TrailerResult trailerResult = new TrailerResult();
            colIndex = trailerCursor.getColumnIndex(MovieContract.TrailerTable.COL_TITLE);
            String trailerName = trailerCursor.getString(colIndex);
            trailerResult.setName(trailerName);

            colIndex = trailerCursor.getColumnIndex(MovieContract.TrailerTable.COL_KEY);
            String trailerKey = trailerCursor.getString(colIndex);
            trailerResult.setKey(trailerKey);

            trailerList.add(trailerResult);
            trailerCursor.moveToNext();
        }

        Trailers trailers = new Trailers();
        trailers.setResults(trailerList);

        List<ReviewResult> reviewList = new ArrayList<>();
        reviewCursor.moveToFirst();
        for(int i = 0; i < reviewCursor.getCount(); i++){
            ReviewResult reviewResult = new ReviewResult();
            colIndex = reviewCursor.getColumnIndex(MovieContract.ReviewTable.COL_AUTHOR);
            String author = reviewCursor.getString(colIndex);
            reviewResult.setAuthor(author);

            colIndex = reviewCursor.getColumnIndex(MovieContract.ReviewTable.COL_CONTENT);
            String content = reviewCursor.getString(colIndex);
            reviewResult.setContent(content);

            reviewList.add(reviewResult);
            reviewCursor.moveToNext();
        }

        Reviews reviews = new Reviews();
        reviews.setResults(reviewList);

        AllMovieDetails allMovieDetails = new AllMovieDetails(movieDetail, trailers, reviews, favorite);
        return allMovieDetails;
    }
}
