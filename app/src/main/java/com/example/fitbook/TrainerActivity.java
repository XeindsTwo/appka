package com.example.fitbook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class TrainerActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private long trainerId;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> dataList;    private TextView tvTrainerName, tvSectionTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer);

        dbHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        trainerId = prefs.getLong(DatabaseHelper.COL_USER_ID, 0);
        String trainerName = prefs.getString(DatabaseHelper.COL_FULL_NAME, "РўСЂРµРЅРµСЂ");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("РџР°РЅРµР»СЊ С‚СЂРµРЅРµСЂР°");

        tvTrainerName = findViewById(R.id.tvTrainerName);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        listView = findViewById(R.id.listView);
        tvTrainerName.setText("рџ‘ЁвЂЌрџЏ« " + trainerName);
        tvSectionTitle.setText("РњРѕРё С‚СЂРµРЅРёСЂРѕРІРєРё");

        dataList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);

        findViewById(R.id.btnMySchedule).setOnClickListener(v -> loadMySchedule());
        findViewById(R.id.btnMyClients).setOnClickListener(v -> loadMyClients());
        findViewById(R.id.btnAssignPlan).setOnClickListener(v -> loadMyClientsForAssign());
        findViewById(R.id.btnOpenProfile).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));        setupBottomNavigation();

        loadMySchedule();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_trainer_schedule);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_trainer_schedule) {
                tvSectionTitle.setText("РњРѕРё С‚СЂРµРЅРёСЂРѕРІРєРё");
                loadMySchedule();
                return true;
            } else if (itemId == R.id.nav_trainer_clients) {
                tvSectionTitle.setText("РњРѕРё РєР»РёРµРЅС‚С‹");
                loadMyClients();
                return true;
            } else if (itemId == R.id.nav_trainer_plan) {
                tvSectionTitle.setText("РќР°Р·РЅР°С‡РµРЅРёРµ РїР»Р°РЅР°");
                loadMyClientsForAssign();
                return true;
            }
            return false;
        });
    }

    private void loadMySchedule() {
        dataList.clear();
        Cursor schedule = dbHelper.getTrainerSchedule(trainerId);

        if (schedule != null && schedule.moveToFirst()) {
            do {
                String workoutType = schedule.getString(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_TYPE));
                String date = schedule.getString(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_DATE));
                String time = schedule.getString(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_TIME));
                int current = schedule.getInt(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_CURRENT_CLIENTS));
                int max = schedule.getInt(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_MAX_CLIENTS));
                int duration = schedule.getInt(schedule.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_DURATION));

                dataList.add("рџЏ‹пёЏ " + workoutType +
                        "\nрџ“… " + date + " " + time + " (" + duration + " РјРёРЅ)" +
                        "\nрџ‘Ґ Р—Р°РїРёСЃР°РЅРѕ: " + current + "/" + max);
            } while (schedule.moveToNext());
            schedule.close();
        } else {
            dataList.add("РЈ РІР°СЃ РїРѕРєР° РЅРµС‚ РЅР°Р·РЅР°С‡РµРЅРЅС‹С… С‚СЂРµРЅРёСЂРѕРІРѕРє");
        }
        adapter.notifyDataSetChanged();
    }

    private void loadMyClients() {
        dataList.clear();
        Cursor clients = dbHelper.getTrainerClients(trainerId);

        if (clients != null && clients.moveToFirst()) {
            do {
                String name = clients.getString(clients.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME));
                String phone = clients.getString(clients.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE));
                String email = clients.getString(clients.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL));

                dataList.add("рџ‘¤ " + name +
                        "\nрџ“ћ " + phone +
                        "\nрџ“§ " + email);
            } while (clients.moveToNext());
            clients.close();
        } else {
            dataList.add("РЈ РІР°СЃ РїРѕРєР° РЅРµС‚ РєР»РёРµРЅС‚РѕРІ");
        }
        adapter.notifyDataSetChanged();
    }

    private void loadMyClientsForAssign() {
        dataList.clear();
        Cursor clients = dbHelper.getTrainerClients(trainerId);

        if (clients != null && clients.moveToFirst()) {
            final ArrayList<Long> clientIds = new ArrayList<>();
            final ArrayList<String> clientNames = new ArrayList<>();

            do {
                long clientId = clients.getLong(clients.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID));
                String name = clients.getString(clients.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME));
                String phone = clients.getString(clients.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE));

                clientIds.add(clientId);
                clientNames.add(name);

                dataList.add("рџ‘¤ " + name +
                        "\nрџ“ћ " + phone +
                        "\nв”Ѓв”Ѓв”Ѓв”Ѓв”Ѓв”Ѓв”Ѓв”Ѓв”Ѓв”Ѓв”Ѓв”Ѓв”Ѓв”Ѓв”Ѓв”Ѓв”Ѓв”Ѓв”Ѓв”Ѓв”Ѓв”Ѓ\nвћЎпёЏ РќР°Р¶РјРёС‚Рµ С‡С‚РѕР±С‹ РЅР°Р·РЅР°С‡РёС‚СЊ РїР»Р°РЅ");
            } while (clients.moveToNext());
            clients.close();

            adapter.notifyDataSetChanged();

            listView.setOnItemClickListener((parent, view, position, id) -> {
                if (position < clientIds.size()) {
                    showAssignPlanDialog(clientIds.get(position), clientNames.get(position));
                }
            });
        } else {
            dataList.add("РЈ РІР°СЃ РїРѕРєР° РЅРµС‚ РєР»РёРµРЅС‚РѕРІ РґР»СЏ РЅР°Р·РЅР°С‡РµРЅРёСЏ РїР»Р°РЅР°");
            adapter.notifyDataSetChanged();
        }
    }

    private void showAssignPlanDialog(final long clientId, String clientName) {
        AlertDialog.Builder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_plan_assign, null);

        final EditText etNotes = view.findViewById(R.id.etNotes);

        builder.setTitle("рџ“‹ РќР°Р·РЅР°С‡РёС‚СЊ РїР»Р°РЅ РґР»СЏ " + clientName)
                .setView(view)
                .setPositiveButton("Р”Р°Р»РµРµ", (dialog, which) -> {
                    long planId = dbHelper.createWorkoutPlan(clientId, trainerId, etNotes.getText().toString());
                    if (planId != -1) {
                        showAddExercisesDialog(planId, clientName);
                    } else {
                        Toast.makeText(this, "РћС€РёР±РєР° СЃРѕР·РґР°РЅРёСЏ РїР»Р°РЅР°", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("РћС‚РјРµРЅР°", null)
                .show();
    }

    private void showAddExercisesDialog(final long planId, String clientName) {
        AlertDialog.Builder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_exercise_add, null);

        final EditText etExerciseName = view.findViewById(R.id.etExerciseName);
        final EditText etSets = view.findViewById(R.id.etSets);
        final EditText etReps = view.findViewById(R.id.etReps);
        final EditText etWeight = view.findViewById(R.id.etWeight);
        Button btnAdd = view.findViewById(R.id.btnAddExercise);
        Button btnFinish = view.findViewById(R.id.btnFinish);
        final TextView tvExercisesList = view.findViewById(R.id.tvExercisesList);

        final StringBuilder exercisesList = new StringBuilder();

        builder.setTitle("вћ• Р”РѕР±Р°РІРёС‚СЊ СѓРїСЂР°Р¶РЅРµРЅРёСЏ РґР»СЏ " + clientName)
                .setView(view)
                .setCancelable(false);

        final AlertDialog dialog = builder.create();

        btnAdd.setOnClickListener(v -> {
            String name = etExerciseName.getText().toString().trim();
            if (!name.isEmpty()) {
                try {
                    int sets = Integer.parseInt(etSets.getText().toString());
                    int reps = Integer.parseInt(etReps.getText().toString());
                    float weight = Float.parseFloat(etWeight.getText().toString());

                    dbHelper.addExerciseToPlan(planId, name, sets, reps, weight);

                    exercisesList.append("вЂў ").append(name).append(": ").append(sets)
                            .append(" x ").append(reps).append(" (").append(weight).append(" РєРі)\n");
                    tvExercisesList.setText(exercisesList.toString());

                    etExerciseName.setText("");
                    etSets.setText("3");
                    etReps.setText("12");
                    etWeight.setText("50");

                    Toast.makeText(this, "РЈРїСЂР°Р¶РЅРµРЅРёРµ РґРѕР±Р°РІР»РµРЅРѕ", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "РћС€РёР±РєР°: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Р’РІРµРґРёС‚Рµ РЅР°Р·РІР°РЅРёРµ СѓРїСЂР°Р¶РЅРµРЅРёСЏ", Toast.LENGTH_SHORT).show();
            }
        });

        btnFinish.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(this, "вњ“ РўСЂРµРЅРёСЂРѕРІРѕС‡РЅС‹Р№ РїР»Р°РЅ РЅР°Р·РЅР°С‡РµРЅ!", Toast.LENGTH_LONG).show();
        });

        dialog.show();
    }

    private void logout() {
        AlertDialog.Builder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        builder.setTitle("Р’С‹С…РѕРґ")
                .setMessage("Р’С‹ СѓРІРµСЂРµРЅС‹, С‡С‚Рѕ С…РѕС‚РёС‚Рµ РІС‹Р№С‚Рё?")
                .setPositiveButton("Р”Р°", (dialog, which) -> {
                    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    prefs.edit().clear().apply();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("РћС‚РјРµРЅР°", null)
                .show();
    }
}

