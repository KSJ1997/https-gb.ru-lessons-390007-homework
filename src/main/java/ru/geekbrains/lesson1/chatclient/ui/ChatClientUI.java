package ru.geekbrains.lesson1.chatclient.ui;

public interface ChatClientUI {
    void initializeUI();
    void connectToServer();
    void sendMessage();
    void appendMessage(String message);
    void loadChatHistory();
    void saveChatHistory(String message);
}
