package com.ronial.app.mail;

import com.ronial.app.MailServerApplication;
import com.ronial.app.conf.ConfigManagement;
import com.ronial.app.context.Context;
import com.ronial.app.context.ContextProvider;
import com.ronial.app.mail.commands.CommandExecutor;
import com.ronial.app.models.Request;
import com.ronial.app.models.Response;
import com.ronial.app.utils.DateUtils;
import com.ronial.app.views.LogFrame;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.time.Instant;

public class Server implements Context {
    private DatagramSocket serverSocket;
    private boolean isRunning;

    private Server() {
    }

    public static Server launch() throws IOException {
        int port = (int) ContextProvider
                .<ConfigManagement>get(ConfigManagement.class)
                .value("server.port").get();
        return new Server(port);
    }

    private Server(int port) throws IOException {
        this.isRunning = true;
        this.serverSocket = new DatagramSocket(port);
        byte[] buffer = new byte[102400000];
        CommandExecutor commandExecutor = new CommandExecutor(this);
        new Thread(() -> {
            try {
                while (isRunning) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    serverSocket.receive(packet);
                    Request request = Request.fromDatagramPacket(packet);
                    commandExecutor.execute(request);
                }
                System.out.println("Server stopped!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        LogFrame logFrame = ContextProvider.get(LogFrame.class);
        logFrame.getStopButton().addActionListener(e -> {
            close();
            logFrame.addLog(Server.class, "Server stopped!");
            logFrame.getStopButton().setEnabled(false);
            String contentLog = logFrame.getLogArea().getText();
            try {
                File logFolder = new File(MailServerApplication.MAIL_LOG_FOLDER_NAME);
                if (!logFolder.exists()) {
                    logFolder.mkdir();
                }
                DataOutputStream stream = new DataOutputStream(
                        new FileOutputStream(logFolder + "/" + Instant.now().getEpochSecond() + ".txt"));
                stream.writeUTF(contentLog);
                stream.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        logFrame.addLog(Server.class, "Server started! Port: " + port);
    }

    public void close() {
        this.isRunning = false;
    }

    public void sendResponse(Response response, DatagramPacket packet) throws IOException {
        DatagramPacket res = new DatagramPacket(
                response.toBytes(),
                response.toBytes().length,
                packet.getAddress(),
                packet.getPort());
        serverSocket.send(res);
    }
}
