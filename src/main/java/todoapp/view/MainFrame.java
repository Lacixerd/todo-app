package todoapp.view;

import todoapp.controller.TaskController;
import todoapp.controller.TaskController.FilterType;
import todoapp.model.Task;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame {
    private final TaskController controller;
    private final TaskFormPanel formPanel;
    private final TaskListPanel listPanel;
    private FilterType currentFilter = FilterType.ALL;
    private JButton undoButton;
    private JPanel filterPanel;
    private JTextField searchField;
    private JLabel statsLabel;
    private JLabel dsLabel;
    private boolean sortByPriority = false;
    private boolean groupByDate = false;
    private String searchQuery = "";
    private JButton sortBtn;
    private JButton dateBtn;

    private static final Color BG_COLOR = new Color(25, 25, 30);
    private static final Color HEADER_START = new Color(40, 40, 48);
    private static final Color HEADER_END = new Color(30, 30, 36);
    private static final Color TOOLBAR_BG = new Color(35, 35, 40);
    private static final Color ACTIVE_FILTER_COLOR = new Color(100, 149, 237);
    private static final Color INACTIVE_FILTER_COLOR = new Color(55, 55, 62);
    private static final Color SEARCH_BG = new Color(44, 44, 50);
    private static final Color SEARCH_BORDER = new Color(60, 60, 68);
    private static final Color ACCENT = new Color(100, 149, 237);
    private static final Color STATS_BG = new Color(32, 32, 38);

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
        setSize(580, 720);
        setMinimumSize(new Dimension(480, 500));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gradient = new GradientPaint(0, 0, HEADER_START, getWidth(), getHeight(), HEADER_END);
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel titleLabel = new JLabel("✓  To-Do List");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Organize your tasks efficiently");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(140, 140, 150));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(subtitleLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        JPanel searchPanel = createSearchBar();
        headerPanel.add(searchPanel, BorderLayout.EAST);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.add(headerPanel);
        topPanel.add(formPanel);
        topPanel.add(createFeatureBar());

        add(topPanel, BorderLayout.NORTH);
        add(listPanel, BorderLayout.CENTER);
    }

    private JPanel createSearchBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        panel.setOpaque(false);

        searchField = new JTextField(14) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SEARCH_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        searchField.setBackground(SEARCH_BG);
        searchField.setForeground(Color.WHITE);
        searchField.setCaretColor(Color.WHITE);
        searchField.setOpaque(false);
        searchField.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        searchField.setPreferredSize(new Dimension(200, 36));

        JLabel searchPlaceholder = new JLabel("  🔍  Search (AVL Tree)...");
        searchPlaceholder.setFont(new Font("SansSerif", Font.PLAIN, 12));
        searchPlaceholder.setForeground(new Color(100, 100, 110));
        searchField.setLayout(new BorderLayout());
        searchField.add(searchPlaceholder);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                searchPlaceholder.setVisible(searchField.getText().isEmpty());
                searchQuery = searchField.getText();
                refreshView();
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        panel.add(searchField);
        return panel;
    }

    private JPanel createFeatureBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        bar.setBackground(new Color(35, 35, 40));
        bar.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));

        sortBtn = createFeatureButton("⇅ Sort by Priority", "BST");
        sortBtn.addActionListener(e -> {
            sortByPriority = !sortByPriority;
            if (sortByPriority) groupByDate = false;
            updateFeatureButtons();
            refreshView();
        });

        dateBtn = createFeatureButton("📅 Group by Date", "TreeMap");
        dateBtn.addActionListener(e -> {
            groupByDate = !groupByDate;
            if (groupByDate) sortByPriority = false;
            updateFeatureButtons();
            refreshView();
        });

        bar.add(sortBtn);
        bar.add(dateBtn);

        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        spacer.setPreferredSize(new Dimension(40, 1));
        bar.add(spacer);

        dsLabel = new JLabel();
        dsLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        dsLabel.setForeground(new Color(120, 120, 130));
        bar.add(dsLabel);

        return bar;
    }

    private JButton createFeatureButton(String text, String dsTag) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setForeground(new Color(170, 170, 180));
        btn.setBackground(new Color(48, 48, 54));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Data Structure: " + dsTag);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!isFeatureActive(btn)) {
                    btn.setBackground(new Color(58, 58, 66));
                    btn.repaint();
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!isFeatureActive(btn)) {
                    btn.setBackground(new Color(48, 48, 54));
                    btn.repaint();
                }
            }
        });

        return btn;
    }

    private boolean isFeatureActive(JButton btn) {
        if (btn == sortBtn) return sortByPriority;
        if (btn == dateBtn) return groupByDate;
        return false;
    }

    private void updateFeatureButtons() {
        updateFeatureButtonStyle(sortBtn, sortByPriority);
        updateFeatureButtonStyle(dateBtn, groupByDate);
    }

    private void updateFeatureButtonStyle(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 40));
            btn.setForeground(ACCENT);
        } else {
            btn.setBackground(new Color(48, 48, 54));
            btn.setForeground(new Color(170, 170, 180));
        }
        btn.repaint();
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
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(50, 50, 56)),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        filterPanel.setOpaque(false);

        filterPanel.add(createFilterButton("All", FilterType.ALL));
        filterPanel.add(createFilterButton("Active", FilterType.ACTIVE));
        filterPanel.add(createFilterButton("Completed", FilterType.COMPLETED));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionPanel.setOpaque(false);

        JButton clearCompletedBtn = createToolbarButton("Clear Done", new Color(120, 120, 130));
        clearCompletedBtn.addActionListener(e -> controller.clearCompleted());

        undoButton = createToolbarButton("↩ Undo", new Color(255, 183, 77));
        undoButton.addActionListener(e -> controller.undoDelete());

        actionPanel.add(clearCompletedBtn);
        actionPanel.add(undoButton);

        topRow.add(filterPanel, BorderLayout.WEST);
        topRow.add(actionPanel, BorderLayout.EAST);

        JPanel statsPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(STATS_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
            }
        };
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        statsLabel = new JLabel();
        statsLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statsLabel.setForeground(new Color(140, 140, 150));
        statsPanel.add(statsLabel, BorderLayout.WEST);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);
        bottomPanel.add(topRow);
        bottomPanel.add(Box.createVerticalStrut(6));
        bottomPanel.add(statsPanel);

        toolbar.add(bottomPanel, BorderLayout.CENTER);
        add(toolbar, BorderLayout.SOUTH);
    }

    private JButton createFilterButton(String text, FilterType filter) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(95, 30));

        boolean isActive = (currentFilter == filter);
        btn.setBackground(isActive ? ACTIVE_FILTER_COLOR : INACTIVE_FILTER_COLOR);
        btn.setForeground(Color.WHITE);

        btn.addActionListener(e -> {
            currentFilter = filter;
            refreshView();
        });

        return btn;
    }

    private JButton createToolbarButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setForeground(color);
        btn.setBackground(new Color(48, 48, 54));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(60, 60, 68));
                btn.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(48, 48, 54));
                btn.repaint();
            }
        });

        return btn;
    }

    private void refreshView() {
        List<Task> tasks;

        if (!searchQuery.isEmpty()) {
            tasks = controller.searchTasks(searchQuery);
            listPanel.refresh(tasks);
        } else if (groupByDate) {
            Map<LocalDate, List<Task>> dateGroups = controller.getTasksByDate();
            listPanel.refreshGroupedByDate(dateGroups);
        } else if (sortByPriority) {
            tasks = controller.getTasksSortedByPriority();
            listPanel.refresh(tasks);
        } else {
            tasks = controller.getFilteredTasks(currentFilter);
            listPanel.refresh(tasks);
        }

        if (undoButton != null) {
            undoButton.setEnabled(controller.canUndo());
        }

        updateStats();
        updateFilterButtons();
        updateDsLabel();
    }

    private void updateStats() {
        if (statsLabel != null) {
            int total = controller.getTotalCount();
            int active = controller.getActiveCount();
            int completed = controller.getCompletedCount();
            int undoSize = controller.getUndoStackSize();

            statsLabel.setText(String.format(
                    "📊  Total: %d  ·  Active: %d  ·  Done: %d  ·  Undo Stack: %d",
                    total, active, completed, undoSize
            ));
        }
    }

    private void updateDsLabel() {
        if (dsLabel != null) {
            String activeDs;
            if (!searchQuery.isEmpty()) {
                activeDs = "🔍 AVL Tree prefix search active";
            } else if (groupByDate) {
                activeDs = "🌳 TreeMap date grouping active";
            } else if (sortByPriority) {
                activeDs = "🌲 BST priority sorting active";
            } else {
                activeDs = "📋 LinkedList + HashMap view";
            }
            dsLabel.setText(activeDs);
        }
    }

    private void updateFilterButtons() {
        if (filterPanel == null) return;
        for (Component comp : filterPanel.getComponents()) {
            if (comp instanceof JButton btn) {
                FilterType filter = switch (btn.getText()) {
                    case "Active" -> FilterType.ACTIVE;
                    case "Completed" -> FilterType.COMPLETED;
                    default -> FilterType.ALL;
                };
                btn.setBackground(currentFilter == filter ? ACTIVE_FILTER_COLOR : INACTIVE_FILTER_COLOR);
                btn.repaint();
            }
        }
    }
}
