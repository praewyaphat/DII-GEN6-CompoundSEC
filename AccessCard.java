import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

abstract class AccessCard {
    protected String cardID;
    protected String accessLevel;

    public AccessCard(String cardID, String accessLevel) {
        this.cardID = cardID;
        this.accessLevel = accessLevel;
    }

    public String getCardID() {
        return cardID;
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String newLevel) {
        this.accessLevel = newLevel;
    }

    public String toString() {
        return cardID + " - " + accessLevel;
    }

    public void logUsage(String userID, String location) {
        String logEntry = "[" + LocalDateTime.now() + "] User: " + userID +
                " | Card: " + cardID + " | Level: " + accessLevel +
                " | Location: " + location;

        System.out.println(logEntry);

        try (FileWriter writer = new FileWriter("audit_log.txt", true)) {
            writer.write(logEntry + "\n");
        } catch (IOException e) {
            System.out.println("Error saving audit log: " + e.getMessage());
        }
    }

    public boolean grantAccess(String requiredLevel) {
        if (requiredLevel.equalsIgnoreCase(accessLevel)) {
            return true;
        }
        return false;
    }

    public void showCardInfo() {
        System.out.println("Card ID: " + cardID + " | Access Level: " + accessLevel);
    }
}