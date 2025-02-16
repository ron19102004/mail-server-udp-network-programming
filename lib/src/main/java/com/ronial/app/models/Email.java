package com.ronial.app.models;

import org.json.JSONObject;

public class Email {
    private long id;
    private String from;
    private String to;
    private String subject;
    private String body;
    private String links;
    private String contentHtml;
    public Email() {}
    public Email(long id, String from, String to, String subject, String body, String links, String contentHtml) {
        this.from = from;
        this.id = id;
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.links = links;
        this.contentHtml = contentHtml;
    }

    public static Email fromJSON(String json) {
        JSONObject obj = new JSONObject(json);
        long id = obj.getLong("id");
        String from = obj.getString("from");
        String to = obj.getString("to");
        String subject = obj.getString("subject");
        String body = obj.getString("body");
        String links = obj.getString("links");
        String contentHtml = obj.getString("contentHtml");
        return new Email(id, from, to, subject, body, links, contentHtml);
    }
    public String toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("from", from);
        obj.put("to", to);
        obj.put("subject", subject);
        obj.put("body", body);
        obj.put("links", links);
        obj.put("contentHtml", contentHtml);
        return obj.toString();
    }
    public long getId() {
        return id;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getLinks() {
        return links;
    }

    public String getContentHtml() {
        return contentHtml;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setLinks(String links) {
        this.links = links;
    }

    public void setContentHtml(String contentHtml) {
        this.contentHtml = contentHtml;
    }
}
