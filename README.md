# MC‑101 Studio

MC‑101 Studio is a multi-editor Java application designed to control the Roland MC‑101 via MIDI. The application provides several editors for managing sound parameters, clips, scenes, and a specialized scatter editor for sending Note On/Note Off messages. It also supports robust project management with options to create new projects, save, save as, and open project files in a single properties file (with extension `.mc101proj`).

## Features

- **Sound Editor**  
  Adjust up to 25 parameters using a mix of sliders (continuous values) and toggle buttons (on/off). Parameters are saved per MIDI channel.

- **Clip Editor**  
  A grid of 16 buttons (2 rows × 8 columns) that send Program Change messages on fixed MIDI channel 16. Double-click a button to rename it. The selected clip is highlighted.

- **Scene Editor**  
  A grid of 128 scene buttons arranged in 16 columns. Clicking a button sends a Program Change on fixed MIDI channel 16, and double-clicking allows renaming each scene. The last selected scene is highlighted.

- **Scatter Editor**  
  A 4×4 grid (16 buttons) sends Note On/Note Off messages for MIDI notes 60 to 75 on channel 16. The buttons toggle state: clicking the inactive button sends Note On and marks it in green; clicking it again sends Note Off and clears the highlight. Only one button may be active at a time. Buttons can be renamed via double-click. The active state resets on project load.

- **Project Management**  
  Easily create, save, and load projects. All editor settings (sound parameters, clip names, scene names, scatter names) are saved in a single properties file (`.mc101proj`). Keyboard shortcuts include:
  - **New Project**: CMD/CTRL+N
  - **Save**: CMD/CTRL+S
  - **Open Project**: CMD/CTRL+O

## Installation and Usage

### Prerequisites
- [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html) installed (Java 8 or later).

### Compilation
You can compile the source files using the provided Makefile or manually compile:

```bash
javac -d bin -cp . src/mc101studio/*.java
