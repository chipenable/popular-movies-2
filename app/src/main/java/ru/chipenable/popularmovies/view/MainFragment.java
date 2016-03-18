package ru.chipenable.popularmovies.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import ru.chipenable.popularmovies.client.service.MovieService;
import ru.chipenable.popularmovies.model.Command;
import ru.chipenable.popularmovies.utils.ImageAdapter;
import ru.chipenable.popularmovies.client.service.MovieClient;
import ru.chipenable.popularmovies.R;
import ru.chipenable.popularmovies.model.movielist.MovieList;
import ru.chipenable.popularmovies.model.movielist.Result;
import ru.chipenable.popularmovies.utils.RecyclerImageAdapter;
import ru.chipenable.popularmovies.utils.Utils;

/* This fragment shows GridView with movie posters
*
* */
public class MainFragment extends BaseFragment implements RecyclerImageAdapter.EndListListener, RecyclerImageAdapter.OnItemClickListener{

    public final static String TAG = "MainFragment";
    private final static String PREF_TYPE_SORT = "type_sort";

    private final static int SORT_BY_POPULARITY = 0;
    private final static int SORT_BY_RATING = 1;
    private final static int FAVORITES = 2;

    @Bind(R.id.poster_recycler_view) RecyclerView mRecyclerView;
    private Handler mHandler;
    private RecyclerImageAdapter mAdapter;
    private boolean mFailureFlag;
    private MovieList mMovieList;
    private List<Result> mMovieResults;

    /***************************** fragment methods ***************************************/

    /*Receiver's callback function. It'll be called when a state of a network is changed*/
    @Override
    public void updateData(boolean connectionState) {
        Log.d(TAG, "connection state: " + connectionState);
        if (connectionState && mFailureFlag) {
            downloadMovies();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "create main fragment");
        setHasOptionsMenu(true);

        mMovieList = new MovieList();
        mMovieResults = new ArrayList<>();
        mFailureFlag = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);

        /*It calculates a size of the ImageView for the RecyclerView*/
        Point screenSize = Utils.getScreenSize(getActivity());
        int imageWidth = screenSize.x/getResources().getInteger(R.integer.num_col);
        int imageHeight = screenSize.y/getResources().getInteger(R.integer.num_row);

        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
                getResources().getInteger(R.integer.num_col)));
        mAdapter = new RecyclerImageAdapter(getActivity(), mMovieResults, imageWidth, imageHeight);
        mAdapter.setEndListListener(this);
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);


        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Log.d(TAG, "handle message");
                int com = msg.arg1;
                Object obj = msg.obj;

                if (com == MovieService.GET_MOVIE_LIST){
                    if (obj != null){
                        mMovieList = (MovieList)obj;
                        int lastPosition = mMovieResults.size();
                        mMovieResults.addAll(mMovieList.getResults());
                        if (mAdapter != null){
                            mAdapter.notifyItemInserted(lastPosition);
                        }
                    }
                    else{
                        mFailureFlag = true;
                        Toast.makeText(getContext(), getString(R.string.connection_problem),
                                Toast.LENGTH_LONG).show();
                    }
                    mCallback.fragmentCallback(Command.STOP_DOWNLOADING, 0);
                }

                return false;
            }
        });

        if (mMovieList.getPage() == 0){
            Log.d(TAG, "initialize movie list");
            downloadMovies();
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    /*it handles user clicks and start DetailActivity/DetailFragment*/
    @Override
    public void onItemClick(View view, int position, long id) {
        mCallback.fragmentCallback(Command.SHOW_DETAIL, id);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort_by_popularity:
                saveTypeSort(SORT_BY_POPULARITY);
                DownloadFirstPage();
                return true;

            case R.id.action_sort_by_rating:
                saveTypeSort(SORT_BY_RATING);
                DownloadFirstPage();
                return true;

            case R.id.action_favorites:
                saveTypeSort(FAVORITES);
                DownloadFirstPage();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*if a user changes the type of sort a first page will
    be downloaded from the internet service*/
    private void DownloadFirstPage() {
        Log.d(TAG, "download first page");
        mMovieList.setPage(0);
        mMovieResults.clear();
        mAdapter.notifyDataSetChanged();
        downloadMovies();
    }

    /*Callback function that is involved by ImageAdapter*/
    @Override
    public void downloadNewData() {
        Log.d(TAG, "download new data");
        downloadMovies();
    }

    /***************************** user methods *******************************************/

    private void saveTypeSort(int typeSort){
        SharedPreferences sharedPref = getActivity().getSharedPreferences(MainActivity.PREF_TAG, Context.MODE_PRIVATE);
        sharedPref.edit().putInt(PREF_TYPE_SORT, typeSort).apply();
    }

    private int getTypeSort(){
        SharedPreferences sharedPref = getActivity().getSharedPreferences(MainActivity.PREF_TAG, Context.MODE_PRIVATE);
        return sharedPref.getInt(PREF_TYPE_SORT, SORT_BY_POPULARITY);
    }

    /*It downloads movie data from the internet service.
    * At first the function checks its parameters and then it sends a request by Retrofit */
    private void downloadMovies(){
        Log.d(TAG, "download movies");

        int curPage = mMovieList.getPage();
        if (curPage != 0 && curPage >= mMovieList.getTotalPages()){
            return;
        }

        int sort = getTypeSort();
        String sortPar;
        switch(sort){
            case SORT_BY_POPULARITY:
                sortPar = MovieClient.POPULARITY;
                break;

            case SORT_BY_RATING:
                sortPar = MovieClient.VOTE_AVERAGE;
                break;

            case FAVORITES:
                sortPar = MovieClient.FAVORITE;
                break;

            default:
                return;
        }

        mFailureFlag = false;
        mCallback.fragmentCallback(Command.START_DOWNLOADING, 0);
        MovieService.getMovieList(getActivity(), sortPar, curPage + 1, mHandler);
        Log.d(TAG, "page: " + Integer.toString(curPage + 1) + " sort par: " + sortPar);
    }


}
