"""Measure REST API latency for NFR01 (POST /analyses) and NFR02 (paged hotspot/file queries) on a live backend."""
import argparse
import json
import statistics
import time
import urllib.error
import urllib.request
from datetime import datetime, timezone
from pathlib import Path


def call(base: str, method: str, path: str, body: dict | None = None) -> tuple[int, object, float]:
    data = json.dumps(body).encode("utf-8") if body is not None else None
    req = urllib.request.Request(base.rstrip("/") + "/api" + path, data=data, method=method,
                                 headers={"Content-Type": "application/json"})
    t0 = time.perf_counter()
    try:
        with urllib.request.urlopen(req, timeout=60) as resp:
            raw = resp.read()
            status = resp.status
    except urllib.error.HTTPError as err:
        raw = err.read()
        status = err.code
    ms = (time.perf_counter() - t0) * 1000.0
    try:
        parsed = json.loads(raw) if raw else None
    except ValueError:
        parsed = raw.decode("utf-8", errors="replace")
    return status, parsed, ms


def summarize(samples: list[float]) -> dict:
    ordered = sorted(samples)
    p95 = ordered[min(len(ordered) - 1, int(round(0.95 * (len(ordered) - 1))))]
    return {
        "n": len(ordered),
        "medianMs": round(statistics.median(ordered), 2),
        "p95Ms": round(p95, 2),
        "maxMs": round(ordered[-1], 2),
        "minMs": round(ordered[0], 2),
    }


def wait_health(base: str, timeout: int = 120) -> None:
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        try:
            with urllib.request.urlopen(base.rstrip("/") + "/api/health", timeout=3) as resp:
                if resp.status == 200:
                    return
        except Exception:
            pass
        time.sleep(1)
    raise RuntimeError("backend did not become healthy")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-url", default="http://127.0.0.1:8080")
    parser.add_argument("--repos", nargs="+", required=True)
    parser.add_argument("--observation-days", type=int, default=180)
    parser.add_argument("--repeats", type=int, default=30)
    parser.add_argument("--out-json", type=Path, required=True)
    args = parser.parse_args()

    wait_health(args.base_url)
    status, caps, _ = call(args.base_url, "GET", "/analysis-capabilities")
    if status != 200 or not isinstance(caps, dict) or not caps.get("canAnalyze"):
        raise RuntimeError(f"analysis disabled: {status} {caps}")

    enqueue_accepted, enqueue_conflict, jobs = [], [], []
    for url in args.repos:
        status, repo, _ = call(args.base_url, "POST", "/repositories", {"url": url})
        if status not in (200, 201):
            raise RuntimeError(f"register failed {url}: {status} {repo}")
        status, job, ms = call(args.base_url, "POST", f"/repositories/{repo['id']}/analyses",
                               {"observationDays": args.observation_days})
        if status != 202:
            raise RuntimeError(f"enqueue failed {url}: {status} {job}")
        enqueue_accepted.append(ms)
        status, dup, ms2 = call(args.base_url, "POST", f"/repositories/{repo['id']}/analyses",
                                {"observationDays": args.observation_days})
        if status == 409:
            enqueue_conflict.append(ms2)
        jobs.append((url, repo["id"], job["jobId"]))

    results = []
    for url, repo_id, job_id in jobs:
        deadline = time.monotonic() + 600
        t_start = time.monotonic()
        state = {}
        while time.monotonic() < deadline:
            _, state, _ = call(args.base_url, "GET", f"/jobs/{job_id}")
            if isinstance(state, dict) and state.get("status") in ("SUCCEEDED", "FAILED"):
                break
            time.sleep(1)
        if state.get("status") != "SUCCEEDED":
            raise RuntimeError(f"job {job_id} for {url} did not succeed: {state}")
        snapshot = state["snapshotId"]
        hotspot_ms, file_ms, page_size = [], [], 0
        first_file = None
        for _ in range(args.repeats):
            status, page, ms = call(args.base_url, "GET", f"/snapshots/{snapshot}/hotspots?page=0&size=100")
            assert status == 200, (status, page)
            hotspot_ms.append(ms)
            page_size = len(page["items"])
            if page["items"]:
                first_file = page["items"][0]["fileId"]
        if first_file is not None:
            for _ in range(args.repeats):
                status, _, ms = call(args.base_url, "GET", f"/snapshots/{snapshot}/files/{first_file}")
                assert status == 200
                file_ms.append(ms)
        results.append({
            "repository": url,
            "jobId": job_id,
            "snapshotId": snapshot,
            "observedJobCompletionSeconds": round(time.monotonic() - t_start, 2),
            "hotspotPageItems": page_size,
            "getHotspotsSize100": summarize(hotspot_ms),
            "getFileDetail": summarize(file_ms) if file_ms else None,
        })

    report = {
        "schemaVersion": 1,
        "kind": "NFR01_NFR02_API_LATENCY",
        "measuredAtUtc": datetime.now(timezone.utc).isoformat(),
        "baseUrl": args.base_url,
        "scopeNote": "Local single-instance backend + PostgreSQL 18 on the student laptop; HTTP measured client-side including JSON parsing. Not a load test.",
        "nfr01PostAnalysesAccepted202": summarize(enqueue_accepted),
        "nfr01PostAnalysesDuplicate409": summarize(enqueue_conflict) if enqueue_conflict else None,
        "nfr01TargetMs": 2000,
        "nfr02TargetMs": 1000,
        "repositories": results,
    }
    args.out_json.parent.mkdir(parents=True, exist_ok=True)
    args.out_json.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
