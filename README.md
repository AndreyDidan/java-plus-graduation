# Описание проекта Java-plus-graduation

Проект состоит из следующих модулей и сервисов:

## Модуль `core`
Содержит модули основных сервисов, отвечающих за работу приложения:

1. **event-service** — управление событиями.
2. **user-service** — управление пользователями.
3. **request-service** — управление заявками на участие в событиях.
4. **comment-service** — управление комментариями пользователей в событиях.
5. **interaction-api** — модуль для DTO классов, классов исключений и клиентов межсервисного взаимодействия.

## Модуль `infra`
Содержит сервисы серверов, обеспечивающих работу приложения:

1. **config-server** — хранилище конфигураций всех сервисов приложения. Конфигурации находятся в директории `resources` модуля.
2. **gateway** — API Gateway, который обеспечивает единую точку входа для всех запросов к сервисам.
3. **discovery-server** — сервис Discovery (Eureka), который регистрирует все микросервисы и обеспечивает их обнаружение.

## Модуль `stats`
Собирает информацию о статистике просмотров событий:

1. **stats-client** — обеспечивает взаимодействие с сервисами модуля `core`.
2. **stats-dto** — модуль с DTO классами.
3. **stats-server** — сервис для сбора статистики.

## Внутренний API для взаимодействия сервисов (Feign)

Сервисы взаимодействуют через следующие Feign клиенты:

1. **UserClient (user-service)**
2. **EventClient (event-service)**
3. **RequestClient (request-service)**
4. **CommentClient (comment-service)**
5. **StatsClient (stats-server)**

## Спецификации внешнего API
Спецификации внешнего API можно найти по следующим ссылкам:
1. [Спецификация основного сервиса](https://github.com/AndreyDidan/java-plus-graduation/blob/main/ewm-main-service-spec.json)
2. [Спецификация сервиса статистики](https://github.com/AndreyDidan/java-plus-graduation/blob/main/ewm-stats-service-spec.json)