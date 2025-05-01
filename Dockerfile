FROM openjdk:17-jdk-slim

# Create working directory
WORKDIR /app

# Copy all files
COPY . .

# Compile Java files
WORKDIR /app/src

RUN javac -cp ".:../lib/*" *.java

# Expose the port your server uses (optional but recommended)
EXPOSE 8000

# Run the HTTP server
CMD ["java", "-cp", ".:../lib/*", "NoticeHttpServer"]
