package cpen221.mp3.wikimediator;

import cpen221.mp3.cache.Cache;
import fastily.jwiki.core.Wiki;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class WikiMediator {

    // TODO: write RI and AF
    // AF: explains the variables in their abstract context assuming RI is met
    // RI: covers entire domain that variables could be in and makes sure they can only take values that make sense

    /* a cache to store search results */
    private Cache wikiCache = new Cache();

    /* access to wikipedia */
    private Wiki wiki = new Wiki("en.wikipedia.org");

    /* variables to keep track of queries */
    private TreeSet<Query> qTree = new TreeSet<>();
    private HashMap<String, Query> qMap = new HashMap<>();

    /* Tracks calls to methods in 30 second intervals */
    private Cache<MethodCall> callCache = new Cache(300, 30);

    /* TODO: Implement this datatype
        You must implement the methods with the exact signatures
        as provided in the statement for this mini-project.

        You must add method signatures even for the methods that you
        do not plan to implement. You should provide skeleton implementation
        for those methods, and the skeleton implementation could return
        values like null.
     */

    /**
     * Creates a new WikiMediator object.
     */
    public WikiMediator() {
    }

    /* //////////////////////////////////////////////////////////////////////////////// */
    /* /////////////////////////// BASIC REQUESTS ///////////////////////////////////// */
    /* //////////////////////////////////////////////////////////////////////////////// */

    /**
     * Given a `query`, return up to
     * `limit` page titles that match the query string (per Wikipedia's search service).
     *
     * @param query The string to search Wikipedia titles with.
     * @param limit The limit to the number of results retrieved, > 0;
     * @return A list of length limit of pages with the query in the title.
     */
    public List<String> simpleSearch(String query, int limit) {
        call();
        queried(query);
        //TODO: use cache

        return wiki.search(query, limit);
    }

    /**
     * Given a `pageTitle`, return the text associated
     * with the Wikipedia page that matches `pageTitle`.
     *
     * @param pageTitle The String title of a Wikipedia page.
     * @return The text associated with pageTitle as a String if the page exists, an empty String otherwise.
     */
    public String getPage(String pageTitle) {
        call();
        queried(pageTitle);

        //TODO: use cache

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
        call();

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
        call();

        //TODO: use cache
        List<String> mostCommonQueries = new ArrayList<>();

        for (Query q : this.qTree.descendingSet()) {
            if (mostCommonQueries.size() < limit) {
                mostCommonQueries.add(q.getQuery());
            }
            if (mostCommonQueries.size() == limit) {
                return mostCommonQueries;
            }
        }
        return mostCommonQueries;
    }

    /**
     * Similar to `zeitgeist()`, but returns the most frequent
     * requests made in the last 30 seconds.
     *
     * @param limit A limit for how many pages can be returned.
     * @return The most commonly queried Strings within the last 30 seconds.
     */
    public List<String> trending(int limit) {
        call();

        //TODO: use cache
        // could work?
        TreeSet<Query> qTree = new TreeSet<>(this.qMap.values());
        Instant now = Instant.now();

        // filters queries within last 30 seconds and gets their string values
        List<String> sortedQueries = qTree.descendingSet().stream()
                .filter(q -> q.within30S(now))
                .map(Query::getQuery)
                .collect(Collectors.toList());

        // returns <= the limit of queries allowed
        if (sortedQueries.size() <= limit) {
            return sortedQueries;
        } else {
            return sortedQueries.subList(0, limit - 1);
        }
    }

    /**
     * What is the maximum number of requests seen in any 30-second window? The
     * request count is to include all requests made using the public API of `WikiMediator`, and
     * therefore counts all 6 methods listed as **basic page requests**.
     *
     * @return The maximum number of method calls within 30 seconds.
     */
    public int peakLoad30s() {
        call();
        return callCache.getMaxCached();
    }

    /* //////////////////////////////////////////////////////////////////////////////// */
    /* /////////////////////////// HELPER METHODS ///////////////////////////////////// */
    /* //////////////////////////////////////////////////////////////////////////////// */

    /**
     * Updates the list of queries with a query.
     *
     * @param query The string queried.
     */
    private void queried(String query) {
        if (qMap.containsKey(query)) {
            Query qToUpdate = qMap.get(query);
            qToUpdate.update(query);
        } else {
            qMap.put(query, new Query(query));
        }
    }

    /**
     * @modifies callCache by adding a method call.
     */
    private void call() {
        try {
            callCache.put(new MethodCall());
        } catch (Exception e) {
            System.out.println("Exception thrown when using callCache.put()");
        }
    }
}
