package ui;

import domain.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import repository.*;
import storage.*;
import system.*;
import user.*;

import java.util.Optional;

public class StorageApp extends Application {
    private StorageManager manager;
    private StorageRepository repository;
    private final ObservableList<container> containerData = FXCollections.observableArrayList();
    private final ObservableList<slot> slotData = FXCollections.observableArrayList();
    private final ObservableList<placement> placementData = FXCollections.observableArrayList();
    private TableView<container> containerTable;
    private TableView<slot> slotTable;
    private TableView<sample> sampleTable;
    private TableView<placement> placementTable;
    private FileStorage fileStorage;
    private String currentFilePath = "storage.json";
    private String currentUser;
    private userService userService;

    private VBox createContainerTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        containerTable = new TableView<>();
        containerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<placement, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<placement, Long> sampleIdCol = new TableColumn<>("Sample ID");
        sampleIdCol.setCellValueFactory(new PropertyValueFactory<>("sampleId"));
        sampleIdCol.setPrefWidth(80);

        TableColumn<placement, Long> containerIdCol = new TableColumn<>("Container ID");
        containerIdCol.setCellValueFactory(new PropertyValueFactory<>("containerId"));
        containerIdCol.setPrefWidth(100);

        TableColumn<placement, Long> slotIdCol = new TableColumn<>("Slot ID");
        slotIdCol.setCellValueFactory(new PropertyValueFactory<>("slotId"));
        slotIdCol.setPrefWidth(80);

        TableColumn<placement, String> ownerCol = new TableColumn<>("Владелец");
        ownerCol.setCellValueFactory(new PropertyValueFactory<>("ownerUsername"));
        ownerCol.setPrefWidth(120);

        HBox buttonBar = new HBox(10);
        Button createBtn = new Button("Добавить контейнер");
        createBtn.setOnAction(e -> handleCreateContainer());

        Button addBtn = new Button("Создать слоты");
        addBtn.setOnAction(e -> handleAddSlots());

        Button deleteBtn = new Button("Удалить контейнер");
        deleteBtn.setOnAction(e -> handleDeleteContainer());

        Button editBtn = new Button("Изменить параметры контейнера");
        editBtn.setOnAction(e -> handleEditContainer());
        return vbox;
    }

    private void handleCreateContainer() {
        Optional<ContainerDialog.ContainerFormData> result = ContainerDialog.show(null);
        result.ifPresent(data -> {
            try {
                manager.addContainer(data.name(), data.type(), "SYSTEM");
                repository.save(manager);
                refreshData();
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage(), "У вас нет прав на удаление этого контейнера");
            }
        });
    }

    private void handleDeleteContainer() {
        container selected = containerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Выберите контейнер для удаления", "У вас нет прав на удаление этого контейнера");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Удалить контейнер \"" + selected.getName() + "\"?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                manager.removeContainer(selected.getId());
                refreshData();
            }
        });
    }

    private void handleEditContainer() {
        container selected = containerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("", "У вас нет прав на удаление этого контейнера");
            return;
        }

    }

    private void handleAddSlots() {
        container selected = containerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Сначала выберите контейнер на вкладке 'Контейнеры'", "У вас нет прав на удаление этого контейнера");
            return;
        }

        Dialog<int[]> dialog = new Dialog<>();
        dialog.setTitle("Создать слоты");
        dialog.setHeaderText("Контейнер: " + selected.getName());

        ButtonType createButtonType = new ButtonType("Создать", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField rowsField = new TextField();
        rowsField.setPromptText("10");
        TextField colsField = new TextField();
        colsField.setPromptText("10");

        grid.add(new Label("Рядов (A..):"), 0, 0);
        grid.add(rowsField, 1, 0);
        grid.add(new Label("Колонок (1..):"), 0, 1);
        grid.add(colsField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                try {
                    int rows = Integer.parseInt(rowsField.getText().trim());
                    int cols = Integer.parseInt(colsField.getText().trim());
                    return new int[]{rows, cols};
                } catch (NumberFormatException e) {
                    showError("Введите корректные числа", "У вас нет прав на удаление этого контейнера");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(dimensions -> {
            try {
                manager.createSlots(selected.getId(), dimensions[0], dimensions[1]);
                refreshData();
                showInfo("Успешно", "Создано " + (dimensions[0] * dimensions[1]) + " слотов");
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage(), "У вас нет прав на удаление этого контейнера");
            }
        });

    }

    //  таблица для образцов
    private VBox createSampleTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // Таблица размещений
        placementTable = new TableView<>();
        placementTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<placement, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<placement, Long> sampleIdCol = new TableColumn<>("Sample ID");
        sampleIdCol.setCellValueFactory(new PropertyValueFactory<>("sampleId"));
        sampleIdCol.setPrefWidth(80);

        TableColumn<placement, Long> containerIdCol = new TableColumn<>("Container ID");
        containerIdCol.setCellValueFactory(new PropertyValueFactory<>("containerId"));
        containerIdCol.setPrefWidth(100);

        TableColumn<placement, Long> slotIdCol = new TableColumn<>("Slot ID");
        slotIdCol.setCellValueFactory(new PropertyValueFactory<>("slotId"));
        slotIdCol.setPrefWidth(80);

        TableColumn<placement, String> ownerCol = new TableColumn<>("Владелец");
        ownerCol.setCellValueFactory(new PropertyValueFactory<>("ownerUsername"));
        ownerCol.setPrefWidth(120);

        placementTable.getColumns().addAll(idCol, sampleIdCol, containerIdCol, slotIdCol, ownerCol);
        placementTable.setItems(placementData);
        placementTable.setPlaceholder(new Label("Нет размещенных образцов"));

        // Кнопки
        HBox buttonBar = new HBox(10);
        Button createBtn = new Button("Создать образец");
        createBtn.setOnAction(e -> handleCreateSample());

        Button placeBtn = new Button("Разместить образец");
        placeBtn.setOnAction(e -> handlePlaceSample());

        Button removeBtn = new Button("Убрать из хранилища");
        removeBtn.setOnAction(e -> handleRemovePlacement());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refreshData());

        buttonBar.getChildren().addAll(placeBtn, removeBtn, refreshBtn);
        buttonBar.setPadding(new Insets(10, 0, 0, 0));

        vbox.getChildren().addAll(placementTable, buttonBar);
        return vbox;
    }

    private void handleCreateSample() {
        sample selected = sampleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Выберите образец для редактирования", "У вас нет прав на удаление этого контейнера");
            return;
        }

        Optional<SampleDialog.SampleFormData> result = SampleDialog.showForEdit(selected);
        result.ifPresent(data -> {
            try {
                manager.updateSample(selected.getID(), data.name());
                refreshData();
                showInfo("Успешно", "Образец обновлён");
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage(), "У вас нет прав на удаление этого контейнера");
            }
        });
    }

    private void handlePlaceSample() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Разместить образец");
        dialog.setHeaderText("Введите данные размещения");

        ButtonType placeButtonType = new ButtonType("Разместить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(placeButtonType, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField sampleIdField = new TextField();
        sampleIdField.setPromptText("12");
        TextField containerIdField = new TextField();
        containerIdField.setPromptText("1");
        TextField slotCodeField = new TextField();
        slotCodeField.setPromptText("A7");

        grid.add(new Label("Sample ID:"), 0, 0);
        grid.add(sampleIdField, 1, 0);
        grid.add(new Label("Container ID:"), 0, 1);
        grid.add(containerIdField, 1, 1);
        grid.add(new Label("Slot Code:"), 0, 2);
        grid.add(slotCodeField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == placeButtonType) {
                String sampleId = sampleIdField.getText().trim();
                String containerId = containerIdField.getText().trim();
                String slotCode = slotCodeField.getText().trim().toUpperCase();

                if (sampleId.isEmpty() || containerId.isEmpty() || slotCode.isEmpty()) {
                    showError("Все поля должны быть заполнены", "У вас нет прав на удаление этого контейнера");
                    return null;
                }

                return new String[]{sampleId, containerId, slotCode};
            }
            return null;
        });

        dialog.showAndWait().ifPresent(data -> {
            try {
                long sampleId = Long.parseLong(data[0]);
                long containerId = Long.parseLong(data[1]);
                String slotCode = data[2];

                manager.placeSample(sampleId, containerId, slotCode, "SYSTEM");
                refreshData();
                showInfo("Успешно", "Образец " + sampleId + " размещен");
            } catch (NumberFormatException e) {
                showError("ID должен быть числом", "У вас нет прав на удаление этого контейнера");
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage(), "У вас нет прав на удаление этого контейнера");
            }
        });
    }


    private void handleRemovePlacement() {
        sample selected = sampleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Выберите образец", "У вас нет прав на удаление этого контейнера");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Убрать образец #" + selected.getSampleID() + " из хранилища?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                manager.removePlacement(selected.getSampleID());
                refreshData();
            }
        });
    }

    private void refreshData() {
        refreshContainers();
        refreshSlots();
        refreshPlacements();
    }

    private void refreshContainers() {
        containerData.clear();
        containerData.addAll(manager.listContainers());
    }

    private void refreshSlots() {
        slotData.clear();
        slotData.addAll(manager.listAllSlots());
    }

    private void refreshPlacements() {
        placementData.clear();
        placementData.addAll(manager.listAllPlacements());
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message, String s) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }


    @Override
    public void start(Stage stage) throws Exception {

    }
}







