package ru.geekbrains.lesson1.chatserver.handlers;

import ru.geekbrains.lesson1.chatserver.ChatServer;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;

    public ClientHandler(Socket socket, String username) {
        this.socket = socket;
        this.username = username;

        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            ChatServer.updateStatus(username + " joined the chat!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                String fullMessage = username + ": " + message;
                ChatServer.updateStatus(fullMessage);
                ChatServer.broadcastMessage(fullMessage, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeClient();
        }
    }

    public void sendMessage(String message) {
        writer.println(message);
    }

    private void closeClient() {
        try {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
                ChatServer.removeClient(this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
