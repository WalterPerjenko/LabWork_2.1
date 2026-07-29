package domain;

import java.time.Instant;

public final class placement {
        private final long id;
        private final long containerID;
        private final long slotID;
        private final long sampleID;
        private final Instant placedAt;
        private String ownerUsername;

        public placement(long id, long sampleID, long containerID, long slotId, String ownerUsername) {
            this.id = id;
            this.containerID = containerID;
            this.slotID = slotId;
            this.sampleID = sampleID;
            this.placedAt = Instant.now();
            this.ownerUsername = ownerUsername;
        }

        public long getID() { return id; }
        public long getContainerID() { return containerID; }
        public long getSlotID() { return slotID; }
    public long getSampleID(){return sampleID;}
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

