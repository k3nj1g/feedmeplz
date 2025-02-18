# Используем официальный образ Clojure
FROM clojure:temurin-23-tools-deps-1.12.0.1501-alpine AS build

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файлы проекта
COPY deps.edn .
COPY src src
COPY resources resources
COPY build.clj .

# Собираем проект
RUN clj -T:build release

# Второй этап: создаем минимальный образ для запуска
FROM alpine/java:22-jre

WORKDIR /app

# Копируем собранный jar из предыдущего этапа
COPY --from=build /app/target/back-0.1.0.jar ./app.jar

# Копируем файл с версией
COPY --from=build /app/resources/version.txt ./resources/version.txt

# Определяем команду запуска приложения
CMD ["java", "-jar", "app.jar"]
