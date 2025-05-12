package mc303studio;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class Main {

    // Holds the file for the currently open project (if any)
    private static File currentProjectFile = null;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("MC-101 Studio");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Create instances of all editors, including ScatterEditor.
            AdvancedSoundEditorPanel soundEditor = new AdvancedSoundEditorPanel();
            ClipEditor clipEditor = new ClipEditor();
            SceneEditor sceneEditor = new SceneEditor();
            ScatterEditor scatterEditor = new ScatterEditor();

            // Create a tabbed pane and add all editors as tabs.
            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.addTab("Sound Editor", soundEditor);
            tabbedPane.addTab("Clip Editor", clipEditor);
            tabbedPane.addTab("Scene Editor", sceneEditor);
            tabbedPane.addTab("Scatter Editor", scatterEditor);

            frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

            // Create menu bar with File and Help menus.
            JMenuBar menuBar = new JMenuBar();
            JMenu fileMenu = new JMenu("File");

            // New Project item (accelerator: CMD/CTRL+N)
            JMenuItem newProjectItem = new JMenuItem("New Project");
            newProjectItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
            newProjectItem.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(frame,
                        "Are you sure you want to create a new project? All unsaved changes will be lost.",
                        "New Project", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    soundEditor.newProject();
                    clipEditor.newProject();
                    sceneEditor.newProject();
                    scatterEditor.newProject();
                    currentProjectFile = null;
                }
            });

            // Save item – saves immediately if a project is open, otherwise calls Save As. (accelerator: CMD/CTRL+S)
            JMenuItem saveItem = new JMenuItem("Save");
            saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
            saveItem.addActionListener(e -> {
                if (currentProjectFile != null) {
                    saveProject(soundEditor, clipEditor, sceneEditor, scatterEditor, frame);
                } else {
                    saveAsProject(soundEditor, clipEditor, sceneEditor, scatterEditor, frame);
                }
            });

            // Save As – always opens the file chooser.
            JMenuItem saveAsItem = new JMenuItem("Save As");
            saveAsItem.addActionListener(e -> saveAsProject(soundEditor, clipEditor, sceneEditor, scatterEditor, frame));

            // Open Project item (accelerator: CMD/CTRL+O)
            JMenuItem openItem = new JMenuItem("Open Project");
            openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
            openItem.addActionListener(e -> openProject(soundEditor, clipEditor, sceneEditor, scatterEditor, frame));

            // Exit item.
            JMenuItem exitItem = new JMenuItem("Exit");
            exitItem.addActionListener(e -> System.exit(0));

            fileMenu.add(newProjectItem);
            fileMenu.addSeparator();
            fileMenu.add(saveItem);
            fileMenu.add(saveAsItem);
            fileMenu.add(openItem);
            fileMenu.addSeparator();
            fileMenu.add(exitItem);
            menuBar.add(fileMenu);

            // Help menu with About item.
            JMenu helpMenu = new JMenu("Help");
            JMenuItem aboutItem = new JMenuItem("About");
            aboutItem.addActionListener(e -> showAboutDialog());
            helpMenu.add(aboutItem);
            menuBar.add(helpMenu);

            frame.setJMenuBar(menuBar);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    /**
     * Saves the current project settings into a file.
     */
    private static void saveProject(AdvancedSoundEditorPanel soundEditor, ClipEditor clipEditor,
                                    SceneEditor sceneEditor, ScatterEditor scatterEditor, Component parent) {
        Properties props = new Properties();
        soundEditor.saveSettings(props);
        clipEditor.saveSettings(props);
        sceneEditor.saveSettings(props);
        scatterEditor.saveSettings(props);
        try (FileOutputStream fos = new FileOutputStream(currentProjectFile)) {
            props.store(fos, "MC-101 Studio Project");
            JOptionPane.showMessageDialog(parent, "Project saved successfully.", "Save Project",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Error saving project.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Prompts the user to choose a save location; if the file exists it asks for confirmation before overwriting.
     */
    private static void saveAsProject(AdvancedSoundEditorPanel soundEditor, ClipEditor clipEditor,
                                      SceneEditor sceneEditor, ScatterEditor scatterEditor, Component parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("MC-101 Studio Project (*.mc101proj)", "mc101proj"));
        int returnVal = chooser.showSaveDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".mc101proj")) {
                file = new File(file.getParentFile(), file.getName() + ".mc101proj");
            }
            if (file.exists()) {
                int confirm = JOptionPane.showConfirmDialog(parent,
                        "File already exists. Overwrite?", "Confirm Overwrite", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            currentProjectFile = file;
            saveProject(soundEditor, clipEditor, sceneEditor, scatterEditor, parent);
        }
    }

    /**
     * Opens a project from a selected file, loading all editor settings.
     * Also resets editor selections so no button is marked.
     */
    private static void openProject(AdvancedSoundEditorPanel soundEditor, ClipEditor clipEditor,
                                    SceneEditor sceneEditor, ScatterEditor scatterEditor, Component parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("MC-101 Studio Project (*.mc101proj)", "mc101proj"));
        int returnVal = chooser.showOpenDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
                soundEditor.loadSettings(props);
                clipEditor.loadSettings(props);
                sceneEditor.loadSettings(props);
                scatterEditor.loadSettings(props);
                currentProjectFile = file;
                JOptionPane.showMessageDialog(parent, "Project loaded successfully.", "Open Project",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(parent, "Error loading project.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Displays the About dialog with developer information and clickable email.
     */
    private static void showAboutDialog() {
        String html = "<html>" +
                "<h2>MC-101 Studio</h2>" +
                "<p>Version 1.0</p>" +
                "<p>Developed by Evandro Veloso Gomes</p>" +
                "<p>Contact: <a href=\"mailto:gnome_gtk2000@yahoo.com.br\">" +
                "gnome_gtk2000@yahoo.com.br</a></p>" +
                "</html>";
        JEditorPane ep = new JEditorPane("text/html", html);
        ep.setEditable(false);
        ep.setOpaque(false);
        ep.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(new java.net.URI(e.getURL().toString()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        JOptionPane.showMessageDialog(null, ep, "About MC-101 Studio", JOptionPane.INFORMATION_MESSAGE);
    }
}
