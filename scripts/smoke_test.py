import requests
import sys

BASE_URL = "http://localhost:8080/api/v1"
TOKEN_URL = "http://localhost:8180/realms/movie-realm/protocol/openid-connect/token"
CLIENT_ID = "MOVIE-API"
CLIENT_SECRET = "VucoFWde1p8CxBJeYeGBi9vWu3y8J5Ib"
USERNAME = "nmaravic"
PASSWORD = "nmaravic"

PASS = 0
FAIL = 0

TEST_MOVIE = {
    "title": "Smoke Test Movie",
    "overview": "A movie created by the smoke test script.",
    "releaseYear": 2024,
    "director": "Smoke Tester",
    "genres": ["ACTION", "THRILLER"]
}


def get_token():
    print("Fetching OAuth2 token...")
    try:
        r = requests.post(TOKEN_URL, data={
            "grant_type": "password",
            "client_id": CLIENT_ID,
            "client_secret": CLIENT_SECRET,
            "username": USERNAME,
            "password": PASSWORD,
        }, timeout=10)
        if r.status_code == 200:
            token = r.json().get("access_token")
            print("  [OK] Token obtained")
            return token
        else:
            print(f"  [FAIL] Could not get token — HTTP {r.status_code}: {r.text}")
            return None
    except requests.exceptions.ConnectionError:
        print(f"  [FAIL] Cannot connect to Keycloak at {TOKEN_URL}")
        return None
    except requests.exceptions.Timeout:
        print("  [FAIL] Timeout connecting to Keycloak")
        return None


def auth_headers(token):
    return {"Authorization": f"Bearer {token}"}


def check(label, response, expected_status, validators=None):
    global PASS, FAIL
    status_ok = response.status_code == expected_status

    if not status_ok:
        print(f"  [FAIL] {label} — expected HTTP {expected_status}, got {response.status_code}")
        FAIL += 1
        return None

    body = None
    try:
        body = response.json()
    except Exception:
        pass

    if validators and body is not None:
        errors = []
        for v in validators:
            result = v(body)
            if result:
                errors.append(result)
        if errors:
            for e in errors:
                print(f"  [FAIL] {label} — {e}")
            FAIL += 1
            return body

    print(f"  [OK]   {label} (HTTP {response.status_code})")
    PASS += 1
    return body

def has_keys(*keys):
    def validator(body):
        missing = [k for k in keys if k not in body]
        if missing:
            return f"Missing keys: {missing}"
    return validator


def is_list():
    def validator(body):
        if not isinstance(body, list):
            return f"Expected list, got {type(body).__name__}"
    return validator

def create_test_movie(token):
    print("\n[Setup] Creating test movie...")
    headers = auth_headers(token)
    r = requests.post(f"{BASE_URL}/movies", json=TEST_MOVIE, headers=headers, timeout=5)
    if r.status_code == 201:
        movie = r.json()
        movie_id = movie.get("id")
        print(f"  [OK] Test movie created (id={movie_id})")
        return movie_id
    else:
        print(f"  [FAIL] Could not create test movie — HTTP {r.status_code}: {r.text}")
        return None


def delete_test_movie(token, movie_id):
    print(f"\n[Teardown] Deleting test movie (id={movie_id})...")
    headers = auth_headers(token)
    r = requests.delete(f"{BASE_URL}/movies/{movie_id}", headers=headers, timeout=5)
    if r.status_code == 204:
        print("  [OK] Test movie deleted")
    else:
        print(f"  [WARN] Could not delete test movie — HTTP {r.status_code}")

def test_movie_read(token, movie_id):
    print("\n[MovieRead] Public endpoints")
    headers = auth_headers(token)

    r = requests.get(f"{BASE_URL}/movies", headers=headers, timeout=5)
    check("GET /movies", r, 200, [has_keys("content", "page", "size", "totalElements", "totalPages")])

    r = requests.get(f"{BASE_URL}/movies?sort=avgRating&order=desc&size=5", headers=headers, timeout=5)
    check("GET /movies?sort=avgRating&order=desc&size=5", r, 200, [has_keys("content", "totalElements")])

    r = requests.get(f"{BASE_URL}/movies?genre=ACTION", headers=headers, timeout=5)
    check("GET /movies?genre=ACTION", r, 200, [has_keys("content")])

    r = requests.get(f"{BASE_URL}/movies?minRating=7.5", headers=headers, timeout=5)
    check("GET /movies?minRating=7.5", r, 200, [has_keys("content")])

    r = requests.get(f"{BASE_URL}/movies/top-rated", timeout=5)
    check("GET /movies/top-rated", r, 200, [is_list()])

    r = requests.get(f"{BASE_URL}/movies/top-rated?limit=5", timeout=5)
    body = check("GET /movies/top-rated?limit=5", r, 200, [is_list()])
    if body and len(body) > 5:
        print(f"  [FAIL] GET /movies/top-rated?limit=5 — returned {len(body)} items, expected <=5")
        global FAIL, PASS
        FAIL += 1
        PASS -= 1

    r = requests.get(f"{BASE_URL}/movies/{movie_id}", headers=headers, timeout=5)
    check(
        f"GET /movies/{movie_id}",
        r, 200,
        [has_keys("id", "title", "releaseYear", "director", "genres", "avgRating")]
    )

    r = requests.get(f"{BASE_URL}/movies/999999", headers=headers, timeout=5)
    check("GET /movies/999999 (not found)", r, 404, [has_keys("status", "error", "message")])


def test_movie_crud(token, movie_id):
    print("\n[MovieCrud] CRUD operations")
    headers = auth_headers(token)

    r = requests.patch(
        f"{BASE_URL}/movies/{movie_id}",
        json={"overview": "Updated by smoke test."},
        headers=headers,
        timeout=5
    )
    check(f"PATCH /movies/{movie_id}", r, 200, [has_keys("id", "title")])

    r = requests.put(
        f"{BASE_URL}/movies/{movie_id}",
        json={**TEST_MOVIE, "overview": "Replaced by smoke test."},
        headers=headers,
        timeout=5
    )
    check(f"PUT /movies/{movie_id}", r, 200, [has_keys("id", "title")])

    print("\n[MovieCrud] Unauthorized access (expect 401/403)")
    global PASS, FAIL

    r = requests.post(f"{BASE_URL}/movies", json=TEST_MOVIE, timeout=5)
    if r.status_code in (401, 403):
        print(f"  [OK]   POST /movies without auth — HTTP {r.status_code} (blocked)")
        PASS += 1
    else:
        print(f"  [WARN] POST /movies without auth returned {r.status_code}")

    r = requests.delete(f"{BASE_URL}/movies/{movie_id}", timeout=5)
    if r.status_code in (401, 403):
        print(f"  [OK]   DELETE /movies/{movie_id} without auth — HTTP {r.status_code} (blocked)")
        PASS += 1
    else:
        print(f"  [WARN] DELETE /movies/{movie_id} without auth returned {r.status_code}")


def test_movie_images(token, movie_id):
    print(f"\n[MovieImage] Images for movie {movie_id}")
    headers = auth_headers(token)

    r = requests.get(f"{BASE_URL}/movies/{movie_id}/images", headers=headers, timeout=5)
    check(f"GET /movies/{movie_id}/images", r, 200, [is_list()])


def test_movie_reviews(token, movie_id):
    print(f"\n[MovieReview] Reviews for movie {movie_id}")
    headers = auth_headers(token)
    global PASS, FAIL

    r = requests.get(f"{BASE_URL}/movies/{movie_id}/reviews", headers=headers, timeout=5)
    check(f"GET /movies/{movie_id}/reviews", r, 200, [has_keys("content", "page", "totalElements")])

    r = requests.post(
        f"{BASE_URL}/movies/{movie_id}/reviews",
        json={"rating": 8, "comment": "Smoke test review."},
        headers=headers,
        timeout=5
    )
    review_id = None
    if r.status_code == 201:
        review_id = r.json().get("id")
        print(f"  [OK]   POST /movies/{movie_id}/reviews (HTTP 201)")
        PASS += 1
    elif r.status_code == 409:
        print("  [INFO] POST review — already reviewed (HTTP 409), skipping")
    else:
        print(f"  [FAIL] POST review — HTTP {r.status_code}")
        FAIL += 1

    if review_id:
        r = requests.delete(f"{BASE_URL}/movies/{movie_id}/reviews/{review_id}", headers=headers, timeout=5)
        check(f"DELETE /movies/{movie_id}/reviews/{review_id}", r, 204)

    r = requests.post(
        f"{BASE_URL}/movies/{movie_id}/reviews",
        json={"rating": 5},
        timeout=5
    )
    if r.status_code in (401, 403):
        print(f"  [OK]   POST review without auth — HTTP {r.status_code} (blocked)")
        PASS += 1
    else:
        print(f"  [WARN] POST review without auth returned {r.status_code}")


def test_validation(token):
    print("\n[Validation] Bad request checks")
    headers = auth_headers(token)
    global PASS, FAIL

    r = requests.get(f"{BASE_URL}/movies?genre=INVALID_GENRE", headers=headers, timeout=5)
    if r.status_code == 400:
        print("  [OK]   GET /movies?genre=INVALID_GENRE — HTTP 400")
        PASS += 1
    else:
        print(f"  [INFO] GET /movies?genre=INVALID_GENRE — HTTP {r.status_code}")

    r = requests.get(f"{BASE_URL}/movies?minRating=999", headers=headers, timeout=5)
    if r.status_code == 400:
        print("  [OK]   GET /movies?minRating=999 — HTTP 400")
        PASS += 1
    else:
        print(f"  [INFO] GET /movies?minRating=999 — HTTP {r.status_code}")


if __name__ == "__main__":
    print("=" * 55)
    print("SMOKE TEST — Movie Service API")
    print(f"Target: {BASE_URL}")
    print("=" * 55)
    token = get_token()
    if not token:
        print("\n[ERROR] Cannot proceed without token — is Keycloak running?")
        sys.exit(2)
    movie_id = create_test_movie(token)
    if not movie_id:
        print("\n[ERROR] Cannot proceed without test movie")
        sys.exit(2)
    try:
        test_movie_read(token, movie_id)
        test_movie_crud(token, movie_id)
        test_movie_images(token, movie_id)
        test_movie_reviews(token, movie_id)
        test_validation(token)
    except requests.exceptions.ConnectionError:
        print(f"\n[ERROR] Cannot connect to {BASE_URL} — is the app running?")
        sys.exit(2)
    except requests.exceptions.Timeout:
        print("\n[ERROR] Request timed out")
        sys.exit(2)
    finally:
        delete_test_movie(token, movie_id)
    print()
    print("=" * 55)
    print(f"Result: {PASS} passed, {FAIL} failed")
    print("=" * 55)
    sys.exit(0 if FAIL == 0 else 1)