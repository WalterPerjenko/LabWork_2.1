package domain;

import java.time.Instant;

public final class container {
        private final long id;
        private String name;
        private ContainerType type;
        private String ownerUsername;
        private final Instant createdAt;
        private Instant updatedAt;

        public container(long id, String name, ContainerType type, String owner) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.ownerUsername = owner;
            this.createdAt = Instant.now();
            this.updatedAt = this.createdAt;
        }


        public long getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; this.updatedAt = Instant.now(); }
        public ContainerType getType() { return type; }
        public void setType(ContainerType type) { this.type = type; this.updatedAt = Instant.now(); }
        public String getOwnerUsername() { return ownerUsername; }
        public Instant getCreatedAt() { return createdAt; }
        public Instant getUpdatedAt() { return updatedAt; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof container)) return false;
            container c = (container) o;
            return id == c.id;
        }
        @Override
        public int hashCode() { return Long.hashCode(id); }


    }
