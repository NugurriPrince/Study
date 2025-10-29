// 파일 이름: RentalRecord.java
import java.io.Serializable;
import java.time.LocalDateTime;

public class RentalRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    private final User user; private final String itemName; private final LocalDateTime rentalTime; private LocalDateTime returnTime;
    public RentalRecord(User user, String itemName) { this.user = user; this.itemName = itemName; this.rentalTime = LocalDateTime.now(); this.returnTime = null; }
    public void markAsReturned() { this.returnTime = LocalDateTime.now(); }
    public User getUser() { return user; } public String getItemName() { return itemName; } public LocalDateTime getRentalTime() { return rentalTime; } public LocalDateTime getReturnTime() { return returnTime; }
}