package truyen.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Liên kết tài khoản với nhà cung cấp xác thực bên ngoài (Google, sau này có thể thêm khác).
 * JavaBean thuần — chỉ dữ liệu, không chứa SQL.
 *
 * provider_uid là `sub` (Subject ID) do Google cấp, bất biến theo thời gian.
 * email lưu tại thời điểm liên kết chỉ phục vụ hiển thị ở trang hồ sơ.
 */
public class UserIdentity implements Serializable {

    private int id;
    private int userId;
    private String provider;      // GOOGLE
    private String providerUid;   // Subject claim từ Google idToken
    private String email;
    private LocalDateTime createdAt;

    public UserIdentity() { }

    public UserIdentity(int userId, String provider, String providerUid, String email) {
        this.userId = userId;
        this.provider = provider;
        this.providerUid = providerUid;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderUid() {
        return providerUid;
    }

    public void setProviderUid(String providerUid) {
        this.providerUid = providerUid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "UserIdentity{" +
                "id=" + id +
                ", userId=" + userId +
                ", provider='" + provider + '\'' +
                ", providerUid='" + providerUid + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
