package com.todo.model;

import com.todo.dsa.TodoLinkedList;

/**
 * TodoItem — bir görevi temsil eder.
 *
 * Alt görevler (subtask'lar) aynı TodoLinkedList yapısıyla tutulur.
 * Bir görev tamamlanabilmek için tüm alt görevlerinin önce tamamlanmış olması gerekir.
 */
public class TodoItem {
    private static int idCounter = 1;

    private int    id;
    private String title;
    private boolean completed;
    private Priority priority;

    /** Alt görevler — aynı custom LinkedList yapısını kullanır */
    private final TodoLinkedList<TodoItem> children = new TodoLinkedList<>();

    public enum Priority { HIGH, MEDIUM, LOW }

    // Constructorlar

    /** Yeni görev oluşturma */
    public TodoItem(String title, Priority priority) {
        this.id       = idCounter++;
        this.title    = title;
        this.completed = false;
        this.priority = priority;
    }

    /** JSON'dan geri yüklerken ID dışarıdan verilir */
    public TodoItem(int id, String title, Priority priority, boolean completed) {
        this.id        = id;
        this.title     = title;
        this.priority  = priority;
        this.completed = completed;
        if (id >= idCounter) idCounter = id + 1;
    }

    public static void resetIdCounter() { idCounter = 1; }

    // Alt görev işlemleri

    public void addChild(TodoItem child) {
        children.addLast(child);
    }

    public boolean removeChild(int id) {
        return children.removeById(id, TodoItem::getId);
    }

    public TodoItem findChild(int id) {
        return children.findById(id, TodoItem::getId);
    }

    /** Tüm alt görevler tamamlandı mı? (boş ise true döner) */
    public boolean allChildrenCompleted() {
        for (TodoItem child : children) {
            if (!child.isCompleted()) return false;
        }
        return true;
    }

    public boolean hasChildren() { return !children.isEmpty(); }

    public TodoLinkedList<TodoItem> getChildren() { return children; }

    /** Alt görevlerin kaçının tamamlandığı */
    public int completedChildCount() {
        int count = 0;
        for (TodoItem c : children) if (c.isCompleted()) count++;
        return count;
    }

    public int totalChildCount() { return children.size(); }

    // Getter/Setter metodları

    public int getId()                  { return id; }
    public String getTitle()            { return title; }
    public void setTitle(String t)      { this.title = t; }
    public boolean isCompleted()        { return completed; }
    public void setCompleted(boolean c) { this.completed = c; }
    public Priority getPriority()       { return priority; }
    public void setPriority(Priority p) { this.priority = p; }

    @Override
    public String toString() {
        String sub = hasChildren()
                ? " [" + completedChildCount() + "/" + totalChildCount() + "]"
                : "";
        return "[" + priority + "] " + title + sub + (completed ? " ✓" : "");
    }
}
