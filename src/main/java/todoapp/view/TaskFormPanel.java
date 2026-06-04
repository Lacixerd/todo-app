package todoapp.view;

import todoapp.model.Priority;

import javax.swing.*;
import java.awt.*;

public class TaskFormPanel extends JPanel {
    private final JTextField titleField;
    private final JComboBox<Priority> priorityCombo;
    private final JButton addButton;

    public TaskFormPanel() {
        setLayout(new BorderLayout(8, 0));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        setBackground(new Color(45, 45, 48));

        titleField = new JTextField();
        titleField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        titleField.setBackground(new Color(60, 60, 65));
        titleField.setForeground(Color.WHITE);
        titleField.setCaretColor(Color.WHITE);
        titleField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 85), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        titleField.setToolTipText("Enter task title...");

        priorityCombo = new JComboBox<>(Priority.values());
        priorityCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        priorityCombo.setBackground(new Color(60, 60, 65));
        priorityCombo.setForeground(Color.WHITE);
        priorityCombo.setPreferredSize(new Dimension(110, 36));
        priorityCombo.setSelectedItem(Priority.MEDIUM);

        addButton = new JButton("Add");
        addButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        addButton.setBackground(new Color(76, 175, 80));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorderPainted(false);
        addButton.setOpaque(true);
        addButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addButton.setPreferredSize(new Dimension(80, 36));

        addButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                addButton.setBackground(new Color(56, 142, 60));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                addButton.setBackground(new Color(76, 175, 80));
            }
        });

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
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
