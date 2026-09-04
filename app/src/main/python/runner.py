import contextlib
import io
import json
import os
import sys
import traceback


def run_code(code, working_dir, package_dir):
    output = io.StringIO()
    errors = io.StringIO()
    old_cwd = os.getcwd()
    original_path = list(sys.path)
    try:
        os.makedirs(working_dir, exist_ok=True)
        os.makedirs(package_dir, exist_ok=True)
        os.chdir(working_dir)
        sys.path.insert(0, package_dir)
        sys.path.insert(0, working_dir)
        scope = {"__name__": "__main__", "__file__": os.path.join(working_dir, "main.py")}
        with contextlib.redirect_stdout(output), contextlib.redirect_stderr(errors):
            exec(compile(code, scope["__file__"], "exec"), scope, scope)
        return json.dumps({"ok": True, "stdout": output.getvalue(), "stderr": errors.getvalue()})
    except Exception:
        traceback.print_exc(file=errors)
        return json.dumps({"ok": False, "stdout": output.getvalue(), "stderr": errors.getvalue()})
    finally:
        os.chdir(old_cwd)
        sys.path[:] = original_path


def install_package(package, package_dir):
    try:
        os.makedirs(package_dir, exist_ok=True)
        from pip._internal.cli.main import main
        exit_code = main([
            "install",
            "--no-input",
            "--disable-pip-version-check",
            "--target",
            package_dir,
            package,
        ])
        return json.dumps({
            "ok": exit_code == 0,
            "message": "Installed " + package if exit_code == 0 else "pip exited with code " + str(exit_code),
        })
    except Exception:
        return json.dumps({"ok": False, "message": traceback.format_exc()})