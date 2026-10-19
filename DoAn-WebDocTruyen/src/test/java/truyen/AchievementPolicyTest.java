package truyen;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import truyen.dao.AchievementDAO;
import truyen.model.Achievement;
import truyen.util.AchievementPolicy;
import static org.junit.jupiter.api.Assertions.*;

class AchievementPolicyTest {
    private Achievement badge(List<Achievement> list, String key) {
        return list.stream().filter(a -> key.equals(a.getId())).findFirst().orElseThrow();
    }

    @Test void readingThresholdsAreDistinctAndNotBasedOnTotalPageViews() {
        Map<String, Integer> stats = new HashMap<>(); stats.put("read_count", 29);
        assertFalse(badge(AchievementPolicy.catalog(stats, false), "bookworm").isUnlocked());
        stats.put("read_count", 30);
        assertTrue(badge(AchievementPolicy.catalog(stats, false), "bookworm").isUnlocked());
        assertFalse(badge(AchievementPolicy.catalog(stats, false), "scholar").isUnlocked());
    }

    @Test void paidVipDoesNotGrantReputationOrMilestoneCoins() {
        List<Achievement> list = AchievementPolicy.catalog(Collections.emptyMap(), true);
        Achievement vip = badge(list, "vip_patron");
        assertTrue(vip.isUnlocked()); assertFalse(vip.isClaimable());
        assertEquals(0, AchievementPolicy.dailyBonus(list));
        assertEquals(0, AchievementPolicy.recommendationBoost(list));
    }

    @Test void authorAchievementsHaveMoreReputationWeightThanReaderBadges() {
        List<Achievement> list = AchievementPolicy.catalog(Collections.emptyMap(), false);
        assertTrue(badge(list, "first_story").getRecommendationWeight() > badge(list, "bookworm").getRecommendationWeight());
        assertTrue(badge(list, "finished_author").getRecommendationWeight() > badge(list, "first_story").getRecommendationWeight());
    }

    @Test void bonusesHaveHardCapsRegardlessOfNumberOfBadges() {
        Map<String, Integer> stats = new HashMap<>();
        for (String key : new String[] { "night_reads", "read_count", "tip_count", "review_count", "max_streak",
                "bookmark_count", "published_count", "published_chapters", "completed_count" }) stats.put(key, 999);
        List<Achievement> list = AchievementPolicy.catalog(stats, true);
        assertEquals(20, AchievementPolicy.dailyBonus(list));
        assertEquals(10, AchievementPolicy.recommendationBoost(list));
    }

    @Test void badgeBonusIsAddedOnBaseNotMultipliedByVip() {
        assertEquals(22, AchievementPolicy.dailyReward(10, 2, 20));
        assertEquals(11, AchievementPolicy.dailyReward(10, 1, 10));
        assertEquals(18, AchievementPolicy.dailyReward(15, 1, 20));
        assertEquals(0, AchievementPolicy.dailyReward(0, 2, 20));
        assertEquals(12, AchievementPolicy.dailyReward(10, Double.NaN, 999));
    }

    @Test void claimedMilestoneNeverRemainsClaimable() {
        Achievement a = badge(AchievementPolicy.catalog(Collections.singletonMap("read_count", 30), false), "bookworm");
        assertTrue(a.isClaimable()); a.setClaimed(true); assertFalse(a.isClaimable());
    }

    @Test void unknownOrInvalidClaimsDoNotReachDatabase() throws Exception {
        assertFalse(new AchievementDAO().claim(0, "bookworm"));
        assertFalse(new AchievementDAO().claim(12, null));
        assertFalse(new AchievementDAO().claim(12, "arbitrary-key"));
    }
}
