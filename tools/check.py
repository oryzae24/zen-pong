"""Run checks without writing build products into the checkout."""
from pathlib import Path
import argparse
import os
import shutil
import subprocess
import sys
import tempfile

ROOT = Path(__file__).resolve().parents[1]


def run(args, label):
    print(label, flush=True)
    subprocess.run([str(a) for a in args], cwd=ROOT, check=True)


def java_tool(name, home):
    executable = name + (".exe" if os.name == "nt" else "")
    if home:
        path = Path(home) / "bin" / executable
        if path.is_file():
            return str(path)
        raise RuntimeError(f"JDK executable not found: {path}")
    path = shutil.which(name)
    if not path:
        raise RuntimeError("Install a JDK, set JAVA_HOME, or pass --java-home.")
    return path


def processing_app(path):
    base = Path(path).resolve()
    for app in [base, base / "app", base / "Contents/app"]:
        if list(app.glob("app-*.jar")) and (app / "resources/modes/java").is_dir():
            return app
    raise RuntimeError("Expected a Processing 4.5.7 app directory with JARs and resources/modes/java.")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--java-home", type=Path)
    parser.add_argument("--processing", type=Path, help="Processing 4.5.7 application directory")
    parser.add_argument("--arduino-cli", help="Arduino CLI executable; compile only, never upload")
    args = parser.parse_args()
    app = processing_app(args.processing) if args.processing else None
    bundled = app / "resources/jdk" if app else None
    home = args.java_home or (bundled if bundled and bundled.is_dir() else os.environ.get("JAVA_HOME"))
    java, javac = java_tool("java", home), java_tool("javac", home)
    sketch = ROOT / "processing/ZenPong"
    with tempfile.TemporaryDirectory(prefix="zen-pong-checks-") as folder:
        temp = Path(folder)
        classes = temp / "checks"
        classes.mkdir()
        run([javac, "-encoding", "UTF-8", "-d", classes,
             sketch / "PongPhysics.java", sketch / "VisualHistory.java", ROOT / "tests/CoreChecks.java"],
            "Compiling standalone checks...")
        run([java, "-cp", classes, "CoreChecks"], "Checking physics and visual history...")
        if app:
            resources = app / "resources"
            build = temp / "sketch"
            cli_cp = os.pathsep.join([str(app / "*"), str(resources / "modes/java/mode/*")])
            run([java, f"-Dcompose.application.resources.dir={resources}", "-cp", cli_cp,
                 "processing.app.ProcessingKt", "cli", f"--sketch={sketch}", f"--output={build}", "--build"],
                "Building the Processing sketch...")
            cp = os.pathsep.join(str(p) for p in [classes, build, build / "classes", app / "*",
                resources / "core/library/*", resources / "modes/java/libraries/serial/library/*"])
            sources = [ROOT / "tests" / (name + ".java") for name in ["PongChecks", "MenuChecks", "VisualChecks"]]
            run([javac, "-encoding", "UTF-8", "-cp", cp, "-d", classes, *sources],
                "Compiling sketch integration checks...")
            for name in ["PongChecks", "MenuChecks", "VisualChecks"]:
                run([java, "-Djava.awt.headless=true", "-cp", cp, name], f"Running {name}...")
        if args.arduino_cli:
            run([args.arduino_cli, "compile", "--fqbn", "arduino:avr:uno", "--build-path", temp / "arduino",
                 ROOT / "arduino/ZenPongController"], "Compiling UNO firmware...")
    print("All requested checks passed.")


if __name__ == "__main__":
    try:
        main()
    except (RuntimeError, subprocess.CalledProcessError, OSError) as error:
        print(f"Check failed: {error}", file=sys.stderr)
        sys.exit(1)
