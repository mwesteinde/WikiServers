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

    private final WikiMediator wikiM = new WikiMediator();
    private int clientLimit;
    private int port;
    private int activeClients = 0;
    private ServerSocket serverSocket;
    private static final int DEFAULT_PORT = 4949;

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
     * @param port the port number to bind the server to, cannot be 4949
     * @param n    the number of concurrent requests the server can handle, cannot be negative.
     */
    public WikiMediatorServer(int port, int n) {
        this.port = port;
        this.clientLimit = n;
        try {
            if ((port <= 0 || port > 65535)) {
                throw new Exception("Port number not allowed.");
            }
            serverSocket = new ServerSocket(this.port);
            serve();
        } catch (IOException e) {
            System.out.println("Exception thrown creating server socket.");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void serve() {
        try {
            // create socket and connect client
            while (true) {
                // waits until one client stops using the server until new client is connected
                Socket clientSocket = serverSocket.accept();
                Thread wikiClientHandler = new Thread(new Runnable() {
                    public void run() {
                        try {
                            try {
                                if (activeClients < clientLimit) {
                                    wikiServerThread(clientSocket);
                                } else {
                                    while (activeClients >= clientLimit) {
                                        Thread.sleep(10);
                                    }
                                    wikiServerThread(clientSocket);
                                }
                            } finally {
                                clientSocket.close();
                            }
                        } catch (IOException | InterruptedException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                });
                // starts thread
                wikiClientHandler.start();
            }

        } catch (IOException ioe) {
            System.out.println("Issue with socketing.");
            ioe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processRequest(JsonObject inputQuery, JsonObject response) throws IOException {
        // GETTING ID
        String id = inputQuery.get("id").getAsString();
        String type = inputQuery.get("type").getAsString();

        response.addProperty("id", id);
        response.addProperty("status", "pending");

        try {
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
                case "peakLoad30s":
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

    private void wikiServerThread(Socket cSocket) throws IOException, InterruptedException {
        activeClients++;

        PrintWriter writer =
                new PrintWriter(new OutputStreamWriter(cSocket.getOutputStream()), true);
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(cSocket.getInputStream()));

        String jsonString;

        for (jsonString = reader.readLine(); jsonString != null; jsonString = reader.readLine()) {
            // parsing to json object
            JsonElement json = JsonParser.parseString(jsonString);

            JsonObject response = new JsonObject();

            if (json.getAsJsonObject() != null) {
                JsonObject inputQuery = json.getAsJsonObject();

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

                // stop both threads
                timer.interrupt();
                process.interrupt();

                // if no timeout
                if (resultOfRace == 1) {
                    response.remove("status");
                    response.addProperty("status", "success");
                    writer.println(response.toString());
                }
                // if timeout
                else {
                    JsonElement timeoutResult = JsonParser.parseString(timeoutReply);
                    JsonObject result = timeoutResult.getAsJsonObject();
                    result.add("id", inputQuery.get("id"));
                    writer.println(result.toString());
                    System.out.println("sent: " + response.toString());
                }
            } else {
                // no response received
                response.remove("status");
                response.addProperty("status", "failed");
                writer.print(response.toString());
            }
        }

        cSocket.close();

        writer.flush();
        reader.close();
        activeClients--;
    }
}


