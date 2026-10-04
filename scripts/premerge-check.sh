#!/bin/zsh

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
REQUEST_FILE="$ROOT_DIR/docs/request-premerge.json"
OUTPUT_FILE="$ROOT_DIR/target/premerge-agent-response.json"
JAR_FILE="$ROOT_DIR/target/rulerefactor-0.1.0-SNAPSHOT.jar"

if [[ ! -f "$REQUEST_FILE" ]]; then
  echo "Missing request file: $REQUEST_FILE"
  exit 2
fi

echo "[premerge] Building project"
"$ROOT_DIR/mvnw" -q -f "$ROOT_DIR/pom.xml" clean package

echo "[premerge] Running agent-api premerge payload"
java -jar "$JAR_FILE" agent-api --input "$REQUEST_FILE" > "$OUTPUT_FILE"

echo "[premerge] Parsing result"
FAILED="$(python3 - <<'PY' "$OUTPUT_FILE"
import json
import sys
with open(sys.argv[1], 'r', encoding='utf-8') as f:
    data = json.load(f)
print(int(data.get('failed', 0)))
PY
)"

APPLIED="$(python3 - <<'PY' "$OUTPUT_FILE"
import json
import sys
with open(sys.argv[1], 'r', encoding='utf-8') as f:
    data = json.load(f)
print(int(data.get('applied', 0)))
PY
)"

echo "[premerge] applied=$APPLIED failed=$FAILED"

if [[ "$FAILED" -gt 0 ]]; then
  echo "[premerge] FAILED: verification errors detected"
  exit 1
fi

echo "[premerge] OK"
