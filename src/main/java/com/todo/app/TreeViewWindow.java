package com.todo.app;

import com.todo.controller.TodoService;
import com.todo.dsa.TaskBST;
import com.todo.model.Action;
import com.todo.model.TodoItem;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.List;

/**
 * Ağaç Görünüm Penceresi
 *
 * Üç bölüm:
 *   1. BST — görevler priority+id'ye göre düzenlenmiş Binary Search Tree
 *   2. Undo Stack — en üstten alta doğru kutu dizisi
 *   3. Redo Stack — en üstten alta doğru kutu dizisi
 */
public class TreeViewWindow {

    // Düğüm boyutları
    private static final int NODE_W  = 150;
    private static final int NODE_H  = 44;
    private static final int V_GAP   = 60;   // Dikey boşluk
    private static final int H_GAP   = 20;   // Yatay minimum boşluk

    // Renk paleti (Catppuccin Mocha)
    private static final String BG        = "#1e1e2e";
    private static final String SURFACE   = "#181825";
    private static final String OVERLAY   = "#313244";
    private static final String TEXT      = "#cdd6f4";
    private static final String SUBTEXT   = "#a6adc8";
    private static final String MUTED     = "#6c7086";

    private static final String C_HIGH    = "#f38ba8"; // kırmızı
    private static final String C_MEDIUM  = "#f9e2af"; // sarı
    private static final String C_LOW     = "#a6e3a1"; // yeşil
    private static final String C_DONE    = "#585b70"; // gri
    private static final String C_EDGE    = "#45475a";
    private static final String C_UNDO    = "#fab387"; // turuncu
    private static final String C_REDO    = "#89dceb"; // açık mavi

    private final TodoService service;
    private Stage stage;

    public TreeViewWindow(TodoService service) {
        this.service = service;
    }

    public void show() {
        if (stage != null && stage.isShowing()) {
            refresh();
            stage.toFront();
            return;
        }

        stage = new Stage();
        stage.setTitle("🌳 DSA Ağaç Görünümü");

        ScrollPane scroll = new ScrollPane(buildContent());
        scroll.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");
        scroll.setFitToWidth(true);

        Scene scene = new Scene(scroll, 1000, 700);
        stage.setScene(scene);
        stage.show();
    }

    public void refresh() {
        if (stage != null && stage.isShowing()) {
            ScrollPane scroll = (ScrollPane) stage.getScene().getRoot();
            scroll.setContent(buildContent());
        }
    }

    // ── CONTENT ───────────────────────────────────────────────────────────

    private VBox buildContent() {
        VBox root = new VBox(24);
        root.setStyle("-fx-background-color: " + BG + ";");
        root.setPadding(new Insets(24));

        root.getChildren().add(sectionTitle("🌳 Binary Search Tree  (Sıralama: HIGH → MEDIUM → LOW, aynı önceliklerde ID'ye göre)"));
        root.getChildren().add(buildBSTSection());

        root.getChildren().add(sectionTitle("↩ Undo Stack  (LIFO — en üstte son yapılan işlem)"));
        root.getChildren().add(buildStackSection(service.getUndoStack().toList(), C_UNDO));

        root.getChildren().add(sectionTitle("↪ Redo Stack  (LIFO — en üstte son geri alınan işlem)"));
        root.getChildren().add(buildStackSection(service.getRedoStack().toList(), C_REDO));

        return root;
    }

    // ── BST ───────────────────────────────────────────────────────────────

    private Pane buildBSTSection() {
        TaskBST bst = service.getBST();

        if (bst.isEmpty()) {
            return emptyPlaceholder("Henüz görev yok — ana ekrandan görev ekleyin.");
        }

        // Önce ağacın boyutunu hesapla
        int depth   = treeDepth(bst.getRoot());
        int leafCount = leafCount(bst.getRoot());
        int canvasW = Math.max(900, leafCount * (NODE_W + H_GAP) + 80);
        int canvasH = (depth + 1) * (NODE_H + V_GAP) + 60;

        Canvas canvas = new Canvas(canvasW, canvasH);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Arka plan
        gc.setFill(Color.web(SURFACE));
        gc.fillRoundRect(0, 0, canvasW, canvasH, 12, 12);

        // Ağacı çiz
        drawBSTNode(gc, bst.getRoot(), canvasW / 2.0, 40, canvasW / 4.0, 0);

        // Legend
        drawLegend(gc, canvasW);

        Pane pane = new Pane(canvas);
        pane.setMinHeight(canvasH);
        return pane;
    }

    /**
     * Özyinelemeli ağaç çizimi.
     * @param cx  bu düğümün merkez X
     * @param cy  bu düğümün merkez Y
     * @param offset  çocukların yatay sapması
     */
    private void drawBSTNode(GraphicsContext gc, TaskBST.BSTNode node,
                              double cx, double cy, double offset, int depth) {
        if (node == null) return;

        double childY = cy + NODE_H + V_GAP;

        // Sol çocuğa kenar
        if (node.left != null) {
            double childX = cx - offset;
            gc.setStroke(Color.web(C_EDGE));
            gc.setLineWidth(1.5);
            gc.strokeLine(cx, cy + NODE_H / 2.0, childX, childY + NODE_H / 2.0);
            drawBSTNode(gc, node.left, childX, childY, offset / 2, depth + 1);
        }

        // Sağ çocuğa kenar
        if (node.right != null) {
            double childX = cx + offset;
            gc.setStroke(Color.web(C_EDGE));
            gc.setLineWidth(1.5);
            gc.strokeLine(cx, cy + NODE_H / 2.0, childX, childY + NODE_H / 2.0);
            drawBSTNode(gc, node.right, childX, childY, offset / 2, depth + 1);
        }

        // Düğüm kutusu
        drawTaskNode(gc, node.data, cx - NODE_W / 2.0, cy);
    }

    private void drawTaskNode(GraphicsContext gc, TodoItem item, double x, double y) {
        String color = item.isCompleted() ? C_DONE : switch (item.getPriority()) {
            case HIGH   -> C_HIGH;
            case MEDIUM -> C_MEDIUM;
            case LOW    -> C_LOW;
        };

        // Gölge
        gc.setFill(Color.rgb(0, 0, 0, 0.3));
        gc.fillRoundRect(x + 3, y + 3, NODE_W, NODE_H, 8, 8);

        // Kutu
        gc.setFill(Color.web(OVERLAY));
        gc.fillRoundRect(x, y, NODE_W, NODE_H, 8, 8);

        // Sol renkli şerit (priority göstergesi)
        gc.setFill(Color.web(color));
        gc.fillRoundRect(x, y, 5, NODE_H, 4, 4);

        // Kenar çizgisi
        gc.setStroke(Color.web(color, 0.5));
        gc.setLineWidth(1);
        gc.strokeRoundRect(x, y, NODE_W, NODE_H, 8, 8);

        // Başlık
        String title = item.getTitle();
        if (title.length() > 14) title = title.substring(0, 13) + "…";
        gc.setFill(item.isCompleted() ? Color.web(MUTED) : Color.web(TEXT));
        gc.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(title, x + 12, y + 17);

        // Alt satır: öncelik + ID
        String priorityLabel = switch (item.getPriority()) {
            case HIGH   -> "HIGH";
            case MEDIUM -> "MED";
            case LOW    -> "LOW";
        };
        gc.setFill(Color.web(color, 0.9));
        gc.setFont(Font.font("Monospace", 10));
        gc.fillText(priorityLabel + "  #" + item.getId() + (item.isCompleted() ? "  ✓" : ""),
                x + 12, y + 33);
    }

    private void drawLegend(GraphicsContext gc, double canvasW) {
        double lx = 12;
        double ly = 10;
        String[] labels = {"HIGH", "MEDIUM", "LOW", "Tamamlandı"};
        String[] colors = {C_HIGH, C_MEDIUM, C_LOW, C_DONE};

        for (int i = 0; i < labels.length; i++) {
            double x = lx + i * 110;
            gc.setFill(Color.web(colors[i]));
            gc.fillRoundRect(x, ly, 10, 10, 3, 3);
            gc.setFill(Color.web(SUBTEXT));
            gc.setFont(Font.font("Segoe UI", 11));
            gc.setTextAlign(TextAlignment.LEFT);
            gc.fillText(labels[i], x + 14, ly + 10);
        }
    }

    // ── STACK GÖRÜNÜMÜ ────────────────────────────────────────────────────

    private Pane buildStackSection(List<Action> items, String accentColor) {
        if (items.isEmpty()) {
            return emptyPlaceholder("Stack boş.");
        }

        int boxW   = 280;
        int boxH   = 46;
        int gap    = 6;
        int canvasW = boxW + 120;
        int canvasH = items.size() * (boxH + gap) + 60;

        Canvas canvas = new Canvas(canvasW, canvasH);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.web(SURFACE));
        gc.fillRoundRect(0, 0, canvasW, canvasH, 10, 10);

        // "TOP" etiketi
        gc.setFill(Color.web(accentColor));
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("▼ TOP", 40 + boxW / 2.0, 20);

        for (int i = 0; i < items.size(); i++) {
            double x = 40;
            double y = 28 + i * (boxH + gap);
            Action action = items.get(i);

            // Bağlayıcı ok (ilk hariç)
            if (i > 0) {
                gc.setStroke(Color.web(accentColor, 0.4));
                gc.setLineWidth(1.5);
                gc.strokeLine(x + boxW / 2.0, y - gap, x + boxW / 2.0, y);
            }

            // Kutu
            gc.setFill(Color.web(OVERLAY));
            gc.fillRoundRect(x, y, boxW, boxH, 6, 6);

            gc.setStroke(Color.web(accentColor, i == 0 ? 0.9 : 0.3));
            gc.setLineWidth(1.2);
            gc.strokeRoundRect(x, y, boxW, boxH, 6, 6);

            // Sol şerit
            gc.setFill(Color.web(accentColor, i == 0 ? 1.0 : 0.4));
            gc.fillRoundRect(x, y, 4, boxH, 3, 3);

            // İkon + aksiyon tipi
            String icon = switch (action.getType()) {
                case ADD      -> "➕";
                case DELETE   -> "🗑";
                case COMPLETE -> "✔";
            };
            gc.setFill(i == 0 ? Color.web(TEXT) : Color.web(SUBTEXT));
            gc.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
            gc.setTextAlign(TextAlignment.LEFT);
            gc.fillText(icon + "  " + action.getType() + ": " + truncate(action.getItem().getTitle(), 22),
                    x + 12, y + 18);

            // Öncelik
            String priorityLabel = switch (action.getItem().getPriority()) {
                case HIGH   -> "HIGH";
                case MEDIUM -> "MED";
                case LOW    -> "LOW";
            };
            gc.setFill(Color.web(MUTED));
            gc.setFont(Font.font("Monospace", 10));
            gc.fillText(priorityLabel + "  #" + action.getItem().getId(), x + 12, y + 34);

            // İndeks
            gc.setFill(Color.web(accentColor, 0.5));
            gc.setFont(Font.font("Monospace", 10));
            gc.setTextAlign(TextAlignment.RIGHT);
            gc.fillText("[" + i + "]", x + boxW - 8, y + 27);
        }

        // "BOTTOM" etiketi
        double bottomY = 28 + items.size() * (boxH + gap) + 4;
        gc.setFill(Color.web(accentColor, 0.4));
        gc.setFont(Font.font("Monospace", 10));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("▲ BOTTOM", 40 + boxW / 2.0, bottomY + 12);

        Pane pane = new Pane(canvas);
        pane.setMinHeight(canvasH);
        return pane;
    }

    // ── YARDIMCI ─────────────────────────────────────────────────────────

    private Pane emptyPlaceholder(String msg) {
        Canvas canvas = new Canvas(500, 60);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.web(SURFACE));
        gc.fillRoundRect(0, 0, 500, 60, 10, 10);
        gc.setFill(Color.web(MUTED));
        gc.setFont(Font.font("Segoe UI", 13));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(msg, 20, 36);
        return new Pane(canvas);
    }

    private Label sectionTitle(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lbl.setTextFill(Color.web(SUBTEXT));
        lbl.setPadding(new Insets(4, 0, 4, 0));
        return lbl;
    }

    private int treeDepth(TaskBST.BSTNode node) {
        if (node == null) return -1;
        return 1 + Math.max(treeDepth(node.left), treeDepth(node.right));
    }

    private int leafCount(TaskBST.BSTNode node) {
        if (node == null) return 0;
        if (node.left == null && node.right == null) return 1;
        return leafCount(node.left) + leafCount(node.right);
    }

    private String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}
