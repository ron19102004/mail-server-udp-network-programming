package com.ronial.app.views;

import com.ronial.app.context.ContextProvider;
import com.ronial.app.mail.MailService;
import com.ronial.app.models.Email;
import com.ronial.app.views.components.ButtonComponent;
import com.ronial.app.views.components.InputComponent;
import com.ronial.app.views.utils.Toast;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class ReplyMailView extends JFrame {
    public JTextField attachLink;
    public JTextArea messageArea;
    public Email email;
    public MailView mailView;
    public ReplyMailView(Email email, MailView mailView) {
        setIconImage(new ImageIcon(Objects.requireNonNull(getClass().getResource("/assets/Email-icon.png"))).getImage());
        this.mailView = mailView;
        this.email = email;
        MailService mailService = ContextProvider.get(MailService.class);
        setTitle("Reply mail - " + email.getSubject());
        setSize(800, 600);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel composePanel = new JPanel(new GridLayout(2, 1, 5, 5));
        composePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Font font = new Font("Arial", Font.PLAIN, 19);

        attachLink = InputComponent.createTextField();
        attachLink.setFont(font);

        messageArea = new JTextArea();
        messageArea.setFont(new Font("Arial", Font.PLAIN, 20));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);

        composePanel.add(new JLabel("Link đính kèm(Sử dụng dấu ; để tách link):"));
        composePanel.add(attachLink);

        add(composePanel, BorderLayout.NORTH);
        JScrollPane messageScrollPane = new JScrollPane(messageArea);
        add(messageScrollPane, BorderLayout.CENTER);

        JButton sendButton = new JButton("Gửi");
        ButtonComponent.styleButton(sendButton);
        sendButton.addActionListener(e -> {
            try {
                mailService.replyMail(this);
            } catch (IOException ex) {
                Toast.error(ex.getMessage());
            }
        });

        add(sendButton, BorderLayout.SOUTH);
        setVisible(true);
    }
}