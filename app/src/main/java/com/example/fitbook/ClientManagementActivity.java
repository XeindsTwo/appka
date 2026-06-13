package com.example.fitbook;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Locale;

public class ClientManagementActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private final ArrayList<ClientItem> allClientItems = new ArrayList<>();
    private final ArrayList<ClientItem> visibleClientItems = new ArrayList<>();
    private final ArrayList<String> visibleRows = new ArrayList<>();
    private final ArrayList<Long> visibleIds = new ArrayList<>();
    private final ArrayList<String> trainerFilterNames = new ArrayList<>();
    private final ArrayList<String> membershipFilterNames = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private ArrayAdapter<String> trainerAdapter;
    private ArrayAdapter<String> membershipAdapter;
    private EditText etSearch;
    private Spinner spinnerTrainer;
    private Spinner spinnerMembership;
    private ListView listView;

    private static class ClientItem {
        long id;
        String username;
        String fullName;
        String phone;
        String email;
        String trainerName;
        String membershipName;
        String membershipStatus;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_management);

        dbHelper = new DatabaseHelper(this);
        etSearch = findViewById(R.id.etSearch);
        spinnerTrainer = findViewById(R.id.spinnerTrainer);
        spinnerMembership = findViewById(R.id.spinnerMembership);
        listView = findViewById(R.id.listView);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, visibleRows);
        listView.setAdapter(adapter);

        findViewById(R.id.btnRegisterClient).setOnClickListener(v -> showRegisterClientDialog());

        trainerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, trainerFilterNames);
        trainerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTrainer.setAdapter(trainerAdapter);
        spinnerTrainer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { renderClients(); }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        membershipAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, membershipFilterNames);
        membershipAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMembership.setAdapter(membershipAdapter);
        spinnerMembership.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { renderClients(); }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { renderClients(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        listView.setOnItemClickListener((parent, view, position, id) -> showClientActions(position));

        loadClients();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClients();
    }

    private void loadClients() {
        allClientItems.clear();
        Cursor clients = dbHelper.getAllClientsWithTrainer();
        if (clients != null) {
            while (clients.moveToNext()) {
                ClientItem item = new ClientItem();
                item.id = clients.getLong(clients.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID));
                item.username = clients.getString(clients.getColumnIndexOrThrow(DatabaseHelper.COL_USERNAME));
                item.fullName = clients.getString(clients.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME));
                item.phone = clients.getString(clients.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE));
                item.email = clients.getString(clients.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL));
                item.trainerName = clients.getString(clients.getColumnIndexOrThrow("trainer_name"));
                item.membershipName = clients.getString(clients.getColumnIndexOrThrow("membership_name"));
                item.membershipStatus = clients.getString(clients.getColumnIndexOrThrow("membership_status"));
                allClientItems.add(item);
            }
            clients.close();
        }
        refreshFilters();
        renderClients();
    }

    private void refreshFilters() {
        if (trainerFilterNames.isEmpty()) {
            trainerFilterNames.add("Все тренеры");
            ArrayList<String> trainerNames = new ArrayList<>();
            for (ClientItem item : allClientItems) {
                if (item.trainerName != null && !trainerNames.contains(item.trainerName) && !"Не назначен".equals(item.trainerName)) {
                    trainerNames.add(item.trainerName);
                }
            }
            trainerNames.sort(String::compareToIgnoreCase);
            trainerFilterNames.addAll(trainerNames);
            trainerAdapter.notifyDataSetChanged();
        }

        if (membershipFilterNames.isEmpty()) {
            membershipFilterNames.add("Все статусы");
            membershipFilterNames.add("Активный");
            membershipFilterNames.add("Истекший");
            membershipFilterNames.add("Без абонемента");
            membershipAdapter.notifyDataSetChanged();
        }
    }

    private void renderClients() {
        visibleClientItems.clear();
        visibleRows.clear();
        visibleIds.clear();

        String query = textOf(etSearch).toLowerCase(Locale.ROOT);
        String selectedTrainer = selectedOf(spinnerTrainer);
        String selectedMembership = selectedOf(spinnerMembership);

        for (ClientItem item : allClientItems) {
            String searchBlob = (safe(item.username) + " " + safe(item.fullName) + " " + safe(item.phone) + " " + safe(item.email) + " " + safe(item.trainerName)).toLowerCase(Locale.ROOT);
            boolean searchMatches = query.isEmpty() || searchBlob.contains(query);

            boolean trainerMatches = "Все тренеры".equals(selectedTrainer)
                    || selectedTrainer.equals(item.trainerName);

            boolean membershipMatches = "Все статусы".equals(selectedMembership)
                    || ("Активный".equals(selectedMembership) && "active".equals(item.membershipStatus))
                    || ("Истекший".equals(selectedMembership) && "expired".equals(item.membershipStatus))
                    || ("Без абонемента".equals(selectedMembership) && ("none".equals(item.membershipStatus) || item.membershipStatus == null));

            if (searchMatches && trainerMatches && membershipMatches) {
                visibleClientItems.add(item);
                visibleIds.add(item.id);
                visibleRows.add(item.fullName +
                        "\n@" + item.username +
                        "\nТренер: " + safe(item.trainerName) +
                        "\nАбонемент: " + safe(item.membershipName) + " • " + statusLabel(item.membershipStatus));
            }
        }

        if (visibleRows.isEmpty()) {
            visibleRows.add("Клиенты не найдены");
        }
        adapter.notifyDataSetChanged();
    }

    private void showClientActions(int position) {
        if (position >= visibleClientItems.size()) return;
        ClientItem item = visibleClientItems.get(position);
        String[] actions = {"Просмотр", "Редактировать", "Назначить тренера", "Удалить"};
        new MaterialAlertDialogBuilder(this)
                .setTitle(item.fullName)
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        showClientInfo(item);
                    } else if (which == 1) {
                        showEditClientDialog(item);
                    } else if (which == 2) {
                        showAssignTrainerDialog(item);
                    } else if (which == 3) {
                        confirmDeleteClient(item);
                    }
                })
                .show();
    }

    private void showClientInfo(ClientItem item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Карточка клиента")
                .setMessage("Логин: " + item.username +
                        "\nФИО: " + item.fullName +
                        "\nТелефон: " + item.phone +
                        "\nEmail: " + item.email +
                        "\nТренер: " + safe(item.trainerName) +
                        "\nАбонемент: " + safe(item.membershipName) +
                        "\nСтатус: " + statusLabel(item.membershipStatus))
                .setPositiveButton("ОК", null)
                .show();
    }

    private void showEditClientDialog(ClientItem item) {
        View view = getLayoutInflater().inflate(R.layout.dialog_client_edit, null);
        TextInputEditText etUsername = view.findViewById(R.id.etUsername);
        TextInputEditText etPassword = view.findViewById(R.id.etPassword);
        TextInputEditText etFullName = view.findViewById(R.id.etFullName);
        TextInputEditText etPhone = view.findViewById(R.id.etPhone);
        TextInputEditText etEmail = view.findViewById(R.id.etEmail);

        etUsername.setText(item.username);
        etFullName.setText(item.fullName);
        etPhone.setText(item.phone);
        etEmail.setText(item.email);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setPositiveButton("Сохранить", null)
                .setNegativeButton("Отмена", (d, w) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String username = textOf(etUsername);
            String password = textOf(etPassword);
            String fullName = textOf(etFullName);
            String phone = textOf(etPhone);
            String email = textOf(etEmail);
            if (username.isEmpty() || fullName.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean success = dbHelper.updateClient(item.id, username, password, fullName, phone, email);
            Toast.makeText(this, success ? "Клиент обновлён" : "Не удалось обновить клиента", Toast.LENGTH_SHORT).show();
            if (success) {
                dialog.dismiss();
                loadClients();
            }
        }));
        dialog.show();
    }

    private void showAssignTrainerDialog(ClientItem item) {
        Cursor trainers = dbHelper.getAllTrainers();
        ArrayList<String> trainerNames = new ArrayList<>();
        ArrayList<Long> trainerIds = new ArrayList<>();
        trainerNames.add("Не назначен");
        trainerIds.add(-1L);
        if (trainers != null) {
            while (trainers.moveToNext()) {
                trainerNames.add(trainers.getString(trainers.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME)));
                trainerIds.add(trainers.getLong(trainers.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)));
            }
            trainers.close();
        }

        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, trainerNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        int selectedIndex = 0;
        if (item.trainerName != null) {
            int index = trainerNames.indexOf(item.trainerName);
            if (index >= 0) selectedIndex = index;
        }
        spinner.setSelection(selectedIndex);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Назначить тренера")
                .setView(spinner)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    int index = spinner.getSelectedItemPosition();
                    if (index <= 0) {
                        dbHelper.clearClientTrainer(item.id);
                        Toast.makeText(this, "Тренер снят", Toast.LENGTH_SHORT).show();
                    } else {
                        dbHelper.assignTrainerToClient(item.id, trainerIds.get(index), "");
                        Toast.makeText(this, "Тренер назначен", Toast.LENGTH_SHORT).show();
                    }
                    loadClients();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void confirmDeleteClient(ClientItem item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Удалить клиента?")
                .setMessage("Удалить клиента " + item.fullName + " со всеми связанными данными?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    boolean success = dbHelper.deleteClient(item.id);
                    Toast.makeText(this, success ? "Клиент удалён" : "Не удалось удалить клиента", Toast.LENGTH_SHORT).show();
                    if (success) loadClients();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showRegisterClientDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_client_register, null);
        TextInputEditText etUsername = view.findViewById(R.id.etUsername);
        TextInputEditText etPassword = view.findViewById(R.id.etPassword);
        TextInputEditText etFullName = view.findViewById(R.id.etFullName);
        TextInputEditText etPhone = view.findViewById(R.id.etPhone);
        TextInputEditText etEmail = view.findViewById(R.id.etEmail);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setPositiveButton("Создать", null)
                .setNegativeButton("Отмена", (d, w) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String username = textOf(etUsername);
            String password = textOf(etPassword);
            String fullName = textOf(etFullName);
            String phone = textOf(etPhone);
            String email = textOf(etEmail);
            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Заполните все обязательные поля", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean success = dbHelper.registerClient(username, password, fullName, phone, email);
            Toast.makeText(this, success ? "Клиент создан" : "Логин уже существует", Toast.LENGTH_SHORT).show();
            if (success) {
                dialog.dismiss();
                loadClients();
            }
        }));
        dialog.show();
    }

    private String textOf(TextView textView) {
        return textView.getText() == null ? "" : textView.getText().toString().trim();
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "—" : value;
    }

    private String selectedOf(Spinner spinner) {
        return spinner.getSelectedItem() == null ? "" : spinner.getSelectedItem().toString();
    }

    private String statusLabel(String status) {
        if ("active".equals(status)) return "Активный";
        if ("expired".equals(status)) return "Истекший";
        return "Без абонемента";
    }
}
