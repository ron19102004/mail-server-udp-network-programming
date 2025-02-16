package com.ronial.app;

import com.ronial.app.conf.ConfigManagement;
import com.ronial.app.conf.ConfigManagementImpl;
import com.ronial.app.context.ContextProvider;
import com.ronial.app.mail.Server;
import com.ronial.app.mail.service.MailService;
import com.ronial.app.mail.service.MailServiceImpl;
import com.ronial.app.security.RSASecurity;
import com.ronial.app.views.LogFrame;

import java.io.File;
import java.io.IOException;

public class MailServerApplication {
    public static final String MAIL_FOLDER_NAME = "mails";
    public static final String MAIL_LOG_FOLDER_NAME = "logs";
    static {
        File mails = new File(MAIL_FOLDER_NAME);
        File logs = new File(MAIL_LOG_FOLDER_NAME);
        if (!mails.exists()) {
            mails.mkdirs();
        }
        if (!logs.exists()) {
            logs.mkdirs();
        }
    }
    public static void main(String[] args) throws IOException {
        ConfigManagement configManagement = new ConfigManagementImpl();
        ContextProvider.register(ConfigManagement.class, configManagement);

        LogFrame logFrame = new LogFrame();
        ContextProvider.register(LogFrame.class,logFrame);

        RSASecurity security = new RSASecurity();
        ContextProvider.register(RSASecurity.class, security);

        MailService mailService = new MailServiceImpl();
        ContextProvider.register(MailService.class, mailService);

        Server server = Server.launch();
        ContextProvider.register(Server.class,server);

    }
}