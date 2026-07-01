#!/usr/bin/env bash
# Runs the .hurl flow tests under this directory, each with a fresh
# {{run_id}} so trace/event IDs never collide with a previous run.
#
# Usage:
#   ./run-tests.sh                 # run every test folder
#   ./run-tests.sh <folder-name>   # run a single test folder, e.g.:
#                                  #   ./run-tests.sh started-to-error
#   ./run-tests.sh <path/to/file.hurl>

set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$script_dir"

generate_run_id() {
  echo "$(date +%s%N)-${RANDOM}"
}

run_test() {
  local hurl_file="$1"
  local run_id
  run_id="$(generate_run_id)"
  echo "==> $(basename "$hurl_file")  (run_id=${run_id})"
  hurl --test --variable run_id="${run_id}" "$hurl_file"
}

run_folder() {
  local dir="$1"
  local file
  for file in "$dir"/*.hurl; do
    [[ -f "$file" ]] || continue
    run_test "$file"
  done
}

status=0

if [[ $# -gt 0 ]]; then
  target="$1"
  if [[ -f "$target" ]]; then
    run_test "$target" || status=1
  elif [[ -d "$target" ]]; then
    run_folder "$target" || status=1
  elif [[ -d "$script_dir/$target" ]]; then
    run_folder "$script_dir/$target" || status=1
  else
    echo "No such test file or folder: $target" >&2
    exit 1
  fi
else
  for dir in */; do
    dir="${dir%/}"
    [[ -d "$dir" ]] || continue
    run_folder "$dir" || status=1
  done
fi

exit "$status"
