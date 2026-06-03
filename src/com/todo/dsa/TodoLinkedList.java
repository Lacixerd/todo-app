package com.todo.dsa;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Custom Singly LinkedList implementation for storing TodoItems.
 * Used as the main data store for all tasks.
 */
public class TodoLinkedList<T> implements Iterable<T> {

    private Node<T> head;
    private int size;

    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    public TodoLinkedList() {
        head = null;
        size = 0;
    }

    /** Listenin sonuna eleman ekle */
    public void addLast(T data) {
        Node<T> newNode = new Node<>(data);
        if (head == null) {
            head = newNode;
        } else {
            Node<T> current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
        size++;
    }

    /** Listenin başına eleman ekle */
    public void addFirst(T data) {
        Node<T> newNode = new Node<>(data);
        newNode.next = head;
        head = newNode;
        size++;
    }

    /** ID'ye göre eleman sil */
    public boolean removeById(int id, java.util.function.ToIntFunction<T> idExtractor) {
        if (head == null) return false;

        if (idExtractor.applyAsInt(head.data) == id) {
            head = head.next;
            size--;
            return true;
        }

        Node<T> current = head;
        while (current.next != null) {
            if (idExtractor.applyAsInt(current.next.data) == id) {
                current.next = current.next.next;
                size--;
                return true;
            }
            current = current.next;
        }
        return false;
    }

    /** ID'ye göre eleman bul */
    public T findById(int id, java.util.function.ToIntFunction<T> idExtractor) {
        Node<T> current = head;
        while (current != null) {
            if (idExtractor.applyAsInt(current.data) == id) {
                return current.data;
            }
            current = current.next;
        }
        return null;
    }

    public T getFirst() {
        if (head == null) throw new NoSuchElementException("Liste boş");
        return head.data;
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    public void clear() {
        head = null;
        size = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            Node<T> current = head;

            @Override
            public boolean hasNext() { return current != null; }

            @Override
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                T data = current.data;
                current = current.next;
                return data;
            }
        };
    }
}
