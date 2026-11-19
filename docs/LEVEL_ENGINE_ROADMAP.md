# Cyber Protocol — Data-Driven Level Engine

## Goal

New campaign levels should be authored as data, not by copying a tutorial screen, Kotlin snapshot, and Python runner function for every level.

A level definition describes:
- the grid map and robot start position;
- interactive objects, positions, prerequisites, and resource effects;
- sensors and scenario-specific readings/obstacles;
- objectives and stable completion events;
- robot APIs exposed to the player;
- Python teaching-feature metadata;
- the starter Python program.

The engine interprets that definition and coordinates execution, state updates, events, and presentation.

## Implemented foundation

### Step 1 — Catalog and schema (merged in PR #6)
- Added core/level/LevelDefinition.kt.
- Added core/level/TrainingLevelCatalog.kt with Tutorial 1 and Tutorial 2.
- Added structural validation and JVM tests.
- Added this migration roadmap.

### Step 2 — Generic Python runtime (merged in PR #7)
- Added run_level_json, driven by a level definition.
- Movement, map boundaries, scenario obstacles, sensors, objectives, interactable objects, and resource changes run through shared logic.
- Kept run_tutorial_json and run_tutorial2_json as compatibility wrappers for older callers and regression tests.

### Step 3 — Kotlin runtime integration (merged in PR #8 and PR #9)
- Extended level data with object effects, prerequisites, resources, and sensor objective completion.
- Added LevelDefinition.toRunnerJson() as the Kotlin-to-Python runtime contract.
- Android tutorial execution now calls run_level_json with the catalog definition.
- Added shared LevelSessionState, LevelRunResult, and LevelExecutionService.
- Existing Tutorial 1 and Tutorial 2 snapshots/results remain compatibility adapters for the current Compose screens.

### Step 4 — Reusable presentation (initial extraction merged in PR #10 and PR #11)
- Extracted the shared guidance-terminal layout.
- Extracted the shared isometric floor map and animated robot renderer.
- The renderer now reads the map rows from TrainingLevelCatalog.
- Level-specific object sprites, guidance text, status rows, and scenario controls remain in their respective tutorial adapters.
- Existing terminal proportions, display insets, tile coordinates, and robot animation timing were preserved in code.

### Step 5 — Campaign progress foundation (merged in PR #12)
- Added CampaignProgress, keyed by stable level ids.
- Added separate SharedPreferences storage for completed level ids.
- Tutorial 1 and Tutorial 2 completion events are persisted.
- Unlock state is derived from catalog sequence rather than tutorial-specific branches.

## Remaining work

1. **Campaign selection and session resume**
   - [x] Add a level-select screen with locked/unlocked/completed states.
   - [x] Allow replaying completed levels and selecting any unlocked level.
   - [x] Refresh the visible campaign progress after a level completion.
   - [ ] Persist and restore an unfinished level session (robot position, resources, objectives, scenario, and editor source).
   - Keep campaign completion and active-session saves separate from settings and language preferences.

2. **Finish reusable presentation**
   - Move remaining level-specific scene/header/footer details into presentation data.
   - Extract the common editor/run-control shell where this does not change the established three-terminal layout.
   - Keep Tutorial 1 panel proportions: guidance 0.29, room 0.40, editor 0.31.
   - Preserve the existing display insets and bottom-anchored guidance status row.

3. **Add new campaign content**
   - Add a level by creating a catalog definition and tests, not by copying a screen or runner.
   - Expand Python features deliberately. PythonFeature is currently teaching metadata; it is not yet a complete per-level syntax permission system.

## Compatibility and safety rules

- Tutorial 1 and Tutorial 2 must keep their current behavior during migration.
- Compatibility snapshots remain until the presentation layer has fully moved to shared session state.
- Validate every level definition before starting a session.
- Do not treat the in-process Python runner as a hardened security sandbox.
- A successful CI unit test does not replace APK build verification or on-device visual testing.
- No device-level visual verification has been performed for the shared renderer changes yet.
