#!/usr/bin/sh

echo call eprog.sh PORT eg.
echo eprog.sh /dev/ttyUSB0

export JAVA_HOME=~/programing/Java/jdk1.6.0_13/bin

$JAVA_HOME/java -Djava.library.path=lib/linux -jar dist/Eprog.jar -p $1 
