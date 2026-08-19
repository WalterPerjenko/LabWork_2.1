package ui;
import domain.container;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import repository.StorageRepository;
import system.StorageManager;

import java.util.Optional;
public class ContainerTableView  extends BorderPane{
    private final StorageManager service;
    private final StorageRepository repository;
    private final ObservableList<container> data = FXCollections.observableArrayList();
    private final TableView<container> table = new TableView<>(data);

    public ContainerTableView(StorageManager service, StorageRepository repository) {
        this.service = service;
        this.repository = repository;
        buildTable();
        buildButtons();
        refreshData();
    }

    @SuppressWarnings("unchecked")
    private void buildTable() {
        TableColumn<container, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(r -> new SimpleStringProperty(String.valueOf(r.getValue().getId())));
        idCol.setPrefWidth(50);

        TableColumn<container, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getName()));
        nameCol.setPrefWidth(200);

        TableColumn<container, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getType().name()));
        typeCol.setPrefWidth(100);

        TableColumn<container, String> slotsCol = new TableColumn<>("Slots");
        slotsCol.setCellValueFactory(r -> new SimpleStringProperty(
                String.valueOf(service.getSlotCount(r.getValue().getId()))));
        slotsCol.setPrefWidth(60);

        table.getColumns().addAll(idCol, nameCol, typeCol, slotsCol);
        table.setPlaceholder(new Label("Нет контейнеров"));
        setCenter(table);
    }

    private void buildButtons() {
        Button addBtn = new Button("Add");
        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");
        Button refreshBtn = new Button("Refresh");

        addBtn.setOnAction(e -> handleAdd());
        editBtn.setOnAction(e -> handleEdit());
        deleteBtn.setOnAction(e -> handleDelete());
        refreshBtn.setOnAction(e -> handleRefresh());

        HBox bar = new HBox(8, addBtn, editBtn, deleteBtn, refreshBtn);
        bar.setPadding(new Insets(8));
        setBottom(bar);
    }

    private void handleAdd() {
        Optional<ContainerDialog.ContainerFormData> result = ContainerDialog.show(null);
        result.ifPresent(data -> {
            try {
                service.addContainer(data.name(), data.type(), "SYSTEM");
                repository.save(service);
                refreshData();
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            }
        });
    }

    private void handleEdit() {
        container selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Выберите контейнер для редактирования"); return; }
        Optional<ContainerDialog.ContainerFormData> result = ContainerDialog.show(selected);
        result.ifPresent(formData -> {
            try {
                service.updateContainer(selected.getId(), formData.name(), formData.type());
                repository.save(service);
                refreshData();
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            }
        });
    }

    private void handleDelete() {
        container selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Выберите контейнер для удаления"); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Удалить контейнер \"" + selected.getName() + "\" и все его ячейки/размещения?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Подтверждение удаления");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                service.removeContainer(selected.getId());
                repository.save(service);
                refreshData();
            }
        });
    }

    private void handleRefresh() {
        repository.load(service);
        refreshData();
    }

    private void refreshData() {
        data.setAll(service.listContainers());
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }

}
