package com.example.fitbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "fitness_club.db";
    private static final int DATABASE_VERSION = 9;

    // ==================== ТАБЛИЦА ПОЛЬЗОВАТЕЛЕЙ ====================
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "user_id";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";
    public static final String COL_ROLE = "role";
    public static final String COL_FULL_NAME = "full_name";
    public static final String COL_PHONE = "phone";
    public static final String COL_EMAIL = "email";

    // ==================== ТАБЛИЦА ТИПОВ АБОНЕМЕНТОВ ====================
    public static final String TABLE_MEMBERSHIP_TYPES = "membership_types";
    public static final String COL_MT_ID = "mt_id";
    public static final String COL_MT_NAME = "mt_name";
    public static final String COL_MT_DESCRIPTION = "mt_description";
    public static final String COL_MT_DURATION_DAYS = "mt_duration_days";
    public static final String COL_MT_PRICE = "mt_price";
    public static final String COL_MT_IS_ACTIVE = "mt_is_active";

    // ==================== ТАБЛИЦА АБОНЕМЕНТОВ КЛИЕНТОВ ====================
    public static final String TABLE_MEMBERSHIPS = "memberships";
    public static final String COL_MEM_ID = "mem_id";
    public static final String COL_MEM_CLIENT_ID = "mem_client_id";
    public static final String COL_MEM_TYPE_ID = "mem_type_id";
    public static final String COL_MEM_START_DATE = "mem_start_date";
    public static final String COL_MEM_END_DATE = "mem_end_date";
    public static final String COL_MEM_STATUS = "mem_status";
    public static final String COL_MEM_PURCHASE_DATE = "mem_purchase_date";
    public static final String COL_MEM_TYPE_NAME = "mem_type_name";

    // ==================== СТРУКТУРА ЗАЯВОК ПО АБОНЕМЕНТАМ ====================
    public static final String TABLE_MEMBERSHIP_APPLICATIONS = "membership_applications";
    public static final String COL_MA_ID = "ma_id";
    public static final String COL_MA_CLIENT_ID = "ma_client_id";
    public static final String COL_MA_TYPE_ID = "ma_type_id";
    public static final String COL_MA_PAYMENT_METHOD = "ma_payment_method";
    public static final String COL_MA_GOAL = "ma_goal";
    public static final String COL_MA_TIME_SLOT = "ma_time_slot";
    public static final String COL_MA_NOTE = "ma_note";
    public static final String COL_MA_CREATED_AT = "ma_created_at";
    public static final String COL_MA_STATUS = "ma_status";

    // ==================== ТАБЛИЦА ТРЕНЕРОВ ====================
    public static final String TABLE_TRAINERS = "trainers";
    public static final String COL_TRAINER_ID = "trainer_id";
    public static final String COL_SPECIALIZATION = "specialization";
    public static final String COL_EXPERIENCE = "experience";

    public static final String TABLE_CLIENT_ASSIGNMENTS = "client_assignments";
    public static final String COL_CA_ID = "ca_id";
    public static final String COL_CA_CLIENT_ID = "ca_client_id";
    public static final String COL_CA_TRAINER_ID = "ca_trainer_id";
    public static final String COL_CA_ASSIGNED_AT = "ca_assigned_at";
    public static final String COL_CA_NOTE = "ca_note";

    // ==================== ТАБЛИЦА РАСПИСАНИЯ ====================
    public static final String TABLE_SCHEDULE = "schedule";
    public static final String COL_SCHEDULE_ID = "schedule_id";
    public static final String COL_SCHEDULE_TRAINER_ID = "schedule_trainer_id";
    public static final String COL_WORKOUT_TYPE = "workout_type";
    public static final String COL_WORKOUT_DATE = "workout_date";
    public static final String COL_WORKOUT_TIME = "workout_time";
    public static final String COL_WORKOUT_DURATION = "workout_duration";
    public static final String COL_MAX_CLIENTS = "max_clients";
    public static final String COL_CURRENT_CLIENTS = "current_clients";

    // ==================== ТАБЛИЦА ЗАПИСЕЙ ====================
    public static final String TABLE_BOOKINGS = "bookings";
    public static final String COL_BOOKING_ID = "booking_id";
    public static final String COL_BOOKING_SCHEDULE_ID = "booking_schedule_id";
    public static final String COL_BOOKING_CLIENT_ID = "booking_client_id";
    public static final String COL_BOOKING_DATE = "booking_date";
    public static final String COL_BOOKING_STATUS = "booking_status";

    // ==================== ТАБЛИЦА ПЛАНОВ ТРЕНИРОВОК ====================
    public static final String TABLE_WORKOUT_PLANS = "workout_plans";
    public static final String COL_PLAN_ID = "plan_id";
    public static final String COL_PLAN_CLIENT_ID = "plan_client_id";
    public static final String COL_PLAN_TRAINER_ID = "plan_trainer_id";
    public static final String COL_PLAN_ASSIGNED_DATE = "plan_assigned_date";
    public static final String COL_PLAN_NOTES = "plan_notes";

    // ==================== ТАБЛИЦА УПРАЖНЕНИЙ ПЛАНА ====================
    public static final String TABLE_PLAN_EXERCISES = "plan_exercises";
    public static final String COL_PE_ID = "pe_id";
    public static final String COL_PE_PLAN_ID = "pe_plan_id";
    public static final String COL_PE_EXERCISE_NAME = "pe_exercise_name";
    public static final String COL_PE_SETS = "pe_sets";
    public static final String COL_PE_REPS = "pe_reps";
    public static final String COL_PE_WEIGHT = "pe_weight";

    // ==================== ТАБЛИЦА РЕЗУЛЬТАТОВ ТРЕНИРОВОК ====================
    public static final String TABLE_WORKOUT_RESULTS = "workout_results";
    public static final String COL_WR_ID = "wr_id";
    public static final String COL_WR_BOOKING_ID = "wr_booking_id";
    public static final String COL_WR_EXERCISE_NAME = "wr_exercise_name";
    public static final String COL_WR_SETS_COMPLETED = "wr_sets_completed";
    public static final String COL_WR_REPS_COMPLETED = "wr_reps_completed";
    public static final String COL_WR_WEIGHT_USED = "wr_weight_used";
    public static final String COL_WR_FEELING = "wr_feeling";

    // ==================== ТАБЛИЦА АНТРОПОМЕТРИИ ====================
    public static final String TABLE_ANTHROPOMETRY = "anthropometry";
    public static final String COL_ANTHRO_ID = "anthro_id";
    public static final String COL_ANTHRO_CLIENT_ID = "anthro_client_id";
    public static final String COL_ANTHRO_DATE = "anthro_date";
    public static final String COL_ANTHRO_WEIGHT = "anthro_weight";
    public static final String COL_ANTHRO_HEIGHT = "anthro_height";
    public static final String COL_ANTHRO_BICEPS = "anthro_biceps";
    public static final String COL_ANTHRO_CHEST = "anthro_chest";
    public static final String COL_ANTHRO_WAIST = "anthro_waist";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        seedDemoDataIfNeeded(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Таблица пользователей
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT UNIQUE, " +
                COL_PASSWORD + " TEXT, " +
                COL_ROLE + " TEXT, " +
                COL_FULL_NAME + " TEXT, " +
                COL_PHONE + " TEXT, " +
                COL_EMAIL + " TEXT)");

        // Таблица типов абонементов
        db.execSQL("CREATE TABLE " + TABLE_MEMBERSHIP_TYPES + " (" +
                COL_MT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_MT_NAME + " TEXT, " +
                COL_MT_DESCRIPTION + " TEXT, " +
                COL_MT_DURATION_DAYS + " INTEGER, " +
                COL_MT_PRICE + " INTEGER, " +
                COL_MT_IS_ACTIVE + " INTEGER DEFAULT 1)");

        // Таблица абонементов клиентов
        db.execSQL("CREATE TABLE " + TABLE_MEMBERSHIPS + " (" +
                COL_MEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_MEM_CLIENT_ID + " INTEGER, " +
                COL_MEM_TYPE_ID + " INTEGER, " +
                COL_MEM_START_DATE + " TEXT, " +
                COL_MEM_END_DATE + " TEXT, " +
                COL_MEM_STATUS + " TEXT, " +
                COL_MEM_PURCHASE_DATE + " TEXT, " +
                COL_MEM_TYPE_NAME + " TEXT)");

        createMembershipApplicationsTable(db);

        // Таблица тренеров
        db.execSQL("CREATE TABLE " + TABLE_TRAINERS + " (" +
                COL_TRAINER_ID + " INTEGER PRIMARY KEY, " +
                COL_SPECIALIZATION + " TEXT, " +
                COL_EXPERIENCE + " INTEGER)");

        db.execSQL("CREATE TABLE " + TABLE_CLIENT_ASSIGNMENTS + " (" +
                COL_CA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CA_CLIENT_ID + " INTEGER, " +
                COL_CA_TRAINER_ID + " INTEGER, " +
                COL_CA_ASSIGNED_AT + " TEXT, " +
                COL_CA_NOTE + " TEXT)");

        // Таблица расписания
        db.execSQL("CREATE TABLE " + TABLE_SCHEDULE + " (" +
                COL_SCHEDULE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SCHEDULE_TRAINER_ID + " INTEGER, " +
                COL_WORKOUT_TYPE + " TEXT, " +
                COL_WORKOUT_DATE + " TEXT, " +
                COL_WORKOUT_TIME + " TEXT, " +
                COL_WORKOUT_DURATION + " INTEGER, " +
                COL_MAX_CLIENTS + " INTEGER, " +
                COL_CURRENT_CLIENTS + " INTEGER)");

        // Таблица записей
        db.execSQL("CREATE TABLE " + TABLE_BOOKINGS + " (" +
                COL_BOOKING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_BOOKING_SCHEDULE_ID + " INTEGER, " +
                COL_BOOKING_CLIENT_ID + " INTEGER, " +
                COL_BOOKING_DATE + " TEXT, " +
                COL_BOOKING_STATUS + " TEXT)");

        // Таблица планов тренировок
        db.execSQL("CREATE TABLE " + TABLE_WORKOUT_PLANS + " (" +
                COL_PLAN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PLAN_CLIENT_ID + " INTEGER, " +
                COL_PLAN_TRAINER_ID + " INTEGER, " +
                COL_PLAN_ASSIGNED_DATE + " TEXT, " +
                COL_PLAN_NOTES + " TEXT)");

        // Таблица упражнений плана
        db.execSQL("CREATE TABLE " + TABLE_PLAN_EXERCISES + " (" +
                COL_PE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PE_PLAN_ID + " INTEGER, " +
                COL_PE_EXERCISE_NAME + " TEXT, " +
                COL_PE_SETS + " INTEGER, " +
                COL_PE_REPS + " INTEGER, " +
                COL_PE_WEIGHT + " REAL)");

        // Таблица результатов тренировок
        db.execSQL("CREATE TABLE " + TABLE_WORKOUT_RESULTS + " (" +
                COL_WR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_WR_BOOKING_ID + " INTEGER, " +
                COL_WR_EXERCISE_NAME + " TEXT, " +
                COL_WR_SETS_COMPLETED + " INTEGER, " +
                COL_WR_REPS_COMPLETED + " INTEGER, " +
                COL_WR_WEIGHT_USED + " REAL, " +
                COL_WR_FEELING + " TEXT)");

        // Таблица антропометрии
        db.execSQL("CREATE TABLE " + TABLE_ANTHROPOMETRY + " (" +
                COL_ANTHRO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ANTHRO_CLIENT_ID + " INTEGER, " +
                COL_ANTHRO_DATE + " TEXT, " +
                COL_ANTHRO_WEIGHT + " REAL, " +
                COL_ANTHRO_HEIGHT + " REAL, " +
                COL_ANTHRO_BICEPS + " REAL, " +
                COL_ANTHRO_CHEST + " REAL, " +
                COL_ANTHRO_WAIST + " REAL)");

        insertTestData(db);
    }

    private void insertTestData(SQLiteDatabase db) {
        // Администратор
        ContentValues admin = new ContentValues();
        admin.put(COL_USERNAME, "admin");
        admin.put(COL_PASSWORD, "admin123");
        admin.put(COL_ROLE, "admin");
        admin.put(COL_FULL_NAME, "Администратор");
        admin.put(COL_PHONE, "+7-999-123-4567");
        admin.put(COL_EMAIL, "admin@fitness.ru");
        db.insert(TABLE_USERS, null, admin);

        // Тренер
        ContentValues trainer = new ContentValues();
        trainer.put(COL_USERNAME, "trainer");
        trainer.put(COL_PASSWORD, "trainer123");
        trainer.put(COL_ROLE, "trainer");
        trainer.put(COL_FULL_NAME, "Иван Петров");
        trainer.put(COL_PHONE, "+7-999-234-5678");
        trainer.put(COL_EMAIL, "trainer@fitness.ru");
        long trainerId = db.insert(TABLE_USERS, null, trainer);

        ContentValues trainerInfo = new ContentValues();
        trainerInfo.put(COL_TRAINER_ID, trainerId);
        trainerInfo.put(COL_SPECIALIZATION, "Силовые тренировки, CrossFit");
        trainerInfo.put(COL_EXPERIENCE, 5);
        db.insert(TABLE_TRAINERS, null, trainerInfo);

        // Клиент
        ContentValues client = new ContentValues();
        client.put(COL_USERNAME, "client");
        client.put(COL_PASSWORD, "client123");
        client.put(COL_ROLE, "client");
        client.put(COL_FULL_NAME, "Алексей Смирнов");
        client.put(COL_PHONE, "+7-999-345-6789");
        client.put(COL_EMAIL, "client@fitness.ru");
        long clientId = db.insert(TABLE_USERS, null, client);

        ContentValues assignment = new ContentValues();
        assignment.put(COL_CA_CLIENT_ID, clientId);
        assignment.put(COL_CA_TRAINER_ID, trainerId);
        assignment.put(COL_CA_ASSIGNED_AT, "2024-01-01 09:00:00");
        assignment.put(COL_CA_NOTE, "Персональная работа по умолчанию");
        db.insert(TABLE_CLIENT_ASSIGNMENTS, null, assignment);

        // Типы абонементов
        ContentValues type1 = new ContentValues();
        type1.put(COL_MT_NAME, "Месячный");
        type1.put(COL_MT_DESCRIPTION, "Абонемент на 1 месяц с посещением в любое время");
        type1.put(COL_MT_DURATION_DAYS, 30);
        type1.put(COL_MT_PRICE, 3000);
        type1.put(COL_MT_IS_ACTIVE, 1);
        db.insert(TABLE_MEMBERSHIP_TYPES, null, type1);

        ContentValues type2 = new ContentValues();
        type2.put(COL_MT_NAME, "Полугодовой");
        type2.put(COL_MT_DESCRIPTION, "Абонемент на 6 месяцев с экономией 20%");
        type2.put(COL_MT_DURATION_DAYS, 180);
        type2.put(COL_MT_PRICE, 15000);
        type2.put(COL_MT_IS_ACTIVE, 1);
        db.insert(TABLE_MEMBERSHIP_TYPES, null, type2);

        ContentValues type3 = new ContentValues();
        type3.put(COL_MT_NAME, "Годовой");
        type3.put(COL_MT_DESCRIPTION, "Абонемент на 12 месяцев с экономией 30%");
        type3.put(COL_MT_DURATION_DAYS, 365);
        type3.put(COL_MT_PRICE, 28000);
        type3.put(COL_MT_IS_ACTIVE, 1);
        db.insert(TABLE_MEMBERSHIP_TYPES, null, type3);

        // Абонемент клиента
        ContentValues membership = new ContentValues();
        membership.put(COL_MEM_CLIENT_ID, clientId);
        membership.put(COL_MEM_TYPE_ID, 1);
        membership.put(COL_MEM_START_DATE, "2024-01-01");
        membership.put(COL_MEM_END_DATE, "2024-12-31");
        membership.put(COL_MEM_STATUS, "active");
        membership.put(COL_MEM_PURCHASE_DATE, "2024-01-01");
        membership.put(COL_MEM_TYPE_NAME, "месячный");
        db.insert(TABLE_MEMBERSHIPS, null, membership);
        seedMembershipHistory(db, clientId);

        // Тестовые тренировки
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());
        String tomorrow = getFutureDate(1);
        String dayAfter = getFutureDate(2);
        String threeDaysLater = getFutureDate(3);

        addTestWorkout(db, trainerId, "Силовая тренировка", today, "18:00", 60, 10, 3);
        addTestWorkout(db, trainerId, "Кардио тренировка", tomorrow, "19:00", 45, 8, 2);
        addTestWorkout(db, trainerId, "Йога", dayAfter, "10:00", 60, 15, 5);
        addTestWorkout(db, trainerId, "CrossFit", threeDaysLater, "20:00", 50, 12, 0);
        addTestWorkout(db, trainerId, "Пилатес", getFutureDate(4), "11:00", 55, 10, 1);
        addTestWorkout(db, trainerId, "Функциональный тренинг", getFutureDate(5), "17:00", 60, 12, 2);
    }

    private void seedDemoDataIfNeeded(SQLiteDatabase db) {
        Cursor marker = db.query(TABLE_USERS, new String[]{COL_USER_ID}, COL_USERNAME + "=?",
                new String[]{"demo_admin"}, null, null, null);
        boolean alreadySeeded = marker != null && marker.getCount() > 0;
        if (marker != null) {
            marker.close();
        }
        if (!alreadySeeded) {
            seedDemoData(db);
        }
    }

    public void reseedDemoData() {
        SQLiteDatabase db = getWritableDatabase();
        clearDemoData(db);
        seedDemoData(db);
    }

    private void clearDemoData(SQLiteDatabase db) {
        List<String> tables = Arrays.asList(
                TABLE_WORKOUT_RESULTS,
                TABLE_BOOKINGS,
                TABLE_PLAN_EXERCISES,
                TABLE_WORKOUT_PLANS,
                TABLE_ANTHROPOMETRY,
                TABLE_CLIENT_ASSIGNMENTS,
                TABLE_SCHEDULE,
                TABLE_MEMBERSHIPS,
                TABLE_MEMBERSHIP_APPLICATIONS,
                TABLE_TRAINERS,
                TABLE_USERS,
                TABLE_MEMBERSHIP_TYPES
        );
        for (String table : tables) {
            db.delete(table, null, null);
        }
    }

    private void seedDemoData(SQLiteDatabase db) {
        if (hasDemoData(db)) {
            return;
        }

        long adminId = ensureUser(db, "demo_admin", "demo123", "admin", "Администратор", "+7-900-000-00-01", "admin@fitbook.demo");
        long trainer1Id = ensureUser(db, "demo_trainer_1", "trainer123", "trainer", "Иван Петров", "+7-900-000-00-02", "ivan.petrov@fitbook.demo");
        long trainer2Id = ensureUser(db, "demo_trainer_2", "trainer123", "trainer", "Мария Смирнова", "+7-900-000-00-03", "maria.smirnova@fitbook.demo");
        long trainer3Id = ensureUser(db, "demo_trainer_3", "trainer123", "trainer", "Алексей Орлов", "+7-900-000-00-04", "alexey.orlov@fitbook.demo");

        ensureTrainerProfile(db, trainer1Id, "Силовые, функциональный тренинг", 7);
        ensureTrainerProfile(db, trainer2Id, "Йога, пилатес, растяжка", 5);
        ensureTrainerProfile(db, trainer3Id, "Кардио, похудение, ОФП", 9);

        long client1Id = ensureUser(db, "demo_client_1", "client123", "client", "Самвел К.", "+7-900-100-00-01", "samvel@fitbook.demo");
        long client2Id = ensureUser(db, "demo_client_2", "client123", "client", "Денис М.", "+7-900-100-00-02", "denis@fitbook.demo");
        long client3Id = ensureUser(db, "demo_client_3", "client123", "client", "Алина С.", "+7-900-100-00-03", "alina@fitbook.demo");
        long client4Id = ensureUser(db, "demo_client_4", "client123", "client", "Ольга В.", "+7-900-100-00-04", "olga@fitbook.demo");
        long client5Id = ensureUser(db, "demo_client_5", "client123", "client", "Роман Т.", "+7-900-100-00-05", "roman@fitbook.demo");
        long client6Id = ensureUser(db, "demo_client_6", "client123", "client", "Екатерина Л.", "+7-900-100-00-06", "kate@fitbook.demo");

        ensureMembershipTypes(db);

        ensureAssignment(db, client1Id, trainer1Id, "Основной клиент");
        ensureAssignment(db, client2Id, trainer1Id, "Набор массы");
        ensureAssignment(db, client3Id, trainer2Id, "Мягкая нагрузка");
        ensureAssignment(db, client4Id, trainer2Id, "Снижение веса");
        ensureAssignment(db, client5Id, trainer3Id, "Поддержание формы");
        ensureAssignment(db, client6Id, trainer3Id, "Подготовка к марафону");

        ensureMembership(db, client1Id, 3, -30, 335, "expired");
        ensureMembership(db, client1Id, 2, -15, 15, "active");
        ensureMembership(db, client2Id, 1, -12, 18, "active");
        ensureMembership(db, client3Id, 4, -40, 325, "expired");
        ensureMembership(db, client3Id, 2, -3, 27, "active");
        ensureMembership(db, client4Id, 2, -10, 20, "active");
        ensureMembership(db, client5Id, 1, -60, -30, "expired");
        ensureMembership(db, client6Id, 3, -5, 360, "active");

        ensureApplication(db, client4Id, 2, "card", "Похудение", "вечер", "Хочу заниматься после работы");
        ensureApplication(db, client5Id, 3, "cash", "Выносливость", "утро", "Без противопоказаний");

        long schedule1 = ensureSchedule(db, trainer1Id, "Силовая тренировка", 1, "18:00", 60, 12, 8);
        long schedule2 = ensureSchedule(db, trainer1Id, "Грудь + трицепс", 2, "19:15", 55, 10, 6);
        long schedule3 = ensureSchedule(db, trainer2Id, "Йога", 1, "09:30", 60, 15, 9);
        long schedule4 = ensureSchedule(db, trainer2Id, "Пилатес", 3, "11:00", 50, 12, 4);
        long schedule5 = ensureSchedule(db, trainer3Id, "Кардио", 1, "20:00", 45, 14, 11);
        long schedule6 = ensureSchedule(db, trainer3Id, "Функциональный тренинг", 4, "17:30", 60, 10, 7);

        ensureBooking(db, schedule1, client1Id, "confirmed");
        ensureBooking(db, schedule2, client2Id, "confirmed");
        ensureBooking(db, schedule3, client3Id, "completed");
        ensureBooking(db, schedule4, client4Id, "confirmed");
        ensureBooking(db, schedule5, client5Id, "cancelled");
        ensureBooking(db, schedule6, client6Id, "confirmed");

        long plan1 = ensureWorkoutPlan(db, client1Id, trainer1Id, "Набор мышечной массы и стабилизация корпуса");
        long plan2 = ensureWorkoutPlan(db, client3Id, trainer2Id, "Мягкий план на мобильность и растяжку");
        long plan3 = ensureWorkoutPlan(db, client6Id, trainer3Id, "Подготовка выносливости и кардио");

        ensurePlanExercise(db, plan1, "Жим лёжа", 4, 10, 45f);
        ensurePlanExercise(db, plan1, "Приседания со штангой", 4, 8, 55f);
        ensurePlanExercise(db, plan1, "Планка", 3, 60, 0f);

        ensurePlanExercise(db, plan2, "Кошка-корова", 3, 12, 0f);
        ensurePlanExercise(db, plan2, "Наклоны", 3, 15, 0f);

        ensurePlanExercise(db, plan3, "Беговая дорожка", 5, 10, 0f);
        ensurePlanExercise(db, plan3, "Интервальный бег", 6, 5, 0f);

        ensureMeasurement(db, client1Id, -14, 84.4f, 178f, 36.0f, 102.0f, 80.0f);
        ensureMeasurement(db, client1Id, -7, 83.8f, 178f, 36.5f, 101.2f, 79.3f);
        ensureMeasurement(db, client3Id, -9, 61.2f, 167f, 29.0f, 88.5f, 66.0f);
        ensureMeasurement(db, client6Id, -4, 72.5f, 171f, 32.0f, 94.0f, 73.5f);
    }

    private boolean hasDemoData(SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID}, COL_USERNAME + " IN (?, ?, ?)",
                new String[]{"demo_admin", "demo_client_1", "demo_trainer_1"}, null, null, null);
        boolean result = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        return result;
    }

    private long ensureUser(SQLiteDatabase db, String username, String password, String role, String fullName, String phone, String email) {
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID}, COL_USERNAME + "=?",
                new String[]{username}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            long userId = cursor.getLong(0);
            cursor.close();
            return userId;
        }
        if (cursor != null) cursor.close();

        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, password);
        values.put(COL_ROLE, role);
        values.put(COL_FULL_NAME, fullName);
        values.put(COL_PHONE, phone);
        values.put(COL_EMAIL, email);
        return db.insert(TABLE_USERS, null, values);
    }

    private void ensureTrainerProfile(SQLiteDatabase db, long trainerId, String specialization, int experience) {
        Cursor cursor = db.query(TABLE_TRAINERS, new String[]{COL_TRAINER_ID}, COL_TRAINER_ID + "=?",
                new String[]{String.valueOf(trainerId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            cursor.close();
            return;
        }
        if (cursor != null) cursor.close();

        ContentValues values = new ContentValues();
        values.put(COL_TRAINER_ID, trainerId);
        values.put(COL_SPECIALIZATION, specialization);
        values.put(COL_EXPERIENCE, experience);
        db.insert(TABLE_TRAINERS, null, values);
    }

    private void ensureMembershipTypes(SQLiteDatabase db) {
        ensureMembershipType(db, "Дневной", "Посещение по будням в дневное время", 30, 2400, 1);
        ensureMembershipType(db, "Месячный", "Полный доступ на месяц", 30, 3900, 1);
        ensureMembershipType(db, "Полугодовой", "Выгодный план на 6 месяцев", 180, 15900, 1);
        ensureMembershipType(db, "Годовой", "Максимальная выгода на 12 месяцев", 365, 28900, 1);
    }

    private void ensureMembershipType(SQLiteDatabase db, String name, String description, int durationDays, int price, int isActive) {
        Cursor cursor = db.query(TABLE_MEMBERSHIP_TYPES, new String[]{COL_MT_ID}, COL_MT_NAME + "=?",
                new String[]{name}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            cursor.close();
            return;
        }
        if (cursor != null) cursor.close();

        ContentValues values = new ContentValues();
        values.put(COL_MT_NAME, name);
        values.put(COL_MT_DESCRIPTION, description);
        values.put(COL_MT_DURATION_DAYS, durationDays);
        values.put(COL_MT_PRICE, price);
        values.put(COL_MT_IS_ACTIVE, isActive);
        db.insert(TABLE_MEMBERSHIP_TYPES, null, values);
    }

    private void ensureAssignment(SQLiteDatabase db, long clientId, long trainerId, String note) {
        Cursor cursor = db.query(TABLE_CLIENT_ASSIGNMENTS, new String[]{COL_CA_ID},
                COL_CA_CLIENT_ID + "=? AND " + COL_CA_TRAINER_ID + "=?",
                new String[]{String.valueOf(clientId), String.valueOf(trainerId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            cursor.close();
            return;
        }
        if (cursor != null) cursor.close();

        ContentValues values = new ContentValues();
        values.put(COL_CA_CLIENT_ID, clientId);
        values.put(COL_CA_TRAINER_ID, trainerId);
        values.put(COL_CA_ASSIGNED_AT, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        values.put(COL_CA_NOTE, note);
        db.insert(TABLE_CLIENT_ASSIGNMENTS, null, values);
    }

    private void ensureMembership(SQLiteDatabase db, long clientId, long typeId, int startOffsetDays, int endOffsetDays, String status) {
        Cursor cursor = db.query(TABLE_MEMBERSHIPS, new String[]{COL_MEM_ID},
                COL_MEM_CLIENT_ID + "=? AND " + COL_MEM_TYPE_ID + "=? AND " + COL_MEM_STATUS + "=?",
                new String[]{String.valueOf(clientId), String.valueOf(typeId), status}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            cursor.close();
            return;
        }
        if (cursor != null) cursor.close();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = shiftDate(startOffsetDays);
        String endDate = shiftDate(endOffsetDays);
        String typeName = getMembershipTypeName(db, typeId);

        ContentValues values = new ContentValues();
        values.put(COL_MEM_CLIENT_ID, clientId);
        values.put(COL_MEM_TYPE_ID, typeId);
        values.put(COL_MEM_START_DATE, startDate);
        values.put(COL_MEM_END_DATE, endDate);
        values.put(COL_MEM_STATUS, status);
        values.put(COL_MEM_PURCHASE_DATE, startDate);
        values.put(COL_MEM_TYPE_NAME, typeName);
        db.insert(TABLE_MEMBERSHIPS, null, values);
    }

    private void ensureApplication(SQLiteDatabase db, long clientId, long typeId, String paymentMethod, String goal, String timeSlot, String note) {
        Cursor cursor = db.query(TABLE_MEMBERSHIP_APPLICATIONS, new String[]{COL_MA_ID},
                COL_MA_CLIENT_ID + "=? AND " + COL_MA_TYPE_ID + "=?",
                new String[]{String.valueOf(clientId), String.valueOf(typeId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            cursor.close();
            return;
        }
        if (cursor != null) cursor.close();

        ContentValues values = new ContentValues();
        values.put(COL_MA_CLIENT_ID, clientId);
        values.put(COL_MA_TYPE_ID, typeId);
        values.put(COL_MA_PAYMENT_METHOD, paymentMethod);
        values.put(COL_MA_GOAL, goal);
        values.put(COL_MA_TIME_SLOT, timeSlot);
        values.put(COL_MA_NOTE, note);
        values.put(COL_MA_CREATED_AT, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        values.put(COL_MA_STATUS, "confirmed");
        db.insert(TABLE_MEMBERSHIP_APPLICATIONS, null, values);
    }

    private long ensureSchedule(SQLiteDatabase db, long trainerId, String workoutType, int dayOffset, String time, int duration, int maxClients, int currentClients) {
        String date = shiftDate(dayOffset);
        Cursor cursor = db.query(TABLE_SCHEDULE, new String[]{COL_SCHEDULE_ID},
                COL_SCHEDULE_TRAINER_ID + "=? AND " + COL_WORKOUT_TYPE + "=? AND " + COL_WORKOUT_DATE + "=? AND " + COL_WORKOUT_TIME + "=?",
                new String[]{String.valueOf(trainerId), workoutType, date, time}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            long scheduleId = cursor.getLong(0);
            cursor.close();
            return scheduleId;
        }
        if (cursor != null) cursor.close();

        ContentValues values = new ContentValues();
        values.put(COL_SCHEDULE_TRAINER_ID, trainerId);
        values.put(COL_WORKOUT_TYPE, workoutType);
        values.put(COL_WORKOUT_DATE, date);
        values.put(COL_WORKOUT_TIME, time);
        values.put(COL_WORKOUT_DURATION, duration);
        values.put(COL_MAX_CLIENTS, maxClients);
        values.put(COL_CURRENT_CLIENTS, currentClients);
        return db.insert(TABLE_SCHEDULE, null, values);
    }

    private void ensureBooking(SQLiteDatabase db, long scheduleId, long clientId, String status) {
        Cursor cursor = db.query(TABLE_BOOKINGS, new String[]{COL_BOOKING_ID},
                COL_BOOKING_SCHEDULE_ID + "=? AND " + COL_BOOKING_CLIENT_ID + "=?",
                new String[]{String.valueOf(scheduleId), String.valueOf(clientId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            cursor.close();
            return;
        }
        if (cursor != null) cursor.close();

        ContentValues values = new ContentValues();
        values.put(COL_BOOKING_SCHEDULE_ID, scheduleId);
        values.put(COL_BOOKING_CLIENT_ID, clientId);
        values.put(COL_BOOKING_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        values.put(COL_BOOKING_STATUS, status);
        db.insert(TABLE_BOOKINGS, null, values);
    }

    private long ensureWorkoutPlan(SQLiteDatabase db, long clientId, long trainerId, String notes) {
        Cursor cursor = db.query(TABLE_WORKOUT_PLANS, new String[]{COL_PLAN_ID},
                COL_PLAN_CLIENT_ID + "=? AND " + COL_PLAN_TRAINER_ID + "=?",
                new String[]{String.valueOf(clientId), String.valueOf(trainerId)}, null, null, COL_PLAN_ID + " DESC");
        if (cursor != null && cursor.moveToFirst()) {
            long planId = cursor.getLong(0);
            cursor.close();
            return planId;
        }
        if (cursor != null) cursor.close();

        ContentValues values = new ContentValues();
        values.put(COL_PLAN_CLIENT_ID, clientId);
        values.put(COL_PLAN_TRAINER_ID, trainerId);
        values.put(COL_PLAN_ASSIGNED_DATE, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        values.put(COL_PLAN_NOTES, notes);
        return db.insert(TABLE_WORKOUT_PLANS, null, values);
    }

    private void ensurePlanExercise(SQLiteDatabase db, long planId, String exerciseName, int sets, int reps, float weight) {
        Cursor cursor = db.query(TABLE_PLAN_EXERCISES, new String[]{COL_PE_ID},
                COL_PE_PLAN_ID + "=? AND " + COL_PE_EXERCISE_NAME + "=?",
                new String[]{String.valueOf(planId), exerciseName}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            cursor.close();
            return;
        }
        if (cursor != null) cursor.close();

        ContentValues values = new ContentValues();
        values.put(COL_PE_PLAN_ID, planId);
        values.put(COL_PE_EXERCISE_NAME, exerciseName);
        values.put(COL_PE_SETS, sets);
        values.put(COL_PE_REPS, reps);
        values.put(COL_PE_WEIGHT, weight);
        db.insert(TABLE_PLAN_EXERCISES, null, values);
    }

    private void ensureMeasurement(SQLiteDatabase db, long clientId, int dayOffset, float weight, float height, float biceps, float chest, float waist) {
        String date = shiftDate(dayOffset);
        Cursor cursor = db.query(TABLE_ANTHROPOMETRY, new String[]{COL_ANTHRO_ID},
                COL_ANTHRO_CLIENT_ID + "=? AND " + COL_ANTHRO_DATE + "=?",
                new String[]{String.valueOf(clientId), date}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            cursor.close();
            return;
        }
        if (cursor != null) cursor.close();

        ContentValues values = new ContentValues();
        values.put(COL_ANTHRO_CLIENT_ID, clientId);
        values.put(COL_ANTHRO_DATE, date);
        values.put(COL_ANTHRO_WEIGHT, weight);
        values.put(COL_ANTHRO_HEIGHT, height);
        values.put(COL_ANTHRO_BICEPS, biceps);
        values.put(COL_ANTHRO_CHEST, chest);
        values.put(COL_ANTHRO_WAIST, waist);
        db.insert(TABLE_ANTHROPOMETRY, null, values);
    }

    @NonNull
    private String getMembershipTypeName(SQLiteDatabase db, long typeId) {
        Cursor cursor = db.query(TABLE_MEMBERSHIP_TYPES, new String[]{COL_MT_NAME},
                COL_MT_ID + "=?", new String[]{String.valueOf(typeId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String value = cursor.getString(0);
            cursor.close();
            return value;
        }
        if (cursor != null) cursor.close();
        return "Абонемент";
    }

    private String shiftDate(int dayOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, dayOffset);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
    }

    private void seedMembershipHistory(SQLiteDatabase db, long clientId) {
        addMembershipRecord(db, clientId, 1, "2025-05-01", "2025-05-31", "expired", "2025-05-01");
        addMembershipRecord(db, clientId, 2, "2025-06-01", "2025-11-27", "expired", "2025-06-01");
        addMembershipRecord(db, clientId, 3, "2025-11-28", "2026-11-27", "active", "2025-11-28");
        addMembershipRecord(db, clientId, 1, "2026-01-10", "2026-02-09", "expired", "2026-01-10");
        addMembershipRecord(db, clientId, 2, "2026-03-01", "2026-08-27", "expired", "2026-03-01");
    }

    private void addMembershipRecord(SQLiteDatabase db, long clientId, long typeId, String startDate, String endDate, String status, String purchaseDate) {
        Cursor type = db.query(TABLE_MEMBERSHIP_TYPES, null, COL_MT_ID + "=?",
                new String[]{String.valueOf(typeId)}, null, null, null);
        if (type == null || !type.moveToFirst()) {
            if (type != null) type.close();
            return;
        }

        ContentValues membership = new ContentValues();
        membership.put(COL_MEM_CLIENT_ID, clientId);
        membership.put(COL_MEM_TYPE_ID, typeId);
        membership.put(COL_MEM_START_DATE, startDate);
        membership.put(COL_MEM_END_DATE, endDate);
        membership.put(COL_MEM_STATUS, status);
        membership.put(COL_MEM_PURCHASE_DATE, purchaseDate);
        membership.put(COL_MEM_TYPE_NAME, type.getString(type.getColumnIndexOrThrow(COL_MT_NAME)));
        db.insert(TABLE_MEMBERSHIPS, null, membership);
        type.close();
    }

    private void addTestWorkout(SQLiteDatabase db, long trainerId, String type, String date, String time, int duration, int max, int current) {
        ContentValues workout = new ContentValues();
        workout.put(COL_SCHEDULE_TRAINER_ID, trainerId);
        workout.put(COL_WORKOUT_TYPE, type);
        workout.put(COL_WORKOUT_DATE, date);
        workout.put(COL_WORKOUT_TIME, time);
        workout.put(COL_WORKOUT_DURATION, duration);
        workout.put(COL_MAX_CLIENTS, max);
        workout.put(COL_CURRENT_CLIENTS, current);
        db.insert(TABLE_SCHEDULE, null, workout);
    }

    private String getFutureDate(int daysFromNow) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_YEAR, daysFromNow);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(cal.getTime());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 9) {
            createMembershipApplicationsTable(db);
        }
        seedMembershipHistory(db);
    }

    private void createMembershipApplicationsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_MEMBERSHIP_APPLICATIONS + " (" +
                COL_MA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_MA_CLIENT_ID + " INTEGER, " +
                COL_MA_TYPE_ID + " INTEGER, " +
                COL_MA_PAYMENT_METHOD + " TEXT, " +
                COL_MA_GOAL + " TEXT, " +
                COL_MA_TIME_SLOT + " TEXT, " +
                COL_MA_NOTE + " TEXT, " +
                COL_MA_CREATED_AT + " TEXT, " +
                COL_MA_STATUS + " TEXT)");
    }

    // ============ МЕТОДЫ ДЛЯ АУТЕНТИФИКАЦИИ ============

    private void seedMembershipHistory(SQLiteDatabase db) {
        Cursor client = db.query(TABLE_USERS, new String[]{COL_USER_ID}, COL_USERNAME + "=?",
                new String[]{"client"}, null, null, null);
        if (client == null || !client.moveToFirst()) {
            if (client != null) client.close();
            return;
        }

        long clientId = client.getLong(client.getColumnIndexOrThrow(COL_USER_ID));
        client.close();

        Cursor existing = db.query(TABLE_MEMBERSHIPS, new String[]{COL_MEM_ID}, COL_MEM_CLIENT_ID + "=?",
                new String[]{String.valueOf(clientId)}, null, null, null);
        if (existing != null && existing.getCount() >= 5) {
            if (existing != null) existing.close();
            return;
        }
        if (existing != null) existing.close();

        seedMembershipHistory(db, clientId);
    }

    public Cursor login(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS, null, COL_USERNAME + "=? AND " + COL_PASSWORD + "=?",
                new String[]{username, password}, null, null, null);
    }

    public Cursor getUserById(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS, null, COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null, null);
    }

    public boolean updateUserProfile(long userId, String fullName, String phone, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FULL_NAME, fullName);
        values.put(COL_PHONE, phone);
        values.put(COL_EMAIL, email);
        if (password != null && !password.trim().isEmpty()) {
            values.put(COL_PASSWORD, password.trim());
        }
        int updated = db.update(TABLE_USERS, values, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
        return updated > 0;
    }

    // ============ МЕТОДЫ ДЛЯ АДМИНИСТРАТОРА ============

    public boolean addTrainer(String name, String phone, String email, String specialization, int experience) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues user = new ContentValues();
        user.put(COL_USERNAME, "trainer_" + System.currentTimeMillis());
        user.put(COL_PASSWORD, "trainer123");
        user.put(COL_ROLE, "trainer");
        user.put(COL_FULL_NAME, name);
        user.put(COL_PHONE, phone);
        user.put(COL_EMAIL, email);
        long userId = db.insert(TABLE_USERS, null, user);

        if (userId != -1) {
            ContentValues trainer = new ContentValues();
            trainer.put(COL_TRAINER_ID, userId);
            trainer.put(COL_SPECIALIZATION, specialization);
            trainer.put(COL_EXPERIENCE, experience);
            db.insert(TABLE_TRAINERS, null, trainer);
            return true;
        }
        return false;
    }

    public boolean updateTrainer(long trainerId, String fullName, String phone, String email, String specialization, int experience) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues userValues = new ContentValues();
        userValues.put(COL_FULL_NAME, fullName);
        userValues.put(COL_PHONE, phone);
        userValues.put(COL_EMAIL, email);
        db.update(TABLE_USERS, userValues, COL_USER_ID + "=?", new String[]{String.valueOf(trainerId)});

        ContentValues trainerValues = new ContentValues();
        trainerValues.put(COL_SPECIALIZATION, specialization);
        trainerValues.put(COL_EXPERIENCE, experience);
        int updated = db.update(TABLE_TRAINERS, trainerValues, COL_TRAINER_ID + "=?", new String[]{String.valueOf(trainerId)});
        return updated > 0;
    }

    public boolean deleteTrainer(long trainerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor schedule = db.query(TABLE_SCHEDULE, null, COL_SCHEDULE_TRAINER_ID + "=?",
                new String[]{String.valueOf(trainerId)}, null, null, null);
        if (schedule != null && schedule.getCount() > 0) {
            schedule.close();
            return false;
        }
        if (schedule != null) schedule.close();

        db.delete(TABLE_TRAINERS, COL_TRAINER_ID + "=?", new String[]{String.valueOf(trainerId)});
        int deleted = db.delete(TABLE_USERS, COL_USER_ID + "=?", new String[]{String.valueOf(trainerId)});
        return deleted > 0;
    }

    public Cursor getAllTrainers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT u.*, t." + COL_SPECIALIZATION + ", t." + COL_EXPERIENCE +
                " FROM " + TABLE_USERS + " u JOIN " + TABLE_TRAINERS + " t ON u." +
                COL_USER_ID + " = t." + COL_TRAINER_ID, null);
    }

    public Cursor getAllClients() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS, null, COL_ROLE + "=?", new String[]{"client"}, null, null, null);
    }

    public Cursor getAllClientsWithTrainer() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT u." + COL_USER_ID + ", u." + COL_USERNAME + ", u." + COL_FULL_NAME + ", u." + COL_PHONE + ", u." + COL_EMAIL +
                        ", COALESCE(t." + COL_FULL_NAME + ", 'Не назначен') AS trainer_name" +
                        ", ca." + COL_CA_ASSIGNED_AT + " AS assigned_at" +
                        ", COALESCE(mt." + COL_MT_NAME + ", 'Без абонемента') AS membership_name" +
                        ", COALESCE(m." + COL_MEM_STATUS + ", 'none') AS membership_status" +
                        " FROM " + TABLE_USERS + " u" +
                        " LEFT JOIN " + TABLE_CLIENT_ASSIGNMENTS + " ca ON ca." + COL_CA_ID + " = (" +
                        "   SELECT ca2." + COL_CA_ID + " FROM " + TABLE_CLIENT_ASSIGNMENTS + " ca2" +
                        "   WHERE ca2." + COL_CA_CLIENT_ID + " = u." + COL_USER_ID +
                        "   ORDER BY ca2." + COL_CA_ASSIGNED_AT + " DESC, ca2." + COL_CA_ID + " DESC LIMIT 1" +
                        " )" +
                        " LEFT JOIN " + TABLE_USERS + " t ON t." + COL_USER_ID + " = ca." + COL_CA_TRAINER_ID +
                        " LEFT JOIN " + TABLE_MEMBERSHIPS + " m ON m." + COL_MEM_ID + " = (" +
                        "   SELECT m2." + COL_MEM_ID + " FROM " + TABLE_MEMBERSHIPS + " m2" +
                        "   WHERE m2." + COL_MEM_CLIENT_ID + " = u." + COL_USER_ID +
                        "   ORDER BY CASE WHEN m2." + COL_MEM_STATUS + " = 'active' THEN 0 ELSE 1 END, m2." + COL_MEM_PURCHASE_DATE + " DESC, m2." + COL_MEM_ID + " DESC LIMIT 1" +
                        " )" +
                        " LEFT JOIN " + TABLE_MEMBERSHIP_TYPES + " mt ON mt." + COL_MT_ID + " = m." + COL_MEM_TYPE_ID +
                        " WHERE u." + COL_ROLE + " = 'client'" +
                        " ORDER BY u." + COL_FULL_NAME + " COLLATE NOCASE",
                null);
    }

    public boolean registerClient(String username, String password, String fullName, String phone, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor check = db.query(TABLE_USERS, null, COL_USERNAME + "=?",
                new String[]{username}, null, null, null);
        if (check != null && check.getCount() > 0) {
            check.close();
            return false;
        }
        if (check != null) check.close();

        ContentValues user = new ContentValues();
        user.put(COL_USERNAME, username);
        user.put(COL_PASSWORD, password);
        user.put(COL_ROLE, "client");
        user.put(COL_FULL_NAME, fullName);
        user.put(COL_PHONE, phone);
        user.put(COL_EMAIL, email);

        long userId = db.insert(TABLE_USERS, null, user);
        return userId != -1;
    }

    public boolean updateClient(long clientId, String username, String password, String fullName, String phone, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        if (password != null && !password.trim().isEmpty()) {
            values.put(COL_PASSWORD, password);
        }
        values.put(COL_FULL_NAME, fullName);
        values.put(COL_PHONE, phone);
        values.put(COL_EMAIL, email);
        int updated = db.update(TABLE_USERS, values, COL_USER_ID + "=? AND " + COL_ROLE + "='client'",
                new String[]{String.valueOf(clientId)});
        return updated > 0;
    }

    public boolean deleteClient(long clientId) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_CLIENT_ASSIGNMENTS, COL_CA_CLIENT_ID + "=?", new String[]{String.valueOf(clientId)});

        Cursor bookings = db.query(TABLE_BOOKINGS, new String[]{COL_BOOKING_ID},
                COL_BOOKING_CLIENT_ID + "=?", new String[]{String.valueOf(clientId)}, null, null, null);
        while (bookings != null && bookings.moveToNext()) {
            long bookingId = bookings.getLong(0);
            db.delete(TABLE_WORKOUT_RESULTS, COL_WR_BOOKING_ID + "=?", new String[]{String.valueOf(bookingId)});
        }
        if (bookings != null) bookings.close();
        db.delete(TABLE_BOOKINGS, COL_BOOKING_CLIENT_ID + "=?", new String[]{String.valueOf(clientId)});

        Cursor plans = db.query(TABLE_WORKOUT_PLANS, new String[]{COL_PLAN_ID},
                COL_PLAN_CLIENT_ID + "=?", new String[]{String.valueOf(clientId)}, null, null, null);
        while (plans != null && plans.moveToNext()) {
            long planId = plans.getLong(0);
            db.delete(TABLE_PLAN_EXERCISES, COL_PE_PLAN_ID + "=?", new String[]{String.valueOf(planId)});
        }
        if (plans != null) plans.close();
        db.delete(TABLE_WORKOUT_PLANS, COL_PLAN_CLIENT_ID + "=?", new String[]{String.valueOf(clientId)});

        db.delete(TABLE_ANTHROPOMETRY, COL_ANTHRO_CLIENT_ID + "=?", new String[]{String.valueOf(clientId)});
        db.delete(TABLE_MEMBERSHIPS, COL_MEM_CLIENT_ID + "=?", new String[]{String.valueOf(clientId)});

        int deleted = db.delete(TABLE_USERS, COL_USER_ID + "=? AND " + COL_ROLE + "='client'",
                new String[]{String.valueOf(clientId)});
        return deleted > 0;
    }

    public boolean assignTrainerToClient(long clientId, long trainerId, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CA_CLIENT_ID, clientId);
        values.put(COL_CA_TRAINER_ID, trainerId);
        values.put(COL_CA_ASSIGNED_AT, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        values.put(COL_CA_NOTE, note);
        return db.insert(TABLE_CLIENT_ASSIGNMENTS, null, values) != -1;
    }

    public boolean clearClientTrainer(long clientId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int deleted = db.delete(TABLE_CLIENT_ASSIGNMENTS, COL_CA_CLIENT_ID + "=?",
                new String[]{String.valueOf(clientId)});
        return deleted >= 0;
    }
    // ============ МЕТОДЫ ДЛЯ РАСПИСАНИЯ ============

    public boolean addSchedule(long trainerId, String workoutType, String date, String time, int duration, int maxClients) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues scheduleValues = new ContentValues();
        scheduleValues.put(COL_SCHEDULE_TRAINER_ID, trainerId);
        scheduleValues.put(COL_WORKOUT_TYPE, workoutType);
        scheduleValues.put(COL_WORKOUT_DATE, date);
        scheduleValues.put(COL_WORKOUT_TIME, time);
        scheduleValues.put(COL_WORKOUT_DURATION, duration);
        scheduleValues.put(COL_MAX_CLIENTS, maxClients);
        scheduleValues.put(COL_CURRENT_CLIENTS, 0);
        return db.insert(TABLE_SCHEDULE, null, scheduleValues) != -1;
    }

    public boolean updateSchedule(long scheduleId, String workoutType, String date, String time, int duration, int maxClients) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_WORKOUT_TYPE, workoutType);
        values.put(COL_WORKOUT_DATE, date);
        values.put(COL_WORKOUT_TIME, time);
        values.put(COL_WORKOUT_DURATION, duration);
        values.put(COL_MAX_CLIENTS, maxClients);
        int updated = db.update(TABLE_SCHEDULE, values, COL_SCHEDULE_ID + "=?", new String[]{String.valueOf(scheduleId)});
        return updated > 0;
    }

    public boolean updateSchedule(long scheduleId, long trainerId, String workoutType, String date, String time, int duration, int maxClients) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SCHEDULE_TRAINER_ID, trainerId);
        values.put(COL_WORKOUT_TYPE, workoutType);
        values.put(COL_WORKOUT_DATE, date);
        values.put(COL_WORKOUT_TIME, time);
        values.put(COL_WORKOUT_DURATION, duration);
        values.put(COL_MAX_CLIENTS, maxClients);
        int updated = db.update(TABLE_SCHEDULE, values, COL_SCHEDULE_ID + "=?", new String[]{String.valueOf(scheduleId)});
        return updated > 0;
    }

    public boolean deleteSchedule(long scheduleId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor bookings = db.query(TABLE_BOOKINGS, null, COL_BOOKING_SCHEDULE_ID + "=?",
                new String[]{String.valueOf(scheduleId)}, null, null, null);
        if (bookings != null && bookings.getCount() > 0) {
            bookings.close();
            return false;
        }
        if (bookings != null) bookings.close();

        int deleted = db.delete(TABLE_SCHEDULE, COL_SCHEDULE_ID + "=?", new String[]{String.valueOf(scheduleId)});
        return deleted > 0;
    }

    public Cursor getAllSchedule() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT s.*, u." + COL_FULL_NAME + " as trainer_name FROM " +
                TABLE_SCHEDULE + " s JOIN " + TABLE_USERS + " u ON s." +
                COL_SCHEDULE_TRAINER_ID + " = u." + COL_USER_ID +
                " ORDER BY s." + COL_WORKOUT_DATE + " ASC", null);
    }

    public Cursor getScheduleById(long scheduleId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT s.*, u." + COL_FULL_NAME + " as trainer_name FROM " +
                TABLE_SCHEDULE + " s JOIN " + TABLE_USERS + " u ON s." +
                COL_SCHEDULE_TRAINER_ID + " = u." + COL_USER_ID +
                " WHERE s." + COL_SCHEDULE_ID + " = ?", new String[]{String.valueOf(scheduleId)});
    }

    public Cursor getAllWorkouts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT s.*, u." + COL_FULL_NAME + " as trainer_name FROM " +
                TABLE_SCHEDULE + " s JOIN " + TABLE_USERS + " u ON s." +
                COL_SCHEDULE_TRAINER_ID + " = u." + COL_USER_ID +
                " ORDER BY s." + COL_WORKOUT_DATE + " DESC", null);
    }

    public Cursor getAvailableWorkouts() {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        return db.rawQuery(
                "SELECT s.*, u." + COL_FULL_NAME + " as trainer_name FROM " +
                        TABLE_SCHEDULE + " s JOIN " + TABLE_USERS + " u ON s." +
                        COL_SCHEDULE_TRAINER_ID + " = u." + COL_USER_ID +
                        " WHERE s." + COL_CURRENT_CLIENTS + " < s." + COL_MAX_CLIENTS +
                        " AND s." + COL_WORKOUT_DATE + " >= ?" +
                        " ORDER BY s." + COL_WORKOUT_DATE + " ASC, s." + COL_WORKOUT_TIME + " ASC",
                new String[]{today});
    }

    // ============ МЕТОДЫ ДЛЯ ТРЕНЕРА ============

    public Cursor getTrainerSchedule(long trainerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_SCHEDULE, null, COL_SCHEDULE_TRAINER_ID + "=?",
                new String[]{String.valueOf(trainerId)}, null, null, COL_WORKOUT_DATE + " ASC");
    }

    public Cursor getTrainerClients(long trainerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT DISTINCT u." + COL_USER_ID + ", u." + COL_FULL_NAME +
                ", u." + COL_PHONE + ", u." + COL_EMAIL +
                " FROM " + TABLE_USERS + " u JOIN " + TABLE_BOOKINGS + " b ON u." +
                COL_USER_ID + " = b." + COL_BOOKING_CLIENT_ID +
                " JOIN " + TABLE_SCHEDULE + " s ON b." + COL_BOOKING_SCHEDULE_ID + " = s." + COL_SCHEDULE_ID +
                " WHERE s." + COL_SCHEDULE_TRAINER_ID + " = ?", new String[]{String.valueOf(trainerId)});
    }

    public long createWorkoutPlan(long clientId, long trainerId, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues plan = new ContentValues();
        plan.put(COL_PLAN_CLIENT_ID, clientId);
        plan.put(COL_PLAN_TRAINER_ID, trainerId);
        plan.put(COL_PLAN_ASSIGNED_DATE, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        plan.put(COL_PLAN_NOTES, notes);
        return db.insert(TABLE_WORKOUT_PLANS, null, plan);
    }

    public boolean addExerciseToPlan(long planId, String exerciseName, int sets, int reps, float weight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues exercise = new ContentValues();
        exercise.put(COL_PE_PLAN_ID, planId);
        exercise.put(COL_PE_EXERCISE_NAME, exerciseName);
        exercise.put(COL_PE_SETS, sets);
        exercise.put(COL_PE_REPS, reps);
        exercise.put(COL_PE_WEIGHT, weight);
        return db.insert(TABLE_PLAN_EXERCISES, null, exercise) != -1;
    }

    public Cursor getClientWorkoutPlan(long clientId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT wp.*, u." + COL_FULL_NAME + " as trainer_name FROM " +
                TABLE_WORKOUT_PLANS + " wp JOIN " + TABLE_USERS + " u ON wp." +
                COL_PLAN_TRAINER_ID + " = u." + COL_USER_ID +
                " WHERE wp." + COL_PLAN_CLIENT_ID + " = ? ORDER BY wp." +
                COL_PLAN_ID + " DESC LIMIT 1", new String[]{String.valueOf(clientId)});
    }

    public long getLatestWorkoutPlanIdForClient(long clientId) {
        Cursor cursor = getClientWorkoutPlan(clientId);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return cursor.getLong(cursor.getColumnIndexOrThrow(COL_PLAN_ID));
                }
            } finally {
                cursor.close();
            }
        }
        return -1;
    }

    public Cursor getPlanExercises(long planId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PLAN_EXERCISES, null, COL_PE_PLAN_ID + "=?",
                new String[]{String.valueOf(planId)}, null, null, null);
    }

    // ============ МЕТОДЫ ДЛЯ ЗАПИСЕЙ ============

    public boolean bookWorkout(long scheduleId, long clientId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues booking = new ContentValues();
        booking.put(COL_BOOKING_SCHEDULE_ID, scheduleId);
        booking.put(COL_BOOKING_CLIENT_ID, clientId);
        booking.put(COL_BOOKING_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        booking.put(COL_BOOKING_STATUS, "confirmed");

        long result = db.insert(TABLE_BOOKINGS, null, booking);
        if (result != -1) {
            db.execSQL("UPDATE " + TABLE_SCHEDULE + " SET " + COL_CURRENT_CLIENTS +
                    " = " + COL_CURRENT_CLIENTS + " + 1 WHERE " + COL_SCHEDULE_ID + " = " + scheduleId);
            return true;
        }
        return false;
    }

    public Cursor getMyBookings(long clientId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT b.*, s." + COL_WORKOUT_TYPE + ", s." + COL_WORKOUT_DATE + ", s." + COL_WORKOUT_TIME +
                ", u." + COL_FULL_NAME + " as trainer_name " +
                "FROM " + TABLE_BOOKINGS + " b JOIN " + TABLE_SCHEDULE + " s ON b." +
                COL_BOOKING_SCHEDULE_ID + " = s." + COL_SCHEDULE_ID + " JOIN " +
                TABLE_USERS + " u ON s." + COL_SCHEDULE_TRAINER_ID + " = u." + COL_USER_ID +
                " WHERE b." + COL_BOOKING_CLIENT_ID + " = " + clientId, null);
    }

    public boolean saveWorkoutResult(long bookingId, String exerciseName, int sets, int reps, float weight, String feeling) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues result = new ContentValues();
        result.put(COL_WR_BOOKING_ID, bookingId);
        result.put(COL_WR_EXERCISE_NAME, exerciseName);
        result.put(COL_WR_SETS_COMPLETED, sets);
        result.put(COL_WR_REPS_COMPLETED, reps);
        result.put(COL_WR_WEIGHT_USED, weight);
        result.put(COL_WR_FEELING, feeling);

        long id = db.insert(TABLE_WORKOUT_RESULTS, null, result);
        if (id != -1) {
            ContentValues update = new ContentValues();
            update.put(COL_BOOKING_STATUS, "completed");
            db.update(TABLE_BOOKINGS, update, COL_BOOKING_ID + "=?", new String[]{String.valueOf(bookingId)});
            return true;
        }
        return false;
    }

    public boolean markBookingCompleted(long bookingId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues update = new ContentValues();
        update.put(COL_BOOKING_STATUS, "completed");
        return db.update(TABLE_BOOKINGS, update, COL_BOOKING_ID + "=?", new String[]{String.valueOf(bookingId)}) > 0;
    }

    public int getCompletedWorkoutsCount(long clientId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BOOKINGS +
                " WHERE " + COL_BOOKING_CLIENT_ID + " = ? AND " +
                COL_BOOKING_STATUS + " = 'completed'", new String[]{String.valueOf(clientId)});
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    // ============ МЕТОДЫ ДЛЯ АНТРОПОМЕТРИИ ============

    public boolean saveMeasurement(long clientId, String date, float weight, float height,
                                   float biceps, float chest, float waist) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ANTHRO_CLIENT_ID, clientId);
        values.put(COL_ANTHRO_DATE, date);
        values.put(COL_ANTHRO_WEIGHT, weight);
        values.put(COL_ANTHRO_HEIGHT, height);
        values.put(COL_ANTHRO_BICEPS, biceps);
        values.put(COL_ANTHRO_CHEST, chest);
        values.put(COL_ANTHRO_WAIST, waist);
        Cursor cursor = db.query(TABLE_ANTHROPOMETRY, new String[]{COL_ANTHRO_ID},
                COL_ANTHRO_CLIENT_ID + "=? AND " + COL_ANTHRO_DATE + "=?",
                new String[]{String.valueOf(clientId), date}, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                long anthroId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ANTHRO_ID));
                return db.update(TABLE_ANTHROPOMETRY, values, COL_ANTHRO_ID + "=?",
                        new String[]{String.valueOf(anthroId)}) > 0;
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return db.insert(TABLE_ANTHROPOMETRY, null, values) != -1;
    }

    public Cursor getAllMeasurements(long clientId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ANTHROPOMETRY, null, COL_ANTHRO_CLIENT_ID + "=?",
                new String[]{String.valueOf(clientId)}, null, null, COL_ANTHRO_DATE + " DESC");
    }

    public Cursor getMembership(long clientId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_MEMBERSHIPS, null, COL_MEM_CLIENT_ID + "=?",
                new String[]{String.valueOf(clientId)}, null, null, null);
    }

    public String getUserName(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_FULL_NAME},
                COL_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(0);
            cursor.close();
            return name;
        }
        return "";
    }

    // ============ МЕТОДЫ ДЛЯ АБОНЕМЕНТОВ ============

    public Cursor getAllMembershipTypes() {
        SQLiteDatabase db = this.getReadableDatabase();
        // Убедитесь, что выбираются только активные абонементы
        return db.query(TABLE_MEMBERSHIP_TYPES, null, COL_MT_IS_ACTIVE + "=1", null, null, null, null);
    }

    public Cursor getAllMembershipTypesAdmin() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_MEMBERSHIP_TYPES, null, null, null, null, null, null);
    }
    // Получение истории всех абонементов клиента
    public Cursor getClientMembershipHistory(long clientId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT m.*, mt." + COL_MT_NAME + ", mt." + COL_MT_DESCRIPTION + ", mt." + COL_MT_DURATION_DAYS + ", mt." + COL_MT_PRICE +
                        " FROM " + TABLE_MEMBERSHIPS + " m " +
                        " LEFT JOIN " + TABLE_MEMBERSHIP_TYPES + " mt ON m." + COL_MEM_TYPE_ID + " = mt." + COL_MT_ID +
                        " WHERE m." + COL_MEM_CLIENT_ID + " = ? ORDER BY m." + COL_MEM_PURCHASE_DATE + " DESC",
                new String[]{String.valueOf(clientId)});
    }
    public Cursor getClientActiveMembership(long clientId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        return db.rawQuery(
                "SELECT m.*, mt." + COL_MT_NAME + ", mt." + COL_MT_DESCRIPTION + ", mt." + COL_MT_DURATION_DAYS + ", mt." + COL_MT_PRICE +
                        " FROM " + TABLE_MEMBERSHIPS + " m " +
                        " LEFT JOIN " + TABLE_MEMBERSHIP_TYPES + " mt ON m." + COL_MEM_TYPE_ID + " = mt." + COL_MT_ID +
                        " WHERE m." + COL_MEM_CLIENT_ID + " = ? AND m." + COL_MEM_STATUS + " = 'active' AND m." + COL_MEM_END_DATE + " >= ?",
                new String[]{String.valueOf(clientId), today});
    }

    public boolean purchaseMembership(long clientId, long typeId) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            Cursor type = db.query(TABLE_MEMBERSHIP_TYPES, null, COL_MT_ID + "=?",
                    new String[]{String.valueOf(typeId)}, null, null, null);
            if (type == null || !type.moveToFirst()) {
                if (type != null) type.close();
                return false;
            }

            int durationDays = type.getInt(type.getColumnIndexOrThrow(COL_MT_DURATION_DAYS));
            String typeName = type.getString(type.getColumnIndexOrThrow(COL_MT_NAME));
            type.close();

            // Деактивируем старые абонементы
            ContentValues deactivateValues = new ContentValues();
            deactivateValues.put(COL_MEM_STATUS, "expired");
            db.update(TABLE_MEMBERSHIPS, deactivateValues,
                    COL_MEM_CLIENT_ID + "=? AND " + COL_MEM_STATUS + "='active'",
                    new String[]{String.valueOf(clientId)});

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String startDate = sdf.format(new Date());

            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.DAY_OF_YEAR, durationDays);
            String endDate = sdf.format(cal.getTime());

            ContentValues membership = new ContentValues();
            membership.put(COL_MEM_CLIENT_ID, clientId);
            membership.put(COL_MEM_TYPE_ID, typeId);
            membership.put(COL_MEM_START_DATE, startDate);
            membership.put(COL_MEM_END_DATE, endDate);
            membership.put(COL_MEM_STATUS, "active");
            membership.put(COL_MEM_PURCHASE_DATE, sdf.format(new Date()));
            membership.put(COL_MEM_TYPE_NAME, typeName);

            long result = db.insert(TABLE_MEMBERSHIPS, null, membership);
            return result != -1;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveMembershipApplication(long clientId, long typeId, String paymentMethod, String goal, String timeSlot, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MA_CLIENT_ID, clientId);
        values.put(COL_MA_TYPE_ID, typeId);
        values.put(COL_MA_PAYMENT_METHOD, paymentMethod);
        values.put(COL_MA_GOAL, goal);
        values.put(COL_MA_TIME_SLOT, timeSlot);
        values.put(COL_MA_NOTE, note);
        values.put(COL_MA_CREATED_AT, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        values.put(COL_MA_STATUS, "confirmed");
        return db.insert(TABLE_MEMBERSHIP_APPLICATIONS, null, values) != -1;
    }

    public Cursor getLatestMembershipApplication(long clientId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT a.*, mt." + COL_MT_NAME + ", mt." + COL_MT_DESCRIPTION + ", mt." + COL_MT_DURATION_DAYS + ", mt." + COL_MT_PRICE +
                        " FROM " + TABLE_MEMBERSHIP_APPLICATIONS + " a" +
                        " LEFT JOIN " + TABLE_MEMBERSHIP_TYPES + " mt ON a." + COL_MA_TYPE_ID + " = mt." + COL_MT_ID +
                        " WHERE a." + COL_MA_CLIENT_ID + " = ?" +
                        " ORDER BY a." + COL_MA_CREATED_AT + " DESC, a." + COL_MA_ID + " DESC LIMIT 1",
                new String[]{String.valueOf(clientId)});
    }

    public boolean createMembershipType(String name, String description, int durationDays, int price) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MT_NAME, name);
        values.put(COL_MT_DESCRIPTION, description);
        values.put(COL_MT_DURATION_DAYS, durationDays);
        values.put(COL_MT_PRICE, price);
        values.put(COL_MT_IS_ACTIVE, 1);
        return db.insert(TABLE_MEMBERSHIP_TYPES, null, values) != -1;
    }

    public boolean updateMembershipType(long typeId, String name, String description, int durationDays, int price, boolean isActive) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MT_NAME, name);
        values.put(COL_MT_DESCRIPTION, description);
        values.put(COL_MT_DURATION_DAYS, durationDays);
        values.put(COL_MT_PRICE, price);
        values.put(COL_MT_IS_ACTIVE, isActive ? 1 : 0);
        int updated = db.update(TABLE_MEMBERSHIP_TYPES, values, COL_MT_ID + "=?", new String[]{String.valueOf(typeId)});
        return updated > 0;
    }

    public boolean deleteMembershipType(long typeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor check = db.query(TABLE_MEMBERSHIPS, null, COL_MEM_TYPE_ID + "=?",
                new String[]{String.valueOf(typeId)}, null, null, null);
        if (check != null && check.getCount() > 0) {
            check.close();
            return false;
        }
        if (check != null) check.close();

        int deleted = db.delete(TABLE_MEMBERSHIP_TYPES, COL_MT_ID + "=?", new String[]{String.valueOf(typeId)});
        return deleted > 0;
    }

    public boolean hasActiveMembership(long clientId) {
        Cursor cursor = getClientActiveMembership(clientId);
        boolean hasActive = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        return hasActive;
    }
}
