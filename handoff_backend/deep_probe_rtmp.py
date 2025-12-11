#!/usr/bin/env python3
"""Parallel RTMP probe using ffmpeg with -rtmp_transport tcp.

Usage examples:
  python tools/deep_probe_rtmp.py --urls "rtmp://host/live/a,rtmp://host/live/b" --probe-duration 6 --workers 3 --output runs/probe_rtmp_tcp.json

The script runs ffmpeg for a short duration per URL, captures stderr, and writes a JSON summary.
"""
import argparse
import json
import subprocess
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path


def probe_one(url: str, probe_duration: int, timeout: int) -> dict:
    start = time.time()
    cmd = [
        "ffmpeg",
        "-hide_banner",
        "-loglevel",
        "info",
        "-rtmp_transport",
        "tcp",
        "-i",
        url,
        "-t",
        str(probe_duration),
        "-f",
        "null",
        "-",
    ]
    result = {
        "url": url,
        "cmd": " ".join(cmd),
        "returncode": None,
        "ok": False,
        "stderr": "",
        "elapsed": None,
    }
    try:
        proc = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=timeout)
        rc = proc.returncode
        stderr = proc.stderr.decode(errors="ignore")
        result.update({"returncode": rc, "stderr": stderr})
        result["ok"] = (rc == 0)

        # if ffmpeg rejected -rtmp_transport (older build/wrapped ffmpeg), try fallback without that option
        if "Unrecognized option 'rtmp_transport'" in stderr or "Option not found" in stderr:
            fallback_cmd = [
                "ffmpeg",
                "-hide_banner",
                "-loglevel",
                "info",
                "-i",
                url,
                "-t",
                str(probe_duration),
                "-f",
                "null",
                "-",
            ]
            try:
                proc2 = subprocess.run(fallback_cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=timeout)
                rc2 = proc2.returncode
                stderr2 = proc2.stderr.decode(errors="ignore")
            except subprocess.TimeoutExpired:
                rc2 = None
                stderr2 = f"fallback ffmpeg timeout after {timeout}s"
            except FileNotFoundError:
                rc2 = None
                stderr2 = "ffmpeg executable not found in PATH (fallback)"
            except Exception as e:
                rc2 = None
                stderr2 = f"exception in fallback: {e}"

            # append fallback stderr and update ok/returncode
            combined = stderr + "\n---- fallback stderr ----\n" + stderr2
            result.update({"stderr": combined, "returncode": rc if rc is not None else rc2})
            if rc2 == 0:
                result["ok"] = True
                result["returncode"] = rc2
    except subprocess.TimeoutExpired as e:
        # attempt to kill if still running
        try:
            e.proc.kill()
        except Exception:
            pass
        result.update({"returncode": None, "stderr": f"ffmpeg timeout after {timeout}s"})
        result["ok"] = False
    except FileNotFoundError:
        result.update({"returncode": None, "stderr": "ffmpeg executable not found in PATH"})
        result["ok"] = False
    except Exception as e:
        result.update({"returncode": None, "stderr": f"exception: {e}"})
        result["ok"] = False
    finally:
        result["elapsed"] = round(time.time() - start, 2)
    return result


def run_parallel(urls, probe_duration=5, timeout=12, workers=4):
    results = []
    with ThreadPoolExecutor(max_workers=workers) as ex:
        futures = {ex.submit(probe_one, url, probe_duration, timeout): url for url in urls}
        for fut in as_completed(futures):
            try:
                res = fut.result()
            except Exception as e:
                res = {"url": futures[fut], "ok": False, "stderr": f"exception in thread: {e}", "returncode": None, "elapsed": None}
            results.append(res)
    return results


def main():
    p = argparse.ArgumentParser(description="Parallel RTMP probe using ffmpeg (-rtmp_transport tcp)")
    p.add_argument("--urls", type=str, default="", help="Comma-separated RTMP URLs to probe")
    p.add_argument("--file", type=str, help="Path to text file with one URL per line")
    p.add_argument("--probe-duration", type=int, default=6, help="ffmpeg -t seconds to read")
    p.add_argument("--timeout", type=int, default=12, help="subprocess timeout per probe (seconds)")
    p.add_argument("--workers", type=int, default=4, help="parallel workers")
    p.add_argument("--output", type=str, default="runs/probe_rtmp_tcp.json", help="JSON output path")
    args = p.parse_args()

    urls = []
    if args.urls:
        parts = [u.strip() for u in args.urls.split(",") if u.strip()]
        urls.extend(parts)
    if args.file:
        fp = Path(args.file)
        if fp.exists():
            with fp.open("r", encoding="utf-8") as f:
                for line in f:
                    line = line.strip()
                    if line:
                        urls.append(line)
    urls = list(dict.fromkeys(urls))
    if not urls:
        print("No URLs provided. Use --urls or --file.")
        return

    outp = Path(args.output)
    outp.parent.mkdir(parents=True, exist_ok=True)

    print(f"Probing {len(urls)} URL(s) with -rtmp_transport tcp using {args.workers} workers...")
    results = run_parallel(urls, probe_duration=args.probe_duration, timeout=args.timeout, workers=args.workers)

    # sort by ok desc then url
    results_sorted = sorted(results, key=lambda r: (not r.get("ok", False), r.get("url", "")))

    with outp.open("w", encoding="utf-8") as f:
        json.dump({"probed_at": time.time(), "results": results_sorted}, f, ensure_ascii=False, indent=2)

    for r in results_sorted:
        status = "OK" if r.get("ok") else "FAIL"
        print(f"[{status}] {r.get('url')} (elapsed={r.get('elapsed')}s) returncode={r.get('returncode')}")
        # print small stderr snippet
        stderr = r.get("stderr", "") or ""
        snippet = stderr.strip().splitlines()[-8:]
        if snippet:
            print("  stderr (tail):\n    " + "\n    ".join(snippet))
        else:
            print("  stderr: <empty>")


if __name__ == "__main__":
    main()
