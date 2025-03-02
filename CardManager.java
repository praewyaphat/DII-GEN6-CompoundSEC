import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardManager {
    private List<AccessCard> cards = new ArrayList<>();
    private final String FILE_NAME = "cards.txt";
    private final String AUDIT_FILE = "audit_log.txt";
    private Map<String, String> usageDetails = new HashMap<>();
    private Map<String, List<String>> usageHistory = new HashMap<>();
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
                logUsage("Admin", "Modified Card", cardID, oldLevel + " -> " + newLevel); // ✅ ใช้ SYSTEM
                return;
            }
        }
        System.out.println("Card not found!");
    }

    public void revokeCard(String cardID) {
        cards.removeIf(card -> {
            logUsage("Admin", "Revoked Card", card.getCardID(), card.getAccessLevel()); // ✅ ใช้ SYSTEM
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

    private void logAction(String username, String action, String cardID, String floor) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(AUDIT_FILE, true))) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String timestamp = LocalDateTime.now().format(formatter);
            String logEntry = String.format("%s | User: %s | Action: %s | Card: %s | Floor: %s%n",
                    timestamp, username, action, cardID, floor);
            writer.write(logEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void logUsage(String username, String action, String cardID, String accessLevel) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = String.format("%s | User: %s | Action: %s | Card: %s | Level: %s%n",
                timestamp, username, action, cardID, accessLevel);

        System.out.println(logEntry);

        try (FileWriter writer = new FileWriter(AUDIT_FILE, true)) {
            writer.write(logEntry);
        } catch (IOException e) {
            System.out.println("Error saving audit log: " + e.getMessage());
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

    public List<AccessCard> getCardList() {
        return cards;
    }

    public String getAllCards() {
        StringBuilder result = new StringBuilder();
        result.append("Registered Cards:\n");
        for (AccessCard card : cards) {
            result.append("Card ID: ").append(card.getCardID()).append(" | Level: ").append(card.getAccessLevel()).append("\n");
        }
        return result.toString();
    }

    public String[] getLowAccessCardIDs() {
        if (cards.isEmpty()) {
            return new String[0];
        }

        ArrayList<String> lowAccessCardIDs = new ArrayList<>();
        for (AccessCard card : cards) {
            if (card.getAccessLevel().equalsIgnoreCase("Low")) {
                lowAccessCardIDs.add(card.getCardID());
            }
        }
        return lowAccessCardIDs.toArray(new String[0]);
    }

    public String getCardStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("Card Status:\n");
        for (AccessCard card : cards) {
            sb.append("Card ID: ").append(card.getCardID())
                    .append(" | Level: ").append(card.getAccessLevel());
            if (usageDetails.containsKey(card.getCardID())) {
                sb.append(" | ").append(usageDetails.get(card.getCardID()));
            } else {
                sb.append(" | Not used");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    public String getCardStatus(String cardID) {
        StringBuilder sb = new StringBuilder();
        AccessCard card = getCard(cardID);
        if (card == null) {
            return "Card not found.";
        }
        sb.append("Card Information:\n");
        List<String> records = usageHistory.get(cardID);
        String info;
        if (records != null && !records.isEmpty()) {
            // ("Card ID: a11 | Floor: Floor 1 | Card Level: Low | Username: praew")
            String latestRecord = records.get(records.size() - 1);
            String[] parts = latestRecord.split("\\|");
            // parts[0] = "Card ID: a11 "
            // parts[1] = " Floor: Floor 1 "
            // parts[2] = " Card Level: Low "
            // parts[3] = " Username: praew"
            info = parts[0].trim() + " | " + parts[2].trim() + " | " + parts[3].trim();
        } else {
            info = "Card ID: " + card.getCardID() + " | Card Level: " + card.getAccessLevel() + " | Username: Not used";
        }
        sb.append(info).append("\n\n");
        sb.append("Access History:\n");
        if (records != null && !records.isEmpty()) {
            for (String rec : records) {
                sb.append(rec).append("\n");
            }
        } else {
            sb.append("No access history.\n");
        }
        return sb.toString();
    }

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

        String currentUsername = "Not used";
        List<String> records = usageHistory.get(selectedCardID);
        if (records != null && !records.isEmpty()) {
            String latestRecord = records.get(records.size() - 1);
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

        sb.append("Access History:\n");
        for (List<String> recList : usageHistory.values()) {
            for (String rec : recList) {
                sb.append(rec).append("\n");
            }
        }
        return sb.toString();
    }

}
