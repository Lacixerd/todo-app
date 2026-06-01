package com.todo.dsa;

import java.util.NoSuchElementException;

/**
 * Custom Queue implementation (FIFO) for task processing order.
 * HIGH priority görevler önce işlenmek üzere kuyruğa alınır.
 */
public class TaskQueue<T> {

    private Node<T> front;
    private Node<T> rear;
    private int size;

    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    public TaskQueue() {
        front = null;
        rear = null;
        size = 0;
    }

    /** Kuyruğun sonuna ekle */
    public void enqueue(T item) {
        Node<T> newNode = new Node<>(item);
        if (rear == null) {
            front = newNode;
            rear = newNode;
        } else {
            rear.next = newNode;
            rear = newNode;
        }
        size++;
    }

    /** Kuyruktan önden çıkar */
    public T dequeue() {
        if (isEmpty()) throw new NoSuchElementException("Kuyruk boş");
        T data = front.data;
        front = front.next;
        if (front == null) rear = null;
        size--;
        return data;
    }

    /** Sıradaki elemana bak (silme) */
    public T peek() {
        if (isEmpty()) throw new NoSuchElementException("Kuyruk boş");
        return front.data;
    }

    public boolean isEmpty() { return front == null; }
    public int size() { return size; }

    public void clear() {
        front = null;
        rear = null;
        size = 0;
    }
}
