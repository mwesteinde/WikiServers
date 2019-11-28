package cpen221.mp3.wikimediator;

import cpen221.mp3.cache.Cache;
import cpen221.mp3.cache.Cacheable;
import fastily.jwiki.core.Wiki;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;


//TODO: should T extends cacheable be used?d
public class WikiMediator<T extends Cacheable> {
    // TODO: write RI and AF
    // AF: explains the variables in their abstract context assuming RI is met
    // RI: covers entire domain that variables could be in and makes sure they can only take values that make sense

    /* a cache to store search results */
    private Cache wikiCache = new Cache();

    /* access to wikipedia */
    private Wiki wiki = new Wiki("en.wikipedia.org");

    /* a set of all queries from simpleSearch and getPage and how many times they have occurred */
    private TreeSet<Query> qTree = new TreeSet<>();

    /* keeps track of the Strings queried */
    private ArrayList<Query> qList = new ArrayList<>();

    private HashMap<String, Query> qMap = new HashMap<>();

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
        //TODO: use cache

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
        //TODO: use cache
        List<String> mostCommonQueries = new ArrayList<>();

        for(Query q: this.qTree.descendingSet()) {
            if (mostCommonQueries.size() < limit) {
                mostCommonQueries.add(q.getQuery());
            } if (mostCommonQueries.size() == limit) {
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
        //TODO: use cache
        // could work?
        TreeSet<Query> qTree = new TreeSet<>(this.qMap.values());
        Instant now = Instant.now();
        List<String> sortedQueries = qTree.descendingSet().stream()
                .filter(q -> q.within30S(now))
                .map(Query::getQuery)
                .collect(Collectors.toList());
        if (sortedQueries.size() <= limit) {
            return sortedQueries;
        } else {
            return sortedQueries.subList(0, limit-1);
        }
//
//        List<String> latestQueries = new ArrayList<String>();
//
//
//        for (Query q : this.queries.descendingSet()) {
//            // adds query if the query was created within the last 30 seconds
//            if (q.timestamp.isAfter(Instant.now().minus(30, ChronoUnit.SECONDS))) {
//                latestQueries.add(q.query);
//            }
//            // returns the latest queries if the limit is reached
//            if (latestQueries.size() == limit) {
//                return latestQueries;
//            }
//        }
//
//        return latestQueries;
//        return null;
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
}
