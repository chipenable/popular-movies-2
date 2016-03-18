package ru.chipenable.popularmovies.utils;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;


import com.bumptech.glide.Glide;

import java.util.List;

import ru.chipenable.popularmovies.R;
import ru.chipenable.popularmovies.client.service.MovieClient;
import ru.chipenable.popularmovies.model.movielist.Result;

/**
 * ImageAdapter is used to show movie posters in GridView
 */
public class ImageAdapter extends BaseAdapter {

    private Context mContext;
    private List<Result> mMovieList;
    private int mWidth;
    private int mHeight;
    private EndListListener mEndListener;


    public interface EndListListener{
        void downloadNewData();
    }

    /**
     * width - width of an ImageView
     * height - height of an ImageView
     * */
    public ImageAdapter(Context c, List<Result> list, int width, int height) {
        mContext = c;
        mMovieList = list;
        mWidth = width;
        mHeight = height;
     }

    public void setEndListListener(EndListListener l){
        mEndListener = l;
    }

    public int getCount() {
        return mMovieList.size();
    }

    public Object getItem(int position) {
        return mMovieList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        /*get ImageView reference*/
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(mWidth, mHeight));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        } else {
            imageView = (ImageView) convertView;
        }




        Result movie = mMovieList.get(position);
        String posterLocalPath = movie.getPosterLocalPath();
        if (posterLocalPath == null) {

            //Show poster by using Picasso library
            String url = MovieClient.BASE_IMG_PATH + movie.getPosterPath();
            Glide.with(mContext)
                    .load(url)
                    .error(R.drawable.noposter)
                    .into(imageView);
        }
        else{
            imageView.setImageURI(Uri.parse(posterLocalPath));
        }

        /*maybe it isn't the best solution to add data to GridView*/
        if ((position + 1) == getCount()){
            if (mEndListener != null){
                mEndListener.downloadNewData();
            }
        }

        return imageView;
    }

}
