package com.example.fitbook;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

public class AddTrainerActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextInputEditText etName;
    private TextInputEditText etPhone;
    private TextInputEditText etEmail;
    private MaterialAutoCompleteTextView etSpecialization;
    private TextInputEditText etExperience;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trainer);

        dbHelper = new DatabaseHelper(this);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etSpecialization = findViewById(R.id.etSpecialization);
        etExperience = findViewById(R.id.etExperience);

        UiFormUtils.attachDarkDropdown(this, etSpecialization, java.util.Arrays.asList(getResources().getStringArray(R.array.specialization_options)));

        MaterialButton btnSave = findViewById(R.id.btnSave);
        MaterialButton btnCancel = findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(v -> saveTrainer());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveTrainer() {
        String name = getText(etName);
        String phone = getText(etPhone);
        String email = getText(etEmail);
        String specialization = getText(etSpecialization);
        String experienceText = getText(etExperience);

        if (name.isEmpty() || phone.isEmpty() || specialization.isEmpty() || experienceText.isEmpty()) {
            Toast.makeText(this, "Заполните все обязательные поля", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int experience = Integer.parseInt(experienceText);
            boolean success = dbHelper.addTrainer(name, phone, email, specialization, experience);
            Toast.makeText(this, success ? "Тренер добавлен" : "Не удалось добавить тренера", Toast.LENGTH_SHORT).show();
            if (success) finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Опыт должен быть числом", Toast.LENGTH_SHORT).show();
        }
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private String getText(MaterialAutoCompleteTextView editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
