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

### Мы переезжаем в контейнер!

Новая инструкция по запуску:
1. Убедиться что запущен Docker Engine
2. Создайте файл <code>.env'</code> со следующим содержанием:

```
GITHUB_TOKEN="your git hub token"
SO_ACCESS_TOKEN="your s.o. access token"
SO_TOKEN_KEY="your s.o. token key"

TELEGRAM_TOKEN="your bot telegram token"

SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379
```

3. `docker-compose build` - соберет необходимые сервисы
4. `docker-compose up` - запустит все сервисы
5. Вы великолепны!

## P.S
Prometeus будет доступен на: `http://localhost:9090/query`

Grafana будет доступна на: `http://localhost:3000`
