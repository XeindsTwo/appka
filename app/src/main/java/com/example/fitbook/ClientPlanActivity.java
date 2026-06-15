package com.example.fitbook;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
            return;
        }
        tvEmpty.setVisibility(View.GONE);

        addLine("Тренер", plan.getString(plan.getColumnIndexOrThrow("trainer_name")));
        addLine("Назначен", DateFormatUtils.formatRussianDate(plan.getString(plan.getColumnIndexOrThrow(DatabaseHelper.COL_PLAN_ASSIGNED_DATE))));
        addLine("Заметки", safe(plan.getString(plan.getColumnIndexOrThrow(DatabaseHelper.COL_PLAN_NOTES))));

        long planId = plan.getLong(plan.getColumnIndexOrThrow(DatabaseHelper.COL_PLAN_ID));
        Cursor exercises = dbHelper.getPlanExercises(planId);
        if (exercises != null && exercises.moveToFirst()) {
            do {
                String name = exercises.getString(exercises.getColumnIndexOrThrow(DatabaseHelper.COL_PE_EXERCISE_NAME));
                int sets = exercises.getInt(exercises.getColumnIndexOrThrow(DatabaseHelper.COL_PE_SETS));
                int reps = exercises.getInt(exercises.getColumnIndexOrThrow(DatabaseHelper.COL_PE_REPS));
                float weight = exercises.getFloat(exercises.getColumnIndexOrThrow(DatabaseHelper.COL_PE_WEIGHT));
                addLine("Упражнение", name + " - " + sets + " x " + reps + " (" + weight + " кг)");
            } while (exercises.moveToNext());
            exercises.close();
        }
        plan.close();
    }

    private void addLine(String title, String value) {
        TextView tv = new TextView(this);
        tv.setText(title + ": " + safe(value));
        tv.setTextColor(getColor(R.color.fitbook_text_primary));
        tv.setTextSize(16f);
        tv.setPadding(0, 0, 0, 18);
        container.addView(tv);
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "—" : value;
    }
}
