#!/bin/sh

PORT=${PORT:-8000}
CLASSPATH=$(find lib -name '*.jar' | paste -sd ':' -):src

echo "🌐 Starting server on port $PORT"
exec java -DPORT=$PORT -cp "$CLASSPATH" NoticeHttpServer
