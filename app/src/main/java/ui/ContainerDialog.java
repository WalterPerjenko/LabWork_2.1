package ui;
import domain.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class ContainerDialog {
    public static Optional<ContainerFormData> show(container existing) {
        Dialog<ContainerFormData> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Добавить контейнер" : "Редактировать контейнер");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Название");
        ChoiceBox<ContainerType> typeBox = new ChoiceBox<>();
        typeBox.getItems().addAll(ContainerType.values());
        typeBox.setValue(ContainerType.FREEZER);

        if (existing != null) {
            nameField.setText(existing.getName());
            typeBox.setValue(existing.getType());
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Название:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Тип:"), 0, 1);
        grid.add(typeBox, 1, 1);

        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
        okButton.setDisable(nameField.getText().trim().isEmpty());
        nameField.textProperty().addListener((obs, oldVal, newVal) ->
                okButton.setDisable(newVal.trim().isEmpty()));

        dialog.setResultConverter(btn -> {
            if (btn == okButtonType) {
                return new ContainerFormData(nameField.getText().trim(), typeBox.getValue());
            }
            return null;
        });

        return dialog.showAndWait();
    }

    public record ContainerFormData(String name, ContainerType type) {}
}
