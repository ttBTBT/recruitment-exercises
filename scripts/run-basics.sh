#!/usr/bin/env bash
set -euo pipefail
repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
main_class="${1:-HelloWorld}"
if (( $# > 0 )); then shift; fi
case "$main_class" in
  HelloWorld|ProductCalculation|Toolbox|OopDemo|PaymentDemo|OrderDemo|FileDemo|TeaDemo|BoundaryTests) ;;
  *) echo 'Unknown main class.' >&2; exit 1 ;;
esac
classes="$repo_root/build/classes/all"
temp_directory="$repo_root/build/temp"
mkdir -p "$classes" "$temp_directory"
sources=()
while IFS= read -r -d '' source_file; do sources+=("$source_file"); done < <(find "$repo_root/questions" -path '*/src/*' -name '*.java' -print0)
sources+=("$repo_root/tests/basics/src/openatom/basics/BoundaryTests.java")
compiler="${JAVA_HOME:+$JAVA_HOME/bin/}javac"
runtime="${JAVA_HOME:+$JAVA_HOME/bin/}java"
"$compiler" -encoding UTF-8 --release 17 -d "$classes" "${sources[@]}"
"$runtime" -Dfile.encoding=UTF-8 "-Djava.io.tmpdir=$temp_directory" -cp "$classes" "openatom.basics.$main_class" "$@"
