package truyen.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Đại diện cho ví xu ảo của người dùng (ISSUE-008).
 * Bảng: wallets.
 */
public class Wallet implements Serializable {

    private static final long serialVersionUID = 1L;

    private int userId;
    private int balance;
    private LocalDateTime updatedAt;

    // Các trường mở rộng hiển thị cho Quản trị viên (ISSUE-023)
    private String username;
    private String displayName;
    private String email;
    private String avatarUrl;
    private int totalReceived;
    private int totalSpent;

    public Wallet() {
    }

    public Wallet(int userId, int balance) {
        this.userId = userId;
        this.balance = balance;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName != null && !displayName.isEmpty() ? displayName : username;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public int getTotalReceived() {
        return totalReceived;
    }

    public void setTotalReceived(int totalReceived) {
        this.totalReceived = totalReceived;
    }

    public int getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(int totalSpent) {
        this.totalSpent = totalSpent;
    }
}
