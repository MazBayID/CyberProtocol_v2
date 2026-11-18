# Cyber Protocol - Gameplay Design (Puzzle Levels)

Core idea: every level is a small puzzle about **navigation, sensors, and decisions**. The player writes Python that drives a robot. The program is the only way to act.

## Principles
1. **The starter code never contains the solution.** It only shows the tools.
2. **One program must beat every scenario.** A level can have several scenarios (different walls, hazards, exits). The program runs on all of them, so hard-coding one route fails. Use `run_level_suite_json`.
3. **Every run starts from the level start.** No hidden state carries over between runs.
4. **Failure is information.** Hazards, an empty battery, a closed door, and walls end the run with a clear reason (`failure` code plus message). Nothing is lost.
5. **Efficiency earns stars.** 1 star = solved. +1 if the program is within `par.lines`. +1 if the worst-case route is within `par.moves`. Lines ignore blank lines and comments.
6. **Python is unlocked per level** (`python_features`). A level without the key keeps the legacy rules (tutorials 1 and 2).

## Robot API (v2)
| Call | Returns | Notes |
|---|---|---|
| `robot.move(direction)` | position text | `"up" "down" "left" "right"`. Costs battery. Sets `facing`. |
| `robot.action()` | message | Uses the object on the current tile (cell, switch, charger, exit, checkpoint). |
| `robot.scan_distance(direction="front")` | int, cm | Map sensor: `(free cells + 1) * 10`, capped by the sensor range. `10` = blocked right in front. Legacy scenario sensors accept no direction. |
| `robot.scan_object(direction="front")` | string | `"wall" "edge" "door" "obstacle" "hazard" "cell" "exit" "checkpoint" "switch" "charger" "empty"` |
| `robot.battery()` | int | Remaining battery. |
| `robot.facing()` | string | Direction of the last move. |

A level lists the methods it exposes in `robot_api`.

## Tiles and objects
Tiles: `.` floor, `#` wall, `H` hazard (stepping on it fails the run), `D` door (blocked until a switch opens it).
Objects: `ENERGY_CELL`, `EXIT`, `CHECKPOINT`, `SWITCH` (`opens_doors`), `CHARGER`, `OBSTACLE`. Objects may set `single_use`, `resource_changes`, `required_objectives`, and `scenario_ids`.

## Chapter 1 - Training Wing
| # | Level | Concept | Python unlocked | Scenarios |
|---|---|---|---|---|
| 1 | Power Up | order of commands, walls | - | 1 |
| 2 | Long Corridor | repeat with `for` | `for` | 1 |
| 3 | Look Ahead | sensor + `if/else` | `conditions` | 2 |
| 4 | Compare Routes | variables, comparison | - | 2 |
| 5 | Follow the Corridor | `while` with unknown length | `while` | 3 |
| 6 | Open the Door | state and order (switch, door) | - | 1 |
| 7 | Battery Budget | resources, planning | - | 1 |
| 8 | Hazard Field | `scan_object`, loop + decision | - | 2 |
| 9 | Reusable Routine | functions | `functions` | 1 |
| 10 | Facility Run | everything together | `lists` | 1 |

Rules checked by tests: at most 2 new language features per level, every level is bilingual (EN/ID), every reference solution earns 3 stars, naive programs fail, and no starter code solves its level.

## Files
- `app/src/main/assets/levels/c1_*.json` - level data (schema in `docs/LEVEL_SCHEMA_V2.md`), plus `index.json`.
- `tools/build_levels.py` - regenerates the levels and verifies them against the runner. Run `python3 tools/build_levels.py`.
- `tests/level_solutions_v2.json` - reference and naive programs used by the tests.
- `tests/test_level_engine_v2.py` - engine and level tests.

## Timeline contract (for the Kotlin replay)
`move:x:y`, `scan:N`, `look:<found>`, `collect_energy`, `open_exit`, `open_door`, `door_open`, `recharge`, `interact:<id>`, `fail:<reason>`. Unknown tokens must be ignored by the UI, never crash it.

## Next (Kotlin side, not part of this change)
1. Load `levels/index.json` and each level JSON; map it to `LevelDefinition`.
2. Send the level without its text fields (`title`, `briefing`, `hints`, `starter`) to `run_level_suite_json`. Keep the payload under 50 000 characters.
3. Replay `timeline` of the `focus` scenario and show a scenario strip (pass/fail per scenario).
4. Show stars and `metrics`. Persist the best star count per level.
5. Editor: symbol bar (`( ) " : = < >` and indent), hint button (3 hints per level), reset to starter, step mode, minimum 12 sp text.
