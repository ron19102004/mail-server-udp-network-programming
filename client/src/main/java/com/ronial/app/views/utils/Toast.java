package com.ronial.app.views.utils;

import javax.swing.*;

public class Toast {
    public static void error(String message){
        JOptionPane.showMessageDialog(null, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
    public static void info(String message){
        JOptionPane.showMessageDialog(null, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }
}
