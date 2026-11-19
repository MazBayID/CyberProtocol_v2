# Cyber Protocol — Asset Master Reference

Dokumen tunggal untuk katalog, aturan integrasi, status migrasi, dan hasil audit aset. Sumber data teknis utama adalah `metadata/asset_manifest.json`; pengaturan animasi berada di `metadata/animation_spec.json`. Jika dokumen ini berbeda dari metadata, metadata terstruktur menjadi acuan dan dokumentasi harus diperbarui.

## 1. Ringkasan koleksi

| Item | Nilai |
|---|---:|
| Aset aktif | **215** |
| Aset arsip `_deprecated` | **27** |
| Total file aset aktif + arsip | **242** |
| Format aktif | WebP |
| Arah visual | Detailed sci-fi isometric simulation |
| Proyeksi | Isometrik 2:1 |
| Unit logis lantai | 128 × 64 px |
| Direktori aset | `assets/` |
| Metadata | `metadata/asset_manifest.json`, `metadata/animation_spec.json` |

`assetCount` hanya menghitung aset aktif. Isi `assets/_deprecated/` tetap disimpan untuk referensi/rollback dan tidak dihitung sebagai aset aktif maupun dimuat sebagai aset produksi.

### Struktur direktori aktual

```text
assets_master/
├── assets/
│   ├── _deprecated/       # arsip aset lama; jangan dipakai sebagai aset aktif
│   ├── characters/robots/
│   ├── effects/
│   ├── environment/
│   ├── machines/
│   ├── resources/
│   └── ui/
│       └── bars/          # frame, fill, gloss untuk bar dinamis
├── metadata/
│   ├── asset_manifest.json
│   └── animation_spec.json
└── ASSETS_MASTER.md
```

## 2. Aturan penggunaan aset

### Koordinat dan skala
- Lantai memakai unit logis 128×64 px (rasio 2:1).
- File lantai 256×152 px menyertakan ketebalan/trim visual; jangan menganggap seluruh tinggi file sebagai bidang pijak. Skala referensi umumnya 0,5× terhadap ukuran sumber.
- Objek dengan tinggi visual (robot, dinding, mesin, dekorasi) harus ditempatkan memakai anchor/pivot yang sesuai, bukan pusat bitmap.
- Untuk robot MX-01 v2, pivot kaki adalah `[128, 206]` pada frame 256×256. Semua arah harus menggunakan transform grid yang sama dengan tile dan objek tabrakan.

### Sprite sheet
- Baca `spriteSheet` pada manifest dan entri terkait di `animation_spec.json`; jangan mengasumsikan semua sheet 4 frame.
- Robot v2 menggunakan 8 kolom frame × 4 baris arah. Urutan baris: `SW, SE, NE, NW`.
- Slice berdasarkan `frameWidth`, `frameHeight`, `columns`, `rows`, dan `frameCount` yang tercatat.
- `asset_manifest.json` adalah katalog lengkap layout spritesheet. FPS, loop, blend, pivot, dan arah tambahan dibaca dari `animation_spec.json` hanya jika aset memiliki spesifikasi khusus di sana.
- Animasi sekali jalan harus berhenti pada frame terakhir; animasi loop harus kembali mulus ke frame awal.

### Bar progres berlapis
Bar dinamis memakai tiga layer: `*_frame`, `*_fill`, dan `*_gloss`.
1. Gambar frame.
2. Gambar fill dengan clipping horizontal sesuai persentase 0–100% dan track rectangle.
3. Gambar gloss di atas fill bila diperlukan.
4. Jangan menggunakan bitmap bar v1 yang sudah diarsipkan.

Ukuran dan posisi track ada di `animation_spec.json` pada bagian `uiCompositions` (`ui_loading_progress`, `ui_progress_bar`, `ui_energy_bar`, dan `ui_health_bar`). Ini adalah komposisi logis, bukan file gambar tunggal. Layer gambar yang dipakai tercantum pada properti `layers`.

### Transparansi dan format
- WebP aktif; pertahankan alpha untuk sprite, UI, objek, dan efek.
- Jangan mengubah aset transparan menjadi latar opaque.
- Latar `bg_*` yang memang penuh boleh tidak memiliki alpha.
- Hindari menyimpan salinan PNG hasil konversi di samping aset aktif tanpa alasan yang terdokumentasi.

## 3. Katalog aset aktif (215)

### Characters (21)

| ID | File | Pixel size | Format | Sprite sheet |
|---|---|---:|---|---|
| `robot_courier_idle` | `assets/characters/robots/robot_courier_idle.webp` | 256×256 | WEBP | — |
| `robot_courier_move` | `assets/characters/robots/robot_courier_move.webp` | 1024×256 | WEBP | 4×1 (4 frames, 256×256; legacy, digantikan `anim_robot_courier_walk`) |
| `robot_maintenance` | `assets/characters/robots/robot_maintenance.webp` | 256×256 | WEBP | — |
| `robot_maintenance_drone` | `assets/characters/robots/robot_maintenance_drone.webp` | 256×256 | WEBP | — |
| `anim_robot_idle` | `assets/characters/robots/anim_robot_idle.webp` | 2048×1024 | WEBP | 8×4 (8 frames, 256×256) |
| `anim_robot_walk` | `assets/characters/robots/anim_robot_walk.webp` | 2048×1024 | WEBP | 8×4 (8 frames, 256×256) |
| `anim_robot_work` | `assets/characters/robots/anim_robot_work.webp` | 2048×1024 | WEBP | 8×4 (8 frames, 256×256) |
| `anim_robot_interact` | `assets/characters/robots/anim_robot_interact.webp` | 2048×1024 | WEBP | 8×4 (8 frames, 256×256) |
| `anim_robot_damaged` | `assets/characters/robots/anim_robot_damaged.webp` | 2048×1024 | WEBP | 8×4 (8 frames, 256×256) |
| `anim_robot_repair` | `assets/characters/robots/anim_robot_repair.webp` | 2048×1024 | WEBP | 8×4 (8 frames, 256×256) |
| `anim_robot_scan` | `assets/characters/robots/anim_robot_scan.webp` | 2048×1024 | WEBP | 8×4 (8 frames, 256×256) |
| `robot_shadow` | `assets/characters/robots/robot_shadow.webp` | 256×128 | WEBP | 1×1 (1 frames, 256×128) |
| `anim_robot_courier_idle` | `assets/characters/robots/anim_robot_courier_idle.webp` | 2048×1024 | WEBP | 8×4 (8 frames, 256×256) |
| `anim_robot_courier_walk` | `assets/characters/robots/anim_robot_courier_walk.webp` | 2048×1024 | WEBP | 8×4 (8 frames, 256×256) |
| `anim_robot_courier_handoff` | `assets/characters/robots/anim_robot_courier_handoff.webp` | 2048×1024 | WEBP | 8×4 (8 frames, 256×256) |
| `anim_robot_maintenance_idle` | `assets/characters/robots/anim_robot_maintenance_idle.webp` | 2048×1024 | WEBP | 8×4 (8 frames, 256×256) |
| `anim_robot_maintenance_walk` | `assets/characters/robots/anim_robot_maintenance_walk.webp` | 2048×1024 | WEBP | 8×4 (8 frames, 256×256) |
| `anim_robot_maintenance_repair` | `assets/characters/robots/anim_robot_maintenance_repair.webp` | 2048×1024 | WEBP | 8×4 (8 frames, 256×256) |
| `anim_robot_drone_idle` | `assets/characters/robots/anim_robot_drone_idle.webp` | 2048×1024 | WEBP | 8×4 (8 frames, 256×256) |
| `anim_robot_drone_move` | `assets/characters/robots/anim_robot_drone_move.webp` | 2048×1024 | WEBP | 8×4 (8 frames, 256×256) |
| `anim_robot_drone_repair` | `assets/characters/robots/anim_robot_drone_repair.webp` | 2048×1024 | WEBP | 8×4 (8 frames, 256×256) |

### Effects (36)

| ID | File | Pixel size | Format | Sprite sheet |
|---|---|---:|---|---|
| `fx_selection_glow` | `assets/effects/fx_selection_glow.webp` | 2048×128 | WEBP | 8×1 (8 frames, 256×128) |
| `fx_electric_spark` | `assets/effects/fx_electric_spark.webp` | 1024×128 | WEBP | 8×1 (8 frames, 128×128) |
| `fx_machine_active` | `assets/effects/fx_machine_active.webp` | 256×152 | WEBP | — |
| `fx_machine_error` | `assets/effects/fx_machine_error.webp` | 256×256 | WEBP | — |
| `fx_data_transfer` | `assets/effects/fx_data_transfer.webp` | 256×152 | WEBP | — |
| `fx_energy_flow` | `assets/effects/fx_energy_flow.webp` | 2048×64 | WEBP | 8×1 (8 frames, 256×64) |
| `fx_resource_collect` | `assets/effects/fx_resource_collect.webp` | 256×256 | WEBP | — |
| `fx_resource_process` | `assets/effects/fx_resource_process.webp` | 256×256 | WEBP | — |
| `fx_interaction_success` | `assets/effects/fx_interaction_success.webp` | 1024×512 | WEBP | 4×2 (8 frames, 256×256) |
| `fx_interaction_failed` | `assets/effects/fx_interaction_failed.webp` | 1024×512 | WEBP | 4×2 (8 frames, 256×256) |
| `fx_robot_spawn` | `assets/effects/fx_robot_spawn.webp` | 1536×512 | WEBP | 6×2 (12 frames, 256×256) |
| `fx_robot_despawn` | `assets/effects/fx_robot_despawn.webp` | 1536×512 | WEBP | 6×2 (12 frames, 256×256) |
| `fx_machine_startup` | `assets/effects/fx_machine_startup.webp` | 256×256 | WEBP | — |
| `fx_machine_shutdown` | `assets/effects/fx_machine_shutdown.webp` | 256×256 | WEBP | — |
| `fx_electric_arc` | `assets/effects/fx_electric_arc.webp` | 2048×128 | WEBP | 8×1 (8 frames, 256×128) |
| `fx_explosion_small` | `assets/effects/fx_explosion_small.webp` | 1536×512 | WEBP | 6×2 (12 frames, 256×256) |
| `fx_smoke` | `assets/effects/fx_smoke.webp` | 1536×512 | WEBP | 6×2 (12 frames, 256×256) |
| `fx_security_laser` | `assets/effects/fx_security_laser.webp` | 256×152 | WEBP | — |
| `fx_environment_leak` | `assets/effects/fx_environment_leak.webp` | 256×152 | WEBP | — |
| `anim_loading_spinner` | `assets/effects/anim_loading_spinner.webp` | 768×256 | WEBP | 6×2 (12 frames, 128×128) |
| `anim_loading_scan` | `assets/effects/anim_loading_scan.webp` | 512×128 | WEBP | 4×1 (4 frames, 128×128) |
| `anim_loading_hex_core` | `assets/effects/anim_loading_hex_core.webp` | 1536×512 | WEBP | 6×2 (12 frames, 256×256) |
| `anim_loading_robot_diagnostic` | `assets/effects/anim_loading_robot_diagnostic.webp` | 1024×256 | WEBP | 4×1 (4 frames, 256×256) |
| `fx_loading_data_stream` | `assets/effects/fx_loading_data_stream.webp` | 1024×256 | WEBP | 8×1 (8 frames, 128×256) |
| `anim_glitch_signal` | `assets/effects/anim_glitch_signal.webp` | 1024×256 | WEBP | 4×1 (4 frames, 256×256) |
| `anim_glitch_screen_tear` | `assets/effects/anim_glitch_screen_tear.webp` | 1024×192 | WEBP | 4×1 (4 frames, 256×192) |
| `fx_glitch_rgb_split` | `assets/effects/fx_glitch_rgb_split.webp` | 384×216 | WEBP | — |
| `fx_glitch_scanlines` | `assets/effects/fx_glitch_scanlines.webp` | 256×256 | WEBP | — |
| `fx_glitch_static_noise` | `assets/effects/fx_glitch_static_noise.webp` | 1536×192 | WEBP | 6×1 (6 frames, 256×192) |
| `fx_glitch_pixel_corruption` | `assets/effects/fx_glitch_pixel_corruption.webp` | 256×256 | WEBP | — |
| `fx_glitch_data_fragments` | `assets/effects/fx_glitch_data_fragments.webp` | 320×192 | WEBP | — |
| `fx_glitch_warning_overlay` | `assets/effects/fx_glitch_warning_overlay.webp` | 512×288 | WEBP | — |
| `fx_glitch_signal_loss` | `assets/effects/fx_glitch_signal_loss.webp` | 512×288 | WEBP | — |
| `fx_glitch_boot_distortion` | `assets/effects/fx_glitch_boot_distortion.webp` | 384×216 | WEBP | — |
| `fx_welding_sparks` | `assets/effects/fx_welding_sparks.webp` | 1024×128 | WEBP | 8×1 (8 frames, 128×128) |
| `fx_scan_sweep` | `assets/effects/fx_scan_sweep.webp` | 1280×256 | WEBP | 5×2 (10 frames, 256×128) |

### Environment (39)

| ID | File | Pixel size | Format | Sprite sheet |
|---|---|---:|---|---|
| `tile_floor_metal` | `assets/environment/tile_floor_metal.webp` | 256×152 | WEBP | — |
| `tile_floor_corner` | `assets/environment/tile_floor_corner.webp` | 256×152 | WEBP | — |
| `tile_wall_straight` | `assets/environment/tile_wall_straight.webp` | 256×256 | WEBP | — |
| `tile_wall_corner` | `assets/environment/tile_wall_corner.webp` | 256×256 | WEBP | — |
| `structure_window` | `assets/environment/structure_window.webp` | 256×256 | WEBP | — |
| `structure_ramp` | `assets/environment/structure_ramp.webp` | 256×256 | WEBP | — |
| `tile_floor_hazard` | `assets/environment/tile_floor_hazard.webp` | 256×152 | WEBP | — |
| `tile_floor_vent` | `assets/environment/tile_floor_vent.webp` | 256×152 | WEBP | — |
| `tile_floor_platform` | `assets/environment/tile_floor_platform.webp` | 256×192 | WEBP | — |
| `structure_door` | `assets/environment/structure_door.webp` | 256×256 | WEBP | — |
| `prop_warning_beacon` | `assets/environment/prop_warning_beacon.webp` | 128×128 | WEBP | — |
| `prop_power_cable` | `assets/environment/prop_power_cable.webp` | 256×152 | WEBP | — |
| `structure_blast_door` | `assets/environment/structure_blast_door.webp` | 256×256 | WEBP | — |
| `structure_elevator` | `assets/environment/structure_elevator.webp` | 256×256 | WEBP | — |
| `prop_data_terminal` | `assets/environment/prop_data_terminal.webp` | 256×256 | WEBP | — |
| `prop_supply_crate` | `assets/environment/prop_supply_crate.webp` | 256×256 | WEBP | — |
| `prop_warning_barrier` | `assets/environment/prop_warning_barrier.webp` | 256×256 | WEBP | — |
| `prop_data_archive` | `assets/environment/prop_data_archive.webp` | 256×256 | WEBP | — |
| `prop_access_key` | `assets/environment/prop_access_key.webp` | 128×128 | WEBP | — |
| `prop_energy_crystal` | `assets/environment/prop_energy_crystal.webp` | 128×128 | WEBP | — |
| `prop_broken_machine` | `assets/environment/prop_broken_machine.webp` | 256×256 | WEBP | — |
| `prop_repair_parts` | `assets/environment/prop_repair_parts.webp` | 128×128 | WEBP | — |
| `prop_mission_beacon` | `assets/environment/prop_mission_beacon.webp` | 256×256 | WEBP | — |
| `tile_floor_grate` | `assets/environment/tile_floor_grate.webp` | 256×152 | WEBP | — |
| `tile_floor_warning` | `assets/environment/tile_floor_warning.webp` | 256×152 | WEBP | — |
| `tile_wall_window` | `assets/environment/tile_wall_window.webp` | 256×256 | WEBP | — |
| `tile_wall_panel` | `assets/environment/tile_wall_panel.webp` | 256×256 | WEBP | — |
| `prop_industrial_pipe` | `assets/environment/prop_industrial_pipe.webp` | 256×152 | WEBP | — |
| `prop_ventilation_unit` | `assets/environment/prop_ventilation_unit.webp` | 256×256 | WEBP | — |
| `prop_industrial_lamp` | `assets/environment/prop_industrial_lamp.webp` | 128×128 | WEBP | — |
| `prop_robot_docking_bay` | `assets/environment/prop_robot_docking_bay.webp` | 256×256 | WEBP | — |
| `prop_hazard_container` | `assets/environment/prop_hazard_container.webp` | 256×256 | WEBP | — |
| `prop_radiation_sign` | `assets/environment/prop_radiation_sign.webp` | 128×128 | WEBP | — |
| `prop_mission_data_drive` | `assets/environment/prop_mission_data_drive.webp` | 128×128 | WEBP | — |
| `prop_security_keycard` | `assets/environment/prop_security_keycard.webp` | 128×128 | WEBP | — |
| `prop_objective_marker` | `assets/environment/prop_objective_marker.webp` | 256×256 | WEBP | — |
| `prop_industrial_workstation` | `assets/environment/prop_industrial_workstation.webp` | 256×256 | WEBP | — |
| `prop_storage_cabinet` | `assets/environment/prop_storage_cabinet.webp` | 256×256 | WEBP | — |
| `prop_industrial_crate_stack` | `assets/environment/prop_industrial_crate_stack.webp` | 256×256 | WEBP | — |

### Machines (24)

| ID | File | Pixel size | Format | Sprite sheet |
|---|---|---:|---|---|
| `machine_terminal` | `assets/machines/machine_terminal.webp` | 256×256 | WEBP | — |
| `machine_generator` | `assets/machines/machine_generator.webp` | 256×256 | WEBP | — |
| `machine_storage` | `assets/machines/machine_storage.webp` | 256×256 | WEBP | — |
| `machine_conveyor` | `assets/machines/machine_conveyor.webp` | 256×256 | WEBP | — |
| `machine_processor` | `assets/machines/machine_processor.webp` | 256×256 | WEBP | — |
| `machine_charging_station` | `assets/machines/machine_charging_station.webp` | 256×256 | WEBP | — |
| `machine_network_router` | `assets/machines/machine_network_router.webp` | 256×256 | WEBP | — |
| `machine_server_rack` | `assets/machines/machine_server_rack.webp` | 256×256 | WEBP | — |
| `machine_control_panel` | `assets/machines/machine_control_panel.webp` | 256×256 | WEBP | — |
| `machine_sensor` | `assets/machines/machine_sensor.webp` | 256×256 | WEBP | — |
| `machine_repair_station` | `assets/machines/machine_repair_station.webp` | 256×256 | WEBP | — |
| `machine_security_camera` | `assets/machines/machine_security_camera.webp` | 128×128 | WEBP | — |
| `anim_generator_active` | `assets/machines/anim_generator_active.webp` | 1024×256 | WEBP | 4×1 (4 frames, 256×256) |
| `anim_conveyor_active` | `assets/machines/anim_conveyor_active.webp` | 3072×512 | WEBP | 12×2 (12 frames, 256×256) |
| `anim_terminal_active` | `assets/machines/anim_terminal_active.webp` | 1024×256 | WEBP | 4×1 (4 frames, 256×256) |
| `anim_processor_active` | `assets/machines/anim_processor_active.webp` | 1024×256 | WEBP | 4×1 (4 frames, 256×256) |
| `anim_door_open` | `assets/machines/anim_door_open.webp` | 1024×256 | WEBP | 4×1 (4 frames, 256×256) |
| `anim_door_close` | `assets/machines/anim_door_close.webp` | 1024×256 | WEBP | 4×1 (4 frames, 256×256) |
| `anim_charging_active` | `assets/machines/anim_charging_active.webp` | 1024×256 | WEBP | 4×1 (4 frames, 256×256) |
| `anim_storage_open` | `assets/machines/anim_storage_open.webp` | 1024×256 | WEBP | 4×1 (4 frames, 256×256) |
| `machine_security_turret` | `assets/machines/machine_security_turret.webp` | 256×256 | WEBP | — |
| `machine_security_barrier` | `assets/machines/machine_security_barrier.webp` | 256×256 | WEBP | — |
| `machine_research_station` | `assets/machines/machine_research_station.webp` | 256×256 | WEBP | — |
| `machine_data_analyzer` | `assets/machines/machine_data_analyzer.webp` | 256×256 | WEBP | — |

### Resources (8)

| ID | File | Pixel size | Format | Sprite sheet |
|---|---|---:|---|---|
| `resource_energy_cell` | `assets/resources/resource_energy_cell.webp` | 128×128 | WEBP | — |
| `resource_data_chip` | `assets/resources/resource_data_chip.webp` | 128×128 | WEBP | — |
| `resource_circuit_board` | `assets/resources/resource_circuit_board.webp` | 128×128 | WEBP | — |
| `resource_metal_ingot` | `assets/resources/resource_metal_ingot.webp` | 128×128 | WEBP | — |
| `resource_coolant` | `assets/resources/resource_coolant.webp` | 128×128 | WEBP | — |
| `resource_research_data` | `assets/resources/resource_research_data.webp` | 128×128 | WEBP | — |
| `resource_robot_parts` | `assets/resources/resource_robot_parts.webp` | 128×128 | WEBP | — |
| `resource_robot_core` | `assets/resources/resource_robot_core.webp` | 128×128 | WEBP | — |

### UI (87)

| ID | File | Pixel size | Format | Sprite sheet |
|---|---|---:|---|---|
| `ui_selection_marker` | `assets/ui/ui_selection_marker.webp` | 256×181 | WEBP | — |
| `bg_main_menu` | `assets/ui/bg_main_menu.webp` | 1376×768 | WEBP | — |
| `bg_gameplay_facility` | `assets/ui/bg_gameplay_facility.webp` | 1376×768 | WEBP | — |
| `bg_server_room` | `assets/ui/bg_server_room.webp` | 1376×768 | WEBP | — |
| `bg_abandoned_facility` | `assets/ui/bg_abandoned_facility.webp` | 1376×768 | WEBP | — |
| `bg_digital_network` | `assets/ui/bg_digital_network.webp` | 1376×768 | WEBP | — |
| `bg_loading_screen` | `assets/ui/bg_loading_screen.webp` | 1376×768 | WEBP | — |
| `ui_inventory_slot` | `assets/ui/ui_inventory_slot.webp` | 128×128 | WEBP | — |
| `ui_interaction_prompt` | `assets/ui/ui_interaction_prompt.webp` | 256×144 | WEBP | — |
| `ui_object_status` | `assets/ui/ui_object_status.webp` | 256×144 | WEBP | — |
| `ui_minimap_frame` | `assets/ui/ui_minimap_frame.webp` | 256×256 | WEBP | — |
| `ui_notification_panel` | `assets/ui/ui_notification_panel.webp` | 384×216 | WEBP | — |
| `ui_terminal_frame` | `assets/ui/ui_terminal_frame.webp` | 512×288 | WEBP | — |
| `ui_resource_counter` | `assets/ui/ui_resource_counter.webp` | 256×144 | WEBP | — |
| `ui_command_panel` | `assets/ui/ui_command_panel.webp` | 384×216 | WEBP | — |
| `ui_tooltip_panel` | `assets/ui/ui_tooltip_panel.webp` | 256×192 | WEBP | — |
| `ui_panel_standard` | `assets/ui/ui_panel_standard.webp` | 384×288 | WEBP | — |
| `ui_panel_compact` | `assets/ui/ui_panel_compact.webp` | 256×192 | WEBP | — |
| `ui_panel_wide` | `assets/ui/ui_panel_wide.webp` | 512×256 | WEBP | — |
| `ui_panel_modal` | `assets/ui/ui_panel_modal.webp` | 448×336 | WEBP | — |
| `ui_panel_sidebar` | `assets/ui/ui_panel_sidebar.webp` | 256×512 | WEBP | — |
| `ui_panel_header` | `assets/ui/ui_panel_header.webp` | 512×128 | WEBP | — |
| `ui_tab_bar` | `assets/ui/ui_tab_bar.webp` | 512×48 | WEBP | — |
| `ui_tab_item_states` | `assets/ui/ui_tab_item_states.webp` | 512×48 | WEBP | 4×1 (4 frames, 128×48) |
| `ui_scrollbar_vertical` | `assets/ui/ui_scrollbar_vertical.webp` | 24×192 | WEBP | — |
| `ui_scrollbar_horizontal` | `assets/ui/ui_scrollbar_horizontal.webp` | 192×24 | WEBP | — |
| `ui_button_primary_states` | `assets/ui/ui_button_primary_states.webp` | 768×64 | WEBP | 4×1 (4 frames, 192×64) |
| `ui_button_secondary_states` | `assets/ui/ui_button_secondary_states.webp` | 768×64 | WEBP | 4×1 (4 frames, 192×64) |
| `ui_button_danger_states` | `assets/ui/ui_button_danger_states.webp` | 768×64 | WEBP | 4×1 (4 frames, 192×64) |
| `ui_icon_button_states` | `assets/ui/ui_icon_button_states.webp` | 256×64 | WEBP | 4×1 (4 frames, 64×64) |
| `ui_toggle_switch_states` | `assets/ui/ui_toggle_switch_states.webp` | 384×48 | WEBP | 4×1 (4 frames, 96×48) |
| `ui_checkbox_states` | `assets/ui/ui_checkbox_states.webp` | 256×64 | WEBP | 4×1 (4 frames, 64×64) |
| `ui_radio_states` | `assets/ui/ui_radio_states.webp` | 256×64 | WEBP | 4×1 (4 frames, 64×64) |
| `ui_slider_track` | `assets/ui/ui_slider_track.webp` | 256×32 | WEBP | — |
| `ui_slider_handle` | `assets/ui/ui_slider_handle.webp` | 48×48 | WEBP | — |
| `ui_dropdown_field` | `assets/ui/ui_dropdown_field.webp` | 256×48 | WEBP | — |
| `ui_dropdown_arrow` | `assets/ui/ui_dropdown_arrow.webp` | 32×32 | WEBP | — |
| `ui_keybind_badge` | `assets/ui/ui_keybind_badge.webp` | 96×48 | WEBP | — |
| `ui_button_focus_ring` | `assets/ui/ui_button_focus_ring.webp` | 208×80 | WEBP | — |
| `ui_terminal_frame_classic` | `assets/ui/ui_terminal_frame_classic.webp` | 512×288 | WEBP | — |
| `ui_terminal_frame_secure` | `assets/ui/ui_terminal_frame_secure.webp` | 512×288 | WEBP | — |
| `ui_terminal_frame_diagnostic` | `assets/ui/ui_terminal_frame_diagnostic.webp` | 512×288 | WEBP | — |
| `ui_terminal_frame_fullscreen` | `assets/ui/ui_terminal_frame_fullscreen.webp` | 640×360 | WEBP | — |
| `ui_terminal_tab_states` | `assets/ui/ui_terminal_tab_states.webp` | 512×40 | WEBP | 4×1 (4 frames, 128×40) |
| `ui_terminal_prompt_prefix` | `assets/ui/ui_terminal_prompt_prefix.webp` | 64×32 | WEBP | — |
| `ui_terminal_cursor_blink` | `assets/ui/ui_terminal_cursor_blink.webp` | 64×32 | WEBP | 2×1 (2 frames, 32×32) |
| `ui_terminal_status_strip` | `assets/ui/ui_terminal_status_strip.webp` | 512×32 | WEBP | — |
| `ui_terminal_command_input` | `assets/ui/ui_terminal_command_input.webp` | 512×48 | WEBP | — |
| `ui_terminal_output_panel` | `assets/ui/ui_terminal_output_panel.webp` | 512×256 | WEBP | — |
| `ui_terminal_scrollbar` | `assets/ui/ui_terminal_scrollbar.webp` | 20×192 | WEBP | — |
| `ui_terminal_lock_overlay` | `assets/ui/ui_terminal_lock_overlay.webp` | 512×288 | WEBP | — |
| `ui_code_editor_frame` | `assets/ui/ui_code_editor_frame.webp` | 512×288 | WEBP | — |
| `ui_code_editor_tab_states` | `assets/ui/ui_code_editor_tab_states.webp` | 512×40 | WEBP | 4×1 (4 frames, 128×40) |
| `ui_code_line_gutter` | `assets/ui/ui_code_line_gutter.webp` | 48×256 | WEBP | — |
| `ui_code_line_highlight` | `assets/ui/ui_code_line_highlight.webp` | 384×28 | WEBP | — |
| `ui_code_selection` | `assets/ui/ui_code_selection.webp` | 256×32 | WEBP | — |
| `ui_code_cursor` | `assets/ui/ui_code_cursor.webp` | 16×32 | WEBP | — |
| `ui_code_breakpoint` | `assets/ui/ui_code_breakpoint.webp` | 32×32 | WEBP | — |
| `ui_code_run_controls` | `assets/ui/ui_code_run_controls.webp` | 256×48 | WEBP | — |
| `ui_code_console_frame` | `assets/ui/ui_code_console_frame.webp` | 512×192 | WEBP | — |
| `ui_code_error_panel` | `assets/ui/ui_code_error_panel.webp` | 384×160 | WEBP | — |
| `ui_code_syntax_legend` | `assets/ui/ui_code_syntax_legend.webp` | 256×128 | WEBP | — |
| `ui_icons_system_atlas` | `assets/ui/ui_icons_system_atlas.webp` | 256×256 | WEBP | 4×4 (16 frames, 64×64) |
| `ui_icons_command_atlas` | `assets/ui/ui_icons_command_atlas.webp` | 256×256 | WEBP | 4×4 (16 frames, 64×64) |
| `ui_icons_navigation_atlas` | `assets/ui/ui_icons_navigation_atlas.webp` | 256×256 | WEBP | 4×4 (16 frames, 64×64) |
| `ui_icons_resource_atlas` | `assets/ui/ui_icons_resource_atlas.webp` | 256×256 | WEBP | 4×4 (16 frames, 64×64) |
| `ui_icons_status_atlas` | `assets/ui/ui_icons_status_atlas.webp` | 256×256 | WEBP | 4×4 (16 frames, 64×64) |
| `ui_notification_states` | `assets/ui/ui_notification_states.webp` | 1280×80 | WEBP | 4×1 (4 frames, 320×80) |
| `ui_tooltip_variants` | `assets/ui/ui_tooltip_variants.webp` | 1024×128 | WEBP | 4×1 (4 frames, 256×128) |
| `ui_dialogue_box` | `assets/ui/ui_dialogue_box.webp` | 512×192 | WEBP | — |
| `ui_choice_button_states` | `assets/ui/ui_choice_button_states.webp` | 1024×64 | WEBP | 4×1 (4 frames, 256×64) |
| `ui_objective_tracker` | `assets/ui/ui_objective_tracker.webp` | 256×320 | WEBP | — |
| `ui_task_complete_stamp` | `assets/ui/ui_task_complete_stamp.webp` | 192×192 | WEBP | — |
| `ui_warning_overlay` | `assets/ui/ui_warning_overlay.webp` | 512×288 | WEBP | — |
| `ui_pause_overlay` | `assets/ui/ui_pause_overlay.webp` | 384×288 | WEBP | — |
| `ui_loading_progress_frame` | `assets/ui/bars/ui_loading_progress_frame.webp` | 512×72 | WEBP | — |
| `ui_loading_progress_fill` | `assets/ui/bars/ui_loading_progress_fill.webp` | 464×40 | WEBP | — |
| `ui_loading_progress_gloss` | `assets/ui/bars/ui_loading_progress_gloss.webp` | 512×72 | WEBP | — |
| `ui_progress_bar_frame` | `assets/ui/bars/ui_progress_bar_frame.webp` | 384×56 | WEBP | — |
| `ui_progress_bar_fill` | `assets/ui/bars/ui_progress_bar_fill.webp` | 344×32 | WEBP | — |
| `ui_progress_bar_gloss` | `assets/ui/bars/ui_progress_bar_gloss.webp` | 384×56 | WEBP | — |
| `ui_energy_bar_frame` | `assets/ui/bars/ui_energy_bar_frame.webp` | 288×48 | WEBP | — |
| `ui_energy_bar_fill` | `assets/ui/bars/ui_energy_bar_fill.webp` | 224×26 | WEBP | — |
| `ui_energy_bar_gloss` | `assets/ui/bars/ui_energy_bar_gloss.webp` | 288×48 | WEBP | — |
| `ui_health_bar_frame` | `assets/ui/bars/ui_health_bar_frame.webp` | 288×48 | WEBP | — |
| `ui_health_bar_fill` | `assets/ui/bars/ui_health_bar_fill.webp` | 224×26 | WEBP | — |
| `ui_health_bar_gloss` | `assets/ui/bars/ui_health_bar_gloss.webp` | 288×48 | WEBP | — |

## 4. Animasi dan perubahan v2

`animation_spec.json` menggunakan tiga kelompok terpisah: `assetSpecs` untuk 34 spesifikasi per aset (termasuk helper render statis seperti `robot_shadow`), `uiCompositions` untuk 4 komposisi bar berlapis, dan `appSequences` untuk 1 sequence milik aplikasi. Jadi jumlahnya bukan 39 animasi aset.

`asset_manifest.json` tetap menjadi katalog lengkap untuk seluruh 215 aset, termasuk 65 spritesheet. Tidak semua spritesheet memerlukan entri di `assetSpecs`: layout dan jumlah frame dasarnya sudah tercatat di manifest; spesifikasi tambahan hanya ditulis ketika ada properti playback/rendering khusus yang perlu menjadi kontrak eksplisit. Jangan menganggap ketiadaan entri sebagai bukti bahwa aset statis atau tidak digunakan.

### Robot MX-01 dan robot lain
- MX-01: `anim_robot_idle`, `anim_robot_walk`, `anim_robot_interact`, `anim_robot_scan`, `anim_robot_work`, `anim_robot_damaged`, dan `anim_robot_repair`.
- Varian courier, maintenance, dan drone memiliki set animasi tersendiri sesuai manifest.
- `robot_courier_move` tetap berada di direktori aktif dan tetap dihitung dalam 215 aset karena file fisiknya masih dipertahankan. Statusnya `legacy`; jangan gunakan untuk integrasi baru. Gunakan `anim_robot_courier_walk` sebagai pengganti.
- Sheet robot yang menggunakan format 8×4 mempunyai 8 frame per arah dan 4 baris arah (`SW/SE/NE/NW`).
- Pivot robot MX-01: `[128, 206]`. Bayangan kontak `robot_shadow` berada di bawah kaki dan tidak boleh menggantikan pivot posisi robot.
- Jangan menyebut animasi scan, sukses, atau gagal sudah terintegrasi ke gameplay hanya karena asetnya tersedia. Integrasi runtime harus diverifikasi di kode aplikasi.

### Efek dan mesin
- Efek dinamis v2 mencakup pemindaian, percikan, aliran energi/data, spawn/despawn, interaksi sukses/gagal, ledakan, asap, dan glitch.
- Animasi mesin/konveyor memiliki orientasi dan jumlah frame sendiri. Gunakan spesifikasi per aset; jangan mengasumsikan satu layout berlaku untuk semua.
- Aset statis tetap statis walaupun namanya diawali `fx_`; cek apakah manifest menyediakan `spriteSheet`.

### Loading
- Animasi kecil dalam library (`anim_loading_spinner`, `anim_loading_scan`, `anim_loading_hex_core`, `anim_loading_robot_diagnostic`) adalah aset terpisah dari sequence pembuka aplikasi.
- Sequence pembuka aplikasi berada di `app/src/main/assets/loading/`, bukan di `assets_master/assets/`. Metadata-nya berada di `appSequences.app_entry_loading_sequence`: frame genap `002, 004, …, 240` (120 frame), 24 FPS.
- Penghapusan frame ganjil adalah perubahan terpisah di PR #19 yang masih Draft; metadata ini mendeskripsikan rancangan sequence dan tidak membuktikan bahwa perubahan tersebut sudah masuk `main`.
- Angka ukuran APK tidak dapat disimpulkan hanya dari ukuran folder sumber. Verifikasi ukuran APK hasil build setelah integrasi.

## 5. Audit kondisi aset

Audit filesystem pada commit dasar dokumen ini menemukan:
- 215 file aktif dan 27 file arsip di `assets_master/assets/`.
- Manifest awal mencatat 220 entri, tetapi 5 entri menunjuk aset yang tidak ada karena sudah diganti oleh versi/layer baru.
- Manifest diperbaiki menjadi 215 entri aktif; kelima entri usang tersebut dikeluarkan dari daftar aset aktif.
- 27 file `_deprecated` tetap dipertahankan. Tidak ada file arsip yang dihapus oleh pembaruan dokumentasi ini.
- Layer bar yang aktif adalah pasangan frame/fill/gloss di `assets/ui/bars/`; bitmap bar lama berada di arsip.
- `robot_courier_move` berstatus `legacy`, bukan arsip: file masih berada di direktori aktif, masuk hitungan 215, dan digantikan untuk integrasi baru oleh `anim_robot_courier_walk`.
- `animation_spec.json` memiliki 34 `assetSpecs`, 4 `uiCompositions`, dan 1 `appSequences`. Manifest memuat 65 spritesheet; 31 di antaranya tidak memiliki spesifikasi playback/rendering tambahan di `assetSpecs`. Layout dan frame dasar tetap tercatat di manifest. Verifikasi file aktual sebelum mengubah renderer.

### Batas audit
Audit ini memeriksa keberadaan file, konsistensi daftar manifest, dan metadata. Audit ini **bukan** bukti bahwa seluruh aset sudah dipakai oleh aplikasi, bahwa animasi tampil benar di perangkat, atau bahwa semua pivot/collision sudah benar dalam gameplay.

## 6. Riwayat perubahan aset

### v2
- Robot diperbarui ke spritesheet multi-frame dan multi-arah dengan pivot yang konsisten.
- Ditambahkan animasi scan dan varian robot courier, maintenance, serta drone.
- Bar progres diperbarui menjadi layer frame/fill/gloss agar nilai dapat berubah 0–100%.
- Conveyor, efek, dan beberapa animasi loading diperbarui dengan jumlah frame lebih banyak.
- Aset versi lama yang digantikan dipindahkan ke `assets/_deprecated/`.

### Status integrasi aplikasi
Ketersediaan file di asset master tidak sama dengan integrasi ke aplikasi. Catat integrasi aktual di dokumentasi implementasi aplikasi/PR dan jangan menandai sebuah aset sebagai terpakai sebelum ada referensi pemakaian runtime atau staging build yang dapat diperiksa.

## 7. Checklist saat menambah atau mengganti aset

- [ ] Tambahkan file ke direktori kategori yang benar.
- [ ] Perbarui `asset_manifest.json` (ID unik, path, ukuran, format, alpha, deskripsi, metadata sheet).
- [ ] Jika animasi, perbarui `animation_spec.json` (FPS, loop, blend, frame size, pivot, arah).
- [ ] Pastikan jumlah frame dan dimensi sheet cocok dengan file.
- [ ] Pastikan aset yang diganti dipindah ke `assets/_deprecated/`, bukan dihapus.
- [ ] Perbarui jumlah aset aktif; jangan menghitung arsip sebagai aset aktif.
- [ ] Audit pemakaian runtime dan ukuran build secara terpisah.
- [ ] Periksa visual dan anchor pada perangkat sebelum menyatakan integrasi selesai.
