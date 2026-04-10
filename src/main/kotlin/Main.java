

import Domain.*;
import System.*;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        StorageService service = new StorageService();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("> ");
            String cmd = sc.next();

            try {
                switch (cmd) {

                    case "cont_add":
                        System.out.print("Название: ");
                        String name = sc.next();
                        System.out.print("Тип: ");
                        ContainerType type = ContainerType.valueOf(sc.next());
                        var c = service.addContainer(name, type);
                        System.out.println("OK container_id=" + c.getId());
                        break;

                    case "cont_list":
                        for (var cont : service.getAllContainers()) {
                            System.out.println(cont.getId() + " " + cont.getName() + " " + cont.getType());
                        }
                        break;

                    case "slot_create":
                        long id = sc.nextLong();
                        int r = sc.nextInt();
                        int col = sc.nextInt();
                        service.createSlots(id, r, col);
                        System.out.println("OK");
                        break;

                    case "place_put":
                        long s = sc.nextLong();
                        long cid = sc.nextLong();
                        String code = sc.next();
                        service.place(s, cid, code);
                        System.out.println("OK");
                        break;

                    case "cont_map":
                        long cid2 = sc.nextLong();
                        System.out.println(service.getMap(cid2));
                        break;

                    case "exit":
                        return;
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
