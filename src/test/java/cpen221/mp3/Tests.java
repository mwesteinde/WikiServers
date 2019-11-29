package cpen221.mp3;

import cpen221.mp3.wikimediator.Query;
import cpen221.mp3.wikimediator.WikiMediator;
import fastily.jwiki.core.Wiki;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class Tests {

    /*
        You can add your tests here.
        Remember to import the packages that you need, such
        as cpen221.mp3.cache.
     */

    @Test
    public void trendingTest() {
        Query query1 = new Query("hello");
    }

    @Test public void simpleSearchTest() {
        WikiMediator wikiM = new WikiMediator();
        Wiki wiki = new Wiki("en.wikipedia.org");
        String query = "UBC";
        int limit = 3;
        List<String> searchResults = wikiM.simpleSearch("UBC", 3);
        List<String> wikiSearchResults = wiki.search("UBC", 3);
        Assert.assertEquals(searchResults, wikiSearchResults);
    }

    @Test
    public void getConnectedTest() {
        WikiMediator wikiM = new WikiMediator();
        Wiki wiki = new Wiki("en.wikipedia.org");

        Assert.assertTrue(wikiM.getConnectedPages("UBC", 0).get(0).equals("UBC"));

    }

}
