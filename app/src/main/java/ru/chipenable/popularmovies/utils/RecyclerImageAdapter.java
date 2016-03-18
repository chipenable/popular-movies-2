package ru.chipenable.popularmovies.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.bumptech.glide.Glide;

import java.util.List;

import ru.chipenable.popularmovies.R;
import ru.chipenable.popularmovies.client.service.MovieClient;
import ru.chipenable.popularmovies.model.movielist.Result;

/**
 * ImageAdapter is used to show movie posters in GridView
 */
public class RecyclerImageAdapter extends RecyclerView.Adapter<RecyclerImageAdapter.ItemHolder>{

    private static final String TAG = "RecyclerImageAdapter";

    private Context mContext;
    private List<Result> mMovieList;
    private int mWidth;
    private int mHeight;
    private EndListListener mEndListener;
    private OnItemClickListener mOnItemClickListener;

    public interface EndListListener{
        void downloadNewData();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, long id);
    }

    /**
     * width - width of an ImageView
     * height - height of an ImageView
     * */
    public RecyclerImageAdapter(Context c, List<Result> list, int width, int height) {
        mContext = c;
        mMovieList = list;
        mWidth = width;
        mHeight = height;
     }

    public void setEndListListener(EndListListener l){
        mEndListener = l;
    }

    public void setOnItemClickListener(OnItemClickListener l){
        mOnItemClickListener = l;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.poster_item, parent, false);
        return new ItemHolder(view, mWidth, mHeight);
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        Result movie = mMovieList.get(position);
        holder.mPosition = position;
        holder.mId = movie.getId();
        String posterPath = movie.getPosterLocalPath();

        if (posterPath == null) {
            posterPath = MovieClient.BASE_IMG_PATH + movie.getPosterPath();
        }

        //Shows poster by using Glide library
        Glide.with(mContext)
                .load(posterPath)
                .error(R.drawable.noposter)
                .into(holder.mImageView);

        if ((position + 1) == getItemCount()){
            if (mEndListener != null){
                mEndListener.downloadNewData();
            }
        }
    }

    @Override
    public int getItemCount() {
        return mMovieList.size();
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public int mPosition;
        public long mId;
        public ImageView mImageView;

        public ItemHolder(View itemView, int width, int height) {
            super(itemView);
            mImageView = (ImageView)itemView.findViewById(R.id.poster_image_view);
            mImageView.setLayoutParams(new LinearLayout.LayoutParams(width, height));
            mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null){
                Log.d(TAG, "position: " + Integer.toString(mPosition));
                mOnItemClickListener.onItemClick(v, mPosition, mId);
            }
        }
    }



}
