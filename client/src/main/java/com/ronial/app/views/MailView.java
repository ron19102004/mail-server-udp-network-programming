package com.ronial.app.views;

import com.ronial.app.context.ContextProvider;
import com.ronial.app.mail.MailHtmlFormat;
import com.ronial.app.mail.MailService;
import com.ronial.app.models.Email;
import com.ronial.app.models.User;
import com.ronial.app.views.utils.OpenBrowserUtils;
import com.ronial.app.views.utils.Toast;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
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
    private SwingBrowser swingBrowser;
    public static void launch(User user) {
        new MailView(user);
    }
    public MailView(User user) {
        mailService = ContextProvider.get(MailService.class);
        this.user = user;
        setTitle("Mail - " + user.getEmail() + " - " + user.getName());
        setIconImage(new ImageIcon(Objects.requireNonNull(getClass().getResource("/assets/Email-icon.png"))).getImage());
        setSize(1300, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 240, 240));

        createMenuBar();

        JPanel leftPanel = createLeftPanel();
        JPanel rightPanel = createRightPanel();

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        inboxEmails = new ArrayList<>();
        sentEmails = new ArrayList<>();

        refreshInbox();
        setVisible(true);

        SwingUtilities.invokeLater(() -> {
            swingBrowser = SwingBrowser.launch();
        });
    }
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuOptions = new JMenu("📩 Tùy chọn");
        JMenuItem newEmailItem = new JMenuItem("📝 Tạo Mail");
        JMenuItem refreshItem = new JMenuItem("🔄 Làm mới");
        JMenuItem deleteMail = new JMenuItem("❌ Xóa mail hiện tại");
        JMenuItem transferMail = new JMenuItem("🚚 Chuyển tiếp mail hiện tại");
        JMenuItem logout = new JMenuItem("☠️ Đăng xuất");

        newEmailItem.addActionListener(e -> new CreateMailView(this));
        refreshItem.addActionListener(e -> {
            refreshInbox();
        });
        transferMail.addActionListener(e -> {
            int selectedEmailIndex = emailList.getSelectedIndex();
            if (selectedEmailIndex > -1) {
                Email email = getEmailFromTabbedPane(selectedEmailIndex);
                new TransferMailView(email, this);
            } else {
                Toast.error("Vui lòng chọn mail cần trả chuyển tiếp");
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


        menuOptions.add(newEmailItem);
        menuOptions.add(refreshItem);
        menuOptions.add(deleteMail);
        menuOptions.add(transferMail);
        menuOptions.add(logout);
        menuBar.add(menuOptions);
        setJMenuBar(menuBar);
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(280, 0));
        inboxModel = new DefaultListModel<>();
        sentModel = new DefaultListModel<>();
        emailList = new JList<>(inboxModel);
        emailList.addListSelectionListener(e -> displayEmailContent());
        JScrollPane emailScrollPane = new JScrollPane(emailList);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Hộp thư đến", null);
        tabbedPane.addTab("Hộp thư đi", null);
        tabbedPane.addChangeListener(e -> switchEmailList());

        leftPanel.add(tabbedPane, BorderLayout.NORTH);
        leftPanel.add(emailScrollPane, BorderLayout.CENTER);

        return leftPanel;
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBorder(BorderFactory.createTitledBorder("📜 Nội dung thư"));

        emailContent = new JEditorPane();
        emailContent.setContentType("text/html");
        emailContent.setEditable(false);
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
        rightPanel.add(contentScrollPane, BorderLayout.CENTER);
        return rightPanel;
    }
    public void refreshInbox() {
        try {
            emailContent.setText("");
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
        emailContent.setText("");
    }
    public Email getEmailFromTabbedPane(int selectedIndex) {
        Email email;
        if (tabbedPane.getSelectedIndex() == 0){
            email = inboxEmails.get(selectedIndex);
        } else {
            email = sentEmails.get(selectedIndex);
        }
        return email;
    }
    private void displayEmailContent() {
        int selectedEmailIndex = emailList.getSelectedIndex();
        if (selectedEmailIndex > -1) {
            Email email = getEmailFromTabbedPane(selectedEmailIndex);
            emailContent.setText(MailHtmlFormat.toContentHtml(email));
            try {
                mailService.readMail(this, email);
            } catch (IOException e) {
                System.out.println(e);
            }
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