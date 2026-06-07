package domain;
import java.time.Instant;
import java.util.Objects;
public class slot {
    private final long id;
    private final long containerId;
    private String code;
    private boolean occupied;
    private final Instant createdAt;

    public slot(long id, long containerId, String code) {
        this.id = id;
        this.containerId = containerId;
        this.code = code;
        this.occupied = false;
        this.createdAt = Instant.now();
    }

    public long getId() {
        return id;
    }

    public long getContainerId() {
        return containerId;
    }

    public String getCode() {
        return code;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof slot)) return false;
        slot s = (slot) o;
        return id == s.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
