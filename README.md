# FitBook

**FitBook** — Android-приложение для фитнес-клуба с тремя ролями:
- **клиент** — просмотр абонементов, профиля, расписания, контактов и личной истории;
- **тренер** — работа с клиентами, тренировками и результатами;
- **администратор** — управление клиентами, тренерами, расписанием и абонементами.

Проект использует **Java**, **SQLite**, **Material Components** и локальные XML-экраны без серверной части.

## Как работает приложение

1. Пользователь открывает `MainActivity` — это стартовый экран приложения.
2. Затем выполняется переход на `LoginActivity` или `RegisterActivity`.
3. После входа система открывает экран в зависимости от роли:
   - `AdminActivity` для администратора;
   - `TrainerActivity` для тренера;
   - `ClientActivity` для клиента.
4. Все данные хранятся локально в SQLite через `DatabaseHelper`.
5. CRUD-операции выполняются через отдельные экраны и модальные окна (`dialog_*.xml`).
6. Дополнительные разделы, такие как контакты, история абонементов и профиль, вынесены в отдельные activity для удобной навигации.

## Общая архитектура

### Слои проекта

- **UI-слой** — `Activity` и XML-разметка экранов.
- **Слой работы с данными** — `DatabaseHelper`, который создаёт таблицы, наполняет тестовыми данными и обслуживает SQLite.
- **Слой вспомогательной логики** — `UiFormUtils`, где собраны общие UI-утилиты и переиспользуемые элементы оформления.

## Структура файлов

### Корневые файлы проекта

- `build.gradle.kts` — общая Gradle-конфигурация проекта.
- `settings.gradle.kts` — подключение модулей.
- `gradle.properties` — параметры сборки.
- `gradlew`, `gradlew.bat` — Gradle Wrapper.
- `.gitignore` — исключения для Git.
- `.editorconfig` — базовые правила форматирования.

### Android Manifest

- `app/src/main/AndroidManifest.xml` — список всех экранов приложения, launcher-activity и разрешение `INTERNET`.

## Java-файлы и их роль

| Файл | Назначение |
|---|---|
| `MainActivity.java` | Стартовый экран приложения, точка входа перед авторизацией. |
| `LoginActivity.java` | Экран входа в аккаунт, проверка логина и пароля. |
| `RegisterActivity.java` | Экран регистрации нового пользователя. |
| `ProfileActivity.java` | Личный кабинет пользователя: просмотр и редактирование данных профиля. |
| `AdminActivity.java` | Главная панель администратора с переходами в разделы управления клубом. |
| `TrainerActivity.java` | Главный экран тренера с доступом к рабочим разделам. |
| `ClientActivity.java` | Главный экран клиента с карточками разделов и нижней навигацией. |
| `ContactsActivity.java` | Контакты клуба: адрес, ссылки на мессенджеры и карта проезда. |
| `MembershipsActivity.java` | Каталог абонементов для клиента: выбор и просмотр доступных тарифов. |
| `MembershipHistoryActivity.java` | История абонементов клиента в порядке, удобном для просмотра. |
| `ClientManagementActivity.java` | Админский экран управления клиентами: поиск, список, карточки и действия. |
| `TrainerManagementActivity.java` | Админский экран управления тренерами: список, поиск, просмотр и редактирование. |
| `MembershipManagementActivity.java` | Админский экран управления абонементами и типами абонементов. |
| `ScheduleManagementActivity.java` | Админский экран управления расписанием тренировок. |
| `AddTrainerActivity.java` | Отдельный экран добавления тренера. |
| `AddScheduleActivity.java` | Отдельный экран добавления тренировки/записи в расписание. |
| `DatabaseHelper.java` | Создание SQLite-таблиц, тестовое наполнение БД и основная работа с данными. |
| `UiFormUtils.java` | Общие UI-методы и вспомогательные функции для форм, полей и диалогов. |

## XML-экраны и назначение

### Основные activity

| Файл | Назначение |
|---|---|
| `app/src/main/res/layout/activity_main.xml` | Стартовый экран приложения. |
| `app/src/main/res/layout/activity_login.xml` | Экран авторизации. |
| `app/src/main/res/layout/activity_register.xml` | Экран регистрации. |
| `app/src/main/res/layout/activity_profile.xml` | Экран профиля пользователя. |
| `app/src/main/res/layout/activity_admin.xml` | Главный экран администратора. |
| `app/src/main/res/layout/activity_trainer.xml` | Главный экран тренера. |
| `app/src/main/res/layout/activity_client.xml` | Главный экран клиента. |
| `app/src/main/res/layout/activity_contacts.xml` | Экран с контактами и картой клуба. |
| `app/src/main/res/layout/activity_memberships.xml` | Каталог абонементов для клиента. |
| `app/src/main/res/layout/activity_membership_history.xml` | История абонементов. |
| `app/src/main/res/layout/activity_client_management.xml` | Управление клиентами. |
| `app/src/main/res/layout/activity_trainer_management.xml` | Управление тренерами. |
| `app/src/main/res/layout/activity_membership_management.xml` | Управление абонементами. |
| `app/src/main/res/layout/activity_schedule_management.xml` | Управление расписанием. |
| `app/src/main/res/layout/activity_add_trainer.xml` | Добавление тренера. |
| `app/src/main/res/layout/activity_add_schedule.xml` | Добавление тренировки. |

### Диалоговые окна

| Файл | Назначение |
|---|---|
| `dialog_client_anthropometry.xml` | Ввод/редактирование антропометрии клиента. |
| `dialog_client_assign_trainer.xml` | Назначение клиента тренеру. |
| `dialog_client_details.xml` | Подробная карточка клиента. |
| `dialog_client_edit.xml` | Редактирование клиента. |
| `dialog_client_register.xml` | Добавление нового клиента. |
| `dialog_client_workout_result.xml` | Добавление результата тренировки клиента. |
| `dialog_exercise_add.xml` | Добавление упражнения. |
| `dialog_membership_add.xml` | Создание нового типа абонемента. |
| `dialog_membership_details.xml` | Детальная карточка абонемента. |
| `dialog_membership_edit.xml` | Редактирование абонемента. |
| `dialog_membership_purchase.xml` | Оформление покупки/выбора абонемента. |
| `dialog_plan_assign.xml` | Назначение плана тренировок клиенту. |
| `dialog_schedule_add.xml` | Добавление тренировки в расписание. |
| `dialog_schedule_details.xml` | Просмотр деталей тренировки. |
| `dialog_schedule_edit.xml` | Редактирование тренировки. |
| `dialog_trainer_add.xml` | Добавление тренера. |
| `dialog_trainer_details.xml` | Детальная карточка тренера. |
| `dialog_trainer_edit.xml` | Редактирование тренера. |

### Элементы списков и карточек

| Файл | Назначение |
|---|---|
| `item_client_management.xml` | Карточка клиента в списке админки. |
| `item_dark_list_text.xml` | Унифицированный тёмный текстовый элемент для списков. |
| `item_dropdown_dark.xml` | Тёмный шаблон для отображения выбранного элемента в выпадающем списке. |
| `item_dropdown_dark_dropdown.xml` | Тёмный шаблон для раскрытого списка. |
| `item_membership_history.xml` | Элемент истории абонементов. |
| `item_membership_management.xml` | Карточка абонемента в админском списке. |
| `item_membership_plan.xml` | Карточка плана/тарифа абонемента. |
| `item_schedule_management.xml` | Карточка тренировки в расписании. |
| `item_trainer_management.xml` | Карточка тренера в списке админки. |

## Ресурсы оформления

### Drawable

- `button_outline_bg.xml` — кнопка с обводкой.
- `card_button_bg.xml` — фон кнопок/карточек.
- `card_list_bg.xml` — фон карточек списков.
- `dashboard_arrow_chip.xml` — декоративный элемент с иконкой стрелки.
- `dashboard_card_bg.xml` — базовый фон карточек на главном экране.
- `dashboard_card_overlay.xml` — затемнение и наложение для карточек с изображением.
- `dashboard_card_tint.xml` — тонировка карточек.
- `edittext_bg.xml` — стиль поля ввода.
- `edittext_bg_simple.xml` — упрощённый стиль поля ввода.
- `edittext_rounded_bg.xml` — скруглённый стиль поля ввода.
- `gradient_background.xml` — основной фон с градиентом.
- `gradient_background_login.xml` — фон экранов авторизации.
- `gradient_bg_simple.xml` — упрощённый градиентный фон.
- `gradient_blue.xml` — синий градиентный фон.
- `ic_launcher_background.xml` — фон иконки приложения.
- `ic_launcher_foreground.xml` — передний слой иконки приложения.
- `ic_trainer_delete.xml` — SVG-иконка удаления тренера.
- `ic_trainer_edit.xml` — SVG-иконка редактирования тренера.
- `ic_trainer_info.xml` — SVG-иконка просмотра информации о тренере.
- `logout_button_bg.xml` — фон кнопки выхода.
- `max_badge_bg.xml` — фон бейджа MAX.
- `profile_avatar_bg.xml` — фон аватара профиля.
- `profile_stats_bg.xml` — фон статистики профиля.
- `vk_badge_bg.xml` — фон бейджа VK.

### Картинки без плотного DPI

- `app/src/main/res/drawable-nodpi/bg_dashboard_body.jpg` — изображение для карточки с телом/прогрессом.
- `app/src/main/res/drawable-nodpi/bg_dashboard_membership.jpg` — изображение для карточки абонементов.
- `app/src/main/res/drawable-nodpi/bg_dashboard_plan.jpg` — изображение для карточки плана тренировок.
- `app/src/main/res/drawable-nodpi/bg_dashboard_workouts.jpg` — изображение для карточки тренировок.

### Шрифты

- `app/src/main/res/font/inter.xml` — подключение шрифта Inter.
- `app/src/main/res/font/inter_variable.ttf` — файл шрифта Inter Variable.

### Меню нижней навигации

- `menu_admin_bottom.xml` — нижняя навигация для администратора.
- `menu_client_bottom.xml` — нижняя навигация для клиента.
- `menu_trainer_bottom.xml` — нижняя навигация для тренера.

### Цвета, темы и строки

- `values/colors.xml` — палитра цветов приложения.
- `values/strings.xml` — все пользовательские строки, подписи, подсказки и тексты экранов.
- `values/themes.xml` — светлая тема приложения.
- `values-night/themes.xml` — тёмная тема приложения.

### Служебные XML

- `xml/backup_rules.xml` — правила резервного копирования.
- `xml/data_extraction_rules.xml` — правила извлечения данных.

### Иконки приложения

- `mipmap-anydpi-v26/ic_launcher.xml` — адаптивная иконка приложения.
- `mipmap-anydpi-v26/ic_launcher_round.xml` — круглая адаптивная иконка.
- `mipmap-hdpi/*`, `mipmap-mdpi/*`, `mipmap-xhdpi/*`, `mipmap-xxhdpi/*`, `mipmap-xxxhdpi/*` — иконки приложения для разных плотностей экрана.

## Логика работы по ролям

### Клиент

- входит в систему через `LoginActivity`;
- видит главный экран `ClientActivity`;
- может открыть каталог абонементов, историю абонементов, профиль и контакты клуба;
- может просматривать актуальные данные, планы и расписание;
- может редактировать свои персональные данные в `ProfileActivity`.

### Тренер

- входит через тот же экран авторизации;
- попадает на `TrainerActivity`;
- работает с подопечными, расписанием и тренировками;
- видит админские карточки и диалоги в тёмном унифицированном стиле;
- может просматривать информацию о клиентах и управлять тренировочным процессом.

### Администратор

- получает доступ к `AdminActivity`;
- управляет клиентской базой, тренерами, абонементами и расписанием;
- открывает отдельные экраны `*ManagementActivity` для CRUD-операций;
- создаёт и редактирует записи через `dialog_*.xml`;
- работает с тестовыми данными и локальной SQLite-базой.

## Работа с базой данных

`DatabaseHelper` отвечает за:
- создание таблиц при первом запуске;
- хранение пользователей, тренеров, клиентов, абонементов, расписания, записей и результатов;
- тестовое наполнение базы;
- поддержку связи между сущностями;
- централизованную работу со SQLite без отдельного backend-сервера.

### Основные таблицы

- `users` — пользователи, логины, пароли, роли, ФИО, телефон, email;
- `membership_types` — виды абонементов;
- `memberships` — абонементы клиентов;
- `membership_applications` — заявки на оформление абонемента;
- `trainers` — тренеры;
- `client_assignments` — назначение клиента тренеру;
- `schedule` — расписание тренировок;
- `bookings` — записи клиентов на занятия;
- `workout_plans` — планы тренировок;
- `plan_exercises` — упражнения в плане;
- `workout_results` — результаты выполненных тренировок;
- `anthropometry` — антропометрические замеры клиентов.

## Что важно знать при доработке

- Большая часть экранов уже приведена к единой тёмной визуальной системе.
- Формы и диалоги лучше расширять на базе уже существующих стилей, а не создавать новые с нуля.
- Для отчёта удобно показывать:
  - экран входа;
  - личный кабинет;
  - каталог абонементов;
  - админскую базу клиентов/тренеров;
  - диалоги добавления и редактирования;
  - контакты клуба с картой.

## Кратко о назначении проекта

Проект демонстрирует:
- работу с локальной БД SQLite;
- разделение по ролям;
- CRUD-операции;
- работу с формами, диалогами и списками;
- современную тёмную мобильную UI-концепцию для защиты диплома.

