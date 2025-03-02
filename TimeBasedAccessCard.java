import java.time.LocalDateTime;

public class TimeBasedAccessCard extends AccessCard {
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public TimeBasedAccessCard(String cardID, String accessLevel, LocalDateTime startTime, LocalDateTime endTime) {
        super(cardID, accessLevel);
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean isCardExpired() {
        return LocalDateTime.now().isAfter(endTime);
    }

    @Override
    public boolean grantAccess(String requiredLevel) {
        if (isCardExpired()) {
            System.out.println("Access Denied: Card " + getCardID() + " is expired.");
            return false;
        }
        return super.grantAccess(requiredLevel);
    }

    // ✅ เพิ่ม Setter เพื่อให้สามารถแก้ไขเวลาได้
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
}
