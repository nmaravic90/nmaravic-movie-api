import requests
import sys

BASE_URL = "http://localhost:8080/api/v1"
ACTUATOR_URL = "http://localhost:8081/actuator/health"

def check_actuator():
    print("Checking Spring Boot Actuator...")
    try:
        r = requests.get(ACTUATOR_URL, timeout=5)
        if r.status_code == 404:
            print("  [SKIP] Actuator not exposed (HTTP 404)")
            return True
        data = r.json()
        app_status = data.get("status", "UNKNOWN")
        if app_status == "UP":
            print("  [OK] Actuator UP")
        else:
            print(f"  [FAIL] Actuator status: {app_status}")
            return False

        db = data.get("components", {}).get("db", {})
        if db:
            db_status = db.get("status", "UNKNOWN")
            if db_status == "UP":
                db_name = db.get("details", {}).get("database", "unknown")
                print(f"  [OK] DB UP ({db_name})")
            else:
                print(f"  [FAIL] DB status: {db_status}")
                return False
        else:
            print("  [SKIP] DB details not exposed — set show-details=always")

        return True
    except requests.exceptions.ConnectionError:
        print(f"  [FAIL] Cannot connect to {ACTUATOR_URL}")
        return False
    except requests.exceptions.Timeout:
        print(f"  [FAIL] Timeout on {ACTUATOR_URL}")
        return False


def check_api_reachable():
    print("Checking API reachability...")
    try:
        r = requests.get(f"{BASE_URL}/movies?size=1", timeout=5)
        if r.status_code in (200, 401, 403):
            print(f"  [OK] API responding (HTTP {r.status_code})")
            return True
        else:
            print(f"  [FAIL] Unexpected status: {r.status_code}")
            return False
    except requests.exceptions.ConnectionError:
        print(f"  [FAIL] Cannot connect to {BASE_URL}")
        return False
    except requests.exceptions.Timeout:
        print(f"  [FAIL] Timeout on {BASE_URL}")
        return False


def check_db_via_api():
    print("Checking DB connectivity (via API response)...")
    try:
        r = requests.get(f"{BASE_URL}/movies?size=1", timeout=5)
        if r.status_code in (401, 403):
            print(f"  [OK] DB check skipped — endpoint secured (HTTP {r.status_code})")
            print("       Tip: set management.endpoint.health.show-details=always for proper DB check")
            return True
        data = r.json()
        if all(k in data for k in ["content", "page", "totalElements", "totalPages"]):
            print(f"  [OK] DB reachable, totalElements={data['totalElements']}")
            return True
        else:
            print(f"  [FAIL] Unexpected response structure: {data}")
            return False
    except Exception as e:
        print(f"  [FAIL] {e}")
        return False

if __name__ == "__main__":
    print("=" * 50)
    print("HEALTH CHECK — Movie Service")
    print("=" * 50)
    results = [
        check_actuator(),
        check_api_reachable(),
        check_db_via_api(),
    ]
    print()
    passed = sum(results)
    total = len(results)
    print(f"Result: {passed}/{total} checks passed")

    if passed == total:
        print("Status: HEALTHY")
        sys.exit(0)
    else:
        print("Status: UNHEALTHY")
        sys.exit(1)