package mc101studio;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Properties;

/**
 * Advanced Sound Editor Panel for controlling 25 parameters.
 * Continuous parameters use a slider (with a value label) and on/off parameters use a toggle button.
 * Parameter values are stored separately per MIDI channel.
 */
public class AdvancedSoundEditorPanel extends JPanel {
    private JComboBox<Integer> midiChannelSelector;
    private int midiChannel = 0; // Current MIDI channel (0–15)
    private final int numParameters = 25;

    private String[] parameterNames = {
        "Modulation (CC 1)", "Portamento Time (CC 5)", "Volume (CC 7)", "Panpot (CC 10)",
        "Expression (CC 11)", "Hold 1 (CC 64)", "Portamento (CC 65)", "Sostenuto (CC 66)",
        "Soft (CC 67)", "Legato Foot Switch (CC 68)", "Resonance (CC 71)", "Release Time (CC 72)",
        "Attack Time (CC 73)", "Cutoff (CC 74)", "Decay Time (CC 75)", "Vibrato Rate (CC 76)",
        "Vibrato Depth (CC 77)", "Vibrato Delay (CC 78)", "FILTER Knob (CC 79)", "MOD Knob (CC 80)",
        "FX Knob (CC 81)", "SOUND Knob (CC 82)", "Portamento control (CC 84)",
        "GP Effect 1 (CC 91)", "GP Effect 3 (CC 92)"
    };

    private int[] ccNumbers = {
        1, 5, 7, 10, 11, 64, 65, 66, 67, 68,
        71, 72, 73, 74, 75, 76, 77, 78, 79, 80,
        81, 82, 84, 91, 92
    };

    private boolean[] isToggle = {
        false, false, false, false, false,
        true, true, true, true, true,
        false, false, false, false, false,
        false, false, false, false, false,
        false, false, false, false, false
    };

    private int[][] channelValues;
    private JComponent[] controls;
    private boolean updating = false;

    public AdvancedSoundEditorPanel() {
        setBorder(BorderFactory.createTitledBorder("Sound Editor"));
        setLayout(new BorderLayout());

        channelValues = new int[16][numParameters];
        for (int ch = 0; ch < 16; ch++) {
            for (int p = 0; p < numParameters; p++) {
                channelValues[ch][p] = isToggle[p] ? 0 : 64;
            }
        }
        controls = new JComponent[numParameters];

        JPanel midiChannelPanel = new JPanel();
        midiChannelPanel.setBorder(BorderFactory.createTitledBorder("MIDI Channel"));
        midiChannelSelector = new JComboBox<>();
        for (int i = 1; i <= 16; i++) {
            midiChannelSelector.addItem(i);
        }
        midiChannelSelector.setSelectedItem(1);
        midiChannelSelector.addActionListener(e -> {
            int newChannel = (Integer) midiChannelSelector.getSelectedItem() - 1;
            if (newChannel != midiChannel) {
                updating = true;
                midiChannel = newChannel;
                for (int i = 0; i < numParameters; i++) {
                    if (isToggle[i]) {
                        JToggleButton toggle = (JToggleButton) controls[i];
                        if (channelValues[midiChannel][i] == 127) {
                            toggle.setSelected(true);
                            toggle.setText("On");
                        } else {
                            toggle.setSelected(false);
                            toggle.setText("Off");
                        }
                    } else {
                        JSlider slider = (JSlider) controls[i];
                        slider.setValue(channelValues[midiChannel][i]);
                    }
                }
                updating = false;
            }
        });
        midiChannelPanel.add(new JLabel("MIDI Channel:"));
        midiChannelPanel.add(midiChannelSelector);
        add(midiChannelPanel, BorderLayout.NORTH);

        JPanel controlsPanel = new JPanel(new GridLayout(numParameters, 1, 1, 1));
        for (int i = 0; i < numParameters; i++) {
            controlsPanel.add(createLabeledControl(parameterNames[i], ccNumbers[i], i));
        }
        JScrollPane scrollPane = new JScrollPane(controlsPanel);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createLabeledControl(String labelText, int ccNumber, int index) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));

        JLabel paramLabel = new JLabel(labelText);
        paramLabel.setPreferredSize(new Dimension(220, 14));
        paramLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));

        if (isToggle[index]) {
            JToggleButton toggle = new JToggleButton("Off");
            toggle.setPreferredSize(new Dimension(500, 14));
            toggle.setFont(new Font("SansSerif", Font.PLAIN, 10));
            toggle.setSelected(channelValues[midiChannel][index] == 127);
            toggle.setText(toggle.isSelected() ? "On" : "Off");
            toggle.addActionListener(e -> {
                if (updating) return;
                int value = toggle.isSelected() ? 127 : 0;
                channelValues[midiChannel][index] = value;
                toggle.setText(value == 127 ? "On" : "Off");
                MIDIManager.getInstance().sendControlChange(midiChannel, ccNumber, value);
            });
            controls[index] = toggle;
            panel.add(paramLabel);
            panel.add(toggle);
        } else {
            JSlider slider = new JSlider(0, 127, channelValues[midiChannel][index]);
            slider.setPreferredSize(new Dimension(500, 14));
            slider.setFont(new Font("SansSerif", Font.PLAIN, 10));
            controls[index] = slider;
            JLabel valueLabel = new JLabel(String.valueOf(channelValues[midiChannel][index]));
            valueLabel.setPreferredSize(new Dimension(40, 14));
            valueLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
            slider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    if (updating) return;
                    int value = slider.getValue();
                    channelValues[midiChannel][index] = value;
                    MIDIManager.getInstance().sendControlChange(midiChannel, ccNumber, value);
                    valueLabel.setText(String.valueOf(value));
                }
            });
            panel.add(paramLabel);
            panel.add(slider);
            panel.add(valueLabel);
        }
        return panel;
    }

    /**
     * Resets all parameter values to the defaults.
     */
    public void newProject() {
        for (int ch = 0; ch < 16; ch++) {
            for (int p = 0; p < numParameters; p++) {
                channelValues[ch][p] = isToggle[p] ? 0 : 64;
            }
        }
        updating = true;
        for (int i = 0; i < numParameters; i++) {
            if (isToggle[i]) {
                JToggleButton toggle = (JToggleButton) controls[i];
                toggle.setSelected(false);
                toggle.setText("Off");
            } else {
                JSlider slider = (JSlider) controls[i];
                slider.setValue(64);
            }
        }
        updating = false;
    }

    /**
     * Saves the sound editor settings to the provided Properties.
     */
    public void saveSettings(Properties props) {
        for (int ch = 0; ch < 16; ch++) {
            for (int p = 0; p < numParameters; p++) {
                props.setProperty("sound.ch" + ch + ".p" + p, String.valueOf(channelValues[ch][p]));
            }
        }
    }

    /**
     * Loads the sound editor settings from the provided Properties.
     */
    public void loadSettings(Properties props) {
        for (int ch = 0; ch < 16; ch++) {
            for (int p = 0; p < numParameters; p++) {
                String key = "sound.ch" + ch + ".p" + p;
                String val = props.getProperty(key);
                if (val != null) {
                    try {
                        channelValues[ch][p] = Integer.parseInt(val);
                    } catch (Exception ex) {
                        // ignore parse errors
                    }
                }
            }
        }
        updating = true;
        for (int i = 0; i < numParameters; i++) {
            if (isToggle[i]) {
                JToggleButton toggle = (JToggleButton) controls[i];
                if (channelValues[midiChannel][i] == 127) {
                    toggle.setSelected(true);
                    toggle.setText("On");
                } else {
                    toggle.setSelected(false);
                    toggle.setText("Off");
                }
            } else {
                JSlider slider = (JSlider) controls[i];
                slider.setValue(channelValues[midiChannel][i]);
            }
        }
        updating = false;
    }
}
