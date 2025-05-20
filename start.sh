#!/bin/sh
CLASSPATH=$(find lib -name '*.jar' | paste -sd ':' -):src
echo "Starting server with classpath: $CLASSPATH"
echo "Using PORT=${PORT:-8000}"
exec java -cp "$CLASSPATH" NoticeHttpServer
