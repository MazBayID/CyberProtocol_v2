import glob, json, os, sys, unittest

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, os.path.join(ROOT, "app", "src", "main", "python"))
import runner  # noqa: E402

LEVEL_DIR = os.path.join(ROOT, "app", "src", "main", "assets", "levels")
TEXT_KEYS = ("briefing", "hints", "title", "starter")


def runnable(level):
    return json.dumps({k: v for k, v in level.items() if k not in TEXT_KEYS})


def run(source, definition, state=None):
    return json.loads(runner.run_level_json(source, json.dumps(definition), json.dumps(state or {})))


def suite(source, definition):
    return json.loads(runner.run_level_suite_json(source, json.dumps(definition)))


def base(**kw):
    d = {"id": "t", "map": ["....."] * 3, "robot_start": {"x": 0, "y": 1},
         "robot_api": ["move", "action", "scan_distance", "scan_object", "battery", "facing"],
         "resources": {"battery": {"initial": 10, "minimum": 0, "maximum": 30, "move_cost": 1}},
         "sensors": [{"id": "r", "type": "DISTANCE", "source": "map", "maximum_range": 100, "cell_size_cm": 10}],
         "scenarios": [], "objects": [], "objectives": [{"id": "done", "completion_event": "done"}]}
    d.update(kw)
    return d


class EngineV2Tests(unittest.TestCase):
    def test_map_sensor_reports_distance_from_the_map(self):
        d = base(map=[".....", "..#..", "....."])
        r = run('x = robot.scan_distance("right")\nprint(x)\n', d, {"robot_x": 0, "robot_y": 1})
        self.assertEqual(r["output"].strip(), "20")  # one free cell, then the wall
        r = run('print(robot.scan_distance("down"))\n', d)
        self.assertEqual(r["output"].strip(), "20")  # one free cell below, then the edge
        r = run('robot.move("right")\nprint(robot.scan_distance())\n', d)  # facing right after the move
        self.assertEqual(r["output"].strip(), "10")  # wall directly ahead

    def test_sensor_distance_is_capped_by_maximum_range(self):
        d = base(map=["." * 20] * 3, sensors=[{"id": "r", "type": "DISTANCE", "source": "map", "maximum_range": 50, "cell_size_cm": 10}])
        self.assertEqual(run('print(robot.scan_distance("right"))', d)["output"].strip(), "50")

    def test_scan_object_names_what_is_next_to_the_robot(self):
        d = base(map=["#.H..", ".....", "..D.."], objects=[
            {"id": "c", "type": "ENERGY_CELL", "position": {"x": 1, "y": 1}, "completes_objective": "done"}])
        src = 'print(robot.scan_object("up"))\nprint(robot.scan_object("right"))\nprint(robot.scan_object("left"))\n'
        self.assertEqual(run(src, d, {"robot_x": 0, "robot_y": 1})["output"].split(), ["wall", "cell", "edge"])
        self.assertEqual(run('print(robot.scan_object("down"))', d, {"robot_x": 2, "robot_y": 1})["output"].strip(), "door")
        self.assertEqual(run('print(robot.scan_object("up"))', d, {"robot_x": 2, "robot_y": 1})["output"].strip(), "hazard")

    def test_hazard_destroys_the_run(self):
        d = base(map=[".H...", ".....", "....."])
        r = run('robot.move("up")\nrobot.move("right")\n', d, {"robot_x": 0, "robot_y": 1})
        self.assertTrue(r["failed"])
        self.assertEqual(r["failure"], "hazard")
        self.assertIn("fail:hazard", r["timeline"])
        self.assertFalse(r["complete"])

    def test_battery_depletion_is_a_failure(self):
        d = base(resources={"battery": {"initial": 2, "minimum": 0, "maximum": 30, "move_cost": 1}})
        r = run('robot.move("right")\nrobot.move("right")\nrobot.move("right")\n', d)
        self.assertTrue(r["failed"])
        self.assertEqual(r["failure"], "battery_depleted")
        self.assertEqual(r["state"]["robot_x"], 2)

    def test_closed_door_blocks_until_switch_is_used(self):
        d = base(map=["..D.."], robot_start={"x": 0, "y": 0}, objects=[
            {"id": "s", "type": "SWITCH", "position": {"x": 1, "y": 0}, "opens_doors": True, "completes_objective": "done"}],
            objectives=[{"id": "done", "completion_event": "done"}, {"id": "later", "completion_event": "later"}])
        blocked = run('robot.move("right")\nrobot.move("right")\n', d)
        self.assertIn("DOOR CLOSED", blocked["error"])
        ok = run('robot.move("right")\nrobot.action()\nrobot.move("right")\nrobot.move("right")\n', d)
        self.assertEqual(ok["error"], "")
        self.assertTrue(ok["state"]["doors_open"])
        self.assertIn("door_open", ok["timeline"])

    def test_single_use_charger_cannot_be_farmed(self):
        d = base(resources={"battery": {"initial": 3, "minimum": 0, "maximum": 30, "move_cost": 1}}, objects=[
            {"id": "c", "type": "CHARGER", "position": {"x": 0, "y": 1}, "single_use": True,
             "resource_changes": {"battery": 5}}])
        r = run('robot.action()\nrobot.action()\n', d)
        self.assertIn("Nothing is left", r["error"])
        self.assertEqual(r["state"]["resources"]["battery"], 8)

    def test_facing_and_battery_queries(self):
        d = base()
        r = run('robot.move("down")\nprint(robot.facing())\nprint(robot.battery())\n', d)
        self.assertEqual(r["output"].split(), ["down", "9"])

    def test_python_features_are_gated_per_level(self):
        d = base(python_features=["conditions"])
        self.assertIn("while loops", run('while True:\n    pass\n', d)["error"])
        self.assertIn("for loops", run('for i in range(2):\n    robot.move("right")\n', d)["error"])
        self.assertIn("functions", run('def f():\n    pass\n', d)["error"])
        self.assertEqual(run('if robot.battery() > 1:\n    robot.move("right")\n', d)["error"], "")

    def test_levels_without_feature_list_keep_legacy_rules(self):
        d = base()
        self.assertIn("Unsupported syntax", run('while True:\n    pass\n', d)["error"])

    def test_while_loop_is_bounded_by_the_execution_budget(self):
        d = base(python_features=["while"])
        r = run('while True:\n    pass\n', d)
        self.assertIn("budget", r["error"].lower())

    def test_memory_guards_still_apply_in_level_mode(self):
        d = base(python_features=["for", "lists"])
        r = run("s = 'a' * 1000\ns = s * 1000\ns = s * 1000\n", d)
        self.assertNotEqual(r["error"], "")

    def test_unknown_python_feature_is_rejected(self):
        self.assertIn("feature", run('pass', base(python_features=["eval"]))["error"].lower())

    def test_stars_use_lines_and_moves(self):
        d = base(map=["....."] * 1, robot_start={"x": 0, "y": 0}, par={"lines": 3, "moves": 4},
                 objects=[{"id": "e", "type": "EXIT", "position": {"x": 4, "y": 0}, "completes_objective": "done"}])
        three = suite('for i in range(4):\n    robot.move("right")\nrobot.action()\n', dict(d, python_features=["for"]))
        self.assertEqual(three["stars"], 3)
        long = suite('robot.move("right")\n' * 4 + 'robot.action()\n', d)
        self.assertEqual(long["stars"], 2)  # moves are within par, lines are not
        self.assertEqual(suite('robot.move("right")\n', d)["stars"], 0)

    def test_suite_requires_every_scenario(self):
        d = base(map=["....."] * 1, robot_start={"x": 0, "y": 0}, scenarios=[
            {"id": "a", "obstacle_positions": []}, {"id": "b", "obstacle_positions": [{"x": 2, "y": 0}]}],
            objects=[{"id": "e", "type": "EXIT", "position": {"x": 4, "y": 0}, "completes_objective": "done"}])
        r = suite('for i in range(4):\n    robot.move("right")\nrobot.action()\n'.replace("for i in range(4):\n    ", "") , d)
        self.assertFalse(r["complete"])
        r = suite('robot.move("right")\nrobot.move("right")\nrobot.move("right")\nrobot.move("right")\nrobot.action()\n', d)
        self.assertFalse(r["complete"])
        self.assertEqual(r["focus"], 1)  # the failing scenario is the one shown
        self.assertEqual([s["complete"] for s in r["scenarios"]], [True, False])

    def test_scenario_map_must_match_level_size(self):
        d = base(scenarios=[{"id": "a", "map": ["..."] * 3}])
        self.assertIn("Scenario map", run("pass", d)["error"])

    def test_legacy_scan_distance_rejects_direction(self):
        d = base(sensors=[{"id": "r", "type": "DISTANCE", "maximum_range": 100}], scenarios=[
            {"id": "s", "sensor_readings": {"r": 50}}])
        self.assertIn("straight ahead", run('robot.scan_distance("left")', d)["error"])
        self.assertEqual(run('print(robot.scan_distance())', d)["output"].strip(), "50")


class ChapterOneLevelTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.levels = {}
        for path in sorted(glob.glob(os.path.join(LEVEL_DIR, "c1_*.json"))):
            with open(path, encoding="utf-8") as f:
                level = json.load(f)
            cls.levels[level["id"]] = level
        with open(os.path.join(ROOT, "tests", "level_solutions_v2.json"), encoding="utf-8") as f:
            cls.solutions = json.load(f)
        with open(os.path.join(LEVEL_DIR, "index.json"), encoding="utf-8") as f:
            cls.index = json.load(f)

    def test_chapter_has_ten_ordered_levels(self):
        self.assertEqual(len(self.levels), 10)
        self.assertEqual([item["id"] for item in self.index["levels"]], sorted(self.levels))
        self.assertEqual([item["order"] for item in self.index["levels"]], list(range(1, 11)))

    def test_every_level_is_valid_and_small_enough_for_the_runner(self):
        for level_id, level in self.levels.items():
            with self.subTest(level=level_id):
                payload = runnable(level)
                self.assertLess(len(payload), 50000)
                result = json.loads(runner.run_level_suite_json("pass\n", payload))
                self.assertNotIn("Invalid", result["error"])
                self.assertIn(result["error"], ("",))

    def test_reference_solutions_earn_three_stars(self):
        for level_id, level in self.levels.items():
            with self.subTest(level=level_id):
                result = json.loads(runner.run_level_suite_json(self.solutions[level_id]["solution"], runnable(level)))
                self.assertTrue(result["complete"], result["scenarios"])
                self.assertEqual(result["stars"], 3)

    def test_naive_programs_do_not_pass(self):
        for level_id, level in self.levels.items():
            for program in self.solutions[level_id]["naive"]:
                with self.subTest(level=level_id):
                    result = json.loads(runner.run_level_suite_json(program, runnable(level)))
                    self.assertFalse(result["complete"])

    def test_starter_code_never_solves_the_level_and_never_crashes(self):
        for level_id, level in self.levels.items():
            with self.subTest(level=level_id):
                result = json.loads(runner.run_level_suite_json(level["starter"], runnable(level)))
                self.assertFalse(result["complete"])
                self.assertEqual(result["error"], "")

    def test_scenario_levels_reject_one_route_programs(self):
        for level_id, level in self.levels.items():
            if len(level["scenarios"]) < 2:
                continue
            with self.subTest(level=level_id):
                result = json.loads(runner.run_level_suite_json(self.solutions[level_id]["naive"][0], runnable(level)))
                self.assertLess(sum(1 for s in result["scenarios"] if s["complete"]), len(result["scenarios"]))

    def test_levels_are_fully_bilingual(self):
        for level_id, level in self.levels.items():
            with self.subTest(level=level_id):
                for key in ("title", "briefing"):
                    self.assertTrue(level[key]["en"].strip() and level[key]["id"].strip())
                self.assertEqual(len(level["hints"]["en"]), 3)
                self.assertEqual(len(level["hints"]["id"]), 3)

    def test_language_features_are_introduced_gradually(self):
        seen = set()
        for item in self.index["levels"]:
            level = self.levels[item["id"]]
            new = set(level["python_features"]) - seen
            self.assertLessEqual(len(new), 2, level["id"])
            seen |= set(level["python_features"])


if __name__ == "__main__":
    unittest.main()
