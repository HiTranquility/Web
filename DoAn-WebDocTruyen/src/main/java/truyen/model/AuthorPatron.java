package truyen.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Độc giả ủng hộ nhiều nhất cho tác giả (Bảng Vàng Fan Cứng / Top Patrons).
 */
public class AuthorPatron implements Serializable {

    private int userId;
    private String username;
    private String displayName;
    private String avatarUrl;
    private int totalAmount;
    private int donationCount;
    private LocalDateTime lastDonatedAt;

    public AuthorPatron() { }

    public AuthorPatron(int userId, String username, String displayName, String avatarUrl,
                        int totalAmount, int donationCount, LocalDateTime lastDonatedAt) {
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.totalAmount = totalAmount;
        this.donationCount = donationCount;
        this.lastDonatedAt = lastDonatedAt;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public int getTotalAmount() { return totalAmount; }
    public void setTotalAmount(int totalAmount) { this.totalAmount = totalAmount; }

    public int getDonationCount() { return donationCount; }
    public void setDonationCount(int donationCount) { this.donationCount = donationCount; }

    public LocalDateTime getLastDonatedAt() { return lastDonatedAt; }
    public void setLastDonatedAt(LocalDateTime lastDonatedAt) { this.lastDonatedAt = lastDonatedAt; }

    public String getName() {
        if (displayName != null && !displayName.trim().isEmpty()) {
            return displayName.trim();
        }
        return username != null ? username : "Độc giả";
    }

    public String getInitial() {
        String n = getName();
        if (n != null && !n.isEmpty()) {
            return n.substring(0, 1).toUpperCase();
        }
        return "U";
    }
}
