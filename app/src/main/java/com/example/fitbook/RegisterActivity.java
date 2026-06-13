package com.example.fitbook;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    private EditText etRegisterFullName, etRegisterUsername, etRegisterPassword, etRegisterPhone;
    private Button btnCreateAccount, btnBackToLogin;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        etRegisterFullName = findViewById(R.id.etRegisterFullName);
        etRegisterUsername = findViewById(R.id.etRegisterUsername);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        etRegisterPhone = findViewById(R.id.etRegisterPhone);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        btnCreateAccount.setOnClickListener(v -> registerClient());
        btnBackToLogin.setOnClickListener(v -> finish());
    }

    private void registerClient() {
        String fullName = etRegisterFullName.getText().toString().trim();
        String username = etRegisterUsername.getText().toString().trim();
        String password = etRegisterPassword.getText().toString().trim();
        String phone = etRegisterPhone.getText().toString().trim();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните ФИО, логин и пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 4) {
            Toast.makeText(this, "Пароль должен быть не короче 4 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = dbHelper.registerClient(username, password, fullName, phone, "");
        if (success) {
            Toast.makeText(this, "Аккаунт создан. Теперь можно войти", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("registered_username", username);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Такой логин уже существует", Toast.LENGTH_LONG).show();
        }
    }
}
