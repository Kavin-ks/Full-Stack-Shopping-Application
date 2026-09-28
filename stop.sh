#!/usr/bin/env bash
# Stops everything started by start.sh
ROOT="$(cd "$(dirname "$0")" && pwd)"
for f in "$ROOT"/backend/logs/*.pid; do
  [ -f "$f" ] || continue
  pid=$(cat "$f")
  kill "$pid" 2>/dev/null && echo "Stopped $(basename "$f" .pid)"
  rm -f "$f"
done
# Fallback: only this project's own processes (never other apps that share the ports)
pkill -f "$ROOT/backend/.*/target/.*-1.0.0.jar" 2>/dev/null
pkill -f "$ROOT/frontend/node_modules/.bin/vite" 2>/dev/null
true
