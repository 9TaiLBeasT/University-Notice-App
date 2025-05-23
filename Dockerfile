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
    # Firebase + Google + Auth + HTTP + JSON
    curl -o lib/extras/firebase-admin-9.2.0.jar https://repo1.maven.org/maven2/com/google/firebase/firebase-admin/9.2.0/firebase-admin-9.2.0.jar && \
    curl -o lib/extras/google-api-client-1.31.5.jar https://repo1.maven.org/maven2/com/google/api-client/google-api-client/1.31.5/google-api-client-1.31.5.jar && \
    curl -o lib/extras/google-auth-library-credentials-0.22.0.jar https://repo1.maven.org/maven2/com/google/auth/google-auth-library-credentials/0.22.0/google-auth-library-credentials-0.22.0.jar && \
    curl -o lib/extras/google-auth-library-credentials-1.17.0.jar https://repo1.maven.org/maven2/com/google/auth/google-auth-library-credentials/1.17.0/google-auth-library-credentials-1.17.0.jar && \
    curl -o lib/extras/google-auth-library-oauth2-http-1.11.0.jar https://repo1.maven.org/maven2/com/google/auth/google-auth-library-oauth2-http/1.11.0/google-auth-library-oauth2-http-1.11.0.jar && \
    curl -o lib/extras/google-http-client-1.43.3.jar https://repo1.maven.org/maven2/com/google/http-client/google-http-client/1.43.3/google-http-client-1.43.3.jar && \
    curl -o lib/extras/google-http-client-gson-1.41.5.jar https://repo1.maven.org/maven2/com/google/http-client/google-http-client-gson/1.41.5/google-http-client-gson-1.41.5.jar && \
    curl -o lib/extras/google-http-client-jackson2-1.43.3.jar https://repo1.maven.org/maven2/com/google/http-client/google-http-client-jackson2/1.43.3/google-http-client-jackson2-1.43.3.jar && \
    \
    # gRPC
    curl -o lib/extras/grpc-api-1.61.1.jar https://repo1.maven.org/maven2/io/grpc/grpc-api/1.61.1/grpc-api-1.61.1.jar && \
    curl -o lib/extras/grpc-auth-1.61.1.jar https://repo1.maven.org/maven2/io/grpc/grpc-auth/1.61.1/grpc-auth-1.61.1.jar && \
    curl -o lib/extras/grpc-context-1.61.1.jar https://repo1.maven.org/maven2/io/grpc/grpc-context/1.61.1/grpc-context-1.61.1.jar && \
    curl -o lib/extras/grpc-core-1.61.1.jar https://repo1.maven.org/maven2/io/grpc/grpc-core/1.61.1/grpc-core-1.61.1.jar && \
    \
    # General utilities
    curl -o lib/extras/gson-2.8.9.jar https://repo1.maven.org/maven2/com/google/code/gson/gson/2.8.9/gson-2.8.9.jar && \
    curl -o lib/extras/guava-32.1.2-jre.jar https://repo1.maven.org/maven2/com/google/guava/guava/32.1.2-jre/guava-32.1.2-jre.jar && \
    curl -o lib/extras/api-common-2.15.0.jar https://repo1.maven.org/maven2/com/google/api/api-common/2.15.0/api-common-2.15.0.jar && \
    \
    # Jackson
    curl -o lib/extras/jackson-core-2.15.0.jar https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.15.0/jackson-core-2.15.0.jar && \
    curl -o lib/extras/jackson-databind-2.15.0.jar https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.15.0/jackson-databind-2.15.0.jar && \
    \
    # JSON
    curl -o lib/extras/json-20210307.jar https://repo1.maven.org/maven2/org/json/json/20210307/json-20210307.jar && \
    \
    # Observability
    curl -o lib/extras/opencensus-api-0.28.3.jar https://repo1.maven.org/maven2/io/opencensus/opencensus-api/0.28.3/opencensus-api-0.28.3.jar && \
    curl -o lib/extras/opencensus-contrib-http-util-0.31.1.jar https://repo1.maven.org/maven2/io/opencensus/opencensus-contrib-http-util/0.31.1/opencensus-contrib-http-util-0.31.1.jar && \
    \
    # Supabase / PostgreSQL / HikariCP / bcrypt
    curl -o lib/extras/postgresql-42.7.5.jar https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.5/postgresql-42.7.5.jar && \
    curl -o lib/extras/HikariCP-5.0.1.jar https://repo1.maven.org/maven2/com/zaxxer/HikariCP/5.0.1/HikariCP-5.0.1.jar && \
    curl -o lib/extras/jbcrypt-0.4.jar https://repo1.maven.org/maven2/org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar && \
    \
    # Protobuf
    curl -o lib/extras/protobuf-java-3.21.12.jar https://repo1.maven.org/maven2/com/google/protobuf/protobuf-java/3.21.12/protobuf-java-3.21.12.jar && \
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
