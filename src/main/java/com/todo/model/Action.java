package com.todo.model;

/**
 * Undo stack'te tutulacak aksiyon modeli.
 */
public class Action {

    public enum Type {
        ADD, DELETE, COMPLETE
    }

    private Type type;
    private TodoItem item;

    public Action(Type type, TodoItem item) {
        this.type = type;
        this.item = item;
    }

    public Type getType() { return type; }
    public TodoItem getItem() { return item; }

    @Override
    public String toString() {
        return type + ": " + item.getTitle();
    }
}
