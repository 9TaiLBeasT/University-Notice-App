FROM openjdk:17-jdk-slim

WORKDIR /app
COPY . .

WORKDIR /app/src

# ✅ Compile with all Firebase & Google dependencies in lib/
RUN javac -cp ".:../lib/*" *.java

ENV GOOGLE_APPLICATION_CREDENTIALS=/app/src/serviceAccountKey.json

EXPOSE 8000
CMD ["java", "-cp", ".:../lib/*", "NoticeHttpServer"]
