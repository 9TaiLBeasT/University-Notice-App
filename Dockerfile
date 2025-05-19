FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy all files
COPY . .

# Generate classpath string manually (since lib/* doesn't expand properly in Docker builds)
RUN CLASSPATH=$(find lib -name "*.jar" | paste -sd ":" -) && \
    echo "Compiling with classpath: $CLASSPATH" && \
    javac -cp "$CLASSPATH:src" src/*.java

# Set environment variable for Firebase
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/src/serviceAccountKey.json

# Run the server
CMD ["java", "-cp", "lib/*:src", "NoticeHttpServer"]
