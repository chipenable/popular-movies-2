package ru.chipenable.popularmovies.view;


import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.chipenable.popularmovies.R;
import ru.chipenable.popularmovies.model.Command;


public class DetailActivity extends AppCompatActivity implements BaseFragment.FragmentCallback{

    private static final String TAG = "DetailActivity";
    private static final String MOVIE_ID = "movie_id";

    @Bind(R.id.progress_bar) ProgressBar mProgressBar;

    /*********************************************************/

    public static void start(Context context, long movieId){
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(MOVIE_ID, movieId);
        context.startActivity(intent);
    }

    private long getMovieId(){
        return getIntent().getLongExtra(MOVIE_ID, 0);
    }

    /**********************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(DetailFragment.TAG) == null) {
            Log.d(TAG, "create detail fragment");
            fm.beginTransaction()
                    .add(R.id.main_frame, DetailFragment.newInstance(getMovieId()), DetailFragment.TAG)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void fragmentCallback(int command, long arg) {
        switch(command) {
            case Command.START_DOWNLOADING:
                mProgressBar.setVisibility(View.VISIBLE);
                break;

            case Command.STOP_DOWNLOADING:
                mProgressBar.setVisibility(View.GONE);
                break;

            default:
        }
    }
}
