package cpen221.mp3;

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
        String reply = reader.readLine();
        System.out.println("Reply received: "+reply);

        if(reply == null) {
            throw new IOException("Connection terminated unexpectedly");
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
}
