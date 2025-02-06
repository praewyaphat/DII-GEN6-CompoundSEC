public class HighAccessCard extends AccessCard {
    public HighAccessCard(String cardID) {
        super(cardID, "High");
    }

    public boolean grantAccess(String requiredLevel) {
        return true; // เข้าได้ทุกที่
    }

    public void showCardInfo() {
        System.out.println("High Access Card: " + cardID + " | Level: " + accessLevel);
    }
}