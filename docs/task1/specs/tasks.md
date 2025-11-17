# Задачи по реализации MR Checker для GitLab

## Общий обзор

Реализация Spring Boot приложения для автоматической проверки Merge Requests из GitLab с использованием локальной LLM. Приложение получает изменения через GitLab API, анализирует код через LLM и публикует результаты в комментариях.

---

## Методология разработки: Test-Driven Development (TDD)

### Принципы TDD

Все Java-классы в проекте разрабатываются с применением методологии **Test-Driven Development** по циклу **Red-Green-Refactor**:

1. **🔴 RED (Красная фаза)**
   - Написать failing тест, который проверяет новую функциональность
   - Тест должен не пройти, так как функциональность еще не реализована
   - Тест должен быть специфичным и проверять конкретное поведение

2. **🟢 GREEN (Зеленая фаза)**
   - Написать минимальный код, необходимый для прохождения теста
   - Цель - заставить тест пройти как можно быстрее
   - Не заботиться о красоте кода на этом этапе

3. **🔵 REFACTOR (Рефакторинг)**
   - Улучшить код, сохраняя все тесты зелеными
   - Устранить дублирование
   - Улучшить читаемость и структуру
   - Добавить документацию
   - Убедиться, что все тесты по-прежнему проходят

### Преимущества TDD для проекта

- **Высокое покрытие тестами**: Каждая строка кода пишется для прохождения теста
- **Меньше багов**: Проблемы выявляются на этапе написания кода
- **Лучший дизайн**: TDD стимулирует писать более модульный и тестируемый код
- **Документация через тесты**: Тесты служат живой документацией поведения системы
- **Уверенность при рефакторинге**: Можно смело изменять код, зная что тесты поймают ошибки
- **Быстрая обратная связь**: Немедленное обнаружение проблем

### Итерационный подход

Каждая задача разбита на **итерации**, где каждая итерация представляет собой полный цикл TDD:
- Итерация фокусируется на одной функции/методе/аспекте класса
- Каждая итерация проходит полный цикл Red-Green-Refactor
- Итерации выполняются последовательно, постепенно наращивая функциональность

### Структура задач с TDD

Каждая задача, связанная с Java-классами (Фазы 2-8, 10.4), содержит:
- **Описание реализации (TDD)** - пошаговый план с итерациями
- **Итерации** - конкретные шаги RED-GREEN-REFACTOR
- **Создаваемые файлы** - включают как тесты, так и production код
- **Ключевые сущности** - классы и их тесты

---

## Фаза 1: Подготовка инфраструктуры

### [ ] 1.1 Настройка зависимостей проекта

**Описание реализации:**
Добавить необходимые зависимости в pom.xml для разработки и тестирования с применением TDD.

**Основные зависимости:**
- `spring-boot-starter-web` - REST API
- `spring-boot-starter-webflux` - WebClient для HTTP запросов
- `spring-boot-starter-validation` - валидация данных
- `lombok` - уменьшение boilerplate кода
- `jackson-databind` - JSON сериализация

**Тестовые зависимости (критически важны для TDD):**
- `spring-boot-starter-test` (включает JUnit 5, Mockito, AssertJ, Spring Test)
- `wiremock-jre8` или `wiremock-standalone` - моки HTTP сервисов для интеграционных тестов
- `mockito-core` и `mockito-junit-jupiter` - моки для unit тестов
- `junit-jupiter` - тестовый фреймворк
- `jacoco-maven-plugin` - анализ покрытия кода тестами

**Изменяемые файлы:**
- `pom.xml`

**Ключевые сущности:**
- Maven dependencies для production
- Maven dependencies для тестирования (scope: test)
- JaCoCo plugin для coverage
- Maven Surefire Plugin для запуска тестов

**Зависимости:**
Нет

---

### [ ] 1.2 Создание структуры пакетов

**Описание реализации:**
Создать базовую структуру пакетов для слоев приложения: config, controller, service, client, model, exception, util.

**Создаваемые директории:**
- `src/main/java/com/mr/checker/config/`
- `src/main/java/com/mr/checker/config/properties/`
- `src/main/java/com/mr/checker/controller/`
- `src/main/java/com/mr/checker/service/`
- `src/main/java/com/mr/checker/client/`
- `src/main/java/com/mr/checker/model/request/`
- `src/main/java/com/mr/checker/model/response/`
- `src/main/java/com/mr/checker/model/gitlab/`
- `src/main/java/com/mr/checker/model/llm/`
- `src/main/java/com/mr/checker/exception/`
- `src/main/java/com/mr/checker/util/`

**Ключевые сущности:**
- Пакеты приложения

**Зависимости:**
- 1.1

---

### [ ] 1.3 Настройка конфигурационных файлов

**Описание реализации:**
Создать application.yml с параметрами для GitLab и LLM (URL, токены, таймауты, модели). Добавить application-dev.yml для разработки.

**Создаваемые/изменяемые файлы:**
- `src/main/resources/application.yml`
- `src/main/resources/application-dev.yml`

**Ключевые сущности:**
- Конфигурационные параметры: gitlab.url, gitlab.token, llm.url, llm.model, timeouts

**Зависимости:**
- 1.1

---

## Фаза 2: Модели данных

### [ ] 2.1 Создание моделей запросов и ответов API

**Описание реализации (TDD):**

**Итерация 1 - CheckMRRequest:**
1. **RED**: Создать тест `CheckMRRequestTest.java` - проверить валидацию обязательных полей (projectId, mrIid не null), сериализацию JSON
2. **GREEN**: Создать `CheckMRRequest.java` с полями, аннотациями валидации (@NotNull), Lombok аннотациями
3. **REFACTOR**: Добавить JavaDoc, убедиться в читаемости кода

**Итерация 2 - Issue:**
1. **RED**: Создать тест `IssueTest.java` - проверить создание объекта, валидацию severity, JSON сериализацию
2. **GREEN**: Создать `Issue.java` с полями (severity, description, recommendation)
3. **REFACTOR**: Добавить enum для severity, если нужно

**Итерация 3 - CategoryResult:**
1. **RED**: Создать тест `CategoryResultTest.java` - проверить группировку issues, подсчет количества
2. **GREEN**: Создать `CategoryResult.java` с полями (category, issuesCount, issues)
3. **REFACTOR**: Добавить вспомогательные методы, если нужно

**Итерация 4 - CheckMRResponse:**
1. **RED**: Создать тест `CheckMRResponseTest.java` - проверить формирование полного ответа, JSON сериализацию
2. **GREEN**: Создать `CheckMRResponse.java` с полями (status, summary, details, checkedAt)
3. **REFACTOR**: Добавить builder pattern через Lombok, если удобнее

**Создаваемые файлы:**
- `src/test/java/com/mr/checker/model/request/CheckMRRequestTest.java`
- `src/main/java/com/mr/checker/model/request/CheckMRRequest.java`
- `src/test/java/com/mr/checker/model/response/IssueTest.java`
- `src/main/java/com/mr/checker/model/response/Issue.java`
- `src/test/java/com/mr/checker/model/response/CategoryResultTest.java`
- `src/main/java/com/mr/checker/model/response/CategoryResult.java`
- `src/test/java/com/mr/checker/model/response/CheckMRResponseTest.java`
- `src/main/java/com/mr/checker/model/response/CheckMRResponse.java`

**Ключевые сущности:**
- Классы: CheckMRRequest, CheckMRResponse, CategoryResult, Issue
- Поля: projectId, mrIid, gitlabToken, status, summary, details, category, severity
- Тесты: CheckMRRequestTest, CheckMRResponseTest, CategoryResultTest, IssueTest

**Зависимости:**
- 1.2

---

### [ ] 2.2 Создание моделей для GitLab API

**Описание реализации (TDD):**

**Итерация 1 - DiffFile:**
1. **RED**: Создать тест `DiffFileTest.java` - проверить десериализацию JSON от GitLab API, обработку null значений
2. **GREEN**: Создать `DiffFile.java` с полями (oldPath, newPath, diff, newFile, deletedFile)
3. **REFACTOR**: Добавить аннотации Jackson для маппинга полей API

**Итерация 2 - MRChanges:**
1. **RED**: Создать тест `MRChangesTest.java` - проверить десериализацию ответа GitLab с массивом changes
2. **GREEN**: Создать `MRChanges.java` с полями (id, iid, title, description, changes)
3. **REFACTOR**: Добавить вспомогательные методы (getTotalChangedLines, getFileCount)

**Итерация 3 - GitLabComment:**
1. **RED**: Создать тест `GitLabCommentTest.java` - проверить сериализацию для отправки в GitLab
2. **GREEN**: Создать `GitLabComment.java` с полем body
3. **REFACTOR**: Убедиться в корректной сериализации JSON

**Создаваемые файлы:**
- `src/test/java/com/mr/checker/model/gitlab/DiffFileTest.java`
- `src/main/java/com/mr/checker/model/gitlab/DiffFile.java`
- `src/test/java/com/mr/checker/model/gitlab/MRChangesTest.java`
- `src/main/java/com/mr/checker/model/gitlab/MRChanges.java`
- `src/test/java/com/mr/checker/model/gitlab/GitLabCommentTest.java`
- `src/main/java/com/mr/checker/model/gitlab/GitLabComment.java`

**Ключевые сущности:**
- Классы: MRChanges, DiffFile, GitLabComment
- Поля: id, iid, title, description, changes, oldPath, newPath, diff, body
- Тесты: MRChangesTest, DiffFileTest, GitLabCommentTest

**Зависимости:**
- 1.2

---

### [ ] 2.3 Создание моделей для LLM API

**Описание реализации (TDD):**

**Итерация 1 - Message:**
1. **RED**: Создать тест `MessageTest.java` - проверить создание сообщений с разными ролями (system, user, assistant)
2. **GREEN**: Создать `Message.java` с полями (role, content)
3. **REFACTOR**: Добавить enum для role, factory методы для удобства

**Итерация 2 - ChatRequest:**
1. **RED**: Создать тест `ChatRequestTest.java` - проверить сериализацию в формат OpenAI API
2. **GREEN**: Создать `ChatRequest.java` с полями (model, messages, temperature, max_tokens)
3. **REFACTOR**: Добавить builder для удобного создания запросов

**Итерация 3 - Choice:**
1. **RED**: Создать тест `ChoiceTest.java` - проверить десериализацию choice из ответа
2. **GREEN**: Создать `Choice.java` с полями (index, message, finish_reason)
3. **REFACTOR**: Добавить Jackson аннотации для snake_case

**Итерация 4 - ChatResponse:**
1. **RED**: Создать тест `ChatResponseTest.java` - проверить полную десериализацию ответа LLM
2. **GREEN**: Создать `ChatResponse.java` с полями (id, choices, created, model)
3. **REFACTOR**: Добавить метод getFirstChoice() для удобства

**Создаваемые файлы:**
- `src/test/java/com/mr/checker/model/llm/MessageTest.java`
- `src/main/java/com/mr/checker/model/llm/Message.java`
- `src/test/java/com/mr/checker/model/llm/ChatRequestTest.java`
- `src/main/java/com/mr/checker/model/llm/ChatRequest.java`
- `src/test/java/com/mr/checker/model/llm/ChoiceTest.java`
- `src/main/java/com/mr/checker/model/llm/Choice.java`
- `src/test/java/com/mr/checker/model/llm/ChatResponseTest.java`
- `src/main/java/com/mr/checker/model/llm/ChatResponse.java`

**Ключевые сущности:**
- Классы: ChatRequest, ChatResponse, Message, Choice
- Поля: model, messages, temperature, max_tokens, choices, content, role
- Тесты: ChatRequestTest, ChatResponseTest, MessageTest, ChoiceTest

**Зависимости:**
- 1.2

---

### [ ] 2.4 Создание модели результата анализа

**Описание реализации (TDD):**

**Итерация 1:**
1. **RED**: Создать тест `AnalysisResultTest.java` - проверить хранение проблем по категориям, подсчет общего количества
2. **GREEN**: Создать `AnalysisResult.java` с полями (logicalErrors, securityVulnerabilities, bestPracticesViolations, performanceIssues)
3. **REFACTOR**: Добавить методы getTotalIssuesCount(), getCategoryResults(), isEmpty()

**Создаваемые файлы:**
- `src/test/java/com/mr/checker/model/AnalysisResultTest.java`
- `src/main/java/com/mr/checker/model/AnalysisResult.java`

**Ключевые сущности:**
- Класс: AnalysisResult
- Поля: logicalErrors, securityVulnerabilities, bestPracticesViolations, performanceIssues
- Тест: AnalysisResultTest

**Зависимости:**
- 1.2, 2.1

---

## Фаза 3: Конфигурация

### [ ] 3.1 Создание Configuration Properties

**Описание реализации (TDD):**

**Итерация 1 - GitLabProperties:**
1. **RED**: Создать тест `GitLabPropertiesTest.java` - проверить загрузку свойств из application-test.yml, валидацию обязательных полей
2. **GREEN**: Создать `GitLabProperties.java` с аннотацией @ConfigurationProperties(prefix="gitlab"), полями (url, token, apiVersion, timeout)
3. **REFACTOR**: Добавить @Validated, @NotNull для обязательных полей, значения по умолчанию

**Итерация 2 - LLMProperties:**
1. **RED**: Создать тест `LLMPropertiesTest.java` - проверить загрузку параметров LLM, значения по умолчанию
2. **GREEN**: Создать `LLMProperties.java` с аннотацией @ConfigurationProperties(prefix="llm"), полями (url, model, timeout, temperature, maxTokens)
3. **REFACTOR**: Добавить валидацию диапазонов (temperature 0.0-2.0), разумные defaults

**Создаваемые файлы:**
- `src/test/java/com/mr/checker/config/properties/GitLabPropertiesTest.java`
- `src/main/java/com/mr/checker/config/properties/GitLabProperties.java`
- `src/test/java/com/mr/checker/config/properties/LLMPropertiesTest.java`
- `src/main/java/com/mr/checker/config/properties/LLMProperties.java`
- `src/test/resources/application-test.yml` (тестовые настройки)

**Ключевые сущности:**
- Классы: GitLabProperties, LLMProperties
- Аннотации: @ConfigurationProperties, @Validated, @NotNull
- Поля: url, token, apiVersion, timeout, model, temperature, maxTokens
- Тесты: GitLabPropertiesTest, LLMPropertiesTest

**Зависимости:**
- 1.3

---

### [ ] 3.2 Настройка RestClient для GitLab API

**Описание реализации (TDD):**

**Итерация 1:**
1. **RED**: Создать тест `GitLabConfigTest.java` - проверить создание bean WebClient, наличие заголовка Authorization, базовый URL
2. **GREEN**: Создать `GitLabConfig.java` с методом @Bean gitLabWebClient(), настроить baseUrl из GitLabProperties, добавить заголовок PRIVATE-TOKEN
3. **REFACTOR**: Вынести настройку таймаутов в отдельный метод, добавить логирование запросов/ответов для dev профиля

**Создаваемые файлы:**
- `src/test/java/com/mr/checker/config/GitLabConfigTest.java`
- `src/main/java/com/mr/checker/config/GitLabConfig.java`

**Ключевые сущности:**
- Класс: GitLabConfig
- Методы: gitLabWebClient(), configureTimeout()
- Beans: WebClient для GitLab
- Тест: GitLabConfigTest

**Зависимости:**
- 3.1

---

### [ ] 3.3 Настройка RestClient для LLM API

**Описание реализации (TDD):**

**Итерация 1:**
1. **RED**: Создать тест `LLMConfigTest.java` - проверить создание bean WebClient для LLM, baseUrl, таймауты
2. **GREEN**: Создать `LLMConfig.java` с методом @Bean llmWebClient(), настроить baseUrl и увеличенные таймауты для LLM
3. **REFACTOR**: Добавить Content-Type заголовки, настроить кодеки для больших ответов

**Создаваемые файлы:**
- `src/test/java/com/mr/checker/config/LLMConfigTest.java`
- `src/main/java/com/mr/checker/config/LLMConfig.java`

**Ключевые сущности:**
- Класс: LLMConfig
- Методы: llmWebClient()
- Beans: WebClient для LLM
- Тест: LLMConfigTest

**Зависимости:**
- 3.1

---

## Фаза 4: Клиенты внешних API

### [ ] 4.1 Реализация GitLab API Client

**Описание реализации (TDD):**

**Итерация 1 - getMRChanges:**
1. **RED**: Создать тест `GitLabApiClientTest.java` - проверить вызов GET /projects/{id}/merge_requests/{iid}/changes с моком WebClient
2. **GREEN**: Создать `GitLabApiClient.java` с методом getMRChanges(), выполнить базовый GET запрос через WebClient
3. **REFACTOR**: Добавить обработку ошибок (404, 401, timeout), логирование, retry при сетевых ошибках

**Итерация 2 - postComment:**
1. **RED**: Добавить тест для postComment - проверить POST /projects/{id}/merge_requests/{iid}/notes с телом комментария
2. **GREEN**: Реализовать метод postComment(), отправить GitLabComment через WebClient
3. **REFACTOR**: Вынести общую логику обработки ошибок, добавить проверку статуса ответа

**Создаваемые файлы:**
- `src/test/java/com/mr/checker/client/GitLabApiClientTest.java`
- `src/main/java/com/mr/checker/client/GitLabApiClient.java`

**Ключевые сущности:**
- Класс: GitLabApiClient
- Методы: getMRChanges(projectId, mrIid), postComment(projectId, mrIid, comment), handleError(response)
- Использует: WebClient, GitLabProperties
- Тест: GitLabApiClientTest (с Mockito для WebClient)

**Зависимости:**
- 2.2, 3.2

---

### [ ] 4.2 Реализация LLM API Client

**Описание реализации (TDD):**

**Итерация 1 - analyzeCode:**
1. **RED**: Создать тест `LLMApiClientTest.java` - проверить отправку ChatRequest на POST /v1/chat/completions
2. **GREEN**: Создать `LLMApiClient.java` с методом analyzeCode(messages), выполнить POST запрос через WebClient
3. **REFACTOR**: Добавить формирование ChatRequest с параметрами из LLMProperties, обработку timeout

**Итерация 2 - обработка ошибок:**
1. **RED**: Добавить тесты для различных ошибок LLM (503, timeout, invalid response)
2. **GREEN**: Реализовать обработку ошибок с выбросом LLMApiException
3. **REFACTOR**: Добавить логирование времени выполнения, размера запроса/ответа

**Создаваемые файлы:**
- `src/test/java/com/mr/checker/client/LLMApiClientTest.java`
- `src/main/java/com/mr/checker/client/LLMApiClient.java`

**Ключевые сущности:**
- Класс: LLMApiClient
- Методы: analyzeCode(messages), buildChatRequest(messages), extractResponse(chatResponse)
- Использует: WebClient, LLMProperties
- Тест: LLMApiClientTest (с Mockito)

**Зависимости:**
- 2.3, 3.3

---

## Фаза 5: Обработка исключений

### [ ] 5.1 Создание кастомных исключений

**Описание реализации (TDD):**

**Итерация 1:**
1. **RED**: Создать тест `CustomExceptionsTest.java` - проверить создание исключений с сообщением, причиной, HTTP статусом
2. **GREEN**: Создать классы GitLabApiException, LLMApiException, MRCheckException с наследованием от RuntimeException
3. **REFACTOR**: Добавить поля для HTTP статуса, кода ошибки, timestamp; добавить несколько конструкторов

**Создаваемые файлы:**
- `src/test/java/com/mr/checker/exception/CustomExceptionsTest.java`
- `src/main/java/com/mr/checker/exception/GitLabApiException.java`
- `src/main/java/com/mr/checker/exception/LLMApiException.java`
- `src/main/java/com/mr/checker/exception/MRCheckException.java`

**Ключевые сущности:**
- Классы: GitLabApiException, LLMApiException, MRCheckException
- Наследование от RuntimeException
- Тест: CustomExceptionsTest

**Зависимости:**
- 1.2

---

### [ ] 5.2 Реализация глобального обработчика исключений

**Описание реализации (TDD):**

**Итерация 1 - обработка кастомных исключений:**
1. **RED**: Создать тест `GlobalExceptionHandlerTest.java` - проверить обработку GitLabApiException, LLMApiException с правильными HTTP статусами
2. **GREEN**: Создать `GlobalExceptionHandler.java` с @RestControllerAdvice, методы handleGitLabException, handleLLMException
3. **REFACTOR**: Унифицировать формат ответа об ошибке (ErrorResponse DTO)

**Итерация 2 - валидация и общие ошибки:**
1. **RED**: Добавить тесты для MethodArgumentNotValidException, общих Exception
2. **GREEN**: Добавить методы handleValidationException, handleGenericException
3. **REFACTOR**: Добавить логирование ошибок, различные уровни детализации для production/dev

**Создаваемые файлы:**
- `src/test/java/com/mr/checker/exception/GlobalExceptionHandlerTest.java`
- `src/main/java/com/mr/checker/exception/GlobalExceptionHandler.java`
- `src/main/java/com/mr/checker/exception/ErrorResponse.java` (DTO для ошибок)

**Ключевые сущности:**
- Класс: GlobalExceptionHandler
- Аннотация: @RestControllerAdvice
- Методы: handleGitLabException, handleLLMException, handleValidationException, handleGenericException
- Тест: GlobalExceptionHandlerTest (с MockMvc)

**Зависимости:**
- 5.1

---

## Фаза 6: Утилиты

### [ ] 6.1 Реализация DiffParser

**Описание реализации (TDD):**

**Итерация 1 - базовый парсинг:**
1. **RED**: Создать тест `DiffParserTest.java` - проверить парсинг простого diff (добавление строк)
2. **GREEN**: Создать `DiffParser.java` с методом parseDeltas(diff), распознать блоки с @@
3. **REFACTOR**: Оптимизировать regex, добавить обработку edge cases

**Итерация 2 - извлечение изменений:**
1. **RED**: Добавить тесты для extractChangedLines - различные типы изменений (+, -, измененные строки)
2. **GREEN**: Реализовать метод extractChangedLines, разделить добавления/удаления
3. **REFACTOR**: Добавить структуру ChangedLine с номерами строк, типом изменения

**Итерация 3 - фильтрация:**
1. **RED**: Тест для filterCodeOnly - исключить пустые строки, комментарии, whitespace-only изменения
2. **GREEN**: Реализовать filterCodeOnly с базовой эвристикой
3. **REFACTOR**: Улучшить эвристику, добавить поддержку разных языков программирования

**Создаваемые файлы:**
- `src/test/java/com/mr/checker/util/DiffParserTest.java`
- `src/main/java/com/mr/checker/util/DiffParser.java`
- `src/main/java/com/mr/checker/util/ChangedLine.java` (вспомогательный класс)

**Ключевые сущности:**
- Класс: DiffParser
- Методы: parseDeltas(diff), extractChangedLines(diff), filterCodeOnly(diff)
- Тест: DiffParserTest (с параметризованными тестами)

**Зависимости:**
- 1.2

---

### [ ] 6.2 Реализация MarkdownFormatter

**Описание реализации (TDD):**

**Итерация 1 - форматирование категории:**
1. **RED**: Создать тест `MarkdownFormatterTest.java` - проверить форматирование одной категории с issues
2. **GREEN**: Создать `MarkdownFormatter.java` с методом formatCategory, вывести заголовок и список
3. **REFACTOR**: Добавить emoji/иконки для severity, улучшить читаемость

**Итерация 2 - форматирование issue:**
1. **RED**: Тест для formatIssue - различные severity (high, medium, low)
2. **GREEN**: Реализовать formatIssue с описанием и рекомендацией
3. **REFACTOR**: Добавить code blocks для примеров кода в issues

**Итерация 3 - полный результат:**
1. **RED**: Тест для formatAnalysisResults - полный AnalysisResult со всеми категориями
2. **GREEN**: Реализовать formatAnalysisResults, собрать все категории
3. **REFACTOR**: Добавить summary секцию вверху, footer с timestamp

**Создаваемые файлы:**
- `src/test/java/com/mr/checker/util/MarkdownFormatterTest.java`
- `src/main/java/com/mr/checker/util/MarkdownFormatter.java`

**Ключевые сущности:**
- Класс: MarkdownFormatter
- Методы: formatAnalysisResults(results), formatCategory(category), formatIssue(issue), buildSummary(results)
- Тест: MarkdownFormatterTest

**Зависимости:**
- 1.2, 2.4

---

## Фаза 7: Сервисы бизнес-логики

### [ ] 7.1 Реализация PromptService

**Описание реализации (TDD):**

**Итерация 1 - системный промпт:**
1. **RED**: Создать тест `PromptServiceTest.java` - проверить buildSystemPrompt возвращает инструкции с четырьмя категориями анализа
2. **GREEN**: Создать `PromptService.java` с методом buildSystemPrompt(), вернуть базовые инструкции для LLM
3. **REFACTOR**: Вынести инструкции в отдельный template файл или константы, добавить структуру JSON для ответа

**Итерация 2 - форматирование кода:**
1. **RED**: Тест для formatCodeForAnalysis - преобразование DiffFile в читаемый формат для LLM
2. **GREEN**: Реализовать formatCodeForAnalysis, добавить имена файлов, номера строк
3. **REFACTOR**: Ограничить размер кода (токены), добавить маркеры начала/конца файлов

**Итерация 3 - полный промпт:**
1. **RED**: Тест для buildAnalysisPrompt - объединение системного промпта и кода из MRChanges
2. **GREEN**: Реализовать buildAnalysisPrompt, собрать массив Messages для LLM
3. **REFACTOR**: Добавить контекст (название MR, автор), оптимизировать длину промпта

**Создаваемые файлы:**
- `src/test/java/com/mr/checker/service/PromptServiceTest.java`
- `src/main/java/com/mr/checker/service/PromptService.java`

**Ключевые сущности:**
- Класс: PromptService
- Методы: buildAnalysisPrompt(mrChanges), buildSystemPrompt(), formatCodeForAnalysis(diffFiles), estimateTokens(text)
- Тест: PromptServiceTest

**Зависимости:**
- 2.2, 2.3, 2.4

---

### [ ] 7.2 Реализация AnalysisFormatter

**Описание реализации (TDD):**

**Итерация 1 - парсинг JSON от LLM:**
1. **RED**: Создать тест `AnalysisFormatterTest.java` - проверить парсинг структурированного JSON ответа от LLM
2. **GREEN**: Создать `AnalysisFormatter.java` с методом parseAnalysisResponse(), парсить JSON в AnalysisResult
3. **REFACTOR**: Добавить обработку невалидного JSON, fallback на текстовый парсинг

**Итерация 2 - извлечение issues:**
1. **RED**: Тест для extractIssues - парсинг неструктурированного текста с issues
2. **GREEN**: Реализовать extractIssues с regex/pattern matching для извлечения проблем
3. **REFACTOR**: Улучшить pattern matching, добавить поддержку различных форматов

**Итерация 3 - категоризация:**
1. **RED**: Тест для categorizeIssues - распределение по категориям, определение severity
2. **GREEN**: Реализовать categorizeIssues на основе ключевых слов
3. **REFACTOR**: Создать CategoryResult объекты, добавить подсчет статистики

**Создаваемые файлы:**
- `src/test/java/com/mr/checker/service/AnalysisFormatterTest.java`
- `src/main/java/com/mr/checker/service/AnalysisFormatter.java`

**Ключевые сущности:**
- Класс: AnalysisFormatter
- Методы: parseAnalysisResponse(llmResponse), extractIssues(text), categorizeIssues(issues), determineSeverity(issue)
- Тест: AnalysisFormatterTest (с примерами реальных ответов LLM)

**Зависимости:**
- 2.1, 2.3, 2.4

---

### [ ] 7.3 Реализация CommentService

**Описание реализации (TDD):**

**Итерация 1 - успешный комментарий:**
1. **RED**: Создать тест `CommentServiceTest.java` - проверить postAnalysisComment форматирует и отправляет комментарий в GitLab
2. **GREEN**: Создать `CommentService.java` с методом postAnalysisComment(), вызвать MarkdownFormatter и GitLabApiClient
3. **REFACTOR**: Добавить логирование, обработку пустых результатов (не создавать комментарий если проблем нет)

**Итерация 2 - комментарий об ошибке:**
1. **RED**: Тест для postErrorComment - отправка информативного сообщения об ошибке
2. **GREEN**: Реализовать postErrorComment с форматированием ошибки
3. **REFACTOR**: Добавить различные шаблоны для разных типов ошибок (GitLab/LLM/timeout)

**Создаваемые файлы:**
- `src/test/java/com/mr/checker/service/CommentServiceTest.java`
- `src/main/java/com/mr/checker/service/CommentService.java`

**Ключевые сущности:**
- Класс: CommentService
- Методы: postAnalysisComment(projectId, mrIid, analysisResult), postErrorComment(projectId, mrIid, error), shouldPostComment(result)
- Использует: GitLabApiClient, MarkdownFormatter
- Тест: CommentServiceTest (с моками)

**Зависимости:**
- 4.1, 6.2, 7.2

---

### [ ] 7.4 Реализация MRCheckerService (основная логика)

**Описание реализации (TDD):**

**Итерация 1 - успешный flow:**
1. **RED**: Создать тест `MRCheckerServiceTest.java` - проверить полный цикл: получение MR, анализ, публикация, возврат результата
2. **GREEN**: Создать `MRCheckerService.java` с методом checkMR(), оркестрировать вызовы всех зависимостей
3. **REFACTOR**: Разделить на подметоды (fetchMR, analyze, publish), добавить логирование шагов

**Итерация 2 - обработка ошибок GitLab:**
1. **RED**: Тесты для ошибок GitLab (MR не найден, нет доступа, timeout)
2. **GREEN**: Реализовать handleError для GitLabApiException, попытаться опубликовать комментарий об ошибке
3. **REFACTOR**: Создать ErrorResult с подробностями для возврата в API

**Итерация 3 - обработка ошибок LLM:**
1. **RED**: Тесты для ошибок LLM (недоступен, timeout, invalid response)
2. **GREEN**: Реализовать обработку LLMApiException, опубликовать комментарий
3. **REFACTOR**: Добавить retry логику для transient ошибок

**Итерация 4 - создание ответа:**
1. **RED**: Тест для processAnalysis - преобразование AnalysisResult в CheckMRResponse
2. **GREEN**: Реализовать processAnalysis, создать CheckMRResponse со статусом
3. **REFACTOR**: Определить логику статуса (success/warning/failure) на основе количества и severity проблем

**Создаваемые файлы:**
- `src/test/java/com/mr/checker/service/MRCheckerServiceTest.java`
- `src/main/java/com/mr/checker/service/MRCheckerService.java`

**Ключевые сущности:**
- Класс: MRCheckerService
- Методы: checkMR(request), fetchMR(projectId, mrIid), analyzeCode(mrChanges), publishResults(projectId, mrIid, result), handleError(exception), buildResponse(result)
- Использует: GitLabApiClient, LLMApiClient, PromptService, AnalysisFormatter, CommentService
- Тест: MRCheckerServiceTest (с extensive mocking)

**Зависимости:**
- 4.1, 4.2, 7.1, 7.2, 7.3

---

## Фаза 8: REST API

### [ ] 8.1 Реализация MRCheckController

**Описание реализации (TDD):**

**Итерация 1 - базовый endpoint:**
1. **RED**: Создать тест `MRCheckControllerTest.java` - проверить POST /api/v1/check-mr принимает CheckMRRequest и возвращает CheckMRResponse
2. **GREEN**: Создать `MRCheckController.java` с методом checkMR(), делегировать в MRCheckerService
3. **REFACTOR**: Добавить @Valid для валидации, правильные HTTP статусы (200 OK, 400 Bad Request)

**Итерация 2 - валидация:**
1. **RED**: Тесты для невалидных запросов (null projectId, null mrIid)
2. **GREEN**: Убедиться, что валидация работает через @Valid и @NotNull аннотации
3. **REFACTOR**: Добавить кастомные сообщения валидации

**Итерация 3 - обработка ошибок:**
1. **RED**: Тесты для различных ошибок сервиса (GitLab недоступен, LLM недоступен)
2. **GREEN**: Убедиться, что GlobalExceptionHandler корректно обрабатывает
3. **REFACTOR**: Добавить логирование запросов, response времени

**Создаваемые файлы:**
- `src/test/java/com/mr/checker/controller/MRCheckControllerTest.java`
- `src/main/java/com/mr/checker/controller/MRCheckController.java`

**Ключевые сущности:**
- Класс: MRCheckController
- Аннотации: @RestController, @RequestMapping, @Validated
- Методы: checkMR(@Valid @RequestBody CheckMRRequest)
- Использует: MRCheckerService
- Тест: MRCheckControllerTest (с @WebMvcTest)

**Зависимости:**
- 2.1, 7.4

---

## Фаза 9: Расширенное тестирование

**Примечание:** Базовые unit-тесты уже созданы в рамках TDD подхода в фазах 2-8. Эта фаза посвящена дополнительным edge-case тестам и интеграционному тестированию.

### [ ] 9.1 Дополнительные unit-тесты и edge cases

**Описание реализации:**
Расширить существующие тесты дополнительными edge cases: очень большие diff, специальные символы, пустые значения, граничные условия.

**Дополнение к существующим файлам:**
- Расширить `DiffParserTest.java` - тесты для некорректных diff, огромных файлов, бинарных файлов
- Расширить `MarkdownFormatterTest.java` - тесты для экранирования специальных символов, очень длинных сообщений
- Расширить `PromptServiceTest.java` - тесты для превышения лимита токенов
- Расширить `AnalysisFormatterTest.java` - тесты для невалидного JSON, неожиданных форматов

**Ключевые сущности:**
- @ParameterizedTest для тестирования множества сценариев
- @TestFactory для динамической генерации тестов
- Тестовые данные в test/resources

**Зависимости:**
- 6.1, 6.2, 7.1, 7.2

---

### [ ] 9.2 Тесты покрытия кода (Coverage)

**Описание реализации:**
Проанализировать покрытие кода тестами (JaCoCo), довести до минимум 70%. Добавить недостающие тесты для непокрытых веток.

**Действия:**
1. Добавить JaCoCo plugin в pom.xml
2. Запустить `mvn test jacoco:report`
3. Проанализировать отчет
4. Добавить тесты для непокрытых методов и веток
5. Особое внимание на обработку исключений

**Создаваемые файлы:**
- Дополнительные тесты в существующих Test классах

**Ключевые сущности:**
- JaCoCo Maven Plugin
- Целевое покрытие: >= 70%

**Зависимости:**
- Все предыдущие задачи с тестами

---

### [ ] 9.3 Тесты производительности (Performance)

**Описание реализации:**
Создать тесты для проверки производительности критичных операций: парсинг больших diff, обработка множественных запросов.

**Создаваемые файлы:**
- `src/test/java/com/mr/checker/performance/DiffParserPerformanceTest.java`
- `src/test/java/com/mr/checker/performance/MRCheckerPerformanceTest.java`

**Ключевые сущности:**
- @Tag("performance") для отдельного запуска
- Измерение времени выполнения
- Проверка на утечки памяти

**Зависимости:**
- 6.1, 7.4

---

### [ ] 9.4 Интеграционные тесты для клиентов с WireMock

**Описание реализации (TDD для интеграции):**

**Итерация 1 - GitLab Client Integration:**
1. **RED**: Создать `GitLabApiClientIntegrationTest.java` - настроить WireMock, проверить реальный HTTP вызов с десериализацией
2. **GREEN**: Запустить тест с моком GitLab API через WireMock, убедиться в работе WebClient
3. **REFACTOR**: Добавить тесты для различных HTTP статусов (200, 404, 401, 500), проверку таймаутов

**Итерация 2 - LLM Client Integration:**
1. **RED**: Создать `LLMApiClientIntegrationTest.java` - настроить WireMock для OpenAI API, проверить полный цикл запрос-ответ
2. **GREEN**: Запустить тест с моком LLM, проверить сериализацию/десериализацию
3. **REFACTOR**: Добавить тесты для больших ответов, задержек, timeout scenarios

**Создаваемые файлы:**
- `src/test/java/com/mr/checker/client/GitLabApiClientIntegrationTest.java`
- `src/test/java/com/mr/checker/client/LLMApiClientIntegrationTest.java`
- `src/test/resources/wiremock/` (JSON файлы с mock ответами)

**Ключевые сущности:**
- Классы: GitLabApiClientIntegrationTest, LLMApiClientIntegrationTest
- Аннотации: @SpringBootTest, @AutoConfigureWireMock
- WireMock stubbing для различных сценариев
- Реальные WebClient beans из конфигурации

**Зависимости:**
- 4.1, 4.2, 3.2, 3.3

---

### [ ] 9.5 End-to-End интеграционные тесты

**Описание реализации (E2E Testing):**

**Итерация 1 - успешный сценарий:**
1. **RED**: Создать `MRCheckControllerE2ETest.java` - полный цикл от HTTP запроса до ответа с моками GitLab/LLM
2. **GREEN**: Запустить E2E тест с @SpringBootTest, MockMvc и WireMock для внешних сервисов
3. **REFACTOR**: Вынести setup в @BeforeEach, создать тестовые данные

**Итерация 2 - сценарии ошибок:**
1. **RED**: Тесты для различных сценариев ошибок end-to-end
2. **GREEN**: Проверить корректность HTTP статусов, формата ошибок, комментариев в GitLab
3. **REFACTOR**: Параметризовать тесты для различных типов ошибок

**Итерация 3 - проверка контрактов:**
1. **RED**: Тесты для валидации JSON контрактов API (schema validation)
2. **GREEN**: Добавить JSON Schema валидацию для запросов/ответов
3. **REFACTOR**: Вынести схемы в отдельные файлы

**Создаваемые файлы:**
- `src/test/java/com/mr/checker/e2e/MRCheckControllerE2ETest.java`
- `src/test/java/com/mr/checker/e2e/ErrorScenariosE2ETest.java`
- `src/test/resources/json-schemas/` (JSON Schema файлы)

**Ключевые сущности:**
- Классы: MRCheckControllerE2ETest, ErrorScenariosE2ETest
- Аннотации: @SpringBootTest(webEnvironment = RANDOM_PORT), @AutoConfigureMockMvc
- Использует: MockMvc, WireMock, TestRestTemplate
- JSON Schema validation

**Зависимости:**
- 8.1, 9.4

---

## Фаза 10: Документация и финализация

### [ ] 10.1 Обновление README.md

**Описание реализации:**
Создать подробный README с описанием проекта, инструкциями по настройке, примерами конфигурации, примерами запросов к API.

**Создаваемые/изменяемые файлы:**
- `README.md`

**Ключевые сущности:**
- Секции: Описание, Требования, Установка, Конфигурация, Использование, API Endpoints, Примеры

**Зависимости:**
- Все предыдущие задачи

---

### [ ] 10.2 Создание примера .gitlab-ci.yml

**Описание реализации:**
Создать пример конфигурации GitLab CI для интеграции MR Checker в pipeline: вызов API при создании MR.

**Создаваемые файлы:**
- `.gitlab-ci.example.yml`
- `docs/integration-guide.md`

**Ключевые сущности:**
- GitLab CI stages, jobs, scripts
- Переменные: GITLAB_TOKEN, MR_CHECKER_URL

**Зависимости:**
- 8.1, 10.1

---

### [ ] 10.3 Настройка логирования

**Описание реализации:**
Настроить logback.xml для структурированного логирования: уровни логов для разных пакетов, формат вывода, ротация файлов.

**Создаваемые файлы:**
- `src/main/resources/logback-spring.xml`

**Ключевые сущности:**
- Appenders: Console, File
- Логгеры для пакетов: com.mr.checker, org.springframework

**Зависимости:**
- Нет

---

### [ ] 10.4 Добавление health check endpoint

**Описание реализации (TDD):**

**Итерация 1 - GitLab Health Indicator:**
1. **RED**: Создать тест `GitLabHealthIndicatorTest.java` - проверить статус UP при доступности GitLab, DOWN при недоступности
2. **GREEN**: Создать `GitLabHealthIndicator.java` с реализацией HealthIndicator, выполнить простой запрос к GitLab
3. **REFACTOR**: Добавить кэширование результата (не проверять при каждом запросе), детали в health response

**Итерация 2 - LLM Health Indicator:**
1. **RED**: Создать тест `LLMHealthIndicatorTest.java` - проверить статус UP/DOWN для LLM
2. **GREEN**: Создать `LLMHealthIndicator.java`, выполнить проверочный запрос к LLM
3. **REFACTOR**: Добавить timeout для health check, информацию о модели

**Итерация 3 - конфигурация Actuator:**
1. Добавить spring-boot-starter-actuator в pom.xml
2. Настроить endpoints в application.yml (включить health, настроить detail level)
3. Создать тест для проверки /actuator/health endpoint

**Изменяемые файлы:**
- `pom.xml` (добавление actuator)
- `application.yml` (настройка endpoints)

**Создаваемые файлы:**
- `src/test/java/com/mr/checker/health/GitLabHealthIndicatorTest.java`
- `src/main/java/com/mr/checker/health/GitLabHealthIndicator.java`
- `src/test/java/com/mr/checker/health/LLMHealthIndicatorTest.java`
- `src/main/java/com/mr/checker/health/LLMHealthIndicator.java`
- `src/test/java/com/mr/checker/health/ActuatorHealthEndpointTest.java`

**Ключевые сущности:**
- Классы: GitLabHealthIndicator, LLMHealthIndicator
- Интерфейс: HealthIndicator
- Endpoints: /actuator/health
- Тесты: GitLabHealthIndicatorTest, LLMHealthIndicatorTest, ActuatorHealthEndpointTest

**Зависимости:**
- 4.1, 4.2

---

## Чек-лист готовности к production

- [ ] Все классы разработаны с применением TDD (red-green-refactor)
- [ ] Все unit и интеграционные тесты проходят успешно
- [ ] Покрытие кода тестами >= 70% (проверено JaCoCo)
- [ ] Пройдены тесты производительности для критичных операций
- [ ] E2E тесты покрывают все основные сценарии использования
- [ ] Настроено корректное логирование на всех уровнях
- [ ] Конфигурация вынесена в application.yml без хардкода
- [ ] Токены и секреты вынесены в переменные окружения
- [ ] Реализована обработка всех типов ошибок (GitLab недоступен, LLM недоступен, timeout)
- [ ] Добавлены health check endpoints для мониторинга
- [ ] README содержит полную документацию по установке и использованию
- [ ] Создан пример интеграции с GitLab CI
- [ ] Проведено ручное тестирование с реальным GitLab и LLM
- [ ] Проверена обработка больших MR (>1000 строк изменений)
- [ ] Настроены таймауты для предотвращения зависаний
- [ ] Проверена корректность форматирования комментариев в GitLab
- [ ] Валидация входных данных работает корректно
- [ ] Код прошел code review
- [ ] Отсутствуют критичные замечания от статических анализаторов
- [ ] Приложение корректно запускается в Docker (опционально)

---

## Порядок выполнения

### Критический путь (последовательно)

1. Фаза 1 → Фаза 2 → Фаза 3 → Фаза 4 → Фаза 5 → Фаза 6 → Фаза 7 → Фаза 8 → Фаза 9 → Фаза 10

### Рекомендуемая последовательность (с учетом TDD)

**Этап 1: Фундамент**
- Выполнить 1.1, 1.2, 1.3 последовательно
- Создать базовую структуру проекта
- ⚠️ Важно: начиная с Этапа 2, применять TDD red-green-refactor для каждой задачи

**Этап 2: Модели (можно параллельно)**
- 2.1, 2.2, 2.3, 2.4 независимы друг от друга
- Можно выполнять одновременно
- 🔴 Для каждого класса: сначала тест (RED), затем реализация (GREEN), затем улучшение (REFACTOR)

**Этап 3: Конфигурация (TDD)**
- 3.1 → затем параллельно 3.2 и 3.3
- 🔴 Начинать с тестов на загрузку конфигурации и создание beans

**Этап 4: Клиенты (TDD с итерациями)**
- 4.1 и 4.2 можно выполнять параллельно после завершения Фазы 3
- 🔴 Итерационный подход: сначала тесты для каждого метода, затем реализация
- Каждая итерация = полный цикл RED-GREEN-REFACTOR

**Этап 5: Исключения (TDD)**
- 5.1 → 5.2 последовательно
- Можно делать параллельно с Фазой 4
- 🔴 Тесты проверяют правильность создания исключений и их обработку

**Этап 6: Утилиты (TDD с параметризованными тестами)**
- 6.1 и 6.2 независимы друг от друга
- 🔴 Множество тестовых случаев для различных входных данных
- Итерационная разработка каждой функции

**Этап 7: Сервисы (TDD - ключевая фаза)**
- 7.1 и 7.2 можно параллельно
- Затем 7.3
- Затем 7.4 (требует все предыдущие из Фазы 7)
- 🔴 Критически важно: сервисы полностью покрываются тестами с моками
- Множество итераций для каждого сервиса

**Этап 8: API (TDD с контроллерами)**
- 8.1 после завершения 7.4
- 🔴 Тесты контроллеров с MockMvc
- Итерационный подход для endpoint, валидации, обработки ошибок

**Этап 9: Расширенное тестирование**
- 9.1 - добавление edge cases к существующим тестам
- 9.2 - анализ покрытия, добавление недостающих тестов
- 9.3 - тесты производительности
- 9.4 - интеграционные тесты с WireMock
- 9.5 - E2E тесты
- ⚠️ На этом этапе покрытие тестами должно быть >= 70%

**Этап 10: Финализация**
- 10.3 можно делать в любой момент
- 10.1, 10.2 после завершения основной разработки
- 10.4 в конце с применением TDD для health indicators

**Принципы TDD на протяжении всей разработки:**
1. 🔴 **RED**: Написать failing тест (тест не проходит)
2. 🟢 **GREEN**: Написать минимальный код для прохождения теста
3. 🔵 **REFACTOR**: Улучшить код, сохраняя все тесты зелеными
4. ♻️ Повторять для каждой новой функции/метода

### Задачи, допустимые к параллельному выполнению

**Группа A (модели):** 2.1, 2.2, 2.3, 2.4

**Группа B (конфигурация клиентов):** 3.2, 3.3

**Группа C (клиенты):** 4.1, 4.2

**Группа D (исключения):** 5.1, 5.2 (параллельно с Группой C)

**Группа E (утилиты):** 6.1, 6.2

**Группа F (сервисы-1):** 7.1, 7.2

**Группа G (тесты утилит):** 9.1 (после Группы E)

**Группа H (тесты клиентов):** 9.4 (после Группы C)

---

## Риски

### Технические риски

**Риск 1: Превышение лимита контекста LLM**
- **Описание:** Большие MR (>5000 строк) могут превышать максимальный размер контекста LLM
- **Вероятность:** Средняя
- **Воздействие:** Высокое (невозможность проанализировать MR)
- **Митигация:** Реализовать разбиение больших MR на чанки, добавить проверку размера перед отправкой

**Риск 2: Timeout при запросах к LLM**
- **Описание:** Анализ кода может занимать длительное время, превышая таймауты
- **Вероятность:** Средняя
- **Воздействие:** Среднее (pipeline упадет с ошибкой)
- **Митигация:** Настроить адекватные таймауты (60-120 сек), добавить retry механизм

**Риск 3: Недоступность LLM сервиса**
- **Описание:** Локальная LLM может быть недоступна (перезагрузка, обновление, сбой)
- **Вероятность:** Низкая
- **Воздействие:** Высокое (блокировка всех MR проверок)
- **Митигация:** Реализовать graceful degradation, создавать понятные комментарии об ошибке

**Риск 4: Изменения в GitLab API**
- **Описание:** GitLab может изменить структуру API ответов
- **Вероятность:** Низкая (используем стабильную версию API v4)
- **Воздействие:** Высокое (поломка интеграции)
- **Митигация:** Использовать официальные SDK или версионирование API, добавить валидацию ответов

**Риск 5: Некорректный парсинг ответа LLM**
- **Описание:** LLM может возвращать ответы в неожиданном формате
- **Вероятность:** Средняя
- **Воздействие:** Среднее (потеря части результатов анализа)
- **Митигация:** Использовать JSON-режим LLM, добавить fallback парсинг, валидацию структуры ответа

### Операционные риски

**Риск 6: Перегрузка приложения**
- **Описание:** Множество одновременных MR могут вызвать перегрузку
- **Вероятность:** Средняя
- **Воздействие:** Среднее (задержки в обработке)
- **Митигация:** Добавить thread pool для ограничения параллельности, рассмотреть очередь задач в будущем

**Риск 7: Утечка токенов**
- **Описание:** Токены GitLab и LLM могут попасть в логи или репозиторий
- **Вероятность:** Низкая
- **Воздействие:** Критическое (компрометация доступа)
- **Митигация:** Использовать переменные окружения, настроить фильтрацию в логах, code review

**Риск 8: Низкое качество анализа LLM**
- **Описание:** LLM может генерировать false positives или пропускать проблемы
- **Вероятность:** Средняя
- **Воздействие:** Среднее (снижение доверия к инструменту)
- **Митигация:** Тонкая настройка промптов, использование подходящей модели, сбор feedback от разработчиков

### Процессные риски

**Риск 9: Неполное покрытие требований**
- **Описание:** В процессе разработки могут выявиться недостающие требования
- **Вероятность:** Средняя
- **Воздействие:** Среднее (доработки после основной реализации)
- **Митигация:** Регулярная обратная связь с заказчиком, инкрементальная разработка

**Риск 10: Сложность интеграции с pipeline**
- **Описание:** Могут возникнуть трудности с интеграцией в существующий GitLab CI/CD
- **Вероятность:** Низкая
- **Воздействие:** Среднее (задержка внедрения)
- **Митигация:** Ранняя разработка примера интеграции, документация, тестирование на реальном pipeline

**Риск 11: Увеличение времени разработки из-за TDD**
- **Описание:** Применение TDD может увеличить время разработки на начальных этапах
- **Вероятность:** Высокая
- **Воздействие:** Среднее (увеличение сроков на 20-30%)
- **Митигация:** Планировать реалистичные сроки с учетом TDD, помнить что TDD снижает время на отладку и рефакторинг в долгосрочной перспективе, повышает качество кода и снижает количество багов

