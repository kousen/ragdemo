#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/.env"

if [[ ! -f "$ENV_FILE" && -f "$SCRIPT_DIR/../.env" ]]; then
  ENV_FILE="$SCRIPT_DIR/../.env"
fi

if [[ -f "$ENV_FILE" ]]; then
  set -a
  source "$ENV_FILE"
  set +a
fi

exec "$SCRIPT_DIR/gradlew" bootRun "$@"
