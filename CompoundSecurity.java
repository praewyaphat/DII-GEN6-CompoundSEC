import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

public class CompoundSecurity extends JFrame {
    private CardManager manager;
    private JTextField cardIDField;
    private JTextArea displayArea;
    private JButton adminButton;
    private JButton userButton;
    private final String ADMIN_PASSWORD = "admin11";
    private Set<String> usedFloors = new HashSet<>();
    private JButton viewStatusButton;

    public CompoundSecurity() {
        manager = new CardManager();
        manager.loadFromFile();

        setTitle("Access Card Management System");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
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

        int option = JOptionPane.showConfirmDialog(null, passwordField, "Enter Admin Password",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if(option == JOptionPane.OK_OPTION) {
            String password = new String(passwordField.getPassword());
            if(password.equals(ADMIN_PASSWORD)) {
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

        JLabel userTypeLabel = new JLabel("User Type:");
        String[] userTypes = {"Guest", "Staff", "Admin"};
        JComboBox<String> userTypeBox = new JComboBox<>(userTypes);

        JButton addButton = new JButton("Add Card");
        JButton modifyButton = new JButton("Modify Card");
        JButton revokeButton = new JButton("Revoke Card");
        JButton showButton = new JButton("View Cards");
        JButton viewLogButton = new JButton("View Audit Log");
        JButton backButton = new JButton("Back");

        Dimension buttonSize = new Dimension(170, 50);
        addButton.setPreferredSize(buttonSize);
        modifyButton.setPreferredSize(buttonSize);
        revokeButton.setPreferredSize(buttonSize);
        showButton.setPreferredSize(buttonSize);
        viewLogButton.setPreferredSize(buttonSize);
        backButton.setPreferredSize(buttonSize);

        displayArea = new JTextArea(30,55);
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String cardID = cardIDField.getText().trim();
                if(cardID.isEmpty()){
                    JOptionPane.showMessageDialog(null, "Please enter a Card ID!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String userType = (String) userTypeBox.getSelectedItem();
                AccessCard newCard = null;
                if(userType.equalsIgnoreCase("Guest")){
                    newCard = new GuestCard(cardID);
                } else if(userType.equalsIgnoreCase("Staff")){
                    newCard = new StaffCard(cardID);
                } else if(userType.equalsIgnoreCase("Admin")){
                    newCard = new AdminCard(cardID);
                }
                manager.addCard(newCard);
                JOptionPane.showMessageDialog(null, "Card Added Successfully!");
                cardIDField.setText("");
            }
        });

        modifyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String cardID = cardIDField.getText().trim();
                if(cardID.isEmpty()){
                    JOptionPane.showMessageDialog(null, "Please enter a Card ID to modify!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String newUserType = (String) userTypeBox.getSelectedItem();
                String newLevel = "Low";
                if(newUserType.equalsIgnoreCase("Staff")){
                    newLevel = "Medium";
                } else if(newUserType.equalsIgnoreCase("Admin")){
                    newLevel = "High";
                }
                manager.modifyCard(cardID, newLevel);
                JOptionPane.showMessageDialog(null, "Card Modified Successfully!");
            }
        });

        revokeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String cardID = cardIDField.getText().trim();
                if(cardID.isEmpty()){
                    JOptionPane.showMessageDialog(null, "Please enter a Card ID to revoke!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                manager.revokeCard(cardID);
                JOptionPane.showMessageDialog(null, "Card Revoked Successfully!");
                cardIDField.setText("");
            }
        });

        showButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                displayArea.setText(manager.getAllCards());
            }
        });

        viewLogButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                displayAuditLog();
            }
        });

        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showRoleSelectionUI();
            }
        });

        add(cardLabel);
        add(cardIDField);
        add(userTypeLabel);
        add(userTypeBox);
        add(addButton);
        add(modifyButton);
        add(revokeButton);
        add(showButton);
        add(viewLogButton);
        add(backButton);
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
        } catch (Exception e) {
            logContent.append("Error reading audit log: ").append(e.getMessage());
        }
        displayArea.setText(logContent.toString());
    }

    private void showUserUI() {
        getContentPane().removeAll();
        setLayout(new FlowLayout());

        JLabel cardLabel = new JLabel("Card ID:");
        // ใช้ comboBox ในการเลือก Card ID
        JComboBox<String> cardComboBox = new JComboBox<>(manager.getCardIDs());

        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField(10);

        JLabel floorLabel = new JLabel("Floor:");
        String[] floors = {"Floor 1", "Floor 2", "Floor 3"};
        JComboBox<String> floorComboBox = new JComboBox<>(floors);

        JButton useCardButton = new JButton("Ok");
        JButton backButton = new JButton("Back");

        Dimension buttonSize = new Dimension(170,50);
        useCardButton.setPreferredSize(buttonSize);
        backButton.setPreferredSize(buttonSize);

        displayArea = new JTextArea(30,55);
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);

        // เก็บชั้นที่ใช้ไปแล้ว
        Set<String> selectedFloors = new HashSet<>();

        useCardButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText().trim();
                String cardID = (String) cardComboBox.getSelectedItem();
                String selectedFloor = (String) floorComboBox.getSelectedItem();

                if(username.isEmpty()){
                    JOptionPane.showMessageDialog(null, "Please enter a Username!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if(cardID == null){
                    JOptionPane.showMessageDialog(null, "Please select a Card!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String cardFloorKey = cardID + "-" + selectedFloor;
                if(selectedFloors.contains(cardFloorKey)){
                    JOptionPane.showMessageDialog(null, "This card has already been used on " + selectedFloor + "!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                AccessCard card = manager.getCard(cardID);
                if(card == null){
                    displayArea.setText("Card not found.");
                    JOptionPane.showMessageDialog(null, "Card not found!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String cardLevel = card.getAccessLevel();
                String cardStatus = "Card ID: " + cardID + " | Card Level: " + cardLevel + " | Username: " + username;

                // ในหน้า customer ห้ามใช้บัตรที่เป็น Medium หรือ High
                if(cardLevel.equalsIgnoreCase("Medium") || cardLevel.equalsIgnoreCase("High")){
                    displayArea.setText(cardStatus + "\nAccess Denied: This card cannot be used.");
                    manager.logUsage(username, "Access Denied", cardID, "N/A");
                    JOptionPane.showMessageDialog(null, "Access Denied: This card cannot be used.", "Access Denied", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                selectedFloors.add(cardFloorKey);
                // ตั้ง AuditTrail ให้เป็น FloorAudit (สำหรับบันทึกระดับชั้น)
                manager.setAuditTrail(new AuditTrails.FloorAudit());
                manager.recordUsage(cardID, username, selectedFloor, cardLevel);
                manager.logUsage(username, "Access Granted", cardID, "Access to " + selectedFloor);
                JOptionPane.showMessageDialog(null, "Access to " + selectedFloor + " granted.", "Access Granted", JOptionPane.INFORMATION_MESSAGE);

                String status = manager.getFullCardStatus(cardID);
                displayArea.setText(status);
            }
        });

        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showRoleSelectionUI();
            }
        });

        add(usernameLabel);
        add(usernameField);
        add(cardLabel);
        add(cardComboBox);
        add(floorLabel);
        add(floorComboBox);
        add(useCardButton);
        add(backButton);
        add(scrollPane);

        revalidate();
        repaint();
    }

    private void showRoleSelectionUI() {
        getContentPane().removeAll();
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
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
        Dimension buttonSize = new Dimension(170,50);
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
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        new CompoundSecurity();
    }
}
