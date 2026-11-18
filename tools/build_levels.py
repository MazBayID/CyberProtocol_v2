"""Builds the chapter-1 level files and verifies each one against the runner.
Run from the repository root:  python3 tools/build_levels.py
Every level ships with a reference solution (must earn 3 stars) and naive programs (must fail)."""
import json, os, sys, collections
sys.path.insert(0, "app/src/main/python")
import runner

SENSOR = {"id": "range", "type": "DISTANCE", "source": "map", "maximum_range": 100, "cell_size_cm": 10}
def battery(initial, maximum=30): return {"battery": {"initial": initial, "minimum": 0, "maximum": maximum, "move_cost": 1}}
def pos(x, y): return {"x": x, "y": y}
def exit_obj(x, y, scen=None, needs=None, oid="exit"):
    o = {"id": oid, "type": "EXIT", "position": pos(x, y), "completes_objective": "open_exit", "timeline_event": "open_exit",
         "event_message": "Exit opened. Level completed.", "prerequisite_error": "The exit is offline. Finish the earlier objective first."}
    if scen: o["scenario_ids"] = scen
    if needs: o["required_objectives"] = needs
    return o
OBJ_EXIT = [{"id": "open_exit", "completion_event": "exit_open"}]
def obstacle(oid, x, y, scen): return {"id": oid, "type": "OBSTACLE", "position": pos(x, y), "scenario_ids": [scen], "blocks_movement": True}
def checkpoint(oid, x, y, scen): return {"id": oid, "type": "CHECKPOINT", "position": pos(x, y), "scenario_ids": [scen], "completes_objective": "open_exit",
         "timeline_event": "open_exit", "event_message": "Checkpoint unlocked."}
def rows(n, w, fill="."): return [fill * w for _ in range(n)]
def set_row(m, y, text): m = list(m); m[y] = text; return m
def T(en, id_): return {"en": en, "id": id_}

L = []
# ---- 01 Sequence ----
L.append(dict(id="c1_01_power_up", order=1, concept="sequence",
  title=T("Power Up", "Isi Daya"),
  briefing=T("A wall splits the room. Guide MX-01 around it, collect the energy cell, then open the exit. Commands run top to bottom.",
             "Sebuah dinding membelah ruangan. Arahkan MX-01 mengitarinya, ambil sel energi, lalu buka pintu keluar. Perintah dijalankan dari atas ke bawah."),
  hints=dict(en=["Walls block movement. Find the gap.", "Use robot.action() while standing on the energy cell.", "Order matters: cell first, exit second."],
             id=["Dinding menghalangi gerak. Cari celahnya.", "Gunakan robot.action() saat berdiri di atas sel energi.", "Urutan penting: sel dulu, baru pintu keluar."]),
  starter='# Move MX-01 around the wall to the energy cell.\n# Then reach the exit.\nrobot.move("up")\n',
  python_features=[], map=[ "......", ".#....", ".#....", ".#....", "......"], robot_start=pos(0, 2),
  robot_api=["move", "action"], resources=battery(14), sensors=[], scenarios=[],
  objects=[{"id": "energy_cell", "type": "ENERGY_CELL", "position": pos(3, 2), "single_use": True, "completes_objective": "collect_energy",
            "timeline_event": "collect_energy", "event_message": "Energy cell collected."}, exit_obj(5, 2, needs=["collect_energy"])],
  objectives=[{"id": "collect_energy", "completion_event": "energy_collected"}, OBJ_EXIT[0]],
  solution='robot.move("up")\nrobot.move("up")\nrobot.move("right")\nrobot.move("right")\nrobot.move("right")\nrobot.move("down")\nrobot.move("down")\nrobot.action()\nrobot.move("right")\nrobot.move("right")\nrobot.action()\n',
  naive=['robot.move("right")\nrobot.action()\n']))
# ---- 02 Loops ----
L.append(dict(id="c1_02_long_corridor", order=2, concept="for loop",
  title=T("Long Corridor", "Lorong Panjang"),
  briefing=T("The corridor is long. Typing every step wastes lines. Repeat a command with a for loop to earn more stars.",
             "Lorongnya panjang. Mengetik tiap langkah memboroskan baris. Ulangi perintah dengan for untuk mendapat lebih banyak bintang."),
  hints=dict(en=["for step in range(3): repeats the indented lines 3 times.", "Collect the cell halfway, then keep going.", "Two loops and two actions are enough."],
             id=["for step in range(3): mengulang baris yang menjorok 3 kali.", "Ambil sel di tengah jalan, lalu lanjut.", "Dua loop dan dua action sudah cukup."]),
  starter='# Repeat robot.move("right") with a loop.\nfor step in range(3):\n    robot.move("right")\n',
  python_features=["for"], map=rows(3, 12), robot_start=pos(0, 1), robot_api=["move", "action"], resources=battery(13), sensors=[], scenarios=[],
  objects=[{"id": "energy_cell", "type": "ENERGY_CELL", "position": pos(6, 1), "single_use": True, "completes_objective": "collect_energy",
            "timeline_event": "collect_energy", "event_message": "Energy cell collected."}, exit_obj(11, 1, needs=["collect_energy"])],
  objectives=[{"id": "collect_energy", "completion_event": "energy_collected"}, OBJ_EXIT[0]],
  solution='for step in range(6):\n    robot.move("right")\nrobot.action()\nfor step in range(5):\n    robot.move("right")\nrobot.action()\n',
  naive=['for step in range(3):\n    robot.move("right")\nrobot.action()\n']))
# ---- 03 Sensor + decision ----
L.append(dict(id="c1_03_look_ahead", order=3, concept="sensor + if/else",
  title=T("Look Ahead", "Lihat ke Depan"),
  briefing=T("An obstacle may or may not block the middle of the room. Read the range sensor, then choose the route. Your program must work in BOTH situations.",
             "Sebuah rintangan mungkin menghalangi tengah ruangan. Baca sensor jarak, lalu pilih rute. Programmu harus berhasil di KEDUA situasi."),
  hints=dict(en=["robot.scan_distance(\"right\") returns the distance in cm. 10 means blocked right in front of you.", "Use if distance > 10: ... else: ...", "Test both scenarios. Hard-coding one route fails the other."],
             id=["robot.scan_distance(\"right\") mengembalikan jarak dalam cm. 10 berarti terhalang tepat di depan.", "Gunakan if jarak > 10: ... else: ...", "Uji kedua skenario. Rute yang ditulis kaku akan gagal di skenario lain."]),
  starter='# Step forward, then scan to the right.\nrobot.move("right")\ndistance = robot.scan_distance("right")\n',
  python_features=["conditions"], map=rows(3, 5), robot_start=pos(0, 1), robot_api=["move", "action", "scan_distance"], resources=battery(12), sensors=[SENSOR],
  scenarios=[{"id": "blocked", "obstacle_positions": [pos(2, 1)]}, {"id": "clear", "obstacle_positions": []}],
  objects=[obstacle("front_obstacle", 2, 1, "blocked"), exit_obj(4, 1)], objectives=OBJ_EXIT,
  solution='robot.move("right")\nif robot.scan_distance("right") > 10:\n    robot.move("right")\n    robot.move("right")\n    robot.move("right")\nelse:\n    robot.move("down")\n    robot.move("right")\n    robot.move("right")\n    robot.move("up")\n    robot.move("right")\nrobot.action()\n',
  naive=['robot.move("right")\nrobot.move("right")\nrobot.move("right")\nrobot.move("right")\nrobot.action()\n']))
# ---- 04 Compare ----
L.append(dict(id="c1_04_compare_routes", order=4, concept="variables + comparison",
  title=T("Compare Routes", "Bandingkan Rute"),
  briefing=T("You start in the middle of a corridor. One side is nearly blocked. Scan both sides and walk toward the side with more room.",
             "Kamu mulai di tengah lorong. Salah satu sisi hampir tertutup. Pindai kedua sisi dan berjalanlah ke sisi yang lebih lega."),
  hints=dict(en=["Store each scan in a variable: left = robot.scan_distance(\"left\")", "Compare the two numbers with >", "A variable can hold a direction: direction = \"left\""],
             id=["Simpan tiap pindaian di variabel: kiri = robot.scan_distance(\"left\")", "Bandingkan kedua angka dengan >", "Variabel bisa menyimpan arah: arah = \"left\""]),
  starter='left = robot.scan_distance("left")\nright = robot.scan_distance("right")\n',
  python_features=["conditions", "for"], map=rows(3, 7), robot_start=pos(3, 1), robot_api=["move", "action", "scan_distance"], resources=battery(10), sensors=[SENSOR],
  scenarios=[{"id": "left_short", "obstacle_positions": [pos(1, 1)]}, {"id": "right_short", "obstacle_positions": [pos(5, 1)]}],
  objects=[obstacle("left_block", 1, 1, "left_short"), obstacle("right_block", 5, 1, "right_short"),
           exit_obj(6, 1, ["left_short"], oid="exit_right"), exit_obj(0, 1, ["right_short"], oid="exit_left")], objectives=OBJ_EXIT,
  solution='left = robot.scan_distance("left")\nright = robot.scan_distance("right")\nif left > right:\n    direction = "left"\nelse:\n    direction = "right"\nfor step in range(3):\n    robot.move(direction)\nrobot.action()\n',
  naive=['for step in range(3):\n    robot.move("right")\nrobot.action()\n']))
# ---- 05 While ----
def corridor_scn(k):
    m = rows(3, 12); m = set_row(m, 1, "." * k + "#" + "." * (11 - k)); return m
L.append(dict(id="c1_05_follow_corridor", order=5, concept="while loop",
  title=T("Follow the Corridor", "Susuri Lorong"),
  briefing=T("The corridor length changes every time. Keep moving while the way ahead is free, then use the checkpoint at the end.",
             "Panjang lorong selalu berubah. Terus bergerak selama jalan di depan kosong, lalu gunakan titik periksa di ujung."),
  hints=dict(en=["while condition: repeats as long as the condition is true.", "Scan first, then move: while robot.scan_distance(\"right\") > 10:", "When the loop ends you are at the end. Use robot.action()."],
             id=["while kondisi: mengulang selama kondisinya benar.", "Pindai dulu, baru gerak: while robot.scan_distance(\"right\") > 10:", "Saat loop selesai kamu ada di ujung. Gunakan robot.action()."]),
  starter='# The corridor length changes each run.\n# Use a while loop and the range sensor.\n',
  python_features=["while"], map=rows(3, 12), robot_start=pos(0, 1), robot_api=["move", "action", "scan_distance"], resources=battery(14), sensors=[SENSOR],
  scenarios=[{"id": "short", "map": corridor_scn(5)}, {"id": "medium", "map": corridor_scn(8)}, {"id": "long", "map": corridor_scn(11)}],
  objects=[checkpoint("cp_short", 4, 1, "short"), checkpoint("cp_medium", 7, 1, "medium"), checkpoint("cp_long", 10, 1, "long")], objectives=OBJ_EXIT,
  solution='while robot.scan_distance("right") > 10:\n    robot.move("right")\nrobot.action()\n',
  naive=['robot.move("right")\nrobot.move("right")\nrobot.move("right")\nrobot.move("right")\nrobot.action()\n']))
# ---- 06 Switch + door ----
L.append(dict(id="c1_06_open_the_door", order=6, concept="state and order",
  title=T("Open the Door", "Buka Pintunya"),
  briefing=T("A locked door blocks the exit. A switch in the side room opens it. Plan the order of actions.",
             "Sebuah pintu terkunci menghalangi jalan keluar. Saklar di ruang samping membukanya. Rencanakan urutan aksi."),
  hints=dict(en=["The door tile D blocks you until a switch opens it.", "robot.scan_object(\"right\") tells you what is next to you.", "Press the switch with robot.action(), then walk through the door."],
             id=["Petak pintu D menghalangimu sampai saklar membukanya.", "robot.scan_object(\"right\") memberi tahu apa yang ada di sebelahmu.", "Tekan saklar dengan robot.action(), lalu lewati pintu."]),
  starter='# Try walking right. What blocks you?\nrobot.move("right")\n',
  python_features=["conditions", "for"], map=["...#...", "...D...", "...#..."], robot_start=pos(0, 1),
  robot_api=["move", "action", "scan_object"], resources=battery(10), sensors=[], scenarios=[],
  objects=[{"id": "door_switch", "type": "SWITCH", "position": pos(2, 0), "opens_doors": True, "single_use": True, "completes_objective": "open_door",
            "timeline_event": "open_door", "event_message": "Switch activated. The door is open."}, exit_obj(6, 1, needs=["open_door"])],
  objectives=[{"id": "open_door", "completion_event": "door_open"}, OBJ_EXIT[0]],
  solution='robot.move("right")\nrobot.move("right")\nrobot.move("up")\nrobot.action()\nrobot.move("down")\nfor step in range(4):\n    robot.move("right")\nrobot.action()\n',
  naive=['for step in range(6):\n    robot.move("right")\nrobot.action()\n']))
# ---- 07 Battery ----
L.append(dict(id="c1_07_battery_budget", order=7, concept="resources + planning",
  title=T("Battery Budget", "Anggaran Baterai"),
  briefing=T("The battery cannot reach the exit. A charger sits on the way. Use it at the right moment.",
             "Baterai tidak cukup untuk sampai ke pintu keluar. Ada pengisi daya di jalan. Pakai pada saat yang tepat."),
  hints=dict(en=["Each move costs 1 battery. robot.battery() tells you what is left.", "The charger works once. Use robot.action() on it.", "Count the moves before you start."],
             id=["Tiap langkah memakai 1 baterai. robot.battery() menunjukkan sisanya.", "Pengisi daya hanya bisa dipakai sekali. Gunakan robot.action() di atasnya.", "Hitung langkah sebelum mulai."]),
  starter='print(robot.battery())\n', python_features=["for"], map=rows(3, 9), robot_start=pos(0, 1),
  robot_api=["move", "action", "battery"], resources=battery(5, 20), sensors=[], scenarios=[],
  objects=[{"id": "charger", "type": "CHARGER", "position": pos(4, 1), "single_use": True, "resource_changes": {"battery": 8}, "completes_objective": "recharged",
            "timeline_event": "recharge", "event_message": "Battery recharged by 8."}, exit_obj(8, 1, needs=["recharged"])],
  objectives=[{"id": "recharged", "completion_event": "recharged"}, OBJ_EXIT[0]],
  solution='for step in range(4):\n    robot.move("right")\nrobot.action()\nfor step in range(4):\n    robot.move("right")\nrobot.action()\n',
  naive=['for step in range(8):\n    robot.move("right")\nrobot.action()\n']))
# ---- 08 Hazards ----
def hazard_map(x): return set_row(rows(3, 8), 1, "." * x + "H" + "." * (7 - x))
L.append(dict(id="c1_08_hazard_field", order=8, concept="scan_object + while + if",
  title=T("Hazard Field", "Medan Berbahaya"),
  briefing=T("Damaged floor tiles (H) destroy the robot. Their position changes. Look before every step and walk around them.",
             "Petak lantai rusak (H) menghancurkan robot. Posisinya berubah-ubah. Lihat sebelum tiap langkah dan hindari."),
  hints=dict(en=["robot.scan_object(\"right\") returns \"hazard\", \"edge\", \"exit\", \"empty\" ...", "Detour: up, right, right, down.", "Loop until the scan says \"edge\", then use the exit."],
             id=["robot.scan_object(\"right\") mengembalikan \"hazard\", \"edge\", \"exit\", \"empty\" ...", "Memutar: up, right, right, down.", "Ulangi sampai pindaian berkata \"edge\", lalu gunakan pintu keluar."]),
  starter='# Look before you step.\nahead = robot.scan_object("right")\nprint(ahead)\n',
  python_features=["conditions", "while"], map=rows(3, 8), robot_start=pos(0, 1), robot_api=["move", "action", "scan_object"], resources=battery(16), sensors=[],
  scenarios=[{"id": "hazard_a", "map": hazard_map(3)}, {"id": "hazard_b", "map": hazard_map(5)}],
  objects=[exit_obj(7, 1)], objectives=OBJ_EXIT,
  solution='while robot.scan_object("right") != "edge":\n    if robot.scan_object("right") == "hazard":\n        robot.move("up")\n        robot.move("right")\n        robot.move("right")\n        robot.move("down")\n    else:\n        robot.move("right")\nrobot.action()\n',
  naive=['for step in range(7):\n    robot.move("right")\nrobot.action()\n'.replace("for step in range(7):\n    ", "").replace('robot.move("right")\n', 'robot.move("right")\n' * 7, 1)]))
# ---- 09 Functions ----
L.append(dict(id="c1_09_reusable_routine", order=9, concept="functions",
  title=T("Reusable Routine", "Rutin yang Dipakai Ulang"),
  briefing=T("Three hazards, same detour each time. Write the detour once as a function and call it whenever you need it.",
             "Tiga bahaya, rute memutar yang sama tiap kali. Tulis rute itu sekali sebagai fungsi dan panggil kapan perlu."),
  hints=dict(en=["def dodge(): defines a function. Call it with dodge().", "The body of the function must be indented.", "Fewer lines earn more stars."],
             id=["def dodge(): membuat fungsi. Panggil dengan dodge().", "Isi fungsi harus menjorok.", "Baris lebih sedikit mendapat bintang lebih banyak."]),
  starter='def dodge():\n    robot.move("up")\n', python_features=["conditions", "while", "functions"],
  map=set_row(rows(3, 12), 1, "...H..H..H.."), robot_start=pos(0, 1), robot_api=["move", "action", "scan_object"], resources=battery(22), sensors=[], scenarios=[],
  objects=[exit_obj(11, 1)], objectives=OBJ_EXIT,
  solution='def dodge():\n    robot.move("up")\n    robot.move("right")\n    robot.move("right")\n    robot.move("down")\n\nwhile robot.scan_object("right") != "edge":\n    if robot.scan_object("right") == "hazard":\n        dodge()\n    else:\n        robot.move("right")\nrobot.action()\n',
  naive=['robot.move("right")\nrobot.move("right")\nrobot.move("right")\nrobot.action()\n']))
# ---- 10 Final: planner-generated ----
F_MAP = ["........", "#######.", "..H.....", ".....H..", ".#######", ".....D.."]
F_START, F_SWITCH, F_CHARGER, F_EXIT, F_DOOR_OPEN_NEEDED = (0, 0), (7, 0), (5, 2), (7, 5), True
F_BAT0, F_BATMAX, F_CHARGE = 12, 30, 16
def plan_final():
    D = {"U": (0, -1), "D": (0, 1), "L": (-1, 0), "R": (1, 0)}
    start = (F_START[0], F_START[1], False, False, F_BAT0)
    seen = {start: None}; q = collections.deque([start])
    while q:
        s = q.popleft(); x, y, door, ch, bat = s
        if (x, y) == F_EXIT and door: return reconstruct(seen, s) + ["A"]
        nxt = []
        for k, (dx, dy) in D.items():
            nx, ny = x + dx, y + dy
            if not (0 <= nx < 8 and 0 <= ny < 6) or F_MAP[ny][nx] == "#" or F_MAP[ny][nx] == "H": continue
            if F_MAP[ny][nx] == "D" and not door: continue
            if bat < 1: continue
            nxt.append((k, (nx, ny, door, ch, bat - 1)))
        if (x, y) == F_SWITCH and not door: nxt.append(("A", (x, y, True, ch, bat)))
        if (x, y) == F_CHARGER and not ch: nxt.append(("A", (x, y, door, True, min(F_BATMAX, bat + F_CHARGE))))
        for k, ns in nxt:
            if ns not in seen: seen[ns] = (s, k); q.append(ns)
    raise SystemExit("final level is unsolvable")
def reconstruct(seen, s):
    out = []
    while seen[s]: s, k = seen[s]; out.append(k)
    return out[::-1]
def to_code(plan):
    names = {"U": "up", "D": "down", "L": "left", "R": "right"}; lines = []; i = 0
    while i < len(plan):
        k = plan[i]
        if k == "A": lines.append("robot.action()"); i += 1; continue
        j = i
        while j < len(plan) and plan[j] == k: j += 1
        n = j - i
        if n >= 3: lines += [f"for step in range({n}):", f'    robot.move("{names[k]}")']
        else: lines += [f'robot.move("{names[k]}")'] * n
        i = j
    return "\n".join(lines) + "\n"
FINAL_PLAN = plan_final()
L.append(dict(id="c1_10_facility_run", order=10, concept="everything",
  title=T("Facility Run", "Lari Fasilitas"),
  briefing=T("Final exam of the training wing: collect power, open the door, avoid damaged tiles, and reach the exit with a battery that is too small. Plan the whole route first.",
             "Ujian akhir sayap latihan: ambil daya, buka pintu, hindari petak rusak, dan capai pintu keluar dengan baterai yang terlalu kecil. Rencanakan seluruh rute lebih dulu."),
  hints=dict(en=["Draw the map on paper. Mark H, D, the switch and the charger.", "The charger is on the way. The switch is far from the exit.", "Loops make long straight runs short."],
             id=["Gambar petanya di kertas. Tandai H, D, saklar, dan pengisi daya.", "Pengisi daya ada di jalan. Saklar jauh dari pintu keluar.", "Loop memendekkan lintasan lurus yang panjang."]),
  starter='# Plan the whole route before you write code.\n', python_features=["conditions", "for", "while", "functions", "lists"],
  map=F_MAP, robot_start=pos(*F_START), robot_api=["move", "action", "scan_object", "battery"], resources=battery(F_BAT0, F_BATMAX), sensors=[], scenarios=[],
  objects=[{"id": "door_switch", "type": "SWITCH", "position": pos(*F_SWITCH), "opens_doors": True, "single_use": True, "completes_objective": "open_door",
            "timeline_event": "open_door", "event_message": "Switch activated. The door is open."},
           {"id": "charger", "type": "CHARGER", "position": pos(*F_CHARGER), "single_use": True, "resource_changes": {"battery": F_CHARGE},
            "timeline_event": "recharge", "event_message": f"Battery recharged by {F_CHARGE}."}, exit_obj(*F_EXIT, needs=["open_door"])],
  objectives=[{"id": "open_door", "completion_event": "door_open"}, OBJ_EXIT[0]],
  solution=to_code(FINAL_PLAN), naive=['for step in range(7):\n    robot.move("right")\nrobot.action()\n']))

out_dir = "app/src/main/assets/levels"; os.makedirs(out_dir, exist_ok=True); sol = {}; index = []
for lv in L:
    solution, naive = lv.pop("solution"), lv.pop("naive"); lv["schema"] = 2; lv["chapter"] = 1
    probe = dict(lv); res = json.loads(runner.run_level_suite_json(solution, json.dumps({k: v for k, v in probe.items() if k not in ("briefing", "hints", "title", "starter")})))
    if not res["complete"]: raise SystemExit(f"{lv['id']}: reference solution failed: {res['error']} {res['scenarios']}")
    lv["par"] = {"lines": res["metrics"]["lines"], "moves": res["metrics"]["moves"]}
    final = json.loads(runner.run_level_suite_json(solution, json.dumps({k: v for k, v in lv.items() if k not in ("briefing", "hints", "title", "starter")})))
    assert final["stars"] == 3, (lv["id"], final["stars"])
    for n in naive:
        r = json.loads(runner.run_level_suite_json(n, json.dumps({k: v for k, v in lv.items() if k not in ("briefing", "hints", "title", "starter")})))
        assert not r["complete"], f"{lv['id']}: naive program unexpectedly passed"
    st = json.loads(runner.run_level_suite_json(lv["starter"], json.dumps({k: v for k, v in lv.items() if k not in ("briefing", "hints", "title", "starter")})))
    assert not st["complete"], f"{lv['id']}: starter code already solves the level"
    with open(f"{out_dir}/{lv['id']}.json", "w") as f: json.dump(lv, f, indent=1, ensure_ascii=False)
    sol[lv["id"]] = {"solution": solution, "naive": naive}
    index.append({"id": lv["id"], "order": lv["order"], "concept": lv["concept"], "par": lv["par"], "scenarios": max(1, len(lv["scenarios"]))})
    print(f"{lv['id']:24s} par: {lv['par']['lines']:2d} baris, {lv['par']['moves']:2d} langkah | skenario: {max(1,len(lv['scenarios']))} | fitur: {','.join(lv['python_features']) or '-'}")
json.dump({"chapter": 1, "title": {"en": "Training Wing", "id": "Sayap Latihan"}, "levels": index}, open(f"{out_dir}/index.json", "w"), indent=1, ensure_ascii=False)
os.makedirs("tests", exist_ok=True); json.dump(sol, open("tests/level_solutions_v2.json", "w"), indent=1)
print("rencana level 10:", " ".join(FINAL_PLAN))
