package com.ronial.app.views;
import com.ronial.app.context.Context;
import com.ronial.app.mail.Server;
import com.ronial.app.views.components.ButtonComponent;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class LogFrame extends JFrame implements Context {
    private JTextArea logArea;
    private JButton stopButton;
    private JScrollPane scrollPane;

    public LogFrame() {
        setIconImage(new ImageIcon(Objects.requireNonNull(getClass().getResource("/assets/Email-icon.png"))).getImage());
        setTitle("Server Control Panel");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Arial", Font.PLAIN, 18));

        scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        stopButton = new JButton("Stop Server");
        ButtonComponent.styleButton(stopButton);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(stopButton);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    public JButton getStopButton() {
        return stopButton;
    }

    public JTextArea getLogArea() {
        return logArea;
    }

    public void addLog(Class<?> clazz, Object log) {
        logArea.append(clazz.getSimpleName() + " ==> " + log.toString() + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength()); // Tự động cuộn xuống cuối
    }
}
