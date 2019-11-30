package cpen221.mp3.wikimediator;

import cpen221.mp3.cache.Cache;
import fastily.jwiki.core.Wiki;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * A public API which acts as a mediator for Wikipedia that allows interaction with the
 * site's content and gathers information usage of this mediator.
 */

public class WikiMediator<InvalidQueryException extends Throwable> {

    // TODO: write RI and AF
    // AF: explains the variables in their abstract context assuming RI is met
    // RI: covers entire domain that variables could be in and makes sure they can only take values that make sense

    // AF: An API that returns and stores Wikipedia page information
    // in response to queries.
    // wikiCache stores search results
    // wiki creates a new access to Wikipedia
    // qTree stores past queries from simpleSearch and getPage in order of popularity
    // qMap represents all past queries

    // RI:
    // qTree.size() == qMap.size() >= 0
    // wikiCache size >= 0
    // Wiki has domain en.wikipedia.org
    //

    /* a cache to store search results */
    private Cache wikiCache = new Cache();

    /* access to wikipedia */
    private Wiki wiki = new Wiki("en.wikipedia.org");

    /* variables to keep track of queries */
    private TreeSet<Query> qTree = new TreeSet<>();
    private HashMap<String, Query> qMap = new HashMap<>();

    /* Tracks calls to methods in 30 second intervals */
    private Cache callCache = new Cache(300, 30);

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
     * Given a query, returns up to a specified number of page titles that match the query
     * string (per Wikipedia's search service).
     *
     * @param query The string to search Wikipedia titles with.
     * @param limit The limit to the number of results retrieved, > 0;
     * @return A list of length limit of pages with the query in the title, an empty list
     * if an empty string is searched.
     */
    public List<String> simpleSearch(String query, int limit) {
        call("search");
        try {
            if (query.equals("")) {
                throw new NullPointerException();
            }

            queried(query);
            //TODO: use cache

            return wiki.search(query, limit);

        } catch (NullPointerException e) {
            System.out.println("Empty string searched");
        }

        return new ArrayList<>();
    }

    /**
     * Given a Wikipedia page, gets the text associated
     * with the Wikipedia page that matches the query.
     *
     * @param pageTitle The title of a Wikipedia page.
     * @return The text associated with pageTitle as a String if the page exists,
     * returns an empty string otherwise.
     */
    public String getPage(String pageTitle) {
        //TODO: use cache
        call("search");
        queried(pageTitle);

        try {
            if (!wiki.exists(pageTitle)) {
                throw new Exception();
            }
            return wiki.getPageText(pageTitle);
        } catch (Exception e) {
            System.out.println("Page does not exist.");
            return "";
        }
    }

    /**
     * Gets a list of all Wikipedia page titles that can be reached by following a
     * specified number of links starting with a specific page.
     *
     * @param pageTitle The title of the page to start hopping from.
     * @param hops      The number of 'hops' to take from pageTitle, must be >= 0.
     * @return A a list of page titles within 'hops' of the page pageTitle,
     * an empty list if the page does not exist.
     */
    public List<String> getConnectedPages(String pageTitle, int hops) {
        call("basicRequest");

        if (!wiki.exists(pageTitle)) {
            return new ArrayList<>();
        }

        List<String> hoppable = new ArrayList<>();
        hoppable.add(pageTitle);

        // Simply return the query
        if (hops == 0) {
            return hoppable;
        }
        // returns only the links connected pages to query if only one hop
        if (hops == 1) {
            hoppable.addAll(wiki.getLinksOnPage(true, pageTitle));
            return hoppable;
        }
        // iterate through all pages and their connections if hops > 1
        else {
            hoppable.addAll(wiki.getLinksOnPage(true, pageTitle));
            List<String> extraHops = new ArrayList<>();
            for (String s : hoppable) {
                extraHops.addAll(getConnectedPages(s, hops - 1));
            }
            hoppable.addAll(extraHops);
        }

        return hoppable;
    }

    /**
     * Gets the most common queries used for 'simpleSearch'
     * and `getPage` method calls on this WikiMediator, with items being sorted in
     * non-increasing count order.
     * When many requests have been made, returns only the specified number of titles.
     *
     * @param limit A limit for how many pages can be returned, cannot be negative.
     * @return The most common queries used with simpleSearch and getPage methods on this
     * WikiMediator.
     */
    public List<String> zeitgeist(int limit) {
        call("basicRequest");
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
     * Returns the most common queries used for 'simpleSearch'
     * and `getPage` requests with this WikiMediator within the last 30 seconds, with
     * items being sorted in non-increasing query count order.
     * When many requests have been made, returns only the specified number of titles.
     *
     * @param limit A limit for how many pages can be returned, cannot be negative.
     * @return The 'limit' most common queries using `simpleSearch`
     * and `getPage` within the last 30 seconds.
     */
    public List<String> trending(int limit) {
        call("basicRequest");
        //TODO: use cache

        if (limit == 0) {
            return new ArrayList<>();
        }

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
     * Gets the maximum load this WikiMediator has handled within a 30 second timeframe since
     * its creation.
     *
     * @return The maximum number of method calls within 30 seconds over the lifespan of
     * this WikiMediator.
     */
    public int peakLoad30s() {
        call("basicRequest");

        return callCache.getMaxCached();
    }

    /* //////////////////////////////////////////////////////////////////////////////// */
    /* /////////////////////////// HELPER METHODS ///////////////////////////////////// */
    /* //////////////////////////////////////////////////////////////////////////////// */

    /**
     * Updates the list of queries with a new query that has been input into a method
     * of this instance of WikiMedia.
     *
     * @param query The query input to a method in this WikiMediator.
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
     * Modifies callCache by adding a method called on this instance of WikiMediator.
     *
     * @param callType The type of method that has been called.
     */
    private void call(String callType) {
        try {
            callCache.put(new MethodCall(callType));
        } catch (Exception e) {
            System.out.println("Exception thrown when using callCache.put()");
        }
    }

    // 3A
    /**
     * Gets a path of links from one specified Wikipedia page to another. If the page does
     * not exist or the pages cannot be linked an empty list is returned.
     *
     * @param startPage A potential Wikipedia page name.
     * @param stopPage A potential Wikipedia page name.
     * @return A list of pages that are on the link path between startPage and stopPage,
     * returns an empty list if one of the pages does not exist or the pages cannot be linked.
     */
    public List<String> getPath(String startPage, String stopPage) {

        return null;
    }

    // 3B
    /**
     * Gets a list results from a specified query.
     *
     * @param query A specified query.
     * @return A list of results that correspond with the specified query. An empty list
     * if no meaningful results are found. Leads to a failed operation when used over
     * the network.
     * @throws InvalidQueryException if the input query cannot be parsed.
     */
    public List<String> executeQuery(String query) throws InvalidQueryException {
        return null;
    }
}
