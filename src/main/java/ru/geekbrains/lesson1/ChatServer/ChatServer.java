package ru.geekbrains.lesson1.ChatServer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class ChatServer {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients;
    private JFrame frame;
    private JTextArea statusTextArea;

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.initializeUI();
        chatServer.start();
    }

    public void initializeUI() {
        frame = new JFrame("Chat Server");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        statusTextArea = new JTextArea();
        statusTextArea.setEditable(false);

        JButton startButton = new JButton("Start Server");
        JButton stopButton = new JButton("Stop Server");

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                start();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeServer();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(statusTextArea), BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    public void start() {
        clients = new ArrayList<>();

        try {
            serverSocket = new ServerSocket(12345);
            updateStatus("Server started!");

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(this, socket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
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
                updateStatus("Server stopped!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateStatus(String message) {
        statusTextArea.append(message + "\n");
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
                writer = new PrintWriter(socket.getOutputStream(), true);
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
                    chatServer.removeClient(this);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
