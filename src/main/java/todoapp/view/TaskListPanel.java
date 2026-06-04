package todoapp.view;

import todoapp.model.Priority;
import todoapp.model.Task;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class TaskListPanel extends JPanel {
    private final JPanel taskContainer;
    private Consumer<String> onDelete;
    private Consumer<String> onToggle;

    private static final Color BG_COLOR = new Color(30, 30, 35);
    private static final Color CARD_COLOR = new Color(44, 44, 50);
    private static final Color CARD_HOVER_COLOR = new Color(54, 54, 62);
    private static final Color CARD_BORDER_COLOR = new Color(60, 60, 68);
    private static final Color TEXT_COLOR = new Color(230, 230, 235);
    private static final Color TEXT_SECONDARY_COLOR = new Color(160, 160, 170);
    private static final Color TEXT_COMPLETED_COLOR = new Color(100, 100, 110);
    private static final Color HIGH_COLOR = new Color(239, 83, 80);
    private static final Color MEDIUM_COLOR = new Color(255, 183, 77);
    private static final Color LOW_COLOR = new Color(102, 187, 106);
    private static final Color DELETE_COLOR = new Color(90, 40, 40);
    private static final Color DELETE_HOVER_COLOR = new Color(239, 83, 80);
    private static final Color DATE_HEADER_COLOR = new Color(38, 38, 44);
    private static final Color ACCENT_COLOR = new Color(100, 149, 237);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    public TaskListPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        taskContainer = new JPanel();
        taskContainer.setLayout(new BoxLayout(taskContainer, BoxLayout.Y_AXIS));
        taskContainer.setBackground(BG_COLOR);

        JScrollPane scrollPane = new JScrollPane(taskContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void setOnDelete(Consumer<String> onDelete) {
        this.onDelete = onDelete;
    }

    public void setOnToggle(Consumer<String> onToggle) {
        this.onToggle = onToggle;
    }

    public void refresh(List<Task> tasks) {
        taskContainer.removeAll();

        if (tasks.isEmpty()) {
            taskContainer.add(Box.createVerticalStrut(60));
            taskContainer.add(createEmptyState());
        } else {
            taskContainer.add(Box.createVerticalStrut(6));
            for (Task task : tasks) {
                taskContainer.add(createTaskCard(task));
                taskContainer.add(Box.createRigidArea(new Dimension(0, 6)));
            }
        }

        taskContainer.add(Box.createVerticalGlue());
        taskContainer.revalidate();
        taskContainer.repaint();
    }

    public void refreshGroupedByDate(Map<java.time.LocalDate, List<Task>> dateGroups) {
        taskContainer.removeAll();

        if (dateGroups.isEmpty()) {
            taskContainer.add(Box.createVerticalStrut(60));
            taskContainer.add(createEmptyState());
        } else {
            taskContainer.add(Box.createVerticalStrut(6));
            for (Map.Entry<LocalDate, List<Task>> entry : dateGroups.entrySet()) {
                taskContainer.add(createDateHeader(entry.getKey()));
                taskContainer.add(Box.createRigidArea(new Dimension(0, 4)));
                for (Task task : entry.getValue()) {
                    taskContainer.add(createTaskCard(task));
                    taskContainer.add(Box.createRigidArea(new Dimension(0, 6)));
                }
                taskContainer.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }

        taskContainer.add(Box.createVerticalGlue());
        taskContainer.revalidate();
        taskContainer.repaint();
    }

    private JPanel createEmptyState() {
        JPanel emptyPanel = new JPanel();
        emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
        emptyPanel.setOpaque(false);
        emptyPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel iconLabel = new JLabel("📋");
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel textLabel = new JLabel("No tasks yet");
        textLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        textLabel.setForeground(TEXT_SECONDARY_COLOR);
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("Add a task above to get started!");
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subLabel.setForeground(new Color(100, 100, 110));
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyPanel.add(iconLabel);
        emptyPanel.add(Box.createVerticalStrut(12));
        emptyPanel.add(textLabel);
        emptyPanel.add(Box.createVerticalStrut(6));
        emptyPanel.add(subLabel);

        return emptyPanel;
    }

    private JPanel createDateHeader(LocalDate date) {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(DATE_HEADER_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        header.setBorder(BorderFactory.createEmptyBorder(6, 20, 6, 20));

        String dateText = date.equals(LocalDate.now()) ? "📅  Today" : "📅  " + date.format(DATE_FORMAT);
        JLabel dateLabel = new JLabel(dateText);
        dateLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        dateLabel.setForeground(ACCENT_COLOR);
        header.add(dateLabel, BorderLayout.WEST);

        return header;
    }

    private JPanel createTaskCard(Task task) {
        JPanel card = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(CARD_BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        Color leftAccent = switch (task.getPriority()) {
            case HIGH -> HIGH_COLOR;
            case MEDIUM -> MEDIUM_COLOR;
            case LOW -> LOW_COLOR;
        };

        JPanel accentBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(leftAccent);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
            }
        };
        accentBar.setOpaque(false);
        accentBar.setPreferredSize(new Dimension(4, 0));

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(CARD_HOVER_COLOR);
                card.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(CARD_COLOR);
                card.repaint();
            }
        });

        JCheckBox checkBox = new JCheckBox();
        checkBox.setSelected(task.isCompleted());
        checkBox.setOpaque(false);
        checkBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        checkBox.addActionListener(e -> {
            if (onToggle != null) {
                onToggle.accept(task.getId());
            }
        });

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel(task.getTitle());
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        if (task.isCompleted()) {
            titleLabel.setForeground(TEXT_COMPLETED_COLOR);
            titleLabel.setText("<html><s>" + task.getTitle() + "</s></html>");
        } else {
            titleLabel.setForeground(TEXT_COLOR);
        }
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel dateLabel = new JLabel(task.getCreatedAt().format(DATE_FORMAT));
        dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        dateLabel.setForeground(new Color(90, 90, 100));
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(dateLabel);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(accentBar);
        leftPanel.add(checkBox);
        leftPanel.add(titlePanel);

        JLabel priorityBadge = createPriorityBadge(task.getPriority());

        JButton deleteBtn = new JButton("🗑") {
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
        deleteBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setBackground(DELETE_COLOR);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setBorderPainted(false);
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.setOpaque(false);
        deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteBtn.setPreferredSize(new Dimension(34, 30));
        deleteBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                deleteBtn.setBackground(DELETE_HOVER_COLOR);
                deleteBtn.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                deleteBtn.setBackground(DELETE_COLOR);
                deleteBtn.repaint();
            }
        });
        deleteBtn.addActionListener(e -> {
            if (onDelete != null) {
                onDelete.accept(task.getId());
            }
        });

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(priorityBadge);
        rightPanel.add(deleteBtn);

        card.add(leftPanel, BorderLayout.CENTER);
        card.add(rightPanel, BorderLayout.EAST);

        return card;
    }

    private JLabel createPriorityBadge(Priority priority) {
        JLabel badge = new JLabel(priority.getLabel()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(getBackground().getRed(), getBackground().getGreen(), getBackground().getBlue(), 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setFont(new Font("SansSerif", Font.BOLD, 11));
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        Color badgeColor = switch (priority) {
            case HIGH -> HIGH_COLOR;
            case MEDIUM -> MEDIUM_COLOR;
            case LOW -> LOW_COLOR;
        };
        badge.setForeground(badgeColor);
        badge.setBackground(badgeColor);
        badge.setPreferredSize(new Dimension(72, 26));

        return badge;
    }
}
