package io.github.abhishekwl.flavr_admin.Activities;

import android.content.Intent;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.google.firebase.auth.FirebaseAuth;
import io.github.abhishekwl.flavr_admin.R;

public class SignInActivity extends AppCompatActivity {

  @BindView(R.id.signInEmailEditText)
  TextInputEditText signInEmailEditText;
  @BindView(R.id.signInPasswordEditText)
  TextInputEditText signInPasswordEditText;
  @BindView(R.id.signInButton)
  Button signInButton;
  @BindView(R.id.signInCreateNewAccountTextView)
  TextView signInCreateNewAccountTextView;
  @BindView(R.id.signInForgotPasswordTextView)
  TextView signInForgotPasswordTextView;
  @BindView(R.id.signInProgressBar)
  ProgressBar signInProgressBar;
  @BindColor(R.color.colorAccent) int colorAccent;

  private Unbinder unbinder;
  private FirebaseAuth firebaseAuth;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().setNavigationBarColor(
        ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
    setContentView(R.layout.activity_sign_in);
    ButterKnife.bind(this);

    initializeComponents();
    initializeViews();
  }

  private void initializeComponents() {
    unbinder = ButterKnife.bind(SignInActivity.this);
    firebaseAuth = FirebaseAuth.getInstance();
  }

  private void initializeViews() {
    signInProgressBar.getProgressDrawable().setColorFilter(colorAccent, Mode.SRC_IN);
  }

  @OnClick(R.id.signInButton)
  public void onSignInButtonPress() {
    String clientEmail = signInEmailEditText.getText().toString();
    String clientPassword = signInPasswordEditText.getText().toString();
    signInProgressBar.setVisibility(View.VISIBLE);
    signInButton.setEnabled(false);

    firebaseAuth.signInWithEmailAndPassword(clientEmail, clientPassword).addOnCompleteListener(
        task -> {
          signInProgressBar.setVisibility(View.GONE);
          signInButton.setEnabled(true);
          if (task.isSuccessful()) {
            startActivity(new Intent(SignInActivity.this, MainActivity.class));
            finish();
          } else
            Snackbar.make(signInButton, task.getException().getMessage(), Snackbar.LENGTH_LONG)
            .setActionTextColor(colorAccent)
            .setAction("RETRY", view -> onSignInButtonPress()).show();
        });
  }

  @Override
  protected void onDestroy() {
    unbinder.unbind();
    super.onDestroy();
  }
}
