package com.todo.model;

/**
 * Undo stack'te tutulacak aksiyon modeli.
 */
public class Action {

    public enum Type {
        ADD, DELETE, COMPLETE,ADD_SUB,COMPLETE_SUB,DELETE_SUB
    }

    private Type type;
    private TodoItem item;
    private TodoItem child;
    private TodoItem parent;

    public Action(Type type, TodoItem item) {
        this.type = type;
        this.item = item;
    }

    public Action(Type type, TodoItem child,TodoItem parent) {
        this.type = type;
        this.child = child;
        this.parent = parent;
    }

    public Type getType() { return type; }
    public TodoItem getItem() { return item; }

    public TodoItem getChild() {
        return child;
    }

    public TodoItem getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return type + ": " + item.getTitle();
    }
}
