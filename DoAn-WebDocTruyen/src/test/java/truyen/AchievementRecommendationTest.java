package truyen;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import truyen.model.RecommendationSet;
import truyen.model.Story;
import static org.junit.jupiter.api.Assertions.*;

class AchievementRecommendationTest {
    private Story story(int id, int author) {
        Story s = new Story(); s.setId(id); s.setAuthorId(author); s.setStatus("PUBLISHED"); s.setChapterCount(1); return s;
    }
    private RecommendationSet rank(int limit, List<List<Story>> sources, Map<Integer, Integer> boosts, List<Story> newcomers) {
        return RecommendationSet.ranked(99, limit, sources, Arrays.asList("Also", "Similar", "Trending"), boosts, newcomers);
    }

    @Test void moreBadgesImproveOrderingWithinRelevantCandidates() {
        Story first = story(1, 1), second = story(2, 2);
        RecommendationSet result = rank(2, Collections.singletonList(Arrays.asList(first, second)),
                Collections.singletonMap(2, 5), Collections.emptyList());
        assertEquals(2, result.getStories().get(0).getId());
    }

    @Test void cappedReputationCannotReplaceStrongRelevanceWithTrending() {
        Story relevant = story(1, 1), trending = story(2, 2);
        RecommendationSet result = rank(1, Arrays.asList(Collections.singletonList(relevant), Collections.emptyList(), Collections.singletonList(trending)),
                Collections.singletonMap(2, 999), Collections.emptyList());
        assertEquals(1, result.getStories().get(0).getId());
    }

    @Test void discoverySlotRemainsAvailableForNewAuthorWithNoBadges() {
        Story newcomer = story(7, 7);
        RecommendationSet result = rank(4, Collections.singletonList(Arrays.asList(story(1, 1), story(2, 2), story(3, 3), story(4, 4))),
                Map.of(1, 10, 2, 10, 3, 10, 4, 10), Collections.singletonList(newcomer));
        assertEquals(4, result.size()); assertEquals(7, result.getStories().get(3).getId());
        assertEquals(RecommendationSet.REASON_NEW_AUTHOR, result.getReasons().get(7));
    }

    @Test void oneAuthorCannotOccupyWholeRecommendationBlock() {
        RecommendationSet result = rank(4, Collections.singletonList(Arrays.asList(story(1, 1), story(2, 1), story(3, 1), story(4, 2), story(5, 3))),
                Collections.singletonMap(1, 10), Collections.emptyList());
        assertEquals(4, result.size());
        assertEquals(2, result.getStories().stream().filter(s -> s.getAuthorId() == 1).count());
    }

    @Test void draftDeletedDuplicatesAndCurrentStoryAreExcluded() {
        Story draft = story(2, 2); draft.setStatus("DRAFT");
        Story removed = story(3, 3); removed.setStatus("DELETED");
        Story valid = story(4, 4);
        RecommendationSet result = rank(8, Arrays.asList(Arrays.asList(story(99, 1), draft, removed, valid), Collections.singletonList(valid)),
                Collections.emptyMap(), Collections.emptyList());
        assertEquals(1, result.size()); assertEquals("Also", result.getReasons().get(4));
    }

    @Test void discoveryNeverDuplicatesAnAlreadySelectedStory() {
        Story newcomer = story(1, 1);
        RecommendationSet result = rank(4, Collections.singletonList(Arrays.asList(newcomer, story(2, 2), story(3, 3), story(4, 4))),
                Collections.emptyMap(), Collections.singletonList(newcomer));
        assertEquals(4, result.size());
        assertEquals(4, result.getStories().stream().map(Story::getId).distinct().count());
    }
}
