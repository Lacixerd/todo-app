package todoapp.view;

import todoapp.model.Priority;
import todoapp.model.Task;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class TaskListPanel extends JPanel {
    private final JPanel taskContainer;
    private Consumer<String> onDelete;
    private Consumer<String> onToggle;

    private static final Color BG_COLOR = new Color(37, 37, 40);
    private static final Color CARD_COLOR = new Color(50, 50, 55);
    private static final Color CARD_HOVER_COLOR = new Color(60, 60, 66);
    private static final Color TEXT_COLOR = new Color(220, 220, 225);
    private static final Color TEXT_COMPLETED_COLOR = new Color(120, 120, 125);
    private static final Color HIGH_COLOR = new Color(244, 67, 54);
    private static final Color MEDIUM_COLOR = new Color(255, 167, 38);
    private static final Color LOW_COLOR = new Color(102, 187, 106);
    private static final Color DELETE_COLOR = new Color(183, 28, 28);
    private static final Color DELETE_HOVER_COLOR = new Color(229, 57, 53);

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
            JLabel emptyLabel = new JLabel("No tasks yet. Add one above!");
            emptyLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
            emptyLabel.setForeground(new Color(140, 140, 145));
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyLabel.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));
            taskContainer.add(emptyLabel);
        } else {
            for (Task task : tasks) {
                taskContainer.add(createTaskCard(task));
                taskContainer.add(Box.createRigidArea(new Dimension(0, 4)));
            }
        }

        taskContainer.add(Box.createVerticalGlue());
        taskContainer.revalidate();
        taskContainer.repaint();
    }

    private JPanel createTaskCard(Task task) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(2, 12, 2, 12),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(CARD_HOVER_COLOR);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(CARD_COLOR);
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

        JLabel titleLabel = new JLabel(task.getTitle());
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        if (task.isCompleted()) {
            titleLabel.setForeground(TEXT_COMPLETED_COLOR);
            titleLabel.setText("<html><s>" + task.getTitle() + "</s></html>");
        } else {
            titleLabel.setForeground(TEXT_COLOR);
        }

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(checkBox);
        leftPanel.add(titleLabel);

        JLabel priorityBadge = createPriorityBadge(task.getPriority());

        JButton deleteBtn = new JButton("✕");
        deleteBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setBackground(DELETE_COLOR);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setBorderPainted(false);
        deleteBtn.setOpaque(true);
        deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteBtn.setPreferredSize(new Dimension(32, 28));
        deleteBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                deleteBtn.setBackground(DELETE_HOVER_COLOR);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                deleteBtn.setBackground(DELETE_COLOR);
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
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setFont(new Font("SansSerif", Font.BOLD, 11));
        badge.setForeground(Color.WHITE);
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));

        Color badgeColor = switch (priority) {
            case HIGH -> HIGH_COLOR;
            case MEDIUM -> MEDIUM_COLOR;
            case LOW -> LOW_COLOR;
        };
        badge.setBackground(badgeColor);
        badge.setPreferredSize(new Dimension(70, 24));

        return badge;
    }
}
