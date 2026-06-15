package com.example.fitbook;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class AddScheduleActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private Spinner spinnerTrainer;
    private TextInputEditText etWorkoutType;
    private TextInputEditText etDate;
    private MaterialAutoCompleteTextView etTime;
    private TextInputEditText etDuration;
    private TextInputEditText etMaxClients;
    private final ArrayList<Long> trainerIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule);

        dbHelper = new DatabaseHelper(this);
        spinnerTrainer = findViewById(R.id.spinnerTrainer);
        etWorkoutType = findViewById(R.id.etWorkoutType);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etDuration = findViewById(R.id.etDuration);
        etMaxClients = findViewById(R.id.etMaxClients);

        MaterialButton btnSave = findViewById(R.id.btnSave);
        MaterialButton btnCancel = findViewById(R.id.btnCancel);

        UiFormUtils.attachDatePicker(this, etDate);
        UiFormUtils.attachQuarterHourTimePicker(this, etTime);
        loadTrainers();
        btnSave.setOnClickListener(v -> saveSchedule());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadTrainers() {
        Cursor trainers = dbHelper.getAllTrainers();
        ArrayList<String> trainerNames = new ArrayList<>();
        trainerIds.clear();

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
    }

    private void saveSchedule() {
        if (trainerIds.isEmpty()) {
            Toast.makeText(this, "Сначала добавьте тренера", Toast.LENGTH_SHORT).show();
            return;
        }

        String workoutType = getText(etWorkoutType);
        String date = getText(etDate);
        String time = getText(etTime);
        String durationText = getText(etDuration);
        String maxClientsText = getText(etMaxClients);

        if (workoutType.isEmpty() || date.isEmpty() || time.isEmpty() || durationText.isEmpty() || maxClientsText.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int duration = Integer.parseInt(durationText);
            int maxClients = Integer.parseInt(maxClientsText);
            long trainerId = trainerIds.get(spinnerTrainer.getSelectedItemPosition());
            boolean success = dbHelper.addSchedule(trainerId, workoutType, date, time, duration, maxClients);
            Toast.makeText(this, success ? "Тренировка добавлена" : "Не удалось добавить тренировку", Toast.LENGTH_SHORT).show();
            if (success) finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Длительность и число клиентов должны быть числами", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getText(android.widget.TextView editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
