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

import java.io.IOException;
import java.util.List;

public class TransferMailCommandStrategy implements Command {
    private final MailService mailService;

    public TransferMailCommandStrategy() {
        mailService = ContextProvider.get(MailService.class);
    }

    private void log(Object log) {
        ContextProvider.<LogFrame>get(LogFrame.class)
                .addLog(TransferMailCommandStrategy.class, log);
    }

    @Override
    public void execute(Server server, Request request) throws IOException {
        log(request.toHostPortString() + " - Transfer mail started!");

        String emailJson = request.getData().getString("email");
        Email email = Email.fromJSON(emailJson);
        String[] emails = request.getData().getString("emails").split(",");
        email.setContentHtml(toContentHtml(request.getData().getString("transferFrom"), email));
        Response response = new Response(true);
        try {
            mailService.transferMail(emails, email);
            response.setMessage("Chuyển tiếp mail thành công 😊");
            log(request.toHostPortString() + " - Transfer mail completed!");
        } catch (RepositoryException e) {
            response.setSuccess(false)
                    .setMessage(e.getMessage());
            log(request.toHostPortString() + " - Transfer mail error: " + e.getMessage());
        } finally {
            server.sendResponse(response, request.getPacket());
        }
    }

    private String toContentHtml(String transferFrom, Email email) {
        StringBuilder html = new StringBuilder();
        html.append("<h2 style='color: #00897B;'>✉️ Chuyển tiếp từ: <span id='emailFrom'>")
                .append(transferFrom).append("</span></h2>")
                .append("<hr>").append(email.getContentHtml());
        return html.toString();
    }
}
