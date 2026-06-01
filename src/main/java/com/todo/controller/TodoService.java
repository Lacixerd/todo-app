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
 *
 *  LinkedList        → görev deposu
 *  UndoStack         → geri alma geçmişi   (↩)
 *  UndoStack         → redo geçmişi        (↪)
 *  TaskQueue         → HIGH priority kuyruğu
 *  TaskBST           → ağaç görünümü
 *  TodoStorageService→ JSON kayıt/yükleme
 */
public class TodoService {

    private final TodoLinkedList<TodoItem> taskList          = new TodoLinkedList<>();
    private final UndoStack<Action>        undoStack         = new UndoStack<>();
    private final UndoStack<Action>        redoStack         = new UndoStack<>();
    private final TaskQueue<TodoItem>      highPriorityQueue = new TaskQueue<>();
    private final TaskBST                  bst               = new TaskBST();
    private final TodoStorageService       storage           = new TodoStorageService();

    // ── DISK ──────────────────────────────────────────────────────────────

    /**
     * Uygulama açılışında çağrılır.
     * Kaydedilmiş görevleri JSON'dan yükler ve tüm yapılara ekler.
     */
    public void loadFromDisk() {
        List<TodoItem> loaded = storage.load();
        for (TodoItem item : loaded) {
            taskList.addLast(item);
            bst.insert(item);
            if (item.getPriority() == TodoItem.Priority.HIGH && !item.isCompleted()) {
                highPriorityQueue.enqueue(item);
            }
        }
        // Undo/redo geçmişi oturuma özgü — diskten yüklenmez
    }

    /**
     * Uygulama kapanışında çağrılır.
     * Mevcut görev listesini JSON'a kaydeder.
     */
    public void saveToDisk() {
        storage.save(getAllTasks());
    }

    /** Kayıt dosyasının yolunu döndür (UI'da bilgi olarak gösterilebilir) */
    public String getSaveFilePath() {
        return storage.getSaveFilePath();
    }

    // ── CRUD ─────────────────────────────────────────────────────────────

    public void addTask(String title, TodoItem.Priority priority) {
        TodoItem item = new TodoItem(title, priority);
        taskList.addLast(item);
        bst.insert(item);

        undoStack.push(new Action(Action.Type.ADD, item));
        redoStack.clear();

        if (priority == TodoItem.Priority.HIGH) {
            highPriorityQueue.enqueue(item);
        }
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

    public boolean completeTask(int id) {
        TodoItem item = taskList.findById(id, TodoItem::getId);
        if (item == null) return false;

        item.setCompleted(true);
        bst.insert(item);

        undoStack.push(new Action(Action.Type.COMPLETE, item));
        redoStack.clear();
        return true;
    }

    // ── UNDO / REDO ───────────────────────────────────────────────────────

    public String undo() {
        if (undoStack.isEmpty()) return null;
        Action action = undoStack.pop();

        switch (action.getType()) {
            case ADD      -> { taskList.removeById(action.getItem().getId(), TodoItem::getId); bst.delete(action.getItem().getId()); }
            case DELETE   -> { taskList.addLast(action.getItem()); bst.insert(action.getItem()); }
            case COMPLETE -> { action.getItem().setCompleted(false); bst.insert(action.getItem()); }
        }

        redoStack.push(action);
        return "Geri alındı: " + action;
    }

    public String redo() {
        if (redoStack.isEmpty()) return null;
        Action action = redoStack.pop();

        switch (action.getType()) {
            case ADD      -> { taskList.addLast(action.getItem()); bst.insert(action.getItem()); }
            case DELETE   -> { taskList.removeById(action.getItem().getId(), TodoItem::getId); bst.delete(action.getItem().getId()); }
            case COMPLETE -> { action.getItem().setCompleted(true); bst.insert(action.getItem()); }
        }

        undoStack.push(action);
        return "İleri alındı: " + action;
    }

    // ── QUEUE ─────────────────────────────────────────────────────────────

    public TodoItem pollNextHighPriority() {
        if (highPriorityQueue.isEmpty()) return null;
        return highPriorityQueue.dequeue();
    }

    public int highPriorityQueueSize() { return highPriorityQueue.size(); }

    // ── BST / QUERIES ────────────────────────────────────────────────────

    public TaskBST getBST() { return bst; }

    public List<TodoItem> getAllTasks() {
        List<TodoItem> result = new ArrayList<>();
        for (TodoItem item : taskList) result.add(item);
        return result;
    }

    public List<TodoItem> getActiveTasks() {
        List<TodoItem> result = new ArrayList<>();
        for (TodoItem item : taskList) if (!item.isCompleted()) result.add(item);
        return result;
    }

    public List<TodoItem> getCompletedTasks() {
        List<TodoItem> result = new ArrayList<>();
        for (TodoItem item : taskList) if (item.isCompleted()) result.add(item);
        return result;
    }

    public UndoStack<Action> getUndoStack() { return undoStack; }
    public UndoStack<Action> getRedoStack() { return redoStack; }

    public boolean canUndo()  { return !undoStack.isEmpty(); }
    public boolean canRedo()  { return !redoStack.isEmpty(); }
    public int totalTasks()   { return taskList.size(); }
}
