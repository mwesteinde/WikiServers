package cpen221.mp3.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cpen221.mp3.wikimediator.WikiMediator;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;


/**
 * A server which allows a client to use WikiMediator and can handle a specified number of
 * requests simultaneously.
 */

public class WikiMediatorServer {

    static final String FAILED_MESSAGE = "failed";
    static final String TIMEOUT_MESSAGE = "Operation timed out.";
    static final String SUCCESS_MESSAGE = "success";
    static final Boolean FAILED = false;
    static final Boolean SUCCESS = true;
    static final String IP_ADDRESS = "197.35.26.96";
    private final WikiMediator wikiM = new WikiMediator();


    private Boolean status;
    private int limit;
    private int port;

    private ServerSocket sSocket;

    // TODO: make this capable of handling multiple clients at once

    //RI: Each input JSON object has a type and an id. Each output JSON object has an id, status and response.
    //The type is a method that has been implemented in WikiMediator
    //Limit >= 0
    //Status is either success or failed

    //AF: The function to implement is represented by the JSON object
    //Id is a unique identifier
    //Type is a method to implement.
    //Limit is the max amount of responses to return
    //Query is an input to the method
    //Status is an identifier whether the method ran correctly or not
    //Response is the output of the method

    /**
     * Start a server at a given port number, with the ability to process
     * up to n requests concurrently. Writes a JSON formatted string with an id, status success or fail and response.
     *
     * @param port the port number to bind the server to
     * @param n    the number of concurrent requests the server can handle, cannot be negative.
     */
    public WikiMediatorServer(int port, int n) {
        //TODO: figure out what to do here
        try {
            if (!(port <= 0 || port > 65535)) {
                throw new Exception("Port issue.");
            }

            this.limit = n;
            this.port = port;

            // create socket
            this.sSocket = new ServerSocket(this.port);
            Socket cSocket = sSocket.accept();

            // create reader and writer
            PrintWriter w = new PrintWriter(new OutputStreamWriter(cSocket.getOutputStream()), true);
            BufferedReader r = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));

            if (!cSocket.isConnected()) {
                throw new IOException("Socket connection issue.");
            }

            // parsing to json object
            String jsonString = r.readLine();
            JsonElement json = JsonParser.parseString(jsonString);
            JsonObject response = new JsonObject();
            response.addProperty("status", "pending");

            if (json.getAsJsonObject() != null) {
                JsonObject inputQuery = json.getAsJsonObject();
                processRequest(inputQuery, response);
                response.remove("status");
                response.addProperty("status", "success");
            } else {
                response.remove("status");
                response.addProperty("status", "failed");
                w.write(response.toString());
            }
        } catch (IOException ioe) {
            System.out.println("Issue with socketing.");
        } catch (Exception e) {
            System.out.println("Error...");
        }
    }


    private void processRequest(JsonObject inputQuery, JsonObject response) throws IOException {
        // GETTING ID
        String id = inputQuery.get("id").getAsString();
        String type = inputQuery.get("type").getAsString();

        response.addProperty("id", id);
        response.addProperty("status", "pending");

        // SIMPLE SEARCH
        switch (type) {
            case "simpleSearch":
                simpleSearchRequest(inputQuery, response);
                break;
            // GETPAGE
            case "getPage":
                getPageRequest(inputQuery, response);
                break;
            // GETCONNECTEDPAGES -- String pageTitle, int hops
            case "getConnectedPages":
                getConnectedPagesRequest(inputQuery, response);
                break;
            // ZEITGEIST -- int limit
            case "zeitgeist":
                zeitgeistRequest(inputQuery, response);
                break;
            // TRENDING -- int limit
            case "trending":
                trendingRequest(inputQuery, response);
                break;
            default:
                throw new IOException();
        }

    }

    private void simpleSearchRequest(JsonObject inputQuery, JsonObject response) {
        // getting information to use wikimediator
        String query = inputQuery.get("query").getAsString();
        String limit = inputQuery.get("limit").getAsString();

        // add search results
        List responseList = wikiM.simpleSearch(inputQuery.get("query").getAsString(), inputQuery.get("limit").getAsInt());
        String stringResponse = String.join(",", responseList);
        response.addProperty("response", stringResponse);
    }

    private void getPageRequest(JsonObject inputQuery, JsonObject response) {

        String responseString = wikiM.getPage(inputQuery.get("pageTitle").getAsString());
        response.addProperty("response", responseString);

    }

    private void getConnectedPagesRequest(JsonObject inputQuery, JsonObject response) {
        List responseList = wikiM.getConnectedPages(inputQuery.get("query").getAsString(), inputQuery.get("hops").getAsInt());
        String stringResponse = String.join(",", responseList);
        response.addProperty("response", stringResponse);

    }

    private void trendingRequest(JsonObject inputQuery, JsonObject response) {
        List responseList = wikiM.trending(inputQuery.get("limit").getAsInt());
        String stringResponse = String.join(",", responseList);
        response.addProperty("response", stringResponse);

    }

    private void zeitgeistRequest(JsonObject inputQuery, JsonObject response) {
        List responseList = wikiM.zeitgeist(inputQuery.get("limit").getAsInt());
        String stringResponse = String.join(",", responseList);
        response.addProperty("response", stringResponse);

    }

}
