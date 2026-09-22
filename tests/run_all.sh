#!/bin/bash
# Runs all test_*.sh scripts in this directory and reports a summary.
set -u
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
export BASE_URL=${BASE_URL:-http://localhost:22315}

PASS_COUNT=0
FAIL_COUNT=0
FAILED_SCRIPTS=()

for script in "$SCRIPT_DIR"/test_*.sh; do
  [ -e "$script" ] || continue
  echo "==== Running $(basename "$script") ===="
  OUTPUT=$(bash "$script")
  RC=$?
  echo "$OUTPUT"
  if [ $RC -eq 0 ] && echo "$OUTPUT" | tail -n1 | grep -q "^PASSED$"; then
    PASS_COUNT=$((PASS_COUNT + 1))
  else
    FAIL_COUNT=$((FAIL_COUNT + 1))
    FAILED_SCRIPTS+=("$(basename "$script")")
  fi
  echo ""
done

echo "==== Summary ===="
echo "PASSED: $PASS_COUNT"
echo "FAILED: $FAIL_COUNT"
if [ $FAIL_COUNT -gt 0 ]; then
  echo "Failed scripts: ${FAILED_SCRIPTS[*]}"
  exit 1
fi
exit 0
