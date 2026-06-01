package com.todo.dsa;

import com.todo.model.TodoItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Binary Search Tree — görevleri önce priority (HIGH < MEDIUM < LOW),
 * sonra ID'ye göre sıralar.
 *
 * Ağaç Penceresi bu yapıyı görsel olarak çizer.
 *
 *        HIGH(1)
 *       /        \
 *  HIGH(0)     MEDIUM(2)
 *                   \
 *                  LOW(3)
 *
 * Karşılaştırma kuralı:
 *   HIGH  = 0  (en küçük → sola)
 *   MEDIUM = 1
 *   LOW   = 2  (en büyük → sağa)
 */
public class TaskBST {

    public static class BSTNode {
        public TodoItem data;
        public BSTNode left;
        public BSTNode right;

        public BSTNode(TodoItem data) {
            this.data = data;
        }
    }

    private BSTNode root;

    // ── INSERT ────────────────────────────────────────────────────────────

    public void insert(TodoItem item) {
        root = insertRec(root, item);
    }

    private BSTNode insertRec(BSTNode node, TodoItem item) {
        if (node == null) return new BSTNode(item);

        int cmp = compare(item, node.data);
        if (cmp < 0)       node.left  = insertRec(node.left,  item);
        else if (cmp > 0)  node.right = insertRec(node.right, item);
        // eşit ID → güncelle
        else               node.data  = item;

        return node;
    }

    // ── DELETE ────────────────────────────────────────────────────────────

    public void delete(int id) {
        root = deleteRec(root, id);
    }

    private BSTNode deleteRec(BSTNode node, int id) {
        if (node == null) return null;

        if (node.data.getId() == id) {
            if (node.left == null)  return node.right;
            if (node.right == null) return node.left;
            // İki çocuk varsa → in-order successor (en sol sağ çocuk)
            BSTNode successor = findMin(node.right);
            node.data  = successor.data;
            node.right = deleteRec(node.right, successor.data.getId());
        } else if (id < node.data.getId()) {
            node.left  = deleteRec(node.left,  id);
        } else {
            node.right = deleteRec(node.right, id);
        }
        return node;
    }

    private BSTNode findMin(BSTNode node) {
        while (node.left != null) node = node.left;
        return node;
    }

    // ── REBUILD ───────────────────────────────────────────────────────────

    /** Tüm ağacı temizleyip listeden yeniden inşa et */
    public void rebuild(List<TodoItem> items) {
        root = null;
        for (TodoItem item : items) insert(item);
    }

    // ── TRAVERSAL ─────────────────────────────────────────────────────────

    /** In-order dolaşım → sıralı liste (HIGH önce) */
    public List<TodoItem> inOrder() {
        List<TodoItem> result = new ArrayList<>();
        inOrderRec(root, result);
        return result;
    }

    private void inOrderRec(BSTNode node, List<TodoItem> result) {
        if (node == null) return;
        inOrderRec(node.left, result);
        result.add(node.data);
        inOrderRec(node.right, result);
    }

    public BSTNode getRoot() { return root; }
    public boolean isEmpty() { return root == null; }

    // ── COMPARE ───────────────────────────────────────────────────────────

    private int compare(TodoItem a, TodoItem b) {
        int pa = priorityValue(a.getPriority());
        int pb = priorityValue(b.getPriority());
        if (pa != pb) return pa - pb;
        return a.getId() - b.getId();
    }

    private int priorityValue(TodoItem.Priority p) {
        return switch (p) {
            case HIGH   -> 0;
            case MEDIUM -> 1;
            case LOW    -> 2;
        };
    }
}
