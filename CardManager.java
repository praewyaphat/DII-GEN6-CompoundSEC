import javax.swing.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CardManager {
    private List<AccessCard> cards = new ArrayList<>();
    private final String FILE_NAME = "cards.txt";
    private final String AUDIT_FILE = "audit_log.txt";
    private Map<String, List<String>> usageHistory = new HashMap<>();
    private Map<String, String> selectedRooms = new HashMap<>();
    private AuditTrails.AuditTrail auditTrail;

    // singeleton pattern//
    private static CardManager instance;

    CardManager() {
        loadFromFile();
    }

    public static CardManager getInstance() {
        if (instance == null) {
            instance = new CardManager();
        }
        return instance;
    }


    public void setAuditTrail(AuditTrails.AuditTrail auditTrail) {
        this.auditTrail = auditTrail;
    }

    public void addCard(AccessCard card) {
        cards.add(card);
        saveToFile();
        logUsage("Admin", "Added Card", card.getCardID(), card.getAccessLevel());
    }

    public void modifyCard(String cardID, String newLevel) {
        for (AccessCard card : cards) {
            if (card.getCardID().equals(cardID)) {
                String oldLevel = card.getAccessLevel();
                card.setAccessLevel(newLevel);
                saveToFile();
                logUsage("Admin", "Modified Card", cardID, oldLevel + " -> " + newLevel);
                return;
            }
        }
        System.out.println("Card not found!");
    }

    public void revokeCard(String cardID) {
        cards.removeIf(card -> {
            logUsage("Admin", "Revoked Card", card.getCardID(), card.getAccessLevel());
            return card.getCardID().equals(cardID);
        });
        saveToFile();
    }

    public AccessCard getCard(String cardID) {
        for (AccessCard card : cards) {
            if (card.getCardID().equals(cardID)) {
                return card;
            }
        }
        return null;
    }

    public String[] getCardIDs() {
        List<String> cardIDs = new ArrayList<>();
        for (AccessCard card : cards) {
            cardIDs.add(card.getCardID());
        }
        return cardIDs.toArray(new String[0]);
    }

    public String getAllCards() {
        StringBuilder result = new StringBuilder();
        result.append("Registered Cards:\n");
        for (AccessCard card : cards) {
            if (card instanceof TimeBasedAccessCard) {
                TimeBasedAccessCard tbc = (TimeBasedAccessCard) card;
                result.append("Card ID: ").append(card.getCardID())
                        .append(" | Level: ").append(card.getAccessLevel())
                        .append(" | Expires On: ").append(tbc.getEndTime())
                        .append(tbc.isCardExpired() ? " | Status: Expired\n" : " | Status: Active\n");
            } else {
                result.append(card.getCardID()).append(" | Level: ").append(card.getAccessLevel()).append("\n");
            }
        }
        return result.toString();
    }


    public void logUsage(String username, String action, String cardID, String detail) {
        if (auditTrail != null) {
            auditTrail.log(username, action, cardID, detail);
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String timestamp = LocalDateTime.now().format(formatter);
            String logEntry = String.format("%s | User: %s | Action: %s | Card: %s | Detail: %s%n",
                    timestamp, username, action, cardID, detail);
            try (FileWriter writer = new FileWriter(AUDIT_FILE, true)) {
                writer.write(logEntry);
            } catch (IOException e) {
                System.out.println("Error saving audit log: " + e.getMessage());
            }
        }
    }

    public void saveToFile() {
        try (FileWriter writer = new FileWriter(FILE_NAME)) {
            for (AccessCard card : cards) {
                if (card instanceof TimeBasedAccessCard) {
                    TimeBasedAccessCard tbc = (TimeBasedAccessCard) card;
                    writer.write(card.getCardID() + "," + card.getAccessLevel() + "," +
                            tbc.getStartTime() + "," + tbc.getEndTime() + "\n");
                } else {
                    writer.write(card.getCardID() + "," + card.getAccessLevel() + "\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    public void loadFromFile() {
        cards.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                String cardID = data[0];
                String level = data[1];

                if (data.length == 4) {  // เป็นบัตรแบบ Time-Based
                    LocalDateTime startTime = LocalDateTime.parse(data[2]);
                    LocalDateTime endTime = LocalDateTime.parse(data[3]);
                    cards.add(new TimeBasedAccessCard(cardID, level, startTime, endTime));
                } else {
                    cards.add(new AccessCard(cardID, level));
                }
            }
            System.out.println("Loaded data from file.");
        } catch (IOException e) {
            System.out.println("No previous data found. Starting fresh.");
        }
    }

    public void recordUsage(String cardID, String username, String floorRoom, String cardLevel) {
        String record = "Card ID: " + cardID + " | Floor & Room: " + floorRoom + " | Card Level: " + cardLevel + " | Username: " + username;
        List<String> records = usageHistory.getOrDefault(cardID, new ArrayList<>());
        records.add(record);
        usageHistory.put(cardID, records);
    }

    public String getFullCardStatus(String selectedCardID) {
        StringBuilder sb = new StringBuilder();
        AccessCard card = getCard(selectedCardID);
        if (card == null) {
            return "Card not found.";
        }

        // ✅ สำหรับ Card Information ให้ดึงข้อมูลของบัตรที่เลือก
        String currentUsername = "Not used";
        String currentFloorRoom = "Not assigned";
        List<String> recordsForCard = usageHistory.get(selectedCardID);

        if (recordsForCard != null && !recordsForCard.isEmpty()) {
            String latestRecord = recordsForCard.get(recordsForCard.size() - 1);
            String[] parts = latestRecord.split("Username: ");
            if (parts.length == 2) {
                currentUsername = parts[1].trim();
            }
        }

        sb.append("Card Information:\n");
        sb.append("Card ID: ").append(card.getCardID())
                .append(" | Card Level: ").append(card.getAccessLevel())
                .append(" | Username: ").append(currentUsername);

        // ✅ ถ้าเป็น Time-Based Access Card ให้เพิ่ม Expiry Date
        if (card instanceof TimeBasedAccessCard) {
            TimeBasedAccessCard tbc = (TimeBasedAccessCard) card;
            sb.append(" | Expires On: ")
                    .append(tbc.getEndTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }

        sb.append("\n\nAccess History:\n");

        // ✅ สำหรับ Access History ให้แสดงประวัติของระบบทั้งหมด
        for (Map.Entry<String, List<String>> entry : usageHistory.entrySet()) {
            for (String rec : entry.getValue()) {
                sb.append(rec);
                if (card instanceof TimeBasedAccessCard) {
                    TimeBasedAccessCard tbc = (TimeBasedAccessCard) card;
                    sb.append(" | Expires On: ")
                            .append(tbc.getEndTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }


    public String getCardStatus(String cardID) {
        AccessCard card = getCard(cardID);
        if (card == null) {
            return "Card not found.";
        }
        if (card instanceof TimeBasedAccessCard) {
            TimeBasedAccessCard tbc = (TimeBasedAccessCard) card;
            return "Card ID: " + cardID + " | Level: " + card.getAccessLevel() +
                    " | Expires On: " + tbc.getEndTime() +
                    (tbc.isCardExpired() ? " | Status: Expired" : " | Status: Active");
        }
        return "Card ID: " + cardID + " | Level: " + card.getAccessLevel() + " | Status: Active";
    }

// Observer  Pattern //
    public void modifyCardWithTime(String cardID, String newLevel, LocalDateTime newStartTime, LocalDateTime newEndTime) {
        for (AccessCard card : cards) {
            if (card.getCardID().equals(cardID)) {
                if (card instanceof TimeBasedAccessCard) {
                    TimeBasedAccessCard tbc = (TimeBasedAccessCard) card;
                    tbc.setAccessLevel(newLevel);
                    tbc.setStartTime(newStartTime);
                    tbc.setEndTime(newEndTime);
                    saveToFile();
                    logUsage("Admin", "Modified Card Time", cardID, "New Time: " + newStartTime + " - " + newEndTime);
                    return;
                } else {
                    JOptionPane.showMessageDialog(null, "This card does not support time-based access!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }
        JOptionPane.showMessageDialog(null, "Card not found!", "Error", JOptionPane.ERROR_MESSAGE);
    }


}

