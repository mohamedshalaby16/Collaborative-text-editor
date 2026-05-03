package persistence;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class TextFileManager {

    public static boolean saveTextToFile(String text, Component parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Document as TXT");
        chooser.setSelectedFile(new File("document.txt"));
        chooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));

        int result = chooser.showSaveDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) {
            return false;
        }

        File selectedFile = chooser.getSelectedFile();
        if (!selectedFile.getName().toLowerCase().endsWith(".txt")) {
            selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".txt");
        }

        try {
            Files.writeString(selectedFile.toPath(), text != null ? text : "", StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent,
                    "Failed to export file:\n" + e.getMessage(),
                    "Export Failed", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public static String loadTextFromFile(Component parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Import Document from TXT");
        chooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));

        int result = chooser.showOpenDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        Path selectedPath = chooser.getSelectedFile().toPath();
        try {
            String content = Files.readString(selectedPath, StandardCharsets.UTF_8);
            return content.replace("\r\n", "\n").replace("\r", "\n");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent,
                    "Failed to import file:\n" + e.getMessage(),
                    "Import Failed", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}
