package cpen221.mp3;

import com.google.gson.JsonObject;
import cpen221.mp3.cache.Cache;
import cpen221.mp3.cache.NotPresentException;
import cpen221.mp3.cache.StringCacheable;
import cpen221.mp3.server.WikiClient;
import cpen221.mp3.server.WikiMediatorServer;
import cpen221.mp3.wikimediator.WikiMediator;
import fastily.jwiki.core.Wiki;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
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
    public void getConnectedTestEmpty() {
        WikiMediator wikiM = new WikiMediator();
        Wiki wiki = new Wiki("en.wikipedia.org");

        Assert.assertEquals("UBC", wikiM.getConnectedPages("UBC", 0).get(0));

        List<String> wikiMConnections = wikiM.getConnectedPages("GFHEKJKNJE", 3);

        Assert.assertTrue(wikiMConnections.isEmpty());
    }

    @Test
    public void getConnectedTestMultipleHops() {
        WikiMediator wikiM = new WikiMediator();
        Wiki wiki = new Wiki("en.wikipedia.org");

        List<String> wikiMConnections = wikiM.getConnectedPages("UBC", 2);
        List<String> wikiConnections = new ArrayList<>();

        wikiMConnections.forEach(System.out::print);
    }

    @Test
    public void getPageTest() {
        WikiMediator wikiM = new WikiMediator();
        Wiki wiki = new Wiki("en.wikipedia.org");

        String sM = wikiM.getPage("UBC");
        String sT = wikiM.getPage("UBC");
        String s = wiki.getPageText("UBC");

        Assert.assertEquals(sM, s);
    }

    @Test
    public void getPageTestEmptyString() {
        WikiMediator wikiM = new WikiMediator();
        Wiki wiki = new Wiki("en.wikipedia.org");

        String sM = wikiM.getPage("UBChfkdkjnfkdnjj");
        Assert.assertEquals(sM, "");

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
        for (int i = 0 ; i < 2; i++) {
            wikiM.getConnectedPages("Barack Obama", 1);
        }


        ArrayList<String> expected = new ArrayList<>();
        expected.add("zeitgeist");
        List actual = wikiM.zeitgeist(1);
        List actual2 = wikiM.zeitgeist(4);
        ArrayList<String> expected2 = new ArrayList<>();
        expected2.add("zeitgeist");
        expected2.add("hello");
        expected2.add("UBC");



        Assert.assertEquals(expected, actual);
        Assert.assertEquals(expected2, actual2);

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

        Assert.assertTrue(wikiM.trending(0).isEmpty());
    }

    @Test
    public void trendingTestRepeated() throws InterruptedException {
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

        Thread.sleep(31*1000);

        for (int i = 0 ; i < 3; i++) {
            wikiM.simpleSearch("zeitgeist", 3);
        }

        for (int i = 0 ; i < 4; i++) {
            wikiM.simpleSearch("Barack Obama", 3);
        }



        ArrayList<String> expected = new ArrayList<>();
        expected.add("Barack Obama");

        Assert.assertEquals(expected, wikiM.trending(1));

        Assert.assertTrue(wikiM.trending(0).isEmpty());
    }

    @Test
    public void trendingTest2() throws InterruptedException {
        WikiMediator wikiM = new WikiMediator();

        for (int i = 0 ; i < 3; i++) {
            wikiM.simpleSearch("zeitgeist", 1);
        }

        for (int i = 0 ; i < 4; i++) {
            wikiM.simpleSearch("hi", 1);
        }

        Thread.sleep(5*1000);

        for (int i = 0 ; i < 5; i++) {
            wikiM.simpleSearch("goodbye", 1);
        }

        for (int i = 0 ; i < 6; i++) {
            wikiM.getPage("UBC");
        }
        Assert.assertTrue(wikiM.checkRep());

        ArrayList<String> expected = new ArrayList<>();
        expected.add("UBC");
        expected.add("goodbye");
        expected.add("hi");

        List trending = wikiM.trending(3);
        System.out.println(expected.toString() + "\n" + trending.toString());
        Assert.assertEquals(expected, trending);
    }

    @Test
    public void simpleSearchTest2() {
        WikiMediator wikiM = new WikiMediator();

        Wiki wiki = new Wiki("en.wikipedia.org");

        List<String> nothing = wikiM.simpleSearch("", 1);

        Assert.assertTrue(nothing.isEmpty());

        System.out.print(wiki.search("dark side of the blue sun", 2).toString() + "\n ");

    }

    @Test
    public void peakLoad30sTest() throws InterruptedException {
        WikiMediator wikiM = new WikiMediator();

        for (int i = 0 ; i < 3; i++) {
            wikiM.simpleSearch("zeitgeist", 1);
        }

        for (int i = 0 ; i < 4; i++) {
            wikiM.simpleSearch("hi", 1);
        }

        Thread.sleep(5*1000);

        for (int i = 0 ; i < 5; i++) {
            wikiM.simpleSearch("goodbye", 1);
        }

        for (int i = 0 ; i < 6; i++) {
            wikiM.getPage("UBC");
        }

        Assert.assertEquals(19, wikiM.peakLoad30s());

    }

    @Test
    public void peakLoad30sTestTime() throws InterruptedException {
        WikiMediator wikiM = new WikiMediator();

        for (int i = 0 ; i < 3; i++) {
            wikiM.simpleSearch("zeitgeist", 1);
        }

        for (int i = 0 ; i < 4; i++) {
            wikiM.simpleSearch("hi", 1);
        }

        Thread.sleep(60*1000);

        for (int i = 0 ; i < 5; i++) {
            wikiM.simpleSearch("goodbye", 1);
        }

        for (int i = 0 ; i < 6; i++) {
            wikiM.getPage("UBC");
        }

        Assert.assertEquals(12, wikiM.peakLoad30s());
}

    @Test
    public void peakLoad30sTestTime2() throws InterruptedException {
        WikiMediator wikiM = new WikiMediator();

        for (int i = 0 ; i < 3; i++) {
            wikiM.simpleSearch("zeitgeist", 1);
        }

        for (int i = 0 ; i < 4; i++) {
            wikiM.simpleSearch("hi", 1);
        }

        Thread.sleep(31*1000);

        for (int i = 0 ; i < 5; i++) {
            wikiM.simpleSearch("goodbye", 1);
        }

        for (int i = 0 ; i < 6; i++) {
            wikiM.getPage("UBC");
        }

        Assert.assertEquals(12, wikiM.peakLoad30s());
    }


    StringCacheable sc1 = new StringCacheable("a", "a");
    StringCacheable sc2 = new StringCacheable("b", "b");
    StringCacheable sc3 = new StringCacheable("c", "c");
    StringCacheable sc4 = new StringCacheable("d", "d");
    StringCacheable sc5 = new StringCacheable("e", "e");
    StringCacheable sc6 = new StringCacheable("f", "f");
    StringCacheable sc7 = new StringCacheable("g", "g");

    @Test(expected = NotPresentException.class)
    public void cacheFullTest() throws NotPresentException, InterruptedException {
        Cache thisCache = new Cache(5,1000);

        thisCache.put(sc1);
        Thread.sleep(1);
        thisCache.put(sc2);
        Thread.sleep(1);
        thisCache.put(sc3);
        Thread.sleep(1);
        thisCache.put(sc4);
        Thread.sleep(1);
        thisCache.put(sc5);
        Thread.sleep(1);
        thisCache.put(sc6);
        Thread.sleep(1);
        thisCache.put(sc7);
        thisCache.put(sc7);

        thisCache.get("a");
        boolean sentinel = false;
        try {
            thisCache.get("b");
        } catch (NotPresentException e) {
            sentinel = true;
        }
        Assert.assertTrue(sentinel);
    }

    @Test
    public void cacheUpdateTest() throws NotPresentException, InterruptedException {
        Cache thisCache = new Cache(7,1000);

        thisCache.put(sc1);
        Thread.sleep(1);
        thisCache.put(sc2);
        Thread.sleep(1);
        thisCache.put(sc3);
        Thread.sleep(1);
        thisCache.put(sc4);
        Thread.sleep(1);
        thisCache.put(sc5);
        Thread.sleep(1);
        thisCache.put(sc6);

        StringCacheable changed = new StringCacheable("a", "sd");


        Assert.assertTrue(thisCache.update(changed));
        Assert.assertTrue(!thisCache.update(sc7));
        Assert.assertEquals(thisCache.get("a"), changed);

    }

    @Test
    public void cacheTouchTest() throws NotPresentException, InterruptedException{
        Cache thisCache = new Cache(7,1000);

        thisCache.put(sc1);
        long time = System.currentTimeMillis();
        Thread.sleep(1);
        thisCache.put(sc2);
        Thread.sleep(1);
        thisCache.put(sc3);
        Thread.sleep(1);
        thisCache.put(sc4);
        Thread.sleep(1);
        thisCache.put(sc5);
        Thread.sleep(1);
        thisCache.put(sc6);

        Assert.assertTrue(thisCache.touch("a"));
        Assert.assertTrue(!thisCache.touch("g"));
        Assert.assertEquals(thisCache.get("a"), sc1);
    }

    @Test
    public void serverTest() {
        //
        WikiMediatorServer wikiServer = new WikiMediatorServer(3, 3);
        try{
            WikiClient bill = new WikiClient("localhost", 3);
            //WikiClient angela = new WikiClient("localhost", 555);
            bill.sendRequest("{id: \"localhost\", type: \"simpleSearch\", query: \"Barack Obama\", limit: \"12\", timeout: \"10000\"}");
            //angela.sendRequest("{id: \"localhost\", type: \"getConnectedPages\", type: \"getConnectedPages\", hops: \"1000\", timeout: \"2\"}");

           // JsonObject replyToAngela = angela.getReply();
            //System.out.println("Reply to angela: " + replyToAngela.toString());

            //angela.sendRequest("{id: \"localhost\", type: \"zeitgeist\", limit: \"2\"}\n" +
             //       "{id: \"localhost\", type: \"peakLoad30s\", timeout: \"2\"}");

            JsonObject replyToBill = bill.getReply();

            //replyToAngela = angela.getReply();
            System.out.println("Both clients: " + replyToBill.toString() + "\n"  /**replyToAngela.toString()*/);

            bill.close();
            //angela.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void serverTest2() {
        new WikiMediatorServer(236, 3);
        try {
            WikiClient bill = new WikiClient("localhost", 236);
            // getConnectedPages : pageTitle hops
            bill.sendRequest("{id: \"localhost\", type: \"getConnectedPages\", pageTitle: \"Barack Obama\", hops: \"2\", timeout: \"4\"}");

            // simplesearch : query, limit
            bill.sendRequest("{id: \"localhost\", type: \"simpleSearch\", pageTitle: \"wikiMediator\", limit: \"3\", timeout: \"2\"}");

            // getPage : pageTitle
            for (int i = 0; i < 2; i++) {
                bill.sendRequest("{id: \"localhost\", type: \"simpleSearch\", pageTitle: \"wikiMediator\", limit: \"3\", timeout: \"2\"}");
            }

            // peakLoad30s
            bill.sendRequest("{id: \"localhost\", type: \"peakLoad30s\"}");

            // trending : limit
            bill.sendRequest("{id: \"localhost\", type: \"trending\", limit: \"3\"}");

            // zeitgeist : limit

            JsonObject reply = bill.getReply();

            System.out.println("Reply: " + reply.toString());

            bill.close();

        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

    }

    @Test
    public void getPath() {
        WikiMediator wikiM = new WikiMediator();
        Wiki wiki = new Wiki("en.wikipedia.org");
        String startPage = "Nhlanhla Nene";
        String stopPage = "Lucas Musculus";

        List<String> wikiMConnections = wikiM.getPath(startPage, stopPage);
        for (int i = 0; i < wikiMConnections.size() - 1; i++) {
            Assert.assertTrue(wiki.getLinksOnPage(wikiMConnections.get(i)).contains(wikiMConnections.get(i+1)));
        }
        Assert.assertTrue(wikiMConnections.get(wikiMConnections.size() - 1).equals(stopPage));

    }

    @Test
    public void getPath2() {
        WikiMediator wikiM = new WikiMediator();
        Wiki wiki = new Wiki("en.wikipedia.org");
        String startPage = "Persig Gunungkidul";
        String stopPage = "CF Peralada";

        List<String> wikiMConnections = wikiM.getPath(startPage, stopPage);
        for (int i = 0; i < wikiMConnections.size() - 1; i++) {
            Assert.assertTrue(wiki.getLinksOnPage(wikiMConnections.get(i)).contains(wikiMConnections.get(i+1)));
        }
        Assert.assertTrue(wikiMConnections.get(wikiMConnections.size() - 1).equals(stopPage));

    }

    @Test
    public void getPath3() {
        WikiMediator wikiM = new WikiMediator();
        Wiki wiki = new Wiki("en.wikipedia.org");
        String startPage = "M. G. Ramachandran's unrealized projects";
        String stopPage = "Bethany England";

        List<String> wikiMConnections = wikiM.getPath(startPage, stopPage);
        for (int i = 0; i < wikiMConnections.size() - 1; i++) {
            Assert.assertTrue(wiki.getLinksOnPage(wikiMConnections.get(i)).contains(wikiMConnections.get(i+1)));
        }
        Assert.assertTrue(wikiMConnections.get(wikiMConnections.size() - 1).equals(stopPage));

    }


}
