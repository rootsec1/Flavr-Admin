package io.github.abhishekwl.flavr_admin.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import io.github.abhishekwl.flavr_admin.R;

public class SplashActivity extends AppCompatActivity {

  private static final int SPLASH_DELAY = 1000;
  private FirebaseAuth firebaseAuth;
  private Intent nextIntent;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
    setContentView(R.layout.activity_splash);

    firebaseAuth = FirebaseAuth.getInstance();
    nextIntent = new Intent(SplashActivity.this, MainActivity.class);
    new Handler().postDelayed(() -> {
      if (firebaseAuth.getCurrentUser()==null) nextIntent = new Intent(SplashActivity.this, SignInActivity.class);
      startActivity(nextIntent);
      finish();
    }, SPLASH_DELAY);
  }
}
