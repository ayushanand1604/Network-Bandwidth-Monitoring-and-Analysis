#!/usr/bin/env bash

# NetPulse Pro Web Server Launcher
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
cd "$DIR"

# Ensure bin exists
if [ ! -d "bin" ]; then
    ./build.sh
fi

echo "Starting NetPulse Pro Embedded Web Server..."
java --enable-native-access=ALL-UNNAMED \
     -cp "bin:lib/*" \
     com.netmonitor.Main --web "$@"
