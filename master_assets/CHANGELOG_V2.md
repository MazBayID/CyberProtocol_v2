# Cyber Protocol Assets v2 - Perbaikan Animasi

Semua hal di bawah sudah diukur atau diperiksa pada file hasil akhir. Aset lama yang digantikan disimpan di `assets/_deprecated/`.

## Yang diperbaiki
| Aset | Masalah v1 | v2 |
|---|---|---|
| `anim_robot_*` (idle, walk, interact, work, damaged, repair) | 4 frame, 1 arah, kaki melompat hingga 28 px | **8 frame x 4 arah** per animasi, render 3D isometrik 2:1, titik pijak kaki konsisten (selisih 0 px) |
| `anim_robot_scan` | Tidak ada | **Baru:** kepala menyapu + antena (untuk `robot.scan_distance()`) |
| `robot_courier_move` | Sprite hanya ~60 px dalam frame 256 (skala tidak konsisten dengan gambar diamnya) | Digantikan `anim_robot_courier_walk` berskala konsisten |
| `anim_robot_turn` | 4 frame diam | Dihapus. Arah kini berupa 4 baris pada setiap animasi |
| `ui_loading_progress`, `ui_progress_bar`, `ui_energy_bar`, `ui_health_bar` | Isi terkunci di 46-67% | **3 lapis** (`_frame`, `_fill`, `_gloss`) sehingga nilai bisa 0-100% |
| `anim_conveyor_active` | Hampir diam (selisih frame 1,16) | 12 frame loop mulus, 2 orientasi |
| `fx_explosion_small`, `fx_smoke`, `fx_electric_arc`, `fx_electric_spark`, `fx_energy_flow`, `fx_loading_data_stream`, `fx_selection_glow`, `fx_robot_spawn`, `fx_robot_despawn`, `fx_interaction_success`, `fx_interaction_failed`, `fx_glitch_static_noise` | Satu gambar diam | **Spritesheet animasi** 6-12 frame |
| `anim_loading_spinner`, `anim_loading_hex_core` | 4-6 frame | 12 frame, loop mulus |

## Aset baru
`anim_robot_courier_{idle,walk,handoff}`, `anim_robot_maintenance_{idle,walk,repair}`, `anim_robot_drone_{idle,move,repair}` (masing-masing 8 frame x 4 arah), `fx_scan_sweep` (pulsa sensor isometrik), `fx_welding_sparks` (percikan las untuk repair), `robot_shadow` (bayangan kontak lembut), `animation_spec.json` (fps, loop, pivot, arah).

## Cara memakai
**Animasi robot** (`anim_robot_*.webp`): frame 256x256, kolom = frame, baris = arah.
Urutan baris: `SW, SE, NE, NW`. Titik pijak kaki `(128, 206)` pada semua frame. Gambar `robot_shadow` di bawah robot.
FPS dan loop ada di `metadata/animation_spec.json`. Contoh: idle 6 fps loop, walk 12 fps loop, interact 10 fps sekali jalan.

**Bar progres:** gambar `_frame`, lalu `_fill` dipotong dari kiri sebesar `nilai x trackWidth` pada posisi (`trackX`, `trackY`), lalu `_gloss`. Ukuran trek ada di `bar` pada manifest.

**Konveyor:** 2 baris orientasi (SE-NW, SW-NE). Putar urutan frame mundur untuk arah sebaliknya.

## Yang belum dikerjakan
- Mesin lain (`anim_generator_active`, terminal, processor, door, charging, storage) masih 4 frame buatan generator AI.
- Gambar diam `robot_courier_idle`, `robot_maintenance`, `robot_maintenance_drone` masih dipertahankan sebagai ilustrasi/konsep (gaya AI lama).
- Efek glitch lain, `fx_machine_*`, `fx_security_laser`, `fx_resource_*`, `fx_environment_leak` masih gambar diam.
- Gaya robot MX-01 v2 hasil render 3D lebih bersih dan sederhana daripada gambar AI lama. Bila ingin seragam, robot lain sebaiknya dibuat dengan alur render yang sama.
