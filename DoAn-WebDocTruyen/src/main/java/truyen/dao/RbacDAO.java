package truyen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import truyen.model.User;
import truyen.util.DBConnection;

/**
 * Quản lý vai trò (Roles) và quyền hạn (Permissions) trong hệ thống RBAC.
 */
public class RbacDAO {

    public List<String> findRolesByUser(int userId) {
        List<String> list = new ArrayList<>();
        if (!DBConnection.isReady() || userId <= 0) return list;

        String sql = "SELECT r.code FROM roles r "
                   + "JOIN user_roles ur ON ur.role_id = r.id "
                   + "WHERE ur.user_id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("code"));
                }
            }
        } catch (SQLException ignored) { }
        return list;
    }

    public Set<String> findPermissionsByUser(int userId) {
        Set<String> set = new HashSet<>();
        if (!DBConnection.isReady() || userId <= 0) return set;

        String sql = "SELECT DISTINCT p.code FROM permissions p "
                   + "JOIN role_permissions rp ON rp.permission_id = p.id "
                   + "JOIN user_roles ur ON ur.role_id = rp.role_id "
                   + "WHERE ur.user_id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    set.add(rs.getString("code"));
                }
            }
        } catch (SQLException ignored) { }
        return set;
    }

    public void populateRbac(User user) {
        if (user == null || user.getId() <= 0) return;
        user.setRoles(findRolesByUser(user.getId()));
        user.setPermissions(findPermissionsByUser(user.getId()));
    }

    public boolean assignRole(int userId, String roleCode) throws SQLException {
        if (!DBConnection.isReady() || userId <= 0 || roleCode == null) return false;
        String sql = "INSERT IGNORE INTO user_roles (user_id, role_id) "
                   + "SELECT ?, id FROM roles WHERE code = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, roleCode);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean removeRole(int userId, String roleCode) throws SQLException {
        if (!DBConnection.isReady() || userId <= 0 || roleCode == null) return false;
        String sql = "DELETE ur FROM user_roles ur "
                   + "JOIN roles r ON r.id = ur.role_id "
                   + "WHERE ur.user_id = ? AND r.code = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, roleCode);
            return ps.executeUpdate() > 0;
        }
    }
}
