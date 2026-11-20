#!/usr/bin/env bash

# NetPulse Pro Run Script
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
cd "$DIR"

# Ensure bin directory exists
if [ ! -d "bin" ]; then
    echo "Compiled classes not found. Building project first..."
    ./build.sh
fi

echo "Starting NetPulse Pro Network Bandwidth Monitoring System..."
java --enable-native-access=ALL-UNNAMED \
     -Dapple.laf.useScreenMenuBar=true \
     -Dapple.awt.application.name="NetPulse Pro" \
     -cp "bin:lib/*" \
     com.netmonitor.Main "$@"
