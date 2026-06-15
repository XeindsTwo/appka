package com.example.fitbook;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class TrainerManagementActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private final ArrayList<TrainerItem> allTrainerItems = new ArrayList<>();
    private final ArrayList<TrainerItem> visibleTrainerItems = new ArrayList<>();
    private TrainerListAdapter adapter;
    private EditText etSearch;
    private ListView listView;
    private TextView tvEmptyTrainers;

    private static class TrainerItem {
        long id;
        String name;
        String phone;
        String email;
        String specialization;
        int experience;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AuthGuard.requireRole(this, "admin")) return;
        setContentView(R.layout.activity_trainer_management);

        dbHelper = new DatabaseHelper(this);
        etSearch = findViewById(R.id.etSearch);
        listView = findViewById(R.id.listView);
        tvEmptyTrainers = findViewById(R.id.tvEmptyTrainers);
        adapter = new TrainerListAdapter();
        listView.setAdapter(adapter);

        findViewById(R.id.btnAddTrainer).setOnClickListener(v -> startActivity(new Intent(this, AddTrainerActivity.class)));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { renderTrainers(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadTrainers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!AuthGuard.requireRole(this, "admin")) return;
        loadTrainers();
    }

    private void loadTrainers() {
        allTrainerItems.clear();
        Cursor trainers = dbHelper.getAllTrainers();
        if (trainers != null) {
            while (trainers.moveToNext()) {
                TrainerItem item = new TrainerItem();
                item.id = trainers.getLong(trainers.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID));
                item.name = trainers.getString(trainers.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME));
                item.phone = trainers.getString(trainers.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE));
                item.email = trainers.getString(trainers.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL));
                item.specialization = trainers.getString(trainers.getColumnIndexOrThrow(DatabaseHelper.COL_SPECIALIZATION));
                item.experience = trainers.getInt(trainers.getColumnIndexOrThrow(DatabaseHelper.COL_EXPERIENCE));
                allTrainerItems.add(item);
            }
            trainers.close();
        }
        renderTrainers();
    }

    private void renderTrainers() {
        visibleTrainerItems.clear();
        String query = etSearch.getText() == null ? "" : etSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
        for (TrainerItem item : allTrainerItems) {
            String blob = (safe(item.name) + " " + safe(item.phone) + " " + safe(item.email) + " " + safe(item.specialization)).toLowerCase(Locale.ROOT);
            if (query.isEmpty() || blob.contains(query)) {
                visibleTrainerItems.add(item);
            }
        }

        adapter.notifyDataSetChanged();
        boolean isEmpty = visibleTrainerItems.isEmpty();
        tvEmptyTrainers.setText(query.isEmpty() ? "Тренеров ещё нет" : "Тренеры не найдены");
        listView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        tvEmptyTrainers.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void showTrainerInfo(TrainerItem item) {
        View view = getLayoutInflater().inflate(R.layout.dialog_trainer_details, null);
        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvSpec = view.findViewById(R.id.tvSpec);
        TextView tvExperience = view.findViewById(R.id.tvExperience);
        TextView tvPhone = view.findViewById(R.id.tvPhone);
        TextView tvEmail = view.findViewById(R.id.tvEmail);

        tvName.setText(item.name);
        tvSpec.setText(safe(item.specialization));
        tvExperience.setText("Опыт: " + item.experience + " лет");
        tvPhone.setText("Телефон: " + safe(item.phone));
        tvEmail.setText("Email: " + safe(item.email));

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .create();

        MaterialButton btnClose = view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.92f),
                    (int) (getResources().getDisplayMetrics().heightPixels * 0.78f)
            );
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams params = window.getAttributes();
            params.dimAmount = 0.82f;
            window.setAttributes(params);
        }
    }

    private void showEditTrainerDialog(TrainerItem item) {
        View view = getLayoutInflater().inflate(R.layout.dialog_trainer_edit, null);
        TextInputEditText etName = view.findViewById(R.id.etName);
        TextInputEditText etPhone = view.findViewById(R.id.etPhone);
        TextInputEditText etEmail = view.findViewById(R.id.etEmail);
        MaterialAutoCompleteTextView etSpecialization = view.findViewById(R.id.etSpecialization);
        TextInputEditText etExperience = view.findViewById(R.id.etExperience);

        etName.setText(item.name);
        etPhone.setText(item.phone);
        etEmail.setText(item.email);
        etSpecialization.setText(item.specialization, false);
        etExperience.setText(String.valueOf(item.experience));
        UiFormUtils.attachDarkDropdown(this, etSpecialization, Arrays.asList(getResources().getStringArray(R.array.specialization_options)));

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setPositiveButton("Сохранить", null)
                .setNegativeButton("Отмена", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = textOf(etName);
            String phone = textOf(etPhone);
            String email = textOf(etEmail);
            String spec = textOf(etSpecialization);
            String expText = textOf(etExperience);
            if (name.isEmpty() || phone.isEmpty() || spec.isEmpty() || expText.isEmpty()) {
                Toast.makeText(this, "Заполните все обязательные поля", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                boolean success = dbHelper.updateTrainer(item.id, name, phone, email, spec, Integer.parseInt(expText));
                Toast.makeText(this, success ? "Тренер обновлён" : "Не удалось обновить тренера", Toast.LENGTH_SHORT).show();
                if (success) {
                    dialog.dismiss();
                    loadTrainers();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Опыт должен быть числом", Toast.LENGTH_SHORT).show();
            }
        }));
        dialog.show();
    }

    private void confirmDeleteTrainer(TrainerItem item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Удалить тренера?")
                .setMessage("Вы уверены, что хотите удалить " + item.name + "?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    boolean success = dbHelper.deleteTrainer(item.id);
                    Toast.makeText(this, success ? "Тренер удалён" : "Нельзя удалить тренера, если он занят в расписании", Toast.LENGTH_LONG).show();
                    if (success) {
                        loadTrainers();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private String textOf(TextView editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    private class TrainerListAdapter extends BaseAdapter {
        @Override public int getCount() { return visibleTrainerItems.size(); }
        @Override public Object getItem(int position) { return visibleTrainerItems.get(position); }
        @Override public long getItemId(int position) { return visibleTrainerItems.get(position).id; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TrainerRowHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(TrainerManagementActivity.this).inflate(R.layout.item_trainer_management, parent, false);
                holder = new TrainerRowHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (TrainerRowHolder) convertView.getTag();
            }
            holder.bind(visibleTrainerItems.get(position));
            return convertView;
        }
    }

    private class TrainerRowHolder {
        private final View root;
        private final TextView tvName;
        private final TextView tvSpec;
        private final TextView tvMeta;
        private final MaterialButton btnEdit;
        private final MaterialButton btnDelete;

        TrainerRowHolder(View root) {
            this.root = root;
            tvName = root.findViewById(R.id.tvName);
            tvSpec = root.findViewById(R.id.tvSpec);
            tvMeta = root.findViewById(R.id.tvMeta);
            btnEdit = root.findViewById(R.id.btnEdit);
            btnDelete = root.findViewById(R.id.btnDelete);
        }

        void bind(TrainerItem item) {
            tvName.setText(item.name);
            tvSpec.setText(safe(item.specialization));
            tvMeta.setText(safe(item.phone) + " | " + item.experience + " лет");
            root.setOnClickListener(v -> showTrainerInfo(item));
            btnEdit.setOnClickListener(v -> showEditTrainerDialog(item));
            btnDelete.setOnClickListener(v -> confirmDeleteTrainer(item));
        }
    }
}
