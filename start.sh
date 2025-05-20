#!/bin/sh

PORT=${PORT:-10000}  # Use 10000 if PORT is not set

CLASSPATH=$(find lib -name '*.jar' | paste -sd ':' -):src
echo "Starting server with classpath: $CLASSPATH"
echo "Using PORT=$PORT"

exec java -DPORT=$PORT -cp "$CLASSPATH" NoticeHttpServer
