package com.example.fitbook;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;


public class AdminActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SharedPreferences prefs;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> dataList;
    private ArrayList<Long> itemIds;
    private ArrayList<ClientItem> allClientItems;
    private Button btnAddMembership;
    private TextView tvAdminName, tvSectionTitle;
    private View clientFilterContainer;
    private EditText etClientSearch;
    private Spinner spinnerClientTrainerFilter;
    private Spinner spinnerClientMembershipFilter;
    private Spinner spinnerClientSort;
    private ArrayAdapter<String> clientTrainerFilterAdapter;
    private ArrayAdapter<String> clientMembershipFilterAdapter;
    private ArrayAdapter<String> clientSortFilterAdapter;
    private final ArrayList<String> clientTrainerFilterNames = new ArrayList<>();
    private final ArrayList<String> clientMembershipFilterNames = new ArrayList<>();
    private final ArrayList<String> clientSortFilterNames = new ArrayList<>();
    private String currentMode = "schedule";

    private static class ClientItem {
        long id;
        String username;
        String fullName;
        String phone;
        String email;
        String trainerName;
        String membershipName;
        String membershipStatus;
        String membershipStatusLabel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AuthGuard.requireRole(this, "admin")) return;
        setContentView(R.layout.activity_admin);

        dbHelper = new DatabaseHelper(this);
        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);

        String adminName = prefs.getString(DatabaseHelper.COL_FULL_NAME, "Администратор");

        tvAdminName = findViewById(R.id.tvAdminName);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        btnAddMembership = findViewById(R.id.btnAddMembership);
        listView = findViewById(R.id.listView);
        tvAdminName.setText("Администрирование клубом");
        tvSectionTitle.setText("Управление клубом");

        dataList = new ArrayList<>();
        itemIds = new ArrayList<>();
        allClientItems = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, R.layout.item_dark_list_text, dataList);
        listView.setAdapter(adapter);

        clientFilterContainer = findViewById(R.id.clientFilterContainer);
        etClientSearch = findViewById(R.id.etClientSearch);
        spinnerClientTrainerFilter = findViewById(R.id.spinnerClientTrainerFilter);
        spinnerClientMembershipFilter = findViewById(R.id.spinnerClientMembershipFilter);
        spinnerClientSort = findViewById(R.id.spinnerClientSort);

        // Кнопки
        findViewById(R.id.btnAddTrainer).setOnClickListener(v -> startActivity(new Intent(this, AddTrainerActivity.class)));
        findViewById(R.id.btnAddSchedule).setOnClickListener(v -> startActivity(new Intent(this, AddScheduleActivity.class)));
        findViewById(R.id.btnViewClients).setOnClickListener(v -> {
            startActivity(new Intent(this, ClientManagementActivity.class));
        });
        findViewById(R.id.btnViewTrainers).setOnClickListener(v -> {
            startActivity(new Intent(this, TrainerManagementActivity.class));
        });
        findViewById(R.id.btnViewSchedule).setOnClickListener(v -> startActivity(new Intent(this, ScheduleManagementActivity.class)));
        findViewById(R.id.btnMembershipTypes).setOnClickListener(v -> startActivity(new Intent(this, MembershipManagementActivity.class)));
        findViewById(R.id.btnRegisterClient).setOnClickListener(v -> showRegisterClientDialog());
        findViewById(R.id.btnOpenProfile).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        btnAddMembership.setOnClickListener(v -> showAddMembershipDialog());
        setupBottomNavigation();

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            showEditDeleteDialog(position);
            return true;
        });

        loadSchedule();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!AuthGuard.requireRole(this, "admin")) return;
        if ("trainers".equals(currentMode)) {
            btnAddMembership.setVisibility(View.GONE);
            loadTrainers();
        } else if ("memberships".equals(currentMode)) {
            btnAddMembership.setVisibility(View.VISIBLE);
            loadMembershipTypes();
        } else if ("clients".equals(currentMode)) {
            btnAddMembership.setVisibility(View.GONE);
            showClientSection();
        } else {
            btnAddMembership.setVisibility(View.GONE);
            loadSchedule();
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_schedule);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_schedule) {
                startActivity(new Intent(this, ScheduleManagementActivity.class));
                return true;
            } else if (itemId == R.id.nav_clients) {
                startActivity(new Intent(this, ClientManagementActivity.class));
                return true;
            } else if (itemId == R.id.nav_trainers) {
                startActivity(new Intent(this, TrainerManagementActivity.class));
                return true;
            } else if (itemId == R.id.nav_memberships) {
                startActivity(new Intent(this, MembershipManagementActivity.class));
                return true;
            }
            return false;
        });
    }

    private void showClientSection() {
        currentMode = "clients";
        btnAddMembership.setVisibility(View.GONE);
        tvSectionTitle.setText("Клиентская база");
        clientFilterContainer.setVisibility(View.VISIBLE);
        refreshClientFilterOptions();
        loadClients();
    }

    private void refreshClientFilterOptions() {
        if (clientTrainerFilterAdapter == null) {
            clientTrainerFilterAdapter = new ArrayAdapter<>(this, R.layout.item_dropdown_dark, clientTrainerFilterNames);
            clientTrainerFilterAdapter.setDropDownViewResource(R.layout.item_dropdown_dark_dropdown);
            spinnerClientTrainerFilter.setAdapter(clientTrainerFilterAdapter);
            spinnerClientTrainerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    renderClients();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        if (clientMembershipFilterAdapter == null) {
            clientMembershipFilterAdapter = new ArrayAdapter<>(this, R.layout.item_dropdown_dark, clientMembershipFilterNames);
            clientMembershipFilterAdapter.setDropDownViewResource(R.layout.item_dropdown_dark_dropdown);
            spinnerClientMembershipFilter.setAdapter(clientMembershipFilterAdapter);
            spinnerClientMembershipFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    renderClients();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        if (clientSortFilterAdapter == null) {
            clientSortFilterAdapter = new ArrayAdapter<>(this, R.layout.item_dropdown_dark, clientSortFilterNames);
            clientSortFilterAdapter.setDropDownViewResource(R.layout.item_dropdown_dark_dropdown);
            spinnerClientSort.setAdapter(clientSortFilterAdapter);
            spinnerClientSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    renderClients();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        clientTrainerFilterNames.clear();
        clientTrainerFilterNames.add("Все тренеры");
        Cursor trainers = dbHelper.getAllTrainers();
        while (trainers.moveToNext()) {
            clientTrainerFilterNames.add(trainers.getString(trainers.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME)));
        }
        trainers.close();
        clientTrainerFilterAdapter.notifyDataSetChanged();

        if (clientMembershipFilterNames.isEmpty()) {
            clientMembershipFilterNames.add("Все статусы");
            clientMembershipFilterNames.add("Активный абонемент");
            clientMembershipFilterNames.add("Истекший абонемент");
            clientMembershipFilterNames.add("Без абонемента");
            clientMembershipFilterAdapter.notifyDataSetChanged();
        }

        if (clientSortFilterNames.isEmpty()) {
            clientSortFilterNames.add("Сортировка: по имени");
            clientSortFilterNames.add("Сортировка: по тренеру");
            clientSortFilterNames.add("Сортировка: по абонементу");
            clientSortFilterAdapter.notifyDataSetChanged();
        }

        spinnerClientTrainerFilter.setSelection(0, false);
        spinnerClientMembershipFilter.setSelection(0, false);
        spinnerClientSort.setSelection(0, false);
        etClientSearch.setText("");
        findViewById(R.id.btnResetClientFilters).setOnClickListener(v -> {
            spinnerClientTrainerFilter.setSelection(0, false);
            spinnerClientMembershipFilter.setSelection(0, false);
            spinnerClientSort.setSelection(0, false);
            etClientSearch.setText("");
            renderClients();
        });

        etClientSearch.setOnEditorActionListener((v, actionId, event) -> {
            renderClients();
            return false;
        });
        etClientSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { renderClients(); }
            @Override public void afterTextChanged(android.text.Editable s) { }
        });
    }

    private void renderClients() {
        dataList.clear();
        itemIds.clear();

        String searchQuery = etClientSearch != null && etClientSearch.getText() != null
                ? etClientSearch.getText().toString().trim().toLowerCase()
                : "";
        String selectedTrainer = spinnerClientTrainerFilter != null && spinnerClientTrainerFilter.getSelectedItem() != null
                ? spinnerClientTrainerFilter.getSelectedItem().toString()
                : "Все тренеры";
        String selectedMembership = spinnerClientMembershipFilter != null && spinnerClientMembershipFilter.getSelectedItem() != null
                ? spinnerClientMembershipFilter.getSelectedItem().toString()
                : "Все статусы";
        String selectedSort = spinnerClientSort != null && spinnerClientSort.getSelectedItem() != null
                ? spinnerClientSort.getSelectedItem().toString()
                : "Сортировка: по имени";

        ArrayList<ClientItem> filteredItems = new ArrayList<>();
        for (ClientItem clientItem : allClientItems) {
            String normalizedSearch = (clientItem.fullName + " " + clientItem.username + " " + clientItem.phone + " " + clientItem.email + " " + clientItem.trainerName)
                    .toLowerCase();
            boolean trainerMatches = selectedTrainer.equals("Все тренеры") || clientItem.trainerName.equals(selectedTrainer);
            boolean membershipMatches = selectedMembership.equals("Все статусы")
                    || (selectedMembership.equals("Активный абонемент") && "active".equals(clientItem.membershipStatus))
                    || (selectedMembership.equals("Истекший абонемент") && "expired".equals(clientItem.membershipStatus))
                    || (selectedMembership.equals("Без абонемента") && "none".equals(clientItem.membershipStatus));
            boolean searchMatches = searchQuery.isEmpty() || normalizedSearch.contains(searchQuery);

            if (trainerMatches && membershipMatches && searchMatches) {
                filteredItems.add(clientItem);
            }
        }

        if (selectedSort.equals("Сортировка: по тренеру")) {
            filteredItems.sort((left, right) -> left.trainerName.compareToIgnoreCase(right.trainerName));
        } else if (selectedSort.equals("Сортировка: по абонементу")) {
            filteredItems.sort((left, right) -> left.membershipStatusLabel.compareToIgnoreCase(right.membershipStatusLabel));
        } else {
            filteredItems.sort((left, right) -> left.fullName.compareToIgnoreCase(right.fullName));
        }

        for (ClientItem clientItem : filteredItems) {
            itemIds.add(clientItem.id);
            dataList.add("ФИО: " + clientItem.fullName +
                    "\nТелефон: " + clientItem.phone +
                    "\nEmail: " + clientItem.email +
                    "\nТренер: " + clientItem.trainerName +
                    "\nАбонемент: " + clientItem.membershipName + " (" + clientItem.membershipStatusLabel + ")");
        }

        if (dataList.isEmpty()) {
            dataList.add("Под фильтр ничего не найдено");
        }

        adapter.notifyDataSetChanged();
    }
        private void showAddTrainerDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_trainer_add, null);

        EditText etName = view.findViewById(R.id.etName);
        EditText etPhone = view.findViewById(R.id.etPhone);
        EditText etEmail = view.findViewById(R.id.etEmail);
        com.google.android.material.textfield.MaterialAutoCompleteTextView etSpecialization = view.findViewById(R.id.etSpecialization);
        EditText etExperience = view.findViewById(R.id.etExperience);

        ArrayAdapter<String> specializationAdapter = new ArrayAdapter<>(this, R.layout.item_dropdown_dark, getResources().getStringArray(R.array.specialization_options));
        specializationAdapter.setDropDownViewResource(R.layout.item_dropdown_dark_dropdown);
        etSpecialization.setAdapter(specializationAdapter);
        etSpecialization.setInputType(android.text.InputType.TYPE_NULL);
        etSpecialization.setKeyListener(null);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_add_trainer_title)
                .setView(view)
                .setPositiveButton(R.string.action_add, null)
                .setNegativeButton(R.string.action_cancel, (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String name = etName.getText() == null ? "" : etName.getText().toString().trim();
                    String phone = etPhone.getText() == null ? "" : etPhone.getText().toString().trim();
                    String email = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();
                    String specialization = etSpecialization.getText() == null ? "" : etSpecialization.getText().toString().trim();
                    String experienceText = etExperience.getText() == null ? "" : etExperience.getText().toString().trim();

                    if (name.isEmpty() || phone.isEmpty() || specialization.isEmpty() || experienceText.isEmpty()) {
                        Toast.makeText(this, R.string.dialog_fill_trainer, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        boolean success = dbHelper.addTrainer(name, phone, email, specialization, Integer.parseInt(experienceText));
                        if (success) {
                            Toast.makeText(this, R.string.dialog_trainer_added, Toast.LENGTH_SHORT).show();
                            currentMode = "trainers";
                            loadTrainers();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(this, R.string.dialog_trainer_add_error, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, getString(R.string.dialog_error_prefix, e.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                }));

        dialog.show();
    }


    private void showAddScheduleDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_schedule_add, null);
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .create();

        Spinner spinnerTrainer = view.findViewById(R.id.spinnerTrainer);
        EditText etWorkoutType = view.findViewById(R.id.etWorkoutType);
        EditText etDate = view.findViewById(R.id.etDate);
        com.google.android.material.textfield.MaterialAutoCompleteTextView etTime = view.findViewById(R.id.etTime);
        EditText etDuration = view.findViewById(R.id.etDuration);
        EditText etMaxClients = view.findViewById(R.id.etMaxClients);
        View btnSave = view.findViewById(R.id.btnSave);
        View btnCancel = view.findViewById(R.id.btnCancel);

        Cursor trainers = dbHelper.getAllTrainers();
        ArrayList<String> trainerNames = new ArrayList<>();
        ArrayList<Long> trainerIds = new ArrayList<>();
        while (trainers.moveToNext()) {
            trainerNames.add(trainers.getString(trainers.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME)));
            trainerIds.add(trainers.getLong(trainers.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)));
        }
        trainers.close();

        UiFormUtils.attachDarkSpinner(this, spinnerTrainer, trainerNames);
        UiFormUtils.attachDatePicker(this, etDate);
        UiFormUtils.attachQuarterHourTimePicker(this, etTime);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            try {
                int pos = spinnerTrainer.getSelectedItemPosition();
                String selectedTime = etTime.getText() == null ? "" : etTime.getText().toString().trim();
                if (!isTimeInWorkRange(selectedTime)) {
                    Toast.makeText(this, "Выберите время с 08:00 до 22:00", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean success = dbHelper.addSchedule(
                        trainerIds.get(pos),
                        etWorkoutType.getText() == null ? "" : etWorkoutType.getText().toString().trim(),
                        etDate.getText() == null ? "" : etDate.getText().toString().trim(),
                        selectedTime,
                        Integer.parseInt(etDuration.getText() == null ? "0" : etDuration.getText().toString().trim()),
                        Integer.parseInt(etMaxClients.getText() == null ? "0" : etMaxClients.getText().toString().trim())
                );
                Toast.makeText(this, success ? "Тренировка создана" : "Не удалось создать тренировку", Toast.LENGTH_SHORT).show();
                if (success) {
                    currentMode = "schedule";
                    loadSchedule();
                    dialog.dismiss();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

        private void showRegisterClientDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_client_register, null);

        EditText etUsername = view.findViewById(R.id.etUsername);
        EditText etPassword = view.findViewById(R.id.etPassword);
        EditText etFullName = view.findViewById(R.id.etFullName);
        EditText etPhone = view.findViewById(R.id.etPhone);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_register_client_title)
                .setView(view)
                .setPositiveButton(R.string.register_button, null)
                .setNegativeButton(R.string.action_cancel, (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String username = etUsername.getText() == null ? "" : etUsername.getText().toString().trim();
                    String password = etPassword.getText() == null ? "" : etPassword.getText().toString().trim();
                    String fullName = etFullName.getText() == null ? "" : etFullName.getText().toString().trim();
                    String phone = etPhone.getText() == null ? "" : etPhone.getText().toString().trim();

                    if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || phone.isEmpty()) {
                        Toast.makeText(this, R.string.dialog_fill_client_register, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        boolean success = dbHelper.registerClient(username, password, fullName, phone, "");
                        if (success) {
                            Toast.makeText(this, R.string.dialog_client_registered, Toast.LENGTH_LONG).show();
                            currentMode = "clients";
                            loadClients();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(this, R.string.dialog_client_register_error, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, getString(R.string.dialog_error_prefix, e.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                }));

        dialog.show();
    }


    private void loadClients() {
        clientFilterContainer.setVisibility(View.VISIBLE);
        allClientItems.clear();
        Cursor clients = dbHelper.getAllClientsWithTrainer();
        while (clients.moveToNext()) {
            ClientItem clientItem = new ClientItem();
            clientItem.id = clients.getLong(clients.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID));
            clientItem.username = clients.getString(clients.getColumnIndexOrThrow(DatabaseHelper.COL_USERNAME));
            clientItem.fullName = clients.getString(clients.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME));
            clientItem.phone = clients.getString(clients.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE));
            clientItem.email = clients.getString(clients.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL));
            clientItem.trainerName = clients.getString(clients.getColumnIndexOrThrow("trainer_name"));
            clientItem.membershipName = clients.getString(clients.getColumnIndexOrThrow("membership_name"));
            clientItem.membershipStatus = clients.getString(clients.getColumnIndexOrThrow("membership_status"));
            if ("active".equals(clientItem.membershipStatus)) {
                clientItem.membershipStatusLabel = "Активный";
            } else if ("expired".equals(clientItem.membershipStatus)) {
                clientItem.membershipStatusLabel = "Истекший";
            } else {
                clientItem.membershipStatusLabel = "Без абонемента";
            }
            allClientItems.add(clientItem);
        }
        clients.close();
        renderClients();
    }
    private void loadTrainers() {
        clientFilterContainer.setVisibility(View.GONE);
        dataList.clear();
        itemIds.clear();
        Cursor trainers = dbHelper.getAllTrainers();
        while (trainers.moveToNext()) {
            long id = trainers.getLong(trainers.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID));
            String name = trainers.getString(trainers.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME));
            String phone = trainers.getString(trainers.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE));
            String spec = trainers.getString(trainers.getColumnIndexOrThrow(DatabaseHelper.COL_SPECIALIZATION));
            int exp = trainers.getInt(trainers.getColumnIndexOrThrow(DatabaseHelper.COL_EXPERIENCE));

            itemIds.add(id);
            dataList.add("Тренер: " + name + "\nТелефон: " + phone + "\nСпециализация: " + spec + "\nОпыт: " + exp + " лет");
        }
        trainers.close();
        adapter.notifyDataSetChanged();

        if (dataList.isEmpty()) {
            dataList.add("Нет зарегистрированных тренеров");
            adapter.notifyDataSetChanged();
        }
    }

    private void loadSchedule() {
        clientFilterContainer.setVisibility(View.GONE);
        dataList.clear();
        itemIds.clear();
        Cursor schedule = dbHelper.getAllSchedule();
        while (schedule.moveToNext()) {
            long id = schedule.getLong(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_SCHEDULE_ID));
            String type = schedule.getString(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_TYPE));
            String date = schedule.getString(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_DATE));
            String time = schedule.getString(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_TIME));
            String trainer = schedule.getString(schedule.getColumnIndexOrThrow("trainer_name"));
            int current = schedule.getInt(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_CURRENT_CLIENTS));
            int max = schedule.getInt(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_MAX_CLIENTS));

            itemIds.add(id);
            dataList.add("Тренировка: " + type + "\nДата: " + date + " " + time + "\nТренер: " + trainer + "\nЗаписано: " + current + "/" + max);
        }
        schedule.close();
        adapter.notifyDataSetChanged();

        if (dataList.isEmpty()) {
            dataList.add("Нет запланированных тренировок");
            adapter.notifyDataSetChanged();
        }
    }

    private void loadMembershipTypes() {
        clientFilterContainer.setVisibility(View.GONE);
        dataList.clear();
        itemIds.clear();
        Cursor types = dbHelper.getAllMembershipTypes();
        if (types != null && types.moveToFirst()) {
            do {
                long id = types.getLong(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_ID));
                String name = types.getString(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_NAME));
                String description = types.getString(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_DESCRIPTION));
                int days = types.getInt(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_DURATION_DAYS));
                int price = types.getInt(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_PRICE));
                int isActive = types.getInt(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_IS_ACTIVE));

                itemIds.add(id);
                String status = isActive == 1 ? "Активен" : "Неактивен";
                dataList.add("Абонемент: " + name + " (" + status + ")" +
                        "\nОписание: " + description +
                        "\nСрок: " + days + " дней" +
                        "\nЦена: " + price + " ₽");
            } while (types.moveToNext());
            types.close();
        } else {
            dataList.add("Нет созданных абонементов");
        }
        adapter.notifyDataSetChanged();
    }

        private void showAddMembershipDialog() {
        AlertDialog.Builder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_membership_add, null);

        EditText etName = view.findViewById(R.id.etName);
        EditText etDescription = view.findViewById(R.id.etDescription);
        EditText etDurationDays = view.findViewById(R.id.etDurationDays);
        EditText etPrice = view.findViewById(R.id.etPrice);

        builder.setTitle(R.string.dialog_add_membership_title)
                .setView(view)
                .setPositiveButton(R.string.action_add, (dialog, which) -> {
                    try {
                        boolean success = dbHelper.createMembershipType(
                                etName.getText().toString(),
                                etDescription.getText().toString(),
                                Integer.parseInt(etDurationDays.getText().toString()),
                                Integer.parseInt(etPrice.getText().toString())
                        );
                        Toast.makeText(this, success ? R.string.dialog_membership_added : R.string.dialog_membership_add_error, Toast.LENGTH_SHORT).show();
                        if (success) {
                            loadMembershipTypes();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, getString(R.string.dialog_error_prefix, e.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }


        private void showEditDeleteDialog(int position) {
        if (position >= itemIds.size()) return;

        final long id = itemIds.get(position);
        String[] options;

        if (currentMode.equals("schedule")) {
            options = new String[]{getString(R.string.action_edit) + " " + getString(R.string.noun_schedule), getString(R.string.action_delete) + " " + getString(R.string.noun_schedule)};
        } else if (currentMode.equals("trainers")) {
            options = new String[]{getString(R.string.action_edit) + " " + getString(R.string.noun_trainer), getString(R.string.action_delete) + " " + getString(R.string.noun_trainer)};
        } else if (currentMode.equals("memberships")) {
            options = new String[]{getString(R.string.action_edit) + " " + getString(R.string.noun_membership), getString(R.string.action_delete) + " " + getString(R.string.noun_membership)};
        } else if (currentMode.equals("clients")) {
            options = new String[]{getString(R.string.action_view), getString(R.string.action_edit), getString(R.string.dialog_assign_trainer_title), getString(R.string.action_delete)};
        } else {
            options = new String[]{getString(R.string.action_view) + " " + getString(R.string.noun_info)};
        }

        AlertDialog.Builder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.dialog_choose_action)
                .setItems(options, (dialog, which) -> {
                    if (currentMode.equals("clients")) {
                        if (which == 0) {
                            showClientInfoDialog(id);
                        } else if (which == 1) {
                            showEditClientDialog(id);
                        } else if (which == 2) {
                            showAssignTrainerDialog(id);
                        } else if (which == 3) {
                            showDeleteClientConfirmDialog(id);
                        }
                    } else if (which == 0) {
                        if (currentMode.equals("schedule")) {
                            showEditScheduleDialog(id);
                        } else if (currentMode.equals("trainers")) {
                            showEditTrainerDialog(id);
                        } else if (currentMode.equals("memberships")) {
                            showEditMembershipDialog(id);
                        } else {
                            showClientInfoDialog(id);
                        }
                    } else if (which == 1 && (currentMode.equals("schedule") || currentMode.equals("trainers") || currentMode.equals("memberships"))) {
                        showDeleteConfirmDialog(id, currentMode);
                    }
                })
                .show();
    }


        private void showEditClientDialog(long clientId) {
        Cursor client = dbHelper.getAllClients();
        String username = "";
        String fullName = "";
        String phone = "";
        String email = "";

        while (client.moveToNext()) {
            if (client.getLong(client.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)) == clientId) {
                username = client.getString(client.getColumnIndexOrThrow(DatabaseHelper.COL_USERNAME));
                fullName = client.getString(client.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME));
                phone = client.getString(client.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE));
                email = client.getString(client.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL));
                break;
            }
        }
        client.close();

        View view = getLayoutInflater().inflate(R.layout.dialog_client_edit, null);
        EditText etUsername = view.findViewById(R.id.etUsername);
        EditText etPassword = view.findViewById(R.id.etPassword);
        EditText etFullName = view.findViewById(R.id.etFullName);
        EditText etPhone = view.findViewById(R.id.etPhone);
        EditText etEmail = view.findViewById(R.id.etEmail);

        etUsername.setText(username);
        etFullName.setText(fullName);
        etPhone.setText(phone);
        etEmail.setText(email);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_edit_client_title)
                .setView(view)
                .setPositiveButton(R.string.action_save, null)
                .setNegativeButton(R.string.action_cancel, (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String newUsername = etUsername.getText() == null ? "" : etUsername.getText().toString().trim();
                    String newPassword = etPassword.getText() == null ? "" : etPassword.getText().toString().trim();
                    String newFullName = etFullName.getText() == null ? "" : etFullName.getText().toString().trim();
                    String newPhone = etPhone.getText() == null ? "" : etPhone.getText().toString().trim();
                    String newEmail = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();

                    if (newUsername.isEmpty() || newFullName.isEmpty() || newPhone.isEmpty()) {
                        Toast.makeText(this, R.string.dialog_fill_client_register, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean success = dbHelper.updateClient(clientId, newUsername, newPassword, newFullName, newPhone, newEmail);
                    if (success) {
                        Toast.makeText(this, R.string.dialog_client_updated, Toast.LENGTH_SHORT).show();
                        loadClients();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, R.string.dialog_client_update_error, Toast.LENGTH_SHORT).show();
                    }
                }));

        dialog.show();
    }


        private void showAssignTrainerDialog(long clientId) {
        View view = getLayoutInflater().inflate(R.layout.dialog_client_assign_trainer, null);
        Spinner spinnerTrainer = view.findViewById(R.id.spinnerTrainer);

        Cursor trainers = dbHelper.getAllTrainers();
        ArrayList<String> trainerNames = new ArrayList<>();
        ArrayList<Long> trainerIds = new ArrayList<>();
        while (trainers.moveToNext()) {
            trainerNames.add(trainers.getString(trainers.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME)));
            trainerIds.add(trainers.getLong(trainers.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)));
        }
        trainers.close();

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, R.layout.item_dropdown_dark, trainerNames);
        spinnerAdapter.setDropDownViewResource(R.layout.item_dropdown_dark_dropdown);
        spinnerTrainer.setAdapter(spinnerAdapter);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_assign_trainer_title)
                .setView(view)
                .setPositiveButton(R.string.action_assign, null)
                .setNegativeButton(R.string.action_cancel, (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    int pos = spinnerTrainer.getSelectedItemPosition();
                    if (pos < 0 || pos >= trainerIds.size()) {
                        Toast.makeText(this, R.string.dialog_choose_trainer, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean success = dbHelper.assignTrainerToClient(clientId, trainerIds.get(pos), getString(R.string.dialog_trainer_assigned_by_admin));
                    if (success) {
                        Toast.makeText(this, R.string.dialog_trainer_assigned, Toast.LENGTH_SHORT).show();
                        loadClients();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, R.string.dialog_trainer_assign_error, Toast.LENGTH_SHORT).show();
                    }
                }));

        dialog.show();
    }


        private void showDeleteClientConfirmDialog(long clientId) {
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_client_title)
                .setMessage(R.string.dialog_delete_client_message)
                .setPositiveButton(R.string.action_delete, null)
                .setNegativeButton(R.string.action_cancel, (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    boolean success = dbHelper.deleteClient(clientId);
                    if (success) {
                        Toast.makeText(this, R.string.dialog_client_deleted, Toast.LENGTH_SHORT).show();
                        loadClients();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, R.string.dialog_client_delete_error, Toast.LENGTH_SHORT).show();
                    }
                }));

        dialog.show();
    }


        private void showEditTrainerDialog(final long trainerId) {
        Cursor trainer = dbHelper.getAllTrainers();
        String name = "";
        String phone = "";
        String email = "";
        String spec = "";
        int exp = 0;

        while (trainer.moveToNext()) {
            if (trainer.getLong(trainer.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)) == trainerId) {
                name = trainer.getString(trainer.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME));
                phone = trainer.getString(trainer.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE));
                email = trainer.getString(trainer.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL));
                spec = trainer.getString(trainer.getColumnIndexOrThrow(DatabaseHelper.COL_SPECIALIZATION));
                exp = trainer.getInt(trainer.getColumnIndexOrThrow(DatabaseHelper.COL_EXPERIENCE));
                break;
            }
        }
        trainer.close();

        View view = getLayoutInflater().inflate(R.layout.dialog_trainer_edit, null);
        EditText etName = view.findViewById(R.id.etName);
        EditText etPhone = view.findViewById(R.id.etPhone);
        EditText etEmail = view.findViewById(R.id.etEmail);
        com.google.android.material.textfield.MaterialAutoCompleteTextView etSpecialization = view.findViewById(R.id.etSpecialization);
        EditText etExperience = view.findViewById(R.id.etExperience);

        ArrayAdapter<String> specializationAdapter = new ArrayAdapter<>(this, R.layout.item_dropdown_dark, getResources().getStringArray(R.array.specialization_options));
        specializationAdapter.setDropDownViewResource(R.layout.item_dropdown_dark_dropdown);
        etSpecialization.setAdapter(specializationAdapter);

        etName.setText(name);
        etPhone.setText(phone);
        etEmail.setText(email);
        etSpecialization.setText(spec);
        etExperience.setText(String.valueOf(exp));

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_edit_trainer_title)
                .setView(view)
                .setPositiveButton(R.string.action_save, null)
                .setNegativeButton(R.string.action_cancel, (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    try {
                        boolean success = dbHelper.updateTrainer(
                                trainerId,
                                etName.getText().toString(),
                                etPhone.getText().toString(),
                                etEmail.getText().toString(),
                                etSpecialization.getText().toString(),
                                Integer.parseInt(etExperience.getText().toString())
                        );
                        Toast.makeText(this, success ? R.string.dialog_trainer_updated : R.string.dialog_trainer_update_error, Toast.LENGTH_SHORT).show();
                        loadTrainers();
                        dialog.dismiss();
                    } catch (Exception e) {
                        Toast.makeText(this, getString(R.string.dialog_error_prefix, e.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                }));
    }


    private void showEditScheduleDialog(final long scheduleId) {
        Cursor schedule = dbHelper.getScheduleById(scheduleId);
        String workoutType = "", date = "", time = "";
        int duration = 0, maxClients = 0;
        long trainerId = -1L;

        if (schedule != null && schedule.moveToFirst()) {
            workoutType = schedule.getString(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_TYPE));
            date = schedule.getString(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_DATE));
            time = schedule.getString(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_TIME));
            duration = schedule.getInt(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_DURATION));
            maxClients = schedule.getInt(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_MAX_CLIENTS));
            trainerId = schedule.getLong(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_SCHEDULE_TRAINER_ID));
            schedule.close();
        }

        View view = getLayoutInflater().inflate(R.layout.dialog_schedule_edit, null);
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .create();

        Spinner spinnerTrainer = view.findViewById(R.id.spinnerTrainer);
        EditText etWorkoutType = view.findViewById(R.id.etWorkoutType);
        EditText etDate = view.findViewById(R.id.etDate);
        com.google.android.material.textfield.MaterialAutoCompleteTextView etTime = view.findViewById(R.id.etTime);
        EditText etDuration = view.findViewById(R.id.etDuration);
        EditText etMaxClients = view.findViewById(R.id.etMaxClients);
        View btnSave = view.findViewById(R.id.btnSave);
        View btnCancel = view.findViewById(R.id.btnCancel);

        Cursor trainers = dbHelper.getAllTrainers();
        ArrayList<String> trainerNames = new ArrayList<>();
        ArrayList<Long> trainerIds = new ArrayList<>();
        int selectedIndex = 0;
        while (trainers.moveToNext()) {
            long currentTrainerId = trainers.getLong(trainers.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID));
            trainerIds.add(currentTrainerId);
            trainerNames.add(trainers.getString(trainers.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME)));
            if (trainerId == currentTrainerId) {
                selectedIndex = trainerIds.size() - 1;
            }
        }
        trainers.close();

        UiFormUtils.attachDarkSpinner(this, spinnerTrainer, trainerNames);
        spinnerTrainer.setSelection(Math.max(0, selectedIndex));
        UiFormUtils.attachDatePicker(this, etDate);
        UiFormUtils.attachQuarterHourTimePicker(this, etTime);

        etWorkoutType.setText(workoutType);
        etDate.setText(date);
        etTime.setText(time);
        etDuration.setText(String.valueOf(duration));
        etMaxClients.setText(String.valueOf(maxClients));

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            try {
                int pos = spinnerTrainer.getSelectedItemPosition();
                String selectedTime = etTime.getText() == null ? "" : etTime.getText().toString().trim();
                if (!isTimeInWorkRange(selectedTime)) {
                    Toast.makeText(this, "Выберите время с 08:00 до 22:00", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean success = dbHelper.updateSchedule(
                        scheduleId,
                        etWorkoutType.getText() == null ? "" : etWorkoutType.getText().toString().trim(),
                        etDate.getText() == null ? "" : etDate.getText().toString().trim(),
                        selectedTime,
                        Integer.parseInt(etDuration.getText() == null ? "0" : etDuration.getText().toString().trim()),
                        Integer.parseInt(etMaxClients.getText() == null ? "0" : etMaxClients.getText().toString().trim())
                );
                Toast.makeText(this, success ? "Тренировка обновлена" : "Не удалось сохранить изменения", Toast.LENGTH_SHORT).show();
                if (success) {
                    loadSchedule();
                    dialog.dismiss();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showEditMembershipDialog(final long typeId) {
        Cursor type = dbHelper.getAllMembershipTypes();
        String name = "", description = "";
        int durationDays = 0, price = 0, isActive = 1;

        if (type != null) {
            while (type.moveToNext()) {
                if (type.getLong(type.getColumnIndexOrThrow(DatabaseHelper.COL_MT_ID)) == typeId) {
                    name = type.getString(type.getColumnIndexOrThrow(DatabaseHelper.COL_MT_NAME));
                    description = type.getString(type.getColumnIndexOrThrow(DatabaseHelper.COL_MT_DESCRIPTION));
                    durationDays = type.getInt(type.getColumnIndexOrThrow(DatabaseHelper.COL_MT_DURATION_DAYS));
                    price = type.getInt(type.getColumnIndexOrThrow(DatabaseHelper.COL_MT_PRICE));
                    isActive = type.getInt(type.getColumnIndexOrThrow(DatabaseHelper.COL_MT_IS_ACTIVE));
                    break;
                }
            }
            type.close();
        }

        AlertDialog.Builder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_membership_edit, null);

        EditText etName = view.findViewById(R.id.etName);
        EditText etDescription = view.findViewById(R.id.etDescription);
        EditText etDurationDays = view.findViewById(R.id.etDurationDays);
        EditText etPrice = view.findViewById(R.id.etPrice);
        CheckBox cbIsActive = view.findViewById(R.id.cbIsActive);

        etName.setText(name);
        etDescription.setText(description);
        etDurationDays.setText(String.valueOf(durationDays));
        etPrice.setText(String.valueOf(price));
        cbIsActive.setChecked(isActive == 1);

        builder.setTitle("Редактировать абонемент")
                .setView(view)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    try {
                        boolean success = dbHelper.updateMembershipType(
                                typeId,
                                etName.getText().toString(),
                                etDescription.getText().toString(),
                                Integer.parseInt(etDurationDays.getText().toString()),
                                Integer.parseInt(etPrice.getText().toString()),
                                cbIsActive.isChecked()
                        );
                        Toast.makeText(this, success ? "Абонемент обновлен" : "Ошибка", Toast.LENGTH_SHORT).show();
                        loadMembershipTypes();
                    } catch (Exception e) {
                        Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

        private void showClientInfoDialog(long clientId) {
        Cursor client = dbHelper.getAllClients();
        String name = "";
        String phone = "";
        String email = "";
        String username = "";

        while (client.moveToNext()) {
            if (client.getLong(client.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)) == clientId) {
                name = client.getString(client.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME));
                phone = client.getString(client.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE));
                email = client.getString(client.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL));
                username = client.getString(client.getColumnIndexOrThrow(DatabaseHelper.COL_USERNAME));
                break;
            }
        }
        client.close();

        AlertDialog.Builder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.dialog_client_info_title)
                .setMessage(getString(R.string.dialog_client_info_message, name, phone, email, username))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }


        private void showDeleteConfirmDialog(final long id, final String mode) {
        String message = getString(R.string.dialog_delete_confirm_message);

        AlertDialog.Builder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.dialog_delete_confirm_title)
                .setMessage(message)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    boolean success;
                    if (mode.equals("schedule")) {
                        success = dbHelper.deleteSchedule(id);
                        Toast.makeText(this, success ? R.string.dialog_deleted_ok : R.string.dialog_delete_error, Toast.LENGTH_LONG).show();
                        if (success) loadSchedule();
                    } else if (mode.equals("trainers")) {
                        success = dbHelper.deleteTrainer(id);
                        Toast.makeText(this, success ? R.string.dialog_deleted_ok : R.string.dialog_delete_error, Toast.LENGTH_LONG).show();
                        if (success) loadTrainers();
                    } else if (mode.equals("memberships")) {
                        success = dbHelper.deleteMembershipType(id);
                        Toast.makeText(this, success ? R.string.dialog_deleted_ok : R.string.dialog_delete_error, Toast.LENGTH_LONG).show();
                        if (success) loadMembershipTypes();
                    } else {
                        success = false;
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
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

    private void logout() {
        AlertDialog.Builder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.dialog_logout_title)
                .setMessage(R.string.dialog_logout_message)
                .setPositiveButton(R.string.profile_logout_positive, (dialog, which) -> {
                    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    prefs.edit().clear().apply();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton(R.string.profile_logout_negative, null)
                .show();
    }

}

