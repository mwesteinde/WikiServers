package cpen221.mp3.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cpen221.mp3.wikimediator.WikiMediator;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * A server which allows a client to use WikiMediator and can handle a specified number of
 * requests simultaneously.
 */

public class WikiMediatorServer {

    static final String IP_ADDRESS = "197.35.26.96";

    private final WikiMediator wikiM = new WikiMediator();
    private int clientLimit;
    private int port;
    private int activeClients = 0;
    private ServerSocket serverSocket;

    private final String timeoutReply = "{status: \"failed\", response: \"Operation timed out.\"}";

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
        this.port = port;
        this.clientLimit = n;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.print("Exception thrown creating server socket.");
            e.printStackTrace();
        }
    }

    private void wikiServe() {
        try {
            if ((port <= 0 || port > 65535)) {
                throw new Exception("Port issue.");
            }

            System.out.println("Running");

            // create socket and connect client
            Boolean listening = true;

            while (listening) {
                System.out.println("Thread started");
                Socket clientSocket = serverSocket.accept();
                Thread wikiClientHandler = new Thread(new Runnable() {
                    public void run() {
                        try {
                            try {
                                wikiServerThread(clientSocket);
                            } finally {
                                clientSocket.close();
                            }
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                });
                // starts thread
                wikiClientHandler.start();
            }

        } catch (InterruptedException ie) {
            System.out.println("Interrupted exception");
            ie.printStackTrace();
        } catch (IOException ioe) {
            System.out.println("Issue with socketing.");
            ioe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processRequest(JsonObject inputQuery, JsonObject response) throws IOException {
        // GETTING ID
        System.out.println("Processing Request");

        String id = inputQuery.get("id").getAsString();
        String type = inputQuery.get("type").getAsString();

        response.addProperty("id", id);
        response.addProperty("status", "pending");

        try {
            switch (type) {
                case "simpleSearch":
                    System.out.println("simpleSearch Request");
                    simpleSearchRequest(inputQuery, response);
                    break;
                // GETPAGE
                case "getPage":
                    System.out.println("getPage Request");
                    getPageRequest(inputQuery, response);
                    break;
                // GETCONNECTEDPAGES -- String pageTitle, int hops
                case "getConnectedPages":
                    System.out.println("getConnectedPages Request");
                    getConnectedPagesRequest(inputQuery, response);
                    break;
                // ZEITGEIST -- int limit
                case "zeitgeist":
                    System.out.println("zeitgeist Request");
                    zeitgeistRequest(inputQuery, response);
                    break;
                // TRENDING -- int limit
                case "trending":
                    System.out.println("trending Request");
                    trendingRequest(inputQuery, response);
                    break;
                case "peakLoad30s":
                    System.out.println("peakLoad30s request");
                    peakLoad30sRequest(inputQuery, response);
                default:
                    response.remove("status");
                    response.addProperty("status", "failed");
                    throw new IOException();
            }
            response.remove("status");
            response.addProperty("status", "success");
        } catch (InterruptedIOException e) {
            e.printStackTrace();
        }
    }

    /*
    inputs: query (string), limit (int)
    returns: String list
    */
    private void simpleSearchRequest(JsonObject inputQuery, JsonObject response) {
        List responseList = wikiM.simpleSearch(inputQuery.get("query").getAsString(), inputQuery.get("limit").getAsInt());
        response.addProperty("response", responseList.toString());
    }

    /*
    inputs: pageTitle (string)
    returns String
    */
    private void getPageRequest(JsonObject inputQuery, JsonObject response) {
        response.addProperty("response", wikiM.getPage(inputQuery.get("pageTitle").getAsString()));
    }

    /*
    inputs: pageTitle (string), hops (int)
    returns: String list
    */
    private void getConnectedPagesRequest(JsonObject inputQuery, JsonObject response) {
        String stringResponse = wikiM.getConnectedPages(inputQuery.get("pageTitle").getAsString(), inputQuery.get("hops").getAsInt()).toString();
        response.addProperty("response", stringResponse);
    }

    /*
     * inputs: limit (int)
     * returns: String list
     * */
    private void trendingRequest(JsonObject inputQuery, JsonObject response) {
        response.addProperty("response", wikiM.trending(inputQuery.get("limit").getAsInt()).toString());
    }

    /*
     * inputs: limit (int)
     * returns: string list
     */
    private void zeitgeistRequest(JsonObject inputQuery, JsonObject response) {
//        List responseList = wikiM.zeitgeist(inputQuery.get("limit").getAsInt());
//        String stringResponse = responseList.toString();
        response.addProperty("response", wikiM.zeitgeist(inputQuery.get("limit").getAsInt()).toString());

    }

    /*
     * input: nothing
     * returns: int
     */
    private void peakLoad30sRequest(JsonObject inputQuery, JsonObject response) {
        response.addProperty("response", wikiM.peakLoad30s());
    }

    private void wikiServerThread(Socket cSocket) throws IOException {
        if (activeClients > clientLimit) {
            System.out.println("Too many clients, please try again later.");
            cSocket.close();
        }

        System.out.println("New client connected");
        activeClients++;

        // create reader and writer
        PrintWriter writer =
                new PrintWriter(new OutputStreamWriter(cSocket.getOutputStream()), true);
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(cSocket.getInputStream()));

        System.out.println("reader and writer created");
        String jsonString;
        for (jsonString = reader.readLine(); jsonString != null; jsonString = reader.readLine()) {
            System.out.println("read line " + jsonString);
            // parsing to json object
            JsonElement json = JsonParser.parseString(jsonString);
            System.out.println("element created");

            JsonObject response = new JsonObject();

            if (json.getAsJsonObject() != null) {
                JsonObject inputQuery = json.getAsJsonObject();
                System.out.println("About to process request");

                // for timeout thread race
                AtomicInteger winner = new AtomicInteger(0);
                //THREAD
                Thread process = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            processRequest(inputQuery, response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        winner.compareAndSet(0, 1);
                    }
                });

                JsonElement timeout = inputQuery.get("timeout");
                Thread timer = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(timeout.getAsInt() * 1000);
                        } catch (InterruptedException ignored) {
                        }
                        winner.compareAndSet(0, 2);
                    }
                });
                if (timeout != null) {
                    timer.start();
                }

                process.start();

                while (winner.get() == 0) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                int resultOfRace = winner.get();
                System.out.println(resultOfRace);

                timer.interrupt();
                process.interrupt();

                if (resultOfRace == 1) {
                    response.remove("status");
                    response.addProperty("status", "success");
                    System.out.println("Parsed and ready: " + response.toString());
                    writer.println(response.toString());
                    System.out.println("sent: " + response.toString());
                } else {
                    JsonElement timeoutResult = JsonParser.parseString(timeoutReply);
                    JsonObject result = timeoutResult.getAsJsonObject();
                    result.add("id", inputQuery.get("id"));
                    writer.println(result.toString());
                    System.out.println("sent: " + response.toString());
                }
            } else {
                response.remove("status");
                response.addProperty("status", "failed");
                System.out.println("Failed ready to send: " + response.toString());
                writer.print(response.toString());
                System.out.println("sent");
            }
        }

        cSocket.close();

        System.out.println("Leaving thread.");

        writer.flush();
        reader.close();
        cSocket.close();
        activeClients--;
    }

    public static void main(String[] args) {
        try {
            WikiMediatorServer wikiServer = new WikiMediatorServer(555, 1);
            wikiServer.wikiServe();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


