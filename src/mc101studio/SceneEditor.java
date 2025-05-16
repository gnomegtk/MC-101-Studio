package mc101studio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Scene Editor sends Program Change messages on fixed MIDI channel 16.
 * Allows renaming scene buttons via double-click.
 * The selected button is highlighted in green.
 * Arranged in a grid with 16 columns.
 */
public class SceneEditor extends JPanel {
    private JButton[] sceneButtons;
    private String[] sceneNames; // Names for scene buttons.
    private static int selectedScene = -1; // Currently selected scene index.
    private final Color defaultButtonColor;

    public SceneEditor() {
        setBorder(BorderFactory.createTitledBorder("Scene Editor"));
        setLayout(new BorderLayout());
        defaultButtonColor = new JButton().getBackground();

        // Initialize default scene names ("1" to "128")
        sceneNames = new String[128];
        for (int i = 0; i < 128; i++) {
            sceneNames[i] = String.valueOf(i + 1);
        }

        JPanel sceneGrid = new JPanel(new GridLayout(0, 16, 2, 2));
        sceneButtons = new JButton[128];
        for (int i = 0; i < 128; i++) {
            final int sceneIndex = i;
            sceneButtons[i] = new JButton(sceneNames[i]);
            sceneButtons[i].setPreferredSize(new Dimension(40, 20));
            sceneButtons[i].setMargin(new Insets(2, 2, 2, 2));
            sceneButtons[i].setOpaque(true);
            sceneButtons[i].setBorderPainted(true);

            // Usar mousePressed para captar cliques de forma imediata.
            sceneButtons[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        if (e.getClickCount() == 2) {
                            // Duplo-clique: renomear
                            String newName = JOptionPane.showInputDialog(
                                    SceneEditor.this,
                                    "Enter new name for scene " + (sceneIndex + 1) + ":",
                                    sceneNames[sceneIndex]);
                            if (newName != null && !newName.trim().isEmpty()) {
                                sceneNames[sceneIndex] = newName.trim();
                                sceneButtons[sceneIndex].setText(sceneNames[sceneIndex]);
                            }
                        } else if (e.getClickCount() == 1) {
                            sendSceneChange(sceneIndex);
                            markSelectedScene(sceneIndex);
                        }
                    }
                }
            });
            sceneGrid.add(sceneButtons[i]);
        }
        JScrollPane scrollPane = new JScrollPane(sceneGrid);
        scrollPane.setPreferredSize(new Dimension(800, 200));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void sendSceneChange(int sceneIndex) {
        // Sends Program Change on fixed MIDI channel 16 (index 15)
        MIDIManager.getInstance().sendProgramChange(15, sceneIndex);
        System.out.println("Sent Scene Change: Channel 16, Scene " + (sceneIndex + 1));
    }

    /**
     * Resets all scene buttons to their default background and marks only the selected one in green.
     */
    private void markSelectedScene(int sceneIndex) {
        for (JButton btn : sceneButtons) {
            btn.setBackground(new JButton().getBackground());
        }
        sceneButtons[sceneIndex].setBackground(Color.GREEN);
        selectedScene = sceneIndex;
        this.revalidate();
        this.repaint();
    }

    public void newProject() {
        for (int i = 0; i < 128; i++) {
            sceneNames[i] = String.valueOf(i + 1);
            sceneButtons[i].setText(sceneNames[i]);
            sceneButtons[i].setBackground(new JButton().getBackground());
        }
        selectedScene = -1;
        this.revalidate();
        this.repaint();
    }

    public void loadSettings(java.util.Properties props) {
        for (int i = 0; i < 128; i++) {
            String name = props.getProperty("scene.name" + i);
            if (name != null) {
                sceneNames[i] = name;
                sceneButtons[i].setText(sceneNames[i]);
            }
        }
        selectedScene = -1;
        for (JButton btn : sceneButtons) {
            btn.setBackground(new JButton().getBackground());
        }
        this.revalidate();
        this.repaint();
    }

    public void saveSettings(java.util.Properties props) {
        for (int i = 0; i < 128; i++) {
            props.setProperty("scene.name" + i, sceneNames[i]);
        }
    }
}
