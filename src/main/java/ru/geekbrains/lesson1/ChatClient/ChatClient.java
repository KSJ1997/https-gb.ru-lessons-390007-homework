package ru.geekbrains.lesson1.ChatClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

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
        JTextField serverField = new JTextField();
        JTextField usernameField = new JTextField();
        Object[] message = {
                "Server address:", serverField,
                "Username:", usernameField
        };
        int option = JOptionPane.showConfirmDialog(null, message, "Enter connection details", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            serverAddress = serverField.getText();
            username = usernameField.getText();

            frame = new JFrame("Chat Client: " + username);
            frame.setSize(500, 400);
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

            inputPanel.setLayout(new BorderLayout());
            inputPanel.add(messageField, BorderLayout.CENTER);
            inputPanel.add(sendButton, BorderLayout.EAST);

            chatArea = new JTextArea();
            chatArea.setEditable(false);

            frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
            frame.add(inputPanel, BorderLayout.SOUTH);
            frame.setVisible(true);

            connectToServer();
        } else {
            System.exit(0);
        }
    }

    private void connectToServer() {
        try {
            socket = new Socket(serverAddress, 12345);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            
            writer.write(username);
            writer.newLine();
            writer.flush();

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

            loadChatHistory();
        } catch (IOException e) {
            e.printStackTrace();
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
