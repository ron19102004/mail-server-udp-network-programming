package com.ronial.app.views;

import com.ronial.app.context.ContextProvider;
import com.ronial.app.mail.MailService;
import com.ronial.app.views.components.ButtonComponent;
import com.ronial.app.views.components.InputComponent;
import com.ronial.app.views.components.PasswordInputComponent;
import com.ronial.app.views.utils.Toast;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class SignInView extends JFrame {
    public JTextField nameField;
    public JTextField emailField;
    public JPasswordField passwordField;
    private MailService mailService;

    public SignInView() {
        setIconImage(new ImageIcon(Objects.requireNonNull(getClass().getResource("/assets/Email-icon.png"))).getImage());
        this.mailService = ContextProvider.get(MailService.class);
        setTitle("Đăng nhập Ya-mail");
        setSize(420, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("ĐĂNG NHẬP YA-MAIL", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 102, 204));
        titleLabel.setBounds(100, 50, 220, 30);
        panel.add(titleLabel);

        Font inputFont = new Font("Arial", Font.PLAIN, 14);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(inputFont);
        emailLabel.setBounds(50, 120, 100, 25);
        panel.add(emailLabel);

        emailField = InputComponent.createTextField(150, 120);
        panel.add(emailField);

        JLabel passwordLabel = new JLabel("Mật khẩu:");
        passwordLabel.setFont(inputFont);
        passwordLabel.setBounds(50, 170, 100, 25);
        panel.add(passwordLabel);

        passwordField = PasswordInputComponent.createPasswordField(150, 170);
        panel.add(passwordField);

        JButton registerButton = ButtonComponent.createButton("Đăng nhập");
        panel.add(registerButton);

        registerButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.error("Vui lòng nhập đầy đủ thông tin!");
            } else {
                try {
                    mailService.signInAccount(this);
                } catch (IOException ex) {
                    Toast.error(ex.getMessage());
                    throw new RuntimeException(ex);
                }
            }
        });

        add(panel);
    }
    public static void launch() {
        new SignInView().setVisible(true);
    }
}