package com.ronial.app.models;

import org.json.JSONObject;

import java.net.DatagramPacket;

public class Response {
    private final boolean success;
    private final String message;
    private final JSONObject data;

    private Response(DatagramPacket packet) {
        String dataJson = new String(packet.getData(), 0, packet.getLength());
        System.out.println(dataJson);
        this.data = new JSONObject(dataJson);
        this.success = data.getBoolean("success");
        this.message = data.getString("message");
    }

    public static Response fromDatagramPacket(DatagramPacket packet) {
        return new Response(packet);
    }

    public JSONObject getData() {
        return data;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
