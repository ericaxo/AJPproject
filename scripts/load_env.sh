#!/usr/bin/env bash
set -euo pipefail

# Load environment variables from .env into the current shell
# Usage: source scripts/load_env.sh

ENV_FILE="$(cd "$(dirname "$0")/.." && pwd)/.env"

if [ ! -f "$ENV_FILE" ]; then
  echo ".env file not found at $ENV_FILE. Skipping env load."
  return 0 2>/dev/null || exit 0
fi

# Export all non-comment lines of the form KEY=VALUE
while IFS= read -r line; do
  # skip comments and empty lines
  [[ -z "$line" || "$line" =~ ^[[:space:]]*# ]] && continue
  # Only process KEY=VALUE lines
  if [[ "$line" =~ ^[A-Za-z_][A-Za-z0-9_]*= ]]; then
    export "$line"
  fi
done < "$ENV_FILE"

echo "Loaded environment variables from .env"




