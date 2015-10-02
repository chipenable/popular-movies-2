package ru.chipenable.popularmovies.model.trailers;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;

public class Trailers {

    @Expose
    private long id;
    @Expose
    private List<TrailerResult> results = new ArrayList<>();

    /**
     * @return The id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return The results
     */
    public List<TrailerResult> getResults() {
        return results;
    }

    /**
     * @param results The results
     */
    public void setResults(List<TrailerResult> results) {
        this.results = results;
    }

}
