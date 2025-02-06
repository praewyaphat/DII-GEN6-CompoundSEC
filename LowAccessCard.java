public class LowAccessCard extends AccessCard {
    public LowAccessCard(String cardID) {
        super(cardID, "Low");
    }

    public boolean grantAccess(String requiredLevel) {
        return requiredLevel.equals("Low");
    }

    public void showCardInfo() {
        System.out.println("Low Access Card: " + cardID + " | Level: " + accessLevel);
    }
}