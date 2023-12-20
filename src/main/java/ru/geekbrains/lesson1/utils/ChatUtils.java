package ru.geekbrains.lesson1.utils;

import javax.swing.*;

public class ChatUtils {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null, "This is a utility class, and it should not be executed directly.");
            }
        });
    }
}
