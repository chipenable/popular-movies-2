package ru.chipenable.popularmovies.view;

import android.content.res.TypedArray;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.Display;

import ru.chipenable.popularmovies.R;
import ru.chipenable.popularmovies.connection.ConnectionStateReceiver;

/**
 * BaseFragment. It implements several common functions for MainFragment and DetailFragment
 */
public abstract class BaseFragment  extends Fragment implements ConnectionStateReceiver.ConnectionListener{

    protected static final String TAG = "BaseFragment";

    protected FragmentCallback mCallback;
    protected ConnectionStateReceiver mReceiver;

    /*interface to communicate with Activity*/
    public interface FragmentCallback {
        void fragmentCallback(int command, long arg);
    }

    private static FragmentCallback mDummyCallback = new FragmentCallback() {
        @Override
        public void fragmentCallback(int command, long arg) {}
    };

    public abstract void updateData(boolean connectionState);

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");

        /*attach callback function*/
        try {
            mCallback = (FragmentCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement FragmentCallback");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onStart() {
        super.onStart();

        /*register receiver to control the network state*/
        mReceiver = new ConnectionStateReceiver(BaseFragment.this);
        getActivity().registerReceiver(mReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        Log.d(TAG, "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();

        /*unregister receiver*/
        if (mReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        Log.d(TAG, "onStop");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = mDummyCallback;
        Log.d(TAG, "onDetach");
    }

    /*This function is used to calculate image sizes*/
    public Point getScreenSize(){

        /*get full size of the display*/
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point screenSize = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(screenSize);
        } else {
            display.getSize(screenSize);
        }

        int displayDiv = getResources().getInteger(R.integer.display_div);
        screenSize.x /= displayDiv;

        /*get height of StatusBar*/
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarHeight = (resourceId > 0)? getResources().getDimensionPixelSize(resourceId) : 0;
        Log.d(TAG, "height of status bar: " + Integer.toString(statusBarHeight));

        //get a height of ActionBar
        TypedArray styledAttributes = getContext().getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize});
        int actionBarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        Log.d(TAG, "height of action bar: " + Integer.toString(actionBarHeight));

        //calculate available screen size
        screenSize.y -= (statusBarHeight + actionBarHeight);
        Log.d(TAG, "display size: " + Integer.toString(screenSize.x) + "x" + Integer.toString(screenSize.y));
        return screenSize;
    }
}
