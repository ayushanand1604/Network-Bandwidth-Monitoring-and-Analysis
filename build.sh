#!/usr/bin/env bash
set -e

echo "=========================================================="
echo " Building NetPulse Pro (Network Bandwidth Monitor)        "
echo "=========================================================="

# Create output directories
mkdir -p bin dist

echo "[1/2] Compiling Java source files..."
javac -cp "lib/*" -d bin $(find src -name "*.java")

echo "[2/2] Build successful! Class files are in bin/"
echo "Run the application with: ./run.sh"
echo "=========================================================="
