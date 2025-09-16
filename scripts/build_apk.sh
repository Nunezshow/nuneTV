#!/bin/bash
set -euo pipefail

if [ -x "./gradlew" ]; then
  ./gradlew assembleDebug
else
  gradle assembleDebug
fi

echo "Debug APK generated at app/build/outputs/apk/debug/app-debug.apk"
