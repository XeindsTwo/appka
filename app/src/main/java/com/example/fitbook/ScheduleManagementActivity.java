package com.example.fitbook;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Locale;

public class ScheduleManagementActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private final ArrayList<ScheduleItem> allItems = new ArrayList<>();
    private final ArrayList<ScheduleItem> visibleItems = new ArrayList<>();
    private ScheduleListAdapter adapter;
    private EditText etSearch;
    private ListView listView;

    private static class ScheduleItem {
        long id;
        long trainerId;
        String trainerName;
        String workoutType;
        String date;
        String time;
        int duration;
        int maxClients;
        int currentClients;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AuthGuard.requireRole(this, "admin")) return;
        setContentView(R.layout.activity_schedule_management);

        dbHelper = new DatabaseHelper(this);
        etSearch = findViewById(R.id.etSearch);
        listView = findViewById(R.id.listView);

        adapter = new ScheduleListAdapter();
        listView.setAdapter(adapter);

        findViewById(R.id.btnAddSchedule).setOnClickListener(v -> startActivity(new Intent(this, AddScheduleActivity.class)));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { renderItems(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!AuthGuard.requireRole(this, "admin")) return;
        loadItems();
    }

    private void loadItems() {
        allItems.clear();
        Cursor schedule = dbHelper.getAllSchedule();
        if (schedule != null) {
            while (schedule.moveToNext()) {
                ScheduleItem item = new ScheduleItem();
                item.id = schedule.getLong(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_SCHEDULE_ID));
                item.trainerId = schedule.getLong(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_SCHEDULE_TRAINER_ID));
                item.workoutType = schedule.getString(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_TYPE));
                item.date = schedule.getString(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_DATE));
                item.time = schedule.getString(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_TIME));
                item.duration = schedule.getInt(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_DURATION));
                item.maxClients = schedule.getInt(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_MAX_CLIENTS));
                item.currentClients = schedule.getInt(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_CURRENT_CLIENTS));
                item.trainerName = schedule.getString(schedule.getColumnIndexOrThrow("trainer_name"));
                allItems.add(item);
            }
            schedule.close();
        }
        renderItems();
    }

    private void renderItems() {
        visibleItems.clear();
        String query = etSearch.getText() == null ? "" : etSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
        for (ScheduleItem item : allItems) {
            String blob = (safe(item.workoutType) + " " + safe(item.trainerName) + " " + safe(item.date) + " " + safe(item.time)).toLowerCase(Locale.ROOT);
            if (query.isEmpty() || blob.contains(query)) {
                visibleItems.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showDetails(ScheduleItem item) {
        View view = getLayoutInflater().inflate(R.layout.dialog_schedule_details, null);
        TextView tvType = view.findViewById(R.id.tvType);
        TextView tvTrainer = view.findViewById(R.id.tvTrainer);
        TextView tvDateTime = view.findViewById(R.id.tvDateTime);
        TextView tvMeta = view.findViewById(R.id.tvMeta);

        tvType.setText(item.workoutType);
        tvTrainer.setText(item.trainerName);
        tvDateTime.setText(item.date + " | " + item.time);
        tvMeta.setText("Длительность: " + item.duration + " мин\nЗаполнено: " + item.currentClients + "/" + item.maxClients);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this).setView(view).create();
        view.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showEditDialog(ScheduleItem item) {
        View view = getLayoutInflater().inflate(R.layout.dialog_schedule_edit, null);
        Spinner spinnerTrainer = view.findViewById(R.id.spinnerTrainer);
        TextInputEditText etWorkoutType = view.findViewById(R.id.etWorkoutType);
        TextInputEditText etDate = view.findViewById(R.id.etDate);
        MaterialAutoCompleteTextView etTime = view.findViewById(R.id.etTime);
        TextInputEditText etDuration = view.findViewById(R.id.etDuration);
        TextInputEditText etMaxClients = view.findViewById(R.id.etMaxClients);
        MaterialButton btnSave = view.findViewById(R.id.btnSave);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);

        ArrayList<Long> trainerIds = new ArrayList<>();
        ArrayList<String> trainerNames = new ArrayList<>();
        Cursor trainers = dbHelper.getAllTrainers();
        if (trainers != null) {
            while (trainers.moveToNext()) {
                trainerNames.add(trainers.getString(trainers.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME)));
                trainerIds.add(trainers.getLong(trainers.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)));
            }
            trainers.close();
        }
        if (trainerNames.isEmpty()) {
            trainerNames.add("Нет тренеров");
        }

        UiFormUtils.attachDarkSpinner(this, spinnerTrainer, trainerNames);
        UiFormUtils.attachDatePicker(this, etDate);
        UiFormUtils.attachQuarterHourTimePicker(this, etTime);

        int trainerIndex = trainerIds.indexOf(item.trainerId);
        spinnerTrainer.setSelection(Math.max(0, trainerIndex));
        etWorkoutType.setText(item.workoutType);
        etDate.setText(item.date);
        etTime.setText(item.time, false);
        etDuration.setText(String.valueOf(item.duration));
        etMaxClients.setText(String.valueOf(item.maxClients));

        AlertDialog dialog = new MaterialAlertDialogBuilder(this).setView(view).create();
        btnSave.setText("Сохранить изменения");
        btnCancel.setText("Отмена");
        btnSave.setOnClickListener(v -> {
            try {
                if (trainerIds.isEmpty()) {
                    Toast.makeText(this, "Сначала добавьте тренера", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isTimeInWorkRange(textOf(etTime))) {
                    Toast.makeText(this, "Выберите время с 08:00 до 22:00", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean success = dbHelper.updateSchedule(
                        item.id,
                        trainerIds.get(spinnerTrainer.getSelectedItemPosition()),
                        textOf(etWorkoutType),
                        textOf(etDate),
                        textOf(etTime),
                        Integer.parseInt(textOf(etDuration)),
                        Integer.parseInt(textOf(etMaxClients))
                );
                Toast.makeText(this, success ? "Тренировка обновлена" : "Не удалось обновить тренировку", Toast.LENGTH_SHORT).show();
                if (success) {
                    dialog.dismiss();
                    loadItems();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Проверьте поля тренировки", Toast.LENGTH_SHORT).show();
            }
        });
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void confirmDelete(ScheduleItem item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Удалить тренировку?")
                .setMessage("Вы точно хотите удалить " + item.workoutType + " на " + item.date + "?")
                .setPositiveButton("Удалить", (d, w) -> {
                    boolean success = dbHelper.deleteSchedule(item.id);
                    Toast.makeText(this, success ? "Тренировка удалена" : "Нельзя удалить тренировку, если есть записи", Toast.LENGTH_LONG).show();
                    if (success) loadItems();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private String textOf(android.widget.TextView editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    private boolean isTimeInWorkRange(String value) {
        if (value == null || !value.matches("\\d{2}:\\d{2}")) {
            return false;
        }
        int hour = Integer.parseInt(value.substring(0, 2));
        int minute = Integer.parseInt(value.substring(3, 5));
        int totalMinutes = hour * 60 + minute;
        return totalMinutes >= 8 * 60 && totalMinutes <= 22 * 60;
    }

    private class ScheduleListAdapter extends BaseAdapter {
        @Override public int getCount() { return visibleItems.size(); }
        @Override public Object getItem(int position) { return visibleItems.get(position); }
        @Override public long getItemId(int position) { return visibleItems.get(position).id; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(ScheduleManagementActivity.this).inflate(R.layout.item_schedule_management, parent, false);
                holder = new Holder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            holder.bind(visibleItems.get(position));
            return convertView;
        }
    }

    private class Holder {
        private final View root;
        private final TextView tvType;
        private final TextView tvDateTime;
        private final TextView tvTrainer;
        private final TextView tvMeta;
        private final MaterialButton btnEdit;
        private final MaterialButton btnDelete;

        Holder(View root) {
            this.root = root;
            tvType = root.findViewById(R.id.tvType);
            tvDateTime = root.findViewById(R.id.tvDateTime);
            tvTrainer = root.findViewById(R.id.tvTrainer);
            tvMeta = root.findViewById(R.id.tvMeta);
            btnEdit = root.findViewById(R.id.btnEdit);
            btnDelete = root.findViewById(R.id.btnDelete);
        }

        void bind(ScheduleItem item) {
            tvType.setText(item.workoutType);
            tvDateTime.setText(item.date + " | " + item.time);
            tvTrainer.setText(item.trainerName);
            tvMeta.setText("Длительность: " + item.duration + " мин | " + item.currentClients + "/" + item.maxClients);
            root.setOnClickListener(v -> showDetails(item));
            btnEdit.setOnClickListener(v -> showEditDialog(item));
            btnDelete.setOnClickListener(v -> confirmDelete(item));
        }
    }
}
