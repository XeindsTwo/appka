package com.example.fitbook;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Locale;

public class MembershipManagementActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private final ArrayList<MembershipItem> allItems = new ArrayList<>();
    private final ArrayList<MembershipItem> visibleItems = new ArrayList<>();
    private MembershipListAdapter adapter;
    private EditText etSearch;
    private ListView listView;

    private static class MembershipItem {
        long id;
        String name;
        String description;
        int durationDays;
        int price;
        boolean active;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_membership_management);

        dbHelper = new DatabaseHelper(this);
        etSearch = findViewById(R.id.etSearch);
        listView = findViewById(R.id.listView);

        adapter = new MembershipListAdapter();
        listView.setAdapter(adapter);

        findViewById(R.id.btnAddMembership).setOnClickListener(v -> showAddMembershipDialog());

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
        loadItems();
    }

    private void loadItems() {
        allItems.clear();
        Cursor types = dbHelper.getAllMembershipTypes();
        if (types != null) {
            while (types.moveToNext()) {
                MembershipItem item = new MembershipItem();
                item.id = types.getLong(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_ID));
                item.name = types.getString(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_NAME));
                item.description = types.getString(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_DESCRIPTION));
                item.durationDays = types.getInt(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_DURATION_DAYS));
                item.price = types.getInt(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_PRICE));
                item.active = types.getInt(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_IS_ACTIVE)) == 1;
                allItems.add(item);
            }
            types.close();
        }
        renderItems();
    }

    private void renderItems() {
        visibleItems.clear();
        String query = etSearch.getText() == null ? "" : etSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
        for (MembershipItem item : allItems) {
            String blob = (safe(item.name) + " " + safe(item.description) + " " + item.durationDays + " " + item.price).toLowerCase(Locale.ROOT);
            if (query.isEmpty() || blob.contains(query)) {
                visibleItems.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showMembershipDetails(MembershipItem item) {
        View view = getLayoutInflater().inflate(R.layout.dialog_membership_details, null);
        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        TextView tvDuration = view.findViewById(R.id.tvDuration);
        TextView tvPrice = view.findViewById(R.id.tvPrice);
        TextView tvStatus = view.findViewById(R.id.tvStatus);

        tvName.setText(item.name);
        tvDescription.setText(safe(item.description));
        tvDuration.setText(item.durationDays + " дней");
        tvPrice.setText(item.price + " ₽");
        tvStatus.setText(item.active ? "Активен" : "Неактивен");

        AlertDialog dialog = new MaterialAlertDialogBuilder(this).setView(view).create();
        view.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showAddMembershipDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_membership_add, null);
        TextInputEditText etName = view.findViewById(R.id.etName);
        TextInputEditText etDescription = view.findViewById(R.id.etDescription);
        TextInputEditText etDurationDays = view.findViewById(R.id.etDurationDays);
        TextInputEditText etPrice = view.findViewById(R.id.etPrice);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Добавление абонемента")
                .setView(view)
                .setPositiveButton("Создать", null)
                .setNegativeButton("Отмена", (d, w) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            try {
                boolean success = dbHelper.createMembershipType(
                        textOf(etName),
                        textOf(etDescription),
                        Integer.parseInt(textOf(etDurationDays)),
                        Integer.parseInt(textOf(etPrice))
                );
                Toast.makeText(this, success ? "Абонемент создан" : "Не удалось создать абонемент", Toast.LENGTH_SHORT).show();
                if (success) {
                    dialog.dismiss();
                    loadItems();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Проверьте поля абонемента", Toast.LENGTH_SHORT).show();
            }
        }));
        dialog.show();
    }

    private void showEditMembershipDialog(MembershipItem item) {
        View view = getLayoutInflater().inflate(R.layout.dialog_membership_edit, null);
        TextInputEditText etName = view.findViewById(R.id.etName);
        TextInputEditText etDescription = view.findViewById(R.id.etDescription);
        TextInputEditText etDurationDays = view.findViewById(R.id.etDurationDays);
        TextInputEditText etPrice = view.findViewById(R.id.etPrice);

        etName.setText(item.name);
        etDescription.setText(item.description);
        etDurationDays.setText(String.valueOf(item.durationDays));
        etPrice.setText(String.valueOf(item.price));

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Редактирование абонемента")
                .setView(view)
                .setPositiveButton("Сохранить", null)
                .setNegativeButton("Отмена", (d, w) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            try {
                boolean success = dbHelper.updateMembershipType(
                        item.id,
                        textOf(etName),
                        textOf(etDescription),
                        Integer.parseInt(textOf(etDurationDays)),
                        Integer.parseInt(textOf(etPrice)),
                        item.active
                );
                Toast.makeText(this, success ? "Абонемент обновлён" : "Не удалось обновить абонемент", Toast.LENGTH_SHORT).show();
                if (success) {
                    dialog.dismiss();
                    loadItems();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Проверьте поля абонемента", Toast.LENGTH_SHORT).show();
            }
        }));
        dialog.show();
    }

    private void confirmDeleteMembership(MembershipItem item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Удалить абонемент?")
                .setMessage("Вы точно хотите удалить " + item.name + "?")
                .setPositiveButton("Удалить", (d, w) -> {
                    boolean success = dbHelper.deleteMembershipType(item.id);
                    Toast.makeText(this, success ? "Абонемент удалён" : "Нельзя удалить абонемент с историей покупок", Toast.LENGTH_LONG).show();
                    if (success) loadItems();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "—" : value;
    }

    private class MembershipListAdapter extends BaseAdapter {
        @Override public int getCount() { return visibleItems.size(); }
        @Override public Object getItem(int position) { return visibleItems.get(position); }
        @Override public long getItemId(int position) { return visibleItems.get(position).id; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MembershipManagementActivity.this).inflate(R.layout.item_membership_management, parent, false);
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
        private final TextView tvDescription;
        private final TextView tvMeta;
        private final MaterialButton btnEdit;
        private final MaterialButton btnDelete;

        Holder(View root) {
            this.root = root;
            tvName = root.findViewById(R.id.tvName);
            tvDescription = root.findViewById(R.id.tvDescription);
            tvMeta = root.findViewById(R.id.tvMeta);
            btnEdit = root.findViewById(R.id.btnEdit);
            btnDelete = root.findViewById(R.id.btnDelete);
        }

        void bind(MembershipItem item) {
            tvName.setText(item.name);
            tvDescription.setText(safe(item.description));
            tvMeta.setText(item.durationDays + " дней • " + item.price + " ₽");
            root.setOnClickListener(v -> showMembershipDetails(item));
            btnEdit.setOnClickListener(v -> showEditMembershipDialog(item));
            btnDelete.setOnClickListener(v -> confirmDeleteMembership(item));
        }
    }
}
