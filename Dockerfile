FROM openjdk:17-slim

WORKDIR /app

COPY target/gymbo-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Убираем лишние параметры подключения к БД
CMD java -jar app.jar \
  --spring.profiles.active=prod \
  --telegram.bot.token=${TELEGRAM_BOT_TOKEN} \

