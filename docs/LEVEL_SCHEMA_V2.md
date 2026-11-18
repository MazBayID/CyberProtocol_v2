# Level schema v2

A level is one JSON object. The Python runner reads the gameplay keys and ignores the rest.

| Key | Type | Required | Meaning |
|---|---|---|---|
| `id`, `order`, `chapter`, `schema` | string/int | yes | identity (`schema` is 2) |
| `title`, `briefing` | `{en, id}` | UI only | texts |
| `hints` | `{en: [3], id: [3]}` | UI only | graduated hints |
| `starter` | string | UI only | starting code (must not solve the level) |
| `concept` | string | no | teaching goal |
| `python_features` | list | no | any of `conditions for while lists functions augassign`. Omitted = legacy rules |
| `map` | list of equal-length strings | yes | tiles `. # H D` |
| `robot_start`, `robot_facing` | `{x,y}`, direction | yes / no | start tile and facing (default `right`) |
| `robot_api` | list | yes | `move action scan_distance scan_object battery facing` |
| `resources` | `{battery:{initial,minimum,maximum,move_cost}}` | no | resource rules |
| `sensors` | list | no | `{id, type:"DISTANCE", source:"map"|"scenario", maximum_range, cell_size_cm}` |
| `scenarios` | list | no | `{id, map?, obstacle_positions?, sensor_readings?}`. `map` replaces the base map (same size) |
| `objects` | list | yes | see below |
| `objectives` | list | yes | `{id, completion_event}`. The level is complete when all are done |
| `par` | `{lines, moves}` | no | star thresholds |

Object keys: `id`, `type`, `position`, `scenario_ids`, `single_use`, `opens_doors` (switch), `resource_changes`, `required_objectives`, `completes_objective`, `timeline_event`, `event_message`, `prerequisite_error`.

Results (`run_level_suite_json`): `complete`, `stars`, `metrics {lines, moves, actions, scans}`, `scenarios [{id, complete, failed, error, moves}]`, `focus`, plus `output`, `error`, `timeline`, `events`, `state`, `failed`, `failure` of the focus scenario.
