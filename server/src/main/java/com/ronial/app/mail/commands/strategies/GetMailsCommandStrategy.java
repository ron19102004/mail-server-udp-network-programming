package com.ronial.app.mail.commands.strategies;

import com.ronial.app.context.ContextProvider;
import com.ronial.app.exceptions.RepositoryException;
import com.ronial.app.mail.Server;
import com.ronial.app.mail.commands.Command;
import com.ronial.app.mail.service.MailService;
import com.ronial.app.models.Email;
import com.ronial.app.models.Request;
import com.ronial.app.models.Response;
import com.ronial.app.views.LogFrame;
import org.json.JSONArray;

import java.io.IOException;
import java.util.List;

public class GetMailsCommandStrategy implements Command {
    private final MailService mailService;

    public GetMailsCommandStrategy() {
        mailService = ContextProvider.get(MailService.class);
    }

    @Override
    public void execute(Server server, Request request) throws IOException {
        String email = request.getData().getString("email");
        log(request.toHostPortString() + ":" + email + " - Get mails started!");

        Response response = new Response(true);
        try {
            List<Email> emails = mailService.getEmails(email);
            JSONArray emailsJson = new JSONArray(emails);
            response.getJSON().put("emails", emailsJson.toString());
            response.setMessage("Lấy dữ liệu thành công😊");
            log(request.toHostPortString() + " - Get mails finished!");
        } catch (RepositoryException e) {
            response.setSuccess(false)
                    .setMessage(e.getMessage());
            log(request.toHostPortString() + " - Get mails error: " + e.getMessage());
        } finally {
            server.sendResponse(response, request.getPacket());
        }
    }

    private void log(Object log) {
        ContextProvider.<LogFrame>get(LogFrame.class)
                .addLog(GetMailsCommandStrategy.class, log);
    }
}
