package repository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import domain.*;
import system.StorageManager;

import java.io.*;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.*;
public class StorageRepository {
    private final String filePath;
    private final ObjectMapper mapper;

    public StorageRepository() {
        this.filePath = resolveFilePath();
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    private String resolveFilePath() {
        String env = System.getenv("STORAGE_FILE");
        if (env != null && !env.isBlank()) return env;
        try (InputStream is = StorageRepository.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                String prop = props.getProperty("storage.file");
                if (prop != null && !prop.isBlank()) return prop;
            }
        } catch (IOException ignored) {}
        return "storage.json";
    }

    public void save(StorageManager svc) {
        StorageSnapshot snap = new StorageSnapshot();
        snap.idCounter = svc.getCurrentIdCounter();

        snap.containers = new ArrayList<>();
        for (container c : svc.getContainers()) {
            StorageSnapshot.ContainerDto dto = new StorageSnapshot.ContainerDto();
            dto.id = c.getId();
            dto.name = c.getName();
            dto.type = c.getType().name();
            dto.ownerUsername = c.getOwnerUsername();
            dto.createdAt = c.getCreatedAt().toString();
            dto.updatedAt = c.getUpdatedAt().toString();
            snap.containers.add(dto);
        }

        snap.slots = new ArrayList<>();
        for (slot s : svc.getSlots()) {
            StorageSnapshot.SlotDto dto = new StorageSnapshot.SlotDto();
            dto.id = s.getId();
            dto.containerId = s.getContainerId();
            dto.code = s.getCode();
            dto.occupied = s.isOccupied();
            dto.createdAt = s.getCreatedAt().toString();
            snap.slots.add(dto);
        }

        snap.placements = new ArrayList<>();
        for (placement p : svc.getPlacements()) {
            StorageSnapshot.PlacementDto dto = new StorageSnapshot.PlacementDto();
            dto.id = p.getID();
            dto.sampleId = p.getSampleID();
            dto.containerId = p.getContainerID();
            dto.slotId = p.getSlotID();
            dto.placedAt = p.getPlacedAt().toString();
            dto.ownerUsername = p.getOwnerUsername();
            snap.placements.add(dto);
        }

        snap.samples = new ArrayList<>();
        for (sample s : svc.getSamples()) {
            StorageSnapshot.SampleDto dto = new StorageSnapshot.SampleDto();
            dto.id = s.getID();
            dto.name = s.getSampleName();
            dto.ownerUsername = s.getOwnerUsername();
            dto.createdAt = s.getCreatedAt().toString();
            dto.updatedAt = s.getUpdatedAt().toString();
            snap.samples.add(dto);
        }

        try {
            mapper.writeValue(new File(filePath), snap);
        } catch (IOException e) {
            System.err.println("Не удалось сохранить данные: " + e.getMessage());
        }
    }

    public void load(StorageManager svc) {
        File file = new File(filePath);
        if (!file.exists()) return;
        try {
            StorageSnapshot snap = mapper.readValue(file, StorageSnapshot.class);

            svc.getContainers().clear();
            svc.getSlots().clear();
            svc.getPlacements().clear();
            svc.getSamples().clear();

            if (snap.containers != null) {
                for (StorageSnapshot.ContainerDto dto : snap.containers) {
                    container c = new container(dto.id, dto.name, ContainerType.valueOf(dto.type), dto.ownerUsername);
                    setInstant(c, "createdAt", dto.createdAt);
                    setInstant(c, "updatedAt", dto.updatedAt);
                    svc.getContainers().add(c);
                }
            }

            if (snap.slots != null) {
                for (StorageSnapshot.SlotDto dto : snap.slots) {
                    slot s = new slot(dto.id, dto.containerId, dto.code);
                    s.setOccupied(dto.occupied);
                    setInstant(s, "createdAt", dto.createdAt);
                    svc.getSlots().add(s);
                }
            }

            if (snap.placements != null) {
                for (StorageSnapshot.PlacementDto dto : snap.placements) {
                    placement p = new placement(dto.id, dto.sampleId, dto.containerId, dto.slotId, dto.ownerUsername);
                    setInstant(p, "placedAt", dto.placedAt);
                    svc.getPlacements().add(p);
                }
            }

            if (snap.samples != null) {
                for (StorageSnapshot.SampleDto dto : snap.samples) {
                    sample s = new sample(dto.id, dto.name, dto.ownerUsername);
                    setInstant(s, "createdAt", dto.createdAt);
                    setInstant(s, "updatedAt", dto.updatedAt);
                    svc.getSamples().add(s);
                }
            }

            if (snap.idCounter > 0) svc.setIdCounter(snap.idCounter);

        } catch (IOException e) {
            System.err.println("Не удалось загрузить данные: " + e.getMessage());
        }
    }

    private void setInstant(Object obj, String fieldName, String isoValue) {
        if (isoValue == null) return;
        try {
            Class<?> cls = obj.getClass();
            Field f = null;
            while (cls != null && f == null) {
                try { f = cls.getDeclaredField(fieldName); }
                catch (NoSuchFieldException ex) { cls = cls.getSuperclass(); }
            }
            if (f == null) return;
            f.setAccessible(true);
            f.set(obj, Instant.parse(isoValue));
        } catch (Exception ignored) {}
    }
}
