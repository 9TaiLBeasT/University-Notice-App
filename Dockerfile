# Use OpenJDK 17 slim image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy everything
COPY . .

# Install curl and fetch missing JARs
RUN apt-get update && apt-get install -y curl && \
    mkdir -p lib/extras && \
    curl -o lib/extras/guava-31.1-jre.jar https://repo1.maven.org/maven2/com/google/guava/guava/31.1-jre/guava-31.1-jre.jar && \
    curl -o lib/extras/google-auth-library-credentials-1.11.0.jar https://repo1.maven.org/maven2/com/google/auth/google-auth-library-credentials/1.11.0/google-auth-library-credentials-1.11.0.jar && \
    curl -o lib/extras/HikariCP-5.0.1.jar https://repo1.maven.org/maven2/com/zaxxer/HikariCP/5.0.1/HikariCP-5.0.1.jar

# Compile all Java source files
RUN CLASSPATH=$(find lib -name "*.jar" | paste -sd ":" -):src && \
    javac -cp "$CLASSPATH" src/*.java

# Firebase service account path
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/src/serviceAccountKey.json

# Expose port (Render uses this to bind)
EXPOSE 8000

# Copy and use entrypoint script
COPY start.sh .
RUN chmod +x start.sh

CMD ["./start.sh"]
