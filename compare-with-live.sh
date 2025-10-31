#!/bin/bash

# Script to download live XML files and compare them with locally generated files

REPO_BASE="https://eve.nikr.net/jeveassets/update/data/data"
LOCAL_DIR="target/data"
COMPARE_DIR="target/data/compare-live"

# Create comparison directory
mkdir -p "$COMPARE_DIR"

# List of XML files to compare
FILES=("locations.xml" "jumps.xml" "flags.xml" "items.xml" "agents.xml" "npccorporation.xml")

echo "Downloading live XML files and comparing with local versions..."
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
        # Compare files
        if diff -q "$local_file" "$live_file" > /dev/null 2>&1; then
            echo "  ✅ Files are identical"
        else
            echo "  ⚠️  Files different!"
            echo ""
            echo "  Differences:"
            diff -u "$live_file" "$local_file" | head -50
            echo ""
            echo "  (Showing first 50 lines of diff. Full diff saved to: $live_file.diff)"
            diff -u "$live_file" "$local_file" > "$live_file.diff" 2>&1
            echo ""
        fi
    else
        echo "  ❌ Failed to download live version"
    fi
    echo ""
done

echo "Comparison complete!"
echo "Live files downloaded to: $COMPARE_DIR"
echo "Diff files saved to: $COMPARE_DIR/*.diff"

