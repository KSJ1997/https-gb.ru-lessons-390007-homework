package ru.geekbrains.lesson1.chatserver.handlers;

import java.io.IOException;

public interface ClientHandler extends Runnable {
    void sendMessage(String message);
    void closeClient() throws IOException;
}
