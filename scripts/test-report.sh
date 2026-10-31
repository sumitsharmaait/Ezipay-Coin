#!/usr/bin/env bash
# Builds a test report from the results Gradle already wrote. It does not run any tests.
#
#   ./gradlew testDebugUnitTest jacocoTestReport          # unit tests + coverage
#   ./gradlew connectedDebugAndroidTest                   # optional, needs an emulator or device
#   scripts/test-report.sh
#
# Output: docs/test-reports/latest.md and docs/test-reports/<UTC-date>-<git-sha>.md
# Env: DEVICE_NAME (label for the connected-test device), REPORT_DIR (default docs/test-reports).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

REPORT_DIR="${REPORT_DIR:-$ROOT/docs/test-reports}"
UNIT_DIR="app/build/test-results/testDebugUnitTest"
CONNECTED_DIR="app/build/outputs/androidTest-results/connected"
JACOCO_XML="app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml"

if ! ls "$UNIT_DIR"/TEST-*.xml >/dev/null 2>&1; then
  echo "No unit test results in $UNIT_DIR. Run: ./gradlew testDebugUnitTest jacocoTestReport" >&2
  exit 1
fi

SHA="$(git rev-parse --short=9 HEAD)"
BRANCH="$(git rev-parse --abbrev-ref HEAD)"
DATE_UTC="$(date -u +%Y-%m-%d)"
NOW_UTC="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
JDK="$(java -version 2>&1 | head -1)"
GRADLE_V="$(grep -o 'gradle-[0-9.]*' gradle/wrapper/gradle-wrapper.properties | head -1 | sed 's/gradle-//')"
AGP_V="$(grep -E '^agp *=' gradle/libs.versions.toml | sed -E 's/.*"(.*)".*/\1/')"
KOTLIN_V="$(grep -E '^kotlin *=' gradle/libs.versions.toml | sed -E 's/.*"(.*)".*/\1/')"
DIRTY=""; [ -n "$(git status --porcelain -- app scripts gradle 2>/dev/null)" ] && DIRTY=" (uncommitted changes)"

mkdir -p "$REPORT_DIR"
OUT="$REPORT_DIR/$DATE_UTC-$SHA.md"

python3 -I - "$UNIT_DIR" "$CONNECTED_DIR" "$JACOCO_XML" "$OUT" "$SHA$DIRTY" "$BRANCH" "$NOW_UTC" "$JDK" "$GRADLE_V" "$AGP_V" "$KOTLIN_V" "${DEVICE_NAME:-}" <<'PY'
import glob, os, sys
import xml.etree.ElementTree as ET

unit_dir, conn_dir, jacoco_xml, out, sha, branch, now, jdk, gradle, agp, kotlin, device_env = sys.argv[1:13]

def load(paths):
    suites = []
    for p in sorted(paths):
        root = ET.parse(p).getroot()
        nodes = [root] if root.tag == "testsuite" else root.findall("testsuite")
        for ts in nodes:
            cases = []
            for tc in ts.findall("testcase"):
                if tc.find("failure") is not None or tc.find("error") is not None:
                    status = "FAILED"
                elif tc.find("skipped") is not None:
                    status = "SKIPPED"
                else:
                    status = "passed"
                cases.append((tc.get("name"), status, float(tc.get("time") or 0)))
            suites.append({"name": ts.get("name"), "cases": cases,
                           "time": float(ts.get("time") or 0),
                           "props": {p.get("name"): p.get("value") for p in ts.findall("properties/property")}})
    return suites

def totals(suites):
    n = sum(len(s["cases"]) for s in suites)
    f = sum(1 for s in suites for c in s["cases"] if c[1] == "FAILED")
    k = sum(1 for s in suites for c in s["cases"] if c[1] == "SKIPPED")
    return dict(suites=len(suites), tests=n, passed=n - f - k, failed=f, skipped=k,
                time=sum(s["time"] for s in suites))

def short(name):
    return name.rsplit(".", 1)[-1]

unit = load(glob.glob(os.path.join(unit_dir, "TEST-*.xml")))
conn = load(glob.glob(os.path.join(conn_dir, "**", "TEST-*.xml"), recursive=True))

L = []
w = L.append
w("# Test report")
w("")
w(f"Generated {now} from `{branch}` @ `{sha}`.")
w("")
w("## Summary")
w("")
w("| Type | Suites | Tests | Passed | Failed | Skipped | Duration |")
w("|---|---:|---:|---:|---:|---:|---:|")
rows = [("Unit (JVM)", totals(unit))]
rows.append(("Instrumented (device)", totals(conn)) if conn else None)
for r in rows:
    if r is None:
        continue
    name, t = r
    w(f"| {name} | {t['suites']} | {t['tests']} | {t['passed']} | {t['failed']} | {t['skipped']} | {t['time']:.1f}s |")
if not conn:
    w("")
    w("Instrumented tests were not part of this run (`connectedDebugAndroidTest` results not found).")
w("")

# ---- coverage ----
w("## Coverage (debug unit tests)")
w("")
if os.path.exists(jacoco_xml):
    root = ET.parse(jacoco_xml).getroot()
    def pct(counter):
        if counter is None:
            return "n/a"
        m, c = int(counter.get("missed")), int(counter.get("covered"))
        return f"{100.0 * c / (m + c):.1f}% ({c}/{m + c})" if m + c else "n/a"
    def counters(node):
        return {c.get("type"): c for c in node.findall("counter")}
    top = counters(root)
    w("| Scope | Line | Branch | Method |")
    w("|---|---:|---:|---:|")
    w(f"| **Overall** | {pct(top.get('LINE'))} | {pct(top.get('BRANCH'))} | {pct(top.get('METHOD'))} |")
    pkgs = []
    for p in root.findall("package"):
        c = counters(p)
        line = c.get("LINE")
        lines = (int(line.get("missed")) + int(line.get("covered"))) if line is not None else 0
        pkgs.append((p.get("name").replace("/", "."), c, lines))
    keys = ("utils.validation", "presentation.dashboard.pay", "presentation.shared", "presentation.profile",
            "presentation.transactions", "data.repository", "data.remote.api", "data.remote.dto")
    shown = [x for x in pkgs if any(x[0].endswith(k) for k in keys)]
    for name, c, _ in sorted(shown):
        w(f"| `{name.replace('com.app.ezipaycoin.', '')}` | {pct(c.get('LINE'))} | {pct(c.get('BRANCH'))} | {pct(c.get('METHOD'))} |")
    w("")
    w("Classes with no executed code are included in the overall figure. The HTML report is at "
      "`app/build/reports/jacoco/jacocoTestReport/html/index.html` after a run.")
else:
    w("No jacoco XML found. Run `./gradlew jacocoTestReport`.")
w("")

# ---- environment ----
w("## Environment")
w("")
w("| | |")
w("|---|---|")
w(f"| Git | `{branch}` @ `{sha}` |")
w(f"| JDK | {jdk} |")
w(f"| Gradle | {gradle} |")
w(f"| Android Gradle Plugin | {agp} |")
w(f"| Kotlin | {kotlin} |")
w(f"| OS | {os.uname().sysname} {os.uname().release} ({os.uname().machine}) |")
if conn:
    devs = sorted({s["props"].get("device") or s["props"].get("deviceName") or "" for s in conn} - {""})
    dev = device_env or ", ".join(devs) or "not recorded"
    w(f"| Device (instrumented) | {dev} |")
w("")

# ---- per suite ----
def section(title, suites):
    w(f"## {title}")
    w("")
    for s in sorted(suites, key=lambda s: s["name"]):
        t = totals([s])
        w(f"### {short(s['name'])}")
        w("")
        w(f"`{s['name']}` - {t['tests']} tests, {t['passed']} passed, {t['failed']} failed, {t['skipped']} skipped, {t['time']:.2f}s")
        w("")
        w("| Test | Status | Time |")
        w("|---|---|---:|")
        for name, status, secs in s["cases"]:
            w(f"| {name} | {status} | {secs:.3f}s |")
        w("")

section("Unit tests", unit)
if conn:
    section("Instrumented tests", conn)

with open(out, "w") as f:
    f.write("\n".join(L) + "\n")

t = totals(unit)
print(f"unit: {t['tests']} tests, {t['failed']} failed, {t['skipped']} skipped")
if conn:
    t = totals(conn)
    print(f"instrumented: {t['tests']} tests, {t['failed']} failed, {t['skipped']} skipped")
PY

cp "$OUT" "$REPORT_DIR/latest.md"
echo "wrote $OUT"
echo "wrote $REPORT_DIR/latest.md"
