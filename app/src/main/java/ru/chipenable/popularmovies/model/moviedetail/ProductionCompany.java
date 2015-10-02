package ru.chipenable.popularmovies.model.moviedetail;

import com.google.gson.annotations.Expose;

/**
 * Created by Pashgan on 11.07.2015.
 */
public class ProductionCompany {

    @Expose
    private String name;
    @Expose
    private long id;

    /**
     *
     * @return
     * The name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    public void setName(String name) {
        this.name = name;
    }

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

}
