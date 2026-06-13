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
        setContentView(R.layout.activity_admin);

        dbHelper = new DatabaseHelper(this);
        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);

        String adminName = prefs.getString(DatabaseHelper.COL_FULL_NAME, "Р В РЎвЂ™Р В РўвЂР В РЎВР В РЎвЂР В Р вЂ¦Р В РЎвЂР РЋР С“Р РЋРІР‚С™Р РЋР вЂљР В Р’В°Р РЋРІР‚С™Р В РЎвЂўР РЋР вЂљ");

        tvAdminName = findViewById(R.id.tvAdminName);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        btnAddMembership = findViewById(R.id.btnAddMembership);
        listView = findViewById(R.id.listView);
        tvAdminName.setText("Администрирование клубом");
        tvSectionTitle.setText("Управление клубом");

        dataList = new ArrayList<>();
        itemIds = new ArrayList<>();
        allClientItems = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);

        clientFilterContainer = findViewById(R.id.clientFilterContainer);
        etClientSearch = findViewById(R.id.etClientSearch);
        spinnerClientTrainerFilter = findViewById(R.id.spinnerClientTrainerFilter);
        spinnerClientMembershipFilter = findViewById(R.id.spinnerClientMembershipFilter);
        spinnerClientSort = findViewById(R.id.spinnerClientSort);

        // Р В Р’В Р РЋРІвЂћСћР В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚СћР В Р’В Р РЋРІР‚вЂќР В Р’В Р РЋРІР‚СњР В Р’В Р РЋРІР‚В
        findViewById(R.id.btnAddTrainer).setOnClickListener(v -> startActivity(new Intent(this, AddTrainerActivity.class)));
        findViewById(R.id.btnAddSchedule).setOnClickListener(v -> startActivity(new Intent(this, AddScheduleActivity.class)));
        findViewById(R.id.btnViewClients).setOnClickListener(v -> {
            startActivity(new Intent(this, ClientManagementActivity.class));
        });
        findViewById(R.id.btnViewTrainers).setOnClickListener(v -> {
            startActivity(new Intent(this, TrainerManagementActivity.class));
        });
        findViewById(R.id.btnViewSchedule).setOnClickListener(v -> {
            currentMode = "schedule";
            btnAddMembership.setVisibility(View.GONE);
            loadSchedule();
        });
        findViewById(R.id.btnMembershipTypes).setOnClickListener(v -> startActivity(new Intent(this, MembershipManagementActivity.class)));
        findViewById(R.id.btnRegisterClient).setOnClickListener(v -> showRegisterClientDialog());
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
                tvSectionTitle.setText("Управление клубом");
                currentMode = "schedule";
                btnAddMembership.setVisibility(View.GONE);
                loadSchedule();
                return true;
            } else if (itemId == R.id.nav_clients) {
                startActivity(new Intent(this, ClientManagementActivity.class));
                return true;
            } else if (itemId == R.id.nav_trainers) {
                tvSectionTitle.setText("Управление клубом");
                currentMode = "trainers";
                btnAddMembership.setVisibility(View.GONE);
                loadTrainers();
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
            clientTrainerFilterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, clientTrainerFilterNames);
            clientTrainerFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
            clientMembershipFilterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, clientMembershipFilterNames);
            clientMembershipFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
            clientSortFilterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, clientSortFilterNames);
            clientSortFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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

        ArrayAdapter<String> specializationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.specialization_options));
        etSpecialization.setAdapter(specializationAdapter);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Р вЂќР С•Р В±Р В°Р Р†Р С‘РЎвЂљРЎРЉ РЎвЂљРЎР‚Р ВµР Р…Р ВµРЎР‚Р В°")
                .setView(view)
                .setPositiveButton("Р вЂќР С•Р В±Р В°Р Р†Р С‘РЎвЂљРЎРЉ", null)
                .setNegativeButton("Р С›РЎвЂљР СР ВµР Р…Р В°", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String name = etName.getText() == null ? "" : etName.getText().toString().trim();
                    String phone = etPhone.getText() == null ? "" : etPhone.getText().toString().trim();
                    String email = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();
                    String specialization = etSpecialization.getText() == null ? "" : etSpecialization.getText().toString().trim();
                    String experienceText = etExperience.getText() == null ? "" : etExperience.getText().toString().trim();

                    if (name.isEmpty() || phone.isEmpty() || specialization.isEmpty() || experienceText.isEmpty()) {
                        Toast.makeText(this, "Р вЂ”Р В°Р С—Р С•Р В»Р Р…Р С‘РЎвЂљР Вµ Р С‘Р СРЎРЏ, РЎвЂљР ВµР В»Р ВµРЎвЂћР С•Р Р…, РЎРѓР С—Р ВµРЎвЂ Р С‘Р В°Р В»Р С‘Р В·Р В°РЎвЂ Р С‘РЎР‹ Р С‘ Р С•Р С—РЎвЂ№РЎвЂљ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        boolean success = dbHelper.addTrainer(
                                name,
                                phone,
                                email,
                                specialization,
                                Integer.parseInt(experienceText)
                        );
                        if (success) {
                            Toast.makeText(this, "Р СћРЎР‚Р ВµР Р…Р ВµРЎР‚ Р Т‘Р С•Р В±Р В°Р Р†Р В»Р ВµР Р…", Toast.LENGTH_SHORT).show();
                            currentMode = "trainers";
                            loadTrainers();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(this, "Р СњР Вµ РЎС“Р Т‘Р В°Р В»Р С•РЎРѓРЎРЉ Р Т‘Р С•Р В±Р В°Р Р†Р С‘РЎвЂљРЎРЉ РЎвЂљРЎР‚Р ВµР Р…Р ВµРЎР‚Р В°", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Р С›Р С—РЎвЂ№РЎвЂљ Р Т‘Р С•Р В»Р В¶Р ВµР Р… Р В±РЎвЂ№РЎвЂљРЎРЉ РЎвЂЎР С‘РЎРѓР В»Р С•Р С", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Р С›РЎв‚¬Р С‘Р В±Р С”Р В°: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }));

        dialog.show();
    }

    private void showAddScheduleDialog() {
        AlertDialog.Builder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_schedule_add, null);

        Spinner spinnerTrainer = view.findViewById(R.id.spinnerTrainer);
        EditText etWorkoutType = view.findViewById(R.id.etWorkoutType);
        EditText etDate = view.findViewById(R.id.etDate);
        EditText etTime = view.findViewById(R.id.etTime);
        EditText etDuration = view.findViewById(R.id.etDuration);
        EditText etMaxClients = view.findViewById(R.id.etMaxClients);

        Cursor trainers = dbHelper.getAllTrainers();
        ArrayList<String> trainerNames = new ArrayList<>();
        ArrayList<Long> trainerIds = new ArrayList<>();
        while (trainers.moveToNext()) {
            trainerNames.add(trainers.getString(trainers.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME)));
            trainerIds.add(trainers.getLong(trainers.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)));
        }
        trainers.close();

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, trainerNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTrainer.setAdapter(spinnerAdapter);

        builder.setTitle("Р В Р’В Р Р†Р вЂљРЎСљР В Р’В Р РЋРІР‚СћР В Р’В Р вЂ™Р’В±Р В Р’В Р вЂ™Р’В°Р В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚ВР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В Р вЂ° Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚ВР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚СњР В Р Р‹Р РЋРІР‚Сљ").setView(view)
                .setPositiveButton("Р В Р’В Р Р†Р вЂљРЎСљР В Р’В Р РЋРІР‚СћР В Р’В Р вЂ™Р’В±Р В Р’В Р вЂ™Р’В°Р В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚ВР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В Р вЂ°", (dialog, which) -> {
                    try {
                        int pos = spinnerTrainer.getSelectedItemPosition();
                        boolean success = dbHelper.addSchedule(
                                trainerIds.get(pos),
                                etWorkoutType.getText().toString(),
                                etDate.getText().toString(),
                                etTime.getText().toString(),
                                Integer.parseInt(etDuration.getText().toString()),
                                Integer.parseInt(etMaxClients.getText().toString())
                        );
                        Toast.makeText(this, success ? "Р В Р’В Р РЋРЎвЂєР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚ВР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’В° Р В Р’В Р СћРІР‚ВР В Р’В Р РЋРІР‚СћР В Р’В Р вЂ™Р’В±Р В Р’В Р вЂ™Р’В°Р В Р’В Р В РІР‚В Р В Р’В Р вЂ™Р’В»Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В°" : "Р В Р’В Р РЋРІР‚С”Р В Р Р‹Р Р†РІР‚С™Р’В¬Р В Р’В Р РЋРІР‚ВР В Р’В Р вЂ™Р’В±Р В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’В°", Toast.LENGTH_SHORT).show();
                        if (success) {
                            currentMode = "schedule";
                            loadSchedule();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Р В Р’В Р РЋРІР‚С”Р В Р Р‹Р Р†РІР‚С™Р’В¬Р В Р’В Р РЋРІР‚ВР В Р’В Р вЂ™Р’В±Р В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’В°: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Р В Р’В Р РЋРІР‚С”Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋР’ВР В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В°", null).show();
    }

    private void showRegisterClientDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_client_register, null);

        EditText etUsername = view.findViewById(R.id.etUsername);
        EditText etPassword = view.findViewById(R.id.etPassword);
        EditText etFullName = view.findViewById(R.id.etFullName);
        EditText etPhone = view.findViewById(R.id.etPhone);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Р В Р ВµР С–Р С‘РЎРѓРЎвЂљРЎР‚Р В°РЎвЂ Р С‘РЎРЏ Р В°Р С”Р С”Р В°РЎС“Р Р…РЎвЂљР В°")
                .setView(view)
                .setPositiveButton("Р вЂ”Р В°РЎР‚Р ВµР С–Р С‘РЎРѓРЎвЂљРЎР‚Р С‘РЎР‚Р С•Р Р†Р В°РЎвЂљРЎРЉ", null)
                .setNegativeButton("Р С›РЎвЂљР СР ВµР Р…Р В°", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String username = etUsername.getText() == null ? "" : etUsername.getText().toString().trim();
                    String password = etPassword.getText() == null ? "" : etPassword.getText().toString().trim();
                    String fullName = etFullName.getText() == null ? "" : etFullName.getText().toString().trim();
                    String phone = etPhone.getText() == null ? "" : etPhone.getText().toString().trim();

                    if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || phone.isEmpty()) {
                        Toast.makeText(this, "Р вЂ”Р В°Р С—Р С•Р В»Р Р…Р С‘РЎвЂљР Вµ Р Р†РЎРѓР Вµ Р С—Р С•Р В»РЎРЏ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        boolean success = dbHelper.registerClient(
                                username,
                                password,
                                fullName,
                                phone,
                                ""
                        );
                        if (success) {
                            Toast.makeText(this, "Р С™Р В»Р С‘Р ВµР Р…РЎвЂљ Р В·Р В°РЎР‚Р ВµР С–Р С‘РЎРѓРЎвЂљРЎР‚Р С‘РЎР‚Р С•Р Р†Р В°Р Р…", Toast.LENGTH_LONG).show();
                            currentMode = "clients";
                            loadClients();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(this, "Р СњР Вµ РЎС“Р Т‘Р В°Р В»Р С•РЎРѓРЎРЉ Р В·Р В°РЎР‚Р ВµР С–Р С‘РЎРѓРЎвЂљРЎР‚Р С‘РЎР‚Р С•Р Р†Р В°РЎвЂљРЎРЉ Р С”Р В»Р С‘Р ВµР Р…РЎвЂљР В°", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Р С›РЎв‚¬Р С‘Р В±Р С”Р В°: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            dataList.add("Р РЋР вЂљР РЋРЎСџР Р†Р вЂљР’ВР В Р С“Р В Р вЂ Р В РІР‚С™Р В Р Р‰Р РЋР вЂљР РЋРЎСџР В Р РЏР вЂ™Р’В« " + name + "\nР РЋР вЂљР РЋРЎСџР Р†Р вЂљРЎС™Р РЋРІР‚С” " + phone + "\nР РЋР вЂљР РЋРЎСџР В РІР‚в„–Р В РІР‚РЋ " + spec + "\nР РЋР вЂљР РЋРЎСџР Р†Р вЂљРЎС™Р Р†Р вЂљР’В¦ Р В Р’В Р РЋРІР‚С”Р В Р’В Р РЋРІР‚вЂќР В Р Р‹Р Р†Р вЂљРІвЂћвЂ“Р В Р Р‹Р Р†Р вЂљРЎв„ў: " + exp + " Р В Р’В Р вЂ™Р’В»Р В Р’В Р вЂ™Р’ВµР В Р Р‹Р Р†Р вЂљРЎв„ў");
        }
        trainers.close();
        adapter.notifyDataSetChanged();

        if (dataList.isEmpty()) {
            dataList.add("Р В Р’В Р РЋРЎС™Р В Р’В Р вЂ™Р’ВµР В Р Р‹Р Р†Р вЂљРЎв„ў Р В Р’В Р вЂ™Р’В·Р В Р’В Р вЂ™Р’В°Р В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’ВµР В Р’В Р РЋРІР‚вЂњР В Р’В Р РЋРІР‚ВР В Р Р‹Р В РЎвЂњР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚ВР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р вЂ™Р’В°Р В Р’В Р В РІР‚В¦Р В Р’В Р В РІР‚В¦Р В Р Р‹Р Р†Р вЂљРІвЂћвЂ“Р В Р Р‹Р Р†Р вЂљР’В¦ Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В ");
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
            dataList.add("Р РЋР вЂљР РЋРЎСџР В Р РЏР Р†Р вЂљРІвЂћвЂ“Р В РЎвЂ”Р РЋРІР‚ВР В Р РЏ " + type + "\nР РЋР вЂљР РЋРЎСџР Р†Р вЂљРЎС™Р Р†Р вЂљР’В¦ " + date + " " + time + "\nР РЋР вЂљР РЋРЎСџР Р†Р вЂљР’ВР В Р С“Р В Р вЂ Р В РІР‚С™Р В Р Р‰Р РЋР вЂљР РЋРЎСџР В Р РЏР вЂ™Р’В« " + trainer + "\nР РЋР вЂљР РЋРЎСџР Р†Р вЂљР’ВР СћРЎвЂ™ Р В Р’В Р Р†Р вЂљРІР‚СњР В Р’В Р вЂ™Р’В°Р В Р’В Р РЋРІР‚вЂќР В Р’В Р РЋРІР‚ВР В Р Р‹Р В РЎвЂњР В Р’В Р вЂ™Р’В°Р В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚Сћ: " + current + "/" + max);
        }
        schedule.close();
        adapter.notifyDataSetChanged();

        if (dataList.isEmpty()) {
            dataList.add("Р В Р’В Р РЋРЎС™Р В Р’В Р вЂ™Р’ВµР В Р Р‹Р Р†Р вЂљРЎв„ў Р В Р’В Р вЂ™Р’В·Р В Р’В Р вЂ™Р’В°Р В Р’В Р РЋРІР‚вЂќР В Р’В Р вЂ™Р’В»Р В Р’В Р вЂ™Р’В°Р В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚ВР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р вЂ™Р’В°Р В Р’В Р В РІР‚В¦Р В Р’В Р В РІР‚В¦Р В Р Р‹Р Р†Р вЂљРІвЂћвЂ“Р В Р Р‹Р Р†Р вЂљР’В¦ Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚ВР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚СћР В Р’В Р РЋРІР‚Сњ");
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
                String status = isActive == 1 ? "Р В Р вЂ Р РЋРЎв„ўР Р†Р вЂљР’В¦ Р В Р’В Р РЋРІР‚в„ўР В Р’В Р РЋРІР‚СњР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚ВР В Р’В Р В РІР‚В Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦" : "Р В Р вЂ Р РЋРЎС™Р В Р вЂ° Р В Р’В Р РЋРЎС™Р В Р’В Р вЂ™Р’ВµР В Р’В Р вЂ™Р’В°Р В Р’В Р РЋРІР‚СњР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚ВР В Р’В Р В РІР‚В Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦";
                dataList.add("Р РЋР вЂљР РЋРЎСџР В Р РЏР вЂ™Р’В·Р В РЎвЂ”Р РЋРІР‚ВР В Р РЏ " + name + " (" + status + ")" +
                        "\nР РЋР вЂљР РЋРЎСџР Р†Р вЂљРЎС™Р РЋРЎС™ " + description +
                        "\nР РЋР вЂљР РЋРЎСџР Р†Р вЂљРЎС™Р Р†Р вЂљР’В¦ " + days + " Р В Р’В Р СћРІР‚ВР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’ВµР В Р’В Р Р†РІР‚С›РІР‚вЂњ" +
                        "\nР РЋР вЂљР РЋРЎСџР Р†Р вЂљРІвЂћСћР вЂ™Р’В° " + price + " Р В Р вЂ Р Р†Р вЂљРЎв„ўР В РІР‚В¦");
            } while (types.moveToNext());
            types.close();
        } else {
            dataList.add("Р В Р’В Р РЋРЎС™Р В Р’В Р вЂ™Р’ВµР В Р Р‹Р Р†Р вЂљРЎв„ў Р В Р Р‹Р В РЎвЂњР В Р’В Р РЋРІР‚СћР В Р’В Р вЂ™Р’В·Р В Р’В Р СћРІР‚ВР В Р’В Р вЂ™Р’В°Р В Р’В Р В РІР‚В¦Р В Р’В Р В РІР‚В¦Р В Р Р‹Р Р†Р вЂљРІвЂћвЂ“Р В Р Р‹Р Р†Р вЂљР’В¦ Р В Р’В Р вЂ™Р’В°Р В Р’В Р вЂ™Р’В±Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’ВµР В Р’В Р РЋР’ВР В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В ");
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

        builder.setTitle("Р В Р вЂ Р РЋРІР‚С”Р Р†Р вЂљРЎС› Р В Р’В Р Р†Р вЂљРЎСљР В Р’В Р РЋРІР‚СћР В Р’В Р вЂ™Р’В±Р В Р’В Р вЂ™Р’В°Р В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚ВР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В Р вЂ° Р В Р’В Р вЂ™Р’В°Р В Р’В Р вЂ™Р’В±Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’ВµР В Р’В Р РЋР’ВР В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р Р‹Р Р†Р вЂљРЎв„ў")
                .setView(view)
                .setPositiveButton("Р В Р’В Р Р†Р вЂљРЎСљР В Р’В Р РЋРІР‚СћР В Р’В Р вЂ™Р’В±Р В Р’В Р вЂ™Р’В°Р В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚ВР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В Р вЂ°", (dialog, which) -> {
                    try {
                        boolean success = dbHelper.createMembershipType(
                                etName.getText().toString(),
                                etDescription.getText().toString(),
                                Integer.parseInt(etDurationDays.getText().toString()),
                                Integer.parseInt(etPrice.getText().toString())
                        );
                        Toast.makeText(this, success ? "Р В Р’В Р РЋРІР‚в„ўР В Р’В Р вЂ™Р’В±Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’ВµР В Р’В Р РЋР’ВР В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р Р‹Р Р†Р вЂљРЎв„ў Р В Р’В Р СћРІР‚ВР В Р’В Р РЋРІР‚СћР В Р’В Р вЂ™Р’В±Р В Р’В Р вЂ™Р’В°Р В Р’В Р В РІР‚В Р В Р’В Р вЂ™Р’В»Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦" : "Р В Р’В Р РЋРІР‚С”Р В Р Р‹Р Р†РІР‚С™Р’В¬Р В Р’В Р РЋРІР‚ВР В Р’В Р вЂ™Р’В±Р В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’В°", Toast.LENGTH_SHORT).show();
                        if (success) {
                            loadMembershipTypes();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Р В Р’В Р РЋРІР‚С”Р В Р Р‹Р Р†РІР‚С™Р’В¬Р В Р’В Р РЋРІР‚ВР В Р’В Р вЂ™Р’В±Р В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’В°: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Р В Р’В Р РЋРІР‚С”Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋР’ВР В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В°", null)
                .show();
    }

    private void showEditDeleteDialog(int position) {
        if (position >= itemIds.size()) return;

        final long id = itemIds.get(position);
        String[] options;

        if (currentMode.equals("schedule")) {
            options = new String[]{"Редактировать тренировку", "Удалить тренировку"};
        } else if (currentMode.equals("trainers")) {
            options = new String[]{"Редактировать тренера", "Удалить тренера"};
        } else if (currentMode.equals("memberships")) {
            options = new String[]{"Редактировать абонемент", "Удалить абонемент"};
        } else if (currentMode.equals("clients")) {
            options = new String[]{"Просмотр", "Редактировать", "Назначить тренера", "Удалить"};
        } else {
            options = new String[]{"Просмотр информации"};
        }

        AlertDialog.Builder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        builder.setTitle("Выберите действие")
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
                .setTitle("Редактирование клиента")
                .setView(view)
                .setPositiveButton("Сохранить", null)
                .setNegativeButton("Отмена", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String newUsername = etUsername.getText() == null ? "" : etUsername.getText().toString().trim();
                    String newPassword = etPassword.getText() == null ? "" : etPassword.getText().toString().trim();
                    String newFullName = etFullName.getText() == null ? "" : etFullName.getText().toString().trim();
                    String newPhone = etPhone.getText() == null ? "" : etPhone.getText().toString().trim();
                    String newEmail = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();

                    if (newUsername.isEmpty() || newFullName.isEmpty() || newPhone.isEmpty()) {
                        Toast.makeText(this, "Заполните логин, ФИО и телефон", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean success = dbHelper.updateClient(clientId, newUsername, newPassword, newFullName, newPhone, newEmail);
                    if (success) {
                        Toast.makeText(this, "Клиент обновлен", Toast.LENGTH_SHORT).show();
                        loadClients();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Не удалось обновить клиента", Toast.LENGTH_SHORT).show();
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

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, trainerNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTrainer.setAdapter(spinnerAdapter);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Назначить тренера")
                .setView(view)
                .setPositiveButton("Назначить", null)
                .setNegativeButton("Отмена", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    int pos = spinnerTrainer.getSelectedItemPosition();
                    if (pos < 0 || pos >= trainerIds.size()) {
                        Toast.makeText(this, "Выберите тренера", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean success = dbHelper.assignTrainerToClient(clientId, trainerIds.get(pos), "Назначено администратором");
                    if (success) {
                        Toast.makeText(this, "Тренер назначен", Toast.LENGTH_SHORT).show();
                        loadClients();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Не удалось назначить тренера", Toast.LENGTH_SHORT).show();
                    }
                }));

        dialog.show();
    }

    private void showDeleteClientConfirmDialog(long clientId) {
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Удалить клиента")
                .setMessage("Удалить клиента вместе с его записями и данными?")
                .setPositiveButton("Удалить", null)
                .setNegativeButton("Отмена", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    boolean success = dbHelper.deleteClient(clientId);
                    if (success) {
                        Toast.makeText(this, "Клиент удален", Toast.LENGTH_SHORT).show();
                        loadClients();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Не удалось удалить клиента", Toast.LENGTH_SHORT).show();
                    }
                }));

        dialog.show();
    }

    private void showEditTrainerDialog(final long trainerId) {
        Cursor trainer = dbHelper.getAllTrainers();
        String name = "", phone = "", email = "", spec = "";
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

        AlertDialog.Builder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_trainer_edit, null);

        EditText etName = view.findViewById(R.id.etName);
        EditText etPhone = view.findViewById(R.id.etPhone);
        EditText etEmail = view.findViewById(R.id.etEmail);
        com.google.android.material.textfield.MaterialAutoCompleteTextView etSpecialization = view.findViewById(R.id.etSpecialization);
        EditText etExperience = view.findViewById(R.id.etExperience);

        ArrayAdapter<String> specializationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.specialization_options));
        etSpecialization.setAdapter(specializationAdapter);

        etName.setText(name);
        etPhone.setText(phone);
        etEmail.setText(email);
        etSpecialization.setText(spec);
        etExperience.setText(String.valueOf(exp));

        builder.setTitle("Р В Р вЂ Р РЋРЎв„ўР В Р РЏР В РЎвЂ”Р РЋРІР‚ВР В Р РЏ Р В Р’В Р вЂ™Р’В Р В Р’В Р вЂ™Р’ВµР В Р’В Р СћРІР‚ВР В Р’В Р вЂ™Р’В°Р В Р’В Р РЋРІР‚СњР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚ВР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р вЂ™Р’В°Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В Р вЂ° Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’В°").setView(view)
                .setPositiveButton("Р В Р’В Р В Р вЂ№Р В Р’В Р РЋРІР‚СћР В Р Р‹Р Р†Р вЂљР’В¦Р В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’В°Р В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚ВР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В Р вЂ°", (dialog, which) -> {
                    try {
                        boolean success = dbHelper.updateTrainer(
                                trainerId,
                                etName.getText().toString(),
                                etPhone.getText().toString(),
                                etEmail.getText().toString(),
                                etSpecialization.getText().toString(),
                                Integer.parseInt(etExperience.getText().toString())
                        );
                        Toast.makeText(this, success ? "Р В Р’В Р РЋРЎвЂєР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РІР‚С™ Р В Р’В Р РЋРІР‚СћР В Р’В Р вЂ™Р’В±Р В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р вЂ™Р’В»Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦" : "Р В Р’В Р РЋРІР‚С”Р В Р Р‹Р Р†РІР‚С™Р’В¬Р В Р’В Р РЋРІР‚ВР В Р’В Р вЂ™Р’В±Р В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’В°", Toast.LENGTH_SHORT).show();
                        loadTrainers();
                    } catch (Exception e) {
                        Toast.makeText(this, "Р В Р’В Р РЋРІР‚С”Р В Р Р‹Р Р†РІР‚С™Р’В¬Р В Р’В Р РЋРІР‚ВР В Р’В Р вЂ™Р’В±Р В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’В°: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Р В Р’В Р РЋРІР‚С”Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋР’ВР В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В°", null).show();
    }

    private void showEditScheduleDialog(final long scheduleId) {
        Cursor schedule = dbHelper.getScheduleById(scheduleId);
        String workoutType = "", date = "", time = "";
        int duration = 0, maxClients = 0;

        if (schedule != null && schedule.moveToFirst()) {
            workoutType = schedule.getString(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_TYPE));
            date = schedule.getString(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_DATE));
            time = schedule.getString(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_TIME));
            duration = schedule.getInt(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_DURATION));
            maxClients = schedule.getInt(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_MAX_CLIENTS));
            schedule.close();
        }

        AlertDialog.Builder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_schedule_edit, null);

        EditText etWorkoutType = view.findViewById(R.id.etWorkoutType);
        EditText etDate = view.findViewById(R.id.etDate);
        EditText etTime = view.findViewById(R.id.etTime);
        EditText etDuration = view.findViewById(R.id.etDuration);
        EditText etMaxClients = view.findViewById(R.id.etMaxClients);

        etWorkoutType.setText(workoutType);
        etDate.setText(date);
        etTime.setText(time);
        etDuration.setText(String.valueOf(duration));
        etMaxClients.setText(String.valueOf(maxClients));

        builder.setTitle("Р В Р вЂ Р РЋРЎв„ўР В Р РЏР В РЎвЂ”Р РЋРІР‚ВР В Р РЏ Р В Р’В Р вЂ™Р’В Р В Р’В Р вЂ™Р’ВµР В Р’В Р СћРІР‚ВР В Р’В Р вЂ™Р’В°Р В Р’В Р РЋРІР‚СњР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚ВР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р вЂ™Р’В°Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В Р вЂ° Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚ВР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚СњР В Р Р‹Р РЋРІР‚Сљ").setView(view)
                .setPositiveButton("Р В Р’В Р В Р вЂ№Р В Р’В Р РЋРІР‚СћР В Р Р‹Р Р†Р вЂљР’В¦Р В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’В°Р В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚ВР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В Р вЂ°", (dialog, which) -> {
                    try {
                        boolean success = dbHelper.updateSchedule(
                                scheduleId,
                                etWorkoutType.getText().toString(),
                                etDate.getText().toString(),
                                etTime.getText().toString(),
                                Integer.parseInt(etDuration.getText().toString()),
                                Integer.parseInt(etMaxClients.getText().toString())
                        );
                        Toast.makeText(this, success ? "Р В Р’В Р РЋРЎвЂєР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚ВР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’В° Р В Р’В Р РЋРІР‚СћР В Р’В Р вЂ™Р’В±Р В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р вЂ™Р’В»Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В°" : "Р В Р’В Р РЋРІР‚С”Р В Р Р‹Р Р†РІР‚С™Р’В¬Р В Р’В Р РЋРІР‚ВР В Р’В Р вЂ™Р’В±Р В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’В°", Toast.LENGTH_SHORT).show();
                        loadSchedule();
                    } catch (Exception e) {
                        Toast.makeText(this, "Р В Р’В Р РЋРІР‚С”Р В Р Р‹Р Р†РІР‚С™Р’В¬Р В Р’В Р РЋРІР‚ВР В Р’В Р вЂ™Р’В±Р В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’В°: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Р В Р’В Р РЋРІР‚С”Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋР’ВР В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В°", null).show();
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

        builder.setTitle("Р В Р вЂ Р РЋРЎв„ўР В Р РЏР В РЎвЂ”Р РЋРІР‚ВР В Р РЏ Р В Р’В Р вЂ™Р’В Р В Р’В Р вЂ™Р’ВµР В Р’В Р СћРІР‚ВР В Р’В Р вЂ™Р’В°Р В Р’В Р РЋРІР‚СњР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚ВР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р вЂ™Р’В°Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В Р вЂ° Р В Р’В Р вЂ™Р’В°Р В Р’В Р вЂ™Р’В±Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’ВµР В Р’В Р РЋР’ВР В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р Р‹Р Р†Р вЂљРЎв„ў")
                .setView(view)
                .setPositiveButton("Р В Р’В Р В Р вЂ№Р В Р’В Р РЋРІР‚СћР В Р Р‹Р Р†Р вЂљР’В¦Р В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’В°Р В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚ВР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В Р вЂ°", (dialog, which) -> {
                    try {
                        boolean success = dbHelper.updateMembershipType(
                                typeId,
                                etName.getText().toString(),
                                etDescription.getText().toString(),
                                Integer.parseInt(etDurationDays.getText().toString()),
                                Integer.parseInt(etPrice.getText().toString()),
                                cbIsActive.isChecked()
                        );
                        Toast.makeText(this, success ? "Р В Р’В Р РЋРІР‚в„ўР В Р’В Р вЂ™Р’В±Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’ВµР В Р’В Р РЋР’ВР В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р Р‹Р Р†Р вЂљРЎв„ў Р В Р’В Р РЋРІР‚СћР В Р’В Р вЂ™Р’В±Р В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р вЂ™Р’В»Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦" : "Р В Р’В Р РЋРІР‚С”Р В Р Р‹Р Р†РІР‚С™Р’В¬Р В Р’В Р РЋРІР‚ВР В Р’В Р вЂ™Р’В±Р В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’В°", Toast.LENGTH_SHORT).show();
                        loadMembershipTypes();
                    } catch (Exception e) {
                        Toast.makeText(this, "Р В Р’В Р РЋРІР‚С”Р В Р Р‹Р Р†РІР‚С™Р’В¬Р В Р’В Р РЋРІР‚ВР В Р’В Р вЂ™Р’В±Р В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’В°: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Р В Р’В Р РЋРІР‚С”Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋР’ВР В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В°", null)
                .show();
    }

    private void showClientInfoDialog(long clientId) {
        Cursor client = dbHelper.getAllClients();
        String name = "", phone = "", email = "", username = "";

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
        builder.setTitle("Р РЋР вЂљР РЋРЎСџР Р†Р вЂљРЎС™Р Р†Р вЂљРІвЂћвЂ“ Р В Р’В Р вЂ™Р’ВР В Р’В Р В РІР‚В¦Р В Р Р‹Р Р†Р вЂљРЎвЂєР В Р’В Р РЋРІР‚СћР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋР’ВР В Р’В Р вЂ™Р’В°Р В Р Р‹Р Р†Р вЂљР’В Р В Р’В Р РЋРІР‚ВР В Р Р‹Р В Р РЏ Р В Р’В Р РЋРІР‚Сћ Р В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’В»Р В Р’В Р РЋРІР‚ВР В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р вЂ™Р’Вµ")
                .setMessage("Р РЋР вЂљР РЋРЎСџР Р†Р вЂљР’ВР вЂ™Р’В¤ Р В Р’В Р вЂ™Р’ВР В Р’В Р РЋР’ВР В Р Р‹Р В Р РЏ: " + name +
                        "\nР РЋР вЂљР РЋРЎСџР Р†Р вЂљРЎС™Р РЋРІР‚С” Р В Р’В Р РЋРЎвЂєР В Р’В Р вЂ™Р’ВµР В Р’В Р вЂ™Р’В»Р В Р’В Р вЂ™Р’ВµР В Р Р‹Р Р†Р вЂљРЎвЂєР В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В¦: " + phone +
                        "\nР РЋР вЂљР РЋРЎСџР Р†Р вЂљРЎС™Р вЂ™Р’В§ Email: " + email +
                        "\nР РЋР вЂљР РЋРЎСџР Р†Р вЂљРЎСљР Р†Р вЂљР’В Р В Р’В Р Р†Р вЂљРЎвЂќР В Р’В Р РЋРІР‚СћР В Р’В Р РЋРІР‚вЂњР В Р’В Р РЋРІР‚ВР В Р’В Р В РІР‚В¦: " + username)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showDeleteConfirmDialog(final long id, final String mode) {
        String message = "";
        if (mode.equals("schedule")) {
            message = "Р В Р’В Р В РІвЂљВ¬Р В Р’В Р СћРІР‚ВР В Р’В Р вЂ™Р’В°Р В Р’В Р вЂ™Р’В»Р В Р’В Р РЋРІР‚ВР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В Р вЂ° Р В Р Р‹Р В Р Р‰Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р РЋРІР‚Сљ Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚ВР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚СњР В Р Р‹Р РЋРІР‚Сљ?";
        } else if (mode.equals("trainers")) {
            message = "Р В Р’В Р В РІвЂљВ¬Р В Р’В Р СћРІР‚ВР В Р’В Р вЂ™Р’В°Р В Р’В Р вЂ™Р’В»Р В Р’В Р РЋРІР‚ВР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В Р вЂ° Р В Р Р‹Р В Р Р‰Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚СћР В Р’В Р РЋРІР‚вЂњР В Р’В Р РЋРІР‚Сћ Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’В°?";
        } else if (mode.equals("memberships")) {
            message = "Р В Р’В Р В РІвЂљВ¬Р В Р’В Р СћРІР‚ВР В Р’В Р вЂ™Р’В°Р В Р’В Р вЂ™Р’В»Р В Р’В Р РЋРІР‚ВР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В Р вЂ° Р В Р Р‹Р В Р Р‰Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚СћР В Р Р‹Р Р†Р вЂљРЎв„ў Р В Р’В Р вЂ™Р’В°Р В Р’В Р вЂ™Р’В±Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’ВµР В Р’В Р РЋР’ВР В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р Р‹Р Р†Р вЂљРЎв„ў?";
        }

        AlertDialog.Builder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        builder.setTitle("Р В Р’В Р РЋРЎСџР В Р’В Р РЋРІР‚СћР В Р’В Р СћРІР‚ВР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р В РІР‚В Р В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’В¶Р В Р’В Р СћРІР‚ВР В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚ВР В Р’В Р вЂ™Р’Вµ Р В Р Р‹Р РЋРІР‚СљР В Р’В Р СћРІР‚ВР В Р’В Р вЂ™Р’В°Р В Р’В Р вЂ™Р’В»Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚ВР В Р Р‹Р В Р РЏ")
                .setMessage(message)
                .setPositiveButton("Р В Р’В Р В РІвЂљВ¬Р В Р’В Р СћРІР‚ВР В Р’В Р вЂ™Р’В°Р В Р’В Р вЂ™Р’В»Р В Р’В Р РЋРІР‚ВР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В Р вЂ°", (dialog, which) -> {
                    boolean success;
                    if (mode.equals("schedule")) {
                        success = dbHelper.deleteSchedule(id);
                        Toast.makeText(this, success ? "Р В Р’В Р РЋРЎвЂєР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚ВР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’В° Р В Р Р‹Р РЋРІР‚СљР В Р’В Р СћРІР‚ВР В Р’В Р вЂ™Р’В°Р В Р’В Р вЂ™Р’В»Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В°" : "Р В Р’В Р РЋРЎС™Р В Р’В Р вЂ™Р’ВµР В Р’В Р вЂ™Р’В»Р В Р Р‹Р В Р вЂ°Р В Р’В Р вЂ™Р’В·Р В Р Р‹Р В Р РЏ Р В Р Р‹Р РЋРІР‚СљР В Р’В Р СћРІР‚ВР В Р’В Р вЂ™Р’В°Р В Р’В Р вЂ™Р’В»Р В Р’В Р РЋРІР‚ВР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В Р вЂ°: Р В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РЎвЂњР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В Р вЂ° Р В Р’В Р вЂ™Р’В·Р В Р’В Р вЂ™Р’В°Р В Р’В Р РЋРІР‚вЂќР В Р’В Р РЋРІР‚ВР В Р Р‹Р В РЎвЂњР В Р’В Р РЋРІР‚В Р В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’В»Р В Р’В Р РЋРІР‚ВР В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В ", Toast.LENGTH_LONG).show();
                        if (success) loadSchedule();
                    } else if (mode.equals("trainers")) {
                        success = dbHelper.deleteTrainer(id);
                        Toast.makeText(this, success ? "Р В Р’В Р РЋРЎвЂєР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РІР‚С™ Р В Р Р‹Р РЋРІР‚СљР В Р’В Р СћРІР‚ВР В Р’В Р вЂ™Р’В°Р В Р’В Р вЂ™Р’В»Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦" : "Р В Р’В Р РЋРЎС™Р В Р’В Р вЂ™Р’ВµР В Р’В Р вЂ™Р’В»Р В Р Р‹Р В Р вЂ°Р В Р’В Р вЂ™Р’В·Р В Р Р‹Р В Р РЏ Р В Р Р‹Р РЋРІР‚СљР В Р’В Р СћРІР‚ВР В Р’В Р вЂ™Р’В°Р В Р’В Р вЂ™Р’В»Р В Р’В Р РЋРІР‚ВР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В Р вЂ°: Р В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РЎвЂњР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В Р вЂ° Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р РЋРІР‚ВР В Р Р‹Р В РІР‚С™Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В Р В Р’В Р РЋРІР‚СњР В Р’В Р РЋРІР‚В", Toast.LENGTH_LONG).show();
                        if (success) loadTrainers();
                    } else if (mode.equals("memberships")) {
                        success = dbHelper.deleteMembershipType(id);
                        Toast.makeText(this, success ? "Р В Р’В Р РЋРІР‚в„ўР В Р’В Р вЂ™Р’В±Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’ВµР В Р’В Р РЋР’ВР В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р Р‹Р Р†Р вЂљРЎв„ў Р В Р Р‹Р РЋРІР‚СљР В Р’В Р СћРІР‚ВР В Р’В Р вЂ™Р’В°Р В Р’В Р вЂ™Р’В»Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦" : "Р В Р’В Р РЋРЎС™Р В Р’В Р вЂ™Р’ВµР В Р’В Р вЂ™Р’В»Р В Р Р‹Р В Р вЂ°Р В Р’В Р вЂ™Р’В·Р В Р Р‹Р В Р РЏ Р В Р Р‹Р РЋРІР‚СљР В Р’В Р СћРІР‚ВР В Р’В Р вЂ™Р’В°Р В Р’В Р вЂ™Р’В»Р В Р’В Р РЋРІР‚ВР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В Р вЂ°: Р В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РЎвЂњР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р В Р вЂ° Р В Р’В Р РЋРІР‚СњР В Р’В Р вЂ™Р’В»Р В Р’В Р РЋРІР‚ВР В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р Р‹Р Р†Р вЂљРІвЂћвЂ“ Р В Р Р‹Р В РЎвЂњ Р В Р Р‹Р В Р Р‰Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚ВР В Р’В Р РЋР’В Р В Р’В Р вЂ™Р’В°Р В Р’В Р вЂ™Р’В±Р В Р’В Р РЋРІР‚СћР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’ВµР В Р’В Р РЋР’ВР В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚СћР В Р’В Р РЋР’В", Toast.LENGTH_LONG).show();
                        if (success) loadMembershipTypes();
                    } else {
                        success = false;
                    }
                })
                .setNegativeButton("Р В Р’В Р РЋРІР‚С”Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋР’ВР В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В°", null)
                .show();
    }

    private void logout() {
        AlertDialog.Builder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        builder.setTitle("Р В Р’В Р Р†Р вЂљРІвЂћСћР В Р Р‹Р Р†Р вЂљРІвЂћвЂ“Р В Р Р‹Р Р†Р вЂљР’В¦Р В Р’В Р РЋРІР‚СћР В Р’В Р СћРІР‚В")
                .setMessage("Р В Р’В Р Р†Р вЂљРІвЂћСћР В Р Р‹Р Р†Р вЂљРІвЂћвЂ“ Р В Р Р‹Р РЋРІР‚СљР В Р’В Р В РІР‚В Р В Р’В Р вЂ™Р’ВµР В Р Р‹Р В РІР‚С™Р В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р Р‹Р Р†Р вЂљРІвЂћвЂ“, Р В Р Р‹Р Р†Р вЂљР Р‹Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚Сћ Р В Р Р‹Р Р†Р вЂљР’В¦Р В Р’В Р РЋРІР‚СћР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚ВР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р вЂ™Р’Вµ Р В Р’В Р В РІР‚В Р В Р Р‹Р Р†Р вЂљРІвЂћвЂ“Р В Р’В Р Р†РІР‚С›РІР‚вЂњР В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋРІР‚В?")
                .setPositiveButton("Р В Р’В Р Р†Р вЂљРЎСљР В Р’В Р вЂ™Р’В°", (dialog, which) -> {
                    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    prefs.edit().clear().apply();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("Р В Р’В Р РЋРІР‚С”Р В Р Р‹Р Р†Р вЂљРЎв„ўР В Р’В Р РЋР’ВР В Р’В Р вЂ™Р’ВµР В Р’В Р В РІР‚В¦Р В Р’В Р вЂ™Р’В°", null)
                .show();
    }
}

