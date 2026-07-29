package system;
import domain.*;
import validators.*;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
public class CLI {
    private final StorageManager manager = new StorageManager();
    private final Scanner scanner = new Scanner(System.in);
    private final String currentUser = "SYSTEM";

    public void run() {

        while (scanner.hasNextLine()) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+");
            String cmd = parts[0].toLowerCase();

            try {
                switch (cmd) {
                    case "exit" -> {
                        System.out.println("Завершение работы.");
                        return;
                    }
                    case "help" -> printHelp();

                    // Sample команды
                    case "sample_add" -> cmdSampleAdd();
                    case "sample_list" -> cmdSampleList();
                    case "sample_show" -> cmdSampleShow(parseId(parts[1]));
                    case "sample_update" -> cmdSampleUpdate(parts);
                    case "sample_remove" -> cmdSampleRemove(parseId(parts[1]));

                    // Container команды
                    case "cont_add" -> cmdContAdd();
                    case "cont_list" -> cmdContList();
                    case "cont_show" -> cmdContShow(parseId(parts[1]));
                    case "slot_create" -> cmdSlotCreate(parseId(parts[1]));
                    case "slot_list" -> cmdSlotList(parts);

                    // Placement команды (sample_put, sample_move, sample_remove)
                    case "sample_put" -> cmdSamplePut(parts);
                    case "sample_move" -> cmdSampleMove(parts);
                    case "sample_find" -> cmdSampleFind(parseId(parts[1]));

                    // Map
                    case "cont_map" -> cmdContMap(parseId(parts[1]));

                    default -> System.out.println("Ошибка: неизвестная команда. Введите 'help'.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: ID должен быть числом");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println("Ошибка выполнения: " + e.getMessage());
            }
        }
    }

    private void printHelp() {
        System.out.println("""
            Доступные команды:
            
            Образцы:
              sample_add              - Добавить образец (интерактивно)
              sample_list             - Список образцов
              sample_show <id>        - Информация об образце
              sample_update <id> <name> - Обновить название
              sample_remove <id>      - Удалить образец (только не размещённый)
            
            Контейнеры:
              cont_add                - Добавить контейнер (интерактивно)
              cont_list               - Список контейнеров
              cont_show <id>          - Информация о контейнере
              slot_create <id>        - Создать сетку ячеек (интерактивно)
              slot_list <id> [--free-only] - Список ячеек
              cont_map <id>           - Карта занятости
            
            Размещение:
              sample_put <sample_id> <container_id> <slot_code> - Разместить образец
              sample_move <sample_id> <container_id> <slot_code> - Переместить образец
              sample_remove <sample_id> - Убрать из хранилища
              sample_find <sample_id> - Найти размещение
            
              help / exit
            """);
    }

    private long parseId(String str) {
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ошибка: неверный формат ID");
        }
    }


    private void cmdSampleAdd() {
        System.out.print("Название образца: ");
        String name = scanner.nextLine();
        long id = manager.addSample(name, currentUser);
        System.out.println("OK sample_id=" + id);
    }

    private void cmdSampleList() {
        List<sample> list = manager.listSamples();
        if (list.isEmpty()) {
            System.out.println("(пусто)");
            return;
        }
        System.out.printf("%-4s %-30s %-10s%n", "ID", "Name", "Owner");
        for (sample s : list) {
            System.out.printf("%-4d %-30s %-10s%n", s.getSampleID(), s.getSampleName(), s.getOwnerUsername());
        }
    }

    private void cmdSampleShow(long id) {
        sample sample = manager.getSampleById(id);
        if (sample == null) {
            throw new IllegalArgumentException("Ошибка: образец с id=" + id + " не найден");
        }
        String location = manager.getSampleLocation(id);
        System.out.printf("Sample#%d name: %s owner: %s location: %s%n",
                sample.getSampleID(), sample.getSampleName(), sample.getOwnerUsername(), location);
    }

    private void cmdSampleUpdate(String[] parts) {
        if (parts.length < 3) {
            throw new IllegalArgumentException("Ошибка: неверное число аргументов");
        }
        long id = parseId(parts[1]);
        String newName = parts[2];
        manager.updateSample(id, newName);
        System.out.println("OK");
    }

    private void cmdSampleRemove(long id) {
        manager.removeSample(id);
        System.out.println("OK sample " + id + " removed");
    }


    private void cmdContAdd() {
        System.out.print("Название: ");
        String name = scanner.nextLine();
        System.out.print("Тип(FREEZER|FRIDGE|BOX): ");
        String typeStr = scanner.nextLine().trim().toUpperCase();
        ContainerType type;
        try {
            type = ContainerType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Ошибка: тип не из списка");
        }
        long id = manager.addContainer(name, type, currentUser);
        System.out.println("OK container_id=" + id);
    }

    private void cmdContList() {
        List<container> list = manager.listContainers();
        if (list.isEmpty()) {
            System.out.println("(пусто)");
            return;
        }
        System.out.printf("%-4s %-20s %s%n", "ID", "Name", "Type");
        for (container c : list) {
            System.out.printf("%-4d %-20s %s%n", c.getId(), c.getName(), c.getType());
        }
    }

    private void cmdContShow(long id) {
        container c = manager.getContainerById(id);
        if (c == null) {
            throw new IllegalArgumentException("Ошибка: контейнер не найден");
        }
        long slotCount = manager.listSlots(id, false).size();
        System.out.printf("Container#%d name: %s type: %s slots: %d%n",
                c.getId(), c.getName(), c.getType(), slotCount);
    }

    private void cmdSlotCreate(long contId) {
        System.out.print("Рядов(A..): ");
        int rows = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Колонок(1..): ");
        int cols = Integer.parseInt(scanner.nextLine().trim());
        manager.createSlots(contId, rows, cols);
        System.out.println("OK created " + (rows * cols) + " slots");
    }

    private void cmdSlotList(String[] parts) {
        long contId = parseId(parts[1]);
        boolean freeOnly = false;
        for (String p : parts) {
            if ("--free-only".equals(p)) freeOnly = true;
        }
        List<slot> list = manager.listSlots(contId, freeOnly);
        if (list.isEmpty()) {
            System.out.println("(пусто)");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (slot s : list) {
            sb.append(s.getCode()).append(" ");
        }
        System.out.println(sb.toString().trim());
    }


    private void cmdSamplePut(String[] parts) {
        if (parts.length < 4) {
            throw new IllegalArgumentException("Ошибка: неверное число аргументов");
        }
        long sampleId = parseId(parts[1]);
        long containerId = parseId(parts[2]);
        String slotCode = parts[3];

        manager.placeSample(sampleId, containerId, slotCode, currentUser);
        container c = manager.getContainerById(containerId);
        System.out.println("OK placed sample " + sampleId + " into " + c.getName() + "/" + slotCode);
    }

    private void cmdSampleMove(String[] parts) {
        if (parts.length < 4) {
            throw new IllegalArgumentException("Ошибка: неверное число аргументов");
        }
        long sampleId = parseId(parts[1]);
        long containerId = parseId(parts[2]);
        String slotCode = parts[3];

        manager.moveSample(sampleId, containerId, slotCode);
        System.out.println("OK moved sample " + sampleId);
    }

    private void cmdSampleFind(long sampleId) {
        String location = manager.getSampleLocation(sampleId);
        System.out.println(location);
    }


    private void cmdContMap(long contId) {
        container c = manager.getContainerById(contId);
        if (c == null) {
            throw new IllegalArgumentException("Ошибка: контейнер не найден");
        }
        Map<Character, List<slot>> grid = manager.getContainerMap(contId);
        if (grid.isEmpty()) {
            System.out.println("(нет ячеек)");
            return;
        }

        for (var row : grid.entrySet()) {
            System.out.print(row.getKey() + ":[");
            for (int i = 0; i < row.getValue().size(); i++) {
                System.out.print(row.getValue().get(i).isOccupied() ? "X" : " ");
                if (i < row.getValue().size() - 1) {
                    System.out.print("][");
                }
            }
            System.out.println("]");
        }
    }
}

