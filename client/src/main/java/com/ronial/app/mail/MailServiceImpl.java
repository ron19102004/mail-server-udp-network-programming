package com.ronial.app.mail;

import com.ronial.app.models.Email;
import com.ronial.app.models.Request;
import com.ronial.app.models.Response;
import com.ronial.app.models.User;
import com.ronial.app.views.*;
import com.ronial.app.views.utils.Toast;
import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MailServiceImpl implements MailService {
    private Client client;

    public MailServiceImpl(Client client) {
        this.client = client;
    }

    @Override
    public void registerAccount(RegisterView view) throws IOException {
        Request request = new Request(CommandType.REGISTER_ACCOUNT);
        request.getData()
                .put("name", view.nameField.getText().trim())
                .put("email", view.emailField.getText().trim())
                .put("password", new String(view.passwordField.getPassword()).trim());

        client.sendRequest(request);

        Response response = client.receiveResponse();
        if (response.isSuccess()) {
            Toast.info(response.getMessage());
            view.dispose();
            SignInView.launch();
        } else {
            Toast.error(response.getMessage());
        }
    }

    @Override
    public void signInAccount(SignInView view) throws IOException {
        Request request = new Request(CommandType.LOGIN_ACCOUNT);
        request.getData()
                .put("email", view.emailField.getText().trim())
                .put("password", new String(view.passwordField.getPassword()).trim());
        client.sendRequest(request);

        Response response = client.receiveResponse();
        if (response.isSuccess()) {
            Toast.info(response.getMessage());
            view.dispose();
            String userJson = response.getData().getString("user");
            User user = User.fromJson(userJson);
            MailView.launch(user);
        } else {
            Toast.error(response.getMessage());
        }
    }

    @Override
    public void sendMail(CreateMailView view) throws IOException {
        Request request = new Request(CommandType.SEND_MAIL);
        Email email = new Email();
        email.setTo(view.recipientField.getText().trim());
        email.setFrom(view.mailView.getUser().getEmail());
        email.setSubject(view.subjectField.getText().trim());
        email.setBody(view.messageArea.getText().trim());
        email.setLinks(view.attachLink.getText().trim());
        email.setIsSeen(false);

        System.out.println(email.toJSON());
        request.getData()
                .put("email", email.toJSON());
        client.sendRequest(request);
        Response response = client.receiveResponse();
        if (response.isSuccess()) {
            Toast.info(response.getMessage());
            view.mailView.getEmailContent().setText("");
            view.mailView.refreshInbox();
            view.dispose();
        } else {
            Toast.error(response.getMessage());
        }
    }

    @Override
    public void loadMails(MailView view) throws IOException {
        Request request = new Request(CommandType.GET_MAILS);
        request.getData().put("email", view.getUser().getEmail());
        client.sendRequest(request);

        Response response = client.receiveResponse();
        if (response.isSuccess()) {
            List<Email> emails = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(response.getData().getString("emails"));
            view.getEmailListModel().clear();
            jsonArray.forEach(o -> {
                Email email = Email.fromJSON(o.toString());
                emails.add(email);
                view.getEmailListModel().addElement("☺️" + (email.isSeen() ? "" : "(Chưa đọc)") + email.getSubject());
            });
            view.setEmails(emails);
        } else {
            Toast.error(response.getMessage());
        }
    }

    @Override
    public void deleteMail(MailView view) throws IOException {
        Request request = new Request(CommandType.DELETE_MAIL);
        int selectedEmailIndex = view.getEmailList().getSelectedIndex();
        Email email = view.getEmails().get(selectedEmailIndex);
        request.getData()
                .put("email", view.getUser().getEmail())
                .put("id", email.getId());
        client.sendRequest(request);
        Response response = client.receiveResponse();
        if (response.isSuccess()) {
            view.refreshInbox();
            view.getEmailContent().setText("");
            Toast.info(response.getMessage());
        } else {
            Toast.error(response.getMessage());
        }
    }

    @Override
    public void replyMail(ReplyMailView view) throws IOException {
        Request request = new Request(CommandType.REPLY_MAIL);
        Email email = new Email();
        email.setBody(view.messageArea.getText().trim());
        email.setLinks(view.attachLink.getText().trim());
        email.setId(view.email.getId());
        email.setFrom(view.mailView.getUser().getEmail());

        request.getData()
                .put("email", email.toJSON());
        client.sendRequest(request);
        Response response = client.receiveResponse();
        if (response.isSuccess()) {
            Toast.info(response.getMessage());
            view.mailView.refreshInbox();
            view.dispose();
        } else {
            Toast.error(response.getMessage());
        }
    }

    @Override
    public void transferMail(TransferMailView view) throws IOException {
        Request request = new Request(CommandType.TRANSFER_MAIL);
        request.getData()
                .put("email", view.email.toJSON())
                .put("emails", view.emailsField.getText().trim())
                .put("transferFrom", view.mailView.getUser().getEmail());
        client.sendRequest(request);
        Response response = client.receiveResponse();
        if (response.isSuccess()) {
            Toast.info(response.getMessage());
            view.dispose();
        } else {
            Toast.error(response.getMessage());
        }
    }

    @Override
    public void readMail(MailView view, Email email) throws IOException {
        Request request = new Request(CommandType.READ_MAIL);
        request.getData()
                .put("email", view.getUser().getEmail())
                .put("id", email.getId());
        client.sendRequest(request);
    }
}
