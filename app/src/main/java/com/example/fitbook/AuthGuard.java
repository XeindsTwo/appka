package com.example.fitbook;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public final class AuthGuard {
    private static final String PREFS_NAME = "user_prefs";

    private AuthGuard() {
    }

    public static boolean requireLoggedIn(AppCompatActivity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long userId = prefs.getLong(DatabaseHelper.COL_USER_ID, 0);
        UserSession session = loadSession(activity, userId);

        if (session != null) {
            prefs.edit().putString(DatabaseHelper.COL_ROLE, session.role).apply();
            return true;
        }

        deny(activity, "Войдите в аккаунт");
        return false;
    }

    public static boolean requireRole(AppCompatActivity activity, String... allowedRoles) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long userId = prefs.getLong(DatabaseHelper.COL_USER_ID, 0);
        UserSession session = loadSession(activity, userId);

        if (session != null) {
            for (String allowedRole : allowedRoles) {
                if (session.role.equals(allowedRole)) {
                    prefs.edit().putString(DatabaseHelper.COL_ROLE, session.role).apply();
                    return true;
                }
            }
        }

        deny(activity, "Нет доступа к этому разделу");
        return false;
    }

    private static UserSession loadSession(Context context, long userId) {
        if (userId <= 0) {
            return null;
        }

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        Cursor user = null;
        try {
            user = dbHelper.getUserById(userId);
            if (user != null && user.moveToFirst()) {
                String role = user.getString(user.getColumnIndexOrThrow(DatabaseHelper.COL_ROLE));
                if (role != null && !role.isEmpty()) {
                    return new UserSession(role);
                }
            }
        } finally {
            if (user != null) {
                user.close();
            }
        }

        return null;
    }

    private static void deny(AppCompatActivity activity, String message) {
        activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply();
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    private static final class UserSession {
        final String role;

        UserSession(String role) {
            this.role = role;
        }
    }
}
