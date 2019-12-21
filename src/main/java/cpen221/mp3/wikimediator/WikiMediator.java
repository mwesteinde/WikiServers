package cpen221.mp3.wikimediator;

import com.google.gson.JsonObject;
import cpen221.mp3.cache.Cache;
import cpen221.mp3.cache.NotPresentException;
import cpen221.mp3.cache.StringCacheable;
import fastily.jwiki.core.Wiki;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

//TODO Mykal: Implement cache in wikimediator and write tests by December 17th, implement task 3, do lab 9 for jokes, Task 3 95% coverage
//TODO Bridget: Implement task 2, more tests on wikimediator to 95% coverage. Task 2 95% coverage tests

/**
 * A public API which acts as a mediator for Wikipedia that allows interaction with the
 * site's content and gathers usage information about this mediator.
 */

public class WikiMediator<InvalidQueryException extends Throwable> {

    // AF: explains the variables in their abstract context assuming RI is met
    // RI: covers entire domain that variables could be in and makes sure they can only take values that make sense

    // AF: An API that returns and stores Wikipedia page information
    // in response to queries.
    // wikiCache stores search results
    // wiki creates a new access to Wikipedia
    // qTree stores past queries from simpleSearch and getPage in order of popularity
    // qMap represents all past queries
    // callCache stores all calls to this WikiMediator

    // RI:
    // qTree.size() == qMap.size() >= 0
    // wikiCache size >= 0
    // Wiki has domain en.wikipedia.org
    // callCache size >= 0

    /* a cache to store search results */
    private Cache wikiCache = new Cache();

    /* access to wikipedia */
    private Wiki wiki = new Wiki("en.wikipedia.org");

    /* variables to keep track of queries */
    private TreeSet<Query> qTree = new TreeSet<>();
    private HashMap<String, Query> qMap = new HashMap<>();

    /* Tracks calls to methods in 30 second intervals */
    private Cache callCache = new Cache(Integer.MAX_VALUE, 30);

    private Cache thisCache = new Cache(256, 12*60*60);


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
     * @param query The string to search Wikipedia titles with, cannot be empty.
     * @param limit The limit to the number of results retrieved, > 0;
     * @return A list of length limit of pages with the query in the title, an empty list
     * if an empty string is searched.
     */
    public List<String> simpleSearch(String query, int limit) {
        call("search");
        writeToLocal("simpleSearch", query);
        try {
            if (query.equals("")) {
                throw new NullPointerException();
            }

            queried(query);
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
     * @param pageTitle The title of a Wikipedia page, cannot be empty.
     * @return The text associated with pageTitle as a String if the page exists,
     * returns an empty string otherwise.
     */
    public String getPage(String pageTitle) {
        writeToLocal("getPage", pageTitle);
        try {
            call("search");
            queried(pageTitle);
            StringCacheable o = (StringCacheable) thisCache.get(pageTitle);
            return o.text();
        } catch (NotPresentException e) {

            try {
                if (!wiki.exists(pageTitle)) {
                    throw new Exception();
                }
                String str = wiki.getPageText(pageTitle);
                StringCacheable cachedString = new StringCacheable(pageTitle, str);
                thisCache.put(cachedString);
                return str;
            } catch (Exception e1) {
                System.out.println("Page does not exist.");
                return "";
            }
        }
    }

    /**
     * Gets a list of all Wikipedia page titles that can be reached by following a
     * specified number of links starting with a specific page.
     *
     * @param pageTitle The title of the page to start hopping from, cannot be empty.
     * @param hops      The number of 'hops' to take from pageTitle, must be >= 0.
     * @return A a list of page titles within 'hops' of the page pageTitle,
     * an empty list if the page does not exist. If hops is 0, return list with pageTitle.
     */
    public List<String> getConnectedPages(String pageTitle, int hops) {
        call("basicRequest");
        writeToLocal("getConnectedPages", pageTitle);

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
        writeToLocal("zeitgeist", "null");

        TreeSet<Query> qTree = new TreeSet<>(this.qMap.values());

        // filters queries within last 30 seconds and gets their string values
        List<String> commonQueries = qTree.descendingSet().stream()
                .sorted(Comparator.comparingInt(Query::getNumQueries).reversed())
                .map(Query::getQuery)
                .collect(Collectors.toList());

        if(commonQueries.size() < limit) {
            return commonQueries;
        } else {
            return commonQueries.subList(0, limit);
        }
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
        writeToLocal("trending", "null");
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
            return sortedQueries.subList(0, limit);
        }
    }

    /**
     * Gets the maximum load this WikiMediator has handled within a 30 second time frame since
     * its creation.
     *
     * @return The maximum number of method calls within 30 seconds over the lifespan of
     * this WikiMediator. peakload30s counts as a method call.
     */
    public int peakLoad30s() {
        call("basicRequest");
        writeToLocal("peakLoad30s","null");
        return callCache.getMaxCached();
    }

    /* //////////////////////////////////////////////////////////////////////////////// */
    /* /////////////////////////// HELPER METHODS ///////////////////////////////////// */
    /* //////////////////////////////////////////////////////////////////////////////// */

    /**
     * Updates the list of queries with a new query that has been input into a method
     * of this instance of WikiMedia.
     *
     * @param query The query input to a method in this WikiMediator, cannot be empty.
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
     * @param callType The type of method that has been called, cannot be empty.
     */
    private void call(String callType) {

            callCache.put(new MethodCall(callType));

            this.qTree = new TreeSet<>(this.qMap.values());
    }

    // 3A
    /**
     * Gets a path of links from one specified Wikipedia page to another. If the page does
     * not exist or the pages cannot be linked an empty list is returned.
     *
     * @param startPage A potential Wikipedia page name, cannot be empty.
     * @param stopPage A potential Wikipedia page name, cannot be empty.
     * @return A list of pages that are on the link path between startPage and stopPage,
     * returns an empty list if one of the pages does not exist or the pages cannot be linked.
     */
    public List<String> getPath(String startPage, String stopPage) {
        List <String> path = new ArrayList<>();
        call("getPath");
        if (startPage.equals(stopPage)) {
            path.add(startPage);
            return path;
        }
        path.add(startPage);
        String next = startPage;
        while (true) {
            next = findBestLink(wiki.getLinksOnPage(next), path, stopPage);
            if (next.equals(stopPage)) {
                path.add(next);
                break;
            }
        }

        return path;
    }


    private String findBestLink(List<String> links, List<String> path, String endPage) {
        String best;
        Map <String, Integer> map = new HashMap<>();


        for (String i: links) {
            if (i.equals(endPage)) {
                return i;
            }
            int val = getValue(i, endPage);
            map.put(i, val);
        }

        best = Collections.max(map.entrySet(), (entry1, entry2) -> entry1.getValue() - entry2.getValue()).getKey();

        return best;
    }

    private int getValue(String link, String endPage) {
        int value = 0;

        String text = wiki.getPageText(endPage);


        return value;
    }

   private synchronized void writeToLocal(String method, String pageTitle) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("local/CacheStorage", true));

            JsonObject json = new JsonObject();
            Long timestamp = System.currentTimeMillis();

            json.addProperty("query", pageTitle);
            json.addProperty("method", method);
            json.addProperty("timestamp", timestamp);
            writer.write(json.toString());
            writer.flush();
        } catch (IOException e) {
            System.out.print("Exception thrown creating filewriter");
        }
    }



    /**
     * Checks the representation invariant.
     *
     * @return True if the representation invariant is held, false otherwise.
     */
    public boolean checkRep() {
        return qTree.size() == qMap.size();
    }
}
