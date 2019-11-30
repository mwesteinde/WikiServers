package cpen221.mp3;

import cpen221.mp3.wikimediator.Query;
import cpen221.mp3.wikimediator.WikiMediator;
import fastily.jwiki.core.Wiki;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tests {

    /*
        You can add your tests here.
        Remember to import the packages that you need, such
        as cpen221.mp3.cache.
     */

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

        Assert.assertEquals("UBC", wikiM.getConnectedPages("UBC", 0).get(0));

        List<String> wikiMConnections = wikiM.getConnectedPages("UBC", 1);
        List<String> wikiConnections = wiki.getExternalLinks("UBC");
        wikiConnections.add("UBC");

        Assert.assertTrue(wikiMConnections.containsAll(wikiConnections));
    }

    @Test
    public void getPageTest() {
        WikiMediator wikiM = new WikiMediator();
        Wiki wiki = new Wiki("en.wikipedia.org");

        String sM = wikiM.getPage("UBC");
        String s = wiki.getPageText("UBC");

        Assert.assertEquals(sM, s);
    }

    @Test
    public void zeitgeistTest() {
        WikiMediator wikiM = new WikiMediator();

        for(int i = 0; i < 3; i++) {
            wikiM.getPage("hello");
        }
        for (int i = 0; i < 1; i++) {
            wikiM.getPage("UBC");
        }
        for (int i = 0 ; i < 4; i++) {
            wikiM.simpleSearch("zeitgeist", 3);
        }

        Assert.assertTrue(wikiM.zeitgeist(1).equals(new ArrayList<>(Collections.singleton("zeitgeist"))));

    }

    @Test
    public void trendingTest() throws InterruptedException {
        WikiMediator wikiM = new WikiMediator();

        for(int i = 0; i < 3; i++) {
            wikiM.getPage("hello");
        }
        for (int i = 0; i < 1; i++) {
            wikiM.getPage("UBC");
        }

        Thread.sleep(31*1000);

        for (int i = 0 ; i < 4; i++) {
            wikiM.simpleSearch("zeitgeist", 3);
        }

        wikiM.trending(3);

        ArrayList<String> expected = new ArrayList<>();
        expected.add("zeitgeist");

        Assert.assertEquals(expected, wikiM.trending(3));
    }

    @Test
    public void trendingTest2() throws InterruptedException {
        WikiMediator wikiM = new WikiMediator();

        for (int i = 0 ; i < 4; i++) {
            wikiM.simpleSearch("zeitgeist", 1);
        }

        for (int i = 0 ; i < 4; i++) {
            wikiM.simpleSearch("hi", 1);
        }

        Thread.sleep(12*1000);

        for (int i = 0 ; i < 5; i++) {
            wikiM.simpleSearch("goodbye", 1);
        }

        for (int i = 0 ; i < 6; i++) {
            wikiM.getPage("UBC");
        }

        ArrayList<String> expected = new ArrayList<>();
        expected.add("UBC");
        expected.add("zeitgeist");
        expected.add("hi");

        System.out.println(expected.toString() + "\n" + wikiM.trending(3).toString());
        Assert.assertEquals(expected, wikiM.trending(3));
    }

    @Test
    public void simpleSearchTest2() {
        WikiMediator wikiM = new WikiMediator();

        Wiki wiki = new Wiki("en.wikipedia.org");

        List<String> nothing = wikiM.simpleSearch("", 1);

        Assert.assertTrue(nothing.isEmpty());

        System.out.print(wiki.search("dark side of the blue sun", 2).toString() + "\n ");

    }


}
