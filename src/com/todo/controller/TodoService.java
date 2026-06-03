package com.todo.controller;

import com.todo.dsa.TaskBST;
import com.todo.dsa.TaskQueue;
import com.todo.dsa.TodoLinkedList;
import com.todo.dsa.UndoStack;
import com.todo.model.Action;
import com.todo.model.TodoItem;
import com.todo.storage.TodoStorageService;

import java.util.ArrayList;
import java.util.List;

/**
 * TodoService — tüm DSA yapılarını yöneten servis katmanı.
 */
public class TodoService {

    private final TodoLinkedList<TodoItem> taskList = new TodoLinkedList<>();
    private final UndoStack<Action> undoStack = new UndoStack<>();
    private final UndoStack<Action> redoStack  = new UndoStack<>();
    private final TaskQueue<TodoItem> highPriorityQueue = new TaskQueue<>();
    private final TaskBST bst = new TaskBST();
    private final TodoStorageService storage = new TodoStorageService();

    // veri yükleme/kaydetöe

    public void loadFromDisk() {
        List<TodoItem> loaded = storage.load();
        for (TodoItem item : loaded) {
            taskList.addLast(item);
            bst.insert(item);
            if (item.getPriority() == TodoItem.Priority.HIGH && !item.isCompleted())
                highPriorityQueue.enqueue(item);
        }
    }

    public void saveToDisk(){ storage.save(getAllTasks()); }
    public String getSaveFilePath() { return storage.getSaveFilePath(); }

    // ANA GÖREV  

    public void addTask(String title, TodoItem.Priority priority) {
        TodoItem item = new TodoItem(title, priority);
        taskList.addLast(item);
        bst.insert(item);
        undoStack.push(new Action(Action.Type.ADD, item));
        redoStack.clear();
        if (priority == TodoItem.Priority.HIGH) highPriorityQueue.enqueue(item);
    }

    public boolean deleteTask(int id) {
        TodoItem item = taskList.findById(id, TodoItem::getId);
        if (item == null) return false;
        taskList.removeById(id, TodoItem::getId);
        bst.delete(id);
        undoStack.push(new Action(Action.Type.DELETE, item));
        redoStack.clear();
        return true;
    }

    //Görev tamamlama
    public String completeTask(int id) {
        TodoItem item = taskList.findById(id, TodoItem::getId);
        if (item == null) return "Görev bulunamadı.";

        if (!item.allChildrenCompleted()) {
            int done  = item.completedChildCount();
            int total = item.totalChildCount();
            return "Önce alt görevleri tamamla! (" + done + "/" + total + " tamamlandı)";
        }

        item.setCompleted(true);
        bst.insert(item);
        undoStack.push(new Action(Action.Type.COMPLETE, item));
        redoStack.clear();
        return null; // null = başarılı
    }

    // Alt Görevler

    /**
     * Seçili ana göreve alt görev ekler.
     * @return eklenen alt görev, veya null (parent bulunamazsa)
     */
    public TodoItem addSubTask(int parentId, String title, TodoItem.Priority priority) {
        TodoItem parent = taskList.findById(parentId, TodoItem::getId);
        if (parent == null) return null;

        TodoItem child = new TodoItem(title, priority);
        parent.addChild(child);
        // Undo: parent referansı ile kaydedilir
        undoStack.push(new Action(Action.Type.ADD_SUB, child, parent));
        redoStack.clear();
        return child;
    }

    /**
     * Alt görevi tamamla.
     * Kendi alt görevleri varsa onlar da bitmiş olmalı.
     */

    public String completeSubTask(int parentId, int childId) {
        TodoItem parent = taskList.findById(parentId, TodoItem::getId);
        if (parent == null) return "Ana görev bulunamadı.";

        TodoItem child = parent.findChild(childId);
        if (child == null) return "Alt görev bulunamadı.";

        if (!child.allChildrenCompleted()) {
            return "⚠ Alt görevin kendi alt görevleri bitmedi!";
        }

        child.setCompleted(true);
        undoStack.push(new Action(Action.Type.COMPLETE_SUB, child, parent));
        redoStack.clear();
        return null;
    }

    public boolean deleteSubTask(int parentId, int childId) {
        TodoItem parent = taskList.findById(parentId, TodoItem::getId);
        if (parent == null) return false;

        TodoItem child = parent.findChild(childId);
        if (child == null) return false;

        parent.removeChild(childId);
        undoStack.push(new Action(Action.Type.DELETE_SUB, child, parent));
        redoStack.clear();
        return true;
    }

    // Undo/Redo stack işlemleri

    public String undo() {
        if (undoStack.isEmpty()) return null;
        Action action = undoStack.pop();

        switch (action.getType()) {
            case ADD         -> { taskList.removeById(action.getItem().getId(), TodoItem::getId); bst.delete(action.getItem().getId()); }
            case DELETE      -> { taskList.addLast(action.getItem()); bst.insert(action.getItem()); }
            case COMPLETE    -> { action.getItem().setCompleted(false); bst.insert(action.getItem()); }
            case ADD_SUB     -> action.getParent().removeChild(action.getItem().getId());
            case DELETE_SUB  -> action.getParent().addChild(action.getItem());
            case COMPLETE_SUB-> action.getItem().setCompleted(false);
        }

        redoStack.push(action);
        return "Geri alındı: " + action;
    }

    public String redo() {
        if (redoStack.isEmpty()) return null;
        Action action = redoStack.pop();

        switch (action.getType()) {
            case ADD -> { taskList.addLast(action.getItem()); bst.insert(action.getItem()); }
            case DELETE -> { taskList.removeById(action.getItem().getId(), TodoItem::getId); bst.delete(action.getItem().getId()); }
            case COMPLETE -> { action.getItem().setCompleted(true); bst.insert(action.getItem()); }
            case ADD_SUB -> action.getParent().addChild(action.getItem());
            case DELETE_SUB -> action.getParent().removeChild(action.getItem().getId());
            case COMPLETE_SUB-> action.getItem().setCompleted(true);
        }

        undoStack.push(action);
        return "İleri alındı: " + action;
    }

    // Queue ve BST
    public TodoItem pollNextHighPriority() {
        if (highPriorityQueue.isEmpty()) return null;
        return highPriorityQueue.dequeue();
    }

    public int highPriorityQueueSize() { return highPriorityQueue.size(); }
    public TaskBST getBST()            { return bst; }

    // görev get işlemleri

    public List<TodoItem> getAllTasks() {
        List<TodoItem> r = new ArrayList<>();
        for (TodoItem t : taskList) r.add(t);
        return r;
    }

    public List<TodoItem> getActiveTasks() {
        List<TodoItem> r = new ArrayList<>();
        for (TodoItem t : taskList) if (!t.isCompleted()) r.add(t);
        return r;
    }

    public List<TodoItem> getCompletedTasks() {
        List<TodoItem> r = new ArrayList<>();
        for (TodoItem t : taskList) if (t.isCompleted()) r.add(t);
        return r;
    }

    //ID'ye göre görev bulma
    public TodoItem findTask(int id) {
        return taskList.findById(id, TodoItem::getId);
    }

    public UndoStack<Action> getUndoStack() { return undoStack; }
    public UndoStack<Action> getRedoStack() { return redoStack; }
    public boolean canUndo()  { return !undoStack.isEmpty(); }
    public boolean canRedo()  { return !redoStack.isEmpty(); }
    public int totalTasks()   { return taskList.size(); }
}
