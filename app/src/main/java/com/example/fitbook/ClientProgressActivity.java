package com.example.fitbook;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class ClientProgressActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private long clientId;
    private LinearLayout container;
    private TextView tvEmpty;
    private TextView tvSummary;

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
        tvEmpty = findViewById(R.id.tvEmpty);
        tvSummary = findViewById(R.id.tvSummary);
        render();
    }

    private void render() {
        container.removeAllViews();

        int completedWorkouts = dbHelper.getCompletedWorkoutsCount(clientId);
        Cursor measurements = dbHelper.getAllMeasurements(clientId);
        int measurementCount = 0;
        float latestWeight = 0f;
        float latestHeight = 0f;

        if (measurements != null && measurements.moveToFirst()) {
            measurementCount = measurements.getCount();
            latestWeight = measurements.getFloat(measurements.getColumnIndexOrThrow(DatabaseHelper.COL_ANTHRO_WEIGHT));
            latestHeight = measurements.getFloat(measurements.getColumnIndexOrThrow(DatabaseHelper.COL_ANTHRO_HEIGHT));
            do {
                addMeasurementCard(
                        DateFormatUtils.formatRussianDate(measurements.getString(measurements.getColumnIndexOrThrow(DatabaseHelper.COL_ANTHRO_DATE))),
                        measurements.getFloat(measurements.getColumnIndexOrThrow(DatabaseHelper.COL_ANTHRO_WEIGHT)),
                        measurements.getFloat(measurements.getColumnIndexOrThrow(DatabaseHelper.COL_ANTHRO_HEIGHT)),
                        measurements.getFloat(measurements.getColumnIndexOrThrow(DatabaseHelper.COL_ANTHRO_BICEPS)),
                        measurements.getFloat(measurements.getColumnIndexOrThrow(DatabaseHelper.COL_ANTHRO_CHEST)),
                        measurements.getFloat(measurements.getColumnIndexOrThrow(DatabaseHelper.COL_ANTHRO_WAIST))
                );
            } while (measurements.moveToNext());
            measurements.close();
        } else if (measurements != null) {
            measurements.close();
        }

        tvSummary.setText("Тренировок завершено: " + completedWorkouts + "\nЗамеров тела: " + measurementCount);
        tvEmpty.setVisibility(measurementCount == 0 ? View.VISIBLE : View.GONE);
        tvEmpty.setText("Замеров пока нет");

        MaterialCardView overview = new MaterialCardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 12);
        overview.setLayoutParams(params);
        overview.setCardBackgroundColor(getColor(R.color.fitbook_surface_light));
        overview.setStrokeColor(getColor(R.color.fitbook_stroke));
        overview.setStrokeWidth(1);
        overview.setRadius(22f);
        overview.setCardElevation(0f);

        LinearLayout overviewContent = new LinearLayout(this);
        overviewContent.setOrientation(LinearLayout.VERTICAL);
        overviewContent.setPadding(18, 18, 18, 18);

        TextView overviewTitle = new TextView(this);
        overviewTitle.setText("Общий прогресс");
        overviewTitle.setTextColor(getColor(R.color.fitbook_text_primary));
        overviewTitle.setTextSize(18f);
        overviewTitle.setTypeface(overviewTitle.getTypeface(), android.graphics.Typeface.BOLD);

        TextView overviewMeta = new TextView(this);
        overviewMeta.setText("Последний вес: " + formatFloat(latestWeight) + " кг\nРост: " + formatFloat(latestHeight) + " см");
        overviewMeta.setTextColor(getColor(R.color.fitbook_text_secondary));
        overviewMeta.setTextSize(13f);
        overviewMeta.setPadding(0, 8, 0, 0);

        overviewContent.addView(overviewTitle);
        overviewContent.addView(overviewMeta);
        overview.addView(overviewContent);
        container.addView(overview);
    }

    private void addMeasurementCard(String date, float weight, float height, float biceps, float chest, float waist) {
        View card = getLayoutInflater().inflate(R.layout.item_client_measurement, container, false);
        ((TextView) card.findViewById(R.id.tvDate)).setText(date);
        ((TextView) card.findViewById(R.id.tvSummary)).setText("Вес " + formatFloat(weight) + " кг");
        ((TextView) card.findViewById(R.id.tvHint)).setText("Контрольный замер от " + date);
        ((TextView) card.findViewById(R.id.tvWeight)).setText("Вес\n" + formatFloat(weight) + " кг");
        ((TextView) card.findViewById(R.id.tvHeight)).setText("Рост\n" + formatFloat(height) + " см");
        ((TextView) card.findViewById(R.id.tvChest)).setText("Грудь\n" + formatFloat(chest) + " см");
        ((TextView) card.findViewById(R.id.tvWaist)).setText("Талия\n" + formatFloat(waist) + " см");
        ((TextView) card.findViewById(R.id.tvBiceps)).setText("Бицепс\n" + formatFloat(biceps) + " см");
        container.addView(card);
    }

    private String formatFloat(float value) {
        if (value <= 0f) {
            return "—";
        }
        if (Math.abs(value - Math.round(value)) < 0.01f) {
            return String.valueOf(Math.round(value));
        }
        return String.format(java.util.Locale.getDefault(), "%.1f", value);
    }
}
