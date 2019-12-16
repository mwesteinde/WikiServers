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

    private Boolean status;
    private int limit;
    private int port;

    private ServerSocket sSocket;

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
        if (port <= 0 || port > 65535) {

        }
        this.limit = n;
        this.port = port;

        // will take inputs of type String which will be JSON formattable
        try {
            this.sSocket = new ServerSocket(this.port);
            Socket cSocket = sSocket.accept();
            PrintWriter w = new PrintWriter(new OutputStreamWriter(cSocket.getOutputStream()), true);
            BufferedReader r = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));

            // should be in JSON formattable string
            //TODO: learn how to check this

            JsonParser parser = new JsonParser();

            // get the string
            String jsonString = r.readLine();
            // create where to store the file
            // create a JSON file with the string
            Gson gson = new Gson();
            gson.toJson(jsonString);
        } catch (Exception e) {
            System.out.println("Server failed to ");
        }


        // Getting unput string, turning into JSON object, reading and responding
        try {
            this.sSocket = new ServerSocket(this.port);
            Socket cSocket = sSocket.accept();
            if (!cSocket.isConnected()) {
                //TODO: return failed JSON object
            }

            PrintWriter w = new PrintWriter(new OutputStreamWriter(cSocket.getOutputStream()), true);
            BufferedReader r = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));

            // PARSING INTO A JSON OBJECT
            String jsonString = r.readLine();
            JsonElement json = JsonParser.parseString(jsonString);

            if (json.isJsonObject()) {
                JsonObject inputQuery = json.getAsJsonObject();

                // GETTING ID
                try {
                    String id = inputQuery.get("id").getAsString();
                } catch (Exception e) {
                    System.out.println("No id");
                }

                // GETTING TYPE
                try {
                    String type = inputQuery.get("type").getAsString();
                    JsonObject response;

                    // SIMPLE SEARCH
                    if (type.equals("simpleSearch")) {
                        try {
                            String query = inputQuery.get("query").getAsString();
                            String limit = inputQuery.get("limit").getAsString();
                            response = responding(inputQuery, "simpleSearch");
                            //TODO: make sure this is ok
                            w.write(response.toString());
                            w.flush();
                        } catch (Exception e) {
                            System.out.println("simpleSearch invalid input");
                        }
                    }
                    // GETPAGE -- String pageTitle
                    if (type.equals("getPage")) {
                        try {
                            String pageTitle = inputQuery.get("getPage").getAsString();
                            response = responding(inputQuery, "simpleSearch");
                            //TODO: make sure this is ok
                            w.write(response.toString());
                            w.flush();

                        } catch (Exception e) {
                            System.out.println("getPage missing pageTitle");
                        }
                    }
                    // GETCONNECTEDPAGES -- String pageTitle, int hops
                    if (type.equals("getConnectedPages")) {
                        try {
                            String pageTitle = inputQuery.get("pageTitle").getAsString();
                            String hops = inputQuery.get("hops").getAsString();
                            response = responding(inputQuery, "getConnectedPages");
                            //TODO: make sure this is ok
                            w.write(response.toString());
                            w.flush();
                        } catch (Exception e) {
                            System.out.println("getConnectedPages invalid input");
                        }
                    }
                    // ZEITGEIST -- int limit
                    // TRENDING -- int limit
                    if (type.equals("zeitgeist") || type.equals("trending")) {
                        try {
                            String limit = inputQuery.get("limit").getAsString();

                            if (type.equals("zeitgeist")) {
                                response = responding(inputQuery, "zeitgeist");
                            } else {
                                response = responding(inputQuery, "trending");
                            }
                            //TODO: make sure this is ok
                            w.write(response.toString());
                            w.flush();
                        } catch (Exception e) {
                            System.out.println("zeitgeist or trending needs limit");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("No type specified");
                }
            }
        } catch (IOException ioe) {
            System.out.println("Issue with socketing");
        }


        // USING wikimediator TO RESPOND


    }

    // generates a json response object
    private JsonObject responding(JsonObject query, String queryType) {
        WikiMediator wikiM = new WikiMediator();
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.add("id", query.get("id"));
        jsonResponse.addProperty("status", "success");

        // SIMPLESEARCH
        if (queryType.equals("simpleSearch")) {
            List response = wikiM.simpleSearch(query.get("query").getAsString(), query.get("limit").getAsInt());
            String stringResponse = String.join(",", response);
            jsonResponse.addProperty("response", stringResponse);
            return jsonResponse;
        }
        // GETPAGE
        if (queryType.equals("getPage")) {
            String response = wikiM.getPage(query.get("pageTitle").getAsString());
            jsonResponse.addProperty("response", response);
            return jsonResponse;
        }
        // GETCONNECTEDPAGES
        if (queryType.equals("getConnectedPages")) {
            List response = wikiM.getConnectedPages(query.get("query").getAsString(), query.get("hops").getAsInt());
            String stringResponse = String.join(",", response);
            jsonResponse.addProperty("response", stringResponse);
            return jsonResponse;
        }
        // ZEITGEIST
        if (queryType.equals("zeitgeist")) {
            List response = wikiM.zeitgeist(query.get("limit").getAsInt());
            String stringResponse = String.join(",", response);
            jsonResponse.addProperty("response", stringResponse);
            return jsonResponse;
        }
        // TRENDING
        if (queryType.equals("trending")) {
            List response = wikiM.trending(query.get("limit").getAsInt());
            String stringResponse = String.join(",", response);
            jsonResponse.addProperty("response", stringResponse);
            return jsonResponse;
        }
        // otherwise failed...?
        jsonResponse.addProperty("status", "failed");
        jsonResponse.addProperty("response", "Invalid query.");
        return jsonResponse;
    }

}
