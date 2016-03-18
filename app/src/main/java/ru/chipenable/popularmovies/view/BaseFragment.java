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


}
