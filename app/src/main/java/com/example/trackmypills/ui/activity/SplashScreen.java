package com.example.trackmypills.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.trackmypills.R;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Spinning pill animation
        ImageView splashImg = findViewById(R.id.splash_img);
        Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
        splashImg.startAnimation(rotate);

        // Disclaimer text
        TextView disclaimerTextView = findViewById(R.id.disclaimer_text);
        disclaimerTextView.setText(HtmlCompat.fromHtml(
                "<b>DISCLAIMER:</b> This app is not intended to diagnose, treat, cure, " +
                        "or prevent any medical condition. " +
                        "It is not a substitute for professional medical advice, diagnosis, or treatment. " +
                        "Always consult a qualified healthcare provider regarding " +
                        "any medical concerns.",
                HtmlCompat.FROM_HTML_MODE_LEGACY));

        // Length of splash screen
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 4000); // 4 seconds

    }
}