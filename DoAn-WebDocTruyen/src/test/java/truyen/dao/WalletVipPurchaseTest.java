package truyen.dao;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WalletVipPurchaseTest {
    /** Kết nối giả lập ba bước ghi; không mở CSDL hay đọc cấu hình thật. */
    static class Transaction {
        int writes;
        int failAt;
        int noRowAt;
        boolean committed;
        boolean rolledBack;
        boolean autoCommit = true;
        int amount;
        int duration;

        final Connection connection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                new Class<?>[] { Connection.class }, (p, method, args) -> {
                    switch (method.getName()) {
                        case "setAutoCommit": autoCommit = (Boolean) args[0]; return null;
                        case "commit": committed = true; return null;
                        case "rollback": rolledBack = true; return null;
                        case "prepareStatement":
                            String sql = (String) args[0];
                            Map<Integer, Object> params = new HashMap<>();
                            return Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(),
                                    new Class<?>[] { PreparedStatement.class }, (statement, op, values) -> {
                                        if (op.getName().equals("setInt") || op.getName().equals("setString")) {
                                            params.put((Integer) values[0], values[1]);
                                        }
                                        if (op.getName().equals("executeUpdate")) {
                                            assertFalse(autoCommit, "All writes must run in one transaction");
                                            writes++;
                                            if (sql.startsWith("UPDATE wallets")) amount = (Integer) params.get(1);
                                            if (sql.startsWith("UPDATE users")) duration = (Integer) params.get(1);
                                            if (writes == failAt) throw new SQLException("Injected write failure");
                                            return writes == noRowAt ? 0 : 1;
                                        }
                                        return null;
                                    });
                        default: return null;
                    }
                });
    }

    @Test void commitsOnlyAfterDebitExtensionAndLedgerAllSucceed() throws Exception {
        Transaction tx = new Transaction();
        assertTrue(new WalletDAO().purchaseVip(tx.connection, 42, 90, 150));
        assertEquals(3, tx.writes); assertTrue(tx.committed); assertFalse(tx.rolledBack);
        assertEquals(150, tx.amount); assertEquals(90, tx.duration); assertTrue(tx.autoCommit);
    }

    @Test void extensionFailureRollsBackDebit() {
        Transaction tx = new Transaction(); tx.failAt = 2;
        assertThrows(SQLException.class, () -> new WalletDAO().purchaseVip(tx.connection, 42, 30, 60));
        assertTrue(tx.rolledBack); assertFalse(tx.committed); assertTrue(tx.autoCommit);
        assertEquals(2, tx.writes);
    }

    @Test void ledgerFailureRollsBackDebitAndExtension() {
        Transaction tx = new Transaction(); tx.failAt = 3;
        assertThrows(SQLException.class, () -> new WalletDAO().purchaseVip(tx.connection, 42, 365, 500));
        assertTrue(tx.rolledBack); assertFalse(tx.committed); assertTrue(tx.autoCommit);
    }

    @Test void insufficientBalanceCannotExtendVip() throws Exception {
        Transaction tx = new Transaction(); tx.noRowAt = 1;
        assertFalse(new WalletDAO().purchaseVip(tx.connection, 42, 30, 60));
        assertEquals(1, tx.writes); assertTrue(tx.rolledBack); assertFalse(tx.committed);
    }

    @Test void missingUserCannotLeaveWalletDebited() throws Exception {
        Transaction tx = new Transaction(); tx.noRowAt = 2;
        assertFalse(new WalletDAO().purchaseVip(tx.connection, 42, 30, 60));
        assertTrue(tx.rolledBack); assertFalse(tx.committed); assertEquals(2, tx.writes);
    }

    @Test void invalidUserOrPlanIsRejectedBeforeDatabaseAccess() throws Exception {
        assertFalse(new WalletDAO().purchaseVip(0, 30));
        assertFalse(new WalletDAO().purchaseVip(42, -10));
        assertFalse(new WalletDAO().purchaseVip(42, 31));
    }
}
