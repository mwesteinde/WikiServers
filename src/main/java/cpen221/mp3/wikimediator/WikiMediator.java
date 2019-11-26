package cpen221.mp3.wikimediator;

import cpen221.mp3.cache.Cache;
import cpen221.mp3.cache.Cacheable;
import fastily.jwiki.core.Wiki;

import java.util.*;


//TODO: should T extends cacheable be used?
public class WikiMediator<T extends Cacheable> {

    /* a cache to store search results */
    private Cache wikiCache = new Cache();
    /* access to wikipedia */
    private Wiki wiki = new Wiki("en.wikipedia.org");
    /* a map of all queries from simpleSearch and getPage and how many times they have occured */
    private HashMap<String, Integer> queries = new HashMap<>();


    //TODO: write RI and AF
    /*
    Representation invariant:
    Abstraction function:
     */

    /* TODO: Implement this datatype
        You must implement the methods with the exact signatures
        as provided in the statement for this mini-project.

        You must add method signatures even for the methods that you
        do not plan to implement. You should provide skeleton implementation
        for those methods, and the skeleton implementation could return
        values like null.
     */

    /**
     * Given a `query`, return up to
     * `limit` page titles that match the query string (per Wikipedia's search service).
     *
     * @param query The string to search Wikipedia titles with.
     * @param limit The limit to the number of results retrieved.
     * @return A list of length limit of pages with the query in the title.
     */
    public List<String> simpleSearch(String query, int limit) {
        queried(query);

        ArrayList<String> results = wiki.search(query, limit);
        return results.subList(0, limit);
    }

    /**
     * Given a `pageTitle`, return the text associated
     * with the Wikipedia page that matches `pageTitle`.
     *
     * @param pageTitle The String title of a Wikipedia page.
     * @return The text associated with pageTitle as a String if the page exists, an empty String otherwise.
     */
    public String getPage(String pageTitle) {
        queried(pageTitle);

        return wiki.getPageText(pageTitle);
    }

    /**
     * Return a list of page
     * titles that can be reached by following up to `hops` links starting with the page
     * specified by `pageTitle`.
     *
     * @param pageTitle The title of the page to start hopping from.
     * @param hops      The number of 'hops' to take from pageTitle.
     * @return A a list of page titles within 'hops' of the page pageTitle.
     */
    public List<String> getConnectedPages(String pageTitle, int hops) {

        List<String> hoppable;

        if (hops == 1) {
            return wiki.getLinksOnPage(true, pageTitle);
        } else {
            hoppable = new ArrayList<>(wiki.getLinksOnPage(true, pageTitle));
            List<String> extraHops = new ArrayList<>();
            for (String s : hoppable) {
                extraHops.addAll(getConnectedPages(s, hops - 1));
            }
            hoppable.addAll(extraHops);
        }

        return hoppable;
    }

    /**
     * Return the most common `String`s used in `simpleSearch`
     * and `getPage` requests, with items being sorted in non-increasing count order. When many requests
     * have been made, return only `limit` items.
     *
     * @param limit A limit for how many pages can be returned.
     * @return The most common 'String's searched for with simpleSearch and getPage.
     */
    public List<String> zeitgeist(int limit) {

        if (this.queries.size() > limit) {
            return new ArrayList<String>(this.queries.keySet());
        }

        TreeMap<Integer, String> sortedQueries = new TreeMap<>();
        for(String s: this.queries.keySet()) {
            sortedQueries.put(this.queries.get(s), s);
        }

        // trying to sort by value rather than key


        return null;
    }

    //TODO: figure this out...
    /**
     * Similar to `zeitgeist()`, but returns the most frequent
     * requests made in the last 30 seconds.
     *
     * @param limit
     * @return
     */
    public List<String> trending(int limit) {

        return null;
    }

    /**
     * What is the maximum number of requests seen in any 30-second window? The
     * request count is to include all requests made using the public API of `WikiMediator`, and
     * therefore counts all 6 methods listed as **basic page requests**.
     *
     * @return
     */
    public int peakLoad30s() {

        return -1;
    }


    /**
     * Creates a cache for this 'WikiMediator' if it does not already exist.
     * @returns True if there is an existing cache, false otherwise.
     */
    private boolean cache(String s) {
//        this.wikiCache.put(s);
        return false;
    }

    /**
     * Updates the list of queries.
     *
     * @param query The string queried.
     */
    private void queried(String query) {
        if (queries.containsKey(query)) {
            queries.replace(query, queries.get(query), queries.get(query)+1);
        } else {
            queries.put(query, 1);
        }
    }

}
