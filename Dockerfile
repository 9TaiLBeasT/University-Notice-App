FROM openjdk:17-jdk-slim

# Create working directory
WORKDIR /app

# Copy all files
COPY . .

# Compile Java files
WORKDIR /app/src
RUN javac -cp ".:../lib/*" *.java

# ✅ Set ENV variable for Firebase to use the service account
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/src/serviceAccountKey.json

# Expose the port
EXPOSE 8000

# Start the server
CMD ["java", "-cp", ".:../lib/*", "NoticeHttpServer"]
