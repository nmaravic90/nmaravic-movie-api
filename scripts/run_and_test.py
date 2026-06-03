import subprocess
import sys
import time
import requests

HEALTH_CHECK_SCRIPT = "health_check.py"
SMOKE_TEST_SCRIPT = "smoke_test.py"
ACTUATOR_URL = "http://localhost:8081/actuator/health"
MAX_WAIT_SECONDS = 120
POLL_INTERVAL = 5

def run(command, description):
    print(f"\n>>> {description}")
    print(f"    {' '.join(command)}")
    result = subprocess.run(command, shell=False)
    if result.returncode != 0:
        print(f"[FAIL] Command failed with exit code {result.returncode}")
        sys.exit(1)
    print("[OK] Done")


def wait_for_app():
    print(f"\n>>> Waiting for app to start (max {MAX_WAIT_SECONDS}s)...")
    elapsed = 0
    while elapsed < MAX_WAIT_SECONDS:
        try:
            r = requests.get(ACTUATOR_URL, timeout=3)
            if r.status_code == 200:
                data = r.json()
                if data.get("status") == "UP":
                    print(f"  [OK] App is UP (after {elapsed}s)")
                    return True
        except Exception:
            pass
        print(f"  Waiting... ({elapsed}s)")
        time.sleep(POLL_INTERVAL)
        elapsed += POLL_INTERVAL

    print(f"  [FAIL] App did not start within {MAX_WAIT_SECONDS}s")
    return False

if __name__ == "__main__":
    print("=" * 55)
    print("RUN & TEST — Movie Service")
    print("=" * 55)

    run(["docker-compose", "down", "-v"], "Stopping containers and removing volumes")
    run(["docker-compose", "up", "--build", "-d"], "Building and starting containers")

    if not wait_for_app():
        print("\n[ERROR] App failed to start — check docker-compose logs")
        subprocess.run(["docker-compose", "logs", "--tail=50", "api"])
        sys.exit(1)

    print("\n" + "=" * 55)
    result = subprocess.run([sys.executable, HEALTH_CHECK_SCRIPT])
    if result.returncode != 0:
        print("\n[ERROR] Health check failed — aborting smoke test")
        sys.exit(1)

    print("\n" + "=" * 55)
    result = subprocess.run([sys.executable, SMOKE_TEST_SCRIPT])
    sys.exit(result.returncode)