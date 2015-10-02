package ru.chipenable.popularmovies.view;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.chipenable.popularmovies.R;
import ru.chipenable.popularmovies.client.service.MovieClient;
import ru.chipenable.popularmovies.model.Command;

/*  At first you must add API_KEY !!!!
 *  See MovieClient.java
 *
 *  p.s.: Sorry for my English.
 *  */

public class MainActivity extends AppCompatActivity implements BaseFragment.FragmentCallback{

    public final static String PREF_TAG = "pref_tag";

    private boolean mTwoPane;
    @Bind(R.id.progress_bar) ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (MovieClient.KEY.equals("")){
            Toast.makeText(this, R.string.no_key, Toast.LENGTH_LONG).show();
            return;
        }

        if (findViewById(R.id.detail_frame) != null) {
            mTwoPane = true;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(MainFragment.TAG) == null){
            fm.beginTransaction()
                    .add(R.id.main_frame, new MainFragment(), MainFragment.TAG)
                    .commit();
        }
    }

    /*Fragment's callback function. It is used to communicate with Activity:
    * - to control the progress bar
    * - to start DetailActivity/DetailFragment*/
    @Override
    public void fragmentCallback(int command, long arg) {
        switch(command){
            case Command.START_DOWNLOADING:
                mProgressBar.setVisibility(View.VISIBLE);
                break;

            case Command.STOP_DOWNLOADING:
                mProgressBar.setVisibility(View.INVISIBLE);
                break;

            case Command.SHOW_DETAIL:
                if (!mTwoPane) {
                    DetailActivity.start(MainActivity.this, arg);
                }
                else{
                    getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.detail_frame, DetailFragment.newInstance(arg))
                        .commit();
                }
                break;

            default:
        }
    }

}
