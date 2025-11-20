#!/bin/bash

# Script to download live XML files and compare them with locally generated files,
# treating files as identical if they contain the same <row .../> content regardless of order.

set -uo pipefail

REPO_BASE="https://eve.nikr.net/jeveassets/update/data/data"
LOCAL_DIR="target/data"
COMPARE_DIR="target/data/compare-live"
NORM_DIR="$COMPARE_DIR/normalized"

mkdir -p "$COMPARE_DIR" "$NORM_DIR"

FILES=("jumps.xml" "flags.xml" "items.xml" "agents.xml" "npccorporation.xml" "locations.xml")

have_xmllint() { command -v xmllint >/dev/null 2>&1; }

# Normalize: extract <row .../> entries, canonicalize (when possible), and sort
normalize_rows() {
  local in_file="$1"
  local out_file="$2"

  # Try robust XML extraction if xmllint is available
  if have_xmllint; then
    # 1) Extract all <row ...> nodes
    # 2) Pretty-format to stabilize whitespace
    # 3) Reduce each to a single self-closing line <row .../>
    # 4) Collapse repeated spaces inside attributes
    # 5) Sort for order-insensitive comparison
    if xmllint --xpath '//*[local-name()="row"]' "$in_file" 2>/dev/null \
      | xmllint --format - 2>/dev/null \
      | sed -n 's/^[[:space:]]*<row\([^>]*\)\/>.*/<row\1\/>/p' \
      | sed -E 's/[[:space:]]+/ /g; s/ >/>/g' \
      | sort > "$out_file"; then
      return 0
    fi
  fi

  # Fallback (no xmllint or parse failed): grep row lines directly
  # - Extracts lines containing <row .../>
  # - Collapses whitespace and sorts
  grep -o '<row[^>]*/>' "$in_file" 2>/dev/null \
    | sed -E 's/[[:space:]]+/ /g; s/ >/>/g' \
    | sort > "$out_file" || true
}

echo "Downloading live XML files and comparing (order-insensitive by <row .../>)..."
echo ""

for file in "${FILES[@]}"; do
  local_file="$LOCAL_DIR/$file"
  live_file="$COMPARE_DIR/$file"

  if [ ! -f "$local_file" ]; then
    echo "⚠️  Local file not found: $local_file"
    continue
  fi

  echo "Processing: $file"

  # Download live version
  if curl -s -f -o "$live_file" "$REPO_BASE/$file"; then
    # Normalize both files to sorted <row .../> lists
    local_norm="$NORM_DIR/${file%.xml}.local.rows"
    live_norm="$NORM_DIR/${file%.xml}.live.rows"

    normalize_rows "$local_file" "$local_norm"
    normalize_rows "$live_file" "$live_norm"

    # If both normalized outputs are empty, fall back to a plain diff
    if [ ! -s "$local_norm" ] && [ ! -s "$live_norm" ]; then
      if diff -q "$local_file" "$live_file" > /dev/null 2>&1; then
        echo "  ✅ Files are identical (raw)"
      else
        echo "  ⚠️  Files differ (raw). Falling back to raw diff preview:"
        diff -u "$live_file" "$local_file" | head -50
        diff -u "$live_file" "$local_file" > "$live_file.diff" 2>&1
        echo "  Full raw diff saved to: $live_file.diff"
      fi
      echo ""
      continue
    fi

    # Compare normalized row sets
    if diff -q "$local_norm" "$live_norm" > /dev/null 2>&1; then
      echo "  ✅ Files are identical by <row .../> content (order-insensitive)"
    else
      echo "  ⚠️  Files different by <row .../> content!"
      echo ""
      echo "  Differences (normalized rows, order-insensitive):"
      diff -u "$live_norm" "$local_norm" | head -50
      echo ""
      norm_diff="$NORM_DIR/${file%.xml}.rows.diff"
      diff -u "$live_norm" "$local_norm" > "$norm_diff" 2>&1
      echo "  (Showing first 50 lines.) Full normalized diff saved to: $norm_diff"
    fi
  else
    echo "  ❌ Failed to download live version"
  fi

  echo ""
done

echo "Comparison complete!"
echo "Live files: $COMPARE_DIR"
echo "Normalized rows: $NORM_DIR"
echo "Diffs: $NORM_DIR/*.rows.diff (order-insensitive)"
