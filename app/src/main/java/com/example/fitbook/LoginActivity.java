package com.example.fitbook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin, btnShowRegister;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.contains(DatabaseHelper.COL_USER_ID);
        if (isLoggedIn) {
            String role = prefs.getString(DatabaseHelper.COL_ROLE, "");
            redirectToRoleActivity(role);
            return;
        }

        dbHelper = new DatabaseHelper(this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnShowRegister = findViewById(R.id.btnShowRegister);

        String registeredUsername = getIntent().getStringExtra("registered_username");
        if (registeredUsername != null) {
            etUsername.setText(registeredUsername);
        }

        btnLogin.setOnClickListener(v -> login());
        btnShowRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void login() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните логин и пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor user = null;
        try {
            user = dbHelper.login(username, password);
            if (user != null && user.moveToFirst()) {
                long userId = user.getLong(user.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID));
                String role = user.getString(user.getColumnIndexOrThrow(DatabaseHelper.COL_ROLE));
                String fullName = user.getString(user.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME));
                String phone = user.getString(user.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE));
                String email = user.getString(user.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL));

                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                prefs.edit()
                        .putLong(DatabaseHelper.COL_USER_ID, userId)
                        .putString(DatabaseHelper.COL_ROLE, role)
                        .putString(DatabaseHelper.COL_FULL_NAME, fullName)
                        .putString(DatabaseHelper.COL_PHONE, phone)
                        .putString(DatabaseHelper.COL_EMAIL, email)
                        .apply();

                user.close();

                Toast.makeText(this, "Добро пожаловать, " + fullName + "!", Toast.LENGTH_SHORT).show();
                redirectToRoleActivity(role);

            } else {
                Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка входа: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (user != null && !user.isClosed()) {
                user.close();
            }
        }
    }

    private void redirectToRoleActivity(String role) {
        Intent intent;
        switch (role) {
            case "admin":
                intent = new Intent(LoginActivity.this, AdminActivity.class);
                break;
            case "trainer":
                intent = new Intent(LoginActivity.this, TrainerActivity.class);
                break;
            case "client":
                intent = new Intent(LoginActivity.this, ClientActivity.class);
                break;
            default:
                intent = new Intent(LoginActivity.this, LoginActivity.class);
                break;
        }
        startActivity(intent);
        finish();
    }
}
