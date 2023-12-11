package ru.geekbrains.lesson1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

    private static final int SERVER_PORT = 12345; // Порт сервера

    private List<ConnectionHandler> clients = new ArrayList<>();

    public ChatServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Server is running on port " + SERVER_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ConnectionHandler handler = new ConnectionHandler(clientSocket);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ConnectionHandler implements Runnable {

        private Socket clientSocket;
        private BufferedReader clientReader;
        private BufferedWriter clientWriter;

        public ConnectionHandler(Socket socket) {
            this.clientSocket = socket;
            try {
                clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                clientWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String message = clientReader.readLine();
                    if (message == null) {
                        break;
                    }
                    broadcastMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                clients.remove(this);
                closeConnection();
            }
        }

        private void broadcastMessage(String message) {
            for (ConnectionHandler handler : clients) {
                try {
                    handler.clientWriter.write(message + "\n");
                    handler.clientWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void closeConnection() {
            try {
                clientReader.close();
                clientWriter.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new ChatServer();
    }
}
