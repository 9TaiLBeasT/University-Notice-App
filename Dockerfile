FROM openjdk:17-jdk-slim

WORKDIR /app

COPY . .

# Expand JAR classpath into a single string (fixes "cannot access" errors)
RUN CLASSPATH=$(find lib -name "*.jar" | tr '\n' ':') && \
    echo "Compiling with classpath: $CLASSPATH" && \
    cd src && \
    javac -cp "$CLASSPATH" *.java

ENV GOOGLE_APPLICATION_CREDENTIALS=/app/src/serviceAccountKey.json

CMD ["java", "-cp", "src:lib/*", "NoticeHttpServer"]
