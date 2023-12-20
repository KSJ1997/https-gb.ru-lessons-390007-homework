package ru.geekbrains.lesson1.chatclient;

import ru.geekbrains.lesson1.chatclient.ui.ChatClientUIImpl;

public class ChatClient {
    public static void main(String[] args) {
        ChatClientUIImpl chatClientUI = new ChatClientUIImpl();
        chatClientUI.initializeUI();
    }
}
