package com.example.fitbook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ClientActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private long clientId;
    private ListView listView;
    private ArrayList<Long> itemIds;

    private ArrayAdapter<String> adapter;
    private ArrayList<String> dataList;
    private TextView tvClientName, tvMembershipType, tvMembershipStatus, tvMembershipEndDate, tvMembershipDaysLeft, tvMembershipPaymentMethod, tvMembershipGoal, tvMembershipTimeSlot, tvSectionTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AuthGuard.requireRole(this, "client")) return;
        setContentView(R.layout.activity_client);

        dbHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        clientId = prefs.getLong(DatabaseHelper.COL_USER_ID, 0);
        String clientName = prefs.getString(DatabaseHelper.COL_FULL_NAME, "Клиент");
        itemIds = new ArrayList<>();
        tvClientName = findViewById(R.id.tvClientName);
        tvMembershipType = findViewById(R.id.tvMembershipType);
        tvMembershipStatus = findViewById(R.id.tvMembershipStatus);
        tvMembershipEndDate = findViewById(R.id.tvMembershipEndDate);
        tvMembershipDaysLeft = findViewById(R.id.tvMembershipDaysLeft);
        tvMembershipPaymentMethod = findViewById(R.id.tvMembershipPaymentMethod);
        tvMembershipGoal = findViewById(R.id.tvMembershipGoal);
        tvMembershipTimeSlot = findViewById(R.id.tvMembershipTimeSlot);
        tvMembershipStatus = findViewById(R.id.tvMembershipStatus);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        listView = findViewById(R.id.listView);
        tvClientName.setText(clientName);
        tvSectionTitle.setText(getString(R.string.client_workouts_title));

        dataList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, R.layout.item_dark_list_text, dataList);
        listView.setAdapter(adapter);

        // Кнопки
        findViewById(R.id.btnAvailableWorkouts).setOnClickListener(v -> startActivity(new Intent(this, ClientWorkoutsActivity.class)));
        findViewById(R.id.btnMyBookings).setOnClickListener(v -> startActivity(new Intent(this, ClientBookingsActivity.class)));
        findViewById(R.id.btnMyPlan).setOnClickListener(v -> startActivity(new Intent(this, ClientPlanActivity.class)));
        findViewById(R.id.btnAnthropometry).setOnClickListener(v -> showAnthropometryDialog());
        findViewById(R.id.btnOpenProfile).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        findViewById(R.id.cardMembershipSummary).setOnClickListener(v -> openMembershipsScreen());
        findViewById(R.id.btnOpenMemberships).setOnClickListener(v -> openMembershipsScreen());
        setupBottomNavigation();

        loadMembershipStatus();
        loadMembershipApplicationSummary();
        loadAvailableWorkouts();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_workouts);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_workouts) {
                startActivity(new Intent(this, ClientWorkoutsActivity.class));
                return true;
            } else if (itemId == R.id.nav_bookings) {
                tvSectionTitle.setText(getString(R.string.client_bookings_title));
                loadMyBookings();
                return true;
            } else if (itemId == R.id.nav_plan) {
                startActivity(new Intent(this, ClientPlanActivity.class));
                return true;
            } else if (itemId == R.id.nav_progress) {
                startActivity(new Intent(this, ClientProgressActivity.class));
                return true;
            } else if (itemId == R.id.nav_contacts) {
                startActivity(new Intent(this, ContactsActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadMembershipStatus() {
        Cursor activeMembership = dbHelper.getClientActiveMembership(clientId);
        if (activeMembership != null && activeMembership.moveToFirst()) {
            String typeName = getCursorString(activeMembership, DatabaseHelper.COL_MT_NAME, "Без абонемента");
            String endDate = getCursorString(activeMembership, DatabaseHelper.COL_MEM_END_DATE, "—");
            String daysLeft = getDaysLeftText(endDate);
            String status = getMembershipStatusText(endDate);

            tvMembershipType.setText(typeName);
            tvMembershipStatus.setText(status);
            tvMembershipEndDate.setText(endDate);
            tvMembershipDaysLeft.setText(daysLeft);
            tvMembershipStatus.setTextColor(getColor(R.color.fitbook_text_on_accent));
            tvMembershipType.setTextColor(getColor(R.color.fitbook_text_primary));
            tvMembershipEndDate.setTextColor(getColor(R.color.fitbook_text_primary));
            tvMembershipDaysLeft.setTextColor(getColor(R.color.fitbook_text_primary));
            activeMembership.close();
        } else {
            tvMembershipType.setText("Нет активного абонемента");
            tvMembershipStatus.setText("Пора выбрать абонемент");
            tvMembershipEndDate.setText("—");
            tvMembershipDaysLeft.setText("—");
            tvMembershipStatus.setTextColor(getColor(R.color.fitbook_text_on_accent));
            tvMembershipType.setTextColor(getColor(R.color.fitbook_text_primary));
            tvMembershipEndDate.setTextColor(getColor(R.color.fitbook_text_secondary));
            tvMembershipDaysLeft.setTextColor(getColor(R.color.fitbook_text_secondary));
        }
    }

    private void openMembershipsScreen() {
        startActivity(new Intent(this, MembershipsActivity.class));
    }

    private void loadMembershipApplicationSummary() {
        Cursor latestApplication = dbHelper.getLatestMembershipApplication(clientId);
        if (latestApplication != null && latestApplication.moveToFirst()) {
            tvMembershipPaymentMethod.setText(getCursorString(latestApplication, DatabaseHelper.COL_MA_PAYMENT_METHOD, "—"));
            tvMembershipGoal.setText(getCursorString(latestApplication, DatabaseHelper.COL_MA_GOAL, "—"));
            tvMembershipTimeSlot.setText(getCursorString(latestApplication, DatabaseHelper.COL_MA_TIME_SLOT, "—"));
            latestApplication.close();
        } else {
            tvMembershipPaymentMethod.setText("—");
            tvMembershipGoal.setText("—");
            tvMembershipTimeSlot.setText("—");
        }
    }

    private String getCursorString(Cursor cursor, String columnName, String fallback) {
        int columnIndex = cursor.getColumnIndexOrThrow(columnName);
        String value = cursor.isNull(columnIndex) ? null : cursor.getString(columnIndex);
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String getMembershipStatusText(String endDate) {
        long daysLeft = getDaysLeft(endDate);
        if (daysLeft > 1) {
            return "Активен";
        } else if (daysLeft == 1) {
            return "Истекает завтра";
        } else if (daysLeft == 0) {
            return "Истекает сегодня";
        }
        return "Истёк";
    }

    private String getDaysLeftText(String endDate) {
        long daysLeft = getDaysLeft(endDate);
        if (daysLeft > 1) {
            return daysLeft + " дней";
        } else if (daysLeft == 1) {
            return "1 день";
        } else if (daysLeft == 0) {
            return "Сегодня";
        }
        return "0 дней";
    }

    private long getDaysLeft(String endDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date end = sdf.parse(endDate);
            if (end == null) {
                return -1;
            }
            long diffMillis = end.getTime() - new Date().getTime();
            return TimeUnit.MILLISECONDS.toDays(diffMillis);
        } catch (Exception e) {
            return -1;
        }
    }

    private void loadAvailableWorkouts() {
        dataList.clear();

        Cursor workouts = dbHelper.getAvailableWorkouts();

        if (workouts != null && workouts.moveToFirst()) {
            final ArrayList<Long> scheduleIds = new ArrayList<>();

            do {
                try {
                    long scheduleId = workouts.getLong(workouts.getColumnIndexOrThrow(DatabaseHelper.COL_SCHEDULE_ID));
                    String workoutType = workouts.getString(workouts.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_TYPE));
                    String date = workouts.getString(workouts.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_DATE));
                    String time = workouts.getString(workouts.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_TIME));
                    String trainer = workouts.getString(workouts.getColumnIndexOrThrow("trainer_name"));
                    int duration = workouts.getInt(workouts.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_DURATION));
                    int current = workouts.getInt(workouts.getColumnIndexOrThrow(DatabaseHelper.COL_CURRENT_CLIENTS));
                    int max = workouts.getInt(workouts.getColumnIndexOrThrow(DatabaseHelper.COL_MAX_CLIENTS));

                    scheduleIds.add(scheduleId);

                    String workoutInfo = workoutType +
                            "\nДата: " + DateFormatUtils.formatRussianDate(date) + " " + time + " (" + duration + " мин)" +
                            "\nТренер: " + trainer +
                            "\nСвободно мест: " + (max - current) + "/" + max +
                            "\nНажмите для записи";
                    dataList.add(workoutInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (workouts.moveToNext());
            workouts.close();

            if (dataList.isEmpty()) {
            dataList.add("Нет доступных тренировок на ближайшие дни");
            }

            adapter.notifyDataSetChanged();

            listView.setOnItemClickListener((parent, view, position, id) -> {
                if (position < scheduleIds.size()) {
                    bookWorkout(scheduleIds.get(position));
                }
            });
        } else {
            dataList.add("Нет доступных тренировок");
            if (workouts != null) workouts.close();
            adapter.notifyDataSetChanged();
        }
    }

    private void bookWorkout(long scheduleId) {
        boolean success = dbHelper.bookWorkout(scheduleId, clientId);
        if (success) {
                Toast.makeText(this, "Вы успешно записались на тренировку", Toast.LENGTH_LONG).show();
            loadAvailableWorkouts();
            loadMyBookings();
        } else {
            Toast.makeText(this, "Не удалось записаться. Возможно, мест уже нет.", Toast.LENGTH_LONG).show();
        }
    }

    private void loadMyBookings() {
        dataList.clear();
        Cursor bookings = dbHelper.getMyBookings(clientId);

        if (bookings != null && bookings.moveToFirst()) {
            final ArrayList<Long> bookingIds = new ArrayList<>();

            do {
                long bookingId = bookings.getLong(bookings.getColumnIndexOrThrow(DatabaseHelper.COL_BOOKING_ID));
                String workoutType = bookings.getString(bookings.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_TYPE));
                String date = bookings.getString(bookings.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_DATE));
                String time = bookings.getString(bookings.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_TIME));
                String trainer = bookings.getString(bookings.getColumnIndexOrThrow("trainer_name"));
                String status = bookings.getString(bookings.getColumnIndexOrThrow(DatabaseHelper.COL_BOOKING_STATUS));

                bookingIds.add(bookingId);

                String statusText = status.equals("confirmed") ? "Ожидает" :
                        (status.equals("completed") ? "Выполнено" : "Отменено");

                dataList.add(workoutType +
                        "\nДата: " + DateFormatUtils.formatRussianDate(date) + " " + time +
                        "\nТренер: " + trainer +
                        "\nСтатус: " + statusText);

                if (status.equals("confirmed")) {
                dataList.add("Нажмите, чтобы отметить выполнение");
                }
            } while (bookings.moveToNext());
            bookings.close();

            adapter.notifyDataSetChanged();

            listView.setOnItemClickListener((parent, view, position, id) -> {
                if (position < bookingIds.size()) {
                    Cursor check = dbHelper.getMyBookings(clientId);
                    if (check != null && check.moveToPosition(position)) {
                        String status = check.getString(check.getColumnIndexOrThrow(DatabaseHelper.COL_BOOKING_STATUS));
                        if (status.equals("confirmed")) {
                            showWorkoutResultDialog(bookingIds.get(position));
                        }
                        check.close();
                    }
                }
            });
        } else {
            dataList.add("У вас пока нет записей на тренировки");
            adapter.notifyDataSetChanged();
        }
    }

    private void loadMyPlan() {
        dataList.clear();
        Cursor plan = dbHelper.getClientWorkoutPlan(clientId);

        if (plan != null && plan.moveToFirst()) {
            String trainerName = plan.getString(plan.getColumnIndexOrThrow("trainer_name"));
            String assignedDate = plan.getString(plan.getColumnIndexOrThrow(DatabaseHelper.COL_PLAN_ASSIGNED_DATE));
            String notes = plan.getString(plan.getColumnIndexOrThrow(DatabaseHelper.COL_PLAN_NOTES));
            long planId = plan.getLong(plan.getColumnIndexOrThrow(DatabaseHelper.COL_PLAN_ID));

            dataList.add("Тренировочный план");
            dataList.add("Тренер: " + trainerName);
            dataList.add("Назначен: " + DateFormatUtils.formatRussianDate(assignedDate));
            dataList.add("Заметки: " + notes);
            dataList.add("");
            dataList.add("Упражнения:");

            Cursor exercises = dbHelper.getPlanExercises(planId);
            if (exercises != null && exercises.moveToFirst()) {
                do {
                    String exName = exercises.getString(exercises.getColumnIndexOrThrow(DatabaseHelper.COL_PE_EXERCISE_NAME));
                    int sets = exercises.getInt(exercises.getColumnIndexOrThrow(DatabaseHelper.COL_PE_SETS));
                    int reps = exercises.getInt(exercises.getColumnIndexOrThrow(DatabaseHelper.COL_PE_REPS));
                    float weight = exercises.getFloat(exercises.getColumnIndexOrThrow(DatabaseHelper.COL_PE_WEIGHT));

                    dataList.add(exName + ": " + sets + " x " + reps + " (" + weight + " кг)");
                } while (exercises.moveToNext());
                exercises.close();
            } else {
                dataList.add("Нет добавленных упражнений");
            }
            plan.close();
        } else {
            dataList.add("Тренировочный план");
            dataList.add("У вас пока нет назначенного плана");
            dataList.add("");
            dataList.add("Обратитесь к вашему тренеру");
            dataList.add("для составления программы тренировок");
        }
        adapter.notifyDataSetChanged();
    }

    private void showAnthropometryDialog() {
        AlertDialog.Builder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_client_anthropometry, null);

        final EditText etWeight = view.findViewById(R.id.etWeight);
        final EditText etHeight = view.findViewById(R.id.etHeight);
        final EditText etBiceps = view.findViewById(R.id.etBiceps);
        final EditText etChest = view.findViewById(R.id.etChest);
        final EditText etWaist = view.findViewById(R.id.etWaist);

        builder.setTitle("Замеры тела")
                .setView(view)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    try {
                        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                        float weight = Float.parseFloat(etWeight.getText().toString());
                        float height = Float.parseFloat(etHeight.getText().toString());
                        float biceps = Float.parseFloat(etBiceps.getText().toString());
                        float chest = Float.parseFloat(etChest.getText().toString());
                        float waist = Float.parseFloat(etWaist.getText().toString());

                        boolean success = dbHelper.saveMeasurement(clientId, date, weight, height, biceps, chest, waist);
                        Toast.makeText(this, success ? "Замеры сохранены" : "Ошибка сохранения", Toast.LENGTH_SHORT).show();
                        if (success) showProgress();
                    } catch (Exception e) {
                        Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showProgress() {
        dataList.clear();

        // Статистика тренировок
        int completedCount = dbHelper.getCompletedWorkoutsCount(clientId);
        dataList.add("Статистика тренировок");
        dataList.add("Проведено тренировок: " + completedCount);
        dataList.add("");

        // История замеров
        dataList.add("История замеров");

        Cursor measurements = dbHelper.getAllMeasurements(clientId);
        if (measurements != null && measurements.moveToFirst()) {
            float firstWeight = -1, lastWeight = -1;
            int count = 0;

            do {
                String date = measurements.getString(measurements.getColumnIndexOrThrow(DatabaseHelper.COL_ANTHRO_DATE));
                float weight = measurements.getFloat(measurements.getColumnIndexOrThrow(DatabaseHelper.COL_ANTHRO_WEIGHT));
                float biceps = measurements.getFloat(measurements.getColumnIndexOrThrow(DatabaseHelper.COL_ANTHRO_BICEPS));
                float chest = measurements.getFloat(measurements.getColumnIndexOrThrow(DatabaseHelper.COL_ANTHRO_CHEST));
                float waist = measurements.getFloat(measurements.getColumnIndexOrThrow(DatabaseHelper.COL_ANTHRO_WAIST));

                if (count == 0) firstWeight = weight;
                lastWeight = weight;
                count++;

                dataList.add(DateFormatUtils.formatRussianDate(date));
                dataList.add("Вес: " + weight + " кг");
                dataList.add("Бицепс: " + biceps + " см");
                dataList.add("Грудная клетка: " + chest + " см");
                dataList.add("Талия: " + waist + " см");
                dataList.add("");

            } while (measurements.moveToNext());
            measurements.close();

            if (count >= 2 && firstWeight != -1 && lastWeight != -1) {
                float weightChange = lastWeight - firstWeight;
                String changeText = weightChange > 0 ? "+" + String.format("%.1f", weightChange) : String.format("%.1f", weightChange);
                dataList.add("Итоговый прогресс:");
                dataList.add("Изменение веса: " + changeText + " кг");
                dataList.add("Количество замеров: " + count);
            }
        } else {
            dataList.add("Нет данных о замерах");
            dataList.add("Добавьте первый замер в разделе Замеры");
        }

        adapter.notifyDataSetChanged();
    }

    private void showWorkoutResultDialog(final long bookingId) {
        AlertDialog.Builder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_client_workout_result, null);

        final EditText etExerciseName = view.findViewById(R.id.etExerciseName);
        final EditText etSets = view.findViewById(R.id.etSets);
        final EditText etReps = view.findViewById(R.id.etReps);
        final EditText etWeight = view.findViewById(R.id.etWeight);
        final Spinner spinnerFeeling = view.findViewById(R.id.spinnerFeeling);

        String[] feelings = {"Отлично", "Хорошо", "Устал", "Плохо"};
        ArrayAdapter<String> feelingAdapter = new ArrayAdapter<>(this, R.layout.item_dropdown_dark, feelings);
        feelingAdapter.setDropDownViewResource(R.layout.item_dropdown_dark_dropdown);
        spinnerFeeling.setAdapter(feelingAdapter);

        builder.setTitle("Отметить выполнение тренировки")
                .setView(view)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    try {
                        String exerciseName = etExerciseName.getText().toString();
                        int sets = Integer.parseInt(etSets.getText().toString());
                        int reps = Integer.parseInt(etReps.getText().toString());
                        float weight = Float.parseFloat(etWeight.getText().toString());
                        String feeling = feelings[spinnerFeeling.getSelectedItemPosition()];

                        boolean success = dbHelper.saveWorkoutResult(bookingId, exerciseName, sets, reps, weight, feeling);
                        Toast.makeText(this, success ? "Результаты сохранены" : "Ошибка сохранения", Toast.LENGTH_SHORT).show();
                        if (success) loadMyBookings();
                    } catch (Exception e) {
                        Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    // ============ МЕТОДЫ ДЛЯ АБОНЕМЕНТОВ ============

    private void showMembershipSection() {
        dataList.clear();
        itemIds.clear();

        try {
            dataList.add("Мои абонементы");

            Cursor activeMembership = dbHelper.getClientActiveMembership(clientId);
            if (activeMembership != null && activeMembership.moveToFirst()) {
                String name = activeMembership.getString(activeMembership.getColumnIndexOrThrow(DatabaseHelper.COL_MT_NAME));
                String endDate = activeMembership.getString(activeMembership.getColumnIndexOrThrow(DatabaseHelper.COL_MEM_END_DATE));
                int price = activeMembership.getInt(activeMembership.getColumnIndexOrThrow(DatabaseHelper.COL_MT_PRICE));
                String purchaseDate = activeMembership.getString(activeMembership.getColumnIndexOrThrow(DatabaseHelper.COL_MEM_PURCHASE_DATE));

                dataList.add("Активный абонемент\nНазвание: " + name + "\nСтоимость: " + price + " ₽\nПриобретен: " + DateFormatUtils.formatRussianDate(purchaseDate) + "\nДействует до: " + DateFormatUtils.formatRussianDate(endDate));
                activeMembership.close();
            } else {
                dataList.add("Нет активного абонемента");
                dataList.add("У вас нет активного абонемента");
                dataList.add("Приобретите абонемент для посещения тренировок в нашем клубе");
            }

            dataList.add("");
            dataList.add("История абонементов");

            Cursor history = dbHelper.getClientMembershipHistory(clientId);
            if (history != null && history.moveToFirst()) {
                boolean hasHistory = false;
                do {
                    String name = history.getString(history.getColumnIndexOrThrow(DatabaseHelper.COL_MT_NAME));
                    String startDate = history.getString(history.getColumnIndexOrThrow(DatabaseHelper.COL_MEM_START_DATE));
                    String endDate = history.getString(history.getColumnIndexOrThrow(DatabaseHelper.COL_MEM_END_DATE));
                    String status = history.getString(history.getColumnIndexOrThrow(DatabaseHelper.COL_MEM_STATUS));
                    int price = history.getInt(history.getColumnIndexOrThrow(DatabaseHelper.COL_MT_PRICE));

                    hasHistory = true;
                    String statusText = "active".equals(status) ? "Активен" : "Завершен";
                    dataList.add(name + "\nСтоимость: " + price + " ₽\nПериод: " + DateFormatUtils.formatRussianDate(startDate) + " - " + DateFormatUtils.formatRussianDate(endDate) + "\nСтатус: " + statusText);
                    dataList.add("");
                } while (history.moveToNext());
                history.close();

                if (!hasHistory) {
                    dataList.add("Нет истории абонементов");
                }
            } else {
                dataList.add("Нет истории абонементов");
            }

            dataList.add("");
            dataList.add("Доступные абонементы");

            Cursor types = dbHelper.getAllMembershipTypes();
            if (types != null && types.moveToFirst()) {
                final ArrayList<Long> typeIds = new ArrayList<>();
                boolean hasItems = false;

                do {
                    long typeId = types.getLong(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_ID));
                    String name = types.getString(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_NAME));
                    String description = types.getString(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_DESCRIPTION));
                    int durationDays = types.getInt(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_DURATION_DAYS));
                    int price = types.getInt(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_PRICE));
                    int isActive = types.getInt(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_IS_ACTIVE));

                    if (isActive == 1) {
                        hasItems = true;
                        typeIds.add(typeId);
                        dataList.add(name + "\nОписание: " + description + "\nСрок: " + durationDays + " дней\nЦена: " + price + " ₽\nНажмите для покупки [ID:" + typeId + "]");
                        dataList.add("");
                    }
                } while (types.moveToNext());
                types.close();

                if (hasItems) {
                    listView.setOnItemClickListener((parent, view, position, id) -> {
                        String selected = dataList.get(position);
                        if (selected.contains("Нажмите для покупки")) {
                            long extractedId = extractMembershipIdFromText(selected);
                            if (extractedId != -1 && typeIds.contains(extractedId)) {
                                showPurchaseMembershipDialog(extractedId);
                            }
                        }
                    });
                } else {
                    dataList.add("Нет доступных абонементов");
                }
            } else {
                dataList.add("Нет доступных абонементов");
                dataList.add("Обратитесь к администратору для добавления");
            }

            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
            dataList.clear();
            dataList.add("Ошибка загрузки");
            dataList.add(e.getMessage());
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showPurchaseMembershipDialog(final long typeId) {
        Cursor types = dbHelper.getAllMembershipTypes();
        String name = "";
        String description = "";
        int durationDays = 0;
        int price = 0;

        if (types != null) {
            while (types.moveToNext()) {
                if (types.getLong(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_ID)) == typeId) {
                    name = types.getString(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_NAME));
                    description = types.getString(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_DESCRIPTION));
                    durationDays = types.getInt(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_DURATION_DAYS));
                    price = types.getInt(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_PRICE));
                    break;
                }
            }
            types.close();
        }

        final String finalName = name;
        final String finalDescription = description;
        final int finalDurationDays = durationDays;
        final int finalPrice = price;

        AlertDialog.Builder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        builder.setTitle("Покупка абонемента")
                .setMessage("Название: " + finalName + "\n" +
                        "Описание: " + finalDescription + "\n" +
                        "Срок: " + finalDurationDays + " дней\n" +
                        "Стоимость: " + finalPrice + " ₽\n" +
                        "Старый абонемент будет автоматически деактивирован")
                .setPositiveButton("Купить", (dialog, which) -> {
                    boolean success = dbHelper.purchaseMembership(clientId, typeId);
                    if (success) {
                        Toast.makeText(this, "Абонемент \"" + finalName + "\" успешно приобретен", Toast.LENGTH_LONG).show();
                        openMembershipsScreen();
                        loadMembershipStatus();
                    } else {
                        Toast.makeText(this, "Ошибка при покупке абонемента", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private long extractMembershipIdFromText(String text) {
        try {
            int start = text.indexOf("[ID:") + 4;
            int end = text.indexOf("]", start);
            return Long.parseLong(text.substring(start, end));
        } catch (Exception e) {
            return -1;
        }
    }
    private void logout() {
        AlertDialog.Builder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        builder.setTitle("Выход")
                .setMessage("Вы уверены, что хотите выйти?")
                .setPositiveButton("Да", (dialog, which) -> {
                    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    prefs.edit().clear().apply();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}

