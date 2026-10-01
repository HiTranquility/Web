package truyen.model;

import java.io.Serializable;

/**
 * Nhiệm vụ hàng ngày nhận xu (Gamification).
 */
public class DailyQuest implements Serializable {

    private String key;
    private String title;
    private String description;
    private int rewardCoins;
    private int targetCount;
    private int currentCount;
    private boolean completed;
    private boolean claimed;

    public DailyQuest() {}

    public DailyQuest(String key, String title, String description, int rewardCoins, int targetCount, int currentCount, boolean claimed) {
        this.key = key;
        this.title = title;
        this.description = description;
        this.rewardCoins = rewardCoins;
        this.targetCount = targetCount;
        this.currentCount = currentCount;
        this.completed = currentCount >= targetCount;
        this.claimed = claimed;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getRewardCoins() { return rewardCoins; }
    public void setRewardCoins(int rewardCoins) { this.rewardCoins = rewardCoins; }

    public int getTargetCount() { return targetCount; }
    public void setTargetCount(int targetCount) { this.targetCount = targetCount; }

    public int getCurrentCount() { return currentCount; }
    public void setCurrentCount(int currentCount) {
        this.currentCount = currentCount;
        this.completed = currentCount >= this.targetCount;
    }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public boolean isClaimed() { return claimed; }
    public void setClaimed(boolean claimed) { this.claimed = claimed; }
}
