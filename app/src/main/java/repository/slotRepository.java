package repository;

import domain.slot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class slotRepository {
    private final Connection connection;

    public slotRepository(Connection connection) {
        this.connection = connection;
    }

    public long insert(long containerId, String code, boolean occupied) throws SQLException {
        String sql = "INSERT INTO slots (container_id, code, occupied) VALUES (?, ?, ?) RETURNING id";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, containerId);
            ps.setString(2, code);
            ps.setBoolean(3, occupied);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("id");
            }
            throw new SQLException("Не удалось получить ID слота");
        }
    }

    public List<slot> findByContainerId(long containerId) throws SQLException {
        String sql = "SELECT id, container_id, code, occupied, created_at FROM slots WHERE container_id = ? ORDER BY code";
        List<slot> slots = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, containerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                slots.add(mapSlot(rs));
            }
        }
        return slots;
    }

    public slot findById(long id) throws SQLException {
        String sql = "SELECT id, container_id, code, occupied, created_at FROM slots WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapSlot(rs);
            }
            return null;
        }
    }

    public void updateOccupied(long id, boolean occupied) throws SQLException {
        String sql = "UPDATE slots SET occupied = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, occupied);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public void deleteByContainerId(long containerId) throws SQLException {
        String sql = "DELETE FROM slots WHERE container_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, containerId);
            ps.executeUpdate();
        }
    }

    private slot mapSlot(ResultSet rs) throws SQLException {
        return new slot(
                rs.getLong("id"),
                rs.getLong("container_id"),
                rs.getString("code"),
                rs.getBoolean("occupied"),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
