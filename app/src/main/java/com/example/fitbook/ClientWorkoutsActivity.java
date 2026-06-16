package com.example.fitbook;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ClientWorkoutsActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private long clientId;
    private ListView listView;
    private TextView tvEmpty;
    private final ArrayList<Long> scheduleIds = new ArrayList<>();
    private final ArrayList<WorkoutItem> items = new ArrayList<>();
    private final Adapter adapter = new Adapter();

    private static class WorkoutItem {
        String title;
        String meta;
        String trainer;
        String seats;
    }

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
        listView.setAdapter(adapter);
        render();
    }

    private void render() {
        items.clear();
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
                WorkoutItem item = new WorkoutItem();
                item.title = workoutType;
                item.meta = DateFormatUtils.formatRussianDate(date) + " • " + time + " • " + duration + " мин";
                item.trainer = "Тренер: " + trainer;
                item.seats = "Свободно: " + (max - current) + " из " + max;
                items.add(item);
            } while (workouts.moveToNext());
            workouts.close();
        }

        boolean empty = items.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        listView.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            WorkoutItem emptyItem = new WorkoutItem();
            emptyItem.title = "Нет доступных тренировок";
            emptyItem.meta = "Загляни позже — расписание ещё не заполнено.";
            emptyItem.trainer = "";
            emptyItem.seats = "";
            items.add(emptyItem);
        }
        adapter.notifyDataSetChanged();
    }

    private void bookWorkout(long scheduleId) {
        boolean success = dbHelper.bookWorkout(scheduleId, clientId);
        Toast.makeText(this, success
                ? "Вы успешно записались на тренировку"
                : "Не удалось записаться: вы уже записаны или мест больше нет", Toast.LENGTH_SHORT).show();
        if (success) {
            render();
        }
    }

    private class Adapter extends BaseAdapter {
        @Override public int getCount() { return items.size(); }
        @Override public Object getItem(int position) { return items.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_client_workout, parent, false);
            }
            WorkoutItem item = items.get(position);
            TextView tvTitle = convertView.findViewById(R.id.tvTitle);
            TextView tvMeta = convertView.findViewById(R.id.tvMeta);
            TextView tvTrainer = convertView.findViewById(R.id.tvTrainer);
            TextView tvSeats = convertView.findViewById(R.id.tvSeats);
            TextView tvHint = convertView.findViewById(R.id.tvHint);
            Button btnAction = convertView.findViewById(R.id.btnAction);

            tvTitle.setText(item.title);
            tvMeta.setText(item.meta);
            tvTrainer.setText(item.trainer);
            tvSeats.setText(item.seats);
            tvHint.setText(position < scheduleIds.size()
                    ? "Подходит для записи в один клик"
                    : "Карточка показывает пример оформления блока");

            boolean canBook = position < scheduleIds.size();
            btnAction.setText("Записаться");
            btnAction.setVisibility(canBook ? View.VISIBLE : View.GONE);
            btnAction.setOnClickListener(v -> {
                if (position < scheduleIds.size()) {
                    bookWorkout(scheduleIds.get(position));
                }
            });
            convertView.setOnClickListener(v -> {
                if (position < scheduleIds.size()) {
                    bookWorkout(scheduleIds.get(position));
                }
            });
            return convertView;
        }
    }
}
