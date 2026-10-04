#!/usr/bin/env sh
set -eu

IMAGE_NAME="rulerefactor-build"
RUNTIME_IMAGE_NAME="rulerefactor"
MAX_RETRIES="4"

retry() {
  attempt=1
  while [ "$attempt" -le "$MAX_RETRIES" ]; do
    if "$@"; then
      return 0
    fi
    echo "Attempt ${attempt}/${MAX_RETRIES} failed. Retrying in 5s..."
    attempt=$((attempt + 1))
    sleep 5
  done
  return 1
}

echo "Building test/package stage..."
retry docker build --target build -t "${IMAGE_NAME}" .

echo "Building runtime image..."
retry docker build -t "${RUNTIME_IMAGE_NAME}" .

echo "Done. Tests passed and jar packaged in Docker build."
echo "Run with: docker run --rm ${RUNTIME_IMAGE_NAME} --help"
