package todoapp.view;

import todoapp.model.Priority;

import javax.swing.*;
import java.awt.*;

public class TaskFormPanel extends JPanel {
    private final JTextField titleField;
    private final JComboBox<Priority> priorityCombo;
    private final JButton addButton;

    private static final Color PANEL_BG = new Color(38, 38, 44);
    private static final Color FIELD_BG = new Color(50, 50, 56);
    private static final Color FIELD_BORDER = new Color(65, 65, 72);
    private static final Color FIELD_FOCUS_BORDER = new Color(100, 149, 237);
    private static final Color ADD_BTN_COLOR = new Color(76, 175, 80);
    private static final Color ADD_BTN_HOVER = new Color(56, 142, 60);

    public TaskFormPanel() {
        setLayout(new BorderLayout(10, 0));
        setBorder(BorderFactory.createEmptyBorder(14, 16, 10, 16));
        setBackground(PANEL_BG);

        titleField = new JTextField() {
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
                g2.setColor(hasFocus() ? FIELD_FOCUS_BORDER : FIELD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        titleField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        titleField.setBackground(FIELD_BG);
        titleField.setForeground(Color.WHITE);
        titleField.setCaretColor(Color.WHITE);
        titleField.setOpaque(false);
        titleField.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        titleField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) { titleField.repaint(); }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) { titleField.repaint(); }
        });

        JLabel placeholder = new JLabel("  ✏️  What needs to be done?");
        placeholder.setFont(new Font("SansSerif", Font.PLAIN, 13));
        placeholder.setForeground(new Color(110, 110, 120));
        titleField.setLayout(new BorderLayout());
        titleField.add(placeholder);
        titleField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                placeholder.setVisible(titleField.getText().isEmpty());
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        priorityCombo = new JComboBox<>(Priority.values());
        priorityCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        priorityCombo.setBackground(FIELD_BG);
        priorityCombo.setForeground(Color.WHITE);
        priorityCombo.setPreferredSize(new Dimension(115, 40));
        priorityCombo.setSelectedItem(Priority.MEDIUM);
        priorityCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Priority p) {
                    Color c = switch (p) {
                        case HIGH -> new Color(239, 83, 80);
                        case MEDIUM -> new Color(255, 183, 77);
                        case LOW -> new Color(102, 187, 106);
                    };
                    setText("● " + p.getLabel());
                    setForeground(isSelected ? Color.WHITE : c);
                    setBackground(isSelected ? new Color(60, 60, 68) : FIELD_BG);
                }
                setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return this;
            }
        });

        addButton = new JButton("+ Add Task") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        addButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        addButton.setBackground(ADD_BTN_COLOR);
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorderPainted(false);
        addButton.setContentAreaFilled(false);
        addButton.setOpaque(false);
        addButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addButton.setPreferredSize(new Dimension(110, 40));

        addButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                addButton.setBackground(ADD_BTN_HOVER);
                addButton.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                addButton.setBackground(ADD_BTN_COLOR);
                addButton.repaint();
            }
        });

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(priorityCombo);
        rightPanel.add(addButton);

        add(titleField, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    public String getTitle() {
        return titleField.getText();
    }

    public Priority getPriority() {
        return (Priority) priorityCombo.getSelectedItem();
    }

    public void clearFields() {
        titleField.setText("");
        titleField.requestFocusInWindow();
    }

    public JButton getAddButton() {
        return addButton;
    }

    public JTextField getTitleField() {
        return titleField;
    }
}
