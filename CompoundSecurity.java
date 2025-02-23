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
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel titleLabel = new JLabel("SUNSET PARADISE");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 35));
        add(titleLabel, gbc);

        adminButton = new JButton("Admin");
        userButton = new JButton("Customer");

        adminButton.setPreferredSize(new Dimension(120, 40));
        userButton.setPreferredSize(new Dimension(120, 40));

        adminButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String password = JOptionPane.showInputDialog("Enter Admin Password:");
                if (password != null && password.equals(ADMIN_PASSWORD)) {
                    showAdminUI();
                } else {
                    JOptionPane.showMessageDialog(null, "Incorrect Password!");
                }
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
        JButton showButton = new JButton("Show All Cards");
        JButton viewLogButton = new JButton("View Audit Log");

        displayArea = new JTextArea(10, 30);
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String cardID = cardIDField.getText();
                String level = (String) levelBox.getSelectedItem();
                AccessCard newCard = null;
                if (level.equalsIgnoreCase("Low")) {
                    newCard = new LowAccessCard(cardID);
                } else if (level.equalsIgnoreCase("Medium")) {
                    newCard = new MediumAccessCard(cardID);
                } else if (level.equalsIgnoreCase("High")) {
                    newCard = new HighAccessCard(cardID);
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
        add(viewLogButton);  // Added View Log Button
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

        displayArea = new JTextArea(10, 30);
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);

        useCardButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String cardID = cardIDField.getText();
                AccessCard card = manager.getCard(cardID);
                if (card != null) {
                    boolean accessGranted = card.grantAccess("Low"); // Assume "Low" level here
                    String message = accessGranted ? "Access Granted" : "Access Denied";
                    displayArea.setText(message);
                } else {
                    displayArea.setText("Card not found.");
                }
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
