package cpen221.mp3.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.Socket;

public class WikiClient {
    private Socket socket;
    private BufferedReader reader;
    private  PrintWriter writer;
    private String hostName;

    public WikiClient(String hostname, int port) throws IOException {
        socket = new Socket(hostname, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        hostName = hostname;
    }

    public void sendRequest(String s) throws IOException {
        writer.println(s);
        System.out.println("Sending request");
        writer.flush();
    }

    public JsonObject getReply() throws IOException {
        System.out.println("getting reply");
        String reply = reader.readLine();
        System.out.println("Reply received: "+reply);

        if(reply == null) {
            throw new IOException("connection terminated unexpectedly");
        }

        try{
            JsonElement jObj = JsonParser.parseString(reply);
            return jObj.getAsJsonObject();
        } catch(Exception e) {
            System.out.println("Issue parsing reply to JSON");
            e.printStackTrace();
        } return null;
    }

    public void close() throws IOException {
        writer.close();
        reader.close();
        socket.close();
    }


    public static void main(String[] args) {
//        try{
////            WikiClient bill = new WikiClient("localhost", 555);
//            WikiClient angela = new WikiClient("localhost", 555);
////            bill.sendRequest("{id: \"localhost\", type: \"simpleSearch\", query: \"Barack Obama\", limit: \"12\", timeout: \"10000\"}");
//            angela.sendRequest("{id: \"localhost\", type: \"getConnectedPages\", type: \"getConnectedPages\", hops: \"1000\", timeout: \"2\"}");
//
//            JsonObject replyToAngela = angela.getReply();
//            System.out.println(/*replyToBill.toString() + "\n" +*/ replyToAngela.toString());
//
//            angela.sendRequest("{id: \"localhost\", type: \"zeitgeist\", limit: \"2\"}\n" +
//                    "{id: \"localhost\", type: \"peakLoad30s\", timeout: \"2\"}");
////            JsonObject replyToBill = bill.getReply();
////           JsonObject firstReplyToAngela = angela.getReply();
////            System.out.println(/*replyToBill.toString() + "\n" +*/ firstReplyToAngela.toString());
//
//            replyToAngela = angela.getReply();
////            System.out.println(/*replyToBill.toString() + "\n" +*/ firstReplyToAngela.toString());
//
//            System.out.println(/*replyToBill.toString() + "\n" +*/ replyToAngela.toString());
////            bill.close();
//            angela.close();
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }



        try {
            WikiClient bill = new WikiClient("localhost", 555);
            JsonObject reply;
            // getConnectedPages : pageTitle hops -- works - timeout
            bill.sendRequest("{id: \"localhost\", type: \"getConnectedPages\", pageTitle: \"Barack Obama\", hops: \"2\", timeout: \"4\"}");

            reply = bill.getReply();
            System.out.println("Reply: " + reply.toString());
//
            // simplesearch : query, limit -- works
            System.out.println("Simple search: ");
            bill.sendRequest("{id: \"localhost\", type: \"simpleSearch\", query: \"wikiMediator\", limit: \"3\", timeout: \"2\"}");
//
            reply = bill.getReply();
            System.out.println("Reply: " + reply.toString());

            System.out.println("pageTitle:");
            // getPage : pageTitle -- doesn't work
            for (int i = 0; i < 2; i++) {
                bill.sendRequest("{id: \"localhost\", type: \"getPage\", pageTitle: \"hello\",timeout: \"2\"}");
                System.out.println("getPage: " + bill.getReply());
            }

            System.out.println("peakLoad:");
            // TODO: fix status update...
            // peakLoad30s -- working
            bill.sendRequest("{id: \"localhost\", type: \"peakLoad30s\"}");
            reply = bill.getReply();
            System.out.println("Reply: " + reply.toString());

            // trending : limit
            bill.sendRequest("{id: \"localhost\", type: \"trending\", limit: \"3\"}");
            reply = bill.getReply();
            System.out.println("Reply: " + reply.toString());

            // zeitgeist : limit
            System.out.println("Zeitgeist:");

            bill.sendRequest("{id: \"localhost\", type: \"zeitgeist\", limit: \"2\"}");
            reply = bill.getReply();
            System.out.println("Reply: " + reply.toString());

            bill.close();

        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
