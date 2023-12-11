package ru.geekbrains.lesson1;

import java.net.Socket;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;

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

    public ChatClient(String username, String serverAddress) {
        this.username = username;
        this.serverAddress = serverAddress;

        initializeUI();
        connectToServer();
        loadChatHistory();
    }

    private void initializeUI() {
        frame = new JFrame("Chat Client");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        messageField = new JTextField();
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

        chatArea = new JTextArea();
        chatArea.setEditable(false);

        sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        frame.add(messageField, BorderLayout.NORTH);
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        frame.add(sendButton, BorderLayout.SOUTH);

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

    public static void main(String[] args) {
        String username = JOptionPane.showInputDialog("Enter your username:");
        String serverAddress = JOptionPane.showInputDialog("Enter server address:");

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatClient(username, serverAddress);
            }
        });
    }
}
