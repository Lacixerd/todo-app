package com.todo.app;

import com.todo.controller.TodoService;
import com.todo.model.TodoItem;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;

public class TodoApp extends Application {

    private final TodoService    service    = new TodoService();
    private final TreeViewWindow treeWindow = new TreeViewWindow(service);

    private ListView<TodoItem> taskListView;
    private TextField          titleField;
    private ComboBox<TodoItem.Priority> priorityBox;
    private Label              statusLabel;
    private Label              statsLabel;

    @Override
    public void start(Stage stage) {
        stage.setTitle("📋 DSA Todo List");

        // ── Disk'ten yükle (pencere açılmadan önce) ──
        service.loadFromDisk();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1e1e2e;");

        root.setTop(buildHeader());
        root.setCenter(buildCenter());
        root.setBottom(buildStatusBar());

        Scene scene = new Scene(root, 720, 620);
        stage.setScene(scene);
        stage.setResizable(false);

        // Pencere kapatılınca kaydet
        stage.setOnCloseRequest(e -> service.saveToDisk());

        stage.show();
        refreshList();

        int count = service.totalTasks();
        if (count > 0)
            setStatus("💾 " + count + " görev yüklendi ← " + service.getSaveFilePath(), "#a6e3a1");
        else
            setStatus("📋 Hazır. Kayıt: " + service.getSaveFilePath(), "#6c7086");
    }

    /** Uygulama stop() — pencere dışında (System.exit vb.) kapatılırsa da çalışır */
    @Override
    public void stop() {
        service.saveToDisk();
    }

    // ── HEADER ────────────────────────────────────────────────────────────

    private VBox buildHeader() {
        Label title = new Label("DSA Todo List");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#cdd6f4"));

        Label subtitle = new Label("LinkedList · Stack (Undo/Redo) · Queue (Priority) · BST (Tree View)");
        subtitle.setFont(Font.font("Segoe UI", 12));
        subtitle.setTextFill(Color.web("#6c7086"));

        VBox header = new VBox(4, title, subtitle);
        header.setPadding(new Insets(20, 24, 12, 24));
        header.setStyle("-fx-background-color: #181825; -fx-border-color: #313244; -fx-border-width: 0 0 1 0;");
        return header;
    }

    // ── CENTER ────────────────────────────────────────────────────────────

    private HBox buildCenter() {
        HBox center = new HBox(16, buildInputPanel(), buildListPanel());
        center.setPadding(new Insets(16, 24, 16, 24));
        return center;
    }

    private VBox buildInputPanel() {
        Label titleLbl = styledLabel("Görev Başlığı");
        titleField = new TextField();
        titleField.setPromptText("Yeni görev gir...");
        styleTextField(titleField);
        titleField.setOnAction(e -> addTask());

        Label priorityLbl = styledLabel("Öncelik");
        priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll(TodoItem.Priority.values());
        priorityBox.setValue(TodoItem.Priority.MEDIUM);
        styleComboBox(priorityBox);

        // --- Butonlar ---
        Button addBtn    = actionButton("➕ Ekle",         "#a6e3a1", "#1e1e2e");
        Button doneBtn   = actionButton("✔ Tamamla",       "#89b4fa", "#1e1e2e");
        Button deleteBtn = actionButton("🗑 Sil",           "#f38ba8", "#1e1e2e");

        Button undoBtn   = actionButton("↩ Geri Al",       "#fab387", "#1e1e2e");
        Button redoBtn   = actionButton("↪ İleri Al",      "#89dceb", "#1e1e2e");  // ← YENİ

        Button nextHPBtn = actionButton("⚡ Sonraki HIGH",  "#cba6f7", "#1e1e2e");
        Button treeBtn   = actionButton("🌳 Ağaç Görünümü", "#f2cdcd", "#1e1e2e"); // ← YENİ

        addBtn.setOnAction(e -> addTask());
        doneBtn.setOnAction(e -> completeTask());
        deleteBtn.setOnAction(e -> deleteTask());
        undoBtn.setOnAction(e -> undoAction());
        redoBtn.setOnAction(e -> redoAction());
        nextHPBtn.setOnAction(e -> nextHighPriority());
        treeBtn.setOnAction(e -> openTreeView());

        statsLabel = new Label();
        statsLabel.setFont(Font.font("Monospace", 11));
        statsLabel.setTextFill(Color.web("#6c7086"));

        VBox panel = new VBox(8,
                titleLbl, titleField,
                priorityLbl, priorityBox,
                new Separator(),
                addBtn, doneBtn, deleteBtn,
                new Separator(),
                undoBtn, redoBtn,
                new Separator(),
                nextHPBtn,
                new Separator(),
                treeBtn,
                new Separator(),
                statsLabel
        );
        panel.setPrefWidth(210);
        panel.setStyle("-fx-background-color: #181825; -fx-background-radius: 10; -fx-padding: 16;");
        return panel;
    }

    private VBox buildListPanel() {
        ToggleGroup filterGroup = new ToggleGroup();
        ToggleButton allBtn    = filterTab("Tümü",       filterGroup);
        ToggleButton activeBtn = filterTab("Aktif",      filterGroup);
        ToggleButton doneBtn2  = filterTab("Tamamlanan", filterGroup);
        allBtn.setSelected(true);

        allBtn.setOnAction(e -> refreshList());
        activeBtn.setOnAction(e -> showTasks(service.getActiveTasks()));
        doneBtn2.setOnAction(e -> showTasks(service.getCompletedTasks()));

        HBox tabs = new HBox(4, allBtn, activeBtn, doneBtn2);

        taskListView = new ListView<>();
        taskListView.setPrefHeight(440);
        taskListView.setStyle("""
                -fx-background-color: #181825;
                -fx-control-inner-background: #181825;
                -fx-border-color: #313244;
                -fx-border-radius: 8;
                -fx-background-radius: 8;
                """);

        taskListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(TodoItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    String icon = switch (item.getPriority()) {
                        case HIGH   -> "🔴";
                        case MEDIUM -> "🟡";
                        case LOW    -> "🟢";
                    };
                    setText(icon + "  " + item.getTitle() + (item.isCompleted() ? "  ✓" : ""));
                    setFont(Font.font("Segoe UI", 14));
                    setTextFill(item.isCompleted() ? Color.web("#585b70") : Color.web("#cdd6f4"));
                    setStyle("-fx-background-color: transparent; -fx-padding: 8 12;");
                }
            }
        });

        VBox panel = new VBox(8, tabs, taskListView);
        HBox.setHgrow(panel, Priority.ALWAYS);
        return panel;
    }

    private VBox buildStatusBar() {
        statusLabel = new Label("Hazır.");
        statusLabel.setFont(Font.font("Segoe UI", 12));
        statusLabel.setTextFill(Color.web("#6c7086"));

        VBox bar = new VBox(statusLabel);
        bar.setPadding(new Insets(8, 24, 12, 24));
        bar.setStyle("-fx-background-color: #181825; -fx-border-color: #313244; -fx-border-width: 1 0 0 0;");
        return bar;
    }

    // ── ACTIONS ───────────────────────────────────────────────────────────

    private void addTask() {
        String text = titleField.getText().trim();
        if (text.isEmpty()) { setStatus("⚠ Görev başlığı boş olamaz!", "#f38ba8"); return; }
        service.addTask(text, priorityBox.getValue());
        titleField.clear();
        service.saveToDisk();
        refreshAndSync();
        setStatus("✅ Görev eklendi: " + text, "#a6e3a1");
    }

    private void deleteTask() {
        TodoItem sel = taskListView.getSelectionModel().getSelectedItem();
        if (sel == null) { setStatus("⚠ Önce bir görev seç!", "#f38ba8"); return; }
        service.deleteTask(sel.getId());
        service.saveToDisk();
        refreshAndSync();
        setStatus("🗑 Silindi: " + sel.getTitle(), "#f38ba8");
    }

    private void completeTask() {
        TodoItem sel = taskListView.getSelectionModel().getSelectedItem();
        if (sel == null) { setStatus("⚠ Önce bir görev seç!", "#f38ba8"); return; }
        service.completeTask(sel.getId());
        service.saveToDisk();
        refreshAndSync();
        setStatus("✔ Tamamlandı: " + sel.getTitle(), "#89b4fa");
    }

    private void undoAction() {
        String result = service.undo();
        if (result == null) { setStatus("⚠ Geri alınacak işlem yok.", "#fab387"); return; }
        service.saveToDisk();
        refreshAndSync();
        setStatus("↩ " + result, "#fab387");
    }

    private void redoAction() {
        String result = service.redo();
        if (result == null) { setStatus("⚠ İleri alınacak işlem yok.", "#89dceb"); return; }
        service.saveToDisk();
        refreshAndSync();
        setStatus("↪ " + result, "#89dceb");
    }

    private void nextHighPriority() {
        TodoItem item = service.pollNextHighPriority();
        if (item == null) { setStatus("⚡ HIGH öncelikli kuyruk boş.", "#cba6f7"); return; }
        setStatus("⚡ Kuyruktan alındı: " + item.getTitle()
                + " | Kalan: " + service.highPriorityQueueSize(), "#cba6f7");
    }

    private void openTreeView() {
        treeWindow.show();
        setStatus("🌳 Ağaç görünümü açıldı.", "#f2cdcd");
    }

    // ── HELPERS ───────────────────────────────────────────────────────────

    /** Liste + stats güncelle ve açık ağaç penceresini de yenile */
    private void refreshAndSync() {
        refreshList();
        treeWindow.refresh();
    }

    private void refreshList() {
        showTasks(service.getAllTasks());
        updateStats();
    }

    private void showTasks(List<TodoItem> tasks) {
        taskListView.getItems().setAll(tasks);
        updateStats();
    }

    private void updateStats() {
        statsLabel.setText(String.format(
                "Toplam: %d  |  Aktif: %d\nTamamlanan: %d  |  HIGH Queue: %d\nUndo: %d  |  Redo: %d",
                service.totalTasks(),
                service.getActiveTasks().size(),
                service.getCompletedTasks().size(),
                service.highPriorityQueueSize(),
                service.getUndoStack().size(),
                service.getRedoStack().size()
        ));
    }

    private void setStatus(String msg, String color) {
        statusLabel.setText(msg);
        statusLabel.setTextFill(Color.web(color));
    }

    // ── UI FACTORY ────────────────────────────────────────────────────────

    private Label styledLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        l.setTextFill(Color.web("#a6adc8"));
        return l;
    }

    private void styleTextField(TextField tf) {
        tf.setStyle("""
                -fx-background-color: #313244;
                -fx-text-fill: #cdd6f4;
                -fx-prompt-text-fill: #585b70;
                -fx-border-color: #45475a;
                -fx-border-radius: 6;
                -fx-background-radius: 6;
                -fx-padding: 6 10;
                """);
    }

    private void styleComboBox(ComboBox<?> cb) {
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setStyle("""
                -fx-background-color: #313244;
                -fx-text-fill: #cdd6f4;
                -fx-border-color: #45475a;
                -fx-border-radius: 6;
                -fx-background-radius: 6;
                """);
    }

    private Button actionButton(String text, String bg, String fg) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        btn.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-background-radius: 6;
                -fx-padding: 7 12;
                -fx-cursor: hand;
                """, bg, fg));
        return btn;
    }

    private ToggleButton filterTab(String text, ToggleGroup group) {
        ToggleButton tb = new ToggleButton(text);
        tb.setToggleGroup(group);
        tb.setFont(Font.font("Segoe UI", 12));
        tb.setStyle("""
                -fx-background-color: #313244;
                -fx-text-fill: #a6adc8;
                -fx-background-radius: 6;
                -fx-padding: 5 14;
                -fx-cursor: hand;
                """);
        tb.selectedProperty().addListener((obs, o, n) -> {
            if (n) tb.setStyle("""
                    -fx-background-color: #89b4fa;
                    -fx-text-fill: #1e1e2e;
                    -fx-background-radius: 6;
                    -fx-padding: 5 14;
                    """);
            else tb.setStyle("""
                    -fx-background-color: #313244;
                    -fx-text-fill: #a6adc8;
                    -fx-background-radius: 6;
                    -fx-padding: 5 14;
                    """);
        });
        return tb;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
