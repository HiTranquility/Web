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
}
