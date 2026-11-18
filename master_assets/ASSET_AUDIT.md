# Audit Aset Cyber Protocol (195 file)

Diperiksa otomatis dari `asset_manifest.json` dan isi gambar. Semua angka di bawah terukur, bukan perkiraan.

## Ringkasan
| Hal | Hasil |
|---|---|
| Total aset | 195 (dulu 22.0 MB PNG, sekarang 9.8 MB WebP, 45%) |
| Format | 189 RGBA + 6 RGB (semua `bg_*`, 1376x768, memang tanpa alpha) |
| Transparansi palsu (latar baked) | Tidak ditemukan |
| Ukuran manifest vs file | Cocok semua |
| Spritesheet | 41, hampir semuanya hanya 4 frame |

## Masalah yang ditemukan
1. **Bar progres hanya gambar diam dengan isi tetap.**
   `ui_loading_progress` terisi ~59%, `ui_progress_bar` ~46%, `ui_energy_bar` ~64%, `ui_health_bar` ~67%.
   Isi sudah menyatu dengan gambar sehingga tidak bisa digerakkan 0-100%.
   Perlu dipecah: bingkai/track kosong + isi (fill) terpisah, atau 9-slice.
2. **Animasi hanya 4 frame, satu arah.**
   `anim_robot_walk`: 4 frame, 1 arah. Posisi kaki melompat 28 px antar frame (anchor tanah tidak konsisten).
   `anim_robot_idle/turn/damaged` juga bergeser 2-3 px. Jalan isometrik idealnya 8 frame x 4-8 arah.
3. **`anim_conveyor_active` hampir tidak beranimasi** (selisih antar frame 1.16, praktis diam).
4. **28 efek hanya satu gambar diam**, padahal sifatnya dinamis: ledakan, asap, percikan, busur listrik,
   laser, glitch (8), aliran data/energi, spawn/despawn robot, mesin start/stop, dan sebagainya.
5. **Animasi loading terlalu pendek:** spinner 6 frame; scan, hex core, dan robot diagnostik 4 frame.
6. **Ukuran ganjil / bukan kelipatan frame:** `ui_selection_marker` 256x181, `fx_selection_glow` 251x156.
7. **Pergeseran antar frame pada ikon atlas dan tooltip** (anchor tidak seragam, 6-16 px).
8. **Format:** semua PNG. Sudah dikonversi ke WebP (lossless + alpha utuh untuk sprite/UI,
   lossy kualitas 90 untuk 6 latar besar).

## Catatan penting: arah visual
Panduan aset menyebut game ini *Programmable Simulation Sandbox* bergaya *isometrik 2:1*.
Konsep di `GAME_CONCEPT.md` (first-person Adventure RPG, tanpa avatar pemain) berbeda. Pastikan paket ini
memang untuk arah yang dipilih sebelum aset diperbaiki.

## Usulan perbaikan (urut prioritas)
1. Bar progres: buat ulang sebagai track + fill terpisah (bisa digambar lewat kode agar tepat 0-100%).
2. Rapikan anchor kaki pada semua sprite robot (pivot sama di setiap frame), lalu tambah frame
   lewat interpolasi/gambar ulang secara prosedural.
3. Efek (fx_*): ubah jadi spritesheet 8-12 frame, sebagian bisa dibuat prosedural (percikan, asap, glitch).
4. Perbaiki ukuran ganjil ke kelipatan genap/frame.
5. Pasang konvensi: nama, ukuran frame, FPS, dan pivot tercatat di manifest.
