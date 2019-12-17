package cpen221.mp3.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.Socket;
import java.nio.Buffer;

public class WikiClient {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public WikiClient(String hostname, int port) throws IOException {
        socket = new Socket(hostname, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void sendRequest(String s) {
        writer.print("Request string: " + s);
        writer.flush();
    }

    public JsonObject getReply() throws IOException {
        String reply = reader.readLine();
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
        try{
            WikiClient bill = new WikiClient("localhost", 4949);

            bill.sendRequest("{id: \"localhost\", type: \"simpleSearch\", query: \"Barack Obama\", limit: \"12\"}");

            JsonObject jObj = bill.getReply();
            System.out.println(jObj.toString());
            bill.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

}
