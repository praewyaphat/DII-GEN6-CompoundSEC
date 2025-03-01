public class AccessCard {
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

    public boolean grantAccess(String requiredLevel) {
        return requiredLevel.equalsIgnoreCase(accessLevel);
    }
}

class AdminCard extends AccessCard {
    public AdminCard(String cardID) {
        super(cardID, "High"); // Admins have the highest level of access by default
    }

    @Override
    public boolean grantAccess(String requiredLevel) {
        return true; // Admin can access any level
    }
}

class GuestCard extends AccessCard {
    public GuestCard(String cardID) {
        super(cardID, "Low"); // Guest has the lowest level of access by default
    }

    @Override
    public boolean grantAccess(String requiredLevel) {
        return requiredLevel.equalsIgnoreCase("Low"); // Guest can only access Low level
    }
}

class StaffCard extends AccessCard {
    public StaffCard(String cardID) {
        super(cardID, "Medium"); // Staff have medium level of access by default
    }

    @Override
    public boolean grantAccess(String requiredLevel) {
        return requiredLevel.equalsIgnoreCase("Low") || requiredLevel.equalsIgnoreCase("Medium");
    }
}
