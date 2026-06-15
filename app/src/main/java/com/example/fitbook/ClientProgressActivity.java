package com.example.fitbook;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ClientProgressActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private long clientId;
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AuthGuard.requireRole(this, "client")) return;
        setContentView(R.layout.activity_client_progress);
        dbHelper = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        clientId = prefs.getLong(DatabaseHelper.COL_USER_ID, 0);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        container = findViewById(R.id.container);
        render();
    }

    private void render() {
        container.removeAllViews();
        addLine("Проведено тренировок", String.valueOf(dbHelper.getCompletedWorkoutsCount(clientId)));
        Cursor measurements = dbHelper.getAllMeasurements(clientId);
        if (measurements != null && measurements.moveToFirst()) {
            do {
                addLine(DateFormatUtils.formatRussianDate(measurements.getString(measurements.getColumnIndexOrThrow(DatabaseHelper.COL_ANTHRO_DATE))),
                        "Вес " + measurements.getFloat(measurements.getColumnIndexOrThrow(DatabaseHelper.COL_ANTHRO_WEIGHT)) + " кг");
            } while (measurements.moveToNext());
            measurements.close();
        }
    }

    private void addLine(String title, String value) {
        TextView tv = new TextView(this);
        tv.setText(title + ": " + value);
        tv.setTextColor(getColor(R.color.fitbook_text_primary));
        tv.setTextSize(16f);
        tv.setPadding(0, 0, 0, 18);
        container.addView(tv);
    }
}
