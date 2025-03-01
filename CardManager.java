import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CardManager {
    private List<AccessCard> cards = new ArrayList<>();
    private final String FILE_NAME = "cards.txt";
    private final String AUDIT_FILE = "audit_log.txt";

    public CardManager() {
        loadFromFile();
    }

   public boolean grantAccess(String requiredLevel, String cardID, String customerName) {
        AccessCard card = getCard(cardID); // หาบัตรจาก ID
        if (card != null) {
            if (card.isUsed()) {
                logAction("Access Denied | Card " + cardID + " already used.");
                return false;
            }

            if (card.grantAccess(requiredLevel)) {
                card.setUsed(true);
                logAction("Access Granted | Card " + cardID + " used by customer: " + customerName);
                return true;
            } else {
                logAction("Access Denied | Card " + cardID + " insufficient access level.");
                return false;
            }
        }
        logAction("Access Denied | Card " + cardID + " not found.");
        return false;
    }

    private void logAction(String action) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(AUDIT_FILE, true))) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String timestamp = LocalDateTime.now().format(formatter);
            writer.write(timestamp + " | " + action + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            cardIDs.add(card.getCardID()); // สมมุติว่า AccessCard มีเมธอด getCardID()
        }
        return cardIDs.toArray(new String[0]); // แปลงเป็นอาเรย์ของ String
    }

    public void addCard(AccessCard card) {
        if (card.getCardID().equals("CARD004")) {
            System.out.println("Skipping Card 4");
            return;
        }
        cards.add(card);
        System.out.println("Added: " + card.getCardID() + " | Level: " + card.getAccessLevel());
        saveToFile();
        logUsage("SystemUser", "Added Card", card.getCardID(), card.getAccessLevel());
    }

    public void modifyCard(String cardID, String newLevel) {
        for (AccessCard card : cards) {
            if (card.getCardID().equals(cardID)) {
                String oldLevel = card.getAccessLevel();
                card.setAccessLevel(newLevel);
                System.out.println("Modified: " + cardID + " -> New Level: " + newLevel);
                saveToFile();
                logUsage("SystemUser", "Modified Card", cardID, oldLevel + " -> " + newLevel);
                return;
            }
        }
        System.out.println("Card not found!");
    }

    public void revokeCard(String cardID) {
        cards.removeIf(card -> {
            logUsage("SystemUser", "Revoked Card", card.getCardID(), card.getAccessLevel());
            return card.getCardID().equals(cardID);
        });
        System.out.println("Revoked: " + cardID);
        saveToFile();
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
                String cardID = data[0];
                String level = data[1];

                if (cardID.equals("CARD004")) {
                    System.out.println("Skipping Card 4 from file.");
                    continue;
                }

                // ใช้ Polymorphism เพื่อสร้างบัตรที่แตกต่างกันตาม level
                AccessCard card = null;
                switch (level) {
                    case "Low":
                        card = new GuestCard(cardID);  // GuestCard สำหรับระดับ Low
                        break;
                    case "Medium":
                        card = new StaffCard(cardID);  // StaffCard สำหรับระดับ Medium
                        break;
                    case "High":
                        card = new AdminCard(cardID);  // AdminCard สำหรับระดับ High
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

    private void logUsage(String userID, String action, String cardID, String accessLevel) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timestamp = LocalDateTime.now().format(formatter);

        String logEntry = timestamp + " | User: " + userID + " | Action: " + action + " | Card: " + cardID + " | Level: " + accessLevel;

        System.out.println(logEntry);

        try (FileWriter writer = new FileWriter(AUDIT_FILE, true)) {
            writer.write(logEntry + "\n");
        } catch (IOException e) {
            System.out.println("Error saving audit log: " + e.getMessage());
        }
    }

    public List<AccessCard> getCardList() {
        return cards;
    }

    public String getAllCards() {
        StringBuilder result = new StringBuilder();
        result.append("Registered Cards:\n");
        for (AccessCard card : cards) {
            result.append(card.toString()).append("\n");
        }
        return result.toString();
    }

    public String[] getLowAccessCardIDs() {
        // ใช้ cards แทน cardList
        if (cards == null || cards.isEmpty()) {
            return new String[0]; // ถ้า cards ไม่มีข้อมูล จะคืนค่าอาร์เรย์ว่าง
        }

        ArrayList<String> lowAccessCardIDs = new ArrayList<>();
        for (AccessCard card : cards) {
            if (card.getAccessLevel().equalsIgnoreCase("Low")) {
                lowAccessCardIDs.add(card.getCardID());
            }
        }
        return lowAccessCardIDs.toArray(new String[0]);
    }

}
