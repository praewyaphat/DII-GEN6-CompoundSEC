// Strategy Pattern //
public class AuditTrails {

    public static abstract class AuditTrail {
        protected final String AUDIT_FILE = "audit_log.txt";
        protected java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Abstract method สำหรับบันทึก Audit Trail
        public abstract void log(String username, String action, String cardID, String detail);
    }

    // สำหรับบันทึกการเข้าถึงในระดับชั้น (Floor)
    public static class FloorAudit extends AuditTrail {
        @Override
        public void log(String username, String action, String cardID, String floor) {
            String timestamp = java.time.LocalDateTime.now().format(formatter);
            String logEntry = String.format("%s | User: %s | Action: %s | Card: %s | Floor: %s%n",
                    timestamp, username, action, cardID, floor);
            try (java.io.FileWriter writer = new java.io.FileWriter(AUDIT_FILE, true)) {
                writer.write(logEntry);
            } catch (java.io.IOException e) {
                System.out.println("Error saving floor audit log: " + e.getMessage());
            }
        }
    }

    public static class RoomAudit extends AuditTrail {
        @Override
        public void log(String username, String action, String cardID, String floorRoom) {
            String timestamp = java.time.LocalDateTime.now().format(formatter);
            String logEntry = String.format("%s | User: %s | Action: %s | Card: %s | Location: %s%n",
                    timestamp, username, action, cardID, floorRoom);
            try (java.io.FileWriter writer = new java.io.FileWriter(AUDIT_FILE, true)) {
                writer.write(logEntry);
            } catch (java.io.IOException e) {
                System.out.println("Error saving room audit log: " + e.getMessage());
            }
        }
    }
}
