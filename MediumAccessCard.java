public class MediumAccessCard extends AccessCard {
    public MediumAccessCard(String cardID) {
        super(cardID, "Medium");
    }

    public boolean grantAccess(String requiredLevel) {
        return requiredLevel.equals("Low") || requiredLevel.equals("Medium");
    }

    public void showCardInfo() {
        System.out.println("Medium Access Card: " + cardID + " | Level: " + accessLevel);
    }
}