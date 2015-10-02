package ru.chipenable.popularmovies.model.movielist;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Result {

    @SerializedName("poster_path")
    @Expose
    private String posterPath;

    @Expose
    private long id;

    private String posterLocalPath;


    /**
     *
     * @return
     * The id
     */
    public long getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(long id) {
        this.id = id;
    }


    /**
     *
     * @return
     * The posterPath
     */
    public String getPosterPath() {
        return posterPath;
    }

    /**
     *
     * @param posterPath
     * The poster_path
     */
    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getPosterLocalPath(){
        return posterLocalPath;
    }

    public void setPosterLocalPath(String posterLocalPath){
        this.posterLocalPath = posterLocalPath;
    }

}