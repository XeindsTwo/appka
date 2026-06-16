package com.example.fitbook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Locale;

public class TrainerActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private long trainerId;
    private ListView listView;
    private TrainerDashboardAdapter adapter;
    private ArrayList<TrainerDashboardItem> dataList;
    private TextView tvTrainerName;
    private TextView tvSectionTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AuthGuard.requireRole(this, "trainer")) return;
        setContentView(R.layout.activity_trainer);

        dbHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        trainerId = prefs.getLong(DatabaseHelper.COL_USER_ID, 0);
        String trainerName = prefs.getString(DatabaseHelper.COL_FULL_NAME, "Тренер");

        tvTrainerName = findViewById(R.id.tvTrainerName);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        listView = findViewById(R.id.listView);

        tvTrainerName.setText(trainerName);
        tvSectionTitle.setText(getString(R.string.trainer_schedule_title));

        dataList = new ArrayList<>();
        adapter = new TrainerDashboardAdapter();
        listView.setAdapter(adapter);

        findViewById(R.id.btnMySchedule).setOnClickListener(v -> {
            tvSectionTitle.setText(getString(R.string.trainer_schedule_title));
            loadMySchedule();
        });
        findViewById(R.id.btnMyClients).setOnClickListener(v -> {
            tvSectionTitle.setText(getString(R.string.trainer_clients_title));
            loadMyClients();
        });
        findViewById(R.id.btnAssignPlan).setOnClickListener(v -> {
            tvSectionTitle.setText(getString(R.string.trainer_plan_title));
            loadMyClientsForAssign();
        });
        findViewById(R.id.btnOpenProfile).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        setupBottomNavigation();
        loadMySchedule();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_trainer_schedule);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_trainer_schedule) {
                tvSectionTitle.setText(getString(R.string.trainer_schedule_title));
                loadMySchedule();
                return true;
            } else if (itemId == R.id.nav_trainer_clients) {
                tvSectionTitle.setText(getString(R.string.trainer_clients_title));
                loadMyClients();
                return true;
            } else if (itemId == R.id.nav_trainer_plan) {
                tvSectionTitle.setText(getString(R.string.trainer_plan_title));
                loadMyClientsForAssign();
                return true;
            }
            return false;
        });
    }

    private void loadMySchedule() {
        dataList.clear();
        listView.setOnItemClickListener(null);
        Cursor schedule = dbHelper.getTrainerSchedule(trainerId);

        if (schedule != null && schedule.moveToFirst()) {
            do {
                String workoutType = schedule.getString(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_TYPE));
                String date = schedule.getString(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_DATE));
                String time = schedule.getString(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_TIME));
                int current = schedule.getInt(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_CURRENT_CLIENTS));
                int max = schedule.getInt(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_MAX_CLIENTS));
                int duration = schedule.getInt(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_DURATION));

                dataList.add(new TrainerDashboardItem(
                        workoutType,
                        DateFormatUtils.formatRussianDate(date) + " | " + time + " | " + duration + " мин",
                        "Записано: " + current + " из " + max,
                        null
                ));
            } while (schedule.moveToNext());
            schedule.close();
        } else {
            dataList.add(new TrainerDashboardItem(
                    "Тренировок пока нет",
                    "Администратор ещё не назначил занятия",
                    "Когда расписание появится, оно будет показано здесь.",
                    null
            ));
        }

        adapter.notifyDataSetChanged();
    }

    private void loadMyClients() {
        dataList.clear();
        listView.setOnItemClickListener(null);
        Cursor clients = dbHelper.getTrainerClients(trainerId);

        if (clients != null && clients.moveToFirst()) {
            do {
                String name = clients.getString(clients.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME));
                String phone = clients.getString(clients.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE));
                String email = clients.getString(clients.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL));

                dataList.add(new TrainerDashboardItem(
                        name,
                        "Телефон: " + emptyToDash(phone),
                        "Email: " + emptyToDash(email),
                        null
                ));
            } while (clients.moveToNext());
            clients.close();
        } else {
            dataList.add(new TrainerDashboardItem(
                    "Клиентов пока нет",
                    "Администратор ещё не назначил клиентов",
                    "Список появится здесь после назначения.",
                    null
            ));
        }

        adapter.notifyDataSetChanged();
    }

    private void loadMyClientsForAssign() {
        dataList.clear();
        Cursor clients = dbHelper.getTrainerClients(trainerId);

        if (clients != null && clients.moveToFirst()) {
            final ArrayList<Long> clientIds = new ArrayList<>();
            final ArrayList<String> clientNames = new ArrayList<>();
            final ArrayList<Long> planIds = new ArrayList<>();

            do {
                long clientId = clients.getLong(clients.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID));
                String name = clients.getString(clients.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME));
                String phone = clients.getString(clients.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE));
                long planId = dbHelper.getLatestWorkoutPlanIdForClient(clientId);

                clientIds.add(clientId);
                clientNames.add(name);
                planIds.add(planId);

                dataList.add(new TrainerDashboardItem(
                        name,
                        "Телефон: " + emptyToDash(phone),
                        planId == -1
                                ? "План ещё не создан. Нажмите, чтобы создать план и добавить упражнения."
                                : "План уже есть. Нажмите, чтобы добавить упражнения к текущему плану.",
                        planId == -1 ? "Создать план" : "Добавить упражнения"
                ));
            } while (clients.moveToNext());
            clients.close();

            listView.setOnItemClickListener((parent, view, position, id) -> {
                if (position < clientIds.size()) {
                    long planId = planIds.get(position);
                    if (planId == -1) {
                        showAssignPlanDialog(clientIds.get(position), clientNames.get(position));
                    } else {
                        showAddExercisesDialog(planId, clientNames.get(position));
                    }
                }
            });
        } else {
            listView.setOnItemClickListener(null);
            dataList.add(new TrainerDashboardItem(
                    "Некому назначить план",
                    "У тренера пока нет клиентов",
                    "После назначения клиентов администратором здесь можно будет создавать планы.",
                    null
            ));
        }

        adapter.notifyDataSetChanged();
    }

    private void showAssignPlanDialog(final long clientId, String clientName) {
        AlertDialog.Builder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_plan_assign, null);

        final EditText etNotes = view.findViewById(R.id.etNotes);

        builder.setTitle(getString(R.string.trainer_assign_plan_title, clientName))
                .setView(view)
                .setPositiveButton("Далее", (dialog, which) -> {
                    long planId = dbHelper.createWorkoutPlan(clientId, trainerId, etNotes.getText().toString());
                    if (planId != -1) {
                        showAddExercisesDialog(planId, clientName);
                    } else {
                        Toast.makeText(this, "Ошибка создания плана", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showAddExercisesDialog(final long planId, String clientName) {
        AlertDialog.Builder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_exercise_add, null);

        final EditText etExerciseName = view.findViewById(R.id.etExerciseName);
        final EditText etSets = view.findViewById(R.id.etSets);
        final EditText etReps = view.findViewById(R.id.etReps);
        final EditText etWeight = view.findViewById(R.id.etWeight);
        Button btnAdd = view.findViewById(R.id.btnAddExercise);
        Button btnFinish = view.findViewById(R.id.btnFinish);
        final TextView tvExercisesList = view.findViewById(R.id.tvExercisesList);

        final StringBuilder exercisesList = new StringBuilder();

        builder.setTitle(getString(R.string.trainer_add_exercises_title, clientName))
                .setView(view)
                .setCancelable(false);

        final AlertDialog dialog = builder.create();

        Cursor existingExercises = dbHelper.getPlanExercises(planId);
        if (existingExercises != null) {
            try {
                if (existingExercises.moveToFirst()) {
                    do {
                        String existingName = existingExercises.getString(existingExercises.getColumnIndexOrThrow(DatabaseHelper.COL_PE_EXERCISE_NAME));
                        int existingSets = existingExercises.getInt(existingExercises.getColumnIndexOrThrow(DatabaseHelper.COL_PE_SETS));
                        int existingReps = existingExercises.getInt(existingExercises.getColumnIndexOrThrow(DatabaseHelper.COL_PE_REPS));
                        float existingWeight = existingExercises.getFloat(existingExercises.getColumnIndexOrThrow(DatabaseHelper.COL_PE_WEIGHT));

                        exercisesList.append("- ").append(existingName).append(": ").append(existingSets)
                                .append(" x ").append(existingReps).append(" (").append(formatWeight(existingWeight)).append(" кг)\n");
                    } while (existingExercises.moveToNext());
                    tvExercisesList.setText(exercisesList.toString());
                }
            } finally {
                existingExercises.close();
            }
        }

        btnAdd.setOnClickListener(v -> {
            String name = etExerciseName.getText().toString().trim();
            if (!name.isEmpty()) {
                try {
                    int sets = Integer.parseInt(etSets.getText().toString().trim());
                    int reps = Integer.parseInt(etReps.getText().toString().trim());
                    float weight = Float.parseFloat(etWeight.getText().toString().trim().replace(',', '.'));

                    dbHelper.addExerciseToPlan(planId, name, sets, reps, weight);

                    exercisesList.append("- ").append(name).append(": ").append(sets)
                            .append(" x ").append(reps).append(" (").append(formatWeight(weight)).append(" кг)\n");
                    tvExercisesList.setText(exercisesList.toString());

                    etExerciseName.setText("");
                    etSets.setText("3");
                    etReps.setText("12");
                    etWeight.setText("50");

                    Toast.makeText(this, "Упражнение добавлено", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Введите название упражнения", Toast.LENGTH_SHORT).show();
            }
        });

        btnFinish.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(this, "Тренировочный план назначен", Toast.LENGTH_LONG).show();
        });

        dialog.show();
    }

    private void logout() {
        AlertDialog.Builder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        builder.setTitle(getString(R.string.trainer_logout_title))
                .setMessage(getString(R.string.trainer_logout_message))
                .setPositiveButton(getString(R.string.trainer_logout_positive), (dialog, which) -> {
                    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    prefs.edit().clear().apply();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton(getString(R.string.trainer_logout_negative), null)
                .show();
    }

    private String emptyToDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    private String formatWeight(float weight) {
        if (weight == (int) weight) {
            return String.valueOf((int) weight);
        }
        return String.format(Locale.US, "%.1f", weight);
    }

    private static class TrainerDashboardItem {
        final String title;
        final String meta;
        final String detail;
        final String action;

        TrainerDashboardItem(String title, String meta, String detail, String action) {
            this.title = title;
            this.meta = meta;
            this.detail = detail;
            this.action = action;
        }
    }

    private class TrainerDashboardAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public TrainerDashboardItem getItem(int position) {
            return dataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder holder;

            if (view == null) {
                view = LayoutInflater.from(TrainerActivity.this)
                        .inflate(R.layout.item_trainer_dashboard, parent, false);
                holder = new ViewHolder();
                holder.title = view.findViewById(R.id.tvItemTitle);
                holder.meta = view.findViewById(R.id.tvItemMeta);
                holder.detail = view.findViewById(R.id.tvItemDetail);
                holder.action = view.findViewById(R.id.tvItemAction);
                holder.hint = view.findViewById(R.id.tvItemHint);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            TrainerDashboardItem item = getItem(position);
            holder.title.setText(item.title);
            holder.meta.setText(item.meta);
            holder.detail.setText(item.detail);
            holder.hint.setText(item.action == null || item.action.isEmpty()
                    ? "Карточка открывается для просмотра и редактирования"
                    : "Нажми ниже, чтобы перейти к следующему шагу");

            if (item.action == null || item.action.isEmpty()) {
                holder.action.setVisibility(View.GONE);
            } else {
                holder.action.setVisibility(View.VISIBLE);
                holder.action.setText(item.action);
            }

            if (item.detail != null && item.detail.contains("РџР»Р°РЅ")) {
                holder.hint.setText("Нажми на карточку клиента, чтобы назначить или дополнить план");
            } else if (item.detail != null && item.detail.contains("Р—Р°РїРёСЃР°РЅРѕ")) {
                holder.hint.setText("Карточка показывает загрузку и время занятия");
            } else if (item.detail != null && item.detail.contains("Email")) {
                holder.hint.setText("Тут собраны контакты клиента для быстрой связи");
            } else {
                holder.hint.setText("Карточка открывается для просмотра и управления");
            }

            return view;
        }
    }

    private static class ViewHolder {
        TextView title;
        TextView meta;
        TextView detail;
        TextView action;
        TextView hint;
    }
}
