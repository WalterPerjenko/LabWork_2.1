package domain;

import java.time.Instant;
import java.util.Objects;

    public final class placement {
        private final long id;
        private final long sampleId;
        private final long containerId;
        private final long slotId;
        private final Instant placedAt;
        private String ownerUsername;

        public placement(long id, long sampleId, long containerId, long slotId, String ownerUsername) {
            this.id = id;
            this.sampleId = sampleId;
            this.containerId = containerId;
            this.slotId = slotId;
            this.placedAt = Instant.now();
            this.ownerUsername = ownerUsername;
        }

        public long getId() { return id; }
        public long getSampleId() { return sampleId; }
        public long getContainerId() { return containerId; }
        public long getSlotId() { return slotId; }
        public Instant getPlacedAt() { return placedAt; }
        public String getOwnerUsername() { return ownerUsername; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof placement)) return false;
            placement p = (placement) o;
            return id == p.id;
        }
        @Override
        public int hashCode() {
            return Long.hashCode(id);
        }
    }

