"""Restricted Python teaching runner. Prototype guardrails, not a hardened sandbox."""
import ast
import contextlib
import json
import sys
import time

MAX_SOURCE_LENGTH = 12000
MAX_OUTPUT_LENGTH = 8000
MAX_TRACE_EVENTS = 50000
MAX_EXECUTION_SECONDS = 3.0
MAX_AST_NODES = 2500
MAX_RANGE_ITEMS = 5000
MAX_INTEGER_ABS = 1_000_000
MAX_LITERAL_STRING_LENGTH = 10_000
MAX_REPEAT_COUNT = 1_000
MAX_STRING_LENGTH = 100_000
MAX_COLLECTION_ITEMS = 10_000
MAX_ALLOCATION_BYTES = 512 * 1024
MAX_INTEGER_BITS = 4_096


class LimitedTextWriter:
    """Capture stdout without allowing unbounded output accumulation."""
    def __init__(self, limit):
        self.limit = limit
        self.parts = []
        self.length = 0

    def write(self, value):
        text = str(value)
        remaining = self.limit - self.length
        if remaining > 0:
            accepted = text[:remaining]
            self.parts.append(accepted)
            self.length += len(accepted)
        return len(text)

    def flush(self):
        return None

    def getvalue(self):
        return "".join(self.parts)


def _bounded_range(*args):
    value = range(*args)
    if len(value) > MAX_RANGE_ITEMS:
        raise RunnerError(
            f"Range is limited to {MAX_RANGE_ITEMS} steps in this training environment."
        )
    return value


SAFE_BUILTINS = {
    "print": print, "range": _bounded_range, "len": len, "str": str, "int": int,
    "float": float, "bool": bool, "enumerate": enumerate, "min": min,
    "max": max, "sum": sum, "abs": abs,
}
ALLOWED_NODES = (
    ast.Module, ast.Expr, ast.Call, ast.Name, ast.Load, ast.Store, ast.Constant,
    ast.Assign, ast.BinOp, ast.Add, ast.Sub, ast.Mult, ast.Div, ast.FloorDiv,
    ast.Mod, ast.UnaryOp, ast.USub, ast.UAdd, ast.Compare, ast.Gt, ast.GtE,
    ast.Lt, ast.LtE, ast.Eq, ast.NotEq, ast.In, ast.NotIn, ast.If, ast.For,
    ast.AugAssign, ast.List, ast.Tuple, ast.BoolOp, ast.And, ast.Or, ast.Not,
    ast.Subscript, ast.FunctionDef, ast.arguments, ast.arg, ast.Return, ast.Attribute,
)


BASE_NODES = (
    ast.Module, ast.Expr, ast.Call, ast.Name, ast.Load, ast.Store, ast.Constant,
    ast.Assign, ast.BinOp, ast.Add, ast.Sub, ast.Mult, ast.Div, ast.FloorDiv,
    ast.Mod, ast.UnaryOp, ast.USub, ast.UAdd, ast.Compare, ast.Gt, ast.GtE,
    ast.Lt, ast.LtE, ast.Eq, ast.NotEq, ast.In, ast.NotIn, ast.BoolOp, ast.And,
    ast.Or, ast.Not, ast.Attribute, ast.Pass,
)
FEATURE_NODES = {
    "conditions": (ast.If,),
    "for": (ast.For,),
    "while": (ast.While, ast.Break, ast.Continue),
    "lists": (ast.List, ast.Tuple, ast.Subscript),
    "functions": (ast.FunctionDef, ast.arguments, ast.arg, ast.Return),
    "augassign": (ast.AugAssign,),
}
FEATURE_LABELS = {
    "conditions": "if / else", "for": "for loops", "while": "while loops",
    "lists": "lists", "functions": "functions", "augassign": "+= and similar operators",
}


def _nodes_for_features(features):
    nodes = list(BASE_NODES)
    for feature in features:
        nodes.extend(FEATURE_NODES[feature])
    return tuple(nodes)


def _feature_hint(node, allowed_nodes):
    if allowed_nodes is ALLOWED_NODES:
        return None
    for feature, nodes in FEATURE_NODES.items():
        if isinstance(node, nodes):
            return f"This level does not allow {FEATURE_LABELS[feature]} yet."
    return None


class RunnerError(Exception):
    pass


def _validate(tree, extra_call_names=(), allowed_robot_methods=("move", "action"), allowed_nodes=ALLOWED_NODES):
    nodes = list(ast.walk(tree))
    if len(nodes) > MAX_AST_NODES:
        raise RunnerError("This program is too complex for the current training environment.")

    constant_assignments = {}
    for node in nodes:
        if (isinstance(node, ast.Assign) and len(node.targets) == 1
                and isinstance(node.targets[0], ast.Name)
                and isinstance(node.value, (ast.Constant, ast.List, ast.Tuple))):
            if isinstance(node.value, ast.Constant):
                constant_assignments[node.targets[0].id] = node.value.value
            elif isinstance(node.value, ast.List):
                constant_assignments[node.targets[0].id] = []
            else:
                constant_assignments[node.targets[0].id] = ()

    def known_value(expression):
        if isinstance(expression, ast.Constant):
            return expression.value
        if isinstance(expression, ast.Name):
            return constant_assignments.get(expression.id)
        if isinstance(expression, ast.List):
            return []
        if isinstance(expression, ast.Tuple):
            return ()
        return None

    defined_functions = {node.name for node in nodes if isinstance(node, ast.FunctionDef)}

    for node in nodes:
        if not isinstance(node, allowed_nodes):
            raise RunnerError(_feature_hint(node, allowed_nodes) or f"Unsupported syntax: {type(node).__name__}")
        if isinstance(node, ast.Name) and (node.id.startswith("_") or "__" in node.id):
            raise RunnerError("Names beginning with underscore are not available.")
        if isinstance(node, ast.arg) and (node.arg.startswith("_") or "__" in node.arg):
            raise RunnerError("Names beginning with underscore are not available.")
        if isinstance(node, ast.AugAssign) and not isinstance(node.target, ast.Name):
            raise RunnerError("Augmented assignment is limited to simple variables in this training environment.")
        if isinstance(node, ast.FunctionDef):
            if node.name.startswith("_") or "__" in node.name:
                raise RunnerError("Names beginning with underscore are not available.")
            if node.decorator_list or node.returns is not None or node.args.vararg or node.args.kwarg or node.args.kwonlyargs or node.args.defaults or node.args.kw_defaults:
                raise RunnerError("Function decorators, annotations, variadic and default arguments are not available.")
        if isinstance(node, ast.Attribute):
            if (not isinstance(node.value, ast.Name) or node.value.id != "robot"
                    or node.attr not in allowed_robot_methods):
                methods = ", ".join(f"robot.{name}()" for name in allowed_robot_methods)
                raise RunnerError(f"Unsupported syntax: Attribute (available robot methods: {methods}).")
        if isinstance(node, ast.Call):
            if isinstance(node.func, ast.Name):
                if node.func.id not in SAFE_BUILTINS and node.func.id not in extra_call_names and node.func.id not in defined_functions:
                    raise RunnerError("Only approved built-in functions may be called.")
            elif isinstance(node.func, ast.Attribute):
                if (not isinstance(node.func.value, ast.Name) or node.func.value.id != "robot"
                        or node.func.attr not in allowed_robot_methods):
                    methods = ", ".join(f"robot.{name}()" for name in allowed_robot_methods)
                    raise RunnerError(f"Only approved robot methods are available: {methods}.")
            else:
                raise RunnerError("Only approved function calls are available.")
        if isinstance(node, ast.Constant) and isinstance(node.value, (bytes, complex)):
            raise RunnerError("This value type is not available in the training runner.")
        if isinstance(node, ast.Constant) and isinstance(node.value, str):
            if len(node.value) > MAX_LITERAL_STRING_LENGTH:
                raise RunnerError("Text literals are limited in this training environment.")
        if isinstance(node, ast.Constant) and isinstance(node.value, int):
            if abs(node.value) > MAX_INTEGER_ABS:
                raise RunnerError("Integer constants are limited in this training environment.")
        if isinstance(node, ast.BinOp) and isinstance(node.op, ast.Mult):
            left = known_value(node.left)
            right = known_value(node.right)
            if isinstance(left, (str, list, tuple)) and isinstance(right, int):
                if abs(right) > MAX_REPEAT_COUNT:
                    raise RunnerError("Text and collection repetition is limited to prevent excessive memory use.")
            if isinstance(right, (str, list, tuple)) and isinstance(left, int):
                if abs(left) > MAX_REPEAT_COUNT:
                    raise RunnerError("Text and collection repetition is limited to prevent excessive memory use.")
        if isinstance(node, (ast.List, ast.Tuple)) and len(node.elts) > 1000:
            raise RunnerError("Collection literals are limited to 1000 items.")


class _BoundedOperationTransformer(ast.NodeTransformer):
    """Route potentially allocating + and * operations through runtime guards."""
    def visit_AugAssign(self, node):
        self.generic_visit(node)
        helper = None
        if isinstance(node.op, ast.Add):
            helper = "_bounded_add"
        elif isinstance(node.op, ast.Mult):
            helper = "_bounded_mul"
        if helper is None:
            return node
        current_value = ast.Name(id=node.target.id, ctx=ast.Load())
        guarded_value = ast.Call(
            func=ast.Name(id=helper, ctx=ast.Load()),
            args=[current_value, node.value],
            keywords=[],
        )
        return ast.copy_location(
            ast.Assign(targets=[node.target], value=guarded_value),
            node,
        )

    def visit_BinOp(self, node):
        self.generic_visit(node)
        helper = None
        if isinstance(node.op, ast.Add):
            helper = "_bounded_add"
        elif isinstance(node.op, ast.Mult):
            helper = "_bounded_mul"
        if helper is None:
            return node
        return ast.copy_location(
            ast.Call(
                func=ast.Name(id=helper, ctx=ast.Load()),
                args=[node.left, node.right],
                keywords=[],
            ),
            node,
        )


def _execute(source, variables=None, workspace_files=None, writable_files=None,
             extra_call_names=(), allowed_robot_methods=("move", "action"), allowed_nodes=ALLOWED_NODES):
    if not isinstance(source, str) or not source.strip():
        raise RunnerError("Write some Python code before running.")
    if len(source) > MAX_SOURCE_LENGTH:
        raise RunnerError("Source is too long for this training exercise.")
    try:
        tree = ast.parse(source, mode="exec")
    except SyntaxError as error:
        return {"output": "", "error": f"SyntaxError: {error.msg} (line {error.lineno})"}
    workspace = dict(workspace_files or {})
    writable = set(writable_files or ())

    def read_text(name):
        if not isinstance(name, str) or name not in workspace:
            raise RunnerError("File is not available in this mission workspace.")
        return workspace[name]

    def write_text(name, content):
        if not isinstance(name, str) or name not in writable:
            raise RunnerError("This file is read-only or outside the mission workspace.")
        if not isinstance(content, str) or len(content) > 50000:
            raise RunnerError("Mission files must contain text up to 50000 characters.")
        workspace[name] = content

    safe_builtins = dict(SAFE_BUILTINS)
    safe_builtins.update({"read_text": read_text, "write_text": write_text})
    try:
        _validate(tree, {"read_text", "write_text", *extra_call_names}, allowed_robot_methods, allowed_nodes)
    except RunnerError as error:
        return {"output": "", "error": str(error)}

    allocation_budget = {"remaining": MAX_ALLOCATION_BYTES}

    def consume_allocation(value):
        if isinstance(value, str):
            if len(value) > MAX_STRING_LENGTH:
                raise RunnerError("Generated text is too large for this training environment.")
            amount = len(value) * 4
        elif isinstance(value, (list, tuple)):
            if len(value) > MAX_COLLECTION_ITEMS:
                raise RunnerError("Generated collections are too large for this training environment.")
            amount = len(value) * 8
        else:
            return
        if amount > allocation_budget["remaining"]:
            raise RunnerError("Memory budget exceeded. Reduce the size of generated text or collections.")
        allocation_budget["remaining"] -= amount

    def bounded_add(left, right):
        if type(left) is type(right) and isinstance(left, (str, list, tuple)):
            size = len(left) + len(right)
            limit = MAX_STRING_LENGTH if isinstance(left, str) else MAX_COLLECTION_ITEMS
            if size > limit:
                raise RunnerError("Generated text or collection is too large for this training environment.")
        result = left + right
        consume_allocation(result)
        return result

    def bounded_mul(left, right):
        sequence, count = None, None
        if isinstance(left, (str, list, tuple)) and isinstance(right, int):
            sequence, count = left, right
        elif isinstance(right, (str, list, tuple)) and isinstance(left, int):
            sequence, count = right, left
        if sequence is not None:
            result_size = len(sequence) * max(0, count)
            limit = MAX_STRING_LENGTH if isinstance(sequence, str) else MAX_COLLECTION_ITEMS
            if result_size > limit:
                raise RunnerError("Text and collection repetition is limited to prevent excessive memory use.")
        result = left * right
        if isinstance(result, int) and result.bit_length() > MAX_INTEGER_BITS:
            raise RunnerError("Integer result is too large for this training environment.")
        consume_allocation(result)
        return result

    tree = _BoundedOperationTransformer().visit(tree)
    ast.fix_missing_locations(tree)
    namespace = dict(variables or {})
    output = LimitedTextWriter(MAX_OUTPUT_LENGTH)
    counter = {"events": 0}
    deadline = time.monotonic() + MAX_EXECUTION_SECONDS

    def trace(frame, event, arg):
        if event == "line":
            counter["events"] += 1
            if counter["events"] > MAX_TRACE_EVENTS:
                raise RunnerError("Execution budget exceeded. Check for an unbounded loop.")
            if time.monotonic() > deadline:
                raise RunnerError("Execution time limit exceeded. Simplify the operation and try again.")
        return trace

    previous_trace = sys.gettrace()
    try:
        sys.settrace(trace)
        execution_scope = {
            "__builtins__": safe_builtins,
            "_bounded_add": bounded_add,
            "_bounded_mul": bounded_mul,
        }
        execution_scope.update(namespace)
        with contextlib.redirect_stdout(output):
            exec(compile(tree, "<training-mission>", "exec"), execution_scope, execution_scope)
    except RunnerError as error:
        return {"output": output.getvalue(), "error": str(error), "files": workspace}
    except Exception as error:
        return {"output": output.getvalue(),
                "error": f"{type(error).__name__}: {error}", "files": workspace}
    finally:
        sys.settrace(previous_trace)
    return {"output": output.getvalue(), "error": "", "files": workspace}




def _legacy_level_definition(level_id):
    """Temporary compatibility catalog until Kotlin passes its LevelDefinition."""
    if level_id == "tutorial_01":
        return {
            "id": "tutorial_01",
            "map": ["....."] * 5,
            "robot_start": {"x": 0, "y": 2},
            "robot_api": ["move", "action"],
            "resources": {
                "battery": {"initial": 32, "minimum": 0, "maximum": 100, "move_cost": 1}
            },
            "sensors": [],
            "scenarios": [],
            "objects": [
                {
                    "id": "energy_cell",
                    "type": "ENERGY_CELL",
                    "position": {"x": 2, "y": 2},
                    "completes_objective": "collect_energy",
                    "timeline_event": "collect_energy",
                    "resource_changes": {"battery": 20},
                    "event_message": "Energy cell collected. Battery restored by 20%.",
                },
                {
                    "id": "exit",
                    "type": "EXIT",
                    "position": {"x": 4, "y": 2},
                    "required_objectives": ["collect_energy"],
                    "completes_objective": "open_exit",
                    "timeline_event": "open_exit",
                    "event_message": "Exit opened. Training room completed.",
                    "prerequisite_error": "The exit is offline. Collect the energy cell first.",
                },
            ],
            "objectives": [
                {"id": "collect_energy", "completion_event": "energy_collected"},
                {"id": "open_exit", "completion_event": "exit_open"},
            ],
        }
    if level_id == "tutorial_02":
        return {
            "id": "tutorial_02",
            "map": ["....."] * 5,
            "robot_start": {"x": 0, "y": 2},
            "robot_api": ["move", "action", "scan_distance"],
            "resources": {},
            "sensors": [
                {
                    "id": "range_sensor",
                    "type": "DISTANCE",
                    "maximum_range": 1000,
                    "requires_scan_before_movement": True,
                    "completes_objective": "read_sensor",
                }
            ],
            "scenarios": [
                {
                    "id": "blocked",
                    "sensor_readings": {"range_sensor": 18},
                    "obstacle_positions": [{"x": 1, "y": 2}],
                },
                {
                    "id": "clear",
                    "sensor_readings": {"range_sensor": 42},
                    "obstacle_positions": [],
                },
            ],
            "objects": [
                {
                    "id": "blocked_checkpoint",
                    "type": "CHECKPOINT",
                    "position": {"x": 0, "y": 3},
                    "scenario_ids": ["blocked"],
                    "requires_sensor_scan": True,
                    "completes_objective": "open_checkpoint",
                    "timeline_event": "open_checkpoint",
                    "event_message": "CHECKPOINT UNLOCKED // TUTORIAL 02 COMPLETE",
                },
                {
                    "id": "clear_checkpoint",
                    "type": "CHECKPOINT",
                    "position": {"x": 1, "y": 2},
                    "scenario_ids": ["clear"],
                    "requires_sensor_scan": True,
                    "completes_objective": "open_checkpoint",
                    "timeline_event": "open_checkpoint",
                    "event_message": "CHECKPOINT UNLOCKED // TUTORIAL 02 COMPLETE",
                },
                {
                    "id": "front_obstacle",
                    "type": "OBSTACLE",
                    "position": {"x": 1, "y": 2},
                    "scenario_ids": ["blocked"],
                    "blocks_movement": True,
                },
            ],
            "objectives": [
                {"id": "read_sensor", "completion_event": "sensor_scanned"},
                {"id": "open_checkpoint", "completion_event": "checkpoint_open"},
            ],
        }
    raise RunnerError("Unknown training level.")


def _position(value, label):
    if not isinstance(value, dict):
        raise RunnerError(f"Invalid {label} position.")
    x, y = value.get("x"), value.get("y")
    if type(x) is not int or type(y) is not int:
        raise RunnerError(f"Invalid {label} position.")
    return x, y


def _validate_level_definition(definition):
    if not isinstance(definition, dict):
        raise RunnerError("Invalid level definition.")
    level_id = definition.get("id")
    map_rows = definition.get("map")
    if not isinstance(level_id, str) or not level_id.strip():
        raise RunnerError("Level id is required.")
    if (not isinstance(map_rows, list) or not map_rows
            or any(not isinstance(row, str) or not row for row in map_rows)):
        raise RunnerError("Level map is invalid.")
    width = len(map_rows[0])
    if width < 1 or any(len(row) != width for row in map_rows):
        raise RunnerError("Level map rows must have equal width.")
    if any(tile not in ".#HD" for row in map_rows for tile in row):
        raise RunnerError("Level map contains an unsupported tile.")
    start_x, start_y = _position(definition.get("robot_start"), "robot start")
    if not (0 <= start_x < width and 0 <= start_y < len(map_rows)):
        raise RunnerError("Robot start is outside the map.")
    if map_rows[start_y][start_x] == "#":
        raise RunnerError("Robot start cannot be inside a wall.")

    allowed_methods = {"move", "action", "scan_distance", "scan_object", "battery", "facing"}
    methods = definition.get("robot_api", [])
    if (not isinstance(methods, list) or not methods
            or any(method not in allowed_methods for method in methods)
            or len(methods) != len(set(methods))):
        raise RunnerError("Level robot API definition is invalid.")

    sensors = definition.get("sensors", [])
    if not isinstance(sensors, list):
        raise RunnerError("Level sensors are invalid.")
    sensor_ids = set()
    for sensor in sensors:
        if not isinstance(sensor, dict):
            raise RunnerError("Level sensor definition is invalid.")
        sensor_id = sensor.get("id")
        maximum = sensor.get("maximum_range")
        if (not isinstance(sensor_id, str) or not sensor_id or sensor_id in sensor_ids
                or type(maximum) is not int or maximum < 1):
            raise RunnerError("Level sensor definition is invalid.")
        if sensor.get("type") != "DISTANCE":
            raise RunnerError("Unsupported sensor type.")
        if sensor.get("source", "scenario") not in ("scenario", "map"):
            raise RunnerError("Unsupported sensor source.")
        if type(sensor.get("cell_size_cm", 10)) is not int or sensor.get("cell_size_cm", 10) < 1:
            raise RunnerError("Level sensor definition is invalid.")
        sensor_ids.add(sensor_id)
    if sensors and "scan_distance" not in methods:
        raise RunnerError("Levels with sensors must expose scan_distance.")

    scenarios = definition.get("scenarios", [])
    if not isinstance(scenarios, list):
        raise RunnerError("Level scenarios are invalid.")
    scenario_ids = set()
    scenario_maps = {}
    for scenario in scenarios:
        if not isinstance(scenario, dict):
            raise RunnerError("Level scenario definition is invalid.")
        scenario_id = scenario.get("id")
        if not isinstance(scenario_id, str) or not scenario_id or scenario_id in scenario_ids:
            raise RunnerError("Level scenario ids must be unique.")
        scenario_ids.add(scenario_id)
        smap = map_rows
        if scenario.get("map") is not None:
            smap = scenario["map"]
            if (not isinstance(smap, list) or len(smap) != len(map_rows)
                    or any(not isinstance(row, str) or len(row) != width for row in smap)
                    or any(tile not in ".#HD" for row in smap for tile in row)):
                raise RunnerError("Scenario map must match the level size and use supported tiles.")
            if smap[start_y][start_x] == "#":
                raise RunnerError("Robot start cannot be inside a wall.")
        scenario_maps[scenario_id] = smap
        readings = scenario.get("sensor_readings", {})
        if not isinstance(readings, dict):
            raise RunnerError("Scenario sensor readings are invalid.")
        for sensor_id, reading in readings.items():
            sensor = next((item for item in sensors if item["id"] == sensor_id), None)
            if sensor is None or type(reading) is not int or not 0 <= reading <= sensor["maximum_range"]:
                raise RunnerError("Scenario contains an invalid sensor reading.")
        for obstacle in scenario.get("obstacle_positions", []):
            x, y = _position(obstacle, "scenario obstacle")
            if not (0 <= x < width and 0 <= y < len(map_rows)) or smap[y][x] == "#":
                raise RunnerError("Scenario obstacle is outside the walkable map.")

    objects = definition.get("objects", [])
    if not isinstance(objects, list):
        raise RunnerError("Level objects are invalid.")
    object_ids = set()
    for obj in objects:
        if not isinstance(obj, dict):
            raise RunnerError("Level object definition is invalid.")
        object_id = obj.get("id")
        if not isinstance(object_id, str) or not object_id or object_id in object_ids:
            raise RunnerError("Level object ids must be unique.")
        object_ids.add(object_id)
        x, y = _position(obj.get("position"), "object")
        if not (0 <= x < width and 0 <= y < len(map_rows)):
            raise RunnerError("Level object is outside the walkable map.")
        if obj.get("type") not in {"ENERGY_CELL", "EXIT", "CHECKPOINT", "OBSTACLE", "SWITCH", "CHARGER"}:
            raise RunnerError("Unsupported level object type.")
        scenario_refs = obj.get("scenario_ids", [])
        if not isinstance(scenario_refs, list) or any(ref not in scenario_ids for ref in scenario_refs):
            raise RunnerError("Level object refers to an unknown scenario.")
        for map_to_check in ([scenario_maps[ref] for ref in scenario_refs] or [map_rows]):
            if map_to_check[y][x] == "#":
                raise RunnerError("Level object is outside the walkable map.")

    objectives = definition.get("objectives", [])
    if not isinstance(objectives, list):
        raise RunnerError("Level objectives are invalid.")
    objective_ids = set()
    for objective in objectives:
        if not isinstance(objective, dict):
            raise RunnerError("Level objective definition is invalid.")
        objective_id = objective.get("id")
        if not isinstance(objective_id, str) or not objective_id or objective_id in objective_ids:
            raise RunnerError("Level objective ids must be unique.")
        if not isinstance(objective.get("completion_event"), str) or not objective["completion_event"]:
            raise RunnerError("Level objective completion event is required.")
        objective_ids.add(objective_id)
    for sensor in sensors:
        completed = sensor.get("completes_objective")
        if completed is not None and completed not in objective_ids:
            raise RunnerError("Level sensor completes an unknown objective.")
    for obj in objects:
        if any(required not in objective_ids for required in obj.get("required_objectives", [])):
            raise RunnerError("Level object requires an unknown objective.")
        completed = obj.get("completes_objective")
        if completed is not None and completed not in objective_ids:
            raise RunnerError("Level object completes an unknown objective.")

    resources = definition.get("resources", {})
    if not isinstance(resources, dict):
        raise RunnerError("Level resources are invalid.")
    for resource_id, resource in resources.items():
        if (not isinstance(resource_id, str) or not resource_id or not isinstance(resource, dict)
                or any(type(resource.get(key)) is not int for key in ("initial", "minimum", "maximum", "move_cost"))
                or resource["minimum"] > resource["initial"]
                or resource["initial"] > resource["maximum"]
                or resource["move_cost"] < 0):
            raise RunnerError("Level resource definition is invalid.")
    features = definition.get("python_features")
    if features is not None and (
            not isinstance(features, list) or any(item not in FEATURE_NODES for item in features)
            or len(features) != len(set(features))):
        raise RunnerError("Level Python feature list is invalid.")
    par = definition.get("par")
    if par is not None and (not isinstance(par, dict)
            or any(key not in ("lines", "moves") or type(value) is not int or value < 1 for key, value in par.items())):
        raise RunnerError("Level par values are invalid.")
    if definition.get("robot_facing", "right") not in ("up", "down", "left", "right"):
        raise RunnerError("Level robot facing is invalid.")
    return width, len(map_rows), methods


_DELTAS = {"up": (0, -1), "down": (0, 1), "left": (-1, 0), "right": (1, 0)}
_OBJECT_LABELS = {
    "ENERGY_CELL": "cell", "EXIT": "exit", "CHECKPOINT": "checkpoint",
    "SWITCH": "switch", "CHARGER": "charger",
}


def _count_code_lines(source):
    return sum(1 for line in source.splitlines() if line.strip() and not line.strip().startswith("#"))


def _stars(definition, lines, moves):
    par = definition.get("par")
    stars = 1
    if par:
        if "lines" in par and lines <= par["lines"]:
            stars += 1
        if "moves" in par and moves <= par["moves"]:
            stars += 1
    return stars


def _run_level(source, definition, supplied_state):
    width, height, allowed_methods = _validate_level_definition(definition)
    if not isinstance(supplied_state, dict):
        raise RunnerError("Invalid level state.")
    features = definition.get("python_features")
    allowed_nodes = ALLOWED_NODES if features is None else _nodes_for_features(features)

    start_x, start_y = _position(definition["robot_start"], "robot start")
    facing = supplied_state.get("facing", definition.get("robot_facing", "right"))
    if facing not in _DELTAS:
        facing = "right"
    state = {
        "robot_x": max(0, min(width - 1, int(supplied_state.get("robot_x", start_x)))),
        "robot_y": max(0, min(height - 1, int(supplied_state.get("robot_y", start_y)))),
        "resources": {},
        "completed_objectives": set(
            item for item in supplied_state.get("completed_objectives", [])
            if isinstance(item, str)
        ),
        "sensor_scanned": bool(supplied_state.get("sensor_scanned", False)),
        "sensor_readings": {},
        "scenario_id": supplied_state.get("scenario_id"),
        "completed": bool(supplied_state.get("completed", False)),
        "facing": facing,
        "doors_open": bool(supplied_state.get("doors_open", False)),
        "used_objects": set(
            item for item in supplied_state.get("used_objects", []) if isinstance(item, str)
        ),
        "failed": False,
        "failure": "",
    }
    for resource_id, config in definition.get("resources", {}).items():
        raw = supplied_state.get("resources", {}).get(resource_id, config["initial"])
        state["resources"][resource_id] = max(config["minimum"], min(config["maximum"], int(raw)))

    scenarios = definition.get("scenarios", [])
    scenario = next((item for item in scenarios if item["id"] == state["scenario_id"]), None)
    if scenario is None and scenarios:
        scenario = scenarios[0]
        state["scenario_id"] = scenario["id"]
    if scenario:
        state["sensor_readings"].update(scenario.get("sensor_readings", {}))
    supplied_readings = supplied_state.get("sensor_readings", {})
    if isinstance(supplied_readings, dict):
        for sensor in definition.get("sensors", []):
            value = supplied_readings.get(sensor["id"])
            if type(value) is int:
                state["sensor_readings"][sensor["id"]] = max(0, min(sensor["maximum_range"], value))

    grid = list(scenario["map"]) if scenario and scenario.get("map") else list(definition["map"])
    timeline = []
    events = []
    counters = {"moves": 0, "actions": 0, "scans": 0}
    obstacles = set()
    if scenario:
        obstacles.update(
            (item["x"], item["y"]) for item in scenario.get("obstacle_positions", [])
        )
    for obj in definition.get("objects", []):
        if obj.get("type") == "OBSTACLE" and (
            not obj.get("scenario_ids") or state["scenario_id"] in obj.get("scenario_ids", [])
        ):
            obstacles.add(_position(obj["position"], "obstacle"))

    def active_objects():
        return [
            obj for obj in definition.get("objects", [])
            if obj.get("type") != "OBSTACLE"
            and (not obj.get("scenario_ids") or state["scenario_id"] in obj["scenario_ids"])
        ]

    def blocked(x, y):
        if not (0 <= x < width and 0 <= y < height):
            return True
        if grid[y][x] == "#" or (grid[y][x] == "D" and not state["doors_open"]):
            return True
        return (x, y) in obstacles

    def finish_if_ready():
        objective_ids = {item["id"] for item in definition.get("objectives", [])}
        state["completed"] = bool(objective_ids) and objective_ids.issubset(state["completed_objectives"])

    def fail(reason, message):
        state["failed"] = True
        state["failure"] = reason
        timeline.append(f"fail:{reason}")
        raise RunnerError(message)

    def resolve_direction(direction, method):
        if direction is None or (isinstance(direction, str) and direction.lower() == "front"):
            return state["facing"]
        if not isinstance(direction, str) or direction.lower() not in _DELTAS:
            raise RunnerError(f'robot.{method}() expects "up", "down", "left", "right" or "front".')
        return direction.lower()

    class LevelRobot:
        def scan_distance(self, direction=None):
            if state["completed"]:
                raise RunnerError("The mission is already complete.")
            sensor = next((item for item in definition.get("sensors", []) if item["type"] == "DISTANCE"), None)
            if sensor is None:
                raise RunnerError("No distance sensor is installed in this level.")
            if sensor.get("source", "scenario") == "map":
                dx, dy = _DELTAS[resolve_direction(direction, "scan_distance")]
                x, y, free = state["robot_x"], state["robot_y"], 0
                while not blocked(x + dx, y + dy):
                    x, y, free = x + dx, y + dy, free + 1
                reading = min(sensor["maximum_range"], (free + 1) * sensor.get("cell_size_cm", 10))
            else:
                if direction is not None:
                    raise RunnerError("This sensor only reads straight ahead. Call robot.scan_distance() without a direction.")
                reading = state["sensor_readings"].get(sensor["id"])
                if type(reading) is not int:
                    raise RunnerError("The distance sensor has no reading in this scenario.")
            state["sensor_scanned"] = True
            counters["scans"] += 1
            timeline.append(f"scan:{reading}")
            events.append(f"RANGE SENSOR // {reading} CM")
            objective_id = sensor.get("completes_objective")
            if objective_id:
                state["completed_objectives"].add(objective_id)
            return reading

        def scan_object(self, direction=None):
            if state["completed"]:
                raise RunnerError("The mission is already complete.")
            name = resolve_direction(direction, "scan_object")
            dx, dy = _DELTAS[name]
            x, y = state["robot_x"] + dx, state["robot_y"] + dy
            if not (0 <= x < width and 0 <= y < height):
                found = "edge"
            elif grid[y][x] == "#":
                found = "wall"
            elif grid[y][x] == "D" and not state["doors_open"]:
                found = "door"
            elif (x, y) in obstacles:
                found = "obstacle"
            elif grid[y][x] == "H":
                found = "hazard"
            else:
                obj = next((item for item in active_objects()
                            if _position(item["position"], "object") == (x, y)
                            and not (item.get("single_use") and item["id"] in state["used_objects"])), None)
                found = _OBJECT_LABELS.get(obj["type"], "empty") if obj else "empty"
            counters["scans"] += 1
            timeline.append(f"look:{found}")
            events.append(f"OBJECT SCAN // {name.upper()}: {found.upper()}")
            return found

        def battery(self):
            if "battery" not in state["resources"]:
                raise RunnerError("This level has no battery.")
            return state["resources"]["battery"]

        def facing(self):
            return state["facing"]

        def move(self, direction):
            if state["completed"]:
                raise RunnerError("The mission is already complete.")
            if any(item.get("requires_scan_before_movement", False)
                   for item in definition.get("sensors", [])) and not state["sensor_scanned"]:
                raise RunnerError("Scan the environment with robot.scan_distance() before moving.")
            if not isinstance(direction, str):
                raise RunnerError('robot.move() expects "up", "down", "left" or "right".')
            direction = direction.lower()
            if direction not in _DELTAS:
                raise RunnerError('Unknown direction. Use "up", "down", "left" or "right".')
            dx, dy = _DELTAS[direction]
            state["facing"] = direction
            next_x, next_y = state["robot_x"] + dx, state["robot_y"] + dy
            if not (0 <= next_x < width and 0 <= next_y < height):
                raise RunnerError("Movement blocked by the level boundary.")
            if (next_x, next_y) in obstacles:
                raise RunnerError("OBSTACLE COLLISION // The selected route is blocked.")
            if grid[next_y][next_x] == "#":
                raise RunnerError("Movement blocked by a wall.")
            if grid[next_y][next_x] == "D" and not state["doors_open"]:
                raise RunnerError("DOOR CLOSED // Find a way to open it first.")
            for resource_id, config in definition.get("resources", {}).items():
                if state["resources"][resource_id] < config["move_cost"]:
                    fail(f"{resource_id}_depleted", f"{resource_id.capitalize()} depleted. The robot cannot move.")
            state["robot_x"], state["robot_y"] = next_x, next_y
            for resource_id, config in definition.get("resources", {}).items():
                state["resources"][resource_id] = max(
                    config["minimum"], state["resources"][resource_id] - config["move_cost"]
                )
            counters["moves"] += 1
            timeline.append(f"move:{next_x}:{next_y}")
            events.append(f"MX-01 moved {direction}.")
            if grid[next_y][next_x] == "H":
                fail("hazard", "HAZARD // MX-01 entered a damaged zone. The run failed.")
            return f"POSITION ({next_x}, {next_y})"

        def action(self):
            if state["completed"]:
                raise RunnerError("The mission is already complete.")
            position = (state["robot_x"], state["robot_y"])
            for obj in active_objects():
                if _position(obj["position"], "object") != position:
                    continue
                if obj.get("single_use") and obj["id"] in state["used_objects"]:
                    raise RunnerError("Nothing is left to use here.")
                if obj.get("requires_sensor_scan", False) and not state["sensor_scanned"]:
                    raise RunnerError("Read the distance sensor before interacting with the checkpoint.")
                required = set(obj.get("required_objectives", []))
                if not required.issubset(state["completed_objectives"]):
                    raise RunnerError(obj.get("prerequisite_error", "Complete the previous objective first."))
                objective_id = obj.get("completes_objective")
                if objective_id:
                    state["completed_objectives"].add(objective_id)
                for resource_id, change in obj.get("resource_changes", {}).items():
                    if resource_id not in state["resources"] or type(change) is not int:
                        raise RunnerError("Level object contains an invalid resource change.")
                    config = definition["resources"][resource_id]
                    state["resources"][resource_id] = max(
                        config["minimum"],
                        min(config["maximum"], state["resources"][resource_id] + change),
                    )
                if obj.get("single_use"):
                    state["used_objects"].add(obj["id"])
                if obj["type"] == "SWITCH" and obj.get("opens_doors"):
                    state["doors_open"] = True
                    timeline.append("door_open")
                counters["actions"] += 1
                timeline.append(obj.get("timeline_event", f"interact:{obj['id']}"))
                if obj.get("event_message"):
                    events.append(obj["event_message"])
                finish_if_ready()
                messages = {
                    "ENERGY_CELL": "ENERGY CELL ACQUIRED", "EXIT": "EXIT OPEN // TRAINING COMPLETE",
                    "SWITCH": "SWITCH ACTIVATED", "CHARGER": "BATTERY RECHARGED",
                }
                return messages.get(obj["type"], "CHECKPOINT OPEN")
            raise RunnerError("No interactive object at the robot's current position.")

    robot = LevelRobot()
    result = _execute(source, variables={"robot": robot}, allowed_robot_methods=tuple(allowed_methods),
                      allowed_nodes=allowed_nodes)
    lines = _count_code_lines(source)
    metrics = {"lines": lines, "moves": counters["moves"], "actions": counters["actions"], "scans": counters["scans"]}
    return {
        "output": result.get("output", ""),
        "error": result.get("error", ""),
        "state": {
            "robot_x": state["robot_x"],
            "robot_y": state["robot_y"],
            "resources": state["resources"],
            "completed_objectives": sorted(state["completed_objectives"]),
            "sensor_scanned": state["sensor_scanned"],
            "sensor_readings": state["sensor_readings"],
            "scenario_id": state["scenario_id"],
            "completed": state["completed"],
            "facing": state["facing"],
            "doors_open": state["doors_open"],
            "used_objects": sorted(state["used_objects"]),
        },
        "timeline": timeline,
        "events": events,
        "complete": state["completed"],
        "failed": state["failed"],
        "failure": state["failure"],
        "metrics": metrics,
        "stars": _stars(definition, lines, counters["moves"]) if state["completed"] else 0,
    }


def _run_level_suite(source, definition):
    """Run one program against EVERY scenario; the level passes only if all succeed."""
    _validate_level_definition(definition)
    scenarios = definition.get("scenarios", []) or [None]
    runs = []
    for scenario in scenarios:
        scenario_id = scenario["id"] if scenario else None
        run = _run_level(source, definition, {"scenario_id": scenario_id} if scenario_id else {})
        run["scenario_id"] = scenario_id
        runs.append(run)
    complete = all(run["complete"] for run in runs)
    focus = next((index for index, run in enumerate(runs) if not run["complete"]), 0)
    shown = runs[focus]
    metrics = {
        "lines": shown["metrics"]["lines"],
        "moves": max(run["metrics"]["moves"] for run in runs),
        "actions": max(run["metrics"]["actions"] for run in runs),
        "scans": max(run["metrics"]["scans"] for run in runs),
    }
    return {
        "complete": complete,
        "scenario_count": len(runs),
        "focus": focus,
        "output": shown["output"],
        "error": shown["error"],
        "timeline": shown["timeline"],
        "events": shown["events"],
        "state": shown["state"],
        "failed": shown["failed"],
        "failure": shown["failure"],
        "metrics": metrics,
        "stars": _stars(definition, metrics["lines"], metrics["moves"]) if complete else 0,
        "scenarios": [
            {"id": run["scenario_id"], "complete": run["complete"], "failed": run["failed"],
             "error": run["error"], "moves": run["metrics"]["moves"]}
            for run in runs
        ],
    }


def run_level_suite_json(source, level_json):
    """Execute a player program against all scenarios of a data-defined level."""
    try:
        if not isinstance(level_json, str) or len(level_json) > 50000:
            raise RunnerError("Level definition is invalid or too large.")
        return json.dumps(_run_level_suite(source, json.loads(level_json)))
    except RunnerError as error:
        return json.dumps({"complete": False, "error": str(error), "scenario_count": 0, "focus": 0,
                           "output": "", "timeline": [], "events": [], "state": {}, "failed": False,
                           "failure": "", "metrics": {}, "stars": 0, "scenarios": []})
    except (TypeError, ValueError, KeyError, json.JSONDecodeError) as error:
        return json.dumps({"complete": False, "error": f"Invalid level data: {error}", "scenario_count": 0,
                           "focus": 0, "output": "", "timeline": [], "events": [], "state": {},
                           "failed": False, "failure": "", "metrics": {}, "stars": 0, "scenarios": []})


def run_level_json(source, level_json, state_json="{}"):
    """Execute a player program against a validated, data-defined level."""
    try:
        if not isinstance(level_json, str) or len(level_json) > 50000:
            raise RunnerError("Level definition is invalid or too large.")
        definition = json.loads(level_json)
        supplied_state = json.loads(state_json)
        return json.dumps(_run_level(source, definition, supplied_state))
    except RunnerError as error:
        return json.dumps({
            "output": "", "error": str(error), "state": {},
            "timeline": [], "events": [], "complete": False,
        })
    except (TypeError, ValueError, KeyError, json.JSONDecodeError) as error:
        return json.dumps({
            "output": "", "error": f"Invalid level data: {error}", "state": {},
            "timeline": [], "events": [], "complete": False,
        })


def run_tutorial_json(source, state_json="{}"):
    """Compatibility wrapper preserving the original Tutorial 1 state contract."""
    try:
        supplied = json.loads(state_json)
        if not isinstance(supplied, dict):
            raise RunnerError("Invalid tutorial state.")
        generic_state = {
            "robot_x": supplied.get("robot_x", 0),
            "robot_y": supplied.get("robot_y", 2),
            "resources": {"battery": supplied.get("battery", 32)},
            "completed_objectives": [
                objective for objective, done in (
                    ("collect_energy", supplied.get("energy_collected", False)),
                    ("open_exit", supplied.get("exit_open", False)),
                ) if done
            ],
            "completed": supplied.get("completed", False),
        }
        result = _run_level(source, _legacy_level_definition("tutorial_01"), generic_state)
        state = result["state"]
        return json.dumps({
            "output": result["output"],
            "error": result["error"],
            "state": {
                "robot_x": state["robot_x"],
                "robot_y": state["robot_y"],
                "energy_collected": "collect_energy" in state["completed_objectives"],
                "exit_open": "open_exit" in state["completed_objectives"],
                "completed": result["complete"],
                "battery": state["resources"]["battery"],
            },
            "timeline": result["timeline"],
            "events": result["events"],
            "complete": result["complete"],
        })
    except RunnerError as error:
        return json.dumps({"output": "", "error": str(error), "state": {}, "timeline": [], "events": [], "complete": False})
    except (TypeError, ValueError, json.JSONDecodeError) as error:
        return json.dumps({"output": "", "error": f"Invalid tutorial state: {error}",
                           "state": {}, "timeline": [], "events": [], "complete": False})


def run_tutorial2_json(source, state_json="{}"):
    """Compatibility wrapper preserving the original Tutorial 2 state contract."""
    try:
        supplied = json.loads(state_json)
        if not isinstance(supplied, dict):
            raise RunnerError("Invalid Tutorial 2 state.")
        sensor_distance = max(0, min(1000, int(supplied.get("sensor_distance", 18))))
        generic_state = {
            "robot_x": supplied.get("robot_x", 0),
            "robot_y": supplied.get("robot_y", 2),
            "sensor_scanned": supplied.get("sensor_scanned", False),
            "sensor_readings": {"range_sensor": sensor_distance},
            "scenario_id": "clear" if sensor_distance > 30 else "blocked",
            "completed_objectives": [
                "read_sensor" if supplied.get("sensor_scanned", False) else None,
                "open_checkpoint" if supplied.get("checkpoint_open", False) else None,
            ],
            "completed": supplied.get("completed", False),
        }
        generic_state["completed_objectives"] = [
            item for item in generic_state["completed_objectives"] if item
        ]
        result = _run_level(source, _legacy_level_definition("tutorial_02"), generic_state)
        state = result["state"]
        return json.dumps({
            "output": result["output"],
            "error": result["error"],
            "state": {
                "robot_x": state["robot_x"],
                "robot_y": state["robot_y"],
                "sensor_scanned": state["sensor_scanned"],
                "sensor_distance": state["sensor_readings"].get("range_sensor", sensor_distance),
                "checkpoint_open": "open_checkpoint" in state["completed_objectives"],
                "completed": result["complete"],
            },
            "timeline": result["timeline"],
            "events": result["events"],
            "complete": result["complete"],
        })
    except RunnerError as error:
        return json.dumps({"output": "", "error": str(error), "state": {}, "timeline": [], "events": [], "complete": False})
    except (TypeError, ValueError, json.JSONDecodeError) as error:
        return json.dumps({"output": "", "error": f"Invalid Tutorial 2 state: {error}",
                           "state": {}, "timeline": [], "events": [], "complete": False})
