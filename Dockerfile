FROM openjdk:17-jdk-slim

WORKDIR /app

COPY . /app

# Compile Java files
RUN javac -cp ".:lib/*" Main.java Notice.java NoticeDAO.java DBConnection.java NoticeHttpServer.java

# Run the server
CMD ["java", "-cp", ".:lib/*", "NoticeHttpServer"]
