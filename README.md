![Build](https://github.com/central-university-dev/backend-academy-2025-spring-template/actions/workflows/build.yaml/badge.svg)

# Link Tracker

<!-- этот файл можно и нужно менять -->

Проект сделан в рамках курса Академия Бэкенда.

Приложение для отслеживания обновлений контента по ссылкам.
При появлении новых событий отправляется уведомление в Telegram.

Проект написан на `Java 23` с использованием `Spring Boot 3`.

Проект состоит из 2-х приложений:
* BotApplication
* ScrapperApplication

Инструкция по запуску:
1) Убедиться что запущен Docker Engine
2) `docker-compose up` - запустит необходимые сервисы
3) Убедиться что в переменных окружения есть.  
Для ScrapperApplication: `SO_TOKEN_KEY` `SO_ACCESS_TOKEN` `GITHUB_TOKEN`
Для BotApplication: `TELEGRAM_TOKEN`
4) Запустить сервисы поочереди из IntelliJ IDEA
5) Вы великолепны!
