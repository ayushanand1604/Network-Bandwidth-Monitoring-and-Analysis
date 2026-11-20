#!/usr/bin/env bash
set -e

echo "=========================================================="
echo " Packaging NetPulse Pro Standalone Executable JAR         "
echo "=========================================================="

mkdir -p bin dist tmp_jar

# Compile first
./build.sh

echo "Extracting dependency libraries for standalone Fat JAR..."
cd tmp_jar
jar -xf ../lib/jfreechart-1.5.4.jar
jar -xf ../lib/sqlite-jdbc-3.45.1.0.jar
jar -xf ../lib/slf4j-api-2.0.12.jar
jar -xf ../lib/slf4j-simple-2.0.12.jar
cp -r ../bin/* .

# Create Manifest
cat << 'EOF' > Manifest.txt
Main-Class: com.netmonitor.Main
Implementation-Title: NetPulse Pro
Implementation-Version: 1.0.0
EOF

echo "Packaging all classes into dist/NetPulse.jar..."
jar -cfm ../dist/NetPulse.jar Manifest.txt .

cd ..
rm -rf tmp_jar

echo "=========================================================="
echo " Packaging Complete! Standalone JAR created at:"
echo " dist/NetPulse.jar"
echo " You can run it anywhere with: java -jar dist/NetPulse.jar"
echo "=========================================================="
