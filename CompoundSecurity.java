import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CompoundSecurity extends JFrame {
    private CardManager manager;
    private JTextField cardIDField;
    private JTextArea displayArea;
    private JButton adminButton;
    private JButton userButton;
    private final String ADMIN_PASSWORD = "admin11";
    private Map<String, String> selectedRooms = new HashMap<>(); // บัตร -> ห้อง
    private Map<String, String> roomUsage = new HashMap<>(); // ห้อง -> บัตร
    private JButton viewStatusButton;

    public CompoundSecurity() {
        manager = new CardManager();
        manager.loadFromFile();
        loadSelectedRooms();

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

        JLabel endTimeLabel = new JLabel("End Time (YYYY-MM-DD):");
        JTextField endTimeField = new JTextField(16);

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

        displayArea = new JTextArea(30,70);
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String cardID = cardIDField.getText().trim();
                if (cardID.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please enter a Card ID!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String userType = (String) userTypeBox.getSelectedItem();
                AccessCard newCard;

                try {
                    LocalDateTime startTime = LocalDateTime.now(); // เริ่มต้นที่เวลาปัจจุบัน
                    LocalDateTime endTime;

                    // ถ้าผู้ใช้ไม่ได้กรอกวันหมดอายุ ให้ใช้ค่าเริ่มต้นเป็นวันนี้ 23:59
                    if (endTimeField.getText().trim().isEmpty()) {
                        endTime = startTime.withHour(23).withMinute(59);
                    } else {
                        // ถ้าผู้ใช้เลือกวันหมดอายุ ให้เติมเวลาเป็น 23:59
                        endTime = LocalDateTime.parse(endTimeField.getText().trim() + "T23:59");

                        if (endTime.isBefore(startTime)) {
                            JOptionPane.showMessageDialog(null, "End Date must be after Start Date!", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    newCard = new TimeBasedAccessCard(cardID, userType, startTime, endTime);
                    manager.addCard(newCard);
                    JOptionPane.showMessageDialog(null, "Card Added Successfully!");
                    cardIDField.setText("");
                    endTimeField.setText("");

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Invalid Date Format! Use YYYY-MM-DD", "Error", JOptionPane.ERROR_MESSAGE);
                }
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

                try {
                    LocalDateTime newStartTime = LocalDateTime.now();
                    LocalDateTime newEndTime = LocalDateTime.parse(endTimeField.getText().trim().replace(" ", "T"));

                    if (newEndTime.isBefore(newStartTime)) {
                        JOptionPane.showMessageDialog(null, "End Time must be after Start Time!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    manager.modifyCardWithTime(cardID, newLevel, newStartTime, newEndTime);
                } catch (Exception ex) {
                    manager.modifyCard(cardID, newLevel); // ถ้าไม่กรอกเวลา ใช้การแก้ไขปกติ
                }

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
        add(endTimeLabel);
        add(endTimeField);
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
        JComboBox<String> cardComboBox = new JComboBox<>(manager.getCardIDs());

        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField(10);

        JLabel floorLabel = new JLabel("Floor:");
        String[] floors = {"Floor 1", "Floor 2", "Floor 3"};
        JComboBox<String> floorComboBox = new JComboBox<>(floors);

        JLabel roomLabel = new JLabel("Room:");
        String[] rooms = {"Room 101", "Room 102", "Room 201", "Room 202", "Room 301", "Room 302"};
        JComboBox<String> roomComboBox = new JComboBox<>(rooms);

        JButton useCardButton = new JButton("Ok");
        JButton backButton = new JButton("Back");

        Dimension buttonSize = new Dimension(170, 50);
        useCardButton.setPreferredSize(buttonSize);
        backButton.setPreferredSize(buttonSize);

        displayArea = new JTextArea(30, 70);
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);

        Set<String> selectedFloors = new HashSet<>();

//        useCardButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                String username = usernameField.getText().trim();
//                String cardID = (String) cardComboBox.getSelectedItem();
//                String selectedFloor = (String) floorComboBox.getSelectedItem();
//                String selectedRoom = (String) roomComboBox.getSelectedItem();
//
//                if (username.isEmpty()) {
//                    JOptionPane.showMessageDialog(null, "Please enter a Username!", "Error", JOptionPane.ERROR_MESSAGE);
//                    return;
//                }
//                if (cardID == null) {
//                    JOptionPane.showMessageDialog(null, "Please select a Card!", "Error", JOptionPane.ERROR_MESSAGE);
//                    return;
//                }
//
//                String cardUsageKey = cardID + "-" + selectedFloor + "-" + selectedRoom;
//                if (selectedRooms.contains(cardUsageKey)) {
//                    JOptionPane.showMessageDialog(null, "This card has already been used for another room on this floor!", "Error", JOptionPane.ERROR_MESSAGE);
//                    return;
//                }
//
//                AccessCard card = manager.getCard(cardID);
//                if (card == null) {
//                    displayArea.setText("Card not found.");
//                    JOptionPane.showMessageDialog(null, "Card not found!", "Error", JOptionPane.ERROR_MESSAGE);
//                    return;
//                }
//
//                String cardLevel = card.getAccessLevel();
//                String cardStatus = "Card ID: " + cardID + " | Card Level: " + cardLevel + " | Username: " + username;
//                String expiryDate = "N/A";
//                String status = "Active";
//
//                if (card instanceof TimeBasedAccessCard) {
//                    TimeBasedAccessCard tbc = (TimeBasedAccessCard) card;
//                    expiryDate = String.valueOf(tbc.getEndTime()
//                            .atZone(ZoneId.systemDefault())
//                            .toInstant()
//                            .toEpochMilli());
//
//                    if (tbc.isCardExpired()) {
//                        status = "Expired";
//                        displayArea.setText(cardStatus + "\nAccess Denied: Card Expired\nTime-Based Encryption | Expires On: " + expiryDate);
//                        manager.logUsage(username, "Access Denied", cardID, "Expired Card");
//                        JOptionPane.showMessageDialog(null, "Access Denied: Card Expired", "Error", JOptionPane.ERROR_MESSAGE);
//                        return;
//                    }
//                    cardStatus += " | Expires On: " + expiryDate;
//                }
//
//                try {
//                    if (card.getClass().getMethod("isRevoked") != null && ((Boolean) card.getClass().getMethod("isRevoked").invoke(card))) {
//                        status = "Revoked";
//                        displayArea.setText(cardStatus + "\nAccess Denied: This card is Revoked.");
//                        manager.logUsage(username, "Access Denied", cardID, "Revoked Card");
//                        JOptionPane.showMessageDialog(null, "Access Denied: This card is Revoked", "Access Denied", JOptionPane.ERROR_MESSAGE);
//                        return;
//                    }
//                } catch (Exception ex) {
//                    // ถ้าไม่มี isRevoked() ให้ทำงานปกติ
//                }
//
//                selectedRooms.add(cardUsageKey);
//
//                manager.setAuditTrail(new AuditTrails.RoomAudit());
//                manager.recordUsage(cardID, username, selectedFloor + " - " + selectedRoom, cardLevel);
//                manager.logUsage(username, "Access Granted", cardID, "Access to " + selectedFloor + " - " + selectedRoom);
//
//                JOptionPane.showMessageDialog(null, "Access to " + selectedFloor + " - " + selectedRoom + " granted.", "Access Granted", JOptionPane.INFORMATION_MESSAGE);
//                displayArea.setText(manager.getFullCardStatus(cardID));
//            }
//        });
        useCardButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText().trim();
                String cardID = (String) cardComboBox.getSelectedItem();
                String selectedFloor = (String) floorComboBox.getSelectedItem();
                String selectedRoom = (String) roomComboBox.getSelectedItem();
                String roomKey = selectedFloor + " - " + selectedRoom; // ห้องที่เลือก

                if (username.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please enter a Username!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (cardID == null) {
                    JOptionPane.showMessageDialog(null, "Please select a Card!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // ✅ ตรวจสอบว่าบัตรนี้เคยถูกใช้ไปแล้วหรือไม่
                if (selectedRooms.containsKey(cardID)) {
                    String previousRoom = selectedRooms.get(cardID);
                    JOptionPane.showMessageDialog(null, "This card has already been used for " + previousRoom + "!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (roomUsage.containsKey(roomKey)) {
                    String previousCard = roomUsage.get(roomKey);
                    JOptionPane.showMessageDialog(null, "This room is already assigned to another card (Card: " + previousCard + ")!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                AccessCard card = manager.getCard(cardID);
                if (card == null) {
                    displayArea.setText("Card not found.");
                    JOptionPane.showMessageDialog(null, "Card not found!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!card.getAccessLevel().equalsIgnoreCase("Guest")) {
                    JOptionPane.showMessageDialog(null, "Access Denied! Only Guest cards can use this feature.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                manager.logUsage(username, "Access Granted", cardID, "Access to " + selectedFloor + " - " + selectedRoom);
                JOptionPane.showMessageDialog(null, "Access to " + selectedFloor + " - " + selectedRoom + " granted.", "Access Granted", JOptionPane.INFORMATION_MESSAGE);
                displayArea.setText(manager.getFullCardStatus(cardID));


                String cardLevel = card.getAccessLevel();

                selectedRooms.put(cardID, roomKey);
                roomUsage.put(roomKey, cardID); // ✅ บันทึกว่าห้องนี้ใช้บัตรไหนแล้ว
                saveSelectedRooms(); // บันทึกข้อมูลลงไฟล์

                manager.setAuditTrail(new AuditTrails.RoomAudit());
                manager.recordUsage(cardID, username, roomKey, cardLevel);
                manager.logUsage(username, "Access Granted", cardID, "Access to " + roomKey);

                JOptionPane.showMessageDialog(null, "Access to " + roomKey + " granted.", "Access Granted", JOptionPane.INFORMATION_MESSAGE);
                displayArea.setText(manager.getFullCardStatus(cardID));
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
        add(roomLabel);
        add(roomComboBox);
        add(useCardButton);
        add(backButton);
        add(scrollPane);

        revalidate();
        repaint();
    }
    private final String SELECTED_ROOMS_FILE = "selected_rooms.txt"; // ไฟล์เก็บข้อมูล

    // ✅ ฟังก์ชันบันทึกข้อมูลลงไฟล์
    private void saveSelectedRooms() {
        try (FileWriter writer = new FileWriter(SELECTED_ROOMS_FILE)) {
            for (Map.Entry<String, String> entry : selectedRooms.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue() + "\n");
            }
            writer.write("###\n"); // แยกส่วนข้อมูล
            for (Map.Entry<String, String> entry : roomUsage.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error saving selected rooms: " + e.getMessage());
        }
    }

    // ✅ ฟังก์ชันโหลดข้อมูลจากไฟล์กลับมาใช้
    private void loadSelectedRooms() {
        try (BufferedReader reader = new BufferedReader(new FileReader(SELECTED_ROOMS_FILE))) {
            String line;
            boolean loadingRoomUsage = false;
            while ((line = reader.readLine()) != null) {
                if (line.equals("###")) {
                    loadingRoomUsage = true;
                    continue;
                }
                String[] data = line.split(",");
                if (data.length == 2) {
                    if (!loadingRoomUsage) {
                        selectedRooms.put(data[0], data[1]);
                    } else {
                        roomUsage.put(data[0], data[1]);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("No previous room usage data found. Starting fresh.");
        }
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
