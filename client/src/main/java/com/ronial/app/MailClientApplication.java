package com.ronial.app;

import com.ronial.app.conf.ConfigManagement;
import com.ronial.app.conf.ConfigManagementImpl;
import com.ronial.app.context.ContextProvider;
import com.ronial.app.mail.Client;


import com.ronial.app.mail.MailService;
import com.ronial.app.mail.MailServiceImpl;
import com.ronial.app.views.MailLaunch;


import java.io.IOException;


public class MailClientApplication {
    public static void main(String[] args) throws IOException {
        ConfigManagement configManagement = new ConfigManagementImpl();
        ContextProvider.register(ConfigManagement.class, configManagement);

        final String HOST = (String) configManagement.value("server.host").get();
        final int PORT =(int) configManagement.value("server.port").get();

        Client client = new Client(HOST, PORT);
        MailService mailService = new MailServiceImpl(client);
        ContextProvider.register(MailService.class, mailService);
        MailLaunch.launch();
    }
}