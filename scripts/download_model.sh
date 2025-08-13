#!/usr/bin/env bash
set -euo pipefail
ASSETS_DIR="$(dirname "$0")/../app/src/main/assets"
mkdir -p "$ASSETS_DIR"
URL="https://storage.googleapis.com/tfhub-lite-models/tensorflow/lite-model/efficientdet/lite0/detection/metadata/1.tflite"
OUT="$ASSETS_DIR/efficientdet_lite0.tflite"
echo "Downloading EfficientDet Lite0 to $OUT"
curl -L "$URL" -o "$OUT"
echo "Done."
