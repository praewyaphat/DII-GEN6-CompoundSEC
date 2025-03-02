import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CardManager {
    private List<AccessCard> cards = new ArrayList<>();
    private final String FILE_NAME = "cards.txt";
    private final String AUDIT_FILE = "audit_log.txt";
    private Map<String, List<String>> usageHistory = new HashMap<>();
    // ตัวแปรสำหรับอ้างอิง AuditTrail (FloorAudit หรือ RoomAudit)
    private AuditTrails.AuditTrail auditTrail;

    public CardManager() {
        loadFromFile();
    }

    public void setAuditTrail(AuditTrails.AuditTrail auditTrail) {
        this.auditTrail = auditTrail;
    }

    public void addCard(AccessCard card) {
        if (card.getCardID().equals("CARD004")) {
            System.out.println("Skipping Card 4");
            return;
        }
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

    // เพิ่มเมธอด getAllCards() เพื่อแสดงรายชื่อบัตรทั้งหมด
    public String getAllCards() {
        StringBuilder result = new StringBuilder();
        result.append("Registered Cards:\n");
        for (AccessCard card : cards) {
            result.append("Card ID: ").append(card.getCardID())
                    .append(" | Level: ").append(card.getAccessLevel()).append("\n");
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
            System.out.println(logEntry);
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
                writer.write(card.getCardID() + "," + card.getAccessLevel() + "\n");
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
                if (data.length < 2) continue; // ป้องกันข้อมูลผิดพลาด
                String cardID = data[0];
                String level = data[1];
                if (cardID.equals("CARD004")) {
                    System.out.println("Skipping Card 4 from file.");
                    continue;
                }
                AccessCard card = null;
                switch (level) {
                    case "Low":
                        card = new GuestCard(cardID);
                        break;
                    case "Medium":
                        card = new StaffCard(cardID);
                        break;
                    case "High":
                        card = new AdminCard(cardID);
                        break;
                }
                if (card != null) {
                    cards.add(card);
                }
            }
            System.out.println("Loaded data from file.");
        } catch (IOException e) {
            System.out.println("No previous data found. Starting fresh.");
        }
    }

    // บันทึกประวัติการใช้บัตร (usage history)
    public void recordUsage(String cardID, String username, String selectedFloor, String cardLevel) {
        String record = "Card ID: " + cardID + "-" + selectedFloor + " | Card Level: " + cardLevel + " | Username: " + username;
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
        // สำหรับ Card Information ให้ดึงข้อมูลของบัตรที่เลือก
        String currentUsername = "Not used";
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
                .append(" | Username: ").append(currentUsername)
                .append("\n\n");
        // สำหรับ Access History ให้แสดงประวัติของระบบทั้งหมด
        sb.append("Access History:\n");
        for (Map.Entry<String, List<String>> entry : usageHistory.entrySet()) {
            for (String rec : entry.getValue()) {
                sb.append(rec).append("\n");
            }
        }
        return sb.toString();
    }
}

