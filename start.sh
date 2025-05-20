#!/bin/sh

CLASSPATH=$(find lib -name '*.jar' | paste -sd ':' -):src
echo "Starting server with classpath: $CLASSPATH"
exec java -cp "$CLASSPATH" NoticeHttpServer
