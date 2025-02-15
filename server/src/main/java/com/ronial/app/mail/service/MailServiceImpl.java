package com.ronial.app.mail.service;

import com.ronial.app.MailServerApplication;
import com.ronial.app.context.ContextProvider;
import com.ronial.app.exceptions.RepositoryException;
import com.ronial.app.models.Email;
import com.ronial.app.models.User;
import com.ronial.app.security.RSASecurity;
import com.ronial.app.views.LogFrame;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MailServiceImpl implements MailService {
    private final String MAIL_FOLDER_NAME;
    private final RSASecurity security;

    public MailServiceImpl() {
        MAIL_FOLDER_NAME = MailServerApplication.MAIL_FOLDER_NAME;
        security = ContextProvider.get(RSASecurity.class);
    }

    @Override
    public void createMailAccount(String email, String password, String name) throws RepositoryException {
        final String folder = MAIL_FOLDER_NAME + "/" + email;
        final String accountPath = folder + "/account.txt";
        File file = new File(folder);
        if (file.exists()) {
            throw new RepositoryException(email + " already exists");
        }
        if (!file.mkdirs()) {
            throw new RepositoryException(email + " could not be created");
        }
        File inbox = new File(folder + "/inbox");
        if (!inbox.exists()) {
            inbox.mkdirs();
        }
        try {
            User user = new User(name, email, password);
            String passwordHash = security.encode(password);
            user.setPassword(passwordHash);
            byte[] dataBytes = user.toJson().getBytes(StandardCharsets.UTF_8);
            FileOutputStream out = new FileOutputStream(accountPath);
            out.write(dataBytes);
            out.close();
        } catch (NoSuchPaddingException | NoSuchAlgorithmException |
                 IllegalBlockSizeException | BadPaddingException |
                 InvalidKeyException | IOException e) {
            ContextProvider.<LogFrame>get(LogFrame.class)
                    .addLog(MailServiceImpl.class, e.getMessage());
            throw new RepositoryException("System error");
        }
    }

    @Override
    public User loginMailAccount(String email, String password) throws RepositoryException {
        final String folder = MAIL_FOLDER_NAME + "/" + email;
        final String accountPath = folder + "/account.txt";
        File file = new File(folder);
        if (!file.exists()) {
            throw new RepositoryException(email + " does not exist");
        }
        try {
            FileInputStream in = new FileInputStream(accountPath);
            byte[] buffer = in.readAllBytes();
            in.close();
            String data = new String(buffer, StandardCharsets.UTF_8);
            User user = User.fromJson(data);
            String passwordDecode = security.decode(user.getPassword());
            if (!password.equals(passwordDecode)) {
                throw new RepositoryException("Wrong password");
            }
            return user;
        } catch (IOException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                 BadPaddingException | InvalidKeyException e) {
            ContextProvider.<LogFrame>get(LogFrame.class)
                    .addLog(MailServiceImpl.class, e.getMessage());
            throw new RepositoryException("System error");
        }
    }

    @Override
    public void saveEmail(Email email) throws RepositoryException {
        final String receiverFolder = MAIL_FOLDER_NAME + "/" + email.getTo();
        final String senderFolder = MAIL_FOLDER_NAME + "/" + email.getFrom();
        File file = new File(receiverFolder);
        if (!file.exists()) {
            throw new RepositoryException(email.getTo() + " does not exist");
        }
        final String receiverBoxFilePath = receiverFolder + "/inbox/" + email.getId() + ".txt";
        final String senderBoxFilePath = senderFolder + "/inbox/" + email.getId() + ".txt";
        try {
            byte[] buffer = email
                    .toJSON()
                    .getBytes(StandardCharsets.UTF_8);

            FileOutputStream receiverOut = new FileOutputStream(receiverBoxFilePath);
            receiverOut.write(buffer);
            receiverOut.close();

            FileOutputStream senderOut = new FileOutputStream(senderBoxFilePath);
            senderOut.write(buffer);
            senderOut.close();
        } catch (IOException e) {
            ContextProvider.<LogFrame>get(LogFrame.class)
                    .addLog(MailServiceImpl.class, e.getMessage());
            throw new RepositoryException("System error");
        }
    }

    @Override
    public Email getEmail(String email, long id) throws RepositoryException {
        final String filePath = MAIL_FOLDER_NAME + "/" + email + "/inbox/" + id + ".txt";
        File file = new File(filePath);
        if (!file.exists()) {
            throw new RepositoryException("Email: " + id + " does not exist");
        }
        try {
            FileInputStream in = new FileInputStream(filePath);
            byte[] buffer = in.readAllBytes();
            in.close();
            String data = new String(buffer, StandardCharsets.UTF_8);
            return Email.fromJSON(data);
        } catch (IOException e) {
            ContextProvider.<LogFrame>get(LogFrame.class)
                    .addLog(MailServiceImpl.class, e.getMessage());
            throw new RepositoryException("System error");
        }
    }

    @Override
    public List<Email> getEmails(String email) throws RepositoryException {
        List<Email> emails = new ArrayList<>();
        final String boxPath = MAIL_FOLDER_NAME + "/" + email + "/inbox";
        File folder = new File(boxPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (files != null && files.length > 0) {
            for (File file : files) {
                try {
                    String content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
                    Email emailDb = Email.fromJSON(content);
                    emails.add(emailDb);
                } catch (IOException e) {
                    ContextProvider.<LogFrame>get(LogFrame.class)
                            .addLog(MailServiceImpl.class, "Lỗi khi đọc file " + file.getName() + ": " + e.getMessage());
                }
            }
        }
        emails.sort((mail1, mail2) -> {
            if (mail1.getId() > mail2.getId()) {
                return -1;
            }
            if (mail2.getId() > mail1.getId()) {
                return 1;
            }
            return 0;
        });
        return emails;
    }

    @Override
    public void deleteEmail(String email, long id) throws RepositoryException {
        final String boxPath = MAIL_FOLDER_NAME + "/" + email + "/inbox/" + id + ".txt";
        File file = new File(boxPath);
        if (!file.exists()) {
            throw new RepositoryException("Email: " + id + " does not exist");
        }
        if (!file.delete()) {
            throw new RepositoryException("System error");
        }
    }

    @Override
    public void replyEmail(Email email) throws RepositoryException {
        Email emailDb = getEmail(email.getFrom(), email.getId());
        String contentHtml = emailDb.getContentHtml() + email.getContentHtml();
        emailDb.setContentHtml(contentHtml);

        try {
            String path = MAIL_FOLDER_NAME + "/" + email.getFrom() + "/inbox/" + email.getId() + ".txt";
            FileOutputStream out = new FileOutputStream(path, false);
            byte[] buffer = emailDb
                    .toJSON()
                    .getBytes(StandardCharsets.UTF_8);
            out.write(buffer);
            out.close();
        } catch (IOException e) {
            ContextProvider.<LogFrame>get(LogFrame.class)
                    .addLog(MailServiceImpl.class, e.getMessage());
        }

        String emailOther = emailDb.getFrom().equals(email.getFrom()) ? emailDb.getTo() : emailDb.getFrom();
        try {
            Email emailOtherDb = getEmail(emailOther, email.getId());
            emailOtherDb.setContentHtml(contentHtml);

            String path = MAIL_FOLDER_NAME + "/" + emailOther + "/inbox/" + email.getId() + ".txt";
            FileOutputStream out = new FileOutputStream(path, false);
            byte[] buffer = emailOtherDb.toJSON().getBytes(StandardCharsets.UTF_8);
            out.write(buffer);
            out.close();
        } catch (RepositoryException | IOException e) {
            ContextProvider.<LogFrame>get(LogFrame.class)
                    .addLog(MailServiceImpl.class, e.getMessage());
            if (e.getMessage().contains("does not exist")) {
                String path = MAIL_FOLDER_NAME + "/" + emailOther + "/inbox/" + email.getId() + ".txt";
                try {
                    FileOutputStream out = new FileOutputStream(path);
                    byte[] buffer = emailDb.toJSON().getBytes(StandardCharsets.UTF_8);
                    out.write(buffer);
                    out.close();
                } catch (IOException ex) {
                    ContextProvider.<LogFrame>get(LogFrame.class)
                            .addLog(MailServiceImpl.class, e.getMessage());
                }
            }
        }
    }

    @Override
    public void transferMail(String[] emails, Email email) throws RepositoryException {
        Arrays.stream(emails).forEach(name -> {
            try {
                transferMailItem(name, email);
            } catch (RepositoryException e) {
                ContextProvider.<LogFrame>get(LogFrame.class)
                        .addLog(MailServiceImpl.class, e.getMessage());
            }
        });
    }

    private void transferMailItem(String emailName, Email email) {
        final String inboxPath = MAIL_FOLDER_NAME + "/" + emailName;
        File folder = new File(inboxPath);
        if (!folder.exists()) {
            throw new RepositoryException("Email: " + emailName + " does not exist");
        }
        try {
            FileOutputStream out = new FileOutputStream(inboxPath + "/inbox/" + email.getId() + ".txt");
            byte[] buffer = email.toJSON().getBytes(StandardCharsets.UTF_8);
            out.write(buffer);
            out.close();
        } catch (IOException e) {
            throw new RepositoryException("System error: " + e.getMessage());
        }
    }
}
