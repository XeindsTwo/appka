package com.example.fitbook;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private static final long SPLASH_DELAY_MS = 1600L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable openNextScreenRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        playSplashAnimation();

        openNextScreenRunnable = this::openNextScreen;
        handler.postDelayed(openNextScreenRunnable, SPLASH_DELAY_MS);
    }

    @Override
    protected void onDestroy() {
        if (openNextScreenRunnable != null) {
            handler.removeCallbacks(openNextScreenRunnable);
        }
        super.onDestroy();
    }

    private void playSplashAnimation() {
        View logoCard = findViewById(R.id.logoCard);
        View title = findViewById(R.id.tvSplashTitle);
        View subtitle = findViewById(R.id.tvSplashSubtitle);
        View loadingIndicator = findViewById(R.id.loadingIndicator);
        View loadingText = findViewById(R.id.tvLoadingText);
        View accentGlow = findViewById(R.id.accentGlow);

        View[] animatedViews = {logoCard, title, subtitle, loadingIndicator, loadingText};
        for (View view : animatedViews) {
            view.setAlpha(0f);
            view.setTranslationY(24f);
        }

        logoCard.setScaleX(0.86f);
        logoCard.setScaleY(0.86f);
        accentGlow.setScaleX(0f);

        AnimatorSet intro = new AnimatorSet();
        intro.playTogether(
                ObjectAnimator.ofFloat(logoCard, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(logoCard, View.TRANSLATION_Y, 24f, 0f),
                ObjectAnimator.ofFloat(logoCard, View.SCALE_X, 0.86f, 1f),
                ObjectAnimator.ofFloat(logoCard, View.SCALE_Y, 0.86f, 1f),
                ObjectAnimator.ofFloat(title, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(title, View.TRANSLATION_Y, 24f, 0f),
                ObjectAnimator.ofFloat(subtitle, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(subtitle, View.TRANSLATION_Y, 24f, 0f),
                ObjectAnimator.ofFloat(loadingIndicator, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(loadingIndicator, View.TRANSLATION_Y, 24f, 0f),
                ObjectAnimator.ofFloat(loadingText, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(loadingText, View.TRANSLATION_Y, 24f, 0f),
                ObjectAnimator.ofFloat(accentGlow, View.SCALE_X, 0f, 1f)
        );
        intro.setDuration(720L);
        intro.setInterpolator(new AccelerateDecelerateInterpolator());
        intro.start();
    }

    private void openNextScreen() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.contains(DatabaseHelper.COL_USER_ID);
        String role = prefs.getString(DatabaseHelper.COL_ROLE, "");

        Intent intent = isLoggedIn ? createRoleIntent(role) : new Intent(this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private Intent createRoleIntent(String role) {
        switch (role) {
            case "admin":
                return new Intent(this, AdminActivity.class);
            case "trainer":
                return new Intent(this, TrainerActivity.class);
            case "client":
                return new Intent(this, ClientActivity.class);
            default:
                return new Intent(this, LoginActivity.class);
        }
    }
}
