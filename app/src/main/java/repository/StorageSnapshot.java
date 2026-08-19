package repository;
import java.util.List;
public class StorageSnapshot {
    public List<ContainerDto> containers;
    public List<SlotDto> slots;
    public List<PlacementDto> placements;
    public List<SampleDto> samples;
    public long idCounter;

    public static class ContainerDto {
        public long id;
        public String name;
        public String type;
        public String ownerUsername;
        public String createdAt;
        public String updatedAt;
    }

    public static class SlotDto {
        public long id;
        public long containerId;
        public String code;
        public boolean occupied;
        public String createdAt;
    }

    public static class PlacementDto {
        public long id;
        public long sampleId;
        public long containerId;
        public long slotId;
        public String placedAt;
        public String ownerUsername;
    }

    public static class SampleDto {
        public long id;
        public String name;
        public String ownerUsername;
        public String createdAt;
        public String updatedAt;
    }
}
