package truyen.dao;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import truyen.model.Achievement;
import static org.junit.jupiter.api.Assertions.*;

class AchievementTransactionsTest {
    /** Mô phỏng sổ cái và rollback; không dùng dữ liệu hoặc cấu hình CSDL thật. */
    static class Ledger {
        int balance = 100, authorBalance, logs;
        boolean claimed, unlocked, active = true, failLog;
        int savedBalance, savedAuthor, savedLogs;
        boolean savedClaim, savedUnlock, autoCommit = true;
        final Connection connection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                new Class<?>[] {Connection.class}, (proxy, op, args) -> {
                    switch (op.getName()) {
                        case "setAutoCommit":
                            autoCommit = (Boolean) args[0];
                            if (!autoCommit) {
                                savedBalance = balance; savedAuthor = authorBalance; savedLogs = logs;
                                savedClaim = claimed; savedUnlock = unlocked;
                            }
                            return null;
                        case "commit": return null;
                        case "rollback":
                            balance = savedBalance; authorBalance = savedAuthor; logs = savedLogs;
                            claimed = savedClaim; unlocked = savedUnlock; return null;
                        case "prepareStatement": return statement((String) args[0]);
                        default: return null;
                    }
                });

        private PreparedStatement statement(String sql) {
            Map<Integer, Object> params = new HashMap<>();
            return (PreparedStatement) Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(),
                    new Class<?>[] {PreparedStatement.class}, (proxy, op, args) -> {
                        if (op.getName().startsWith("set")) { params.put((Integer) args[0], args[1]); return null; }
                        if ("executeQuery".equals(op.getName())) {
                            boolean found = sql.startsWith("SELECT id FROM users") ? active : unlocked;
                            boolean[] consumed = {false};
                            return Proxy.newProxyInstance(ResultSet.class.getClassLoader(), new Class<?>[] {ResultSet.class},
                                    (row, method, values) -> {
                                        if ("next".equals(method.getName())) {
                                            if (consumed[0]) return false;
                                            consumed[0] = true; return found;
                                        }
                                        return null;
                                    });
                        }
                        if (!"executeUpdate".equals(op.getName())) return null;
                        assertFalse(autoCommit, "No independent credit/debit is allowed");
                        if (sql.startsWith("UPDATE user_achievements")) {
                            if (claimed || !active) return 0;
                            claimed = true; return 1;
                        }
                        if (sql.startsWith("INSERT IGNORE INTO wallets")) return 0;
                        if (sql.startsWith("UPDATE wallets SET balance = balance -")) {
                            int price = (Integer) params.get(1);
                            if (balance < price) return 0;
                            balance -= price; return 1;
                        }
                        if (sql.startsWith("UPDATE wallets SET balance = balance +")) {
                            balance += (Integer) params.get(1); return 1;
                        }
                        if (sql.startsWith("INSERT INTO wallets")) { authorBalance += (Integer) params.get(2); return 1; }
                        if (sql.startsWith("INSERT INTO chapter_unlocks")) { unlocked = true; return 1; }
                        if (sql.startsWith("INSERT INTO transactions")) {
                            if (failLog) throw new SQLException("Injected ledger failure");
                            logs++; return 1;
                        }
                        throw new AssertionError("Unexpected write");
                    });
        }
    }

    private Achievement milestone() {
        Achievement a = new Achievement(); a.setId("first_story"); a.setTitle("Khởi Đầu Sáng Tác");
        a.setUnlocked(true); a.setRewardCoins(20); return a;
    }

    @Test void milestoneCreditsExactlyOnceEvenWhenSubmittedAgain() throws Exception {
        Ledger ledger = new Ledger(); AchievementDAO dao = new AchievementDAO();
        assertTrue(dao.claim(ledger.connection, 2, milestone()));
        assertEquals(120, ledger.balance); assertEquals(1, ledger.logs);
        assertFalse(dao.claim(ledger.connection, 2, milestone()));
        assertEquals(120, ledger.balance); assertEquals(1, ledger.logs);
    }

    @Test void failedMilestoneLedgerWriteRestoresClaimAndBalance() {
        Ledger ledger = new Ledger(); ledger.failLog = true;
        assertThrows(SQLException.class, () -> new AchievementDAO().claim(ledger.connection, 2, milestone()));
        assertEquals(100, ledger.balance); assertFalse(ledger.claimed); assertEquals(0, ledger.logs);
        assertTrue(ledger.autoCommit);
    }

    @Test void chapterUnlockChargesOnceAndGrantsDurableAccess() throws Exception {
        Ledger ledger = new Ledger(); WalletDAO dao = new WalletDAO();
        assertTrue(dao.unlockChapter(ledger.connection, 2, 1001, 3, 10, 10));
        assertEquals(90, ledger.balance); assertEquals(10, ledger.authorBalance); assertTrue(ledger.unlocked);
        assertTrue(dao.unlockChapter(ledger.connection, 2, 1001, 3, 10, 10));
        assertEquals(90, ledger.balance); assertEquals(10, ledger.authorBalance); assertEquals(1, ledger.logs);
    }

    @Test void chapterUnlockFailureRollsBackBothWalletsAndAccess() {
        Ledger ledger = new Ledger(); ledger.failLog = true;
        assertThrows(SQLException.class, () -> new WalletDAO().unlockChapter(ledger.connection, 2, 1001, 3, 10, 10));
        assertEquals(100, ledger.balance); assertEquals(0, ledger.authorBalance);
        assertFalse(ledger.unlocked); assertEquals(0, ledger.logs); assertTrue(ledger.autoCommit);
    }

    @Test void insufficientBalanceDoesNotGrantChapterOrCreditAuthor() throws Exception {
        Ledger ledger = new Ledger(); ledger.balance = 5;
        assertFalse(new WalletDAO().unlockChapter(ledger.connection, 2, 1001, 3, 10, 10));
        assertEquals(5, ledger.balance); assertFalse(ledger.unlocked); assertEquals(0, ledger.authorBalance);
    }

    @Test void bannedUserCannotReceiveRewardOrPurchaseChapter() throws Exception {
        Ledger ledger = new Ledger(); ledger.active = false;
        assertFalse(new AchievementDAO().claim(ledger.connection, 2, milestone()));
        assertFalse(new WalletDAO().unlockChapter(ledger.connection, 2, 1001, 3, 10, 10));
        assertEquals(100, ledger.balance); assertEquals(0, ledger.logs);
    }

    @Test void demoModeNeverPretendsItSavedChapterUnlock() throws Exception {
        if (!truyen.util.DBConnection.isReady()) {
            assertFalse(new WalletDAO().unlockChapter(2, 1001, 3, 10, 10));
        }
    }
}
