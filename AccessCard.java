import java.time.LocalDateTime;

public class AccessCard implements AccessControl{
    protected String cardID;
    protected String accessLevel;
    private boolean used;
    private boolean revoked = false;

    public AccessCard(String cardID, String accessLevel) {
        this.cardID = cardID;
        this.accessLevel = accessLevel;
        this.used = false;
    }

    public String getCardID() {
        return cardID;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void revokeCard() {
        this.revoked = true;
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String newLevel) {
        this.accessLevel = newLevel;
    }

    public String getLevel() {
        return this.accessLevel;
    }

    @Override
    public boolean grantAccess(String requiredLevel) {
        if (used) {
            return false;
        }
        if (requiredLevel.equalsIgnoreCase(accessLevel)) {
            used = true;
            return true;
        }
        return false;
    }
}

class AdminCard extends AccessCard {
    public AdminCard(String cardID) {
        super(cardID, "High");
    }

    @Override
    public boolean grantAccess(String requiredLevel) {
        return true;
    }
}

class GuestCard extends AccessCard {
    public GuestCard(String cardID) {
        super(cardID, "Low");
    }

    @Override
    public boolean grantAccess(String requiredLevel) {
        return requiredLevel.equalsIgnoreCase("Low");
    }
}

class StaffCard extends AccessCard {
    public StaffCard(String cardID) {
        super(cardID, "Medium");
    }

    @Override
    public boolean grantAccess(String requiredLevel) {
        return requiredLevel.equalsIgnoreCase("Low") || requiredLevel.equalsIgnoreCase("Medium");
    }
}
