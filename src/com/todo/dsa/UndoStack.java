package com.todo.dsa;

import java.util.EmptyStackException;

/**
 * UndoStack ve RedoStack aynı yapıyı paylaşır

 *   action  undoStack.push()
 *    Geri Al , undoStack.pop(),  tersini uygula, redoStack.push()
 *    İleri Al , redoStack.pop(), yeniden uygula,  undoStack.push()
 */
public class UndoStack<T> {

    private Node<T> top;
    private int size;

    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T data, Node<T> next) {
            this.data = data;
            this.next = next;
        }
    }

    public UndoStack() {
        top = null;
        size = 0;
    }

    public void push(T item) {
        top = new Node<>(item, top);
        size++;
    }

    public T pop() {
        if (isEmpty()) throw new EmptyStackException();
        T data = top.data;
        top = top.next;
        size--;
        return data;
    }

    public T peek() {
        if (isEmpty()) throw new EmptyStackException();
        return top.data;
    }

    public boolean isEmpty() { return top == null; }
    public int size() { return size; }

    public void clear() {
        top = null;
        size = 0;
    }

    public java.util.List<T> toList() {
        java.util.List<T> list = new java.util.ArrayList<>();
        Node<T> cur = top;
        while (cur != null) { list.add(cur.data); cur = cur.next; }
        return list;
    }
}
