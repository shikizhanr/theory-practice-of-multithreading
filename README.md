# Spring Boot Web Crawler

Многопоточный веб-краулер для сбора контактных данных (Email, Телефоны) с веб-страниц.
Приложение обходит указанный URL, сохраняет найденные контакты в базу данных H2 и предоставляет API для фильтрации результатов.

---

## Технологический стек
* **Java 17**
* **Spring Boot 3.2.x** (Web, Data JPA)
* **Maven** — управление зависимостями
* **Jsoup** — парсинг HTML-страниц
* **H2 Database** — база данных в оперативной памяти
* **Lombok** — для сокращения кода
* **Concurrency** — `ExecutorService`, `CompletableFuture` (многопоточность)
* **Stream API** — фильтрация и сортировка данных

---

## Как запустить
1. Откройте файл `src/main/java/com/example/crawler/CrawlerApplication.java`.
2. Запустите метод `main`.

## API Эндпоинты
1. Запуск краулера
Инициирует асинхронный процесс парсинга указанного сайта.

- URL: `/run`
- Метод: `GET`
- Параметры:
    - `url` (обязательный) — адрес сайта для старта.

Пример запроса:
```
http://localhost:8080/run?url=[https://www.vyatsu.ru/contacts](https://www.vyatsu.ru/contacts)
```

2. Получение результатов
Возвращает список найденных контактов в формате JSON. Использует Parallel Stream API для обработки данных.

- URL: `/answer`
- Метод: `GET`
- Параметры:
    - `typeFilter` (опционально) — тип контакта (`EMAIL` или `PHONE`). По умолчанию `EMAIL`.
    - `sortNewest` (опционально) — сортировка. `true` (сначала новые), `false` (сначала старые).

Примеры запроса(получить все телефоны и email-адреса):
```
http://localhost:8080/answer?typeFilter=PHONE&sortNewest=true

http://localhost:8080/answer?typeFilter=EMAIL
```

## Доступ к базе данных (H2 Console)
1. Перейдите по адресу: http://localhost:8080/h2-console
2. Введите настройки подключения:
- Driver Class: org.h2.Driver
- JDBC URL: jdbc:h2:mem:testdb
- User Name: sa
- Password: (оставьте пустым)
3. Connect
4. `SELECT * FROM CONTACTS;`


## Структура проекта
- `config/` — Конфигурация пула потоков (ExecutorService).

- `controller/` — REST контроллеры для обработки HTTP запросов.

- `model/` — JPA сущности (ContactData).

- `repository/` — Интерфейсы для работы с БД.

- `service/` — Логика парсинга (Jsoup) и регулярные выражения.
