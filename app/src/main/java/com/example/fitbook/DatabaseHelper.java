package com.example.fitbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "fitness_club.db";
    private static final int DATABASE_VERSION = 8;

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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKOUT_RESULTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAN_EXERCISES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKOUT_PLANS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLIENT_ASSIGNMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHEDULE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRAINERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEMBERSHIPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEMBERSHIP_TYPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANTHROPOMETRY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // ============ МЕТОДЫ ДЛЯ АУТЕНТИФИКАЦИИ ============

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
