package ru.geekbrains.lesson1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients;

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.start();
    }

    public void start() {
        clients = new ArrayList<>();

        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            this.serverSocket = serverSocket;

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(this, socket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeServer();
        }
    }

    public void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    private void closeServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private ChatServer chatServer;

        public ClientHandler(ChatServer chatServer, Socket socket) {
            this.chatServer = chatServer;
            this.socket = socket;

            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                String message;
                while ((message = reader.readLine()) != null) {
                    chatServer.broadcastMessage(message, this);
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
                }
                chatServer.removeClient(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
