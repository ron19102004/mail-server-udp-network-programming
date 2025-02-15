package com.ronial.app.views;

import com.ronial.app.context.ContextProvider;
import com.ronial.app.mail.MailService;
import com.ronial.app.models.Email;
import com.ronial.app.models.User;
import com.ronial.app.views.utils.Toast;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MailView extends JFrame {
    private User user;
    private DefaultListModel<String> emailListModel;
    private JList<String> emailList;
    private JEditorPane emailContent;
    private JMenuBar menuBar;
    private JMenu menuOptions;
    private JMenuItem newEmailItem, refreshItem, deleteMail, replyMail, transferMail;
    private List<Email> emails;
    private MailService mailService;

    public MailView(User user) {
        this.user = user;
        mailService = ContextProvider.get(MailService.class);
        setIconImage(new ImageIcon(Objects.requireNonNull(getClass().getResource("/assets/Email-icon.png"))).getImage());
        setTitle("Mail - " + user.getEmail() + " - " + user.getName());
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 240, 240));

        menuBar = new JMenuBar();
        menuBar.setBackground(new Color(0, 137, 123));
        menuOptions = new JMenu("📩 Tùy chọn");
        menuOptions.setForeground(Color.WHITE);

        newEmailItem = new JMenuItem("📝 Tạo Mail");
        refreshItem = new JMenuItem("🔄 Làm mới");
        deleteMail = new JMenuItem("❌ Xóa mail hiện tại");
        replyMail = new JMenuItem("📝 Trả lời mail hiện tại");
        transferMail= new JMenuItem("🚚 Chuyển tiếp mail hiện tại");


        newEmailItem.setBackground(new Color(0, 150, 136));
        newEmailItem.setForeground(Color.WHITE);
        refreshItem.setBackground(new Color(0, 150, 136));
        refreshItem.setForeground(Color.WHITE);
        deleteMail.setBackground(new Color(0, 150, 136));
        deleteMail.setForeground(Color.WHITE);
        replyMail.setBackground(new Color(0, 150, 136));
        replyMail.setForeground(Color.WHITE);
        transferMail.setBackground(new Color(0, 150, 136));
        transferMail.setForeground(Color.WHITE);



        menuOptions.add(newEmailItem);
        menuOptions.add(refreshItem);
        menuOptions.add(deleteMail);
        menuOptions.add(replyMail);
        menuOptions.add(transferMail);
        menuBar.add(menuOptions);
        setJMenuBar(menuBar);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(280, 0));
        leftPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0, 150, 136), 2), "📥 Hộp thư đến"));
        leftPanel.setBackground(Color.WHITE);

        emailListModel = new DefaultListModel<>();
        emailList = new JList<>(emailListModel);
        emailList.setFont(new Font("Arial", Font.PLAIN, 18));
        emailList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        emailList.setBackground(new Color(245, 245, 245)); // Xám nhạt dịu mắt
        emailList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JScrollPane emailScrollPane = new JScrollPane(emailList);
        leftPanel.add(emailScrollPane, BorderLayout.CENTER);

        // ✉️ RIGHT PANEL - Nội dung email
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0, 150, 136), 2), "📜 Nội dung thư"));
        rightPanel.setBackground(Color.WHITE);

        emailContent = new JEditorPane();
        emailContent.setContentType("text/html"); // Hỗ trợ HTML
        emailContent.setEditable(false);
        emailContent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane contentScrollPane = new JScrollPane(emailContent);
        rightPanel.add(contentScrollPane, BorderLayout.CENTER);

        replyMail.addActionListener(e -> {
            int selectedEmailIndex = emailList.getSelectedIndex();
            if (selectedEmailIndex > -1) {
                Email email = emails.get(selectedEmailIndex);
                new ReplyMailView(email,this);
            } else {
                Toast.error("Vui lòng chọn mail cần trả lời");
            }

        });
        transferMail.addActionListener(e -> {
            int selectedEmailIndex = emailList.getSelectedIndex();
            if (selectedEmailIndex > -1) {
                Email email = emails.get(selectedEmailIndex);
                new TransferMailView(email,this);
            } else {
                Toast.error("Vui lòng chọn mail cần trả chuyển tiếp");
            }
        });
        newEmailItem.addActionListener(e -> new CreateMailView(this));
        refreshItem.addActionListener(e -> refreshInbox());
        emailList.addListSelectionListener(e -> displayEmailContent());
        deleteMail.addActionListener(e -> {
            try {
                mailService.deleteMail(this);
            } catch (IOException ex) {
                Toast.error(ex.getMessage());
            }
        });

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        refreshInbox(); //refresh first
        setVisible(true);
    }

    public JEditorPane getEmailContent() {
        return emailContent;
    }

    public List<Email> getEmails() {
        return emails;
    }

    public void setEmails(List<Email> emails) {
        this.emails = emails;
    }

    public User getUser() {
        return user;
    }

    public DefaultListModel<String> getEmailListModel() {
        return emailListModel;
    }

    public void refreshInbox() {
        try {
            emailContent.setText("");
            mailService.loadMails(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JList<String> getEmailList() {
        return emailList;
    }

    private void displayEmailContent() {
        int selectedEmailIndex = emailList.getSelectedIndex();
        if (selectedEmailIndex > -1) {
            Email email = emails.get(selectedEmailIndex);
            emailContent.setText(email.getContentHtml());
        }
    }

    public static void launch(User user) {
        new MailView(user);
    }
}
