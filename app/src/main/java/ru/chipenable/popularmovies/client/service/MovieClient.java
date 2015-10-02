package ru.chipenable.popularmovies.client.service;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.QueryMap;
import ru.chipenable.popularmovies.model.moviedetail.MovieDetail;
import ru.chipenable.popularmovies.model.movielist.MovieList;
import ru.chipenable.popularmovies.model.reviews.Reviews;
import ru.chipenable.popularmovies.model.trailers.Trailers;

/**
 * Created by Pashgan on 07.07.2015.
 */
public interface MovieClient {

    /*You must add your value of the key !!!!!*/
    String KEY = "";

    String ENDPOINT = "http://api.themoviedb.org/3";
    String REQ_DISCOVER = "/discover/movie";
    String REQ_MOVIES = "/movie/{id}";
    String REQ_TRAILERS = "/movie/{id}/videos";
    String REQ_REVIEWS = "/movie/{id}/reviews";

    String ID = "id";
    String BASE_IMG_PATH = "http://image.tmdb.org/t/p/w185/";
    String BASE_IMG_PATH_300 = "http://image.tmdb.org/t/p/w300/";
    String API_KEY = "?api_key=" + KEY;

    String PAR_PAGE = "page";
    String PAR_SORT_BY = "sort_by";
    String POPULARITY = "popularity.desc";
    String VOTE_AVERAGE = "vote_average.desc";
    String FAVORITE = "favorite";


    @GET(REQ_DISCOVER + API_KEY)
    MovieList getMovieList(@Query(PAR_SORT_BY) String par, @Query(PAR_PAGE) int page);

    @GET(REQ_MOVIES + API_KEY)
    MovieDetail getMovieDetails(@Path(ID) long id);

    @GET(REQ_TRAILERS + API_KEY)
    Trailers getMovieTrailers(@Path(ID) long id);

    @GET(REQ_REVIEWS + API_KEY)
    Reviews getReviews(@Path(ID) long id);
}