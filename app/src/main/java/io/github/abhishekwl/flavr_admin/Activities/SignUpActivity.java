package io.github.abhishekwl.flavr_admin.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import io.github.abhishekwl.flavr_admin.Helpers.ApiClient;
import io.github.abhishekwl.flavr_admin.Helpers.ApiInterface;
import io.github.abhishekwl.flavr_admin.Models.Hotel;
import io.github.abhishekwl.flavr_admin.R;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

  @BindView(R.id.signUpOrganizationNameEditText)
  TextInputEditText signUpOrganizationNameEditText;
  @BindView(R.id.signUpContactNumberEditText)
  TextInputEditText signUpContactNumberEditText;
  @BindView(R.id.signUpEmailAddressEditText)
  TextInputEditText signUpEmailAddressEditText;
  @BindView(R.id.signUpPasswordEditText)
  TextInputEditText signUpPasswordEditText;
  @BindView(R.id.signUpConfirmPasswordEditText)
  TextInputEditText signUpConfirmPasswordEditText;
  @BindView(R.id.signUpButton)
  Button signUpButton;

  private Unbinder unbinder;
  private FirebaseAuth firebaseAuth;
  private ApiInterface apiInterface;
  private FusedLocationProviderClient fusedLocationProviderClient;
  private Location deviceLocation;
  private MaterialDialog materialDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sign_up);

    unbinder = ButterKnife.bind(SignUpActivity.this);
    initializeComponents();
    initializeViews();
  }

  private void initializeComponents() {
    firebaseAuth = FirebaseAuth.getInstance();
    apiInterface = ApiClient.getRetrofit(getApplicationContext()).create(ApiInterface.class);
    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(SignUpActivity.this);
  }

  private void initializeViews() {

  }

  @SuppressLint("MissingPermission")
  private void retrieveLocation() {
    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> deviceLocation = location).addOnFailureListener(e -> {
      Snackbar.make(signUpButton, "We are facing a little trouble finding out where you are :(", Snackbar.LENGTH_INDEFINITE)
          .setActionTextColor(Color.YELLOW)
          .setAction("SETTINGS", v -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))).show();
    });
  }

  private void notifyMessage(String message) {
    Snackbar.make(signUpButton, message, Snackbar.LENGTH_SHORT).show();
  }

  @OnClick(R.id.signUpButton)
  public void onSignUpButtonPress() {
    if (deviceLocation==null) notifyMessage("Please try again in a while. We are having trouble finding where your location");
    else {
      String hotelName = signUpOrganizationNameEditText.getText().toString();
      String contactNumber = signUpContactNumberEditText.getText().toString();
      String emailAddress = signUpEmailAddressEditText.getText().toString();
      String password = signUpPasswordEditText.getText().toString();
      String confirmPassword = signUpConfirmPasswordEditText.getText().toString();

      if (validateEntries(hotelName, contactNumber, emailAddress, password, confirmPassword)) {
        disableUI("Creating new admin account");

        firebaseAuth.createUserWithEmailAndPassword(emailAddress, password).addOnSuccessListener(
            authResult -> {
              performNetworkRequest(authResult.getUser().getUid(), hotelName, contactNumber, emailAddress);
            }).addOnFailureListener(e -> notifyMessage(e.getMessage()));
      }
    }
  }

  private boolean validateEntries(String hotelName, String contactNumber, String emailAddress, String password, String confirmPassword) {
    if (TextUtils.isEmpty(hotelName)) {
      notifyMessage("Please enter a valid name for your organization");
      return false;
    } else if (TextUtils.isEmpty(contactNumber)) {
      notifyMessage("Please enter a contact number for your organization");
      return false;
    } else if (TextUtils.isEmpty(emailAddress) || !emailAddress.contains("@")) {
      notifyMessage("Please enter a valid Email Address");
      return false;
    } else if (!password.equals(confirmPassword)) {
      notifyMessage("Passwords do not match");
      return false;
    } else return true;
  }

  private void performNetworkRequest(String uid, String hotelName, String contactNumber, String emailAddress) {
    if (deviceLocation==null) {
      notifyMessage("Please try again in a while. We are having trouble finding your location.");
      Objects.requireNonNull(firebaseAuth.getCurrentUser()).delete();
    } else {
      apiInterface.createNewHotel(uid, hotelName, emailAddress, contactNumber, deviceLocation.getLatitude(), deviceLocation.getLongitude()).enqueue(
          new Callback<Hotel>() {
            @Override
            public void onResponse(@NonNull Call<Hotel> call, @NonNull Response<Hotel> response) {
              enableUI();
              if (TextUtils.isEmpty(response.body().getId())) {
                notifyMessage("There has been an error creating your account. Please try again.");
                firebaseAuth.getCurrentUser().delete();
              } else {
                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                finish();
              }
            }

            @Override
            public void onFailure(@NonNull Call<Hotel> call, @NonNull Throwable t) {
              notifyMessage(t.getMessage());
              firebaseAuth.getCurrentUser().delete();
            }
          });
    }
  }

  private void enableUI() {
    if (materialDialog!=null && materialDialog.isShowing()) materialDialog.dismiss();
    if (!signUpButton.isEnabled()) signUpButton.setEnabled(true);
  }

  private void disableUI(String message) {
    materialDialog = new MaterialDialog.Builder(SignUpActivity.this)
        .title(R.string.app_name)
        .content(message)
        .contentColor(Color.BLACK)
        .progress(true, 0)
        .show();
    signUpButton.setEnabled(false);
  }

  @Override
  protected void onStart() {
    super.onStart();
    retrieveLocation();
  }

  @Override
  protected void onDestroy() {
    unbinder.unbind();
    super.onDestroy();
  }
}
