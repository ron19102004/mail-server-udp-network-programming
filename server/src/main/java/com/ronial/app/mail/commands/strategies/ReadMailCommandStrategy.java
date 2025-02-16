package com.ronial.app.mail.commands.strategies;

import com.ronial.app.context.ContextProvider;
import com.ronial.app.exceptions.RepositoryException;
import com.ronial.app.mail.Server;
import com.ronial.app.mail.commands.Command;
import com.ronial.app.mail.service.MailService;
import com.ronial.app.models.Request;
import com.ronial.app.views.LogFrame;

import java.io.IOException;

public class ReadMailCommandStrategy implements Command {
    private final MailService mailService;

    public ReadMailCommandStrategy() {
        mailService = ContextProvider.get(MailService.class);
    }
    private void log(Object log) {
        ContextProvider.<LogFrame>get(LogFrame.class)
                .addLog(ReadMailCommandStrategy.class, log);
    }
    @Override
    public void execute(Server server, Request request) throws IOException {
        String email = request.getData().getString("email");
        long mailId = request.getData().getLong("id");
        log(request.toHostPortString() + " - " + email + " read mail " + mailId);
        try {
            mailService.readMail(email,mailId);
            log(request.toHostPortString() + " - " + email + " read mail " + mailId + " successfully");
        } catch (RepositoryException e) {
            log(request.toHostPortString() + " - Reply mail error: " + e.getMessage());
        }
    }
}
