package ru.chipenable.popularmovies.model;

import ru.chipenable.popularmovies.model.moviedetail.MovieDetail;
import ru.chipenable.popularmovies.model.reviews.Reviews;
import ru.chipenable.popularmovies.model.trailers.Trailers;

/**
 * Created by Pashgan on 22.09.2015.
 */
public class AllMovieDetails {

    private MovieDetail mMovieDetail;
    private Trailers mTrailers;
    private Reviews mReviews;
    private boolean mIsFavorite;

    public AllMovieDetails(MovieDetail movieDetail, Trailers trailers, Reviews reviews, boolean flag){
        mMovieDetail = movieDetail;
        mTrailers = trailers;
        mReviews = reviews;
        mIsFavorite = flag;
    }

    public MovieDetail getMovieDetail() {
        return mMovieDetail;
    }

    public void setMovieDetail(MovieDetail mMovieDetail) {
        this.mMovieDetail = mMovieDetail;
    }

    public Trailers getTrailers() {
        return mTrailers;
    }

    public void setTrailers(Trailers mTrailers) {
        this.mTrailers = mTrailers;
    }

    public Reviews getReviews(){
        return mReviews;
    }

    public void setReviews(Reviews reviews){
        this.mReviews = reviews;
    }

    public void setFavoriteFlag(boolean flag){
        mIsFavorite = flag;
    }

    public boolean getFavoriteFlag(){
        return mIsFavorite;
    }
}
