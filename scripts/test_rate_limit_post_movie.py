import requests
import sys

BASE_URL = "http://localhost:8080/api/v1/movies"
TOKEN_URL = "http://localhost:8180/realms/movie-realm/protocol/openid-connect/token"
CLIENT_ID = "MOVIE-API"
CLIENT_SECRET = "VucoFWde1p8CxBJeYeGBi9vWu3y8J5Ib"
USERNAME = "nmaravic"
PASSWORD = "nmaravic"

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
            print("  [OK] Token obtained\n")
            return token
        else:
            print(f"  [FAIL] Could not get token — HTTP {r.status_code}: {r.text}")
            return None
    except requests.exceptions.ConnectionError:
        print(f"  [FAIL] Cannot connect to Keycloak at {TOKEN_URL}")
        return None


def test_rate_limit(token, requests_count=120):
    headers = {"Authorization": f"Bearer {token}"}
    print(f"Testing rate limit — sending {requests_count} requests to {BASE_URL}")
    print("-" * 65)

    for i in range(1, requests_count + 1):
        response = requests.get(BASE_URL, headers=headers)
        remaining = response.headers.get("X-Rate-Limit-Remaining", "N/A")
        retry_after = response.headers.get("X-Rate-Limit-Retry-After-Seconds", "N/A")

        print(f"Request {i:3} | Status: {response.status_code} | Remaining: {remaining}", end="")

        if response.status_code == 429:
            print(f" | Retry after: {retry_after}s")
            print("-" * 65)
            print(f"[OK] Rate limit triggered at request {i}")
            return
        else:
            print()
    print("-" * 65)
    print(f"[INFO] Rate limit not triggered after {requests_count} requests")


if __name__ == "__main__":
    token = get_token()
    if not token:
        print("[ERROR] Cannot proceed without token — is Keycloak running?")
        sys.exit(1)
    test_rate_limit(token)