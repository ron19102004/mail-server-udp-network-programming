package com.ronial.app;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.ronial.app.context.ContextProvider;
import com.ronial.app.mail.Client;


import com.ronial.app.mail.MailService;
import com.ronial.app.mail.MailServiceImpl;
import com.ronial.app.views.MailLaunch;
import com.ronial.app.views.SwingBrowser;


import javax.swing.*;
import java.io.IOException;


public class MailClientApplication {
    static {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf()); // Hoặc FlatDarkLaf cho chế độ tối
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws IOException {
        SwingUtilities.invokeLater(() -> {
            SwingBrowser swingBrowser = SwingBrowser.launch();
            ContextProvider.register(SwingBrowser.class, swingBrowser);
        });
        Client client = new Client("localhost", 3000);
        MailService mailService = new MailServiceImpl(client);
        ContextProvider.register(MailService.class, mailService);
        MailLaunch.launch();
    }
}