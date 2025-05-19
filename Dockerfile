FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy all files
COPY . .

# Download missing dependencies for Firebase
RUN apt-get update && apt-get install -y curl && \
    mkdir -p lib/extras && \
    curl -o lib/extras/guava-31.1-jre.jar https://repo1.maven.org/maven2/com/google/guava/guava/31.1-jre/guava-31.1-jre.jar && \
    curl -o lib/extras/google-auth-library-credentials-1.11.0.jar https://repo1.maven.org/maven2/com/google/auth/google-auth-library-credentials/1.11.0/google-auth-library-credentials-1.11.0.jar

# Generate classpath string manually (since lib/* doesn't expand properly in Docker builds)
RUN CLASSPATH=$(find lib -name "*.jar" | paste -sd ":" -) && \
    echo "Compiling with classpath: $CLASSPATH" && \
    javac -cp "$CLASSPATH:src" src/*.java

# Set environment variable for Firebase
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/src/serviceAccountKey.json

# Run the server
CMD ["java", "-cp", "lib/*:src", "NoticeHttpServer"]