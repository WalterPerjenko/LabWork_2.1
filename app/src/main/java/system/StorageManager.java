package system;




import domain.*;
import validators.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


public class StorageManager {

    private final Set<sample> samples = new HashSet<>();
    private final Set<container> containers = new HashSet<>();
    private final Set<slot> slots = new HashSet<>();
    private final Set<placement> placements = new HashSet<>();

    private final AtomicLong idGen = new AtomicLong(1);

// sample
    public long addSample(String name, String owner) {
        SampleValidator.validateName(name);
        long id = idGen.getAndIncrement();
        sample sample = new sample(id ,name.trim(), owner );
        samples.add(sample);
        return sample.getID();
    }
    public sample getSampleById(long id) {
        return samples.stream()
                .filter(s -> s.getID() == id)
                .findFirst()
                .orElse(null);
    }
    public List<sample> listSamples() {return new ArrayList<>(samples);}
public sample getSample (long ID) {return samples.stream().filter(s -> s.getID() == ID).findFirst().orElse(null);}
    public void updateSample(long id, String newName) {
        sample sample = getSampleById(id);
        if (sample == null) {
            throw new IllegalArgumentException("Ошибка: образец с id=" + id + " не найден");
        }
        SampleValidator.validateName(newName);
        sample.setName(newName);
    }

    public void removeSample(long id) {
        sample sample = getSampleById(id);
        if (sample == null) {
            throw new IllegalArgumentException("Ошибка: образец не найден");
        }

        placement placement = findPlacementBySampleId(id);
        if (placement != null) {
            throw new IllegalArgumentException("Ошибка: нельзя удалить размещённый образец. Сначала выполните sample_remove");
        }
        samples.remove(sample);
    }

// container
    public long addContainer(String name, ContainerType type, String owner) {
        ContainerValidator.validateName(name);
        ContainerValidator.validateType(type);
        container container = new container(idGen.getAndIncrement(), name.trim(), type, owner);
        containers.add(container);
        return container.getId();
    }

    public container getContainerById(long id) {
        return containers.stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public List<container> listContainers() {
        return new ArrayList<>(containers);
    }
    public void updateContainer(long id, String newName, ContainerType newType) {
        container c = getContainerById(id);
        if (c == null) throw new IllegalArgumentException("Ошибка: контейнер с id=" + id + " не найден");
        ContainerValidator.validateUpdate(newName, newType);
        if (newName != null) c.setName(newName);
        if (newType != null) c.setType(newType);
    }
    public void removeContainer(long id) {
        container c = getContainerById(id);
        if (c == null) throw new IllegalArgumentException("Ошибка: контейнер не найден");
        containers.remove(c);
        slots.removeIf(s -> s.getContainerId() == id);
        placements.removeIf(p -> p.getContainerID() == id);
    }
    public container findContainerById(long containerID){
        return  containers.stream()
                .filter(c ->c.getId() == containerID)
                .findFirst()
                .orElse(null);
    }
    // slot

    public void createSlots(long containerId, int rows, int cols) {
        if (getContainerById(containerId) == null) {
            throw new IllegalArgumentException("Ошибка: контейнер не найден");
        }
        SlotValidator.validateDimensions(rows, cols);

        for (int r = 0; r < rows; r++) {
            for (int c = 1; c <= cols; c++) {
                String code = "" + (char) ('A' + r) + c;
                boolean exists = slots.stream()
                        .anyMatch(s -> s.getContainerId() == containerId && s.getCode().equals(code));
                if (exists) {
                    throw new IllegalArgumentException("Ошибка: слот " + code + " уже существует");
                }
                slots.add(new slot(idGen.getAndIncrement(), containerId, code));
            }
        }
    }

    public List<slot> listSlots(long containerId, boolean freeOnly) {
        if (getContainerById(containerId) == null) {
            throw new IllegalArgumentException("Ошибка: контейнер не найден");
        }
        return slots.stream()
                .filter(s -> s.getContainerId() == containerId && (!freeOnly || !s.isOccupied()))
                .sorted(Comparator.comparing(slot::getCode))
                .collect(Collectors.toList());
    }

    public slot findSlot(long containerId, String code) {
        return slots.stream()
                .filter(s -> s.getContainerId() == containerId && s.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }


    public void placeSample(long sampleId, long containerId, String slotCode, String owner) {
        sample sample = getSampleById(sampleId);
        if (sample == null) {
            throw new IllegalArgumentException("Ошибка: образец с id=" + sampleId + " не найден");
        }
        container container = getContainerById(containerId);
        if (container == null) {
            throw new IllegalArgumentException("Ошибка: контейнер не найден");
        }
        slot slot = findSlot(containerId, slotCode);
        if (slot == null) {
            throw new IllegalArgumentException("Ошибка: слот не существует");
        }
        if (slot.isOccupied()) {
            throw new IllegalArgumentException("Ошибка: слот занят");
        }
        placement existing = findPlacementBySampleId(sampleId);
        if (existing != null) {
            throw new IllegalArgumentException("Ошибка: образец уже размещён. Используйте sample_move для перемещения");
        }
        placements.add(new placement(idGen.getAndIncrement(), sampleId, containerId, slot.getId(), owner));
        slot.setOccupied(true);
    }

    public void moveSample(long sampleId, long containerId, String slotCode) {

        placement currentPlacement = findPlacementBySampleId(sampleId);
        if (currentPlacement == null) {
            throw new IllegalArgumentException("Ошибка: образец не размещён");
        }
        slot oldSlot = findSlotById(currentPlacement.getSlotID());
        if (oldSlot != null) {
            oldSlot.setOccupied(false);
        }
        placements.remove(currentPlacement);
        placeSample(sampleId, containerId, slotCode, currentPlacement.getOwnerUsername());
    }

    public void removePlacement(long sampleId) {
        placement placement = findPlacementBySampleId(sampleId);
        if (placement == null) {
            throw new IllegalArgumentException("Ошибка: образец не размещён");
        }
        slot slot = findSlotById(placement.getSlotID());
        if (slot != null) {
            slot.setOccupied(false);
        }
        placements.remove(placement);
    }



    public placement findPlacementBySampleId(long sampleId) {
        return placements.stream()
                .filter(p -> p.getSampleID() == sampleId)
                .findFirst()
                .orElse(null);
    }

    public slot findSlotById(long slotId) {
        return slots.stream()
                .filter(s -> s.getId() == slotId)
                .findFirst()
                .orElse(null);
    }


    public String getSampleLocation(long sampleId) {
        placement placement = findPlacementBySampleId(sampleId);
        if (placement == null) {
            return "Not placed";
        }
        container container = getContainerById(placement.getContainerID());
        slot slot = findSlotById(placement.getSlotID());
        return container.getName() + "/" + slot.getCode();
    }


    public Map<Character, List<slot>> getContainerMap(long containerId) {
        if (getContainerById(containerId) == null) {
            throw new IllegalArgumentException("Ошибка: контейнер не найден");
        }
        Map<Character, List<slot>> grid = new TreeMap<>();
        slots.stream()
                .filter(s -> s.getContainerId() == containerId)
                .forEach(s -> grid.computeIfAbsent(s.getCode().charAt(0), k -> new ArrayList<>()).add(s));
        return grid;
    }

    public void clear() {
        containers.clear();
        slots.clear();
        placements.clear();
    }



    public Set<container> getAllContainers() {
        return containers;
    }


    public int getSlotCount(long containerId) {
        return (int) slots.stream().filter(s -> s.getContainerId() == containerId).count();

    }
    public Set<container> getContainers() { return containers; }
    public Set<slot> getSlots() { return slots; }
    public Set<placement> getPlacements() { return placements; }
    public Set<sample> getSamples() { return samples; }
    public long getCurrentIdCounter() { return idGen.get(); }
    public void setIdCounter(long value) { idGen.set(value); }
}