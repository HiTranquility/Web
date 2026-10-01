package truyen.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Điểm danh hàng ngày nhận xu (Gamification).
 */
public class DailyCheckin implements Serializable {

    private int id;
    private int userId;
    private LocalDate checkinDate;
    private int streakDays;
    private int rewardCoins;
    private LocalDateTime createdAt;

    public DailyCheckin() {}

    public DailyCheckin(int userId, LocalDate checkinDate, int streakDays, int rewardCoins) {
        this.userId = userId;
        this.checkinDate = checkinDate;
        this.streakDays = streakDays;
        this.rewardCoins = rewardCoins;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public LocalDate getCheckinDate() { return checkinDate; }
    public void setCheckinDate(LocalDate checkinDate) { this.checkinDate = checkinDate; }

    public int getStreakDays() { return streakDays; }
    public void setStreakDays(int streakDays) { this.streakDays = streakDays; }

    public int getRewardCoins() { return rewardCoins; }
    public void setRewardCoins(int rewardCoins) { this.rewardCoins = rewardCoins; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
