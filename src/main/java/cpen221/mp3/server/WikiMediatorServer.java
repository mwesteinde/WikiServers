package cpen221.mp3.server;

/**
 * A server which allows a client to use WikiMediator and can handle a specified number of
 * requests simultaneously.
 */

public class WikiMediatorServer {

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

        /* TODO: Implement this method */

    }

}
