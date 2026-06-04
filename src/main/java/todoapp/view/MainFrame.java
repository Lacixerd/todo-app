package todoapp.view;

import todoapp.controller.TaskController;
import todoapp.controller.TaskController.FilterType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MainFrame extends JFrame {
    private final TaskController controller;
    private final TaskFormPanel formPanel;
    private final TaskListPanel listPanel;
    private FilterType currentFilter = FilterType.ALL;
    private JButton undoButton;

    private static final Color BG_COLOR = new Color(37, 37, 40);
    private static final Color TOOLBAR_BG = new Color(45, 45, 48);
    private static final Color ACTIVE_FILTER_COLOR = new Color(66, 133, 244);
    private static final Color INACTIVE_FILTER_COLOR = new Color(70, 70, 75);

    public MainFrame(TaskController controller) {
        this.controller = controller;
        this.formPanel = new TaskFormPanel();
        this.listPanel = new TaskListPanel();

        setupFrame();
        setupFormActions();
        setupListActions();
        setupToolbar();

        controller.setOnDataChange(this::refreshView);
        refreshView();
    }

    private void setupFrame() {
        setTitle("To-Do List");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 600);
        setMinimumSize(new Dimension(420, 400));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout());

        JLabel headerLabel = new JLabel("  ✓  To-Do List");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setOpaque(true);
        headerLabel.setBackground(new Color(30, 30, 33));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(14, 12, 14, 12));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(headerLabel, BorderLayout.NORTH);
        topPanel.add(formPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(listPanel, BorderLayout.CENTER);
    }

    private void setupFormActions() {
        formPanel.getAddButton().addActionListener(e -> addTask());

        formPanel.getTitleField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    addTask();
                }
            }
        });
    }

    private void addTask() {
        String title = formPanel.getTitle();
        if (!title.trim().isEmpty()) {
            controller.addTask(title, formPanel.getPriority());
            formPanel.clearFields();
        }
    }

    private void setupListActions() {
        listPanel.setOnDelete(id -> controller.deleteTask(id));
        listPanel.setOnToggle(id -> controller.toggleComplete(id));
    }

    private void setupToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(TOOLBAR_BG);
        toolbar.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        filterPanel.setOpaque(false);

        JButton allBtn = createFilterButton("All", FilterType.ALL);
        JButton activeBtn = createFilterButton("Active", FilterType.ACTIVE);
        JButton completedBtn = createFilterButton("Completed", FilterType.COMPLETED);

        filterPanel.add(allBtn);
        filterPanel.add(activeBtn);
        filterPanel.add(completedBtn);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actionPanel.setOpaque(false);

        JButton clearCompletedBtn = createToolbarButton("Clear Done", new Color(120, 120, 125));
        clearCompletedBtn.addActionListener(e -> controller.clearCompleted());

        undoButton = createToolbarButton("Undo ↩", new Color(255, 167, 38));
        undoButton.addActionListener(e -> controller.undoDelete());

        actionPanel.add(clearCompletedBtn);
        actionPanel.add(undoButton);

        toolbar.add(filterPanel, BorderLayout.WEST);
        toolbar.add(actionPanel, BorderLayout.EAST);

        add(toolbar, BorderLayout.SOUTH);
    }

    private JButton createFilterButton(String text, FilterType filter) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(90, 30));

        boolean isActive = (currentFilter == filter);
        btn.setBackground(isActive ? ACTIVE_FILTER_COLOR : INACTIVE_FILTER_COLOR);
        btn.setForeground(Color.WHITE);

        btn.addActionListener(e -> {
            currentFilter = filter;
            refreshView();
            refreshFilterButtons();
        });

        return btn;
    }

    private JButton createToolbarButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setForeground(color);
        btn.setBackground(new Color(55, 55, 60));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(70, 70, 75));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(55, 55, 60));
            }
        });

        return btn;
    }

    private void refreshView() {
        listPanel.refresh(controller.getFilteredTasks(currentFilter));
        if (undoButton != null) {
            undoButton.setEnabled(controller.canUndo());
        }
        refreshFilterButtons();
    }

    private void refreshFilterButtons() {
        Component toolbar = getContentPane().getComponent(2);
        if (toolbar instanceof JPanel toolbarPanel) {
            Component west = ((BorderLayout) toolbarPanel.getLayout()).getLayoutComponent(BorderLayout.WEST);
            if (west instanceof JPanel filterPanel) {
                for (Component comp : filterPanel.getComponents()) {
                    if (comp instanceof JButton btn) {
                        FilterType filter = switch (btn.getText()) {
                            case "Active" -> FilterType.ACTIVE;
                            case "Completed" -> FilterType.COMPLETED;
                            default -> FilterType.ALL;
                        };
                        btn.setBackground(currentFilter == filter ? ACTIVE_FILTER_COLOR : INACTIVE_FILTER_COLOR);
                    }
                }
            }
        }
    }
}
