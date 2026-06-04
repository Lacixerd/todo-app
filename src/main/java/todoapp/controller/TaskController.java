package todoapp.controller;

import todoapp.datastructure.BinarySearchTree;
import todoapp.model.Priority;
import todoapp.model.Task;
import todoapp.model.TaskRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class TaskController {

    public enum FilterType {
        ALL, ACTIVE, COMPLETED
    }

    private final TaskRepository repository;
    private final Stack<Task> undoStack;
    private final BinarySearchTree<String> priorityTree;
    private Runnable onDataChange;

    public TaskController(TaskRepository repository) {
        this.repository = repository;
        this.undoStack = new Stack<>();
        this.priorityTree = new BinarySearchTree<>();
    }

    public void setOnDataChange(Runnable onDataChange) {
        this.onDataChange = onDataChange;
    }

    public void addTask(String title, Priority priority) {
        if (title == null || title.trim().isEmpty()) {
            return;
        }
        Task task = new Task(title.trim(), priority);
        repository.add(task);

        String priorityKey = buildPriorityKey(task);
        priorityTree.insert(priorityKey);

        notifyChange();
    }

    public void deleteTask(String id) {
        Task removed = repository.remove(id);
        if (removed != null) {
            undoStack.push(removed);

            String priorityKey = buildPriorityKey(removed);
            priorityTree.remove(priorityKey);

            notifyChange();
        }
    }

    public void toggleComplete(String id) {
        Task task = repository.findById(id);
        if (task != null) {
            task.toggleCompleted();
            notifyChange();
        }
    }

    public void undoDelete() {
        if (undoStack.isEmpty()) {
            return;
        }
        Task restored = undoStack.pop();
        repository.add(restored);

        String priorityKey = buildPriorityKey(restored);
        priorityTree.insert(priorityKey);

        notifyChange();
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public List<Task> getFilteredTasks(FilterType filter) {
        return switch (filter) {
            case ACTIVE -> repository.findByStatus(false);
            case COMPLETED -> repository.findByStatus(true);
            default -> repository.findAll();
        };
    }

    public List<Task> getTasksSortedByPriority() {
        List<String> keys = priorityTree.inOrder();
        List<Task> sorted = new ArrayList<>();
        for (int i = keys.size() - 1; i >= 0; i--) {
            String key = keys.get(i);
            String id = key.substring(key.indexOf('_') + 1);
            Task task = repository.findById(id);
            if (task != null) {
                sorted.add(task);
            }
        }
        return sorted;
    }

    public List<Task> searchTasks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return repository.findAll();
        }
        String lowerQuery = query.toLowerCase().trim();
        List<Task> all = repository.findAll();
        List<Task> results = new ArrayList<>();
        for (Task task : all) {
            if (task.getTitle().toLowerCase().contains(lowerQuery)) {
                results.add(task);
            }
        }
        return results;
    }

    public List<String> searchTaskTitles(String prefix) {
        return repository.searchTitles(prefix);
    }

    public Map<LocalDate, List<Task>> getTasksByDate() {
        return repository.getTasksByDate();
    }

    public int getTotalCount() {
        return repository.findAll().size();
    }

    public int getActiveCount() {
        return repository.findByStatus(false).size();
    }

    public int getCompletedCount() {
        return repository.findByStatus(true).size();
    }

    public int getUndoStackSize() {
        return undoStack.size();
    }

    public void clearCompleted() {
        List<Task> completed = repository.findByStatus(true);
        for (Task task : completed) {
            priorityTree.remove(buildPriorityKey(task));
            repository.remove(task.getId());
        }
        if (!completed.isEmpty()) {
            notifyChange();
        }
    }

    public void sortByPriority(List<Task> tasks) {
        tasks.sort((a, b) -> b.getPriority().compareTo(a.getPriority()));
    }

    private String buildPriorityKey(Task task) {
        int priorityValue = switch (task.getPriority()) {
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
        return String.format("%d_%s", priorityValue, task.getId());
    }

    private void notifyChange() {
        if (onDataChange != null) {
            onDataChange.run();
        }
    }
}
