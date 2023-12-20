package ru.geekbrains.lesson1.chatserver;

import ru.geekbrains.lesson1.chatserver.handlers.ClientHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
    private static ServerSocket serverSocket;
    private static List<ClientHandler> clients;
    private static JFrame frame;
    private static JTextArea statusTextArea;

    public static void main(String[] args) {
        initializeUI();
        start();
    }

    public static void initializeUI() {
        frame = new JFrame("Chat Server");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        statusTextArea = new JTextArea();
        statusTextArea.setEditable(false);

        JButton startButton = new JButton("Start");
        JButton stopButton = new JButton("Stop");

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

    public static void start() {
        clients = new ArrayList<>();

        try {
            serverSocket = new ServerSocket(12345);
            updateStatus("Server started!");

            while (true) {
                Socket socket = serverSocket.accept();
                String username = new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine();
                ClientHandler clientHandler = new ClientHandlerImpl(socket, username);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public static void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public static void updateStatus(String message) {
        statusTextArea.append(message + "\n");
    }

    private static void closeServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                updateStatus("Server stopped!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
