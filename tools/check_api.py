#!/usr/bin/env python3
import argparse
import json
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path


DEFAULT_BASE_URL = "http://136.116.64.6"
DEFAULT_ORIGIN = "http://localhost:57323"
DEFAULT_TIMEOUT = 20


def build_url(base_url: str, path: str) -> str:
    return urllib.parse.urljoin(base_url.rstrip("/") + "/", path.lstrip("/"))


def shorten_text(text: str, limit: int = 220) -> str:
    text = " ".join(text.split())
    if len(text) <= limit:
        return text
    return text[: limit - 3] + "..."


def decode_body(raw: bytes) -> str:
    if not raw:
        return ""
    try:
        return raw.decode("utf-8")
    except UnicodeDecodeError:
        return raw.decode("latin-1", errors="replace")


def http_request(url: str, method: str = "GET", headers=None, body=None, timeout: int = DEFAULT_TIMEOUT):
    headers = headers or {}
    if isinstance(body, str):
        body = body.encode("utf-8")

    request = urllib.request.Request(url=url, data=body, headers=headers, method=method)
    started = time.perf_counter()
    try:
        with urllib.request.urlopen(request, timeout=timeout) as response:
            raw_body = response.read()
            duration_ms = round((time.perf_counter() - started) * 1000, 1)
            return {
                "ok": 200 <= response.status < 400,
                "status": response.status,
                "reason": getattr(response, "reason", ""),
                "duration_ms": duration_ms,
                "headers": dict(response.headers.items()),
                "body": decode_body(raw_body),
            }
    except urllib.error.HTTPError as error:
        raw_body = error.read()
        duration_ms = round((time.perf_counter() - started) * 1000, 1)
        return {
            "ok": False,
            "status": error.code,
            "reason": getattr(error, "reason", ""),
            "duration_ms": duration_ms,
            "headers": dict(error.headers.items()) if error.headers else {},
            "body": decode_body(raw_body),
        }
    except Exception as error:  # noqa: BLE001
        duration_ms = round((time.perf_counter() - started) * 1000, 1)
        return {
            "ok": False,
            "status": None,
            "reason": type(error).__name__,
            "duration_ms": duration_ms,
            "headers": {},
            "body": str(error),
        }


def load_openapi(base_url: str, timeout: int):
    return http_request(build_url(base_url, "/v3/api-docs"), timeout=timeout)


def login(base_url: str, email: str, password: str, origin: str, timeout: int):
    payload = json.dumps({"email": email, "password": password})
    result = http_request(
        build_url(base_url, "/api/v1/auth/login"),
        method="POST",
        headers={
            "Content-Type": "application/json",
            "Accept": "application/json",
            "Origin": origin,
        },
        body=payload,
        timeout=timeout,
    )

    token = None
    if result["status"] == 200:
        try:
            parsed = json.loads(result["body"] or "{}")
            token = parsed.get("token") or parsed.get("accessToken")
        except json.JSONDecodeError:
            token = None

    return result, token


def preflight(base_url: str, path: str, origin: str, request_method: str, timeout: int):
    return http_request(
        build_url(base_url, path),
        method="OPTIONS",
        headers={
            "Origin": origin,
            "Access-Control-Request-Method": request_method,
        },
        timeout=timeout,
    )


def materialize_path(path: str, sample_id: str):
    parts = []
    unresolved = []
    for segment in path.split("/"):
        if not segment:
            continue
        if segment.startswith("{") and segment.endswith("}"):
            unresolved.append(segment[1:-1])
            parts.append(sample_id)
        else:
            parts.append(segment)
    return "/" + "/".join(parts), unresolved


def build_check_list(openapi_doc: dict, sample_id: str):
    checks = []
    for path, methods in sorted(openapi_doc.get("paths", {}).items()):
        concrete_path, replaced_params = materialize_path(path, sample_id)
        for method, details in sorted(methods.items()):
            if method.lower() not in {"get", "post", "put", "patch", "delete"}:
                continue
            checks.append(
                {
                    "method": method.upper(),
                    "path": path,
                    "concrete_path": concrete_path,
                    "operation_id": details.get("operationId", ""),
                    "summary": details.get("summary", ""),
                    "replaced_params": replaced_params,
                    "requires_body": "requestBody" in details,
                    "security": details.get("security", []),
                }
            )
    return checks


def should_skip(check):
    public_auth_paths = {
        "/api/v1/auth/login",
        "/api/v1/auth/register",
        "/api/v1/auth/refresh",
        "/api/v1/auth/forgot-password",
        "/api/v1/auth/reset-password",
    }
    if check["path"] in {"/v3/api-docs"}:
        return "already checked separately"
    if check["concrete_path"] != check["path"] and not check["replaced_params"]:
        return "unresolved path parameters"
    if check["requires_body"] and check["path"] not in public_auth_paths:
        return "request body example not configured"
    return None


def run_endpoint_check(base_url: str, check: dict, token: str | None, origin: str, timeout: int):
    headers = {"Accept": "application/json", "Origin": origin}
    if token:
        headers["Authorization"] = f"Bearer {token}"

    body = None
    if check["path"] == "/api/v1/auth/refresh":
        body = json.dumps({"refreshToken": "placeholder"})
        headers["Content-Type"] = "application/json"
    elif check["path"] == "/api/v1/auth/register":
        body = json.dumps(
            {
                "email": "probe-user@example.com",
                "password": "Password123!",
                "firstName": "Probe",
                "lastName": "User",
                "role": "ROLE_STUDENT",
            }
        )
        headers["Content-Type"] = "application/json"
    elif check["path"] == "/api/v1/auth/forgot-password":
        query = urllib.parse.urlencode({"email": "missing@example.com"})
        return http_request(
            build_url(base_url, f"{check['concrete_path']}?{query}"),
            method=check["method"],
            headers=headers,
            timeout=timeout,
        )
    elif check["path"] == "/api/v1/auth/reset-password":
        query = urllib.parse.urlencode(
            {
                "token": "placeholder",
                "password": "Password123!",
                "confirmPassword": "Password123!",
            }
        )
        return http_request(
            build_url(base_url, f"{check['concrete_path']}?{query}"),
            method=check["method"],
            headers=headers,
            timeout=timeout,
        )

    return http_request(
        build_url(base_url, check["concrete_path"]),
        method=check["method"],
        headers=headers,
        body=body,
        timeout=timeout,
    )


def classify_result(result: dict):
    status = result.get("status")
    if status is None:
        return "network_error"
    if 200 <= status < 300:
        return "ok"
    if status in {401, 403}:
        return "auth_blocked"
    if status == 404:
        return "missing"
    if 400 <= status < 500:
        return "client_error"
    if status >= 500:
        return "server_error"
    return "other"


def print_summary(report: dict):
    print(f"Base URL: {report['base_url']}")
    print(f"OpenAPI: {report['openapi']['status']} in {report['openapi']['duration_ms']} ms")
    print(f"Login: {report['login']['status']} in {report['login']['duration_ms']} ms")
    if report["login"].get("diagnosis"):
        print(f"Login diagnosis: {report['login']['diagnosis']}")
    print(f"Total endpoint checks: {len(report['checks'])}")
    print()

    grouped = {}
    for item in report["checks"]:
        grouped[item["classification"]] = grouped.get(item["classification"], 0) + 1

    for key in sorted(grouped):
        print(f"{key}: {grouped[key]}")

    print()
    print("Problem endpoints:")
    problem_found = False
    for item in report["checks"]:
        if item["classification"] == "ok" or item["classification"] == "skipped":
            continue
        problem_found = True
        print(
            f"- {item['method']} {item['path']} -> {item['status']} {item['reason']} | "
            f"{item['classification']} | {item['note']}"
        )
    if not problem_found:
        print("- none")


def main():
    parser = argparse.ArgumentParser(description="Smoke-check EduPedu backend endpoints.")
    parser.add_argument("--base-url", default=DEFAULT_BASE_URL)
    parser.add_argument("--origin", default=DEFAULT_ORIGIN)
    parser.add_argument("--email", default="admin@edupage.com")
    parser.add_argument("--password", default="admin123")
    parser.add_argument("--timeout", type=int, default=DEFAULT_TIMEOUT)
    parser.add_argument("--sample-id", default="1")
    parser.add_argument("--output", default="")
    args = parser.parse_args()

    report = {
        "base_url": args.base_url.rstrip("/"),
        "origin": args.origin,
        "generated_at_epoch": int(time.time()),
        "openapi": {},
        "preflight": {},
        "login": {},
        "checks": [],
    }

    openapi_response = load_openapi(args.base_url, args.timeout)
    report["openapi"] = {
        "status": openapi_response["status"],
        "reason": openapi_response["reason"],
        "duration_ms": openapi_response["duration_ms"],
        "body_preview": shorten_text(openapi_response["body"]),
    }
    if openapi_response["status"] != 200:
        print("Failed to load /v3/api-docs", file=sys.stderr)
        print_summary(report)
        return 1

    try:
        openapi_doc = json.loads(openapi_response["body"])
    except json.JSONDecodeError:
        print("Invalid JSON in /v3/api-docs", file=sys.stderr)
        print_summary(report)
        return 1

    preflight_response = preflight(args.base_url, "/api/v1/auth/login", args.origin, "POST", args.timeout)
    report["preflight"] = {
        "status": preflight_response["status"],
        "reason": preflight_response["reason"],
        "duration_ms": preflight_response["duration_ms"],
        "allow_origin": preflight_response["headers"].get("Access-Control-Allow-Origin", ""),
        "allow_methods": preflight_response["headers"].get("Access-Control-Allow-Methods", ""),
    }

    login_response, token = login(args.base_url, args.email, args.password, args.origin, args.timeout)
    diagnosis = ""
    if login_response["status"] == 403:
        diagnosis = "Auth endpoint rejected the request. Check deployed build, auth config, and whether this user exists in the production DB."
    elif login_response["status"] == 401:
        diagnosis = "Credentials were rejected. Check email/password or the stored user record."
    elif login_response["status"] is None:
        diagnosis = "Network or timeout failure while trying to log in."
    elif login_response["status"] == 200 and not token:
        diagnosis = "Login returned 200 but no access token was found in the JSON response."

    report["login"] = {
        "status": login_response["status"],
        "reason": login_response["reason"],
        "duration_ms": login_response["duration_ms"],
        "diagnosis": diagnosis,
        "body_preview": shorten_text(login_response["body"]),
        "has_token": bool(token),
    }

    checks = build_check_list(openapi_doc, args.sample_id)
    for check in checks:
        skip_reason = should_skip(check)
        if skip_reason:
            report["checks"].append(
                {
                    "method": check["method"],
                    "path": check["path"],
                    "status": None,
                    "reason": "",
                    "classification": "skipped",
                    "note": skip_reason,
                }
            )
            continue

        result = run_endpoint_check(args.base_url, check, token, args.origin, args.timeout)
        note_parts = []
        if check["replaced_params"]:
            note_parts.append(f"used sample id={args.sample_id} for {', '.join(check['replaced_params'])}")
        if result["body"]:
            note_parts.append(shorten_text(result["body"]))

        report["checks"].append(
            {
                "method": check["method"],
                "path": check["path"],
                "status": result["status"],
                "reason": result["reason"],
                "classification": classify_result(result),
                "duration_ms": result["duration_ms"],
                "note": " | ".join(note_parts),
            }
        )

    if args.output:
        output_path = Path(args.output)
        output_path.write_text(json.dumps(report, indent=2), encoding="utf-8")

    print_summary(report)
    if args.output:
        print()
        print(f"Saved full report to {args.output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
