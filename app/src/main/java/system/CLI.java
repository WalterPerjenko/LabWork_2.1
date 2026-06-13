package system;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import domain.*;
import validators.*;
public class CLI {
        private final StorageService manager = new StorageService();
        private final Scanner scanner = new Scanner(System.in);
        private final String currentUser = "SYSTEM";

        public void run() {
            System.out.println("Введите 'help' для списка команд, 'exit' для выхода.\n");
            while (scanner.hasNextLine()) {  // Проверяем, есть ли ввод
                System.out.print("> ");
                String line = scanner.nextLine().trim();

                if (line.isEmpty()) continue;
                if (line.equals("exit")) break;


                String[] parts = line.split("\\s+");
                String cmd = parts[0].toLowerCase();

                try {
                    switch (cmd) {
                        case "exit" -> { System.out.println("Завершение работы."); return; }
                        case "help" -> printHelp();
                        case "cont_add" -> cmdContAdd();
                        case "cont_list" -> cmdContList();
                        case "cont_show" -> cmdContShow(parseId(parts[1]));
                        case "slot_create" -> cmdSlotCreate(parseId(parts[1]));
                        case "slot_list" -> cmdSlotList(parts);
                        case "place_put" -> cmdPlacePut(parts);
                        case "place_move" -> cmdPlaceMove(parts);
                        case "place_remove" -> cmdPlaceRemove(parseId(parts[1]));
                        case "place_find" -> cmdPlaceFind(parseId(parts[1]));
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
              cont_add              - Добавить контейнер (интерактивно)
              cont_list             - Список контейнеров
              cont_show <id>        - Информация о контейнере
              slot_create <id>      - Создать сетку ячеек (интерактивно)
              slot_list <id> [--free-only] - Список ячеек
              place_put <sample> <cont> <slot> - Разместить образец
              place_move <sample> <cont> <slot> - Переместить образец
              place_remove <sample> - Убрать из хранилища
              place_find <sample>   - Найти размещение
              cont_map <id>         - Карта занятости
              help / exit
            """);
        }

        private long parseId(String str) {
            try { return Long.parseLong(str); }
            catch (NumberFormatException e) { throw new IllegalArgumentException("Ошибка: неверный формат ID"); }
        }

        // --- Command Implementations ---
        private void cmdContAdd() {
            System.out.print("Название: "); String name = scanner.nextLine();
            System.out.print("Тип(FREEZER|FRIDGE|BOX): "); String typeStr = scanner.nextLine().trim().toUpperCase();
            ContainerType type;
            try { type = ContainerType.valueOf(typeStr); }
            catch (IllegalArgumentException e) { throw new IllegalArgumentException("Ошибка: тип не из списка"); }

            long id = manager.addContainer(name, type, currentUser);
            System.out.println("OK container_id=" + id);
        }

        private void cmdContList() {
            List<container> list = manager.listContainers();
            if (list.isEmpty()) { System.out.println("(пусто)"); return; }
            System.out.printf("%-4s %-20s %s%n", "ID", "Name", "Type");
            for (container c : list) {
                System.out.printf("%-4d %-20s %s%n", c.getId(), c.getName(), c.getType());
            }
        }

        private void cmdContShow(long id) {
            container c = manager.getContainer(id);
            if (c == null) throw new IllegalArgumentException("Ошибка: контейнер не найден");
            long slotCount = manager.listSlots(id, false).size();
                System.out.printf("Container#%d name: %s type: %s slots: %d%n", c.getId(), c.getName(), c.getType(), slotCount);
        }

        private void cmdSlotCreate(long contId) {
            System.out.print("Рядов(A..): "); int rows = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Колонок(1..): "); int cols = Integer.parseInt(scanner.nextLine().trim());
            manager.createSlots(contId, rows, cols);
            System.out.println("OK created " + (rows * cols) + " slots");
        }

        private void cmdSlotList(String[] parts) {
            long contId = parseId(parts[1]);
            boolean freeOnly = false;
            for (String p : parts) if ("--free-only".equals(p)) freeOnly = true;

            List<slot> list = manager.listSlots(contId, freeOnly);
            if (list.isEmpty()) { System.out.println("(пусто)"); return; }
            StringBuilder sb = new StringBuilder();
            for (slot s : list) sb.append(s.getCode()).append(" ");
            System.out.println(sb.toString().trim());
        }

        private void cmdPlacePut(String[] parts) {
            if (parts.length < 4) throw new IllegalArgumentException("Ошибка: неверное число аргументов");
            long sampleId = parseId(parts[1]);
            long contId = parseId(parts[2]);
            String slotCode = parts[3];

            manager.placeSample(sampleId, contId, slotCode, currentUser);
            container c = manager.getContainer(contId);
            System.out.println("OK placed sample " + sampleId + " into " + c.getName() + "/" + slotCode);
        }

        private void cmdPlaceMove(String[] parts) {
            if (parts.length < 4) throw new IllegalArgumentException("Ошибка: неверное число аргументов");
            manager.moveSample(parseId(parts[1]), parseId(parts[2]), parts[3]);
            System.out.println("OK moved");
        }

        private void cmdPlaceRemove(long sampleId) {
            manager.removePlacement(sampleId);
            System.out.println("OK removed from storage");
        }

        private void cmdPlaceFind(long sampleId) {
            placement p = manager.findPlacement(sampleId);
            if (p == null) { System.out.println("Not placed"); return; }
            container c = manager.getContainer(p.getContainerId());
            slot s = manager.listSlots(p.getContainerId(), false).stream()
                    .filter(sl -> sl.getId() == p.getSlotId()).findFirst().orElse(null);
            System.out.println(c.getName() + "/" + (s != null ? s.getCode() : "?"));
        }

        private void cmdContMap(long contId) {
            container c = manager.getContainer(contId);
            if (c == null) throw new IllegalArgumentException("Ошибка: контейнер не найден");
            Map<Character, List<slot>> grid = manager.getContainerMap(contId);
            if (grid.isEmpty()) { System.out.println("(нет ячеек)"); return; }

            for (var row : grid.entrySet()) {
                System.out.print(row.getKey() + ":[");
                for (int i = 0; i < row.getValue().size(); i++) {
                    System.out.print(row.getValue().get(i).isOccupied() ? "X" : " ");
                    if (i < row.getValue().size() - 1) System.out.print("][");
                }
                System.out.println("]");
            }
        }
    }

