package com.todo.model;

public class TodoItem {
    private static int idCounter = 1;

    private int id;
    private String title;
    private boolean completed;
    private Priority priority;

    public enum Priority {
        HIGH, MEDIUM, LOW
    }

    /** Normal constructor — yeni görev oluştururken */
    public TodoItem(String title, Priority priority) {
        this.id = idCounter++;
        this.title = title;
        this.completed = false;
        this.priority = priority;
    }

    /**
     * Yükleme constructor'ı — JSON'dan geri yüklerken kullanılır.
     * ID dışarıdan verilir; idCounter otomatik güncellenir.
     */
    public TodoItem(int id, String title, Priority priority, boolean completed) {
        this.id        = id;
        this.title     = title;
        this.priority  = priority;
        this.completed = completed;
        if (id >= idCounter) idCounter = id + 1;
    }

    /** Yükleme öncesinde idCounter'ı sıfırla */
    public static void resetIdCounter() { idCounter = 1; }

    // Getters & Setters
    public int getId()                  { return id; }
    public String getTitle()            { return title; }
    public void setTitle(String t)      { this.title = t; }
    public boolean isCompleted()        { return completed; }
    public void setCompleted(boolean c) { this.completed = c; }
    public Priority getPriority()       { return priority; }
    public void setPriority(Priority p) { this.priority = p; }

    @Override
    public String toString() {
        return "[" + priority + "] " + title + (completed ? " ✓" : "");
    }
}
