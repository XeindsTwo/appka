package com.example.fitbook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ProfileActivity extends AppCompatActivity {
    private static final int REQUEST_PICK_PROFILE_PHOTO = 1001;
    private static final String PREF_PROFILE_PHOTO_URI = "profile_photo_uri";

    private DatabaseHelper dbHelper;
    private SharedPreferences prefs;
    private long userId;
    private String role;

    private ImageView ivProfilePhoto;
    private TextView tvProfileName, tvProfileRole, tvProfileInitials;
    private TextInputEditText etProfileName, etProfilePhone, etProfileEmail, etProfilePassword;
    private TextInputLayout tilProfilePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);
        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = prefs.getLong(DatabaseHelper.COL_USER_ID, 0);
        role = prefs.getString(DatabaseHelper.COL_ROLE, "client");

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileRole = findViewById(R.id.tvProfileRole);
        tvProfileInitials = findViewById(R.id.tvProfileInitials);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        etProfileName = findViewById(R.id.etProfileName);
        etProfilePhone = findViewById(R.id.etProfilePhone);
        etProfileEmail = findViewById(R.id.etProfileEmail);
        etProfilePassword = findViewById(R.id.etProfilePassword);
        tilProfilePassword = findViewById(R.id.tilProfilePassword);

        findViewById(R.id.btnProfileBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnProfilePhoto).setOnClickListener(v -> showPhotoDialog());
        findViewById(R.id.btnProfileSave).setOnClickListener(v -> saveProfile());
        findViewById(R.id.btnProfileLogout).setOnClickListener(v -> confirmLogout());

        movePasswordEyeLeft();

        loadProfile();
        loadProfilePhoto();
    }

    private void loadProfile() {
        String fullName = prefs.getString(DatabaseHelper.COL_FULL_NAME, "");
        String phone = prefs.getString(DatabaseHelper.COL_PHONE, "");
        String email = prefs.getString(DatabaseHelper.COL_EMAIL, "");

        Cursor user = dbHelper.getUserById(userId);
        if (user != null && user.moveToFirst()) {
            fullName = user.getString(user.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME));
            phone = user.getString(user.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE));
            email = user.getString(user.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL));
            user.close();
        }

        String roleLabel = getRoleLabel(role);
        tvProfileName.setText(fullName == null || fullName.trim().isEmpty() ? roleLabel : fullName);
        tvProfileRole.setText(getString(R.string.profile_role_format, roleLabel, getString(R.string.profile_role_suffix)));
        tvProfileInitials.setText(getInitials(fullName, roleLabel));
        etProfileName.setText(fullName);
        etProfilePhone.setText(phone);
        etProfileEmail.setText(email);
    }

    private void saveProfile() {
        String fullName = valueOf(etProfileName);
        String phone = valueOf(etProfilePhone);
        String email = valueOf(etProfileEmail);
        String password = valueOf(etProfilePassword);

        if (fullName.isEmpty()) {
            Toast.makeText(this, R.string.profile_name_required, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean saved = dbHelper.updateUserProfile(userId, fullName, phone, email, password);
        if (saved) {
            prefs.edit()
                    .putString(DatabaseHelper.COL_FULL_NAME, fullName)
                    .putString(DatabaseHelper.COL_PHONE, phone)
                    .putString(DatabaseHelper.COL_EMAIL, email)
                    .apply();
            etProfilePassword.setText("");
            tvProfileName.setText(fullName);
            tvProfileInitials.setText(getInitials(fullName, getRoleLabel(role)));
            Toast.makeText(this, R.string.profile_saved, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.profile_save_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void showPhotoDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(R.string.profile_photo_title)
                .setItems(new CharSequence[]{
                        getString(R.string.profile_photo_choose),
                        getString(R.string.profile_photo_remove)
                }, (dialog, which) -> {
                    if (which == 0) {
                        pickProfilePhoto();
                    } else {
                        removeProfilePhoto();
                    }
                })
                .show();
    }

    private void pickProfilePhoto() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_PICK_PROFILE_PHOTO);
    }

    private void loadProfilePhoto() {
        String photoUri = prefs.getString(PREF_PROFILE_PHOTO_URI + "_" + userId, "");
        if (photoUri.isEmpty()) {
            ivProfilePhoto.setImageDrawable(null);
            tvProfileInitials.setVisibility(View.VISIBLE);
            return;
        }

        ivProfilePhoto.setImageURI(Uri.parse(photoUri));
        tvProfileInitials.setVisibility(View.GONE);
    }

    private void removeProfilePhoto() {
        prefs.edit().remove(PREF_PROFILE_PHOTO_URI + "_" + userId).apply();
        ivProfilePhoto.setImageDrawable(null);
        tvProfileInitials.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_PROFILE_PHOTO && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri photoUri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(photoUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException ignored) {
            }
            prefs.edit().putString(PREF_PROFILE_PHOTO_URI + "_" + userId, photoUri.toString()).apply();
            loadProfilePhoto();
        }
    }

    private String getRoleLabel(String role) {
        switch (role) {
            case "admin":
                return getString(R.string.profile_role_admin);
            case "trainer":
                return getString(R.string.profile_role_trainer);
            default:
                return getString(R.string.profile_role_client);
        }
    }

    private String valueOf(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private void movePasswordEyeLeft() {
        if (tilProfilePassword == null) {
            return;
        }
        View endIcon = tilProfilePassword.findViewById(com.google.android.material.R.id.text_input_end_icon);
        if (endIcon != null) {
            int offset = (int) (6 * getResources().getDisplayMetrics().density);
            endIcon.setTranslationX(-offset);
        }
    }

    private String getInitials(String fullName, String fallback) {
        String source = fullName == null || fullName.trim().isEmpty() ? fallback : fullName.trim();
        String[] parts = source.split("\\s+");
        String first = parts.length > 0 && !parts[0].isEmpty() ? parts[0].substring(0, 1) : "Ф";
        return first.toUpperCase();
    }

    private void confirmLogout() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(R.string.profile_logout_title)
                .setMessage(R.string.profile_logout_message)
                .setPositiveButton(R.string.profile_logout_positive, (dialog, which) -> logout())
                .setNegativeButton(R.string.profile_logout_negative, null)
                .show();
    }

    private void logout() {
        prefs.edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
