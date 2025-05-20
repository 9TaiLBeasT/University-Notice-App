#!/bin/sh

CLASSPATH=$(find lib -name '*.jar' | paste -sd ':' -):src
echo "Starting server with classpath: $CLASSPATH"

# This keeps the server alive and listens correctly
exec java -cp "$CLASSPATH" NoticeHttpServer
