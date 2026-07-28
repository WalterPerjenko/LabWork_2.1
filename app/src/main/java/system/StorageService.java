package system;




import domain.*;
import validators.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class StorageService {

    private final Set<container> containers = new HashSet<>();
    private final Set<slot> slots = new HashSet<>();
    private final Set<placement> placements = new HashSet<>();
    private final Set<sample> sample = new HashSet<>();

    // Генератор  ID
    private final AtomicLong idGenerator = new AtomicLong(1);

    public long addContainer(String name, ContainerType type, String owner) {
        ContainerValidator.validateName(name);
        ContainerValidator.validateType(type);
        container c = new container(idGenerator.getAndIncrement(), name.trim(), type, owner);
        containers.add(c);
        return c.getId();
    }

    public container getContainer(long id) {
        return containers.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
    }

    public List<container> listContainers() {
        return new ArrayList<>(containers);
    }
    public Map<sample , sample.sampleID> HashSetSamples(){
        return  Map<>(sample);
    }

    public void updateContainer(long id, String newName, ContainerType newType) {
        container c = getContainer(id);
        if (c == null) throw new IllegalArgumentException("Ошибка: контейнер с id=" + id + " не найден");
        ContainerValidator.validateUpdate(newName, newType);
        if (newName != null) c.setName(newName);
        if (newType != null) c.setType(newType);
    }

    public void removeContainer(long id) {
        container c = getContainer(id);
        if (c == null) throw new IllegalArgumentException("Ошибка: контейнер не найден");
        containers.remove(c);
        // Удаляем связанные слоты и размещения для целостности
        slots.removeIf(s -> s.getContainerId() == id);
        placements.removeIf(p -> p.getContainerId() == id);
    }


    public void createSlots(long containerId, int rows, int cols) {
        if (getContainerById(containerId) == null) {
            throw new IllegalArgumentException("Ошибка: контейнер не найден");
        }
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Ошибка: количество рядов/колонок должно быть > 0");
        }
        if (rows > 1000) {
            throw new IllegalArgumentException("Ошибка: максимальное количество рядов - 1000");
        }
        if (cols > 1000) {
            throw new IllegalArgumentException("Ошибка: максимальное количество колонок - 1000");
        }
        int created = 0;
        for (int r = 1; r <= rows; r++) {
            String rowCode = Slotgenerator.rowToCode(r);

            for (int c = 1; c <= cols; c++) {
                String slotCode = rowCode + c;

                // Проверка дублей внутри контейнера
                boolean exists = slots.stream()
                        .anyMatch(s -> s.getContainerId() == containerId && s.getCode().equals(slotCode));

                if (exists) {
                    throw new IllegalArgumentException("Ошибка: слот " + slotCode + " уже существует");
                }

                slots.add(new slot(idGenerator.getAndIncrement(), containerId, slotCode));
                created++;
            }
        }
    }

    public List<slot> listSlots(long containerId, boolean freeOnly) {
        if (getContainer(containerId) == null)
            throw new IllegalArgumentException("Ошибка: контейнер не найден");
        return slots.stream()
                .filter(s -> s.getContainerId() == containerId && (!freeOnly || !s.isOccupied()))
                .sorted(Comparator.comparing(slot::getCode))
                .collect(Collectors.toList());
    }

    public slot findSlot(long containerId, String code) {
        return slots.stream()
                .filter(s -> s.getContainerId() == containerId && s.getCode().equals(code))
                .findFirst().orElse(null);
    }
// sample manager
    public void createSample (long sampleID, String sampleName, String ownerUsername){
        SampleValidator.validateID(sampleID);
        SampleValidator.validateName(sampleName);
        sample s = new sample(idGenerator.getAndIncrement(), sampleID, sampleName, ownerUsername);
    }

    //  Placement Management
    public void placeSample(long sampleId, long containerId, String slotCode, String owner) {

        if (getContainer(containerId) == null)
            throw new IllegalArgumentException("Ошибка: контейнер не найден");
        slot slot = findSlot(containerId, slotCode);
        if (slot == null) throw new IllegalArgumentException("Ошибка: ячейка не существует");
        if (slot.isOccupied()) throw new IllegalArgumentException("Ошибка: ячейка уже занята");


        removePlacementIfExists(sampleId);

        placements.add(new placement(idGenerator.getAndIncrement(), sampleId, containerId, slot.getId(), owner));
        slot.setOccupied(true);
    }

    public void moveSample(long sampleId, long containerId, String slotCode) {
        placement old = placements.stream().filter(p -> p.getsampleID() == sampleId).findFirst().orElse(null);
        if (old == null) throw new IllegalArgumentException("Ошибка: образец не размещён");
        placeSample(sampleId, containerId, slotCode, old.getOwnerUsername());
    }

    public void removePlacement(long sampleId) {
        placement p = placements.stream().filter(pl -> pl.getSampleId() == sampleId).findFirst().orElse(null);
        if (p == null) throw new IllegalArgumentException("Ошибка: размещение не найдено");
        placements.remove(p);
        slot s = slots.stream().filter(sl -> sl.getId() == p.getSlotId()).findFirst().orElse(null);
        if (s != null) s.setOccupied(false);
    }

    private void removePlacementIfExists(long sampleId) {
        sample.removeIf(smp -> smp.getSampleID() == sampleId);
    }

    public placement findPlacement(long sampleId) {
        return placements.stream().filter(p -> p.getSampleId() == sampleId).findFirst().orElse(null);
    }
    public Map<Character, List<slot>> getContainerMap(long containerId) {
        Map<Character, List<slot>> grid = new TreeMap<>();
        slots.stream()
                .filter(s -> s.getContainerId() == containerId)
                .forEach(s -> grid.computeIfAbsent(s.getCode().charAt(0), k -> new ArrayList<>()).add(s));
        return grid;
    }
}
