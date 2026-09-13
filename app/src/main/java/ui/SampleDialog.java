package ui;
import domain.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Optional;

import static ui.SlotDialog.showError;

public class SampleDialog {
    public record SampleFormData(String name) {}
    public static Optional<SampleFormData> showForCreate() {
        Dialog<SampleFormData> dialog = new Dialog<>();
        dialog.setTitle("Создать образец");
        dialog.setHeaderText("Введите название образца");

        ButtonType createButtonType = new ButtonType("Создать", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Например: Water sample #1");

        grid.add(new Label("Название:"), 0, 0);
        grid.add(nameField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    showError("Название не может быть пустым");
                    return null;
                }
                return new SampleFormData(name);
            }
            return null;
        });

        return dialog.showAndWait();
    }
    public static Optional<SampleFormData> showForEdit(sample sample) {
        Dialog<SampleFormData> dialog = new Dialog<>();
        dialog.setTitle("Редактировать образец");
        dialog.setHeaderText("Образец #" + sample.getSampleID());

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(sample.getSampleName());
        nameField.setPromptText("Название");

        grid.add(new Label("Название:"), 0, 0);
        grid.add(nameField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    showError("Название не может быть пустым");
                    return null;
                }
                return new SampleFormData(name);
            }
            return null;
        });

        return dialog.showAndWait();
    }
    public static boolean showDeleteConfirmation(sample sample) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Удаление образца");
        alert.setHeaderText("Удалить образец #" + sample.getSampleID() + "?");
        alert.setContentText("Название: " + sample.getSampleName() + "\n\n" +
                "Это действие нельзя отменить!");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    public static void showSampleInfo(sample sample, String location) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация об образце");
        alert.setHeaderText("Образец #" + sample.getSampleID());

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.getChildren().addAll(
                new Label("ID: " + sample.getSampleID()),
                new Label("Название: " + sample.getSampleName()),
                new Label("Владелец: " + sample.getOwnerUsername()),
                new Label("Создан: " + sample.getCreatedAt()),
                new Label("Местоположение: " + location)
        );

        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }
    private static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
