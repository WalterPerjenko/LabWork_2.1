package repository;

import domain.placement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class placementRepository {
    private final Connection connection;

    public placementRepository(Connection connection) {
        this.connection = connection;
    }

    public long insert(long sampleId, long containerId, long slotId, String ownerUsername) throws SQLException {
        String sql = "INSERT INTO placements (sample_id, container_id, slot_id, owner_username) VALUES (?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, sampleId);
            ps.setLong(2, containerId);
            ps.setLong(3, slotId);
            ps.setString(4, ownerUsername);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("id");
            }
            throw new SQLException("Не удалось получить ID размещения");
        }
    }

    public placement findBySampleId(long sampleId) throws SQLException {
        String sql = "SELECT id, sample_id, container_id, slot_id, placed_at, owner_username FROM placements WHERE sample_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, sampleId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapPlacement(rs);
            }
            return null;
        }
    }

    public List<placement> findByContainerId(long containerId) throws SQLException {
        String sql = "SELECT id, sample_id, container_id, slot_id, placed_at, owner_username FROM placements WHERE container_id = ?";
        List<placement> placements = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, containerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                placements.add(mapPlacement(rs));
            }
        }
        return placements;
    }

    public void deleteBySampleId(long sampleId) throws SQLException {
        String sql = "DELETE FROM placements WHERE sample_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, sampleId);
            ps.executeUpdate();
        }
    }

    public void deleteByContainerId(long containerId) throws SQLException {
        String sql = "DELETE FROM placements WHERE container_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, containerId);
            ps.executeUpdate();
        }
    }

    private placement mapPlacement(ResultSet rs) throws SQLException {
        return new placement(
                rs.getLong("id"),
                rs.getLong("sample_id"),
                rs.getLong("container_id"),
                rs.getLong("slot_id"),
                rs.getTimestamp("placed_at").toInstant(),
                rs.getString("owner_username")
        );
    }
}
