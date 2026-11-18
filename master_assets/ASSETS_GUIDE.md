# Cyber Protocol — Complete Asset Library Guide

**Game:** Cyber Protocol  
**Tagline:** Code the World. Control the System.  
**Genre:** Programmable Simulation Sandbox  
**Visual Direction:** Detailed Sci-Fi Isometric Simulation (Strict 2:1 Isometric Projection)  
**Logical Floor Tile Unit:** `128 × 64` px  
**Total Assets:** 195

---

## Directory Structure

```text
CyberProtocol_Assets/
├── assets/
│   ├── environment/
│   │   ├── tile_floor_metal.png
│   │   ├── tile_floor_corner.png
│   │   ├── tile_wall_straight.png
│   │   ├── tile_wall_corner.png
│   │   ├── structure_window.png
│   │   ├── structure_ramp.png
│   │   ├── tile_floor_hazard.png
│   │   ├── tile_floor_vent.png
│   │   ├── tile_floor_platform.png
│   │   ├── structure_door.png
│   │   ├── prop_warning_beacon.png
│   │   ├── prop_power_cable.png
│   │   ├── structure_blast_door.png
│   │   ├── structure_elevator.png
│   │   ├── prop_data_terminal.png
│   │   ├── prop_supply_crate.png
│   │   ├── prop_warning_barrier.png
│   │   ├── prop_data_archive.png
│   │   ├── prop_access_key.png
│   │   ├── prop_energy_crystal.png
│   │   ├── prop_broken_machine.png
│   │   ├── prop_repair_parts.png
│   │   ├── prop_mission_beacon.png
│   │   ├── tile_floor_grate.png
│   │   ├── tile_floor_warning.png
│   │   ├── tile_wall_window.png
│   │   ├── tile_wall_panel.png
│   │   ├── prop_industrial_pipe.png
│   │   ├── prop_ventilation_unit.png
│   │   ├── prop_industrial_lamp.png
│   │   ├── prop_robot_docking_bay.png
│   │   ├── prop_hazard_container.png
│   │   ├── prop_radiation_sign.png
│   │   ├── prop_mission_data_drive.png
│   │   ├── prop_security_keycard.png
│   │   ├── prop_objective_marker.png
│   │   ├── prop_industrial_workstation.png
│   │   ├── prop_storage_cabinet.png
│   │   └── prop_industrial_crate_stack.png
│   ├── characters/
│   │   └── robots/
│   │       ├── robot_courier_idle.png
│   │       ├── robot_courier_move.png
│   │       ├── robot_maintenance.png
│   │       ├── robot_maintenance_drone.png
│   │       ├── anim_robot_idle.png
│   │       ├── anim_robot_walk.png
│   │       ├── anim_robot_work.png
│   │       ├── anim_robot_interact.png
│   │       ├── anim_robot_damaged.png
│   │       ├── anim_robot_repair.png
│   │       └── anim_robot_turn.png
│   ├── machines/
│   │   ├── machine_terminal.png
│   │   ├── machine_generator.png
│   │   ├── machine_storage.png
│   │   ├── machine_conveyor.png
│   │   ├── machine_processor.png
│   │   ├── machine_charging_station.png
│   │   ├── machine_network_router.png
│   │   ├── machine_server_rack.png
│   │   ├── machine_control_panel.png
│   │   ├── machine_sensor.png
│   │   ├── machine_repair_station.png
│   │   ├── machine_security_camera.png
│   │   ├── anim_generator_active.png
│   │   ├── anim_conveyor_active.png
│   │   ├── anim_terminal_active.png
│   │   ├── anim_processor_active.png
│   │   ├── anim_door_open.png
│   │   ├── anim_door_close.png
│   │   ├── anim_charging_active.png
│   │   ├── anim_storage_open.png
│   │   ├── machine_security_turret.png
│   │   ├── machine_security_barrier.png
│   │   ├── machine_research_station.png
│   │   └── machine_data_analyzer.png
│   ├── resources/
│   │   ├── resource_energy_cell.png
│   │   ├── resource_data_chip.png
│   │   ├── resource_circuit_board.png
│   │   ├── resource_metal_ingot.png
│   │   ├── resource_coolant.png
│   │   ├── resource_research_data.png
│   │   ├── resource_robot_parts.png
│   │   └── resource_robot_core.png
│   ├── ui/
│   │   ├── ui_selection_marker.png
│   │   ├── bg_main_menu.png
│   │   ├── bg_gameplay_facility.png
│   │   ├── bg_server_room.png
│   │   ├── bg_abandoned_facility.png
│   │   ├── bg_digital_network.png
│   │   ├── bg_loading_screen.png
│   │   ├── ui_health_bar.png
│   │   ├── ui_energy_bar.png
│   │   ├── ui_inventory_slot.png
│   │   ├── ui_interaction_prompt.png
│   │   ├── ui_object_status.png
│   │   ├── ui_minimap_frame.png
│   │   ├── ui_notification_panel.png
│   │   ├── ui_terminal_frame.png
│   │   ├── ui_progress_bar.png
│   │   ├── ui_resource_counter.png
│   │   ├── ui_command_panel.png
│   │   ├── ui_tooltip_panel.png
│   │   ├── ui_panel_standard.png
│   │   ├── ui_panel_compact.png
│   │   ├── ui_panel_wide.png
│   │   ├── ui_panel_modal.png
│   │   ├── ui_panel_sidebar.png
│   │   ├── ui_panel_header.png
│   │   ├── ui_tab_bar.png
│   │   ├── ui_tab_item_states.png
│   │   ├── ui_scrollbar_vertical.png
│   │   ├── ui_scrollbar_horizontal.png
│   │   ├── ui_button_primary_states.png
│   │   ├── ui_button_secondary_states.png
│   │   ├── ui_button_danger_states.png
│   │   ├── ui_icon_button_states.png
│   │   ├── ui_toggle_switch_states.png
│   │   ├── ui_checkbox_states.png
│   │   ├── ui_radio_states.png
│   │   ├── ui_slider_track.png
│   │   ├── ui_slider_handle.png
│   │   ├── ui_dropdown_field.png
│   │   ├── ui_dropdown_arrow.png
│   │   ├── ui_keybind_badge.png
│   │   ├── ui_button_focus_ring.png
│   │   ├── ui_terminal_frame_classic.png
│   │   ├── ui_terminal_frame_secure.png
│   │   ├── ui_terminal_frame_diagnostic.png
│   │   ├── ui_terminal_frame_fullscreen.png
│   │   ├── ui_terminal_tab_states.png
│   │   ├── ui_terminal_prompt_prefix.png
│   │   ├── ui_terminal_cursor_blink.png
│   │   ├── ui_terminal_status_strip.png
│   │   ├── ui_terminal_command_input.png
│   │   ├── ui_terminal_output_panel.png
│   │   ├── ui_terminal_scrollbar.png
│   │   ├── ui_terminal_lock_overlay.png
│   │   ├── ui_code_editor_frame.png
│   │   ├── ui_code_editor_tab_states.png
│   │   ├── ui_code_line_gutter.png
│   │   ├── ui_code_line_highlight.png
│   │   ├── ui_code_selection.png
│   │   ├── ui_code_cursor.png
│   │   ├── ui_code_breakpoint.png
│   │   ├── ui_code_run_controls.png
│   │   ├── ui_code_console_frame.png
│   │   ├── ui_code_error_panel.png
│   │   ├── ui_code_syntax_legend.png
│   │   ├── ui_icons_system_atlas.png
│   │   ├── ui_icons_command_atlas.png
│   │   ├── ui_icons_navigation_atlas.png
│   │   ├── ui_icons_resource_atlas.png
│   │   ├── ui_icons_status_atlas.png
│   │   ├── ui_loading_progress.png
│   │   ├── ui_notification_states.png
│   │   ├── ui_tooltip_variants.png
│   │   ├── ui_dialogue_box.png
│   │   ├── ui_choice_button_states.png
│   │   ├── ui_objective_tracker.png
│   │   ├── ui_task_complete_stamp.png
│   │   ├── ui_warning_overlay.png
│   │   └── ui_pause_overlay.png
│   └── effects/
│       ├── fx_selection_glow.png
│       ├── fx_electric_spark.png
│       ├── fx_machine_active.png
│       ├── fx_machine_error.png
│       ├── fx_data_transfer.png
│       ├── fx_energy_flow.png
│       ├── fx_resource_collect.png
│       ├── fx_resource_process.png
│       ├── fx_interaction_success.png
│       ├── fx_interaction_failed.png
│       ├── fx_robot_spawn.png
│       ├── fx_robot_despawn.png
│       ├── fx_machine_startup.png
│       ├── fx_machine_shutdown.png
│       ├── fx_electric_arc.png
│       ├── fx_explosion_small.png
│       ├── fx_smoke.png
│       ├── fx_security_laser.png
│       ├── fx_environment_leak.png
│       ├── anim_loading_spinner.png
│       ├── anim_loading_scan.png
│       ├── anim_loading_hex_core.png
│       ├── anim_loading_robot_diagnostic.png
│       ├── fx_loading_data_stream.png
│       ├── anim_glitch_signal.png
│       ├── anim_glitch_screen_tear.png
│       ├── fx_glitch_rgb_split.png
│       ├── fx_glitch_scanlines.png
│       ├── fx_glitch_static_noise.png
│       ├── fx_glitch_pixel_corruption.png
│       ├── fx_glitch_data_fragments.png
│       ├── fx_glitch_warning_overlay.png
│       ├── fx_glitch_signal_loss.png
│       └── fx_glitch_boot_distortion.png
├── originals/
│   └── <high-resolution unscaled PNG originals (195 files)>
├── metadata/
│   └── asset_manifest.json
└── ASSETS_GUIDE.md
```

---

## Included Game-Ready Assets (195)

| Asset ID | Filename | Category | Dimensions | Format | Transparency | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| `tile_floor_metal` | `tile_floor_metal.png` | Environment | 256 × 152 px | PNG | Yes (Alpha) | Standard modular 2:1 isometric industrial metal floor tile with dark graphite |
| `tile_floor_corner` | `tile_floor_corner.png` | Environment | 256 × 152 px | PNG | Yes (Alpha) | Modular floor tile variation matching tile_floor_metal geometry and materials |
| `tile_wall_straight` | `tile_wall_straight.png` | Environment | 256 × 256 px | PNG | Yes (Alpha) | Straight modular armored wall segment featuring dark graphite panels, deep blu |
| `tile_wall_corner` | `tile_wall_corner.png` | Environment | 256 × 256 px | PNG | Yes (Alpha) | True 90-degree L-shaped modular wall corner matching tile_wall_straight constr |
| `robot_courier_idle` | `robot_courier_idle.png` | Characters | 256 × 256 px | PNG | Yes (Alpha) | Compact two-legged autonomous industrial courier robot in a stable idle pose w |
| `robot_courier_move` | `robot_courier_move.png` | Characters | 1024 × 256 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`256×256` per frame) |
| `machine_terminal` | `machine_terminal.png` | Machines | 256 × 256 px | PNG | Yes (Alpha) | Freestanding industrial computer terminal with dark graphite housing, angled c |
| `resource_energy_cell` | `resource_energy_cell.png` | Resources | 128 × 128 px | PNG | Yes (Alpha) | Compact portable industrial energy canister with dark graphite casing, brushed |
| `ui_selection_marker` | `ui_selection_marker.png` | UI | 256 × 181 px | PNG | Yes (Alpha) | Minimal crisp cyan geometric corner-bracket reticle aligned to a 2:1 isometric |
| `fx_selection_glow` | `fx_selection_glow.png` | Effects | 251 × 156 px | PNG | Yes (Alpha) | Soft cyan-teal isometric diamond selection perimeter glow designed to render b |
| `machine_generator` | `machine_generator.png` | Machines | 256 × 256 px | PNG | Yes (Alpha) | Freestanding 2:1 isometric industrial power generator with dark graphite casin |
| `machine_storage` | `machine_storage.png` | Machines | 256 × 256 px | PNG | Yes (Alpha) | Modular 2:1 isometric armored industrial storage vault featuring locker drawer |
| `machine_conveyor` | `machine_conveyor.png` | Machines | 256 × 256 px | PNG | Yes (Alpha) | Modular straight 2:1 isometric industrial conveyor belt with dark graphite sid |
| `machine_processor` | `machine_processor.png` | Machines | 256 × 256 px | PNG | Yes (Alpha) | Freestanding 2:1 isometric fabrication processor unit with enclosed robotic la |
| `machine_charging_station` | `machine_charging_station.png` | Machines | 256 × 256 px | PNG | Yes (Alpha) | Low-profile 2:1 isometric inductive robot charging platform with recessed cyan |
| `robot_maintenance` | `robot_maintenance.png` | Characters | 256 × 256 px | PNG | Yes (Alpha) | Bipedal autonomous industrial maintenance and repair robot with dual cyan opti |
| `fx_electric_spark` | `fx_electric_spark.png` | Effects | 256 × 256 px | PNG | Yes (Alpha) | Crisp high-voltage cyan-teal branching electric spark discharge burst with lum |
| `fx_machine_active` | `fx_machine_active.png` | Effects | 256 × 152 px | PNG | Yes (Alpha) | Isometric concentric glowing cyan-teal holographic operational indicator rings |
| `fx_machine_error` | `fx_machine_error.png` | Effects | 256 × 256 px | PNG | Yes (Alpha) | Dark graphite octagonal hazard housing with glowing amber-crimson warning brac |
| `fx_data_transfer` | `fx_data_transfer.png` | Effects | 256 × 152 px | PNG | Yes (Alpha) | Directional 2:1 isometric holographic cyan telemetry ribbon featuring uniform |
| `fx_energy_flow` | `fx_energy_flow.png` | Effects | 256 × 152 px | PNG | Yes (Alpha) | Luminous 2:1 isometric cyan-teal plasma power beam with surging energy core no |
| `resource_data_chip` | `resource_data_chip.png` | Resources | 128 × 128 px | PNG | Yes (Alpha) | Compact octagonal armored dark-graphite data storage chip with brushed steel c |
| `resource_circuit_board` | `resource_circuit_board.png` | Resources | 128 × 128 px | PNG | Yes (Alpha) | Modular multi-layer dark slate PCB assembly inside a brushed steel frame with |
| `resource_metal_ingot` | `resource_metal_ingot.png` | Resources | 128 × 128 px | PNG | Yes (Alpha) | Precision-machined chamfered industrial alloy ingot bar in brushed muted steel |
| `resource_coolant` | `resource_coolant.png` | Resources | 128 × 128 px | PNG | Yes (Alpha) | Pressurized cryogenic coolant cylinder with dark graphite end-caps, top valve |
| `structure_window` | `structure_window.png` | Environment | 256 × 256 px | PNG | Yes (Alpha) | Straight 2:1 isometric modular armored wall segment matching tile_wall_straigh |
| `structure_ramp` | `structure_ramp.png` | Environment | 256 × 256 px | PNG | Yes (Alpha) | Modular 2:1 isometric non-slip metallic incline ramp with raised brushed-steel |
| `tile_floor_hazard` | `tile_floor_hazard.png` | Environment | 256 × 152 px | PNG | Yes (Alpha) | 2:1 isometric industrial metal floor tile matching tile_floor_metal geometry w |
| `tile_floor_vent` | `tile_floor_vent.png` | Environment | 256 × 152 px | PNG | Yes (Alpha) | 2:1 isometric heavy industrial ventilation floor tile featuring a recessed exh |
| `tile_floor_platform` | `tile_floor_platform.png` | Environment | 256 × 192 px | PNG | Yes (Alpha) | Modular elevated 2:1 isometric metal deck platform supported by an exposed dar |
| `structure_door` | `structure_door.png` | Environment | 256 × 256 px | PNG | Yes (Alpha) | Straight 2:1 isometric modular armored bulkhead wall with sliding brushed-stee |
| `prop_warning_beacon` | `prop_warning_beacon.png` | Environment | 128 × 128 px | PNG | Yes (Alpha) | Compact 2:1 isometric caged amber hazard beacon light mounted on a dark graphi |
| `prop_power_cable` | `prop_power_cable.png` | Environment | 256 × 152 px | PNG | Yes (Alpha) | Modular 2:1 isometric ribbed heavy industrial power cable segment with brushed |
| `machine_network_router` | `machine_network_router.png` | Machines | 256 × 256 px | PNG | Yes (Alpha) | Freestanding 2:1 isometric telemetry routing unit with dual top parabolic rece |
| `machine_server_rack` | `machine_server_rack.png` | Machines | 256 × 256 px | PNG | Yes (Alpha) | Freestanding 2:1 isometric mainframe server rack cabinet with glass blade-bay |
| `machine_control_panel` | `machine_control_panel.png` | Machines | 256 × 256 px | PNG | Yes (Alpha) | 2:1 isometric operator control station featuring dual waveform monitors, physi |
| `machine_sensor` | `machine_sensor.png` | Machines | 256 × 256 px | PNG | Yes (Alpha) | Compact 2:1 isometric cylindrical radar and optical sensor pylon with glowing |
| `structure_blast_door` | `structure_blast_door.png` | Environment | 256 × 256 px | PNG | Yes (Alpha) | Heavy double-leaf armored 2:1 isometric security blast door featuring X-braced |
| `structure_elevator` | `structure_elevator.png` | Environment | 256 × 256 px | PNG | Yes (Alpha) | 2:1 isometric industrial lift deck with corner hydraulic guide posts, brushed- |
| `machine_repair_station` | `machine_repair_station.png` | Machines | 256 × 256 px | PNG | Yes (Alpha) | 2:1 isometric robot servicing bay featuring an overhead dual-arm manipulator g |
| `machine_security_camera` | `machine_security_camera.png` | Machines | 128 × 128 px | PNG | Yes (Alpha) | Articulated wall-mounted industrial surveillance camera in dark graphite armor |
| `prop_data_terminal` | `prop_data_terminal.png` | Environment | 256 × 256 px | PNG | Yes (Alpha) | Slim freestanding 2:1 isometric facility kiosk pedestal with angled cyan telem |
| `prop_supply_crate` | `prop_supply_crate.png` | Environment | 256 × 256 px | PNG | Yes (Alpha) | 2:1 isometric reinforced industrial logistics container with brushed-steel cor |
| `prop_warning_barrier` | `prop_warning_barrier.png` | Environment | 256 × 256 px | PNG | Yes (Alpha) | Modular 2:1 isometric industrial safety barricade with brushed-steel tubular f |
| `robot_maintenance_drone` | `robot_maintenance_drone.png` | Characters | 256 × 256 px | PNG | Yes (Alpha) | Hovering quad-rotor industrial maintenance drone with dual cyan optical sensor |
| `prop_data_archive` | `prop_data_archive.png` | Environment | 256 × 256 px | PNG | Yes (Alpha) | Armored hexagonal data repository cylinder with glowing cyan holographic stora |
| `prop_access_key` | `prop_access_key.png` | Environment | 128 × 128 px | PNG | Yes (Alpha) | High-clearance industrial security key fob with brushed-steel housing, gold co |
| `prop_energy_crystal` | `prop_energy_crystal.png` | Environment | 128 × 128 px | PNG | Yes (Alpha) | Luminous cyan-teal crystalline power cluster anchored inside a dark graphite h |
| `prop_broken_machine` | `prop_broken_machine.png` | Environment | 256 × 256 px | PNG | Yes (Alpha) | Weathered 2:1 isometric offline terminal with shattered display glass, scorche |
| `prop_repair_parts` | `prop_repair_parts.png` | Environment | 128 × 128 px | PNG | Yes (Alpha) | Dark graphite modular parts tray stocked with machined steel gears, servo coup |
| `prop_mission_beacon` | `prop_mission_beacon.png` | Environment | 256 × 256 px | PNG | Yes (Alpha) | Heavy octagonal deployable beacon pylon projecting a floating glowing cyan iso |
| `bg_main_menu` | `bg_main_menu.png` | UI | 1376 × 768 px | PNG | Opaque (16:9) | Full-bleed 16:9 atmospheric sci-fi control room and isometric catwalk corridor |
| `bg_gameplay_facility` | `bg_gameplay_facility.png` | UI | 1376 × 768 px | PNG | Opaque (16:9) | Deep atmospheric industrial reactor shaft and steel truss scaffolding backdrop |
| `bg_server_room` | `bg_server_room.png` | UI | 1376 × 768 px | PNG | Opaque (16:9) | Symmetric high-tech data center aisle lined with illuminated cyan server racks |
| `bg_abandoned_facility` | `bg_abandoned_facility.png` | UI | 1376 × 768 px | PNG | Opaque (16:9) | Moody decommissioned industrial hall with hanging cables, dusty console banks, |
| `bg_digital_network` | `bg_digital_network.png` | UI | 1376 × 768 px | PNG | Opaque (16:9) | Dark obsidian cyberspace background featuring a central glowing 2:1 isometric |
| `bg_loading_screen` | `bg_loading_screen.png` | UI | 1376 × 768 px | PNG | Opaque (16:9) | Atmospheric overhead-lit industrial bay featuring an autonomous courier robot |
| `ui_health_bar` | `ui_health_bar.png` | UI | 256 × 64 px | PNG | Yes (Alpha) | Industrial chamfered dark graphite HUD health bar container with brushed steel |
| `ui_energy_bar` | `ui_energy_bar.png` | UI | 256 × 64 px | PNG | Yes (Alpha) | Sleek industrial energy HUD meter housing with dark slate interior, power glyp |
| `ui_inventory_slot` | `ui_inventory_slot.png` | UI | 128 × 128 px | PNG | Yes (Alpha) | Square chamfered industrial item socket with dark graphite bezel, brushed stee |
| `ui_interaction_prompt` | `ui_interaction_prompt.png` | UI | 256 × 144 px | PNG | Yes (Alpha) | Compact chamfered industrial HUD action plate featuring brushed steel framing, |
| `ui_object_status` | `ui_object_status.png` | UI | 256 × 144 px | PNG | Yes (Alpha) | Modular dark-graphite inspector status card with header bar, recessed progress |
| `ui_minimap_frame` | `ui_minimap_frame.png` | UI | 256 × 256 px | PNG | Yes (Alpha) | Heavy octagonal brushed-steel minimap bezel featuring engraved N/E/S/W compass |
| `ui_notification_panel` | `ui_notification_panel.png` | UI | 384 × 216 px | PNG | Yes (Alpha) | Clean wide dark-slate system alert container with chamfered metallic trim, lef |
| `ui_terminal_frame` | `ui_terminal_frame.png` | UI | 512 × 288 px | PNG | Yes (Alpha) | Wide industrial terminal window chassis with heavy brushed-steel side vents, t |
| `ui_progress_bar` | `ui_progress_bar.png` | UI | 256 × 64 px | PNG | Yes (Alpha) | Horizontal dark-graphite progress track with precision tick marks, brushed ste |
| `ui_resource_counter` | `ui_resource_counter.png` | UI | 256 × 144 px | PNG | Yes (Alpha) | Framed metallic HUD counter module with left hex icon socket, dark numeric dis |
| `ui_command_panel` | `ui_command_panel.png` | UI | 384 × 216 px | PNG | Yes (Alpha) | Heavy modular industrial command deck container with recessed action slot bays |
| `ui_tooltip_panel` | `ui_tooltip_panel.png` | UI | 256 × 192 px | PNG | Yes (Alpha) | Clean chamfered dark slate hover tooltip container featuring brushed steel cor |
| `anim_robot_idle` | `anim_robot_idle.png` | Characters | 1024 × 256 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`256×256` per frame) |
| `anim_robot_walk` | `anim_robot_walk.png` | Characters | 1024 × 256 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`256×256` per frame) |
| `anim_robot_work` | `anim_robot_work.png` | Characters | 1024 × 256 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`256×256` per frame) |
| `anim_robot_interact` | `anim_robot_interact.png` | Characters | 1024 × 256 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`256×256` per frame) |
| `anim_robot_damaged` | `anim_robot_damaged.png` | Characters | 1024 × 256 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`256×256` per frame) |
| `anim_robot_repair` | `anim_robot_repair.png` | Characters | 1024 × 256 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`256×256` per frame) |
| `anim_robot_turn` | `anim_robot_turn.png` | Characters | 1024 × 256 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`256×256` per frame) |
| `anim_generator_active` | `anim_generator_active.png` | Machines | 1024 × 256 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`256×256` per frame) |
| `anim_conveyor_active` | `anim_conveyor_active.png` | Machines | 1024 × 256 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`256×256` per frame) |
| `anim_terminal_active` | `anim_terminal_active.png` | Machines | 1024 × 256 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`256×256` per frame) |
| `anim_processor_active` | `anim_processor_active.png` | Machines | 1024 × 256 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`256×256` per frame) |
| `anim_door_open` | `anim_door_open.png` | Machines | 1024 × 256 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`256×256` per frame) |
| `anim_door_close` | `anim_door_close.png` | Machines | 1024 × 256 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`256×256` per frame) |
| `anim_charging_active` | `anim_charging_active.png` | Machines | 1024 × 256 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`256×256` per frame) |
| `anim_storage_open` | `anim_storage_open.png` | Machines | 1024 × 256 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`256×256` per frame) |
| `fx_resource_collect` | `fx_resource_collect.png` | Effects | 256 × 256 px | PNG | Yes (Alpha) | Crisp upward-spiraling cyan-teal energy ribbon and crystalline shard burst sig |
| `fx_resource_process` | `fx_resource_process.png` | Effects | 256 × 256 px | PNG | Yes (Alpha) | Holographic isometric concentric fabrication grid with converging cyan process |
| `fx_interaction_success` | `fx_interaction_success.png` | Effects | 256 × 256 px | PNG | Yes (Alpha) | Glowing octagonal emerald-cyan radial pulse waves framing an illuminated holog |
| `fx_interaction_failed` | `fx_interaction_failed.png` | Effects | 256 × 256 px | PNG | Yes (Alpha) | Dark octagonal industrial warning housing with glowing crimson-amber brackets |
| `fx_robot_spawn` | `fx_robot_spawn.png` | Effects | 256 × 256 px | PNG | Yes (Alpha) | Vertical translucent cyan-teal scanline projection column rising from concentr |
| `fx_robot_despawn` | `fx_robot_despawn.png` | Effects | 256 × 256 px | PNG | Yes (Alpha) | Upward-dissolving cyan holographic voxel particle vortex rising off contractin |
| `fx_machine_startup` | `fx_machine_startup.png` | Effects | 256 × 256 px | PNG | Yes (Alpha) | Luminous 2:1 isometric cyan boot wave ring sweeping upward through glowing orb |
| `fx_machine_shutdown` | `fx_machine_shutdown.png` | Effects | 256 × 256 px | PNG | Yes (Alpha) | Contracting muted slate mechanical ring frame showing powered-down segments an |
| `fx_electric_arc` | `fx_electric_arc.png` | Effects | 256 × 256 px | PNG | Yes (Alpha) | High-voltage branching cyan plasma arc with incandescent white filament core a |
| `fx_explosion_small` | `fx_explosion_small.png` | Effects | 256 × 256 px | PNG | Yes (Alpha) | Stylized compact industrial overload blast combining a bright amber-orange cor |
| `fx_smoke` | `fx_smoke.png` | Effects | 256 × 256 px | PNG | Yes (Alpha) | Layered cool graphite and pale mist-cyan volumetric industrial steam cloud wit |
| `tile_floor_grate` | `tile_floor_grate.png` | Environment | 256 × 152 px | PNG | Yes (Alpha) | Intact modular 2:1 isometric perforated diamond-mesh steel floor grate framed |
| `tile_floor_warning` | `tile_floor_warning.png` | Environment | 256 × 152 px | PNG | Yes (Alpha) | Modular 2:1 isometric industrial metal floor tile featuring weathered amber ca |
| `tile_wall_window` | `tile_wall_window.png` | Environment | 256 × 256 px | PNG | Yes (Alpha) | Straight 2:1 isometric modular armored wall panel fitted with braced reinforce |
| `tile_wall_panel` | `tile_wall_panel.png` | Environment | 256 × 256 px | PNG | Yes (Alpha) | Straight 2:1 isometric industrial wall segment integrated with recessed servic |
| `prop_industrial_pipe` | `prop_industrial_pipe.png` | Environment | 256 × 152 px | PNG | Yes (Alpha) | Heavy 2:1 isometric dark-steel industrial pipeline assembly with bolted flange |
| `prop_ventilation_unit` | `prop_ventilation_unit.png` | Environment | 256 × 256 px | PNG | Yes (Alpha) | Modular 2:1 isometric heavy ventilation blower unit featuring dual steel exhau |
| `prop_industrial_lamp` | `prop_industrial_lamp.png` | Environment | 128 × 128 px | PNG | Yes (Alpha) | Slim 2:1 isometric industrial light stanchion with bolted octagonal floor base |
| `machine_security_turret` | `machine_security_turret.png` | Machines | 256 × 256 px | PNG | Yes (Alpha) | Freestanding 2:1 isometric twin-barrel defense turret on an armored octagonal |
| `machine_security_barrier` | `machine_security_barrier.png` | Machines | 256 × 256 px | PNG | Yes (Alpha) | Twin-pylon 2:1 isometric security gate projecting horizontal cyan-amber force- |
| `fx_security_laser` | `fx_security_laser.png` | Effects | 256 × 152 px | PNG | Yes (Alpha) | Isometric diagonal crimson-coral high-energy security laser beam streak with i |
| `machine_research_station` | `machine_research_station.png` | Machines | 256 × 256 px | PNG | Yes (Alpha) | Modular 2:1 isometric research workbench machine featuring an enclosed glass s |
| `machine_data_analyzer` | `machine_data_analyzer.png` | Machines | 256 × 256 px | PNG | Yes (Alpha) | Vertical 2:1 isometric diagnostic mainframe tower with angled cyan spectral wa |
| `resource_research_data` | `resource_research_data.png` | Resources | 128 × 128 px | PNG | Yes (Alpha) | Compact octagonal armored capsule cradling an illuminated cyan-teal holographi |
| `resource_robot_parts` | `resource_robot_parts.png` | Resources | 128 × 128 px | PNG | Yes (Alpha) | Compact dark-graphite upgrade carrier tray holding a precision machined servo |
| `resource_robot_core` | `resource_robot_core.png` | Resources | 128 × 128 px | PNG | Yes (Alpha) | Octagonal armored robot CPU and power core module featuring gold contact colla |
| `prop_robot_docking_bay` | `prop_robot_docking_bay.png` | Environment | 256 × 256 px | PNG | Yes (Alpha) | Modular 2:1 isometric robot docking cradle featuring pneumatic wheel clamps, c |
| `prop_hazard_container` | `prop_hazard_container.png` | Environment | 256 × 256 px | PNG | Yes (Alpha) | Heavy ribbed dark-steel industrial chemical waste tank mounted on a square pal |
| `prop_radiation_sign` | `prop_radiation_sign.png` | Environment | 128 × 128 px | PNG | Yes (Alpha) | Freestanding 2:1 isometric safety stanchion with octagonal graphite base, tria |
| `fx_environment_leak` | `fx_environment_leak.png` | Effects | 256 × 152 px | PNG | Yes (Alpha) | Isometric luminescent chartreuse-teal chemical spill puddle with crisp liquid |
| `prop_mission_data_drive` | `prop_mission_data_drive.png` | Environment | 128 × 128 px | PNG | Yes (Alpha) | Armored portable field storage case featuring brushed steel shock corners, loc |
| `prop_security_keycard` | `prop_security_keycard.png` | Environment | 128 × 128 px | PNG | Yes (Alpha) | Brushed-steel and dark-graphite executive clearance pass lying in 2:1 isometri |
| `prop_objective_marker` | `prop_objective_marker.png` | Environment | 256 × 256 px | PNG | Yes (Alpha) | Floating 3D isometric cyan diamond-chevron holographic marker hovering above a |
| `prop_industrial_workstation` | `prop_industrial_workstation.png` | Environment | 256 × 256 px | PNG | Yes (Alpha) | Modular 2:1 isometric engineer workbench with pegboard tools, dual cyan diagno |
| `prop_storage_cabinet` | `prop_storage_cabinet.png` | Environment | 256 × 256 px | PNG | Yes (Alpha) | Tall narrow 2:1 isometric dark-steel facility locker cabinet with louvered dou |
| `prop_industrial_crate_stack` | `prop_industrial_crate_stack.png` | Environment | 256 × 256 px | PNG | Yes (Alpha) | Modular 2:1 isometric stack of three reinforced industrial supply crates resti |
| `ui_panel_standard` | `ui_panel_standard.png` | UI | 384 × 288 px | PNG | Yes (Alpha) | Front-facing dark graphite and brushed steel rectangular interface panel with  |
| `ui_panel_compact` | `ui_panel_compact.png` | UI | 256 × 192 px | PNG | Yes (Alpha) | Compact front-facing dark graphite UI window housing with chamfered metallic b |
| `ui_panel_wide` | `ui_panel_wide.png` | UI | 512 × 256 px | PNG | Yes (Alpha) | Wide landscape dark-slate industrial interface frame featuring brushed steel c |
| `ui_panel_modal` | `ui_panel_modal.png` | UI | 448 × 336 px | PNG | Yes (Alpha) | Elevated front-facing modal dialog window chassis with dedicated title header  |
| `ui_panel_sidebar` | `ui_panel_sidebar.png` | UI | 256 × 512 px | PNG | Yes (Alpha) | Tall vertical dark-graphite sidebar dock panel with chamfered left/right steel |
| `ui_panel_header` | `ui_panel_header.png` | UI | 512 × 128 px | PNG | Yes (Alpha) | Wide horizontal top HUD header container framed in brushed steel with symmetri |
| `ui_tab_bar` | `ui_tab_bar.png` | UI | 512 × 48 px | PNG | Yes (Alpha) | Clean horizontal dark-graphite tab bar track with brushed steel frame, top bev |
| `ui_tab_item_states` | `ui_tab_item_states.png` | UI | 512 × 48 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`128×48` per frame) |
| `ui_scrollbar_vertical` | `ui_scrollbar_vertical.png` | UI | 24 × 192 px | PNG | Yes (Alpha) | Slim front-facing vertical industrial scrollbar track in dark graphite with a  |
| `ui_scrollbar_horizontal` | `ui_scrollbar_horizontal.png` | UI | 192 × 24 px | PNG | Yes (Alpha) | Slim front-facing horizontal industrial scrollbar track in dark graphite with  |
| `ui_button_primary_states` | `ui_button_primary_states.png` | UI | 768 × 64 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`192×64` per frame) |
| `ui_button_secondary_states` | `ui_button_secondary_states.png` | UI | 768 × 64 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`192×64` per frame) |
| `ui_button_danger_states` | `ui_button_danger_states.png` | UI | 768 × 64 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`192×64` per frame) |
| `ui_icon_button_states` | `ui_icon_button_states.png` | UI | 256 × 64 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`64×64` per frame) |
| `ui_toggle_switch_states` | `ui_toggle_switch_states.png` | UI | 384 × 48 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`96×48` per frame) |
| `ui_checkbox_states` | `ui_checkbox_states.png` | UI | 256 × 64 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`64×64` per frame) |
| `ui_radio_states` | `ui_radio_states.png` | UI | 256 × 64 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`64×64` per frame) |
| `ui_slider_track` | `ui_slider_track.png` | UI | 256 × 32 px | PNG | Yes (Alpha) | Front-facing dark graphite recessed slider groove with brushed steel endcaps a |
| `ui_slider_handle` | `ui_slider_handle.png` | UI | 48 × 48 px | PNG | Yes (Alpha) | Compact octagonal brushed-steel slider thumb knob with recessed cyan grip notc |
| `ui_dropdown_field` | `ui_dropdown_field.png` | UI | 256 × 48 px | PNG | Yes (Alpha) | Clean front-facing dark-slate dropdown select container with brushed steel fra |
| `ui_dropdown_arrow` | `ui_dropdown_arrow.png` | UI | 32 × 32 px | PNG | Yes (Alpha) | Crisp cyan-teal metallic chevron downward arrow glyph sized for dropdown field |
| `ui_keybind_badge` | `ui_keybind_badge.png` | UI | 96 × 48 px | PNG | Yes (Alpha) | Blank dark-graphite chamfered keyboard keycap plate with raised brushed-steel  |
| `ui_button_focus_ring` | `ui_button_focus_ring.png` | UI | 208 × 80 px | PNG | Yes (Alpha) | Crisp cyan chamfered corner-bracket focus outline designed to overlay active o |
| `ui_terminal_frame_classic` | `ui_terminal_frame_classic.png` | UI | 512 × 288 px | PNG | Yes (Alpha) | Front-facing industrial command-line terminal chassis with brushed steel casin |
| `ui_terminal_frame_secure` | `ui_terminal_frame_secure.png` | UI | 512 × 288 px | PNG | Yes (Alpha) | High-security armored terminal window housing with reinforced steel side locks |
| `ui_terminal_frame_diagnostic` | `ui_terminal_frame_diagnostic.png` | UI | 512 × 288 px | PNG | Yes (Alpha) | Precision engineering diagnostic terminal frame featuring chamfered side vents |
| `ui_terminal_frame_fullscreen` | `ui_terminal_frame_fullscreen.png` | UI | 640 × 360 px | PNG | Yes (Alpha) | Wide 16:9 full-screen terminal perimeter frame with ultra-clean viewport openi |
| `ui_terminal_tab_states` | `ui_terminal_tab_states.png` | UI | 512 × 40 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`128×40` per frame) |
| `ui_terminal_prompt_prefix` | `ui_terminal_prompt_prefix.png` | UI | 64 × 32 px | PNG | Yes (Alpha) | Clean glowing cyan-teal `>_` industrial shell prompt prefix graphic sized for  |
| `ui_terminal_cursor_blink` | `ui_terminal_cursor_blink.png` | UI | 64 × 32 px | PNG | Yes (Alpha) | 2-frame horizontal sheet (`32×32` per frame) |
| `ui_terminal_status_strip` | `ui_terminal_status_strip.png` | UI | 512 × 32 px | PNG | Yes (Alpha) | Slim horizontal dark-slate terminal footer status bar with recessed metallic d |
| `ui_terminal_command_input` | `ui_terminal_command_input.png` | UI | 512 × 48 px | PNG | Yes (Alpha) | Front-facing recessed single-line CLI command input field with chamfered dark  |
| `ui_terminal_output_panel` | `ui_terminal_output_panel.png` | UI | 512 × 256 px | PNG | Yes (Alpha) | Clean scrollable terminal output container framed in dark graphite and brushed |
| `ui_terminal_scrollbar` | `ui_terminal_scrollbar.png` | UI | 20 × 192 px | PNG | Yes (Alpha) | Narrow industrial scrollbar track and brushed-steel cyan-accented thumb tailor |
| `ui_terminal_lock_overlay` | `ui_terminal_lock_overlay.png` | UI | 512 × 288 px | PNG | Yes (Alpha) | Translucent security lock overlay frame with dark frosted aperture, brushed st |
| `ui_code_editor_frame` | `ui_code_editor_frame.png` | UI | 512 × 288 px | PNG | Yes (Alpha) | Front-facing IDE code editor chassis with dark graphite bezel, clean blank edi |
| `ui_code_editor_tab_states` | `ui_code_editor_tab_states.png` | UI | 512 × 40 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`128×40` per frame) |
| `ui_code_line_gutter` | `ui_code_line_gutter.png` | UI | 48 × 256 px | PNG | Yes (Alpha) | Vertical dark-slate IDE gutter rail with subtle right border separator and rec |
| `ui_code_line_highlight` | `ui_code_line_highlight.png` | UI | 384 × 28 px | PNG | Yes (Alpha) | Translucent horizontal cyan-tinted active cursor line highlight strip with cri |
| `ui_code_selection` | `ui_code_selection.png` | UI | 256 × 32 px | PNG | Yes (Alpha) | Tileable translucent cyan-teal selection block overlay for highlighting select |
| `ui_code_cursor` | `ui_code_cursor.png` | UI | 16 × 32 px | PNG | Yes (Alpha) | High-contrast luminous cyan vertical I-beam text caret cursor with subtle glow |
| `ui_code_breakpoint` | `ui_code_breakpoint.png` | UI | 32 × 32 px | PNG | Yes (Alpha) | Industrial crimson-amber octagonal debugger breakpoint marker with crisp metal |
| `ui_code_run_controls` | `ui_code_run_controls.png` | UI | 256 × 48 px | PNG | Yes (Alpha) | Compact horizontal IDE control cluster containing Run (emerald play), Pause (a |
| `ui_code_console_frame` | `ui_code_console_frame.png` | UI | 512 × 192 px | PNG | Yes (Alpha) | Wide bottom-docked IDE execution output console chassis with brushed steel hea |
| `ui_code_error_panel` | `ui_code_error_panel.png` | UI | 384 × 160 px | PNG | Yes (Alpha) | Chamfered dark-slate IDE error alert frame featuring muted crimson-amber left  |
| `ui_code_syntax_legend` | `ui_code_syntax_legend.png` | UI | 256 × 128 px | PNG | Yes (Alpha) | Clean text-free IDE color swatch strip displaying Cyber Protocol syntax token  |
| `ui_icons_system_atlas` | `ui_icons_system_atlas.png` | UI | 256 × 256 px | PNG | Yes (Alpha) | 4×4 grid icon atlas (`64×64` px per cell, 16 icons) |
| `ui_icons_command_atlas` | `ui_icons_command_atlas.png` | UI | 256 × 256 px | PNG | Yes (Alpha) | 4×4 grid icon atlas (`64×64` px per cell, 16 icons) |
| `ui_icons_navigation_atlas` | `ui_icons_navigation_atlas.png` | UI | 256 × 256 px | PNG | Yes (Alpha) | 4×4 grid icon atlas (`64×64` px per cell, 16 icons) |
| `ui_icons_resource_atlas` | `ui_icons_resource_atlas.png` | UI | 256 × 256 px | PNG | Yes (Alpha) | 4×4 grid icon atlas (`64×64` px per cell, 16 icons) |
| `ui_icons_status_atlas` | `ui_icons_status_atlas.png` | UI | 256 × 256 px | PNG | Yes (Alpha) | 4×4 grid icon atlas (`64×64` px per cell, 16 icons) |
| `anim_loading_spinner` | `anim_loading_spinner.png` | Effects | 384 × 64 px | PNG | Yes (Alpha) | 6-frame horizontal sheet (`64×64` per frame) |
| `anim_loading_scan` | `anim_loading_scan.png` | Effects | 512 × 128 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`128×128` per frame) |
| `anim_loading_hex_core` | `anim_loading_hex_core.png` | Effects | 512 × 128 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`128×128` per frame) |
| `anim_loading_robot_diagnostic` | `anim_loading_robot_diagnostic.png` | Effects | 1024 × 256 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`256×256` per frame) |
| `fx_loading_data_stream` | `fx_loading_data_stream.png` | Effects | 256 × 152 px | PNG | Yes (Alpha) | Layered translucent cyan-teal and pale blue telemetry data packet ribbons for  |
| `ui_loading_progress` | `ui_loading_progress.png` | UI | 384 × 48 px | PNG | Yes (Alpha) | Wide front-facing dark-graphite loading bar housing with brushed steel chamfer |
| `anim_glitch_signal` | `anim_glitch_signal.png` | Effects | 1024 × 256 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`256×256` per frame) |
| `anim_glitch_screen_tear` | `anim_glitch_screen_tear.png` | Effects | 1024 × 192 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`256×192` per frame) |
| `fx_glitch_rgb_split` | `fx_glitch_rgb_split.png` | Effects | 384 × 216 px | PNG | Yes (Alpha) | Crisp translucent cyan/crimson/white chromatic aberration shear bars designed  |
| `fx_glitch_scanlines` | `fx_glitch_scanlines.png` | Effects | 256 × 256 px | PNG | Yes (Alpha) | Tileable translucent 256×256 subtle cyan-tinted horizontal scanline texture fo |
| `fx_glitch_static_noise` | `fx_glitch_static_noise.png` | Effects | 256 × 256 px | PNG | Yes (Alpha) | Tileable translucent 256×256 high-frequency cyan, red, and slate static noise  |
| `fx_glitch_pixel_corruption` | `fx_glitch_pixel_corruption.png` | Effects | 256 × 256 px | PNG | Yes (Alpha) | Sharp rectangular cyan, crimson, and dark-graphite corrupted pixel block clust |
| `fx_glitch_data_fragments` | `fx_glitch_data_fragments.png` | Effects | 320 × 192 px | PNG | Yes (Alpha) | Scattered horizontal cyan, amber, and coral packet fragments representing corr |
| `fx_glitch_warning_overlay` | `fx_glitch_warning_overlay.png` | Effects | 512 × 288 px | PNG | Yes (Alpha) | Symmetrical crimson-amber and dark-graphite glitch warning perimeter overlay w |
| `fx_glitch_signal_loss` | `fx_glitch_signal_loss.png` | Effects | 512 × 288 px | PNG | Yes (Alpha) | Crisp broken-signal interference burst combining diagonal fractured scan strea |
| `fx_glitch_boot_distortion` | `fx_glitch_boot_distortion.png` | Effects | 384 × 216 px | PNG | Yes (Alpha) | Horizontal displaced cyan/crimson boot-up distortion waves for cold-start term |
| `ui_notification_states` | `ui_notification_states.png` | UI | 1280 × 80 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`320×80` per frame) |
| `ui_tooltip_variants` | `ui_tooltip_variants.png` | UI | 1024 × 128 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`256×128` per frame) |
| `ui_dialogue_box` | `ui_dialogue_box.png` | UI | 512 × 192 px | PNG | Yes (Alpha) | Wide front-facing dark-graphite narrative dialogue panel featuring a left cham |
| `ui_choice_button_states` | `ui_choice_button_states.png` | UI | 1024 × 64 px | PNG | Yes (Alpha) | 4-frame horizontal sheet (`256×64` per frame) |
| `ui_objective_tracker` | `ui_objective_tracker.png` | UI | 256 × 320 px | PNG | Yes (Alpha) | Vertical dark-slate HUD mission checklist frame featuring a header bar, brushe |
| `ui_task_complete_stamp` | `ui_task_complete_stamp.png` | UI | 192 × 192 px | PNG | Yes (Alpha) | Hexagonal industrial emerald-cyan verification badge featuring crisp checkmark |
| `ui_warning_overlay` | `ui_warning_overlay.png` | UI | 512 × 288 px | PNG | Yes (Alpha) | Symmetrical front-facing perimeter alert vignette with amber-crimson caution c |
| `ui_pause_overlay` | `ui_pause_overlay.png` | UI | 384 × 288 px | PNG | Yes (Alpha) | Centered industrial pause modal housing in dark graphite and brushed steel wit |

---

## Integration Notes

- **2:1 Isometric Alignment:** Floor tiles (`256 × 152` px, preserving chamfered slab thickness beneath the 2:1 diamond deck) are exported at 2× supersampled resolution and scale by `0.5×` to the logical `128 × 64` grid unit.
- **Sprite Sheet Slicing:** Load every 4-frame horizontal sheet (`1024 × 256` px under `assets/characters/robots/anim_robot_*.png`, `robot_courier_move.png`, and `assets/machines/anim_*.png`) with `frameWidth = 256`, `frameHeight = 256`, `columns = 4`, `rows = 1`.
- **Machine-Readable Manifest:** Parse `metadata/asset_manifest.json` directly in your game asset loader.
- **Front-Facing Orthographic UI:** UI panels, controls, terminal windows, Python code editor modules, and gameplay feedback elements use crisp front-facing orthographic framing with clean alpha channels and zero baked placeholder text.
- **Icon Atlases (`4×4` Grid):** Slice `ui_icons_*_atlas.png` (`256 × 256` px) using `cellWidth = 64`, `cellHeight = 64`, `columns = 4`, `rows = 4` (`16` icons per atlas).
- **UI State & Loading Sheets:** Horizontal UI button/tab/control state sheets pack 4 equal frames left-to-right (`Default`, `Hover`, `Active/Pressed`, `Disabled`). `ui_terminal_cursor_blink` uses `2` frames (`32 × 32`) and `anim_loading_spinner` uses `6` frames (`64 × 64`).
