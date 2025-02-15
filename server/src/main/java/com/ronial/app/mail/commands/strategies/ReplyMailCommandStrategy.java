package com.ronial.app.mail.commands.strategies;

import com.ronial.app.context.ContextProvider;
import com.ronial.app.exceptions.RepositoryException;
import com.ronial.app.mail.Server;
import com.ronial.app.mail.commands.Command;
import com.ronial.app.mail.service.MailService;
import com.ronial.app.models.Email;
import com.ronial.app.models.Request;
import com.ronial.app.models.Response;
import com.ronial.app.utils.DateUtils;
import com.ronial.app.views.LogFrame;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class ReplyMailCommandStrategy implements Command {
    private final MailService mailService;

    public ReplyMailCommandStrategy() {
        mailService = ContextProvider.get(MailService.class);
    }
    private void log(Object log) {
        ContextProvider.<LogFrame>get(LogFrame.class)
                .addLog(ReplyMailCommandStrategy.class, log);
    }
    @Override
    public void execute(Server server, Request request) throws IOException {
        Instant time = Instant.now();
        log(request.toHostPortString()+" - Reply mail started!");
        String emailJson = request.getData().getString("email");
        Email email = Email.fromJSON(emailJson);
        logReplyMail(request.toHostPortString(), email);

        List<String> links = !email.getLinks().trim().isEmpty() ?
                Arrays.stream(email.getLinks().split(";")).toList() : List.of();
        String contentHtml = toContentHtml(email.getFrom(), DateUtils.formatInstant(time),email.getBody(),links);
        email.setContentHtml(contentHtml);
        Response response = new Response(true);
        try {
            mailService.replyEmail(email);
            response.setMessage("Trả lời mail thành công 😊");
            log(request.toHostPortString() + " - Reply mail completed!");
        } catch (RepositoryException e) {
            response.setSuccess(false)
                    .setMessage(e.getMessage());
            log(request.toHostPortString() + " - Reply mail error: " + e.getMessage());
        } finally {
            server.sendResponse(response, request.getPacket());
        }
    }
    private void logReplyMail(String hostPort,Email email){
        log(hostPort + "#REPLY FROM: " + email.getFrom());
        log(hostPort + "#TO MAIL: " + email.getId());
        log(hostPort + "#BODY: " + email.getBody());
        log(hostPort + "#LINKS: " + email.getLinks());
    }
    private String toContentHtml(String from,
                                 String time,
                                 String body,
                                 List<String> links) {
        StringBuilder html = new StringBuilder();
        html.append("<hr>")
                .append("<h2 style='color: #00897B;'>✉️ Trả lời từ: <span id='emailFrom'>").append(from).append("</span></h2>")
                .append("<p style='font-size: 12px; color: gray; margin-top: -10px; margin-bottom: 15px;'>🕒 Gửi lúc: <span id='emailTime'>")
                .append(time).append("</span></p>")
                .append("<p><span style='color: #00897B; font-weight: bold;'>Nội dung:</span></p>")
                .append("<p style='font-size: 14px; color: #333;'>").append(body).append("</p>");

        if (links != null && !links.isEmpty()) {
            html.append("<p><span style='color: #00897B; font-weight: bold;'>Danh sách link liên kết:</span></p>")
                    .append("<ul style='list-style-type: decimal;'>");
            for (String link : links) {
                html.append("<li><a href='").append(link).append("'>").append(link).append("</a></li>");
            }
            html.append("</ul>");
        }

        html.append("<p style='color: gray; font-size: 12px; margin-top: 20px;'>📌Ký tên: ").append(from).append("</p>");
        return html.toString();
    }
}
