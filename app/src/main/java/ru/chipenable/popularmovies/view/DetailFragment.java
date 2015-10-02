package ru.chipenable.popularmovies.view;

import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.chipenable.popularmovies.R;
import ru.chipenable.popularmovies.client.service.MovieClient;
import ru.chipenable.popularmovies.client.service.MovieService;
import ru.chipenable.popularmovies.model.AllMovieDetails;
import ru.chipenable.popularmovies.model.Command;
import ru.chipenable.popularmovies.model.moviedetail.MovieDetail;
import ru.chipenable.popularmovies.model.reviews.ReviewResult;
import ru.chipenable.popularmovies.model.reviews.Reviews;
import ru.chipenable.popularmovies.model.trailers.TrailerResult;
import ru.chipenable.popularmovies.model.trailers.Trailers;

/**
 * Created by Pashgan on 07.07.2015.
 */
public class DetailFragment extends BaseFragment {

    public static final String TAG = "DetailFragment";
    private static final String MOVIE_ID = "movie_id";

    private static final int MAX_AMOUNT_TRAILERS = 5;
    private static final int MAX_AMOUNT_REVIEWS = 5;

    //view components
    @Bind(R.id.title) TextView mTitle;
    @Bind(R.id.date) TextView mDate;
    @Bind(R.id.duration) TextView mDuration;
    @Bind(R.id.rating) TextView mRating;
    @Bind(R.id.like_button) Button mLikeButton;
    @Bind(R.id.poster) ImageView mPoster;
    @Bind(R.id.plot) TextView mPlot;
    @Bind(R.id.trailer_list) LinearLayout mTrailerList;
    @Bind(R.id.review_list) LinearLayout mReviewList;

    private Handler mHandler;
    private long mMovieId;
    private AllMovieDetails mAllMovieDetails;
    private boolean mFailureFlag;
    private ShareActionProvider mShareActionProvider;

    /*********************************************************/

    public static DetailFragment newInstance(long movieId) {
        Bundle bundle = new Bundle();
        bundle.putLong(MOVIE_ID, movieId);
        DetailFragment detailFragment = new DetailFragment();
        detailFragment.setArguments(bundle);
        return detailFragment;
    }

    private long getMovieId() {
        return getArguments().getLong(MOVIE_ID);
    }

    /**********************************************************/

    @Override
    public void updateData(boolean connectionState) {
        Log.d(TAG, "network state: " + Boolean.toString(connectionState));
        if (connectionState && mFailureFlag) {
            downloadMovieDetails();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mMovieId = getMovieId();
        Log.d(TAG, "id: " + Long.toString(mMovieId));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, view);

        Point screenSize = getScreenSize();
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mPoster.getLayoutParams();
        layoutParams.height = screenSize.y / getResources().getInteger(R.integer.poster_div);
        mPoster.setLayoutParams(layoutParams);

        mHandler = new Handler(new Handler.Callback() {

            @Override
            public boolean handleMessage(Message msg) {
                Log.d(TAG, "handler");
                int com    = msg.arg1;
                int result = msg.arg2;
                Object obj = msg.obj;

                switch (com) {
                    case MovieService.DELETE_MOVIE:
                        if (result == 0){
                            mLikeButton.setText(R.string.mark_as_favorite);
                        }
                        mLikeButton.setEnabled(true);
                        break;

                    case MovieService.SAVE_MOVIE:
                        if (result == 0){
                            mLikeButton.setText(R.string.favorite);
                        }
                        mLikeButton.setEnabled(true);
                        break;

                    case MovieService.GET_ALL_MOVIE_DETAILS:
                        if (obj != null){
                            mAllMovieDetails = (AllMovieDetails)obj;
                            displayMovieData(mAllMovieDetails);
                            mShareActionProvider.setShareIntent(createShareIntent());
                        }
                        else{
                            mFailureFlag = true;
                        }
                        break;

                    default:
                }

                mCallback.fragmentCallback(Command.STOP_DOWNLOADING, 0);
                return false;
            }
        });

        downloadMovieDetails();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.like_button)
    public void markAsFavorite(){

        if (mAllMovieDetails.getFavoriteFlag()) {
            mAllMovieDetails.setFavoriteFlag(false);
            MovieService.deleteMovie(getActivity(), mMovieId, mHandler);
        }
        else{
            mAllMovieDetails.setFavoriteFlag(true);
            MovieService.saveMovie(getActivity(), mMovieId, mAllMovieDetails, mHandler);
        }
        mCallback.fragmentCallback(Command.START_DOWNLOADING, 0);
        mLikeButton.setEnabled(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate menu resource file.
        inflater.inflate(R.menu.menu_detail, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
    }

    //**********************************************************************

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        String shareData = null;
        if (mAllMovieDetails != null) {
            MovieDetail movieDetail = mAllMovieDetails.getMovieDetail();
            String title = movieDetail.getTitle();
            String overview = movieDetail.getOverview();

            String trailerPath = "";
            List<TrailerResult> trailerList = mAllMovieDetails.getTrailers().getResults();
            if (trailerList!= null && trailerList.size() > 0) {
                String key = trailerList.get(0).getKey();
                if (key != null && !key.equals("")) {
                    trailerPath = "http://www.youtube.com/watch?v=" + key;
                }
            }
            StringBuilder stringBuilder = new StringBuilder();
            int overviewEnd = (overview.length() < 200)? overview.length(): 200;
            stringBuilder.append(title + "\r\n")
                    .append(overview.substring(0, overviewEnd) + "...\r\n")
                    .append(trailerPath);
            shareData = stringBuilder.toString();
        }
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareData);
        return shareIntent;
    }

    private void downloadMovieDetails() {
        /*if (!ConnectionStateReceiver.checkConnection(getActivity())){
            Toast.makeText(getActivity(), getString(R.string.connection_problem), Toast.LENGTH_LONG).show();
            return;
        }*/

        if (mAllMovieDetails == null){
            Log.d(TAG, "download all movie details");
            mFailureFlag = false;
            MovieService.getAllMovieDetails(getActivity(), mMovieId, mHandler);
            mCallback.fragmentCallback(Command.START_DOWNLOADING, 0);
        }
        else{
            Log.d(TAG, "display all movie details");
            displayMovieData(mAllMovieDetails);
        }
    }

    private void displayMovieData(AllMovieDetails allMovieDetails){
        displayDetails(allMovieDetails.getMovieDetail());
        displayTrailers(allMovieDetails.getTrailers());
        displayReviews(allMovieDetails.getReviews());
    }

    private void displayDetails(MovieDetail movieDetail) {
        if (movieDetail == null || getActivity() == null) {
            return;
        }

        Log.d(TAG, "display movie details");
        mTitle.setText(movieDetail.getTitle());
        mDate.setText(movieDetail.getReleaseDate());

        String duration = Integer.toString(movieDetail.getRuntime()) + " " + getString(R.string.min);
        mDuration.setText(duration);

        String rating = Double.toString(movieDetail.getVoteAverage()) + "/10.0";
        mRating.setText(rating);

        String posterLocalPath = movieDetail.getPosterLocalPath();
        if (posterLocalPath == null) {
            String posterUrl = MovieClient.BASE_IMG_PATH_300 + movieDetail.getPosterPath();
            Picasso.with(getActivity())
                    .load(posterUrl)
                    .error(R.drawable.noposter)
                    .into(mPoster);
        }
        else{
            Uri posterUri = Uri.parse(posterLocalPath);
            mPoster.setImageURI(posterUri);
        }

        if (mAllMovieDetails.getFavoriteFlag()) {
            mLikeButton.setText("favorite");
        }
        mLikeButton.setVisibility(View.VISIBLE);

        mPlot.setText(movieDetail.getOverview());
    }

    void displayTrailers(Trailers trailers) {
        Log.d(TAG, "display trailers");
        if (getActivity() == null || trailers == null || trailers.getResults().size() == 0) {
            return;
        }

        mTrailerList.setVisibility(View.VISIBLE);
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        List<TrailerResult> trailerList = trailers.getResults();
        int listSize = trailerList.size();
        listSize = (trailerList.size() < MAX_AMOUNT_TRAILERS) ? listSize : MAX_AMOUNT_TRAILERS;

        View.OnClickListener trailerListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                TrailerResult r = mAllMovieDetails.getTrailers().getResults().get(id);
                playVideo(r.getKey());
            }
        };

        for (int i = 0; i < listSize; i++) {
            TrailerResult r = trailerList.get(i);
            View view = layoutInflater.inflate(R.layout.trailer_list_item, null);
            view.setId(i);
            TextView trailerName = (TextView) view.findViewById(R.id.trailer_name);
            trailerName.setText(r.getName());
            view.setOnClickListener(trailerListener);
            mTrailerList.addView(view);
        }
    }

    private void displayReviews(Reviews reviews){
        Log.d(TAG, "display reviews");
        if (getActivity() == null || reviews == null || reviews.getResults().size() == 0) {
            return;
        }

        mReviewList.setVisibility(View.VISIBLE);
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        List<ReviewResult> reviewList = reviews.getResults();
        int listSize = reviewList.size();
        if (listSize > MAX_AMOUNT_REVIEWS){
            listSize = MAX_AMOUNT_REVIEWS;
        }

        for (int i = 0; i < listSize; i++) {
            ReviewResult r = reviewList.get(i);
            View view = layoutInflater.inflate(R.layout.review_list_item, null);

            TextView content = (TextView) view.findViewById(R.id.review_content);
            String review = "<b>" + r.getAuthor() + ": </b>" + r.getContent();
            content.setText(Html.fromHtml(review));

            mReviewList.addView(view);
        }

    }

    void playVideo(String key) {
        if (key != null) {
            String videoUrl = "http://www.youtube.com/watch?v=" + key;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
            startActivity(intent);
        }
    }


}
