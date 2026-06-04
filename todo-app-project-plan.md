# To-Do List Application — Project Plan

**Course:** Data Structures & Algorithms  
**Language:** Java (Swing GUI)  
**Team:** X, Y, Z  
**Estimated Time:** 2–3 hours total  

---

## 1. Project Overview

A desktop To-Do List application built with Java Swing. The application lets users create, complete, delete, and filter tasks. Internally, it demonstrates core data structure usage (LinkedList, Stack) relevant to the course.

**Key Goals:**
- Clean OOP design with separation of concerns
- MVC (Model-View-Controller) architecture
- Demonstrable data structure usage for academic context
- Simple, functional Swing UI

---

## 2. Architecture: MVC Pattern

```
src/
└── todoapp/
    ├── model/
    │   ├── Task.java
    │   ├── Priority.java           (enum)
    │   └── TaskRepository.java
    ├── controller/
    │   └── TaskController.java
    ├── view/
    │   ├── MainFrame.java
    │   ├── TaskListPanel.java
    │   └── TaskFormPanel.java
    └── Main.java
```

**Layer responsibilities:**

| Layer | Responsibility |
|---|---|
| `model` | Data classes, business logic, in-memory storage |
| `controller` | Mediates between view and model; handles user actions |
| `view` | Swing components only; zero business logic |
| `Main` | Entry point; wires everything together |

---

## 3. Data Structures Used

| Structure | Where | Why |
|---|---|---|
| `LinkedList<Task>` | `TaskRepository` | O(1) add/remove from head; natural for task queues |
| `Stack<Task>` | `TaskController` | Undo last-delete operation |
| `List<Task>` (filtered view) | `TaskController` | Filtered results without mutating the source list |

These are not incidental — they are architectural decisions tied to the feature set.

---

## 4. Feature Set

### Core Features (must-have)
- Add a task with title and priority (LOW / MEDIUM / HIGH)
- Mark task as completed (checkbox)
- Delete a task
- Undo last delete (Stack-based)
- Filter view: All / Active / Completed

### Stretch Features (if time permits)
- Sort by priority
- Clear all completed tasks

---

## 5. Class Specifications

### 5.1 `Priority.java` — Enum
```
Enum constants: LOW, MEDIUM, HIGH
```

### 5.2 `Task.java` — Model
```
Fields:
  - String id           (UUID)
  - String title
  - Priority priority
  - boolean completed
  - LocalDate createdAt

Methods:
  - getters / setters
  - toggleCompleted()
  - toString()          (for debugging)
```

### 5.3 `TaskRepository.java` — In-memory store
```
Fields:
  - LinkedList<Task> tasks

Methods:
  - add(Task task)
  - remove(String id) → Task
  - findAll() → List<Task>
  - findByStatus(boolean completed) → List<Task>
```

### 5.4 `TaskController.java` — Application logic
```
Fields:
  - TaskRepository repository
  - Stack<Task> undoStack
  - Runnable onDataChange       (callback to refresh view)

Methods:
  - addTask(String title, Priority priority)
  - deleteTask(String id)
  - toggleComplete(String id)
  - undoDelete()
  - getFilteredTasks(FilterType filter) → List<Task>
```

### 5.5 `MainFrame.java` — Root Swing frame
```
Extends: JFrame
Contains: TaskFormPanel (top), TaskListPanel (center), filter buttons (bottom toolbar)
Wires: TaskController callbacks
```

### 5.6 `TaskFormPanel.java` — Input area
```
Extends: JPanel
Components: JTextField (title), JComboBox<Priority>, JButton (Add)
Exposes: getTitle(), getPriority(), clearFields()
```

### 5.7 `TaskListPanel.java` — Task display
```
Extends: JPanel
Contains: JScrollPane wrapping a JList or custom cell renderer
Methods:
  - refresh(List<Task> tasks)
```

---

## 6. Team Task Distribution

### X — Model Layer + Data Structures

**Files:** `Priority.java`, `Task.java`, `TaskRepository.java`

**Responsibilities:**
- Define the `Priority` enum with display labels
- Implement `Task` with UUID generation, field validation, and `toggleCompleted()`
- Implement `TaskRepository` using `LinkedList<Task>` as the backing store
- Write `findByStatus()` filter using stream API
- Ensure `remove(String id)` returns the removed task (required by controller's undo stack)

**What to explain to the instructor:**
> "I implemented the model layer. `TaskRepository` uses a `LinkedList` because we benefit from O(1) insertion at the head and efficient removal by reference when the id is located. The `Task` class uses UUID for stable identity across operations."

---

### Y — Controller Layer + Undo Logic

**Files:** `TaskController.java`

**Responsibilities:**
- Implement all application logic methods
- Implement undo-delete using `Stack<Task>` — push on delete, pop on undo, re-add to repository
- Implement `FilterType` enum (ALL, ACTIVE, COMPLETED) and `getFilteredTasks()`
- Accept a `Runnable onDataChange` callback from the view to trigger UI refresh (Observer-lite pattern)
- Handle edge cases: undo on empty stack, toggle on non-existent id

**What to explain to the instructor:**
> "I implemented the controller and the undo feature. When a task is deleted, it's pushed onto a `Stack<Task>`. Undo pops the top and re-inserts it into the repository. This is a classic use of stack semantics — LIFO matches the expected undo behavior. The controller also accepts a `Runnable` callback so the view re-renders whenever data changes, keeping the controller decoupled from Swing."

---

### Z — View Layer (Swing UI)

**Files:** `MainFrame.java`, `TaskFormPanel.java`, `TaskListPanel.java`, `Main.java`

**Responsibilities:**
- Build `MainFrame` as the root container using `BorderLayout`
- Build `TaskFormPanel` with input field, priority dropdown, and Add button
- Build `TaskListPanel` with a `DefaultListModel<Task>` and custom `ListCellRenderer` for colored priority badges and strikethrough on completed tasks
- Wire filter buttons (All / Active / Completed) to controller
- Wire Undo button to `controller.undoDelete()`
- Implement `Main.java` entry point with `SwingUtilities.invokeLater`

**What to explain to the instructor:**
> "I implemented the entire view layer using Java Swing. `TaskListPanel` uses a `DefaultListModel` which acts as an observable list — when we call `refresh()`, it clears and repopulates the model, which notifies the `JList` to re-render. I implemented a custom `ListCellRenderer` to show priority colors and visually cross out completed tasks."

---

## 7. Implementation Order

Work in this sequence to avoid blocking each other (all done by same person in practice):

```
Step 1  →  Priority.java, Task.java
Step 2  →  TaskRepository.java
Step 3  →  TaskController.java  (stub view callback as no-op for now)
Step 4  →  TaskFormPanel.java, TaskListPanel.java
Step 5  →  MainFrame.java  (wire everything)
Step 6  →  Main.java
Step 7  →  Test all features manually
Step 8  →  (optional) add sort by priority, clear completed
```

---

## 8. UI Layout Sketch

```
┌─────────────────────────────────────────────────┐
│  [Task title input field       ] [Priority ▾] [Add] │  ← TaskFormPanel
├─────────────────────────────────────────────────┤
│  ☐  Buy groceries          [HIGH]          [🗑]  │
│  ☑  Read chapter 3         [LOW]           [🗑]  │  ← TaskListPanel
│  ☐  Finish assignment      [MEDIUM]        [🗑]  │
│  ...                                            │
├─────────────────────────────────────────────────┤
│  [All]  [Active]  [Completed]        [Undo ↩]   │  ← Toolbar
└─────────────────────────────────────────────────┘
```

---

## 9. Key Design Decisions

**Why MVC?**  
Decouples UI from logic. The controller can be tested without touching Swing. The view can be redesigned without touching business logic.

**Why `LinkedList` not `ArrayList`?**  
Frequent insertion at head and removal by traversal favor `LinkedList`. For this scale it's academically the correct choice to demonstrate.

**Why `Stack` for undo?**  
Stack's LIFO semantics map directly to undo: the last deleted item is the first to be restored. Using Java's `java.util.Stack` makes the intent explicit.

**Why UUID for task IDs?**  
Avoids index-based identity which breaks on deletion. Stable identity simplifies controller logic.

**Why `Runnable` callback not direct view reference?**  
Keeps the controller unaware of Swing. If the view layer were replaced (e.g., with a CLI), the controller requires zero changes.

---

## 10. Deliverable Checklist

- [ ] All `.java` files compile without warnings
- [ ] Add task works with all three priorities
- [ ] Checkbox toggles completion and updates visual state
- [ ] Delete removes task from list
- [ ] Undo restores last deleted task
- [ ] Filter buttons show correct subsets
- [ ] Empty state is handled gracefully (no crash on undo with empty stack)
- [ ] Code has no inline comments — self-documenting through naming
