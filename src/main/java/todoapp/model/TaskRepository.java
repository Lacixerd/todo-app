package todoapp.model;

import todoapp.datastructure.AVLTree;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class TaskRepository {
    private final LinkedList<Task> tasks;
    private final HashMap<String, Task> taskIndex;
    private final TreeMap<LocalDate, List<Task>> dateIndex;
    private final AVLTree<String> titleIndex;

    public TaskRepository() {
        this.tasks = new LinkedList<>();
        this.taskIndex = new HashMap<>();
        this.dateIndex = new TreeMap<>();
        this.titleIndex = new AVLTree<>();
    }

    public void add(Task task) {
        tasks.addFirst(task);
        taskIndex.put(task.getId(), task);

        dateIndex.computeIfAbsent(task.getCreatedAt(), k -> new ArrayList<>()).add(task);

        titleIndex.insert(task.getTitle().toLowerCase());
    }

    public Task remove(String id) {
        Task task = taskIndex.remove(id);
        if (task != null) {
            tasks.remove(task);

            List<Task> dateTasks = dateIndex.get(task.getCreatedAt());
            if (dateTasks != null) {
                dateTasks.remove(task);
                if (dateTasks.isEmpty()) {
                    dateIndex.remove(task.getCreatedAt());
                }
            }

            titleIndex.remove(task.getTitle().toLowerCase());
        }
        return task;
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
        return taskIndex.get(id);
    }

    public Map<LocalDate, List<Task>> getTasksByDate() {
        return Collections.unmodifiableMap(dateIndex);
    }

    public List<Task> findByDate(LocalDate date) {
        return dateIndex.getOrDefault(date, Collections.emptyList());
    }

    public List<String> searchTitles(String prefix) {
        return titleIndex.prefixSearch(prefix.toLowerCase(),
                (title, p) -> title.startsWith(p));
    }

    public List<String> getAllTitlesSorted() {
        return titleIndex.inOrder();
    }
}
