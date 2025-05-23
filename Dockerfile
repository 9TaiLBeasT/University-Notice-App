# Use OpenJDK 17 slim base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy all source files
COPY . .

# Install curl and download required jars
RUN apt-get update && apt-get install -y curl && \
    mkdir -p lib/extras && \
    \
    # Firebase + Auth + HTTP + JSON
    curl -o lib/extras/firebase-admin-9.2.0.jar https://repo1.maven.org/maven2/com/google/firebase/firebase-admin/9.2.0/firebase-admin-9.2.0.jar && \
    curl -o lib/extras/google-auth-library-oauth2-http-1.11.0.jar https://repo1.maven.org/maven2/com/google/auth/google-auth-library-oauth2-http/1.11.0/google-auth-library-oauth2-http-1.11.0.jar && \
    curl -o lib/extras/google-auth-library-credentials-1.11.0.jar https://repo1.maven.org/maven2/com/google/auth/google-auth-library-credentials/1.11.0/google-auth-library-credentials-1.11.0.jar && \
    curl -o lib/extras/google-oauth-client-1.31.5.jar https://repo1.maven.org/maven2/com/google/oauth-client/google-oauth-client/1.31.5/google-oauth-client-1.31.5.jar && \
    curl -o lib/extras/google-http-client-1.41.5.jar https://repo1.maven.org/maven2/com/google/http-client/google-http-client/1.41.5/google-http-client-1.41.5.jar && \
    curl -o lib/extras/google-http-client-gson-1.41.5.jar https://repo1.maven.org/maven2/com/google/http-client/google-http-client-gson/1.41.5/google-http-client-gson-1.41.5.jar && \
    curl -o lib/extras/gson-2.8.9.jar https://repo1.maven.org/maven2/com/google/code/gson/gson/2.8.9/gson-2.8.9.jar && \
    curl -o lib/extras/guava-31.1-jre.jar https://repo1.maven.org/maven2/com/google/guava/guava/31.1-jre/guava-31.1-jre.jar && \
    curl -o lib/extras/protobuf-java-3.21.12.jar https://repo1.maven.org/maven2/com/google/protobuf/protobuf-java/3.21.12/protobuf-java-3.21.12.jar && \
    \
    # Required for Firebase `ApiFuture`
    curl -o lib/extras/api-common-1.10.0.jar https://repo1.maven.org/maven2/com/google/api/api-common/1.10.0/api-common-1.10.0.jar && \
    curl -o lib/extras/google-api-client-1.34.1.jar https://repo1.maven.org/maven2/com/google/api-client/google-api-client/1.34.1/google-api-client-1.34.1.jar && \
    \
    # Supabase / PostgreSQL / HikariCP / bcrypt
    curl -o lib/extras/postgresql-42.7.5.jar https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.5/postgresql-42.7.5.jar && \
    curl -o lib/extras/HikariCP-5.0.1.jar https://repo1.maven.org/maven2/com/zaxxer/HikariCP/5.0.1/HikariCP-5.0.1.jar && \
    curl -o lib/extras/bcrypt-0.4.jar https://repo1.maven.org/maven2/org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar && \
    \
    # Cleanup
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Compile Java source files using all libraries
RUN CLASSPATH=$(find lib -name "*.jar" | paste -sd ":" -):src && \
    javac -cp "$CLASSPATH" src/*.java

# Expose backend port
EXPOSE 10000

# Copy and make entrypoint script executable
COPY start.sh .
RUN chmod +x start.sh

# Run server
CMD ["./start.sh"]
