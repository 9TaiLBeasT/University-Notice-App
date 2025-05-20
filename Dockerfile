# Use OpenJDK 17 slim image
FROM openjdk:17-jdk-slim

# Set working directory inside container
WORKDIR /app

# Copy all project files into container
COPY . .

# Install curl and download any missing JARs (you can skip this step if already committed to Git)
RUN apt-get update && apt-get install -y curl && \
    mkdir -p lib/extras && \
    curl -o lib/extras/guava-31.1-jre.jar https://repo1.maven.org/maven2/com/google/guava/guava/31.1-jre/guava-31.1-jre.jar && \
    curl -o lib/extras/google-auth-library-credentials-1.11.0.jar https://repo1.maven.org/maven2/com/google/auth/google-auth-library-credentials/1.11.0/google-auth-library-credentials-1.11.0.jar && \
    curl -o lib/extras/HikariCP-5.0.1.jar https://repo1.maven.org/maven2/com/zaxxer/HikariCP/5.0.1/HikariCP-5.0.1.jar

# Compile all Java files using full classpath
RUN CLASSPATH=$(find lib -name "*.jar" | paste -sd ":" -) && \
    echo "Compiling with classpath: $CLASSPATH" && \
    javac -cp "$CLASSPATH:src" src/*.java

# Firebase credentials environment variable
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/src/serviceAccountKey.json

# Run the Java server
CMD ["sh", "-c", "java -cp \"$(find lib -name '*.jar' | paste -sd ':' -):src\" NoticeHttpServer"]
