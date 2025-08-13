$assetsDir = Join-Path $PSScriptRoot "..\app\src\main\assets"
New-Item -ItemType Directory -Force -Path $assetsDir | Out-Null
$url = "https://storage.googleapis.com/tfhub-lite-models/tensorflow/lite-model/efficientdet/lite0/detection/metadata/1.tflite"
$out = Join-Path $assetsDir "efficientdet_lite0.tflite"
Write-Host "Downloading EfficientDet Lite0 to $out"
Invoke-WebRequest -Uri $url -OutFile $out
Write-Host "Done."
