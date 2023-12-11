package ru.geekbrains.lesson1.ChatClient;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.Socket;
import javax.swing.*;

public class ChatClient {
    private JFrame frame;
    private JTextField messageField;
    private JTextArea chatArea;
    private JButton sendButton;
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String username;
    private String serverAddress;
    private static final String HISTORY_FILE_PATH = "chat_history.txt";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatClient().initializeUI();
            }
        });
    }

    public void initializeUI() {
        frame = new JFrame("Chat Client");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        messageField = new JTextField();
        sendButton = new JButton("Send");

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        messageField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        inputPanel.setLayout(new BorderLayout());
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        chatArea = new JTextArea();
        chatArea.setEditable(false);

        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);

        // Добавляем поля ввода адреса сервера и порта
        JPanel connectionPanel = new JPanel();
        JTextField addressField = new JTextField();
        JTextField portField = new JTextField();
        JLabel addressLabel = new JLabel("Server Address:");
        JLabel portLabel = new JLabel("Port:");

        connectionPanel.setLayout(new GridLayout(2, 2));
        connectionPanel.add(addressLabel);
        connectionPanel.add(addressField);
        connectionPanel.add(portLabel);
        connectionPanel.add(portField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                serverAddress = addressField.getText();
                connectToServer();
            }
        });

        frame.add(connectionPanel, BorderLayout.NORTH);
        frame.add(loginButton, BorderLayout.NORTH);

        frame.setVisible(true);
    }

    private void connectToServer() {
        try {
            socket = new Socket(serverAddress, 12345);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            appendMessage(line);
                            saveChatHistory(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
            appendMessage("Connection failed!");
        }
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            try {
                writer.write(username + ": " + message);
                writer.newLine();
                writer.flush();

                appendMessage(username + ": " + message);
                saveChatHistory(username + ": " + message);

                messageField.setText("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void appendMessage(String message) {
        chatArea.append(message + "\n");
    }

    private void loadChatHistory() {
        try (BufferedReader historyReader = new BufferedReader(new FileReader(HISTORY_FILE_PATH))) {
            String line;
            while ((line = historyReader.readLine()) != null) {
                appendMessage(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveChatHistory(String message) {
        try (BufferedWriter historyWriter = new BufferedWriter(new FileWriter(HISTORY_FILE_PATH, true))) {
            historyWriter.write(message);
            historyWriter.newLine();
            historyWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
