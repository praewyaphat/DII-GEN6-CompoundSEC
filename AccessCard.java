public class AccessCard {
    protected String cardID;
    protected String accessLevel;
    private boolean used; // สถานะการใช้บัตร

    public AccessCard(String cardID, String accessLevel) {
        this.cardID = cardID;
        this.accessLevel = accessLevel;
        this.used = false; // เริ่มต้นการ์ดยังไม่ถูกใช้
    }

    public String getCardID() {
        return cardID;
    }

    public boolean isUsed() {
        return used; // คืนค่าสถานะว่าใช้บัตรแล้วหรือยัง
    }

    public void setUsed(boolean used) {
        this.used = used; // กำหนดสถานะการใช้บัตร
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String newLevel) {
        this.accessLevel = newLevel;
    }

    public String getLevel() {
        return this.accessLevel;  // คืนค่าระดับของการ์ด
    }


    public boolean grantAccess(String requiredLevel) {
        if (used) {
            return false; // ถ้าบัตรถูกใช้ไปแล้ว ไม่อนุญาตให้ใช้
        }

        if (requiredLevel.equalsIgnoreCase(accessLevel)) {
            used = true; // ตั้งค่าสถานะว่าบัตรถูกใช้แล้ว
            return true; // อนุญาตการเข้าถึง
        }

        return false;
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