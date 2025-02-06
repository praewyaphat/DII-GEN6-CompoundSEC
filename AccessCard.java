public abstract class AccessCard {
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

    public abstract boolean grantAccess(String requiredLevel);

    public abstract void showCardInfo();
}
