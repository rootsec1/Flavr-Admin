package io.github.abhishekwl.flavr_admin.Fragments;


import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.Builder;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import io.github.abhishekwl.flavr_admin.Activities.HistoryActivity;
import io.github.abhishekwl.flavr_admin.Activities.MainActivity;
import io.github.abhishekwl.flavr_admin.Helpers.ApiClient;
import io.github.abhishekwl.flavr_admin.Helpers.ApiInterface;
import io.github.abhishekwl.flavr_admin.Models.Hotel;
import io.github.abhishekwl.flavr_admin.R;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

  @BindView(R.id.profileFragmentImageView)
  ImageView profileFragmentImageView;
  @BindView(R.id.profileFragmentAppBar)
  AppBarLayout profileFragmentAppBar;
  @BindView(R.id.profileFragmentOrganizationNameEditText)
  TextInputEditText profileFragmentOrganizationNameEditText;
  @BindView(R.id.profileFragmentContactNumberEditText)
  TextInputEditText profileFragmentContactNumberEditText;
  @BindView(R.id.profileFragmentUpdateDetailsButton)
  Button profileFragmentUpdateDetailsButton;
  @BindView(R.id.profileFragmentOrderHistoryLinearLayout)
  LinearLayout profileFragmentOrderHistoryLinearLayout;
  @BindColor(R.color.colorAccent)
  int colorAccent;
  @BindView(R.id.profileFragmentCameraButton)
  FloatingActionButton profileFragmentCameraButton;

  private View rootView;
  private Unbinder unbinder;
  private FirebaseAuth firebaseAuth;
  private ApiInterface apiInterface;
  private MaterialDialog materialDialog;
  private StorageReference storageReference;
  private static final int RC_IMAGE_PICK_GALLERY = 661;

  public ProfileFragment() {
    // Required empty public constructor
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    rootView = inflater.inflate(R.layout.fragment_profile, container, false);
    unbinder = ButterKnife.bind(this, rootView);
    initializeComponents();
    initializeViews();
    return rootView;
  }

  private void initializeComponents() {
    firebaseAuth = FirebaseAuth.getInstance();
    apiInterface = ApiClient.getRetrofit(rootView.getContext()).create(ApiInterface.class);
    storageReference = FirebaseStorage.getInstance()
        .getReference("hotels/" + firebaseAuth.getUid() + "/profile_picture.jpg");
  }

  private void initializeViews() {
    fetchHotelDetails();
  }

  private void fetchHotelDetails() {
    disableUI("Fetching profile data");
    apiInterface.getHotel(firebaseAuth.getUid()).enqueue(new Callback<Hotel>() {
      @Override
      public void onResponse(@NonNull Call<Hotel> call, @NonNull Response<Hotel> response) {
        enableUI();
        if (response.body() == null) {
          notifyMessage("There has been an error contacting our servers :(");
        } else {
          renderHotelObject(Objects.requireNonNull(response.body()));
        }
      }

      @Override
      public void onFailure(@NonNull Call<Hotel> call, @NonNull Throwable t) {
        notifyMessage(t.getMessage());
      }
    });
  }

  private void renderHotelObject(Hotel hotel) {
    Glide.with(rootView.getContext())
        .load(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getPhotoUrl())
        .into(profileFragmentImageView);
    profileFragmentOrganizationNameEditText.setText(hotel.getName());
    profileFragmentContactNumberEditText.setText(hotel.getPhone());
    notifyMessage("Profile fetched/updated.");
  }

  private void disableUI(String message) {
    materialDialog = new Builder(rootView.getContext())
        .title(R.string.app_name)
        .content(message)
        .progress(true, 0)
        .show();
    profileFragmentUpdateDetailsButton.setEnabled(false);
  }

  private void enableUI() {
    if (materialDialog != null && materialDialog.isShowing()) {
      materialDialog.dismiss();
    }
    if (profileFragmentUpdateDetailsButton!=null && !profileFragmentUpdateDetailsButton.isEnabled()) {
      profileFragmentUpdateDetailsButton.setEnabled(true);
    }
  }

  private void notifyMessage(String message) {
    enableUI();
    Snackbar.make(profileFragmentUpdateDetailsButton, message, Snackbar.LENGTH_SHORT).show();
  }

  @OnClick(R.id.profileFragmentUpdateDetailsButton)
  public void onUpdateDetailsButtonPressed() {
    if (((MainActivity) Objects.requireNonNull(getActivity())).getDeviceLocation() == null) {
      Snackbar.make(profileFragmentUpdateDetailsButton,
          "We are facing a little trouble finding out where you are :(", Snackbar.LENGTH_LONG)
          .setActionTextColor(colorAccent)
          .setAction("SETTINGS",
              v -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))).show();
    } else {
      String uid = firebaseAuth.getUid();
      String name = profileFragmentOrganizationNameEditText.getText().toString();
      String phone = profileFragmentContactNumberEditText.getText().toString();
      String email = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getEmail();
      double latitude = ((MainActivity) Objects.requireNonNull(getActivity())).getDeviceLocation()
          .getLatitude();
      double longitude = ((MainActivity) Objects.requireNonNull(getActivity())).getDeviceLocation()
          .getLongitude();
      disableUI("Updating profile");
      updateHotelDetails(uid, name, phone, email, latitude, longitude);
    }
  }

  private void updateHotelDetails(String uid, String name, String phone, String email,
      double latitude, double longitude) {
    apiInterface.updateHotel(uid, name, email, phone, latitude, longitude).enqueue(
        new Callback<Hotel>() {
          @Override
          public void onResponse(@NonNull Call<Hotel> call, @NonNull Response<Hotel> response) {
            enableUI();
            if (response.body() == null) {
              notifyMessage("There has been an error contacting our servers :(");
            } else {
              renderHotelObject(Objects.requireNonNull(response.body()));
            }
          }

          @Override
          public void onFailure(@NonNull Call<Hotel> call, @NonNull Throwable t) {
            notifyMessage(t.getMessage());
          }
        });
  }

  @OnClick(R.id.profileFragmentCameraButton)
  public void onCameraButtonPress() {
    Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    pickIntent.setType("image/*");
    Intent chooserIntent = Intent.createChooser(pickIntent, "Select Image");
    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
    startActivityForResult(chooserIntent, RC_IMAGE_PICK_GALLERY);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode==RC_IMAGE_PICK_GALLERY) {
      if (resultCode==RESULT_OK && data!=null && data.getData()!=null) {
        Uri selectedImageUri = data.getData();
        updateHotelProfileImage(selectedImageUri);
      } else notifyMessage("Operation cancelled by user.");
    }
  }

  private void updateHotelProfileImage(Uri selectedImageUri) {
    disableUI("Updating profile picture.");
    storageReference.putFile(selectedImageUri).addOnCompleteListener(
        task -> {
            enableUI();
            if (task.isSuccessful()) {
              storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                if (uri!=null) {
                  UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                      .setPhotoUri(uri)
                      .build();
                  Objects.requireNonNull(firebaseAuth.getCurrentUser()).updateProfile(userProfileChangeRequest).addOnCompleteListener(
                      task1 -> {
                          if (task1.isSuccessful()) {
                            Glide.with(rootView.getContext()).load(firebaseAuth.getCurrentUser().getPhotoUrl()).into(profileFragmentImageView);
                            notifyMessage("Profile picture updated.");
                          }
                          else notifyMessage("There has been an error updating your profile picture");
                      });
                } else notifyMessage("There has been an error updating your profile picture.");
              });
            }
            else notifyMessage("There has been an error updating your profile picture");
        });
  }

  @OnClick(R.id.profileFragmentOrderHistoryLinearLayout)
  public void onOrderHistoryButtonPress() {
    startActivity(new Intent(getActivity(), HistoryActivity.class));
  }

  @Override
  public void onStart() {
    super.onStart();
    unbinder = ButterKnife.bind(this, rootView);
  }

  @Override
  public void onDestroyView() {
    unbinder.unbind();
    super.onDestroyView();
  }
}
