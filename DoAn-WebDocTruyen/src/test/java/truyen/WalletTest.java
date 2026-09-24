package truyen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import truyen.dao.WalletDAO;

@DisplayName("WalletDAO — Ví xu ảo & Chuyển xu (ISSUE-008)")
class WalletTest {

    private final WalletDAO walletDAO = new WalletDAO();

    @Test
    @DisplayName("Ca 1: Chuyển xu với số tiền <= 0 trả về false")
    void testNegativeAmount() throws SQLException {
        assertFalse(walletDAO.transfer(1, 2, 10, 0, "Test"));
        assertFalse(walletDAO.transfer(1, 2, 10, -50, "Test"));
    }

    @Test
    @DisplayName("Ca 2: Tự chuyển xu cho chính mình trả về false")
    void testSelfTransfer() throws SQLException {
        assertFalse(walletDAO.transfer(1, 1, 10, 20, "Self tip"));
    }

    @Test
    @DisplayName("Ca 3: findByUserId trả về đối tượng Wallet với số dư hợp lệ")
    void testFindByUserId() throws SQLException {
        truyen.model.Wallet w = walletDAO.findByUserId(1);
        org.junit.jupiter.api.Assertions.assertNotNull(w);
        assertEquals(1, w.getUserId());
        assertTrue(w.getBalance() >= 0);
    }

    @Test
    @DisplayName("Ca 4: findHistoryByUserId với limit hợp lệ trả về danh sách an toàn")
    void testFindHistory() throws SQLException {
        java.util.List<truyen.model.Transaction> list = walletDAO.findHistoryByUserId(1, 10);
        org.junit.jupiter.api.Assertions.assertNotNull(list);
    }
}
