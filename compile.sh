#!/bin/sh
rm -rf target/classes
mkdir -p target/classes
javac -cp lib/bluecove-2.1.2.jar -d target/classes src/main/java/com/frc1706/scouting/bluetooth/*.java
