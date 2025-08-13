# CameraObjectDetect (Android, Kotlin)
CameraX + TensorFlow Lite (Task Library) app that:
- opens the camera with front/rear toggle
- provides zoom in/out buttons
- detects people and vehicles on-device
- flags moving objects via a lightweight tracker
- prints **absolute pixel coordinates** for each detected object

## Preview
- Absolute coordinates are reported in analyzed frame pixels (after rotation), origin at top-left.
- Front camera preview may be mirrored; coordinates are from the analysis frame (pre-mirror).

## Getting Started
```bash
# optional: fetch the detection model
./scripts/download_model.sh   # macOS/Linux
# or
powershell -ExecutionPolicy Bypass -File scripts\download_model.ps1  # Windows

# then open in Android Studio and Run on device
```
> The model is stored at `app/src/main/assets/efficientdet_lite0.tflite`.

## Tech
- CameraX (Preview + ImageAnalysis)
- TFLite Task Library (EfficientDet Lite0, COCO)
- Simple IoU-based tracker for movement

## License
MIT
