package System;

import Domain.*;
import Domain.*;

import java.util.Scanner;

public class Main {

    private static final StorageService service =
            new StorageService();

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        while (true) {

            System.out.print("> ");

            String command = sc.next();

            try {

                switch (command) {

                    case "help":
                        printHelp();
                        break;

                    case "exit":
                        return;

                        case "cont_add":
                        handleAdd(sc);
                        break;

                    case "cont_list":
                        handleList();
                        break;

                    default:
                        System.out.println(
                                "Ошибка: неизвестная команда"
                        );}

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
    private static void handleAdd(Scanner sc) {

        System.out.print("Название: ");
        String name = sc.next();

        System.out.print(
                "Тип (FREEZER|FRIDGE|BOX): "
        );

        ContainerType type =
                ContainerType.valueOf(sc.next());

        Container c =
                service.addContainer(name, type);

        System.out.println(
                "OK container_id=" + c.getId()
        );
    }

    private static void handleList() {

        System.out.println("ID Name Type");

        for (Container c : service.getAllContainers()) {

            System.out.println(
                    c.getId()
                            + " "
                            + c.getName()
                            + " "
                            + c.getType()
            );
        }
    }
    private static void printHelp() {

        System.out.println("help");
        System.out.println("exit");
        System.out.println("cont_add");
        System.out.println("cont_list");
    }
}