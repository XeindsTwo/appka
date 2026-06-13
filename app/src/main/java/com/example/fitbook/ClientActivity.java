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
    private TextView tvClientName, tvMembershipType, tvMembershipStatus, tvMembershipEndDate, tvMembershipDaysLeft, tvSectionTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        dbHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        clientId = prefs.getLong(DatabaseHelper.COL_USER_ID, 0);
        String clientName = prefs.getString(DatabaseHelper.COL_FULL_NAME, "Р С™Р В»Р С‘Р ВµР Р…РЎвЂљ");
        itemIds = new ArrayList<>();
        tvClientName = findViewById(R.id.tvClientName);
        tvMembershipType = findViewById(R.id.tvMembershipType);
        tvMembershipStatus = findViewById(R.id.tvMembershipStatus);
        tvMembershipEndDate = findViewById(R.id.tvMembershipEndDate);
        tvMembershipDaysLeft = findViewById(R.id.tvMembershipDaysLeft);
        tvMembershipStatus = findViewById(R.id.tvMembershipStatus);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        listView = findViewById(R.id.listView);
        tvClientName.setText(clientName);
        tvSectionTitle.setText("Р вЂќР С•РЎРѓРЎвЂљРЎС“Р С—Р Р…РЎвЂ№Р Вµ РЎвЂљРЎР‚Р ВµР Р…Р С‘РЎР‚Р С•Р Р†Р С”Р С‘");

        dataList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);

        // Р В РЎв„ўР В Р вЂ¦Р В РЎвЂўР В РЎвЂ”Р В РЎвЂќР В РЎвЂ
        findViewById(R.id.btnAvailableWorkouts).setOnClickListener(v -> loadAvailableWorkouts());
        findViewById(R.id.btnMyBookings).setOnClickListener(v -> loadMyBookings());
        findViewById(R.id.btnMyPlan).setOnClickListener(v -> loadMyPlan());
        findViewById(R.id.btnAnthropometry).setOnClickListener(v -> showAnthropometryDialog());
        findViewById(R.id.btnOpenProfile).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));        setupBottomNavigation();

        loadMembershipStatus();
        loadAvailableWorkouts();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_workouts);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_workouts) {
                tvSectionTitle.setText("Р вЂќР С•РЎРѓРЎвЂљРЎС“Р С—Р Р…РЎвЂ№Р Вµ РЎвЂљРЎР‚Р ВµР Р…Р С‘РЎР‚Р С•Р Р†Р С”Р С‘");
                loadAvailableWorkouts();
                return true;
            } else if (itemId == R.id.nav_bookings) {
                tvSectionTitle.setText("Р СљР С•Р С‘ Р В·Р В°Р С—Р С‘РЎРѓР С‘");
                loadMyBookings();
                return true;
            } else if (itemId == R.id.nav_plan) {
                tvSectionTitle.setText("Р СљР С•Р в„– Р С—Р В»Р В°Р Р…");
                loadMyPlan();
                return true;
            } else if (itemId == R.id.nav_progress) {
                tvSectionTitle.setText("Р СџРЎР‚Р С•Р С–РЎР‚Р ВµРЎРѓРЎРѓ");
                showProgress();
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

                    String workoutInfo = "РЎР‚РЎСџР РЏРІР‚в„–Р С—РЎвЂР РЏ " + workoutType +
                            "\nРЎР‚РЎСџРІР‚СљРІР‚В¦ " + date + " " + time + " (" + duration + " Р В РЎВР В РЎвЂР В Р вЂ¦)" +
                            "\nРЎР‚РЎСџРІР‚ВР РѓР Р†Р вЂљР РЉРЎР‚РЎСџР РЏР’В« Р В РЎС›Р РЋР вЂљР В Р’ВµР В Р вЂ¦Р В Р’ВµР РЋР вЂљ: " + trainer +
                            "\nРЎР‚РЎСџРІР‚ВРўС’ Р В Р Р‹Р В Р вЂ Р В РЎвЂўР В Р’В±Р В РЎвЂўР В РўвЂР В Р вЂ¦Р В РЎвЂў Р В РЎВР В Р’ВµР РЋР С“Р РЋРІР‚С™: " + (max - current) + "/" + max +
                            "\nР Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“\nР Р†РЎвЂєР Р‹Р С—РЎвЂР РЏ Р В РЎСљР В РЎвЂ™Р В РІР‚вЂњР В РЎС™Р В Р’ВР В РЎС›Р В РІР‚Сћ Р В РІР‚СњР В РІР‚С”Р В Р вЂЎ Р В РІР‚вЂќР В РЎвЂ™Р В РЎСџР В Р’ВР В Р Р‹Р В Р’В";
                    dataList.add(workoutInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (workouts.moveToNext());
            workouts.close();

            if (dataList.isEmpty()) {
                dataList.add("РЎР‚РЎСџРІР‚СљРІР‚В¦ Р В РЎСљР В Р’ВµР РЋРІР‚С™ Р В РўвЂР В РЎвЂўР РЋР С“Р РЋРІР‚С™Р РЋРЎвЂњР В РЎвЂ”Р В Р вЂ¦Р РЋРІР‚в„–Р РЋРІР‚В¦ Р РЋРІР‚С™Р РЋР вЂљР В Р’ВµР В Р вЂ¦Р В РЎвЂР РЋР вЂљР В РЎвЂўР В Р вЂ Р В РЎвЂўР В РЎвЂќ Р В Р вЂ¦Р В Р’В° Р В Р’В±Р В Р’В»Р В РЎвЂР В Р’В¶Р В Р’В°Р В РІвЂћвЂ“Р РЋРІвЂљВ¬Р В РЎвЂР В Р’Вµ Р В РўвЂР В Р вЂ¦Р В РЎвЂ");
            }

            adapter.notifyDataSetChanged();

            listView.setOnItemClickListener((parent, view, position, id) -> {
                if (position < scheduleIds.size()) {
                    bookWorkout(scheduleIds.get(position));
                }
            });
        } else {
            dataList.add("РЎР‚РЎСџРІР‚СљРІР‚В¦ Р В РЎСљР В Р’ВµР РЋРІР‚С™ Р В РўвЂР В РЎвЂўР РЋР С“Р РЋРІР‚С™Р РЋРЎвЂњР В РЎвЂ”Р В Р вЂ¦Р РЋРІР‚в„–Р РЋРІР‚В¦ Р РЋРІР‚С™Р РЋР вЂљР В Р’ВµР В Р вЂ¦Р В РЎвЂР РЋР вЂљР В РЎвЂўР В Р вЂ Р В РЎвЂўР В РЎвЂќ");
            if (workouts != null) workouts.close();
            adapter.notifyDataSetChanged();
        }
    }

    private void bookWorkout(long scheduleId) {
        boolean success = dbHelper.bookWorkout(scheduleId, clientId);
        if (success) {
            Toast.makeText(this, "Р Р†РЎС™РІР‚Сљ Р В РІР‚в„ўР РЋРІР‚в„– Р РЋРЎвЂњР РЋР С“Р В РЎвЂ”Р В Р’ВµР РЋРІвЂљВ¬Р В Р вЂ¦Р В РЎвЂў Р В Р’В·Р В Р’В°Р В РЎвЂ”Р В РЎвЂР РЋР С“Р В Р’В°Р В Р’В»Р В РЎвЂР РЋР С“Р РЋР Р‰ Р В Р вЂ¦Р В Р’В° Р РЋРІР‚С™Р РЋР вЂљР В Р’ВµР В Р вЂ¦Р В РЎвЂР РЋР вЂљР В РЎвЂўР В Р вЂ Р В РЎвЂќР РЋРЎвЂњ!", Toast.LENGTH_LONG).show();
            loadAvailableWorkouts();
            loadMyBookings();
        } else {
            Toast.makeText(this, "Р Р†РЎСљР Р‰ Р В РЎСљР В Р’Вµ Р РЋРЎвЂњР В РўвЂР В Р’В°Р В Р’В»Р В РЎвЂўР РЋР С“Р РЋР Р‰ Р В Р’В·Р В Р’В°Р В РЎвЂ”Р В РЎвЂР РЋР С“Р В Р’В°Р РЋРІР‚С™Р РЋР Р‰Р РЋР С“Р РЋР РЏ. Р В РІР‚в„ўР В РЎвЂўР В Р’В·Р В РЎВР В РЎвЂўР В Р’В¶Р В Р вЂ¦Р В РЎвЂў, Р В РЎВР В Р’ВµР РЋР С“Р РЋРІР‚С™ Р РЋРЎвЂњР В Р’В¶Р В Р’Вµ Р В Р вЂ¦Р В Р’ВµР РЋРІР‚С™.", Toast.LENGTH_LONG).show();
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

                String statusEmoji = status.equals("confirmed") ? "Р Р†Р РЏРЎвЂ“ Р В РЎвЂєР В Р’В¶Р В РЎвЂР В РўвЂР В Р’В°Р В Р’ВµР РЋРІР‚С™" :
                        (status.equals("completed") ? "Р Р†РЎС™РІР‚В¦ Р В РІР‚в„ўР РЋРІР‚в„–Р В РЎвЂ”Р В РЎвЂўР В Р’В»Р В Р вЂ¦Р В Р’ВµР В Р вЂ¦Р В РЎвЂў" : "Р Р†РЎСљР Р‰ Р В РЎвЂєР РЋРІР‚С™Р В РЎВР В Р’ВµР В Р вЂ¦Р В Р’ВµР В Р вЂ¦Р В РЎвЂў");
                String statusIcon = status.equals("confirmed") ? "Р Р†Р РЏРЎвЂ“" :
                        (status.equals("completed") ? "Р Р†РЎС™РІР‚В¦" : "Р Р†РЎСљР Р‰");

                dataList.add(statusIcon + " " + workoutType +
                        "\nРЎР‚РЎСџРІР‚СљРІР‚В¦ " + date + " " + time +
                        "\nРЎР‚РЎСџРІР‚ВР РѓР Р†Р вЂљР РЉРЎР‚РЎСџР РЏР’В« Р В РЎС›Р РЋР вЂљР В Р’ВµР В Р вЂ¦Р В Р’ВµР РЋР вЂљ: " + trainer +
                        "\nРЎР‚РЎСџРІР‚СљР Р‰ Р В Р Р‹Р РЋРІР‚С™Р В Р’В°Р РЋРІР‚С™Р РЋРЎвЂњР РЋР С“: " + statusEmoji);

                if (status.equals("confirmed")) {
                    dataList.add("Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“\nР Р†РЎвЂєР Р‹Р С—РЎвЂР РЏ Р В РЎСљР В Р’В°Р В Р’В¶Р В РЎВР В РЎвЂР РЋРІР‚С™Р В Р’Вµ Р РЋРІР‚РЋР РЋРІР‚С™Р В РЎвЂўР В Р’В±Р РЋРІР‚в„– Р В РЎвЂўР РЋРІР‚С™Р В РЎВР В Р’ВµР РЋРІР‚С™Р В РЎвЂР РЋРІР‚С™Р РЋР Р‰ Р В Р вЂ Р РЋРІР‚в„–Р В РЎвЂ”Р В РЎвЂўР В Р’В»Р В Р вЂ¦Р В Р’ВµР В Р вЂ¦Р В РЎвЂР В Р’Вµ");
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
            dataList.add("Р В Р в‚¬ Р В Р вЂ Р В Р’В°Р РЋР С“ Р В РЎвЂ”Р В РЎвЂўР В РЎвЂќР В Р’В° Р В Р вЂ¦Р В Р’ВµР РЋРІР‚С™ Р В Р’В·Р В Р’В°Р В РЎвЂ”Р В РЎвЂР РЋР С“Р В Р’ВµР В РІвЂћвЂ“ Р В Р вЂ¦Р В Р’В° Р РЋРІР‚С™Р РЋР вЂљР В Р’ВµР В Р вЂ¦Р В РЎвЂР РЋР вЂљР В РЎвЂўР В Р вЂ Р В РЎвЂќР В РЎвЂ");
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

            dataList.add("РЎР‚РЎСџРІР‚СљРІР‚в„– Р В РЎС›Р В Р’В Р В РІР‚СћР В РЎСљР В Р’ВР В Р’В Р В РЎвЂєР В РІР‚в„ўР В РЎвЂєР В Р’В§Р В РЎСљР В Р’В«Р В РІвЂћСћ Р В РЎСџР В РІР‚С”Р В РЎвЂ™Р В РЎСљ");
            dataList.add("Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“");
            dataList.add("РЎР‚РЎСџРІР‚ВР РѓР Р†Р вЂљР РЉРЎР‚РЎСџР РЏР’В« Р В РЎС›Р РЋР вЂљР В Р’ВµР В Р вЂ¦Р В Р’ВµР РЋР вЂљ: " + trainerName);
            dataList.add("РЎР‚РЎСџРІР‚СљРІР‚В¦ Р В РЎСљР В Р’В°Р В Р’В·Р В Р вЂ¦Р В Р’В°Р РЋРІР‚РЋР В Р’ВµР В Р вЂ¦: " + assignedDate);
            dataList.add("РЎР‚РЎСџРІР‚СљРЎСљ Р В РІР‚вЂќР В Р’В°Р В РЎВР В Р’ВµР РЋРІР‚С™Р В РЎвЂќР В РЎвЂ: " + notes);
            dataList.add("");
            dataList.add("РЎР‚РЎСџР РЏРІР‚в„–Р С—РЎвЂР РЏ Р В Р в‚¬Р В РЎСџР В Р’В Р В РЎвЂ™Р В РІР‚вЂњР В РЎСљР В РІР‚СћР В РЎСљР В Р’ВР В Р вЂЎ:");

            Cursor exercises = dbHelper.getPlanExercises(planId);
            if (exercises != null && exercises.moveToFirst()) {
                do {
                    String exName = exercises.getString(exercises.getColumnIndexOrThrow(DatabaseHelper.COL_PE_EXERCISE_NAME));
                    int sets = exercises.getInt(exercises.getColumnIndexOrThrow(DatabaseHelper.COL_PE_SETS));
                    int reps = exercises.getInt(exercises.getColumnIndexOrThrow(DatabaseHelper.COL_PE_REPS));
                    float weight = exercises.getFloat(exercises.getColumnIndexOrThrow(DatabaseHelper.COL_PE_WEIGHT));

                    dataList.add("   Р Р†Р вЂљРЎС› " + exName + ": " + sets + " x " + reps + " (" + weight + " Р В РЎвЂќР В РЎвЂ“)");
                } while (exercises.moveToNext());
                exercises.close();
            } else {
                dataList.add("   Р В РЎСљР В Р’ВµР РЋРІР‚С™ Р В РўвЂР В РЎвЂўР В Р’В±Р В Р’В°Р В Р вЂ Р В Р’В»Р В Р’ВµР В Р вЂ¦Р В Р вЂ¦Р РЋРІР‚в„–Р РЋРІР‚В¦ Р РЋРЎвЂњР В РЎвЂ”Р РЋР вЂљР В Р’В°Р В Р’В¶Р В Р вЂ¦Р В Р’ВµР В Р вЂ¦Р В РЎвЂР В РІвЂћвЂ“");
            }
            plan.close();
        } else {
            dataList.add("РЎР‚РЎСџРІР‚СљРІР‚в„– Р В РЎС›Р В Р’В Р В РІР‚СћР В РЎСљР В Р’ВР В Р’В Р В РЎвЂєР В РІР‚в„ўР В РЎвЂєР В Р’В§Р В РЎСљР В Р’В«Р В РІвЂћСћ Р В РЎСџР В РІР‚С”Р В РЎвЂ™Р В РЎСљ");
            dataList.add("Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“");
            dataList.add("Р В Р в‚¬ Р В Р вЂ Р В Р’В°Р РЋР С“ Р В РЎвЂ”Р В РЎвЂўР В РЎвЂќР В Р’В° Р В Р вЂ¦Р В Р’ВµР РЋРІР‚С™ Р В Р вЂ¦Р В Р’В°Р В Р’В·Р В Р вЂ¦Р В Р’В°Р РЋРІР‚РЋР В Р’ВµР В Р вЂ¦Р В Р вЂ¦Р В РЎвЂўР В РЎвЂ“Р В РЎвЂў Р В РЎвЂ”Р В Р’В»Р В Р’В°Р В Р вЂ¦Р В Р’В°");
            dataList.add("");
            dataList.add("Р В РЎвЂєР В Р’В±Р РЋР вЂљР В Р’В°Р РЋРІР‚С™Р В РЎвЂР РЋРІР‚С™Р В Р’ВµР РЋР С“Р РЋР Р‰ Р В РЎвЂќ Р В Р вЂ Р В Р’В°Р РЋРІвЂљВ¬Р В Р’ВµР В РЎВР РЋРЎвЂњ Р РЋРІР‚С™Р РЋР вЂљР В Р’ВµР В Р вЂ¦Р В Р’ВµР РЋР вЂљР РЋРЎвЂњ");
            dataList.add("Р В РўвЂР В Р’В»Р РЋР РЏ Р РЋР С“Р В РЎвЂўР РЋР С“Р РЋРІР‚С™Р В Р’В°Р В Р вЂ Р В Р’В»Р В Р’ВµР В Р вЂ¦Р В РЎвЂР РЋР РЏ Р В РЎвЂ”Р РЋР вЂљР В РЎвЂўР В РЎвЂ“Р РЋР вЂљР В Р’В°Р В РЎВР В РЎВР РЋРІР‚в„– Р РЋРІР‚С™Р РЋР вЂљР В Р’ВµР В Р вЂ¦Р В РЎвЂР РЋР вЂљР В РЎвЂўР В Р вЂ Р В РЎвЂўР В РЎвЂќ");
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

        builder.setTitle("РЎР‚РЎСџРІР‚СљР РЏ Р В РІР‚вЂќР В Р’В°Р В РЎВР В Р’ВµР РЋР вЂљР РЋРІР‚в„– Р РЋРІР‚С™Р В Р’ВµР В Р’В»Р В Р’В°")
                .setView(view)
                .setPositiveButton("Р В Р Р‹Р В РЎвЂўР РЋРІР‚В¦Р РЋР вЂљР В Р’В°Р В Р вЂ¦Р В РЎвЂР РЋРІР‚С™Р РЋР Р‰", (dialog, which) -> {
                    try {
                        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                        float weight = Float.parseFloat(etWeight.getText().toString());
                        float height = Float.parseFloat(etHeight.getText().toString());
                        float biceps = Float.parseFloat(etBiceps.getText().toString());
                        float chest = Float.parseFloat(etChest.getText().toString());
                        float waist = Float.parseFloat(etWaist.getText().toString());

                        boolean success = dbHelper.saveMeasurement(clientId, date, weight, height, biceps, chest, waist);
                        Toast.makeText(this, success ? "Р Р†РЎС™РІР‚Сљ Р В РІР‚вЂќР В Р’В°Р В РЎВР В Р’ВµР РЋР вЂљР РЋРІР‚в„– Р РЋР С“Р В РЎвЂўР РЋРІР‚В¦Р РЋР вЂљР В Р’В°Р В Р вЂ¦Р В Р’ВµР В Р вЂ¦Р РЋРІР‚в„–!" : "Р Р†РЎСљР Р‰ Р В РЎвЂєР РЋРІвЂљВ¬Р В РЎвЂР В Р’В±Р В РЎвЂќР В Р’В° Р РЋР С“Р В РЎвЂўР РЋРІР‚В¦Р РЋР вЂљР В Р’В°Р В Р вЂ¦Р В Р’ВµР В Р вЂ¦Р В РЎвЂР РЋР РЏ", Toast.LENGTH_SHORT).show();
                        if (success) showProgress();
                    } catch (Exception e) {
                        Toast.makeText(this, "Р В РЎвЂєР РЋРІвЂљВ¬Р В РЎвЂР В Р’В±Р В РЎвЂќР В Р’В°: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Р В РЎвЂєР РЋРІР‚С™Р В РЎВР В Р’ВµР В Р вЂ¦Р В Р’В°", null)
                .show();
    }

    private void showProgress() {
        dataList.clear();

        // Р В Р Р‹Р РЋРІР‚С™Р В Р’В°Р РЋРІР‚С™Р В РЎвЂР РЋР С“Р РЋРІР‚С™Р В РЎвЂР В РЎвЂќР В Р’В° Р РЋРІР‚С™Р РЋР вЂљР В Р’ВµР В Р вЂ¦Р В РЎвЂР РЋР вЂљР В РЎвЂўР В Р вЂ Р В РЎвЂўР В РЎвЂќ
        int completedCount = dbHelper.getCompletedWorkoutsCount(clientId);
        dataList.add("РЎР‚РЎСџР РЏРІР‚В  Р В Р Р‹Р В РЎС›Р В РЎвЂ™Р В РЎС›Р В Р’ВР В Р Р‹Р В РЎС›Р В Р’ВР В РЎв„ўР В РЎвЂ™ Р В РЎС›Р В Р’В Р В РІР‚СћР В РЎСљР В Р’ВР В Р’В Р В РЎвЂєР В РІР‚в„ўР В РЎвЂєР В РЎв„ў");
        dataList.add("Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“");
        dataList.add("Р Р†РЎС™РІР‚В¦ Р В РЎСџР РЋР вЂљР В РЎвЂўР В Р вЂ Р В Р’ВµР В РўвЂР В Р’ВµР В Р вЂ¦Р В РЎвЂў Р РЋРІР‚С™Р РЋР вЂљР В Р’ВµР В Р вЂ¦Р В РЎвЂР РЋР вЂљР В РЎвЂўР В Р вЂ Р В РЎвЂўР В РЎвЂќ: " + completedCount);
        dataList.add("");

        // Р В Р’ВР РЋР С“Р РЋРІР‚С™Р В РЎвЂўР РЋР вЂљР В РЎвЂР РЋР РЏ Р В Р’В·Р В Р’В°Р В РЎВР В Р’ВµР РЋР вЂљР В РЎвЂўР В Р вЂ 
        dataList.add("РЎР‚РЎСџРІР‚СљР вЂ° Р В Р’ВР В Р Р‹Р В РЎС›Р В РЎвЂєР В Р’В Р В Р’ВР В Р вЂЎ Р В РІР‚вЂќР В РЎвЂ™Р В РЎС™Р В РІР‚СћР В Р’В Р В РЎвЂєР В РІР‚в„ў");
        dataList.add("Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“");

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

                dataList.add("РЎР‚РЎСџРІР‚СљРІР‚В¦ " + date);
                dataList.add("   Р Р†РЎв„ўРІР‚вЂњР С—РЎвЂР РЏ Р В РІР‚в„ўР В Р’ВµР РЋР С“: " + weight + " Р В РЎвЂќР В РЎвЂ“");
                dataList.add("   РЎР‚РЎСџРІР‚в„ўР вЂћ Р В РІР‚ВР В РЎвЂР РЋРІР‚В Р В Р’ВµР В РЎвЂ”Р РЋР С“: " + biceps + " Р РЋР С“Р В РЎВ");
                dataList.add("   РЎР‚РЎСџРІР‚СљР РЏ Р В РІР‚СљР РЋР вЂљР РЋРЎвЂњР В РўвЂР В Р вЂ¦Р В Р’В°Р РЋР РЏ Р В РЎвЂќР В Р’В»Р В Р’ВµР РЋРІР‚С™Р В РЎвЂќР В Р’В°: " + chest + " Р РЋР С“Р В РЎВ");
                dataList.add("   РЎР‚РЎСџР вЂ№Р вЂЎ Р В РЎС›Р В Р’В°Р В Р’В»Р В РЎвЂР РЋР РЏ: " + waist + " Р РЋР С“Р В РЎВ");
                dataList.add("");

            } while (measurements.moveToNext());
            measurements.close();

            if (count >= 2 && firstWeight != -1 && lastWeight != -1) {
                float weightChange = lastWeight - firstWeight;
                String changeText = weightChange > 0 ? "+" + String.format("%.1f", weightChange) : String.format("%.1f", weightChange);
                dataList.add("РЎР‚РЎСџРІР‚СљРІвЂљВ¬ Р В Р’ВР В РЎС›Р В РЎвЂєР В РІР‚СљР В РЎвЂєР В РІР‚в„ўР В Р’В«Р В РІвЂћСћ Р В РЎСџР В Р’В Р В РЎвЂєР В РІР‚СљР В Р’В Р В РІР‚СћР В Р Р‹Р В Р Р‹:");
                dataList.add("   Р В Р’ВР В Р’В·Р В РЎВР В Р’ВµР В Р вЂ¦Р В Р’ВµР В Р вЂ¦Р В РЎвЂР В Р’Вµ Р В Р вЂ Р В Р’ВµР РЋР С“Р В Р’В°: " + changeText + " Р В РЎвЂќР В РЎвЂ“");
                dataList.add("   Р В РЎв„ўР В РЎвЂўР В Р’В»Р В РЎвЂР РЋРІР‚РЋР В Р’ВµР РЋР С“Р РЋРІР‚С™Р В Р вЂ Р В РЎвЂў Р В Р’В·Р В Р’В°Р В РЎВР В Р’ВµР РЋР вЂљР В РЎвЂўР В Р вЂ : " + count);
            }
        } else {
            dataList.add("Р В РЎСљР В Р’ВµР РЋРІР‚С™ Р В РўвЂР В Р’В°Р В Р вЂ¦Р В Р вЂ¦Р РЋРІР‚в„–Р РЋРІР‚В¦ Р В РЎвЂў Р В Р’В·Р В Р’В°Р В РЎВР В Р’ВµР РЋР вЂљР В Р’В°Р РЋРІР‚В¦");
            dataList.add("Р В РІР‚СњР В РЎвЂўР В Р’В±Р В Р’В°Р В Р вЂ Р РЋР Р‰Р РЋРІР‚С™Р В Р’Вµ Р В РЎвЂ”Р В Р’ВµР РЋР вЂљР В Р вЂ Р РЋРІР‚в„–Р В РІвЂћвЂ“ Р В Р’В·Р В Р’В°Р В РЎВР В Р’ВµР РЋР вЂљ Р В Р вЂ  Р РЋР вЂљР В Р’В°Р В Р’В·Р В РўвЂР В Р’ВµР В Р’В»Р В Р’Вµ 'Р В РІР‚вЂќР В Р’В°Р В РЎВР В Р’ВµР РЋР вЂљР РЋРІР‚в„–'");
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

        String[] feelings = {"Р В РЎвЂєР РЋРІР‚С™Р В Р’В»Р В РЎвЂР РЋРІР‚РЋР В Р вЂ¦Р В РЎвЂў РЎР‚РЎСџРІР‚в„ўР вЂћ", "Р В РўС’Р В РЎвЂўР РЋР вЂљР В РЎвЂўР РЋРІвЂљВ¬Р В РЎвЂў РЎР‚РЎСџРІР‚ВР РЉ", "Р В Р в‚¬Р РЋР С“Р РЋРІР‚С™Р В Р’В°Р В Р’В» РЎР‚РЎСџР’ВРІР‚Сљ", "Р В РЎСџР В Р’В»Р В РЎвЂўР РЋРІР‚В¦Р В РЎвЂў РЎР‚РЎСџР’ВРЎвЂє"};
        ArrayAdapter<String> feelingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, feelings);
        feelingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFeeling.setAdapter(feelingAdapter);

        builder.setTitle("РЎР‚РЎСџР РЏРІР‚в„–Р С—РЎвЂР РЏ Р В РЎвЂєР РЋРІР‚С™Р В РЎВР В Р’ВµР РЋРІР‚С™Р В РЎвЂР РЋРІР‚С™Р РЋР Р‰ Р В Р вЂ Р РЋРІР‚в„–Р В РЎвЂ”Р В РЎвЂўР В Р’В»Р В Р вЂ¦Р В Р’ВµР В Р вЂ¦Р В РЎвЂР В Р’Вµ Р РЋРІР‚С™Р РЋР вЂљР В Р’ВµР В Р вЂ¦Р В РЎвЂР РЋР вЂљР В РЎвЂўР В Р вЂ Р В РЎвЂќР В РЎвЂ")
                .setView(view)
                .setPositiveButton("Р В Р Р‹Р В РЎвЂўР РЋРІР‚В¦Р РЋР вЂљР В Р’В°Р В Р вЂ¦Р В РЎвЂР РЋРІР‚С™Р РЋР Р‰", (dialog, which) -> {
                    try {
                        String exerciseName = etExerciseName.getText().toString();
                        int sets = Integer.parseInt(etSets.getText().toString());
                        int reps = Integer.parseInt(etReps.getText().toString());
                        float weight = Float.parseFloat(etWeight.getText().toString());
                        String feeling = feelings[spinnerFeeling.getSelectedItemPosition()];

                        boolean success = dbHelper.saveWorkoutResult(bookingId, exerciseName, sets, reps, weight, feeling);
                        Toast.makeText(this, success ? "Р Р†РЎС™РІР‚Сљ Р В Р’В Р В Р’ВµР В Р’В·Р РЋРЎвЂњР В Р’В»Р РЋР Р‰Р РЋРІР‚С™Р В Р’В°Р РЋРІР‚С™Р РЋРІР‚в„– Р РЋР С“Р В РЎвЂўР РЋРІР‚В¦Р РЋР вЂљР В Р’В°Р В Р вЂ¦Р В Р’ВµР В Р вЂ¦Р РЋРІР‚в„–!" : "Р Р†РЎСљР Р‰ Р В РЎвЂєР РЋРІвЂљВ¬Р В РЎвЂР В Р’В±Р В РЎвЂќР В Р’В° Р РЋР С“Р В РЎвЂўР РЋРІР‚В¦Р РЋР вЂљР В Р’В°Р В Р вЂ¦Р В Р’ВµР В Р вЂ¦Р В РЎвЂР РЋР РЏ", Toast.LENGTH_SHORT).show();
                        if (success) loadMyBookings();
                    } catch (Exception e) {
                        Toast.makeText(this, "Р В РЎвЂєР РЋРІвЂљВ¬Р В РЎвЂР В Р’В±Р В РЎвЂќР В Р’В°: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Р В РЎвЂєР РЋРІР‚С™Р В РЎВР В Р’ВµР В Р вЂ¦Р В Р’В°", null)
                .show();
    }

    // ============ Р В РЎС™Р В РІР‚СћР В РЎС›Р В РЎвЂєР В РІР‚СњР В Р’В« Р В РІР‚СњР В РІР‚С”Р В Р вЂЎ Р В РЎвЂ™Р В РІР‚ВР В РЎвЂєР В РЎСљР В РІР‚СћР В РЎС™Р В РІР‚СћР В РЎСљР В РЎС›Р В РЎвЂєР В РІР‚в„ў ============

    private void showMembershipSection() {
        dataList.clear();
        itemIds.clear();

        try {
            // Р В РІР‚вЂќР В Р’В°Р В РЎвЂ“Р В РЎвЂўР В Р’В»Р В РЎвЂўР В Р вЂ Р В РЎвЂўР В РЎвЂќ
            dataList.add("Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“");
            dataList.add("           РЎР‚РЎСџР вЂ№Р’В« Р В РЎС™Р В РЎвЂєР В Р’В Р В РЎвЂ™Р В РІР‚ВР В РЎвЂєР В РЎСљР В РІР‚СћР В РЎС™Р В РІР‚СћР В РЎСљР В РЎС›Р В Р’В«");
            dataList.add("Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“");
            dataList.add("");

            // Р В РЎСџР В РЎвЂўР В РЎвЂќР В Р’В°Р В Р’В·Р РЋРІР‚в„–Р В Р вЂ Р В Р’В°Р В Р’ВµР В РЎВ Р В Р’В°Р В РЎвЂќР РЋРІР‚С™Р В РЎвЂР В Р вЂ Р В Р вЂ¦Р РЋРІР‚в„–Р В РІвЂћвЂ“ Р В Р’В°Р В Р’В±Р В РЎвЂўР В Р вЂ¦Р В Р’ВµР В РЎВР В Р’ВµР В Р вЂ¦Р РЋРІР‚С™
            Cursor activeMembership = dbHelper.getClientActiveMembership(clientId);
            if (activeMembership != null && activeMembership.moveToFirst()) {
                String name = activeMembership.getString(activeMembership.getColumnIndexOrThrow(DatabaseHelper.COL_MT_NAME));
                String endDate = activeMembership.getString(activeMembership.getColumnIndexOrThrow(DatabaseHelper.COL_MEM_END_DATE));
                int price = activeMembership.getInt(activeMembership.getColumnIndexOrThrow(DatabaseHelper.COL_MT_PRICE));
                String purchaseDate = activeMembership.getString(activeMembership.getColumnIndexOrThrow(DatabaseHelper.COL_MEM_PURCHASE_DATE));

                dataList.add("Р Р†РЎС™РІР‚В¦ Р В РЎвЂ™Р В РЎв„ўР В РЎС›Р В Р’ВР В РІР‚в„ўР В РЎСљР В Р’В«Р В РІвЂћСћ Р В РЎвЂ™Р В РІР‚ВР В РЎвЂєР В РЎСљР В РІР‚СћР В РЎС™Р В РІР‚СћР В РЎСљР В РЎС›");
                dataList.add("Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“");
                dataList.add("РЎР‚РЎСџР РЏР’В·Р С—РЎвЂР РЏ " + name);
                dataList.add("РЎР‚РЎСџРІР‚в„ўР’В° " + price + " Р Р†РІР‚С™Р вЂ¦");
                dataList.add("РЎР‚РЎСџРІР‚СљРІР‚В¦ Р В РЎСџР РЋР вЂљР В РЎвЂР В РЎвЂўР В Р’В±Р РЋР вЂљР В Р’ВµР РЋРІР‚С™Р В Р’ВµР В Р вЂ¦: " + purchaseDate);
                dataList.add("РЎР‚РЎСџРІР‚СљРІР‚В¦ Р В РІР‚СњР В Р’ВµР В РІвЂћвЂ“Р РЋР С“Р РЋРІР‚С™Р В Р вЂ Р РЋРЎвЂњР В Р’ВµР РЋРІР‚С™ Р В РўвЂР В РЎвЂў: " + endDate);
                dataList.add("Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“");
                dataList.add("");
                activeMembership.close();
            } else {
                dataList.add("Р Р†РЎв„ўР’В Р С—РЎвЂР РЏ Р В РЎСљР В РІР‚СћР В РЎС› Р В РЎвЂ™Р В РЎв„ўР В РЎС›Р В Р’ВР В РІР‚в„ўР В РЎСљР В РЎвЂєР В РІР‚СљР В РЎвЂє Р В РЎвЂ™Р В РІР‚ВР В РЎвЂєР В РЎСљР В РІР‚СћР В РЎС™Р В РІР‚СћР В РЎСљР В РЎС›Р В РЎвЂ™");
                dataList.add("Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“");
                dataList.add("Р В Р в‚¬ Р В Р вЂ Р В Р’В°Р РЋР С“ Р В Р вЂ¦Р В Р’ВµР РЋРІР‚С™ Р В Р’В°Р В РЎвЂќР РЋРІР‚С™Р В РЎвЂР В Р вЂ Р В Р вЂ¦Р В РЎвЂўР В РЎвЂ“Р В РЎвЂў Р В Р’В°Р В Р’В±Р В РЎвЂўР В Р вЂ¦Р В Р’ВµР В РЎВР В Р’ВµР В Р вЂ¦Р РЋРІР‚С™Р В Р’В°");
                dataList.add("Р В РЎСџР РЋР вЂљР В РЎвЂР В РЎвЂўР В Р’В±Р РЋР вЂљР В Р’ВµР РЋРІР‚С™Р В РЎвЂР РЋРІР‚С™Р В Р’Вµ Р В Р’В°Р В Р’В±Р В РЎвЂўР В Р вЂ¦Р В Р’ВµР В РЎВР В Р’ВµР В Р вЂ¦Р РЋРІР‚С™ Р В РўвЂР В Р’В»Р РЋР РЏ Р В РЎвЂ”Р В РЎвЂўР РЋР С“Р В Р’ВµР РЋРІР‚В°Р В Р’ВµР В Р вЂ¦Р В РЎвЂР РЋР РЏ");
                dataList.add("Р РЋРІР‚С™Р РЋР вЂљР В Р’ВµР В Р вЂ¦Р В РЎвЂР РЋР вЂљР В РЎвЂўР В Р вЂ Р В РЎвЂўР В РЎвЂќ Р В Р вЂ  Р В Р вЂ¦Р В Р’В°Р РЋРІвЂљВ¬Р В Р’ВµР В РЎВ Р В РЎвЂќР В Р’В»Р РЋРЎвЂњР В Р’В±Р В Р’Вµ!");
                dataList.add("Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“");
                dataList.add("");
            }

            // Р В Р’ВР РЋР С“Р РЋРІР‚С™Р В РЎвЂўР РЋР вЂљР В РЎвЂР РЋР РЏ Р В Р’В°Р В Р’В±Р В РЎвЂўР В Р вЂ¦Р В Р’ВµР В РЎВР В Р’ВµР В Р вЂ¦Р РЋРІР‚С™Р В РЎвЂўР В Р вЂ 
            dataList.add("РЎР‚РЎСџРІР‚СљРЎС™ Р В Р’ВР В Р Р‹Р В РЎС›Р В РЎвЂєР В Р’В Р В Р’ВР В Р вЂЎ Р В РЎвЂ™Р В РІР‚ВР В РЎвЂєР В РЎСљР В РІР‚СћР В РЎС™Р В РІР‚СћР В РЎСљР В РЎС›Р В РЎвЂєР В РІР‚в„ў");
            dataList.add("Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“");

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
                    String statusIcon = status.equals("active") ? "Р Р†РЎС™РІР‚В¦" : "Р Р†РЎСљР Р‰";
                    String statusText = status.equals("active") ? "Р В РЎвЂ™Р В РЎвЂќР РЋРІР‚С™Р В РЎвЂР В Р вЂ Р В Р’ВµР В Р вЂ¦" : "Р В РІР‚вЂќР В Р’В°Р В Р вЂ Р В Р’ВµР РЋР вЂљР РЋРІвЂљВ¬Р В Р’ВµР В Р вЂ¦";

                    dataList.add(statusIcon + " " + name);
                    dataList.add("РЎР‚РЎСџРІР‚в„ўР’В° " + price + " Р Р†РІР‚С™Р вЂ¦");
                    dataList.add("РЎР‚РЎСџРІР‚СљРІР‚В¦ " + startDate + " Р Р†РІР‚В РІР‚в„ў " + endDate);
                    dataList.add("РЎР‚РЎСџРІР‚СљР Р‰ Р В Р Р‹Р РЋРІР‚С™Р В Р’В°Р РЋРІР‚С™Р РЋРЎвЂњР РЋР С“: " + statusText);
                    dataList.add("Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“");
                } while (history.moveToNext());
                history.close();

                if (!hasHistory) {
                    dataList.add("Р В РЎСљР В Р’ВµР РЋРІР‚С™ Р В РЎвЂР РЋР С“Р РЋРІР‚С™Р В РЎвЂўР РЋР вЂљР В РЎвЂР В РЎвЂ Р В Р’В°Р В Р’В±Р В РЎвЂўР В Р вЂ¦Р В Р’ВµР В РЎВР В Р’ВµР В Р вЂ¦Р РЋРІР‚С™Р В РЎвЂўР В Р вЂ ");
                    dataList.add("Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“");
                }
            } else {
                dataList.add("Р В РЎСљР В Р’ВµР РЋРІР‚С™ Р В РЎвЂР РЋР С“Р РЋРІР‚С™Р В РЎвЂўР РЋР вЂљР В РЎвЂР В РЎвЂ Р В Р’В°Р В Р’В±Р В РЎвЂўР В Р вЂ¦Р В Р’ВµР В РЎВР В Р’ВµР В Р вЂ¦Р РЋРІР‚С™Р В РЎвЂўР В Р вЂ ");
                dataList.add("Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“");
            }
            dataList.add("");

            // Р В РІР‚СњР В РЎвЂўР РЋР С“Р РЋРІР‚С™Р РЋРЎвЂњР В РЎвЂ”Р В Р вЂ¦Р РЋРІР‚в„–Р В Р’Вµ Р В Р’В°Р В Р’В±Р В РЎвЂўР В Р вЂ¦Р В Р’ВµР В РЎВР В Р’ВµР В Р вЂ¦Р РЋРІР‚С™Р РЋРІР‚в„– Р В РўвЂР В Р’В»Р РЋР РЏ Р В РЎвЂ”Р В РЎвЂўР В РЎвЂќР РЋРЎвЂњР В РЎвЂ”Р В РЎвЂќР В РЎвЂ
            dataList.add("РЎР‚РЎСџРІР‚С”РІР‚в„ў Р В РІР‚СњР В РЎвЂєР В Р Р‹Р В РЎС›Р В Р в‚¬Р В РЎСџР В РЎСљР В Р’В«Р В РІР‚Сћ Р В РЎвЂ™Р В РІР‚ВР В РЎвЂєР В РЎСљР В РІР‚СћР В РЎС™Р В РІР‚СћР В РЎСљР В РЎС›Р В Р’В«");
            dataList.add("Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“");

            Cursor types = dbHelper.getAllMembershipTypes();

            if (types != null && types.moveToFirst()) {
                final ArrayList<Long> typeIds = new ArrayList<>(); // Р Р†РІР‚В РЎвЂ™ Р РЋР С“Р В РўвЂР В Р’ВµР В Р’В»Р В Р’В°Р В Р вЂ¦Р В РЎвЂў final
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
                        typeIds.add(typeId); // Р Р†РІР‚В РЎвЂ™ Р В РўвЂР В РЎвЂўР В Р’В±Р В Р’В°Р В Р вЂ Р В Р’В»Р РЋР РЏР В Р’ВµР В РЎВ Р В Р вЂ  final Р РЋР С“Р В РЎвЂ”Р В РЎвЂР РЋР С“Р В РЎвЂўР В РЎвЂќ

                        String status = isActive == 1 ? "Р Р†РЎС™РІР‚В¦ Р В РЎвЂ™Р В РЎвЂќР РЋРІР‚С™Р В РЎвЂР В Р вЂ Р В Р’ВµР В Р вЂ¦" : "Р Р†РЎСљР Р‰ Р В РЎСљР В Р’ВµР В Р’В°Р В РЎвЂќР РЋРІР‚С™Р В РЎвЂР В Р вЂ Р В Р’ВµР В Р вЂ¦";
                        dataList.add("РЎР‚РЎСџР РЏР’В·Р С—РЎвЂР РЏ " + name + " (" + status + ")");
                        dataList.add("РЎР‚РЎСџРІР‚СљРЎСљ " + description);
                        dataList.add("РЎР‚РЎСџРІР‚СљРІР‚В¦ " + durationDays + " Р В РўвЂР В Р вЂ¦Р В Р’ВµР В РІвЂћвЂ“");
                        dataList.add("РЎР‚РЎСџРІР‚в„ўР’В° " + price + " Р Р†РІР‚С™Р вЂ¦");
                        dataList.add("Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“");
                        dataList.add("РЎР‚РЎСџРІР‚ВРІР‚В° Р В РЎСљР В Р’В°Р В Р’В¶Р В РЎВР В РЎвЂР РЋРІР‚С™Р В Р’Вµ Р В РўвЂР В Р’В»Р РЋР РЏ Р В РЎвЂ”Р В РЎвЂўР В РЎвЂќР РЋРЎвЂњР В РЎвЂ”Р В РЎвЂќР В РЎвЂ [ID:" + typeId + "]");
                        dataList.add("Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“");
                        dataList.add("");
                    }
                } while (types.moveToNext());
                types.close();

                if (!hasItems) {
                    dataList.add("Р В РЎСљР В Р’ВµР РЋРІР‚С™ Р В РўвЂР В РЎвЂўР РЋР С“Р РЋРІР‚С™Р РЋРЎвЂњР В РЎвЂ”Р В Р вЂ¦Р РЋРІР‚в„–Р РЋРІР‚В¦ Р В Р’В°Р В Р’В±Р В РЎвЂўР В Р вЂ¦Р В Р’ВµР В РЎВР В Р’ВµР В Р вЂ¦Р РЋРІР‚С™Р В РЎвЂўР В Р вЂ ");
                    dataList.add("Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“");
                } else {
                    // Р В Р’ВР РЋР С“Р В РЎвЂ”Р В РЎвЂўР В Р’В»Р РЋР Р‰Р В Р’В·Р РЋРЎвЂњР В Р’ВµР В РЎВ final Р В РЎвЂ”Р В Р’ВµР РЋР вЂљР В Р’ВµР В РЎВР В Р’ВµР В Р вЂ¦Р В Р вЂ¦Р РЋРЎвЂњР РЋР вЂ№ Р В Р вЂ  Р В Р’В»Р РЋР РЏР В РЎВР В Р’В±Р В РўвЂР В Р’Вµ
                    listView.setOnItemClickListener((parent, view, position, id) -> {
                        String selected = dataList.get(position);
                        if (selected.contains("Р В РЎвЂ”Р В РЎвЂўР В РЎвЂќР РЋРЎвЂњР В РЎвЂ”Р В РЎвЂќР В РЎвЂ") || selected.contains("Р В РЎСљР В Р’В°Р В Р’В¶Р В РЎВР В РЎвЂР РЋРІР‚С™Р В Р’Вµ")) {
                            long extractedId = extractMembershipIdFromText(selected);
                            if (extractedId != -1 && typeIds.contains(extractedId)) { // Р Р†РІР‚В РЎвЂ™ Р В РЎвЂР РЋР С“Р В РЎвЂ”Р В РЎвЂўР В Р’В»Р РЋР Р‰Р В Р’В·Р РЋРЎвЂњР В Р’ВµР В РЎВ typeIds
                                showPurchaseMembershipDialog(extractedId);
                            }
                        }
                    });
                }
            } else {
                dataList.add("Р В РЎСљР В Р’ВµР РЋРІР‚С™ Р В РўвЂР В РЎвЂўР РЋР С“Р РЋРІР‚С™Р РЋРЎвЂњР В РЎвЂ”Р В Р вЂ¦Р РЋРІР‚в„–Р РЋРІР‚В¦ Р В Р’В°Р В Р’В±Р В РЎвЂўР В Р вЂ¦Р В Р’ВµР В РЎВР В Р’ВµР В Р вЂ¦Р РЋРІР‚С™Р В РЎвЂўР В Р вЂ ");
                dataList.add("Р В РЎвЂєР В Р’В±Р РЋР вЂљР В Р’В°Р РЋРІР‚С™Р В РЎвЂР РЋРІР‚С™Р В Р’ВµР РЋР С“Р РЋР Р‰ Р В РЎвЂќ Р В Р’В°Р В РўвЂР В РЎВР В РЎвЂР В Р вЂ¦Р В РЎвЂР РЋР С“Р РЋРІР‚С™Р РЋР вЂљР В Р’В°Р РЋРІР‚С™Р В РЎвЂўР РЋР вЂљР РЋРЎвЂњ Р В РўвЂР В Р’В»Р РЋР РЏ Р В РўвЂР В РЎвЂўР В Р’В±Р В Р’В°Р В Р вЂ Р В Р’В»Р В Р’ВµР В Р вЂ¦Р В РЎвЂР РЋР РЏ");
                dataList.add("Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“");
                if (types != null) types.close();
            }

            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
            dataList.clear();
            dataList.add("Р Р†РЎСљР Р‰ Р В РЎвЂєР В Р РѓР В Р’ВР В РІР‚ВР В РЎв„ўР В РЎвЂ™ Р В РІР‚вЂќР В РЎвЂ™Р В РІР‚СљР В Р’В Р В Р в‚¬Р В РІР‚вЂќР В РЎв„ўР В Р’В");
            dataList.add("Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“");
            dataList.add(e.getMessage());
            dataList.add("Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“");
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Р В РЎвЂєР РЋРІвЂљВ¬Р В РЎвЂР В Р’В±Р В РЎвЂќР В Р’В°: " + e.getMessage(), Toast.LENGTH_LONG).show();
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

        // Р В Р Р‹Р В РЎвЂўР В Р’В·Р В РўвЂР В Р’В°Р В Р’ВµР В РЎВ final Р В РЎвЂ”Р В Р’ВµР РЋР вЂљР В Р’ВµР В РЎВР В Р’ВµР В Р вЂ¦Р В Р вЂ¦Р РЋРІР‚в„–Р В Р’Вµ Р В РўвЂР В Р’В»Р РЋР РЏ Р В РЎвЂР РЋР С“Р В РЎвЂ”Р В РЎвЂўР В Р’В»Р РЋР Р‰Р В Р’В·Р В РЎвЂўР В Р вЂ Р В Р’В°Р В Р вЂ¦Р В РЎвЂР РЋР РЏ Р В Р вЂ  Р В Р’В»Р РЋР РЏР В РЎВР В Р’В±Р В РўвЂР В Р’Вµ
        final String finalName = name;
        final String finalDescription = description;
        final int finalDurationDays = durationDays;
        final int finalPrice = price;

        AlertDialog.Builder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        builder.setTitle("РЎР‚РЎСџРІР‚С”РІР‚в„ў Р В РЎСџР В РЎвЂўР В РЎвЂќР РЋРЎвЂњР В РЎвЂ”Р В РЎвЂќР В Р’В° Р В Р’В°Р В Р’В±Р В РЎвЂўР В Р вЂ¦Р В Р’ВµР В РЎВР В Р’ВµР В Р вЂ¦Р РЋРІР‚С™Р В Р’В°")
                .setMessage("Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“\n" +
                        "РЎР‚РЎСџР РЏР’В·Р С—РЎвЂР РЏ " + finalName + "\n" +
                        "Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“\n" +
                        "РЎР‚РЎСџРІР‚СљРЎСљ " + finalDescription + "\n" +
                        "РЎР‚РЎСџРІР‚СљРІР‚В¦ " + finalDurationDays + " Р В РўвЂР В Р вЂ¦Р В Р’ВµР В РІвЂћвЂ“\n" +
                        "РЎР‚РЎСџРІР‚в„ўР’В° " + finalPrice + " Р Р†РІР‚С™Р вЂ¦\n" +
                        "Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“\n" +
                        "Р В Р Р‹Р РЋРІР‚С™Р В Р’В°Р РЋР вЂљР РЋРІР‚в„–Р В РІвЂћвЂ“ Р В Р’В°Р В Р’В±Р В РЎвЂўР В Р вЂ¦Р В Р’ВµР В РЎВР В Р’ВµР В Р вЂ¦Р РЋРІР‚С™ Р В Р’В±Р РЋРЎвЂњР В РўвЂР В Р’ВµР РЋРІР‚С™ Р В Р’В°Р В Р вЂ Р РЋРІР‚С™Р В РЎвЂўР В РЎВР В Р’В°Р РЋРІР‚С™Р В РЎвЂР РЋРІР‚РЋР В Р’ВµР РЋР С“Р В РЎвЂќР В РЎвЂ\n" +
                        "Р В РўвЂР В Р’ВµР В Р’В°Р В РЎвЂќР РЋРІР‚С™Р В РЎвЂР В Р вЂ Р В РЎвЂР РЋР вЂљР В РЎвЂўР В Р вЂ Р В Р’В°Р В Р вЂ¦\n" +
                        "Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“Р Р†РІР‚СњР С“")
                .setPositiveButton("Р Р†РЎС™РІР‚В¦ Р В РЎв„ўР РЋРЎвЂњР В РЎвЂ”Р В РЎвЂР РЋРІР‚С™Р РЋР Р‰", (dialog, which) -> {
                    boolean success = dbHelper.purchaseMembership(clientId, typeId);
                    if (success) {
                        Toast.makeText(this, "Р Р†РЎС™РІР‚Сљ Р В РЎвЂ™Р В Р’В±Р В РЎвЂўР В Р вЂ¦Р В Р’ВµР В РЎВР В Р’ВµР В Р вЂ¦Р РЋРІР‚С™ \"" + finalName + "\" Р РЋРЎвЂњР РЋР С“Р В РЎвЂ”Р В Р’ВµР РЋРІвЂљВ¬Р В Р вЂ¦Р В РЎвЂў Р В РЎвЂ”Р РЋР вЂљР В РЎвЂР В РЎвЂўР В Р’В±Р РЋР вЂљР В Р’ВµР РЋРІР‚С™Р В Р’ВµР В Р вЂ¦!", Toast.LENGTH_LONG).show();
                        showMembershipSection();
                        loadMembershipStatus();
                    } else {
                        Toast.makeText(this, "Р Р†РЎСљР Р‰ Р В РЎвЂєР РЋРІвЂљВ¬Р В РЎвЂР В Р’В±Р В РЎвЂќР В Р’В° Р В РЎвЂ”Р РЋР вЂљР В РЎвЂ Р В РЎвЂ”Р В РЎвЂўР В РЎвЂќР РЋРЎвЂњР В РЎвЂ”Р В РЎвЂќР В Р’Вµ Р В Р’В°Р В Р’В±Р В РЎвЂўР В Р вЂ¦Р В Р’ВµР В РЎВР В Р’ВµР В Р вЂ¦Р РЋРІР‚С™Р В Р’В°", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Р В РЎвЂєР РЋРІР‚С™Р В РЎВР В Р’ВµР В Р вЂ¦Р В Р’В°", null)
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
        builder.setTitle("Р В РІР‚в„ўР РЋРІР‚в„–Р РЋРІР‚В¦Р В РЎвЂўР В РўвЂ")
                .setMessage("Р В РІР‚в„ўР РЋРІР‚в„– Р РЋРЎвЂњР В Р вЂ Р В Р’ВµР РЋР вЂљР В Р’ВµР В Р вЂ¦Р РЋРІР‚в„–, Р РЋРІР‚РЋР РЋРІР‚С™Р В РЎвЂў Р РЋРІР‚В¦Р В РЎвЂўР РЋРІР‚С™Р В РЎвЂР РЋРІР‚С™Р В Р’Вµ Р В Р вЂ Р РЋРІР‚в„–Р В РІвЂћвЂ“Р РЋРІР‚С™Р В РЎвЂ?")
                .setPositiveButton("Р В РІР‚СњР В Р’В°", (dialog, which) -> {
                    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    prefs.edit().clear().apply();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("Р В РЎвЂєР РЋРІР‚С™Р В РЎВР В Р’ВµР В Р вЂ¦Р В Р’В°", null)
                .show();
    }
}


