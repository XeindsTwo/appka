package com.example.fitbook;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class ClientWorkoutsActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private long clientId;
    private ListView listView;
    private TextView tvEmpty;
    private final ArrayList<Long> scheduleIds = new ArrayList<>();
    private final ArrayList<String> dataList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AuthGuard.requireRole(this, "client")) return;
        setContentView(R.layout.activity_client_workouts);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        clientId = prefs.getLong(DatabaseHelper.COL_USER_ID, 0);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        listView = findViewById(R.id.listView);
        tvEmpty = findViewById(R.id.tvEmpty);
        adapter = new ArrayAdapter<>(this, R.layout.item_dark_list_text, dataList);
        listView.setAdapter(adapter);
        render();
    }

    private void render() {
        dataList.clear();
        scheduleIds.clear();
        Cursor workouts = dbHelper.getAvailableWorkouts();
        if (workouts != null && workouts.moveToFirst()) {
            do {
                long scheduleId = workouts.getLong(workouts.getColumnIndexOrThrow(DatabaseHelper.COL_SCHEDULE_ID));
                String workoutType = workouts.getString(workouts.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_TYPE));
                String date = workouts.getString(workouts.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_DATE));
                String time = workouts.getString(workouts.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_TIME));
                String trainer = workouts.getString(workouts.getColumnIndexOrThrow("trainer_name"));
                int duration = workouts.getInt(workouts.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_DURATION));
                int current = workouts.getInt(workouts.getColumnIndexOrThrow(DatabaseHelper.COL_CURRENT_CLIENTS));
                int max = workouts.getInt(workouts.getColumnIndexOrThrow(DatabaseHelper.COL_MAX_CLIENTS));

                scheduleIds.add(scheduleId);
                dataList.add(workoutType +
                        "\nДата: " + DateFormatUtils.formatRussianDate(date) + " " + time +
                        "\nТренер: " + trainer +
                        "\nДлительность: " + duration + " мин" +
                        "\nСвободно мест: " + (max - current) + "/" + max);
            } while (workouts.moveToNext());
            workouts.close();
        }

        boolean empty = dataList.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        listView.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            dataList.add("Нет доступных тренировок");
        }
        adapter.notifyDataSetChanged();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position < scheduleIds.size()) {
                bookWorkout(scheduleIds.get(position));
            }
        });
    }

    private void bookWorkout(long scheduleId) {
        boolean success = dbHelper.bookWorkout(scheduleId, clientId);
        Toast.makeText(this, success ? "Вы успешно записались на тренировку" : "Не удалось записаться", Toast.LENGTH_SHORT).show();
        if (success) {
            render();
        }
    }
}
