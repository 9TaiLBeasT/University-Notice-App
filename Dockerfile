FROM openjdk:17-jdk-slim

# Create and set working directory
WORKDIR /app

# Copy everything
COPY . .

# ✅ Compile using full classpath with all jars in lib/
RUN javac -cp "lib/*" src/*.java

# ✅ Run the server using correct classpath
CMD ["java", "-cp", "lib/*:src", "NoticeHttpServer"]
