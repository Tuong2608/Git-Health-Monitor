"""Seed staging/demo environment with sample repositories and alert rules for RQ3 tasks T2..T7."""
import argparse
import json
import time
import urllib.error
import urllib.request
from pathlib import Path

DEFAULT_REPOS = (
    "https://github.com/spring-projects/spring-petclinic",
    "https://github.com/apache/commons-csv",
)


def call_api(base_url: str, method: str, path: str, body: dict | None = None) -> tuple[int, object]:
    url = base_url.rstrip("/") + "/api" + path
    payload = json.dumps(body).encode("utf-8") if body is not None else None
    req = urllib.request.Request(
        url,
        data=payload,
        method=method,
        headers={"Content-Type": "application/json", "Accept": "application/json"},
    )
    try:
        with urllib.request.urlopen(req, timeout=45) as resp:
            raw = resp.read().decode("utf-8")
            return resp.status, (json.loads(raw) if raw else None)
    except urllib.error.HTTPError as err:
        raw = err.read().decode("utf-8", errors="replace")
        try:
            parsed = json.loads(raw) if raw else {"raw": raw}
        except Exception:
            parsed = {"raw": raw}
        return err.code, parsed


def wait_for_health(base_url: str, timeout_sec: int = 120) -> None:
    deadline = time.monotonic() + timeout_sec
    url = base_url.rstrip("/") + "/api/health"
    while time.monotonic() < deadline:
        try:
            with urllib.request.urlopen(url, timeout=5) as resp:
                if resp.status == 200 and resp.read().strip() == b"OK":
                    return
        except Exception:
            pass
        time.sleep(2)
    raise RuntimeError(f"Timed out waiting for {url}")


def seed_repository(base_url: str, repo_url: str, observation_days: int, timeout_sec: int) -> dict:
    status, existing = call_api(base_url, "GET", "/repositories")
    if status != 200 or not isinstance(existing, list):
        raise RuntimeError(f"GET /repositories failed: {status} {existing}")

    repo = next((r for r in existing if str(r.get("url", "")).lower() == repo_url.lower()), None)
    if repo is None:
        status, created = call_api(base_url, "POST", "/repositories", {"url": repo_url})
        if status not in (200, 201) or not isinstance(created, dict):
            raise RuntimeError(f"POST /repositories failed for {repo_url}: {status} {created}")
        repo = created

    repo_id = repo["id"]
    call_api(
        base_url,
        "POST",
        f"/repositories/{repo_id}/alert-rules",
        {"type": "STRONG_COUPLING", "threshold": 0.5, "minShared": 5, "enabled": True},
    )
    call_api(
        base_url,
        "POST",
        f"/repositories/{repo_id}/alert-rules",
        {"type": "HOTSPOT_INCREASE", "threshold": 0.15, "enabled": True},
    )

    status, job = call_api(
        base_url,
        "POST",
        f"/repositories/{repo_id}/analyses",
        {"observationDays": observation_days},
    )
    if status != 202 or not isinstance(job, dict):
        raise RuntimeError(f"POST /repositories/{repo_id}/analyses failed: {status} {job}")

    job_id = job["jobId"]
    deadline = time.monotonic() + timeout_sec
    state = job
    while time.monotonic() < deadline:
        status, state = call_api(base_url, "GET", f"/jobs/{job_id}")
        if status == 200 and isinstance(state, dict) and state.get("status") in ("SUCCEEDED", "FAILED"):
            break
        time.sleep(2)

    if not isinstance(state, dict) or state.get("status") != "SUCCEEDED":
        raise RuntimeError(f"Analysis job {job_id} for {repo_url} did not succeed: {state}")

    snapshot_id = state["snapshotId"]
    _, snap = call_api(base_url, "GET", f"/snapshots/{snapshot_id}")
    _, hotspots = call_api(base_url, "GET", f"/snapshots/{snapshot_id}/hotspots?page=0&size=5")
    return {
        "repositoryUrl": repo_url,
        "repositoryId": repo_id,
        "jobId": job_id,
        "snapshotId": snapshot_id,
        "headSha": snap.get("headSha") if isinstance(snap, dict) else None,
        "summary": snap.get("summary") if isinstance(snap, dict) else None,
        "topHotspotCount": len(hotspots.get("items", [])) if isinstance(hotspots, dict) else 0,
    }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-url", default="http://127.0.0.1:5173", help="Base URL of frontend/staging proxy")
    parser.add_argument("--repos", nargs="*", default=list(DEFAULT_REPOS))
    parser.add_argument("--observation-days", type=int, default=365)
    parser.add_argument("--timeout-sec", type=int, default=300)
    parser.add_argument("--out-json", type=Path, default=None)
    args = parser.parse_args()

    wait_for_health(args.base_url)
    _, caps = call_api(args.base_url, "GET", "/analysis-capabilities")
    seeded = [seed_repository(args.base_url, r, args.observation_days, args.timeout_sec) for r in args.repos]
    report = {
        "baseUrl": args.base_url,
        "capabilities": caps,
        "seededRepositories": seeded,
        "note": "gs-rest-service is intentionally left unseeded so RQ3 participants can register & analyze it in Task T1.",
    }
    if args.out_json:
        args.out_json.parent.mkdir(parents=True, exist_ok=True)
        args.out_json.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
