# Development and verification

## Requirements

- Play or edit: Processing 4, Java mode. Tested locally with Processing 4.5.7 on Windows.
- Core checks: Python 3.10+ and a JDK 17+ with `java` and `javac` on PATH, or `JAVA_HOME` set.
- Full sketch integration checks: Processing 4.5.7's application directory and its bundled JDK. The helper supports its `app/resources` layout.
- Firmware compilation: Arduino CLI and `arduino:avr` core 1.8.8, or the Arduino IDE.
- Editing the controller project: a standard Fritzing installation. Custom parts are embedded in the `.fzz`.

There are no third-party Python packages or additional Processing libraries to install.

## Run checks

From the repository root:

```sh
python tools/check.py
```

This compiles and runs the standalone physics and visual-history checks in a temporary directory. It does not open a serial port or require Processing or Fritzing to be installed.

For a Processing 4.5.7 installation, pass its application directory (the directory containing the application JARs and `resources/`):

```sh
python tools/check.py --processing /path/to/Processing/app
```

On Windows, quote paths containing spaces:

```powershell
python tools/check.py --processing "C:\Tools\Processing\app"
```

The helper uses the bundled JDK when available. `--java-home` can select a JDK explicitly. It invokes the Processing CLI, compiles the real sketch and runs `PongChecks`, `MenuChecks` and `VisualChecks` against that build. A differently packaged Processing installation may need its own CLI build; normal IDE use does not depend on this helper.

To additionally compile firmware, install the core and pass an Arduino CLI executable:

```sh
arduino-cli core update-index
arduino-cli core install arduino:avr@1.8.8
python tools/check.py --arduino-cli arduino-cli
```

This compiles for `arduino:avr:uno`; it never uploads. Options can be combined.

The GitHub workflow runs the core checks on Windows and Linux, and compiles UNO firmware on Linux. Full Processing integration, graphics, sound and physical controller behavior are checked locally; CI does not exercise them.

## Controller diagrams

Open `hardware/ZenPongController.fzz` in Fritzing to inspect or edit both views. Standard parts come from Fritzing's core library; the three custom parts and their graphics are embedded in the project. No separate part download is needed.

If the wiring changes, update the project and both SVG diagrams. Review the connections in both native views and inspect the exported SVGs visually; the automated game checks do not validate the diagrams. The breadboard SVG marks unconnected crossings with white gaps; native Fritzing rendering may show crossings differently.

## Design boundaries

- `PongPhysics.java` owns the ball, paddle, reflection, selected speed and score. It uses seconds and court pixels, independently of screen scale.
- `VisualHistory.java` bounds and expires samples; `Effects.pde` draws them. Mode changes never modify ball velocity.
- `ZenPong.pde` owns state transitions, fixed-step updates and keyboard input.
- `Hardware.pde` validates serial input and detects disconnects.
- `Audio.pde` schedules notes and generates PC audio on a separate thread.
- `Render.pde` draws the minimal interface and vertical pause menu.

Test coverage includes ten simulated minutes of central rallies across all five speeds, reflection angles, misses, input edges, menu navigation and bounded visual history. It does not measure frame rate or prove electrical behavior. After changes, also play with the keyboard and, if available, the assembled controller.
