package com.ronial.app;

import com.ronial.app.context.ContextProvider;
import com.ronial.app.mail.Client;

import com.ronial.app.mail.CommandType;
import com.ronial.app.mail.MailService;
import com.ronial.app.mail.MailServiceImpl;
import com.ronial.app.views.MailLaunch;
import com.ronial.app.views.SignInView;


import java.io.IOException;


public class MailClientApplication {
    public static void main(String[] args) throws IOException {
        Client client = new Client("localhost", 2000);
        MailService mailService = new MailServiceImpl(client);
        ContextProvider.register(MailService.class, mailService);
        MailLaunch.launch();
    }
}