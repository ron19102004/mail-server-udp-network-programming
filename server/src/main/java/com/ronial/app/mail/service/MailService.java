package com.ronial.app.mail.service;

import com.ronial.app.context.Context;
import com.ronial.app.exceptions.RepositoryException;
import com.ronial.app.models.Email;
import com.ronial.app.models.User;

import java.util.List;

public interface MailService extends Context {
    void createMailAccount(String email, String password, String name) throws RepositoryException;
    User loginMailAccount(String email, String password) throws RepositoryException;
    void saveEmail(Email email) throws RepositoryException;
    Email getEmail(String email,long id) throws RepositoryException;
    List<Email> getEmails(String email) throws RepositoryException;
    void deleteEmail(String email,long id) throws RepositoryException;
    void replyEmail(Email email) throws RepositoryException;
    void transferMail(String[] emails ,Email email) throws RepositoryException;
}
