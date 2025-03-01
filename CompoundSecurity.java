import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CompoundSecurity extends JFrame {
    private CardManager manager;
    private JTextField cardIDField;
    private JComboBox<String> levelBox;
    private JTextArea displayArea;
    private JButton adminButton;
    private JButton userButton;
    private final String ADMIN_PASSWORD = "admin11";

    public CompoundSecurity() {
        manager = new CardManager();
        manager.loadFromFile();

        setTitle("Access Card Management System");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel titleLabel = new JLabel("SUNSET PARADISE \uD83C\uDF05");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        titleLabel.setForeground(new Color(0x4E342E));
        add(titleLabel, gbc);

        adminButton = new JButton("Admin");
        userButton = new JButton("Customer");

        // Set preferred size for all buttons
        Dimension buttonSize = new Dimension(170, 50);

        adminButton.setPreferredSize(buttonSize);
        userButton.setPreferredSize(buttonSize);

        adminButton.setBackground(Color.decode("#8E715B"));
        adminButton.setForeground(Color.WHITE);
        userButton.setBackground(Color.decode("#8E715B"));
        userButton.setForeground(Color.WHITE);

        adminButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showAdminPasswordPrompt();
            }
        });

        userButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showUserUI();
            }
        });

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(adminButton, gbc);

        gbc.gridx = 1;
        add(userButton, gbc);

        setVisible(true);
    }

    private void showAdminPasswordPrompt() {
        JPasswordField passwordField = new JPasswordField(10);
        passwordField.setEchoChar('●');

        int option = JOptionPane.showConfirmDialog(
                null,
                passwordField,
                "Enter Admin Password",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option == JOptionPane.OK_OPTION) {
            String password = new String(passwordField.getPassword());
            if (password.equals(ADMIN_PASSWORD)) {
                showAdminUI();
            } else {
                JOptionPane.showMessageDialog(null, "Incorrect Password!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAdminUI() {
        getContentPane().removeAll();
        setLayout(new FlowLayout());

        JLabel cardLabel = new JLabel("Card ID:");
        cardIDField = new JTextField(10);

        JLabel levelLabel = new JLabel("Access Level:");
        String[] levels = {"Low", "Medium", "High"};
        levelBox = new JComboBox<>(levels);

        JButton addButton = new JButton("Add Card");
        JButton modifyButton = new JButton("Modify Card");
        JButton revokeButton = new JButton("Revoke Card");
        JButton showButton = new JButton("View Cards");
        JButton viewLogButton = new JButton("View Audit Log");

        // Set preferred size for all buttons
        Dimension buttonSize = new Dimension(170, 50);

        addButton.setPreferredSize(buttonSize);
        modifyButton.setPreferredSize(buttonSize);
        revokeButton.setPreferredSize(buttonSize);
        showButton.setPreferredSize(buttonSize);
        viewLogButton.setPreferredSize(buttonSize);

        displayArea = new JTextArea(30, 55);
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String cardID = cardIDField.getText();
                String level = (String) levelBox.getSelectedItem();
                AccessCard newCard = null;
                if (level.equalsIgnoreCase("Low")) {
                    newCard = new GuestCard(cardID);  // Use GuestCard for Low level
                } else if (level.equalsIgnoreCase("Medium")) {
                    newCard = new StaffCard(cardID);  // Use StaffCard for Medium level
                } else if (level.equalsIgnoreCase("High")) {
                    newCard = new AdminCard(cardID);  // Use AdminCard for High level
                }
                manager.addCard(newCard);
                JOptionPane.showMessageDialog(null, "Card Added Successfully!");
                cardIDField.setText("");
            }
        });

        showButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                displayArea.setText(manager.getAllCards());
            }
        });

        modifyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String cardID = cardIDField.getText();
                String newLevel = (String) levelBox.getSelectedItem();
                manager.modifyCard(cardID, newLevel);
            }
        });

        revokeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String cardID = cardIDField.getText();
                manager.revokeCard(cardID);
                cardIDField.setText("");
            }
        });

        viewLogButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                displayAuditLog();
            }
        });

        add(cardLabel);
        add(cardIDField);
        add(levelLabel);
        add(levelBox);
        add(addButton);
        add(modifyButton);
        add(revokeButton);
        add(showButton);
        add(viewLogButton);
        add(scrollPane);

        revalidate();
        repaint();
    }

    private void displayAuditLog() {
        StringBuilder logContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader("audit_log.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logContent.append(line).append("\n");
            }
        } catch (IOException e) {
            logContent.append("Error reading audit log: ").append(e.getMessage());
        }
        displayArea.setText(logContent.toString());
    }

    private void showUserUI() {
        getContentPane().removeAll();
        setLayout(new FlowLayout());

        JLabel cardLabel = new JLabel("Card ID:");
        cardIDField = new JTextField(10);

        JButton useCardButton = new JButton("Use Card");

        // Set preferred size for use card button
        Dimension buttonSize = new Dimension(170, 50);
        useCardButton.setPreferredSize(buttonSize);

        displayArea = new JTextArea(10, 30);
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);

        useCardButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String cardID = cardIDField.getText();
                AccessCard card = manager.getCard(cardID);
                boolean accessGranted = card != null && card.grantAccess("Low");  // Assuming "Low" for user access level
                displayArea.setText(accessGranted ? "Access Granted" : "Access Denied");
            }
        });

        add(cardLabel);
        add(cardIDField);
        add(useCardButton);
        add(scrollPane);

        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        new CompoundSecurity();
    }
}
