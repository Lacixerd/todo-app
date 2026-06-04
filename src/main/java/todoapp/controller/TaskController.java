package todoapp.controller;

import todoapp.model.Priority;
import todoapp.model.Task;
import todoapp.model.TaskRepository;

import java.util.List;
import java.util.Stack;

public class TaskController {

    public enum FilterType {
        ALL, ACTIVE, COMPLETED
    }

    private final TaskRepository repository;
    private final Stack<Task> undoStack;
    private Runnable onDataChange;

    public TaskController(TaskRepository repository) {
        this.repository = repository;
        this.undoStack = new Stack<>();
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
        notifyChange();
    }

    public void deleteTask(String id) {
        Task removed = repository.remove(id);
        if (removed != null) {
            undoStack.push(removed);
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

    public void clearCompleted() {
        List<Task> completed = repository.findByStatus(true);
        for (Task task : completed) {
            repository.remove(task.getId());
        }
        if (!completed.isEmpty()) {
            notifyChange();
        }
    }

    public void sortByPriority(List<Task> tasks) {
        tasks.sort((a, b) -> b.getPriority().compareTo(a.getPriority()));
    }

    private void notifyChange() {
        if (onDataChange != null) {
            onDataChange.run();
        }
    }
}
