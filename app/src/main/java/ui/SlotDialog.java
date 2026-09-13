package ui;

import domain.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class SlotDialog {
    public record SlotFormData(int rows, int cols) {}

    /**
     * Показывает диалог создания слотов для контейнера
     */
    public static Optional<SlotFormData> showForCreate(container container) {
        Dialog<SlotFormData> dialog = new Dialog<>();
        dialog.setTitle("Создать слоты");
        dialog.setHeaderText("Контейнер: " + container.getName());

        ButtonType createButtonType = new ButtonType("Создать", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField rowsField = new TextField();
        rowsField.setPromptText("10");
        TextField colsField = new TextField();
        colsField.setPromptText("10");

        grid.add(new Label("Рядов (A..Z):"), 0, 0);
        grid.add(rowsField, 1, 0);
        grid.add(new Label("Колонок (1..N):"), 0, 1);
        grid.add(colsField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                try {
                    int rows = Integer.parseInt(rowsField.getText().trim());
                    int cols = Integer.parseInt(colsField.getText().trim());

                    if (rows <= 0 || cols <= 0) {
                        throw new NumberFormatException("Числа должны быть больше 0");
                    }

                    if (rows > 26) {
                        showError("Максимальное количество рядов - 26 (A-Z)");
                        return null;
                    }

                    return new SlotFormData(rows, cols);
                } catch (NumberFormatException e) {
                    showError("Введите корректные положительные числа");
                    return null;
                }
            }
            return null;
        });

        return dialog.showAndWait();
    }

    /**
     * Показывает диалог подтверждения удаления слота
     */
    public static boolean showDeleteConfirmation(slot slot, container container) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Удаление слота");
        alert.setHeaderText("Удалить слот " + slot.getCode() + "?");
        alert.setContentText("Контейнер: " + container.getName() + "\nСлот: " + slot.getCode());

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Показывает информацию о слоте
     */
    public static void showSlotInfo(slot slot, container container) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация о слоте");
        alert.setHeaderText("Слот: " + slot.getCode());

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.getChildren().addAll(
                new Label("Контейнер: " + container.getName()),
                new Label("Код: " + slot.getCode()),
                new Label("Статус: " + (slot.isOccupied() ? "ЗАНЯТ" : "СВОБОДЕН")),
                new Label("ID: " + slot.getId())
        );

        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    /**
     * Показывает диалог выбора слота из списка
     */
    public static Optional<slot> showSlotSelection(java.util.List<slot> slots, container container) {
        Dialog<slot> dialog = new Dialog<>();
        dialog.setTitle("Выберите слот");
        dialog.setHeaderText("Контейнер: " + container.getName());

        ButtonType selectButtonType = new ButtonType("Выбрать", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

        ComboBox<slot> slotCombo = new ComboBox<>();
        slotCombo.setPromptText("Выберите слот...");

        for (slot slot : slots) {
            String status = slot.isOccupied() ? " [ЗАНЯТ]" : " [СВОБОДЕН]";
            slotCombo.getItems().add(slot);
            // Устанавливаем отображение
            int index = slotCombo.getItems().size() - 1;
            slotCombo.setCellFactory(lv -> new javafx.scene.control.ListCell<slot>() {
                @Override
                protected void updateItem(slot item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String status = item.isOccupied() ? " [ЗАНЯТ]" : " [СВОБОДЕН]";
                        setText(item.getCode() + status);
                    }
                }
            });
        }

        if (!slots.isEmpty()) {
            slotCombo.setValue(slots.get(0));
        }

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(
                new Label("Доступные слоты:"),
                slotCombo
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType) {
                return slotCombo.getValue();
            }
            return null;
        });

        return dialog.showAndWait();
    }

    static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
