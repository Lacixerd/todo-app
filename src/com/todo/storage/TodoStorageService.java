package com.todo.storage;

import com.google.gson.*;
import com.todo.model.TodoItem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TodoStorageService — görevleri (ve alt görevlerini) JSON'a kaydeder/yükler.
 *
 * Dosya: ~/.dsa-todo/tasks.json
 *
 * JSON formatı:
 * {
 *   "version": 2,
 *   "tasks": [
 *     {
 *       "id": 1, "title": "Proje", "priority": "HIGH", "completed": false,
 *       "children": [
 *         { "id": 3, "title": "Araştırma", "priority": "MEDIUM", "completed": true, "children": [] }
 *       ]
 *     }
 *   ]
 * }
 */
public class TodoStorageService {

    private static final int FORMAT_VERSION = 2;
    private static final String DIR_NAME = ".dsa-todo";
    private static final String FILE_NAME = "tasks.json";

    private final Path saveFile;
    private final Gson gson;

    public TodoStorageService() {
        Path home = Paths.get(System.getProperty("user.home"));
        this.saveFile = home.resolve(DIR_NAME).resolve(FILE_NAME);
        this.gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    }

    // ── KAYDET ────────────────────────────────────────────────────────────

    public void save(List<TodoItem> tasks) {
        try {
            Files.createDirectories(saveFile.getParent());

            List<TaskDto> dtoList = new ArrayList<>();
            for (TodoItem item : tasks) dtoList.add(toDto(item));

            String json = gson.toJson(new SaveData(FORMAT_VERSION, dtoList));

            Path tmp = saveFile.resolveSibling(FILE_NAME + ".tmp");
            Files.writeString(tmp, json, StandardCharsets.UTF_8);
            Files.move(tmp, saveFile,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);

            System.out.println("[Storage] Kaydedildi → " + saveFile);
        } catch (IOException e) {
            System.err.println("[Storage] KAYIT HATASI: " + e.getMessage());
        }
    }

    // ── YÜKLE ─────────────────────────────────────────────────────────────

    public List<TodoItem> load() {
        List<TodoItem> result = new ArrayList<>();
        if (!Files.exists(saveFile)) {
            System.out.println("[Storage] Kayıt yok, temiz başlanıyor.");
            return result;
        }
        try {
            String json   = Files.readString(saveFile, StandardCharsets.UTF_8);
            SaveData data = gson.fromJson(json, SaveData.class);
            if (data == null || data.tasks == null) return result;

            TodoItem.resetIdCounter();
            for (TaskDto dto : data.tasks) result.add(fromDto(dto));

            System.out.println("[Storage] " + result.size() + " görev yüklendi ← " + saveFile);
        } catch (IOException | JsonParseException e) {
            System.err.println("[Storage] YÜKLEME HATASI: " + e.getMessage());
        }
        return result;
    }

    public String getSaveFilePath() { return saveFile.toString(); }

    // ── DTO DÖNÜŞÜM (recursive) ───────────────────────────────────────────

    private TaskDto toDto(TodoItem item) {
        List<TaskDto> childDtos = new ArrayList<>();
        for (TodoItem child : item.getChildren()) childDtos.add(toDto(child));
        return new TaskDto(item.getId(), item.getTitle(),
                item.getPriority().name(), item.isCompleted(), childDtos);
    }

    private TodoItem fromDto(TaskDto dto) {
        TodoItem.Priority priority;
        try { priority = TodoItem.Priority.valueOf(dto.priority); }
        catch (Exception e) { priority = TodoItem.Priority.MEDIUM; }

        TodoItem item = new TodoItem(dto.id, dto.title, priority, dto.completed);

        if (dto.children != null) {
            for (TaskDto childDto : dto.children) {
                item.addChild(fromDto(childDto)); // recursive
            }
        }
        return item;
    }

    // ── DTO SINIFLAR ──────────────────────────────────────────────────────

    private static class SaveData {
        int version; List<TaskDto> tasks;
        SaveData(int v, List<TaskDto> t) { version = v; tasks = t; }
    }

    private static class TaskDto {
        int id; String title; String priority; boolean completed;
        List<TaskDto> children;
        TaskDto(int id, String title, String priority, boolean completed, List<TaskDto> children) {
            this.id = id; this.title = title; this.priority = priority;
            this.completed = completed; this.children = children;
        }
    }
}
