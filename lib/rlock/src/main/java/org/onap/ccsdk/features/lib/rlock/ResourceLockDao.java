package org.onap.ccsdk.features.lib.rlock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import javax.sql.DataSource;

public class ResourceLockDao implements AutoCloseable {

    private Connection con;

    public ResourceLockDao(DataSource dataSource) {
        try {
            con = dataSource.getConnection();
            con.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException("Error getting DB connection: " + e.getMessage(), e);
        }
    }

    public void add(ResourceLock l) {
        String sql = "INSERT INTO RESOURCE_LOCK (resource_name, lock_holder, lock_count, lock_time, expiration_time)\n"
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, l.resourceName);
            ps.setString(2, l.lockHolder);
            ps.setInt(3, l.lockCount);
            ps.setTimestamp(4, new Timestamp(l.lockTime.getTime()));
            ps.setTimestamp(5, new Timestamp(l.expirationTime.getTime()));
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding lock to DB: " + e.getMessage(), e);
        }
    }

    public void update(long id, String lockHolder, Date lockTime, Date expirationTime, int lockCount) {
        String sql =
                "UPDATE RESOURCE_LOCK SET lock_holder = ?, lock_time = ?, expiration_time = ?, lock_count = ?\n"
                + "WHERE resource_lock_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, lockHolder);
            ps.setTimestamp(2, new Timestamp(lockTime.getTime()));
            ps.setTimestamp(3, new Timestamp(expirationTime.getTime()));
            ps.setInt(4, lockCount);
            ps.setLong(5, id);
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating lock in DB: " + e.getMessage(), e);
        }
    }

    public ResourceLock getByResourceName(String resourceName) {
        String sql = "SELECT * FROM RESOURCE_LOCK WHERE resource_name = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, resourceName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ResourceLock rl = new ResourceLock();
                    rl.id = rs.getLong("resource_lock_id");
                    rl.resourceName = rs.getString("resource_name");
                    rl.lockHolder = rs.getString("lock_holder");
                    rl.lockCount = rs.getInt("lock_count");
                    rl.lockTime = rs.getTimestamp("lock_time");
                    rl.expirationTime = rs.getTimestamp("expiration_time");
                    return rl;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading lock from DB: " + e.getMessage(), e);
        }
    }

    public void delete(long id) {
        String sql = "DELETE FROM RESOURCE_LOCK WHERE resource_lock_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting lock from DB: " + e.getMessage(), e);
        }
    }

    public void decrementLockCount(long id) {
        String sql = "UPDATE RESOURCE_LOCK SET lock_count = lock_count - 1 WHERE resource_lock_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating lock count in DB: " + e.getMessage(), e);
        }
    }

    public void commit() {
        try {
            con.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error committing DB connection: " + e.getMessage(), e);
        }
    }

    public void rollback() {
        try {
            con.rollback();
        } catch (SQLException e) {
        }
    }

    @Override
    public void close() {
        try {
            con.close();
        } catch (SQLException e) {
        }
    }
}
