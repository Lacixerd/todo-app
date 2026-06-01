# DSA Todo List — JavaFX

Basit bir JavaFX Todo List uygulaması. Gerçek DSA yapıları kullanılarak yazılmıştır.

---

## 📁 Proje Yapısı

```
src/main/java/com/todo/
├── app/
│   └── TodoApp.java           ← JavaFX ana uygulama (UI)
├── controller/
│   └── TodoService.java       ← İş mantığı katmanı
├── dsa/
│   ├── TodoLinkedList.java    ← Custom generic LinkedList
│   ├── UndoStack.java         ← Custom generic Stack
│   └── TaskQueue.java         ← Custom generic Queue
└── model/
    ├── TodoItem.java          ← Görev modeli (id, title, priority, completed)
    └── Action.java            ← Undo aksiyonu modeli
```

---

## 🧱 Kullanılan DSA Yapıları

### 1. `TodoLinkedList<T>` — Görev Deposu
- Tüm görevler singly linked list'te tutulur
- `addLast()`, `addFirst()`, `removeById()`, `findById()` metodları
- Iterable → for-each ile dolaşılabilir
- **Neden?** Ortadan silme O(n) ama dinamik büyüme için ideal

### 2. `UndoStack<T>` — Geri Alma
- LIFO (Last In First Out)
- Her `ADD`, `DELETE`, `COMPLETE` aksiyonu push'lanır
- `↩ Geri Al` butonuna basılınca pop() ile son işlem tersine çevrilir
- **Neden?** Undo/Redo mantığı için stack klasik çözümdür

### 3. `TaskQueue<T>` — HIGH Priority Kuyruğu
- FIFO (First In First Out)
- Sadece `HIGH` priority görevler kuyruğa eklenir
- `⚡ Sonraki HIGH` butonu ile sıradaki kritik görev alınır
- **Neden?** İşlem sırası gerektiren görevler için queue idealdir

---

## 🚀 Çalıştırma

### Gereksinimler
- Java 17+
- Maven 3.8+

### Komut
```bash
cd todo-javafx
mvn javafx:run
```

---

## 🎮 UI Özellikleri

| Buton | Açıklama |
|-------|----------|
| ➕ Ekle | Yeni görev ekler → Stack'e push |
| ✔ Tamamla | Seçili görevi tamamlandı işaretler |
| 🗑 Sil | Seçili görevi siler → Stack'e push |
| ↩ Geri Al | Son aksiyonu geri alır → Stack'ten pop |
| ⚡ Sonraki HIGH | Queue'dan bir HIGH görev çeker |

### Filtreler
- **Tümü** → LinkedList'in tamamı
- **Aktif** → Tamamlanmamış görevler
- **Tamamlanan** → Biten görevler

---

## 💡 Geliştirme Fikirleri

- [ ] `DoublyLinkedList` ile çift yönlü gezinme
- [ ] `Redo Stack` ekle (undo'nun tersini yap)
- [ ] `BinarySearchTree` ile başlığa göre arama
- [ ] `HashMap` ile ID → TodoItem hızlı erişim
- [ ] Görevleri dosyaya kaydet/yükle
