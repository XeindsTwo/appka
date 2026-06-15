package com.example.fitbook;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Locale;

public class ClientManagementActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private final ArrayList<ClientItem> allItems = new ArrayList<>();
    private final ArrayList<ClientItem> visibleItems = new ArrayList<>();
    private final ArrayList<String> trainerFilterNames = new ArrayList<>();
    private final ArrayList<String> membershipFilterNames = new ArrayList<>();
    private ClientListAdapter adapter;
    private ArrayAdapter<String> trainerAdapter;
    private ArrayAdapter<String> membershipAdapter;
    private EditText etSearch;
    private Spinner spinnerTrainer;
    private Spinner spinnerMembership;
    private ListView listView;
    private TextView tvEmptyClients;
    private TextView tvTotalClients;
    private TextView tvClientsWithTrainer;
    private TextView tvActiveMembershipClients;

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
        if (!AuthGuard.requireRole(this, "admin")) return;
        setContentView(R.layout.activity_client_management);

        dbHelper = new DatabaseHelper(this);
        etSearch = findViewById(R.id.etSearch);
        spinnerTrainer = findViewById(R.id.spinnerTrainer);
        spinnerMembership = findViewById(R.id.spinnerMembership);
        listView = findViewById(R.id.listView);
        tvEmptyClients = findViewById(R.id.tvEmptyClients);
        tvTotalClients = findViewById(R.id.tvTotalClients);
        tvClientsWithTrainer = findViewById(R.id.tvClientsWithTrainer);
        tvActiveMembershipClients = findViewById(R.id.tvActiveMembershipClients);

        adapter = new ClientListAdapter();
        listView.setAdapter(adapter);

        findViewById(R.id.btnRegisterClient).setOnClickListener(v -> showRegisterClientDialog());

        trainerAdapter = new ArrayAdapter<>(this, R.layout.item_dropdown_dark, trainerFilterNames);
        trainerAdapter.setDropDownViewResource(R.layout.item_dropdown_dark_dropdown);
        spinnerTrainer.setAdapter(trainerAdapter);
        spinnerTrainer.setOnItemSelectedListener(new SimpleItemSelectedListener(this::renderItems));

        membershipAdapter = new ArrayAdapter<>(this, R.layout.item_dropdown_dark, membershipFilterNames);
        membershipAdapter.setDropDownViewResource(R.layout.item_dropdown_dark_dropdown);
        spinnerMembership.setAdapter(membershipAdapter);
        spinnerMembership.setOnItemSelectedListener(new SimpleItemSelectedListener(this::renderItems));

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
                allItems.add(item);
            }
            clients.close();
        }
        refreshFilters();
        updateSummary();
        renderItems();
    }

    private void updateSummary() {
        int totalClients = allItems.size();
        int withTrainer = 0;
        int activeMemberships = 0;
        for (ClientItem item : allItems) {
            if (item.trainerName != null && !item.trainerName.isEmpty() && !"Не назначен".equals(item.trainerName)) {
                withTrainer++;
            }
            if ("active".equals(item.membershipStatus)) {
                activeMemberships++;
            }
        }
        tvTotalClients.setText(String.valueOf(totalClients));
        tvClientsWithTrainer.setText(String.valueOf(withTrainer));
        tvActiveMembershipClients.setText(String.valueOf(activeMemberships));
    }

    private void refreshFilters() {
        if (trainerFilterNames.isEmpty()) {
            trainerFilterNames.add("Все тренеры");
        }
        trainerFilterNames.subList(1, trainerFilterNames.size()).clear();
        ArrayList<String> trainerNames = new ArrayList<>();
        for (ClientItem item : allItems) {
            if (item.trainerName != null && !item.trainerName.isEmpty() && !"Не назначен".equals(item.trainerName) && !trainerNames.contains(item.trainerName)) {
                trainerNames.add(item.trainerName);
            }
        }
        trainerNames.sort(String::compareToIgnoreCase);
        trainerFilterNames.addAll(trainerNames);
        trainerAdapter.notifyDataSetChanged();

        if (membershipFilterNames.isEmpty()) {
            membershipFilterNames.add("Все статусы");
            membershipFilterNames.add("Активный");
            membershipFilterNames.add("Истекший");
            membershipFilterNames.add("Без абонемента");
            membershipAdapter.notifyDataSetChanged();
        }
    }

    private void renderItems() {
        visibleItems.clear();
        String query = textOf(etSearch).toLowerCase(Locale.ROOT);
        String selectedTrainer = selectedOf(spinnerTrainer);
        String selectedMembership = selectedOf(spinnerMembership);

        for (ClientItem item : allItems) {
            String blob = (safe(item.username) + " " + safe(item.fullName) + " " + safe(item.phone) + " " + safe(item.email) + " " + safe(item.trainerName)).toLowerCase(Locale.ROOT);
            boolean searchMatches = query.isEmpty() || blob.contains(query);
            boolean trainerMatches = "Все тренеры".equals(selectedTrainer) || selectedTrainer.equals(item.trainerName);
            boolean membershipMatches = "Все статусы".equals(selectedMembership)
                    || ("Активный".equals(selectedMembership) && "active".equals(item.membershipStatus))
                    || ("Истекший".equals(selectedMembership) && "expired".equals(item.membershipStatus))
                    || ("Без абонемента".equals(selectedMembership) && ("none".equals(item.membershipStatus) || item.membershipStatus == null));

            if (searchMatches && trainerMatches && membershipMatches) {
                visibleItems.add(item);
            }
        }

        adapter.notifyDataSetChanged();
        boolean isEmpty = visibleItems.isEmpty();
        listView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        tvEmptyClients.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void showClientDetails(ClientItem item) {
        View view = getLayoutInflater().inflate(R.layout.dialog_client_details, null);
        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvUsername = view.findViewById(R.id.tvUsername);
        TextView tvPhone = view.findViewById(R.id.tvPhone);
        TextView tvEmail = view.findViewById(R.id.tvEmail);
        TextView tvTrainer = view.findViewById(R.id.tvTrainer);
        TextView tvMembership = view.findViewById(R.id.tvMembership);

        tvName.setText(item.fullName);
        tvUsername.setText("Логин: " + safe(item.username));
        tvPhone.setText("Телефон: " + safe(item.phone));
        tvEmail.setText("Email: " + safe(item.email));
        tvTrainer.setText("Тренер: " + safe(item.trainerName));
        tvMembership.setText("Абонемент: " + safe(item.membershipName) + " • " + statusLabel(item.membershipStatus));

        AlertDialog dialog = new MaterialAlertDialogBuilder(this).setView(view).create();
        view.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        expandDialog(dialog, 0.92f, 0.82f);
    }

    private void showEditClientDialog(ClientItem item) {
        View view = getLayoutInflater().inflate(R.layout.dialog_client_edit, null);
        TextInputEditText etUsername = view.findViewById(R.id.etUsername);
        TextInputEditText etPassword = view.findViewById(R.id.etPassword);
        TextInputEditText etFullName = view.findViewById(R.id.etFullName);
        TextInputEditText etPhone = view.findViewById(R.id.etPhone);
        TextInputEditText etEmail = view.findViewById(R.id.etEmail);
        Spinner spinnerTrainer = view.findViewById(R.id.spinnerTrainer);

        etUsername.setText(item.username);
        etFullName.setText(item.fullName);
        etPhone.setText(item.phone);
        etEmail.setText(item.email);

        ArrayList<String> trainerNames = new ArrayList<>();
        ArrayList<Long> trainerIds = new ArrayList<>();
        trainerNames.add("Не назначен");
        trainerIds.add(-1L);
        Cursor trainers = dbHelper.getAllTrainers();
        if (trainers != null) {
            while (trainers.moveToNext()) {
                trainerNames.add(trainers.getString(trainers.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME)));
                trainerIds.add(trainers.getLong(trainers.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)));
            }
            trainers.close();
        }
        ArrayAdapter<String> trainerAdapter = new ArrayAdapter<>(this, R.layout.item_dropdown_dark, trainerNames);
        trainerAdapter.setDropDownViewResource(R.layout.item_dropdown_dark_dropdown);
        spinnerTrainer.setAdapter(trainerAdapter);
        int selectedTrainerIndex = 0;
        if (item.trainerName != null) {
            int existingIndex = trainerNames.indexOf(item.trainerName);
            if (existingIndex >= 0) {
                selectedTrainerIndex = existingIndex;
            }
        }
        spinnerTrainer.setSelection(selectedTrainerIndex);

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
            if (success) {
                int trainerIndex = spinnerTrainer.getSelectedItemPosition();
                if (trainerIndex <= 0) {
                    dbHelper.clearClientTrainer(item.id);
                } else {
                    dbHelper.assignTrainerToClient(item.id, trainerIds.get(trainerIndex), "");
                }
            }
            Toast.makeText(this, success ? "Клиент обновлён" : "Не удалось обновить клиента", Toast.LENGTH_SHORT).show();
            if (success) {
                dialog.dismiss();
                loadItems();
            }
        }));
        dialog.show();
        expandDialog(dialog, 0.95f, 0.88f);
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_dropdown_dark, trainerNames);
        adapter.setDropDownViewResource(R.layout.item_dropdown_dark_dropdown);
        spinner.setAdapter(adapter);
        int selectedIndex = 0;
        if (item.trainerName != null) {
            int index = trainerNames.indexOf(item.trainerName);
            if (index >= 0) selectedIndex = index;
        }
        spinner.setSelection(selectedIndex);

        AlertDialog assignDialog = new MaterialAlertDialogBuilder(this)
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
                    loadItems();
                })
                .setNegativeButton("Отмена", null)
                .create();
        assignDialog.show();
        expandDialog(assignDialog, 0.88f, 0.5f);
    }

    private void confirmDeleteClient(ClientItem item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Удалить клиента?")
                .setMessage("Вы точно хотите удалить " + item.fullName + "?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    boolean success = dbHelper.deleteClient(item.id);
                    Toast.makeText(this, success ? "Клиент удалён" : "Не удалось удалить клиента", Toast.LENGTH_SHORT).show();
                    if (success) loadItems();
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
                loadItems();
                }
        }));
        dialog.show();
        expandDialog(dialog, 0.95f, 0.82f);
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

    private void expandDialog(AlertDialog dialog, float widthRatio, float heightRatio) {
        if (dialog.getWindow() == null) {
            return;
        }
        int width = (int) (getResources().getDisplayMetrics().widthPixels * widthRatio);
        int height = (int) (getResources().getDisplayMetrics().heightPixels * heightRatio);
        dialog.getWindow().setLayout(width, height);
    }

    private class ClientListAdapter extends BaseAdapter {
        @Override public int getCount() { return visibleItems.size(); }
        @Override public Object getItem(int position) { return visibleItems.get(position); }
        @Override public long getItemId(int position) { return visibleItems.get(position).id; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(ClientManagementActivity.this).inflate(R.layout.item_client_management, parent, false);
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
        private final TextView tvName;
        private final TextView tvUsername;
        private final TextView tvMeta;
        private final MaterialButton btnEdit;
        private final MaterialButton btnDelete;

        Holder(View root) {
            this.root = root;
            tvName = root.findViewById(R.id.tvName);
            tvUsername = root.findViewById(R.id.tvUsername);
            tvMeta = root.findViewById(R.id.tvMeta);
            btnEdit = root.findViewById(R.id.btnEdit);
            btnDelete = root.findViewById(R.id.btnDelete);
        }

        void bind(ClientItem item) {
            tvName.setText(item.fullName);
            tvUsername.setText("@" + safe(item.username));
            tvMeta.setText("Тренер: " + safe(item.trainerName) + "\nАбонемент: " + safe(item.membershipName) + " | " + statusLabel(item.membershipStatus));
            root.setOnClickListener(v -> showClientDetails(item));
            btnEdit.setOnClickListener(v -> showEditClientDialog(item));
            btnDelete.setOnClickListener(v -> confirmDeleteClient(item));
        }
    }

    private static class SimpleItemSelectedListener implements android.widget.AdapterView.OnItemSelectedListener {
        private final Runnable onSelected;

        SimpleItemSelectedListener(Runnable onSelected) {
            this.onSelected = onSelected;
        }

        @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) { onSelected.run(); }
        @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
    }
}
