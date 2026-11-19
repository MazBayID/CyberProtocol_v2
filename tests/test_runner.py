import json
import pathlib
import sys
import unittest

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parents[1] / "app/src/main/python"))
import runner


class TutorialRunnerTests(unittest.TestCase):
    def run_tutorial(self, source, state=None):
        return json.loads(runner.run_tutorial_json(
            source, json.dumps(state or {})
        ))

    def test_robot_can_collect_energy_and_complete_room(self):
        source = (
            'robot.move("right")\n'
            'robot.move("right")\n'
            'robot.action()\n'
            'robot.move("right")\n'
            'robot.move("right")\n'
            'robot.action()'
        )
        result = self.run_tutorial(source)
        self.assertEqual("", result["error"], result)
        self.assertTrue(result["complete"])
        self.assertTrue(result["state"]["energy_collected"])
        self.assertTrue(result["state"]["exit_open"])
        self.assertEqual(4, result["state"]["robot_x"])

    def test_large_repeated_string_is_blocked_before_allocation(self):
        source = (
            "s = 'a' * 1000\n"
            "s = s * 1000\n"
            "s = s * 1000"
        )
        result = runner._execute(source)
        self.assertIn("repetition is limited", result["error"])

    def test_augmented_string_repetition_is_bounded(self):
        result = runner._execute("value = 'a'\nvalue *= 1000000")
        self.assertIn("repetition is limited", result["error"].lower())

    def test_augmented_string_growth_uses_cumulative_budget(self):
        source = (
            "value = ''\n"
            "for index in range(5000):\n"
            "    value += 'a'"
        )
        result = runner._execute(source)
        self.assertIn("Memory budget exceeded", result["error"])

    def test_variable_based_string_repetition_is_bounded(self):
        source = (
            "base = 'a' * 1000\n"
            "count = 1000\n"
            "result = base * count"
        )
        result = runner._execute(source)
        self.assertIn("repetition is limited", result["error"].lower())

    def test_collection_repetition_is_bounded(self):
        source = (
            "items = [0] * 1000\n"
            "items = items * 1000"
        )
        result = runner._execute(source)
        self.assertIn("repetition is limited", result["error"].lower())

    def test_repeated_growth_inside_loop_is_bounded(self):
        source = (
            "value = 'a'\n"
            "for index in range(5000):\n"
            "    value = value * 2"
        )
        result = runner._execute(source)
        self.assertIn("repetition is limited", result["error"].lower())

    def test_cumulative_string_allocation_budget_is_enforced(self):
        source = (
            "value = ''\n"
            "for index in range(5000):\n"
            "    value = value + 'a'"
        )
        result = runner._execute(source)
        self.assertIn("Memory budget exceeded", result["error"])

    def test_generic_tutorial1_wrapper_preserves_battery_and_objectives(self):
        source = (
            'robot.move("right")\n'
            'robot.move("right")\n'
            'robot.action()\n'
            'robot.move("right")\n'
            'robot.move("right")\n'
            'robot.action()'
        )
        result = self.run_tutorial(source)
        self.assertEqual("", result["error"], result)
        self.assertTrue(result["complete"])
        self.assertEqual(48, result["state"]["battery"])
        self.assertEqual(["move:1:2", "move:2:2", "collect_energy",
                          "move:3:2", "move:4:2", "open_exit"], result["timeline"])

    def test_generic_tutorial2_wrapper_preserves_sensor_and_checkpoint_flow(self):
        source = (
            "distance = robot.scan_distance()\n"
            "if distance > 30:\n"
            '    robot.move("right")\n'
            "else:\n"
            '    robot.move("down")\n'
            "robot.action()"
        )
        result = self.run_tutorial2(source)
        self.assertEqual("", result["error"], result)
        self.assertTrue(result["complete"])
        self.assertEqual(["scan:18", "move:0:3", "open_checkpoint"], result["timeline"])
        self.assertTrue(result["state"]["sensor_scanned"])
        self.assertTrue(result["state"]["checkpoint_open"])

    def test_generic_tutorial2_clear_scenario_remains_available(self):
        source = (
            "distance = robot.scan_distance()\n"
            "if distance > 30:\n"
            '    robot.move("right")\n'
            "else:\n"
            '    robot.move("down")\n'
            "robot.action()"
        )
        result = self.run_tutorial2(source, {"sensor_distance": 42})
        self.assertEqual("", result["error"], result)
        self.assertTrue(result["complete"])
        self.assertEqual((1, 2), (result["state"]["robot_x"], result["state"]["robot_y"]))
        self.assertEqual(["scan:42", "move:1:2", "open_checkpoint"], result["timeline"])

    def test_generic_level_runner_rejects_unknown_robot_api(self):
        definition = {
            "id": "invalid_api",
            "map": ["..."],
            "robot_start": {"x": 0, "y": 0},
            "robot_api": ["move", "teleport"],
            "resources": {},
            "sensors": [],
            "scenarios": [],
            "objects": [],
            "objectives": [],
        }
        result = json.loads(runner.run_level_json("print(1)", json.dumps(definition), "{}"))
        self.assertIn("robot API definition is invalid", result["error"])

    def test_generic_level_runner_rejects_scenario_reading_outside_sensor_range(self):
        definition = {
            "id": "invalid_sensor",
            "map": ["..."],
            "robot_start": {"x": 0, "y": 0},
            "robot_api": ["move", "action", "scan_distance"],
            "resources": {},
            "sensors": [{"id": "range", "type": "DISTANCE", "maximum_range": 50}],
            "scenarios": [{"id": "bad", "sensor_readings": {"range": 51}}],
            "objects": [],
            "objectives": [],
        }
        result = json.loads(runner.run_level_json("robot.scan_distance()", json.dumps(definition), "{}"))
        self.assertIn("invalid sensor reading", result["error"])

    def test_small_string_operations_still_work(self):
        result = runner._execute("value = 'cyber' + ' protocol'\nprint(value)")
        self.assertEqual("", result["error"])
        self.assertEqual("cyber protocol\n", result["output"])

    def test_tutorial_rejects_unsupported_python(self):
        result = self.run_tutorial("import os")
        self.assertIn("Unsupported syntax", result["error"])

    def test_robot_cannot_leave_room_boundaries(self):
        result = self.run_tutorial('robot.move("left")')
        self.assertIn("boundary", result["error"])

    def test_invalid_direction_is_rejected(self):
        result = self.run_tutorial('robot.move("north")')
        self.assertIn("Unknown direction", result["error"])


    def run_tutorial2(self, source, state=None):
        return json.loads(runner.run_tutorial2_json(
            source, json.dumps(state or {})
        ))

    def test_tutorial2_sensor_condition_selects_safe_detour(self):
        source = (
            "distance = robot.scan_distance()\n"
            "if distance > 30:\n"
            '    robot.move("right")\n'
            "else:\n"
            '    robot.move("down")\n'
            "robot.action()"
        )
        result = self.run_tutorial2(source)
        self.assertEqual("", result["error"], result)
        self.assertTrue(result["complete"])
        self.assertEqual(18, result["state"]["sensor_distance"])
        self.assertTrue(result["state"]["sensor_scanned"])
        self.assertEqual((0, 3), (result["state"]["robot_x"], result["state"]["robot_y"]))
        self.assertEqual(["scan:18", "move:0:3", "open_checkpoint"], result["timeline"])

    def test_tutorial2_clear_sensor_uses_forward_route(self):
        source = (
            "distance = robot.scan_distance()\n"
            "if distance > 30:\n"
            '    robot.move("right")\n'
            "else:\n"
            '    robot.move("down")\n'
            "robot.action()"
        )
        result = self.run_tutorial2(source, {"sensor_distance": 42})
        self.assertEqual("", result["error"], result)
        self.assertTrue(result["complete"])
        self.assertEqual(42, result["state"]["sensor_distance"])
        self.assertEqual((1, 2), (result["state"]["robot_x"], result["state"]["robot_y"]))
        self.assertEqual(["scan:42", "move:1:2", "open_checkpoint"], result["timeline"])

    def test_tutorial2_rejects_blocked_route(self):
        source = (
            "distance = robot.scan_distance()\n"
            'robot.move("right")'
        )
        result = self.run_tutorial2(source)
        self.assertIn("OBSTACLE COLLISION", result["error"])
        self.assertFalse(result["complete"])
        self.assertEqual(0, result["state"]["robot_x"])

    def test_tutorial2_requires_sensor_scan_before_movement(self):
        result = self.run_tutorial2('robot.move("down")')
        self.assertIn("robot.scan_distance()", result["error"])

    def test_tutorial1_does_not_gain_tutorial2_sensor_api(self):
        result = self.run_tutorial('robot.scan_distance()')
        self.assertIn("Only approved robot methods", result["error"])


if __name__ == "__main__":
    unittest.main()
