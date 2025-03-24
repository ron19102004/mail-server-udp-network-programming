package com.ronial.app.mail.commands.strategies;

import com.ronial.app.context.ContextProvider;
import com.ronial.app.exceptions.ServiceException;
import com.ronial.app.mail.Server;
import com.ronial.app.mail.commands.Command;
import com.ronial.app.mail.service.MailService;
import com.ronial.app.models.Request;
import com.ronial.app.views.LogFrame;

import java.io.IOException;
import java.sql.SQLException;

public class ReadMailCommandStrategy implements Command {
    private final MailService mailService;

    public ReadMailCommandStrategy() {
        mailService = ContextProvider.get(MailService.class);
    }

    private void log(Object log) {
        ContextProvider
                .<LogFrame>get(LogFrame.class)
                .addLog(ReadMailCommandStrategy.class, log);
    }

    @Override
    public void execute(Server server, Request request) throws IOException {
        String email = request.getData().getString("email");
        int mailId = request.getData().getInt("id");
        log(request.toHostPortString() + " - " + email + " read mail " + mailId);
        try {
            mailService.readMail(mailId);
            log(request.toHostPortString() + " - " + email + " read mail " + mailId + " successfully");
        } catch (ServiceException | SQLException e) {
            log(request.toHostPortString() + " - Read mail error: " + e.getMessage());
        }
    }
}
