# Use OpenJDK 17 slim base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy all source files
COPY . .

# Install curl, download missing dependencies in one layer, and clean up
RUN apt-get update && apt-get install -y curl && \
    mkdir -p lib/extras && \
    curl -o lib/extras/guava-31.1-jre.jar https://repo1.maven.org/maven2/com/google/guava/guava/31.1-jre/guava-31.1-jre.jar && \
    curl -o lib/extras/google-auth-library-credentials-1.11.0.jar https://repo1.maven.org/maven2/com/google/auth/google-auth-library-credentials/1.11.0/google-auth-library-credentials-1.11.0.jar && \
    curl -o lib/extras/HikariCP-5.0.1.jar https://repo1.maven.org/maven2/com/zaxxer/HikariCP/5.0.1/HikariCP-5.0.1.jar && \
    curl -o lib/extras/bcrypt-0.4.jar https://repo1.maven.org/maven2/org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Compile Java source files using all libraries
RUN CLASSPATH=$(find lib -name "*.jar" | paste -sd ":" -):src && \
    javac -cp "$CLASSPATH" src/*.java

# Environment variable for Firebase/FCM (used in FCM push)
ENV GOOGLE_APPLICATION_CREDENTIALS=./serviceAccountKey.json

# Expose the port used by the backend
EXPOSE 10000

# Copy entrypoint script and ensure it's executable
COPY start.sh .
RUN chmod +x start.sh

# Run server
CMD ["./start.sh"]