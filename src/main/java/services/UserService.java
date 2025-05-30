package services;

import entities.User;
import utils.MyDatabase;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserService implements Service<User> {

    private Connection cnx;

    public UserService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(User user) throws SQLException {
        String sql = "INSERT INTO user (last_name, first_name, email, password, is_verified, is_blocked, roles) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, user.getLastName());
        ps.setString(2, user.getFirstName());
        ps.setString(3, user.getEmail());
        ps.setString(4, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        ps.setBoolean(5, false);
        ps.setBoolean(6, false);
        ps.setString(7, "[\"ROLE_STUDENT\"]");
        ps.executeUpdate();
    }
    public void ajouterAdmin(User user) throws SQLException {
        String sql = "INSERT INTO user (last_name, first_name, email, password, is_verified, is_blocked, roles) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, user.getLastName());
        ps.setString(2, user.getFirstName());
        ps.setString(3, user.getEmail());
        ps.setString(4, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        ps.setBoolean(5, false);
        ps.setBoolean(6, false);
        ps.setString(7, "[\"ROLE_ADMIN\"]");
        ps.executeUpdate();
    }
    public void ajouterNouveau(User user) throws SQLException {
        // Check if the email already exists
        if (findByEmail(user.getEmail()) != null) {
            throw new SQLException("Email address already exists.");
        }
        ajouter(user); // Use the original ajouter method to perform the insertion
    }
    @Override
    public void modifier(User user) throws SQLException {
        String sql = "update user set last_name = ?, first_name = ?, email = ?, is_blocked = ? where id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, user.getLastName());
        ps.setString(2, user.getFirstName());
        ps.setString(3, user.getEmail());
        ps.setBoolean(4, user.isBlocked());
        ps.setInt(5, user.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(User user) throws SQLException {
        String sql = "delete from user where id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, user.getId());
        ps.executeUpdate();
    }

    @Override
    public List<User> recuperer() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, first_name, last_name, email, roles, is_blocked FROM user";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            int id = rs.getInt("id");
            String firstName = rs.getString("first_name");
            String lastName = rs.getString("last_name");
            String email = rs.getString("email");
            String rolesStr = rs.getString("roles");
            boolean isBlocked = rs.getBoolean("is_blocked");
            List<String> roles = rolesStr != null ? Arrays.asList(rolesStr.replace("[\"", "").replace("\"]", "").split(",")) : new ArrayList<>();

            // Set other fields to defaults since not retrieved
            User user = new User(id, firstName, lastName, email, null, false, isBlocked, null, roles);
            users.add(user);
        }

        return users;
    }

    public User findByEmail(String email) throws SQLException {
        String sql = "select * from user where email = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            int id = rs.getInt("id");
            String lastName = rs.getString("last_name");
            String firstName = rs.getString("first_name");
            String password = rs.getString("password");
            boolean isVerified = rs.getBoolean("is_verified");
            boolean isBlocked = rs.getBoolean("is_blocked");
            String profileIMG = rs.getString("profile_img");
            String rolesStr = rs.getString("roles");
            List<String> roles = rolesStr != null ? Arrays.asList(rolesStr.replace("[\"", "").replace("\"]", "").split(",")) : new ArrayList<>();

            return new User(id, firstName, lastName, email, password, isVerified, isBlocked, profileIMG, roles);
        }

        throw new SQLException("User not found");
    }

    public void updateBlockStatus(int userId, boolean isBlocked) throws SQLException {
        String sql = "UPDATE user SET is_blocked = ? WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setBoolean(1, isBlocked);
        ps.setInt(2, userId);
        int rowsUpdated = ps.executeUpdate();
        if (rowsUpdated == 0) {
            throw new SQLException("No user found with id: " + userId);
        }
    }
    public User getUserById(int id) throws SQLException {
        String query = "SELECT id, first_name, last_name, email, roles, is_blocked FROM users WHERE id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setEmail(rs.getString("email"));
                // Assuming roles is stored as a comma-separated string in the database
                String roles = rs.getString("roles");
                user.setRoles(roles != null ? Arrays.asList(roles.split(",")) : new ArrayList<>());
                user.setBlocked(rs.getBoolean("is_blocked"));
                return user;
            }
            return null; // User not found
        }
    }
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String lastName = rs.getString("last_name");
        String firstName = rs.getString("first_name");
        String email = rs.getString("email");
        String password = rs.getString("password");
        boolean isVerified = rs.getBoolean("is_verified");
        boolean isBlocked = rs.getBoolean("is_blocked");
        String profileIMG = rs.getString("profile_img");
        String rolesStr = rs.getString("roles");
        List<String> roles = rolesStr != null ? Arrays.asList(rolesStr.replace("[\"", "").replace("\"]", "").split(",")) : new ArrayList<>();
        return new User(id, firstName, lastName, email, password, isVerified, isBlocked, profileIMG, roles);
    }

    public void updateAdminProfile(User admin) throws SQLException {
        String sql = "UPDATE user SET last_name = ?, first_name = ?, email = ?, password = ? WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, admin.getLastName());
        ps.setString(2, admin.getFirstName());
        ps.setString(3, admin.getEmail());
        ps.setString(4, admin.getPassword()); // Assuming password might be updated
        ps.setInt(5, admin.getId());
        ps.executeUpdate();
    }
    public User getAdminDetails(int adminId) throws SQLException {
        String sql = "SELECT * FROM user WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, adminId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return mapResultSetToUser(rs);
        }
        return null;
    }
}



