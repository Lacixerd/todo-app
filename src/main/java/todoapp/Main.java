package todoapp;

import todoapp.controller.TaskController;
import todoapp.model.TaskRepository;
import todoapp.view.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            TaskRepository repository = new TaskRepository();
            TaskController controller = new TaskController(repository);
            MainFrame frame = new MainFrame(controller);
            frame.setVisible(true);
        });
    }
}
