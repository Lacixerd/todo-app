
# To-Do List — Data Structures & Algorithms Project

Java Swing desktop application demonstrating MVC architecture with `LinkedList` and `Stack` data structures.

## Requirements

- **Java 21 or higher** — [Download](https://www.oracle.com/java/technologies/downloads/)
- No Maven, no extra libraries needed.

## How to Run

### 1. Clone the repository

```bash
git clone https://github.com/Lacixerd/todo-app.git
cd todo-app
```

### 2. Compile

```bash
javac -d target/classes $(find src/main/java -name "*.java")
```

> On **Windows** (Command Prompt):
> ```cmd
> mkdir target\classes
> for /r src\main\java %f in (*.java) do javac -d target\classes "%f"
> ```

### 3. Run

```bash
java -cp target/classes todoapp.Main
```

> On **Windows**:
> ```cmd
> java -cp target\classes todoapp.Main
> ```

---

## Project Structure

```
src/
└── main/java/todoapp/
    ├── model/
    │   ├── Priority.java        ← enum: LOW, MEDIUM, HIGH
    │   ├── Task.java            ← task model (UUID, title, priority, status)
    │   └── TaskRepository.java  ← LinkedList<Task> storage
    ├── controller/
    │   └── TaskController.java  ← Stack<Task> undo, filter logic
    ├── view/
    │   ├── MainFrame.java       ← root window
    │   ├── TaskFormPanel.java   ← input area
    │   └── TaskListPanel.java   ← task list with custom rendering
    └── Main.java                ← entry point
```
 
## Features

- Add task with title and priority (LOW / MEDIUM / HIGH)
- Mark task as completed
- Delete task
- **Undo** last delete (Stack-based)
- Filter: All / Active / Completed
- Clear all completed tasks
