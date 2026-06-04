package todoapp.model;

import java.time.LocalDate;
import java.util.UUID;

public class Task {
    private final String id;
    private String title;
    private Priority priority;
    private boolean completed;
    private final LocalDate createdAt;

    public Task(String title, Priority priority) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.priority = priority;
        this.completed = false;
        this.createdAt = LocalDate.now();
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void toggleCompleted() {
        this.completed = !this.completed;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Task{id='" + id + "', title='" + title + "', priority=" + priority +
               ", completed=" + completed + ", createdAt=" + createdAt + "}";
    }
}
