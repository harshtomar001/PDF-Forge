package com.harsh.pdfstudio.ui;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;


public class SplashActivity extends AppCompatActivity {

    // Total animation duration: wait(350) + scatter(700) + pause(300) + gather(1000) + shine(800) = ~3200ms
    // We navigate to MainActivity after animation finishes
    private static final long NAV_DELAY_MS = 3400;

    private SplashView splashView;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // Full-screen, no action bar
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        splashView = new SplashView(this);
        setContentView(splashView);

        // Navigate to main screen after animation completes
        handler.postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            // Fade transition — matches the logo fade-in at end of animation
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, NAV_DELAY_MS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (splashView != null) splashView.stopAnimation();
    }
}