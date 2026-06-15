package com.example.fitbook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.Locale;

public class MembershipsActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private long clientId;

    private TextView tvMembershipType;
    private TextView tvMembershipStatus;
    private TextView tvMembershipEndDate;
    private TextView tvMembershipDaysLeft;
    private TextView tvMembershipPaymentMethod;
    private TextView tvMembershipGoal;
    private TextView tvMembershipTimeSlot;
    private TextView tvHistorySummary;
    private LinearLayout plansContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memberships);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        clientId = prefs.getLong(DatabaseHelper.COL_USER_ID, 0);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvMembershipType = findViewById(R.id.tvMembershipType);
        tvMembershipStatus = findViewById(R.id.tvMembershipStatus);
        tvMembershipEndDate = findViewById(R.id.tvMembershipEndDate);
        tvMembershipDaysLeft = findViewById(R.id.tvMembershipDaysLeft);
        tvMembershipPaymentMethod = findViewById(R.id.tvMembershipPaymentMethod);
        tvMembershipGoal = findViewById(R.id.tvMembershipGoal);
        tvMembershipTimeSlot = findViewById(R.id.tvMembershipTimeSlot);
        tvHistorySummary = findViewById(R.id.tvHistorySummary);
        plansContainer = findViewById(R.id.plansContainer);
        findViewById(R.id.btnOpenHistory).setOnClickListener(v -> startActivity(new Intent(this, MembershipHistoryActivity.class)));

        renderActiveMembership();
        renderPlans();
        renderHistorySummary();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderActiveMembership();
        renderHistorySummary();
    }

    private void renderActiveMembership() {
        Cursor activeMembership = dbHelper.getClientActiveMembership(clientId);
        if (activeMembership != null && activeMembership.moveToFirst()) {
            tvMembershipType.setText(getCursorString(activeMembership, DatabaseHelper.COL_MT_NAME, getString(R.string.client_membership_none)));
            tvMembershipStatus.setText(getMembershipStatusText(getCursorString(activeMembership, DatabaseHelper.COL_MEM_END_DATE, "—")));
            tvMembershipEndDate.setText(getCursorString(activeMembership, DatabaseHelper.COL_MEM_END_DATE, "—"));
            tvMembershipDaysLeft.setText(getDaysLeftText(getCursorString(activeMembership, DatabaseHelper.COL_MEM_END_DATE, "—")));
            activeMembership.close();
        } else {
            tvMembershipType.setText(getString(R.string.client_membership_none));
            tvMembershipStatus.setText(getString(R.string.client_membership_empty));
            tvMembershipEndDate.setText("—");
            tvMembershipDaysLeft.setText("—");
        }

        Cursor latestApplication = dbHelper.getLatestMembershipApplication(clientId);
        if (latestApplication != null && latestApplication.moveToFirst()) {
            tvMembershipPaymentMethod.setText(getCursorString(latestApplication, DatabaseHelper.COL_MA_PAYMENT_METHOD, "—"));
            tvMembershipGoal.setText(getCursorString(latestApplication, DatabaseHelper.COL_MA_GOAL, "—"));
            tvMembershipTimeSlot.setText(getCursorString(latestApplication, DatabaseHelper.COL_MA_TIME_SLOT, "—"));
            latestApplication.close();
        } else {
            tvMembershipPaymentMethod.setText("—");
            tvMembershipGoal.setText("—");
            tvMembershipTimeSlot.setText("—");
        }
    }

    private void renderPlans() {
        plansContainer.removeAllViews();
        Cursor types = dbHelper.getAllMembershipTypes();
        if (types == null || !types.moveToFirst()) {
            addEmptyState("Пока нет доступных планов");
            return;
        }

        long activeTypeId = -1;
        Cursor activeMembership = dbHelper.getClientActiveMembership(clientId);
        if (activeMembership != null && activeMembership.moveToFirst()) {
            activeTypeId = activeMembership.getLong(activeMembership.getColumnIndexOrThrow(DatabaseHelper.COL_MEM_TYPE_ID));
            activeMembership.close();
        }

        do {
            long typeId = types.getLong(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_ID));
            String name = types.getString(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_NAME));
            String description = types.getString(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_DESCRIPTION));
            int durationDays = types.getInt(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_DURATION_DAYS));
            int price = types.getInt(types.getColumnIndexOrThrow(DatabaseHelper.COL_MT_PRICE));

            View cardView = LayoutInflater.from(this).inflate(R.layout.item_membership_plan, plansContainer, false);
            TextView tvPlanName = cardView.findViewById(R.id.tvPlanName);
            TextView tvPlanPrice = cardView.findViewById(R.id.tvPlanPrice);
            TextView tvPlanDuration = cardView.findViewById(R.id.tvPlanDuration);
            TextView tvPlanDescription = cardView.findViewById(R.id.tvPlanDescription);
            MaterialButton btnSelectPlan = cardView.findViewById(R.id.btnSelectPlan);

            tvPlanName.setText(name);
            tvPlanPrice.setText(String.format(Locale.getDefault(), "%d ₽", price));
            tvPlanDuration.setText(durationDays + " дней");
            tvPlanDescription.setText(description);

            if (typeId == activeTypeId) {
                btnSelectPlan.setText("Текущий");
                btnSelectPlan.setEnabled(false);
                btnSelectPlan.setAlpha(0.7f);
            } else {
                btnSelectPlan.setText(getString(R.string.client_membership_choose));
                btnSelectPlan.setOnClickListener(v -> showPurchaseDialog(typeId, name, durationDays, price, description));
            }

            plansContainer.addView(cardView);
        } while (types.moveToNext());

        types.close();
    }

    private void addEmptyState(String message) {
        TextView emptyView = new TextView(this);
        emptyView.setText(message);
        emptyView.setTextColor(getColor(R.color.fitbook_text_secondary));
        emptyView.setTextSize(14f);
        emptyView.setPadding(8, 12, 8, 12);
        plansContainer.addView(emptyView);
    }

    private void renderHistorySummary() {
        Cursor latestApplication = dbHelper.getLatestMembershipApplication(clientId);
        if (latestApplication != null && latestApplication.moveToFirst()) {
            String planName = getCursorString(latestApplication, DatabaseHelper.COL_MT_NAME, "—");
            String paymentMethod = getCursorString(latestApplication, DatabaseHelper.COL_MA_PAYMENT_METHOD, "—");
            String goal = getCursorString(latestApplication, DatabaseHelper.COL_MA_GOAL, "—");
            String timeSlot = getCursorString(latestApplication, DatabaseHelper.COL_MA_TIME_SLOT, "—");
            String createdAt = getCursorString(latestApplication, DatabaseHelper.COL_MA_CREATED_AT, "—");
            tvHistorySummary.setText(planName + " • " + paymentMethod + " • " + goal + " • " + timeSlot + "\n" + createdAt);
            latestApplication.close();
        } else {
            tvHistorySummary.setText(getString(R.string.client_membership_empty));
        }
    }

    private void showPurchaseDialog(long typeId, String planName, int durationDays, int price, String description) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_membership_purchase, null, false);

        MaterialAutoCompleteTextView actPaymentMethod = view.findViewById(R.id.actPaymentMethod);
        MaterialAutoCompleteTextView actGoal = view.findViewById(R.id.actGoal);
        MaterialAutoCompleteTextView actTimeSlot = view.findViewById(R.id.actTimeSlot);
        EditText etNote = view.findViewById(R.id.etNote);
        TextView tvPlanInfo = view.findViewById(R.id.tvPlanInfo);

        tvPlanInfo.setText(planName + " • " + durationDays + " дней • " + price + " ₽\n" + description);

        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                new String[]{"Карта", "Наличные", "Перевод", "СБП"});
        ArrayAdapter<String> goalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                new String[]{"Похудение", "Набор мышц", "Поддержание формы", "Мобильность"});
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                new String[]{"Утро", "День", "Вечер", "Любое время"});

        actPaymentMethod.setAdapter(paymentAdapter);
        actGoal.setAdapter(goalAdapter);
        actTimeSlot.setAdapter(timeAdapter);
        actPaymentMethod.setText(paymentAdapter.getItem(0), false);
        actGoal.setText(goalAdapter.getItem(0), false);
        actTimeSlot.setText(timeAdapter.getItem(0), false);

        new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setPositiveButton(R.string.client_membership_confirm, (dialog, which) -> {
                    String paymentMethod = TextUtils.isEmpty(actPaymentMethod.getText()) ? "Карта" : actPaymentMethod.getText().toString();
                    String goal = TextUtils.isEmpty(actGoal.getText()) ? "Похудение" : actGoal.getText().toString();
                    String timeSlot = TextUtils.isEmpty(actTimeSlot.getText()) ? "Любое время" : actTimeSlot.getText().toString();
                    String note = etNote.getText() == null ? "" : etNote.getText().toString().trim();

                    boolean activated = dbHelper.purchaseMembership(clientId, typeId);
                    boolean saved = dbHelper.saveMembershipApplication(clientId, typeId, paymentMethod, goal, timeSlot, note);
                    if (activated || saved) {
                        Toast.makeText(this, R.string.client_membership_purchase_ok, Toast.LENGTH_SHORT).show();
                        renderActiveMembership();
                        renderPlans();
                        renderHistorySummary();
                    } else {
                        Toast.makeText(this, R.string.client_membership_purchase_error, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.client_membership_cancel, null)
                .show();
    }

    private String getCursorString(Cursor cursor, String columnName, String fallback) {
        int columnIndex = cursor.getColumnIndexOrThrow(columnName);
        String value = cursor.isNull(columnIndex) ? null : cursor.getString(columnIndex);
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String getMembershipStatusText(String endDate) {
        long daysLeft = getDaysLeft(endDate);
        if (daysLeft > 1) {
            return "Активен";
        } else if (daysLeft == 1) {
            return "Истекает завтра";
        } else if (daysLeft == 0) {
            return "Истекает сегодня";
        }
        return "Истёк";
    }

    private String getDaysLeftText(String endDate) {
        long daysLeft = getDaysLeft(endDate);
        if (daysLeft > 1) {
            return daysLeft + " дней";
        } else if (daysLeft == 1) {
            return "1 день";
        } else if (daysLeft == 0) {
            return "Сегодня";
        }
        return "0 дней";
    }

    private long getDaysLeft(String endDate) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            java.util.Date end = sdf.parse(endDate);
            if (end == null) {
                return -1;
            }
            long diffMillis = end.getTime() - new java.util.Date().getTime();
            return java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffMillis);
        } catch (Exception e) {
            return -1;
        }
    }
}
