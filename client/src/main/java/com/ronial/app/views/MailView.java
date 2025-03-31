package com.ronial.app.views;

import com.ronial.app.context.ContextProvider;
import com.ronial.app.mail.MailHtmlFormat;
import com.ronial.app.mail.MailService;
import com.ronial.app.models.Email;
import com.ronial.app.models.User;
import com.ronial.app.utils.StringUtils;
import com.ronial.app.views.utils.OpenBrowserUtils;
import com.ronial.app.views.utils.Toast;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

public class MailView extends JFrame {
    private final User user;
    private DefaultListModel<String> inboxModel, sentModel;
    private JList<String> emailList;
    private JEditorPane emailContent;
    private List<Email> inboxEmails, sentEmails;
    private final MailService mailService;
    private JTabbedPane tabbedPane;
    private final SwingBrowser swingBrowser;
    private WebEngine webEngine;

    public static void launch(User user) {
        new MailView(user);
    }

    public MailView(User user) {
        swingBrowser = ContextProvider.get(SwingBrowser.class);
        mailService = ContextProvider.get(MailService.class);
        this.user = user;
        setTitle("Mail - " + user.getEmail() + " - " + user.getName());
        setIconImage(new ImageIcon(Objects.requireNonNull(getClass().getResource("/assets/Email-icon.png"))).getImage());
        setSize(1300, 800);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 240, 240));

        createMenuBar();

        JPanel leftPanel = createLeftPanel();
        JPanel rightPanel = createRightPanelFX();

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        inboxEmails = new ArrayList<>();
        sentEmails = new ArrayList<>();

        refreshInbox();
        setVisible(true);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(245, 245, 245)); // Màu nền sáng

        JMenu menuOptions = new JMenu("♨️ Hệ thống");
        JMenu menuMails = new JMenu("📩 Tùy chọn thư");

        JMenuItem newEmailItem = createMenuItem("📝 Tạo Mail", "Soạn email mới");
        JMenuItem refreshItem = createMenuItem("🔄 Làm mới", "Làm mới danh sách email");
        JMenuItem deleteMail = createMenuItem("❌ Xóa mail hiện tại", "Xóa email đang mở");
        JMenuItem transferMail = createMenuItem("🚚 Chuyển tiếp mail", "Chuyển tiếp email hiện tại");
        JMenuItem logout = createMenuItem("☠️ Đăng xuất", "Đăng xuất khỏi tài khoản");
        JMenuItem exit = createMenuItem("❌ Thoát", "Thoát ứng dụng");

        newEmailItem.addActionListener(e -> new CreateMailView(this));
        refreshItem.addActionListener(e -> refreshInbox());
        transferMail.addActionListener(e -> {
            int selectedEmailIndex = emailList.getSelectedIndex();
            if (selectedEmailIndex > -1) {
                Email email = getEmailFromTabbedPane(selectedEmailIndex);
                new TransferMailView(email, this);
            } else {
                Toast.error("Vui lòng chọn mail cần chuyển tiếp");
            }
        });
        deleteMail.addActionListener(e -> {
            try {
                mailService.deleteMail(this);
            } catch (IOException ex) {
                Toast.error(ex.getMessage());
            }
        });
        logout.addActionListener(e -> {
            dispose();
            MailLaunch.launch();
        });
        exit.addActionListener(e -> System.exit(0));

        menuMails.add(newEmailItem);
        menuMails.add(refreshItem);
        menuMails.add(deleteMail);
        menuMails.add(transferMail);

        menuOptions.add(logout);
        menuOptions.addSeparator();
        menuOptions.add(exit);

        menuBar.add(menuOptions);
        menuBar.add(menuMails);
        setJMenuBar(menuBar);
    }

    private JMenuItem createMenuItem(String title, String tooltip) {
        JMenuItem item = new JMenuItem(title);
        item.setOpaque(true);
        item.setFont(item.getFont().deriveFont(14f));
        item.setBackground(new Color(245, 245, 245));  // Màu nền sáng
        item.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        item.setToolTipText(tooltip);

        // Thêm hiệu ứng hover cho mục menu
        item.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                item.setBackground(new Color(230, 230, 230));  // Màu nền khi hover
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                item.setBackground(new Color(245, 245, 245));  // Màu nền khi không hover
            }
        });

        return item;
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(280, 0));

        inboxModel = new DefaultListModel<>();
        sentModel = new DefaultListModel<>();
        emailList = new JList<>(inboxModel);
        emailList.addListSelectionListener(e -> displayEmailContent());

        // Thêm Border cho bảng
        JScrollPane emailScrollPane = new JScrollPane(emailList);
        emailScrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        emailScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);


        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Hộp thư đến", null);
        tabbedPane.addTab("Hộp thư đi", null);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0)); // Border dưới tab
        tabbedPane.setBackground(new Color(240, 240, 240)); // Màu nền nhẹ cho tab

        tabbedPane.addChangeListener(e -> switchEmailList());

        leftPanel.add(tabbedPane, BorderLayout.NORTH);
        leftPanel.add(emailScrollPane, BorderLayout.CENTER);

        return leftPanel;
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBorder(BorderFactory.createTitledBorder("📜 Nội dung thư"));

        emailContent = new JEditorPane();
        emailContent.setEditorKit(new HTMLEditorKit());
        emailContent.setContentType("text/html");
        emailContent.setEditable(false);
        emailContent.setBackground(new Color(245, 245, 245));
        emailContent.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    swingBrowser.open(e.getURL().toString());
                } catch (NullPointerException ex) {
                    System.err.println(ex.getMessage());
                }
            }
        });
        JScrollPane contentScrollPane = new JScrollPane(emailContent);
        contentScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); // Show vertical scrollbar if needed
        rightPanel.add(contentScrollPane, BorderLayout.CENTER);
        return rightPanel;
    }

    private JPanel createRightPanelFX() {
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBorder(BorderFactory.createTitledBorder("📜 Nội dung thư"));

        JFXPanel fxPanel = new JFXPanel(); // Nhúng JavaFX vào Swing
        Platform.runLater(() -> {
            WebView webView = new WebView();
            webEngine = webView.getEngine();
            webEngine.setJavaScriptEnabled(true);

//            Path tempFile = null;
//            try {
//                tempFile = Files.createTempFile("emoji_page", ".html");
//                Files.write(tempFile, htmlContent.getBytes(StandardCharsets.UTF_8));
//                webEngine.load(tempFile.toUri().toString());
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }

            BorderPane root = new BorderPane();
            root.setCenter(webView);
            fxPanel.setScene(new Scene(root));
        });

        rightPanel.add(fxPanel, BorderLayout.CENTER);
        return rightPanel;
    }

    public void refreshInbox() {
        try {
//            emailContent.setText("");
            mailService.loadMails(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void switchEmailList() {
        if (tabbedPane.getSelectedIndex() == 0) {
            emailList.setModel(inboxModel);
        } else {
            emailList.setModel(sentModel);
        }
//        emailContent.setText("");
    }

    public Email getEmailFromTabbedPane(int selectedIndex) {
        Email email;
        if (tabbedPane.getSelectedIndex() == 0) {
            email = inboxEmails.get(selectedIndex);
        } else {
            email = sentEmails.get(selectedIndex);
        }
        return email;
    }

    public void displayEmailContent(Email email) {
//        SwingUtilities.invokeLater(() -> {
//            emailContent.setText(MailHtmlFormat.toContentHtml(email));
//        });
        Platform.runLater(() -> {
            String htmlContent = "<html><head><meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>" +
                    "<style>body { font-family: 'Segoe UI Emoji', 'Noto Color Emoji', sans-serif; }</style>" +
                    "</head><body>" +
                    MailHtmlFormat.toContentHtml(email) +
                    "</body></html>";

            String base64Content = Base64.getEncoder().encodeToString(htmlContent.getBytes(StandardCharsets.UTF_8));
            String htmlData = "data:text/html;charset=UTF-8;base64," + base64Content;

            webEngine.load(htmlData);
        });
    }

    private void displayEmailContent() {
        int selectedEmailIndex = emailList.getSelectedIndex();
        if (selectedEmailIndex > -1) {
            Email email = getEmailFromTabbedPane(selectedEmailIndex);
            displayEmailContent(email);
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    mailService.readMail(this, email);
                } catch (IOException e) {
                    System.out.println(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }

    public JEditorPane getEmailContent() {
        return emailContent;
    }

    public User getUser() {
        return user;
    }

    public DefaultListModel<String> getInboxModel() {
        return inboxModel;
    }

    public DefaultListModel<String> getSentModel() {
        return sentModel;
    }

    public void setInboxEmails(List<Email> inboxEmails) {
        this.inboxEmails = inboxEmails;
    }

    public void setSentEmails(List<Email> sentEmails) {
        this.sentEmails = sentEmails;
    }

    public JList<String> getEmailList() {
        return emailList;
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public List<Email> getSentEmails() {
        return sentEmails;
    }

    public List<Email> getInboxEmails() {
        return inboxEmails;
    }
}