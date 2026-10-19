package truyen.model;

import java.io.Serializable;

/**
 * Model biểu diễn một Huy hiệu Thành tích Độc giả (Reader Achievement Badge).
 */
public class Achievement implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String icon;
    private String category;
    private String description;
    private boolean unlocked;
    private int currentProgress;
    private int targetProgress;
    private String progressText;
    private int progressPercent;
    private int bonusPercent;
    private int recommendationWeight;
    private int rewardCoins;
    private boolean claimed;

    public int getBonusPercent() { return bonusPercent; }
    public void setBonusPercent(int value) { bonusPercent = value; }
    public int getRecommendationWeight() { return recommendationWeight; }
    public void setRecommendationWeight(int value) { recommendationWeight = value; }
    public int getRewardCoins() { return rewardCoins; }
    public void setRewardCoins(int value) { rewardCoins = value; }
    public boolean isClaimed() { return claimed; }
    public void setClaimed(boolean value) { claimed = value; }
    public boolean isClaimable() { return unlocked && !claimed && rewardCoins > 0; }

    public Achievement() {}

    public Achievement(String id, String title, String icon, String category,
                       String description, boolean unlocked,
                       int currentProgress, int targetProgress, String progressText) {
        this.id = id;
        this.title = title;
        this.icon = icon;
        this.category = category;
        this.description = description;
        this.unlocked = unlocked;
        this.currentProgress = currentProgress;
        this.targetProgress = targetProgress;
        this.progressText = progressText;
        if (targetProgress > 0) {
            this.progressPercent = Math.min(100, (int) Math.round((currentProgress * 100.0) / targetProgress));
        } else {
            this.progressPercent = unlocked ? 100 : 0;
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }

    public int getCurrentProgress() { return currentProgress; }
    public void setCurrentProgress(int currentProgress) { this.currentProgress = currentProgress; }

    public int getTargetProgress() { return targetProgress; }
    public void setTargetProgress(int targetProgress) { this.targetProgress = targetProgress; }

    public String getProgressText() { return progressText; }
    public void setProgressText(String progressText) { this.progressText = progressText; }

    public int getProgressPercent() { return progressPercent; }
    public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }
}
