package truyen;

import org.junit.jupiter.api.Test;
import truyen.util.ReadingEvidence;
import static org.junit.jupiter.api.Assertions.*;

class ReadingEvidenceTest {
    @Test void progressAloneWithoutServerStartCannotEarnCredit() {
        WebRequestFixture web = new WebRequestFixture();
        assertFalse(ReadingEvidence.qualifies(web.request.getSession(true), 1, 10, 10, 50_000));
    }
    @Test void requiresBothThirtySecondsAndEightyPercent() {
        WebRequestFixture web = new WebRequestFixture();
        ReadingEvidence.opened(web.request.getSession(true), 1, 1000);
        assertFalse(ReadingEvidence.qualifies(web.session.proxy, 1, 10, 10, 30_999));
        assertFalse(ReadingEvidence.qualifies(web.session.proxy, 1, 7, 10, 31_000));
        assertTrue(ReadingEvidence.qualifies(web.session.proxy, 1, 8, 10, 31_000));
        assertFalse(ReadingEvidence.qualifies(web.session.proxy, 2, 10, 10, 31_000));
    }
    @Test void repeatedOpenDoesNotResetReadingTimer() {
        WebRequestFixture web = new WebRequestFixture();
        ReadingEvidence.opened(web.request.getSession(true), 1, 1000);
        ReadingEvidence.opened(web.session.proxy, 1, 20_000);
        assertTrue(ReadingEvidence.qualifies(web.session.proxy, 1, 3, 3, 31_000));
    }
    @Test void emptyChapterAndNegativePositionNeverQualify() {
        WebRequestFixture web = new WebRequestFixture();
        ReadingEvidence.opened(web.request.getSession(true), 1, 1000);
        assertFalse(ReadingEvidence.qualifies(web.session.proxy, 1, 0, 0, 31_000));
        assertFalse(ReadingEvidence.qualifies(web.session.proxy, 1, -10, 3, 31_000));
    }
}
