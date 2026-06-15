package com.example.fitbook;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MembershipHistoryActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private long clientId;
    private LinearLayout historyContainer;
    private TextView tvEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AuthGuard.requireRole(this, "client")) return;
        setContentView(R.layout.activity_membership_history);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        clientId = prefs.getLong(DatabaseHelper.COL_USER_ID, 0);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        historyContainer = findViewById(R.id.historyContainer);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        renderHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!AuthGuard.requireRole(this, "client")) return;
        renderHistory();
    }

    private void renderHistory() {
        historyContainer.removeAllViews();
        Cursor history = dbHelper.getClientMembershipHistory(clientId);
        if (history == null || !history.moveToFirst()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            return;
        }

        tvEmptyState.setVisibility(View.GONE);
        do {
            View item = LayoutInflater.from(this).inflate(R.layout.item_membership_history, historyContainer, false);
            TextView tvHistoryPlan = item.findViewById(R.id.tvHistoryPlan);
            TextView tvHistoryStatus = item.findViewById(R.id.tvHistoryStatus);
            TextView tvHistoryPurchaseDate = item.findViewById(R.id.tvHistoryPurchaseDate);
            TextView tvHistoryStartDate = item.findViewById(R.id.tvHistoryStartDate);
            TextView tvHistoryEndDate = item.findViewById(R.id.tvHistoryEndDate);

            String planName = getCursorString(history, DatabaseHelper.COL_MT_NAME, getString(R.string.client_membership_none));
            String status = getMembershipStatusText(getCursorString(history, DatabaseHelper.COL_MEM_END_DATE, "—"));
            String startDate = getCursorString(history, DatabaseHelper.COL_MEM_START_DATE, "—");
            String endDate = getCursorString(history, DatabaseHelper.COL_MEM_END_DATE, "—");
            String purchaseDate = DateFormatUtils.formatRussianDate(getCursorString(history, DatabaseHelper.COL_MEM_PURCHASE_DATE, "—"));
            String startDatePretty = DateFormatUtils.formatRussianDate(startDate);
            String endDatePretty = DateFormatUtils.formatRussianDate(endDate);

            tvHistoryPlan.setText(planName);
            tvHistoryStatus.setText(status);
            tvHistoryPurchaseDate.setText(purchaseDate);
            tvHistoryStartDate.setText(startDatePretty);
            tvHistoryEndDate.setText(endDatePretty);

            historyContainer.addView(item);
        } while (history.moveToNext());

        history.close();
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

    private long getDaysLeft(String endDate) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date end = sdf.parse(endDate);
            if (end == null) return -1;
            long diffMillis = end.getTime() - new java.util.Date().getTime();
            return java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffMillis);
        } catch (Exception e) {
            return -1;
        }
    }
}
