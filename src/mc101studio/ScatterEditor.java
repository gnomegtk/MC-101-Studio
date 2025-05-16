package mc101studio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Properties;

/**
 * ScatterEditor sends Note On/Note Off messages on fixed MIDI channel 16.
 * Displays 16 buttons arranged in a 4x4 grid.
 * Each button corresponds to a MIDI note from 60 to 75.
 * On a click, if the button is not active, it sends Note On and becomes active (marked in green);
 * if it is already active, it sends Note Off and deactivates.
 * When another button is clicked, any active button is deactivated.
 * Double-click allows renaming the button.
 * Settings are saved with the project.
 */
public class ScatterEditor extends JPanel {
    private JButton[] scatterButtons;
    private String[] scatterNames;
    // activeButton == -1 indicates no button is active.
    private int activeButton = -1;
    private final Color defaultButtonColor;

    public ScatterEditor() {
        setBorder(BorderFactory.createTitledBorder("Scatter Editor"));
        setLayout(new BorderLayout());
        defaultButtonColor = new JButton().getBackground();

        // Initialize default names ("1" to "16")
        scatterNames = new String[16];
        for (int i = 0; i < 16; i++) {
            scatterNames[i] = String.valueOf(i + 1);
        }

        // Create grid layout with 4 rows and 4 columns.
        JPanel scatterGrid = new JPanel(new GridLayout(4, 4, 5, 5));
        scatterButtons = new JButton[16];
        for (int i = 0; i < 16; i++) {
            final int index = i;
            scatterButtons[i] = new JButton(scatterNames[i]);
            scatterButtons[i].setPreferredSize(new Dimension(60, 60));
            scatterButtons[i].setOpaque(true);
            scatterButtons[i].setBorderPainted(true);

            // Usar mousePressed para captar cliques rapidamente.
            scatterButtons[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        if (e.getClickCount() == 2) {
                            // Duplo-clique: renomeia o botão.
                            String newName = JOptionPane.showInputDialog(
                                    ScatterEditor.this,
                                    "Enter new name for scatter button " + (index + 1) + ":",
                                    scatterNames[index]
                            );
                            if (newName != null && !newName.trim().isEmpty()) {
                                scatterNames[index] = newName.trim();
                                scatterButtons[index].setText(scatterNames[index]);
                            }
                        } else if (e.getClickCount() == 1) {
                            toggleButton(index);
                        }
                    }
                }
            });
            scatterGrid.add(scatterButtons[i]);
        }
        add(scatterGrid, BorderLayout.CENTER);
    }

    /**
     * Toggle behavior:
     * If the clicked button is active, sends Note Off and deactivates it;
     * otherwise, if another button is active, sends Note Off for that button,
     * then sends Note On for the clicked button.
     * Updates the backgrounds accordingly.
     */
    private void toggleButton(int index) {
        int channel = 15;       // Fixed MIDI channel 16 (index 15).
        int note = 60 + index;  // MIDI note from 60 to 75.
        int velocity = 100;
        if (activeButton == index) {
            MIDIManager.getInstance().sendNoteOff(channel, note, velocity);
            activeButton = -1;
        } else {
            if (activeButton != -1) {
                int prevNote = 60 + activeButton;
                MIDIManager.getInstance().sendNoteOff(channel, prevNote, velocity);
            }
            MIDIManager.getInstance().sendNoteOn(channel, note, velocity);
            activeButton = index;
        }
        // Atualiza imediatamente os botões.
        for (int i = 0; i < scatterButtons.length; i++) {
            scatterButtons[i].setBackground(new JButton().getBackground());
        }
        if (activeButton != -1) {
            scatterButtons[activeButton].setBackground(Color.GREEN);
        }
        this.revalidate();
        this.repaint();
    }

    public void newProject() {
        for (int i = 0; i < 16; i++) {
            scatterNames[i] = String.valueOf(i + 1);
            scatterButtons[i].setText(scatterNames[i]);
            scatterButtons[i].setBackground(new JButton().getBackground());
        }
        activeButton = -1;
        this.revalidate();
        this.repaint();
    }

    public void loadSettings(Properties props) {
        for (int i = 0; i < 16; i++) {
            String name = props.getProperty("scatter.name" + i);
            if (name != null) {
                scatterNames[i] = name;
                scatterButtons[i].setText(scatterNames[i]);
            }
        }
        activeButton = -1;
        for (JButton btn : scatterButtons) {
            btn.setBackground(new JButton().getBackground());
        }
        this.revalidate();
        this.repaint();
    }

    public void saveSettings(Properties props) {
        for (int i = 0; i < 16; i++) {
            props.setProperty("scatter.name" + i, scatterNames[i]);
        }
    }
}
