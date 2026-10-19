package truyen.model;

import java.time.LocalDate;

/** Earned rewards, distinct from spendable wallet balance and recommendation weight. */
public final class RewardSummary {
    private final LocalDate date;
    private final long todayEarned;
    private final long totalEarned;

    public RewardSummary(LocalDate date, long todayEarned, long totalEarned) {
        this.date = date;
        this.todayEarned = todayEarned;
        this.totalEarned = totalEarned;
    }

    public LocalDate getDate() { return date; }
    public long getTodayEarned() { return todayEarned; }
    public long getTotalEarned() { return totalEarned; }
}
