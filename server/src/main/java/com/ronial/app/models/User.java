package com.ronial.app.models;

import org.json.JSONObject;

public class User {
    private String name;
    private String email;
    private String password;

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public static User fromJson(String json) {
        JSONObject jsonObject = new JSONObject(json);
        String name = jsonObject.getString("name");
        String email = jsonObject.getString("email");
        String password = jsonObject.getString("password");
        return new User(name, email, password);
    }

    public String toJson() {
        return new JSONObject()
                .put("name", name)
                .put("email", email)
                .put("password", password)
                .toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
