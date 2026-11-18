# CYBER PROTOCOL

**Code the World. Control the System.**

Cyber Protocol is a futuristic programming, hacking, and automation simulation for Android. The player uses **Python** to control robots, process sensor information, make decisions, and build their own programs and devices.

Through a campaign of progressively harder challenges, the player learns to build, operate, and automate systems in order to complete missions.

> **Status: early development (v0.6.0).** The game is playable up to the second training mission. Everything marked *Planned* below is a direction, not a finished feature.

---

## How it plays

You do not steer the robot with a joystick. You write the program that does it.

```python
robot.move("right")
robot.move("right")
robot.action()
```

Each mission puts a robot in a simulated environment with an objective. You read the situation, write Python, press **RUN**, and watch the robot carry out your program. The environment reacts, and your next program builds on the result.

### Available now

| Training | Goal | You learn |
|---|---|---|
| **001 / First Steps** | Collect the energy cell to restore the battery, then reach the open exit. | Sequences of commands, `robot.move()`, `robot.action()` |
| **002 / Sensor Corridor** | Read the range sensor and choose a safe route. | Variables, sensor data, `if` / `else`, `robot.scan_distance()` |

Also in the current build: an intro boot sequence, a main menu, a settings screen (language, sound, vibration), and full **English / Bahasa Indonesia** support.

### Planned

- A campaign with staged missions and growing robot capabilities.
- More Python as the player progresses (loops, functions, lists, dictionaries), unlocked level by level.
- More sensors, devices, and machines to control and automate.
- Hacking and system-control challenges.
- Writing and reusing your own programs and devices.

---

## Design principles

- **Learn by doing.** Every new idea appears inside a mission that needs it.
- **Programs, not buttons.** The player's code is the only way to act.
- **Safe to fail.** A wrong program gives clear feedback and can be run again.
- **Fully offline.** No account and no network permission are required.

---

## Tech

| Area | Choice |
|---|---|
| Platform | Android (minSdk 24, arm64-v8a) |
| UI | Kotlin, Jetpack Compose |
| Python | CPython 3.13 via [Chaquopy](https://chaquo.com/chaquopy/) |
| Code execution | Restricted training runner (`app/src/main/python/runner.py`): allow-listed syntax, execution time and size limits |
| Languages | English, Bahasa Indonesia |

> The runner is a restricted training environment. It is **not** a hardened sandbox and must not be used to run untrusted third-party code.

### Project layout

```text
app/src/main/java/com/zeroday/cyberprotocol/   Kotlin UI and game screens
app/src/main/python/runner.py                  Python runner and robot simulation
app/src/main/assets/loading/                   Loading animation frames
tests/                                         Python runner tests
.github/workflows/                             CI (tests, debug preview APK, release)
```

### Running the tests

```bash
python -m unittest discover -s tests -v
```

Android builds run in CI (`.github/workflows/`). They need Android platform 36 and the Android SDK build-tools.

---

## Ringkasan (Bahasa Indonesia)

Cyber Protocol adalah game simulasi pemrograman, hacking, dan otomasi futuristik untuk Android. Pemain memakai **Python** untuk mengendalikan robot, memproses informasi sensor, membuat keputusan, serta mengembangkan program dan perangkat sendiri. Lewat campaign dengan tantangan bertahap, pemain belajar membangun, mengoperasikan, dan mengotomatisasi sistem untuk menyelesaikan misi.

**Status:** masih tahap awal (v0.6.0). Saat ini tersedia dua misi latihan (Training 001 dan 002), dengan dukungan penuh Bahasa Indonesia dan English. Fitur lain pada bagian *Planned* masih berupa arah pengembangan.

Semua organisasi, tokoh, dan data dalam game ini fiktif.
