package ru.chipenable.popularmovies.model.movielist;

/**
 * Created by Pashgan on 07.07.2015.
 */


import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MovieList {

    @Expose
    private int page;

    @Expose
    private List<Result> results;

    @SerializedName("total_pages")
    @Expose
    private int totalPages;

    @SerializedName("total_results")
    @Expose
    private int totalResults;


    public MovieList(){
        page = 0;
        totalPages = 0;
        results = new ArrayList<>();
    }



    /**
     * @return The page
     */
    public int getPage() {
        return page;
    }

    /**
     * @param page The page
     */
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * @return The results
     */
    public List<Result> getResults() {
        return results;
    }

    /**
     * @param results The results
     */
    public void setResults(List<Result> results) {
        this.results = results;
    }

    /**
     * @return The totalPages
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * @param totalPages The total_pages
     */
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    /**
     * @return The totalResults
     */
    public int getTotalResults() {
        return totalResults;
    }

    /**
     * @param totalResults The total_results
     */
    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public void addResult(Result result){
        results.add(result);
    }

    public void addResults(List<Result> results){
        results.addAll(results);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder()
                .append("page:" + Integer.toString(page) + "\n")
                .append("total pages:" + Integer.toString(totalPages) + "\n")
                .append("total results:" + Integer.toString(totalResults) + "\n");

        return sb.toString();
    }
}