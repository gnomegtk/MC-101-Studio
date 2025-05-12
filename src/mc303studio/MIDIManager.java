package mc303studio;

import javax.sound.midi.*;
import javax.swing.JOptionPane;

public class MIDIManager {
    private static MIDIManager instance;
    private Receiver receiver;
    private boolean deviceConnected = false;
    private boolean warningShown = false; // Evita múltiplos alertas

    private MIDIManager() {
        setupMIDI();
    }

    public static MIDIManager getInstance() {
        if (instance == null) {
            instance = new MIDIManager();
        }
        return instance;
    }

    private void setupMIDI() {
        try {
            System.out.println("Available MIDI Devices:");
            MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
            boolean foundMC101 = false;

            for (MidiDevice.Info info : infos) {
                System.out.println(" - " + info.getName());
                try {
                    if (info.getName().contains("MC-101")) { 
                        MidiDevice device = MidiSystem.getMidiDevice(info);
                        device.open();
                        receiver = device.getReceiver();
                        deviceConnected = true;
                        foundMC101 = true;
                        System.out.println("Connected to MC-101: " + info.getName());
                        break;
                    }
                } catch (MidiUnavailableException ex) {
                    System.out.println("Failed to open MIDI device: " + info.getName());
                }
            }

            if (!foundMC101) {
                System.out.println("MC-101 not found. Ensure the connection is properly configured.");
                deviceConnected = false;
                if (!warningShown) {
                    JOptionPane.showMessageDialog(
                        null, 
                        "MC-101 not connected. Ensure the connection is properly configured.", 
                        "MIDI Error", 
                        JOptionPane.ERROR_MESSAGE
                    );
                    warningShown = true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean isDeviceConnected() {
        return deviceConnected;
    }

    // Método para enviar mensagens de Control Change
    public void sendControlChange(int channel, int control, int value) {
        if (!deviceConnected) {
            System.out.println("MC-101 not connected. Ignoring Control Change.");
            showWarning();
            return;
        }
        try {
            ShortMessage msg = new ShortMessage();
            msg.setMessage(ShortMessage.CONTROL_CHANGE, channel, control, value);
            receiver.send(msg, -1);
            System.out.println("Sent Control Change | Channel: " + (channel + 1) + ", Control: " + control + ", Value: " + value);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Método para enviar Program Change (usado para troca de clips e scenes)
    public void sendProgramChange(int channel, int program) {
        if (!deviceConnected) {
            System.out.println("MC-101 not connected. Ignoring Program Change.");
            showWarning();
            return;
        }
        try {
            ShortMessage msg = new ShortMessage();
            msg.setMessage(ShortMessage.PROGRAM_CHANGE, channel, program, 0);
            receiver.send(msg, -1);
            System.out.println("Sent Program Change | Channel: " + (channel + 1) + ", Program: " + program);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Método para enviar Note On (usado para Scatter Pad, por exemplo)
    public void sendNoteOn(int channel, int note, int velocity) {
        if (!deviceConnected) {
            System.out.println("MC-101 not connected. Ignoring Note On.");
            showWarning();
            return;
        }
        try {
            ShortMessage msg = new ShortMessage();
            msg.setMessage(ShortMessage.NOTE_ON, channel, note, velocity);
            receiver.send(msg, -1);
            System.out.println("Sent Note On | Channel: " + (channel + 1) + ", Note: " + note + ", Velocity: " + velocity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Método para enviar Note Off (para liberar a tecla após Note On)
    public void sendNoteOff(int channel, int note, int velocity) {
        if (!deviceConnected) {
            System.out.println("MC-101 not connected. Ignoring Note Off.");
            showWarning();
            return;
        }
        try {
            ShortMessage msg = new ShortMessage();
            msg.setMessage(ShortMessage.NOTE_OFF, channel, note, velocity);
            receiver.send(msg, -1);
            System.out.println("Sent Note Off | Channel: " + (channel + 1) + ", Note: " + note + ", Velocity: " + velocity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Método auxiliar para exibir mensagem de erro se o dispositivo não estiver conectado
    private void showWarning() {
        if (!warningShown) {
            JOptionPane.showMessageDialog(
                null, 
                "MC-101 not connected. Ensure the connection is properly configured.", 
                "MIDI Error", 
                JOptionPane.ERROR_MESSAGE
            );
            warningShown = true;
        }
    }
}
