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

public class ClientPlanActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private long clientId;
    private TextView tvEmpty;
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AuthGuard.requireRole(this, "client")) return;
        setContentView(R.layout.activity_client_plan);
        dbHelper = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        clientId = prefs.getLong(DatabaseHelper.COL_USER_ID, 0);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        tvEmpty = findViewById(R.id.tvEmpty);
        container = findViewById(R.id.container);
        render();
    }

    private void render() {
        container.removeAllViews();
        Cursor plan = dbHelper.getClientWorkoutPlan(clientId);
        if (plan == null || !plan.moveToFirst()) {
            tvEmpty.setVisibility(View.VISIBLE);
            if (plan != null) {
                plan.close();
            }
            return;
        }
        tvEmpty.setVisibility(View.GONE);

        MaterialCardView summaryCard = new MaterialCardView(this);
        LinearLayout.LayoutParams summaryParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        summaryParams.setMargins(0, 0, 0, 14);
        summaryCard.setLayoutParams(summaryParams);
        summaryCard.setCardBackgroundColor(getColor(R.color.fitbook_surface_light));
        summaryCard.setStrokeColor(getColor(R.color.fitbook_stroke));
        summaryCard.setStrokeWidth(1);
        summaryCard.setRadius(22f);
        summaryCard.setCardElevation(0f);

        LinearLayout summaryContent = new LinearLayout(this);
        summaryContent.setOrientation(LinearLayout.VERTICAL);
        summaryContent.setPadding(18, 18, 18, 18);

        TextView summaryTitle = new TextView(this);
        summaryTitle.setText("Текущий план");
        summaryTitle.setTextColor(getColor(R.color.fitbook_text_primary));
        summaryTitle.setTextSize(18f);
        summaryTitle.setTypeface(summaryTitle.getTypeface(), android.graphics.Typeface.BOLD);

        TextView summaryMeta = new TextView(this);
        summaryMeta.setText("Тренер: " + safe(plan.getString(plan.getColumnIndexOrThrow("trainer_name"))) + "\nНазначен: " +
                DateFormatUtils.formatRussianDate(plan.getString(plan.getColumnIndexOrThrow(DatabaseHelper.COL_PLAN_ASSIGNED_DATE))) +
                "\nЗаметки: " + safe(plan.getString(plan.getColumnIndexOrThrow(DatabaseHelper.COL_PLAN_NOTES))));
        summaryMeta.setTextColor(getColor(R.color.fitbook_text_secondary));
        summaryMeta.setTextSize(13f);
        summaryMeta.setPadding(0, 10, 0, 0);

        summaryContent.addView(summaryTitle);
        summaryContent.addView(summaryMeta);
        summaryCard.addView(summaryContent);
        container.addView(summaryCard);

        TextView header = new TextView(this);
        header.setText("Упражнения");
        header.setTextColor(getColor(R.color.fitbook_text_primary));
        header.setTextSize(20f);
        header.setTypeface(header.getTypeface(), android.graphics.Typeface.BOLD);
        header.setPadding(0, 10, 0, 14);
        container.addView(header);

        long planId = plan.getLong(plan.getColumnIndexOrThrow(DatabaseHelper.COL_PLAN_ID));
        Cursor exercises = dbHelper.getPlanExercises(planId);
        boolean hasExercises = false;
        if (exercises != null && exercises.moveToFirst()) {
            hasExercises = true;
            do {
                addExerciseCard(
                        safe(exercises.getString(exercises.getColumnIndexOrThrow(DatabaseHelper.COL_PE_EXERCISE_NAME))),
                        exercises.getInt(exercises.getColumnIndexOrThrow(DatabaseHelper.COL_PE_SETS)),
                        exercises.getInt(exercises.getColumnIndexOrThrow(DatabaseHelper.COL_PE_REPS)),
                        exercises.getFloat(exercises.getColumnIndexOrThrow(DatabaseHelper.COL_PE_WEIGHT))
                );
            } while (exercises.moveToNext());
            exercises.close();
        }

        if (!hasExercises) {
            TextView emptyExercises = new TextView(this);
            emptyExercises.setText("Пока в плане нет упражнений");
            emptyExercises.setTextColor(getColor(R.color.fitbook_text_secondary));
            emptyExercises.setTextSize(14f);
            emptyExercises.setPadding(0, 8, 0, 0);
            container.addView(emptyExercises);
        }
        plan.close();
    }

    private void addExerciseCard(String name, int sets, int reps, float weight) {
        View card = getLayoutInflater().inflate(R.layout.item_client_plan_exercise, container, false);
        ((TextView) card.findViewById(R.id.tvName)).setText(name);
        ((TextView) card.findViewById(R.id.tvDetails)).setText(sets + " подходов × " + reps + " повторений • " + formatFloat(weight) + " кг");
        ((TextView) card.findViewById(R.id.tvHint)).setText("Подсказка: выполняй упражнение по технике, заданной тренером");
        container.addView(card);
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "—" : value;
    }

    private String formatFloat(float value) {
        if (value <= 0f) return "—";
        if (Math.abs(value - Math.round(value)) < 0.01f) return String.valueOf(Math.round(value));
        return String.format(java.util.Locale.getDefault(), "%.1f", value);
    }
}
