package mc303studio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Clip Editor sends Program Change messages on fixed MIDI channel 16.
 * Allows renaming clip buttons via double-click.
 * The selected button is highlighted in green.
 */
public class ClipEditor extends JPanel {
    private JButton[] clipButtons;
    private String[] clipNames; // Names for clip buttons.
    private static int selectedClip = -1; // Currently selected clip index.
    private final Color defaultButtonColor;

    public ClipEditor() {
        setBorder(BorderFactory.createTitledBorder("Clip Editor"));
        setLayout(new BorderLayout());
        // Using a new JButton's background for consistency.
        defaultButtonColor = new JButton().getBackground();

        // Initialize default clip names ("1" to "16")
        clipNames = new String[16];
        for (int i = 0; i < 16; i++) {
            clipNames[i] = String.valueOf(i + 1);
        }

        JPanel clipGrid = new JPanel(new GridLayout(2, 8, 5, 5));
        clipButtons = new JButton[16];
        for (int i = 0; i < 16; i++) {
            final int clipIndex = i;
            clipButtons[i] = new JButton(clipNames[i]);
            clipButtons[i].setMargin(new Insets(5, 5, 5, 5));
            clipButtons[i].setPreferredSize(new Dimension(500, 14));
            clipButtons[i].setOpaque(true);
            clipButtons[i].setBorderPainted(true);

            // Usando mousePressed para capturar cliques mais rapidamente.
            clipButtons[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        if (e.getClickCount() == 2) {
                            // Duplo-clique: renomear.
                            String newName = JOptionPane.showInputDialog(
                                    ClipEditor.this,
                                    "Enter new name for clip " + (clipIndex + 1) + ":",
                                    clipNames[clipIndex]);
                            if (newName != null && !newName.trim().isEmpty()) {
                                clipNames[clipIndex] = newName.trim();
                                clipButtons[clipIndex].setText(clipNames[clipIndex]);
                            }
                        } else if (e.getClickCount() == 1) {
                            sendClipChange(clipIndex);
                            markSelectedClip(clipIndex);
                        }
                    }
                }
            });
            clipGrid.add(clipButtons[i]);
        }
        add(clipGrid, BorderLayout.CENTER);
    }

    private void sendClipChange(int clipIndex) {
        // Sends Program Change on fixed MIDI channel 16 (index 15)
        MIDIManager.getInstance().sendProgramChange(15, clipIndex);
        System.out.println("Sent Clip Change: Channel 16, Clip " + (clipIndex + 1));
    }

    /**
     * Resets all buttons to their default background and marks the selected one in green.
     */
    private void markSelectedClip(int clipIndex) {
        for (JButton btn : clipButtons) {
            btn.setBackground(new JButton().getBackground());
        }
        clipButtons[clipIndex].setBackground(Color.GREEN);
        selectedClip = clipIndex;
        // Atualiza a interface uma única vez.
        this.revalidate();
        this.repaint();
    }

    public void newProject() {
        for (int i = 0; i < 16; i++) {
            clipNames[i] = String.valueOf(i + 1);
            clipButtons[i].setText(clipNames[i]);
            clipButtons[i].setBackground(new JButton().getBackground());
        }
        selectedClip = -1;
        this.revalidate();
        this.repaint();
    }

    public void loadSettings(java.util.Properties props) {
        for (int i = 0; i < 16; i++) {
            String name = props.getProperty("clip.name" + i);
            if (name != null) {
                clipNames[i] = name;
                clipButtons[i].setText(clipNames[i]);
            }
        }
        selectedClip = -1;
        for (JButton btn : clipButtons) {
            btn.setBackground(new JButton().getBackground());
        }
        this.revalidate();
        this.repaint();
    }

    public void saveSettings(java.util.Properties props) {
        for (int i = 0; i < 16; i++) {
            props.setProperty("clip.name" + i, clipNames[i]);
        }
    }
}
