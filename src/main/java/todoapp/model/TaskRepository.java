package todoapp.model;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class TaskRepository {
    private final LinkedList<Task> tasks;

    public TaskRepository() {
        this.tasks = new LinkedList<>();
    }

    public void add(Task task) {
        tasks.addFirst(task);
    }

    public Task remove(String id) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(id)) {
                return tasks.remove(i);
            }
        }
        return null;
    }

    public List<Task> findAll() {
        return new LinkedList<>(tasks);
    }

    public List<Task> findByStatus(boolean completed) {
        return tasks.stream()
                .filter(task -> task.isCompleted() == completed)
                .collect(Collectors.toList());
    }

    public Task findById(String id) {
        return tasks.stream()
                .filter(task -> task.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
