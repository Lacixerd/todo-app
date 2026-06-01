package com.todo.storage;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.todo.model.TodoItem;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TodoStorageService — görevlerin diske JSON olarak kaydedilmesi ve yüklenmesi.
 *
 * Dosya konumu: kullanıcının home dizini altında  ~/.dsa-todo/tasks.json
 * Örnek:  /home/ahmet/.dsa-todo/tasks.json   (Linux/Mac)
 *         C:\Users\Ahmet\.dsa-todo\tasks.json (Windows)
 *
 * JSON formatı:
 * {
 *   "version": 1,
 *   "tasks": [
 *     { "id": 1, "title": "Matematik ödevi", "priority": "HIGH", "completed": false },
 *     { "id": 2, "title": "Market alışverişi", "priority": "LOW",  "completed": true  }
 *   ]
 * }
 */
public class TodoStorageService {

    private static final int    FORMAT_VERSION = 1;
    private static final String DIR_NAME       = ".dsa-todo";
    private static final String FILE_NAME      = "tasks.json";

    private final Path saveFile;
    private final Gson gson;

    public TodoStorageService() {
        // ~/.dsa-todo/tasks.json
        Path home = Paths.get(System.getProperty("user.home"));
        Path dir  = home.resolve(DIR_NAME);
        this.saveFile = dir.resolve(FILE_NAME);

        this.gson = new GsonBuilder()
                .setPrettyPrinting()           // okunabilir girinti
                .serializeNulls()
                .create();
    }

    // ── KAYDET ────────────────────────────────────────────────────────────

    /**
     * Görev listesini diske yazar.
     * Dizin yoksa oluşturulur. Hata olursa fırlatmadan loglanır.
     */
    public void save(List<TodoItem> tasks) {
        try {
            // Dizin yoksa oluştur
            Files.createDirectories(saveFile.getParent());

            // DTO listesi oluştur
            List<TaskDto> dtoList = new ArrayList<>();
            for (TodoItem item : tasks) {
                dtoList.add(new TaskDto(
                        item.getId(),
                        item.getTitle(),
                        item.getPriority().name(),
                        item.isCompleted()
                ));
            }

            // Kök nesne
            SaveData data = new SaveData(FORMAT_VERSION, dtoList);
            String json   = gson.toJson(data);

            // Önce geçici dosyaya yaz, sonra atomik rename (veri bozulmasını önler)
            Path tmp = saveFile.resolveSibling(FILE_NAME + ".tmp");
            Files.writeString(tmp, json, StandardCharsets.UTF_8);
            Files.move(tmp, saveFile,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);

            System.out.println("[Storage] " + tasks.size() + " görev kaydedildi → " + saveFile);

        } catch (IOException e) {
            System.err.println("[Storage] KAYIT HATASI: " + e.getMessage());
        }
    }

    // ── YÜKLE ─────────────────────────────────────────────────────────────

    /**
     * Diskten görevleri yükler.
     * Dosya yoksa veya bozuksa boş liste döner.
     */
    public List<TodoItem> load() {
        List<TodoItem> result = new ArrayList<>();

        if (!Files.exists(saveFile)) {
            System.out.println("[Storage] Kayıt dosyası bulunamadı, temiz başlanıyor.");
            return result;
        }

        try {
            String json = Files.readString(saveFile, StandardCharsets.UTF_8);
            SaveData data = gson.fromJson(json, SaveData.class);

            if (data == null || data.tasks == null) {
                System.out.println("[Storage] Dosya boş veya geçersiz, temiz başlanıyor.");
                return result;
            }

            // idCounter'ı sıfırla; constructor içinde max id izlenir
            TodoItem.resetIdCounter();

            for (TaskDto dto : data.tasks) {
                TodoItem.Priority priority;
                try {
                    priority = TodoItem.Priority.valueOf(dto.priority);
                } catch (Exception ex) {
                    priority = TodoItem.Priority.MEDIUM; // bozuk değer için varsayılan
                }
                result.add(new TodoItem(dto.id, dto.title, priority, dto.completed));
            }

            System.out.println("[Storage] " + result.size() + " görev yüklendi ← " + saveFile);

        } catch (IOException | JsonParseException e) {
            System.err.println("[Storage] YÜKLEME HATASI: " + e.getMessage());
        }

        return result;
    }

    /** Kayıt dosyasının tam yolunu döndür (UI'da göstermek için) */
    public String getSaveFilePath() {
        return saveFile.toString();
    }

    // ── DTO SINIFLAR ──────────────────────────────────────────────────────
    // Gson'un serialize/deserialize ettiği basit veri taşıyıcılar.
    // TodoItem'dan bağımsız tutuldu — model ile depolama katmanı ayrışık kalır.

    private static class SaveData {
        int           version;
        List<TaskDto> tasks;

        SaveData(int version, List<TaskDto> tasks) {
            this.version = version;
            this.tasks   = tasks;
        }
    }

    private static class TaskDto {
        int     id;
        String  title;
        String  priority;
        boolean completed;

        TaskDto(int id, String title, String priority, boolean completed) {
            this.id        = id;
            this.title     = title;
            this.priority  = priority;
            this.completed = completed;
        }
    }
}
