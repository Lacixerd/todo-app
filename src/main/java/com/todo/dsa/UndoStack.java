package com.todo.dsa;

import java.util.EmptyStackException;

/**
 * Custom Stack implementation (LIFO).
 * UndoStack ve RedoStack aynı yapıyı paylaşır —
 * RedoStack bu sınıfın ikinci bir instance'ıdır.
 *
 * Undo flow:
 *   aksiyon → undoStack.push()
 *   ↩ Geri Al → undoStack.pop() → tersini uygula → redoStack.push()
 *   ↪ İleri Al → redoStack.pop() → yeniden uygula → undoStack.push()
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

    /** Aksiyonu stack'e ekle */
    public void push(T item) {
        top = new Node<>(item, top);
        size++;
    }

    /** En son aksiyonu al ve stack'ten çıkar */
    public T pop() {
        if (isEmpty()) throw new EmptyStackException();
        T data = top.data;
        top = top.next;
        size--;
        return data;
    }

    /** En üstteki elemana bak (silme) */
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

    /** Stack içeriğini liste olarak döndür (top → bottom sırası) */
    public java.util.List<T> toList() {
        java.util.List<T> list = new java.util.ArrayList<>();
        Node<T> cur = top;
        while (cur != null) { list.add(cur.data); cur = cur.next; }
        return list;
    }
}
