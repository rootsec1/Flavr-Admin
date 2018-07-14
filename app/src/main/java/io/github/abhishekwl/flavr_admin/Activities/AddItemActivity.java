package io.github.abhishekwl.flavr_admin.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import io.github.abhishekwl.flavr_admin.Helpers.ApiClient;
import io.github.abhishekwl.flavr_admin.Helpers.ApiInterface;
import io.github.abhishekwl.flavr_admin.Models.Category;
import io.github.abhishekwl.flavr_admin.Models.Food;
import io.github.abhishekwl.flavr_admin.Models.Hotel;
import io.github.abhishekwl.flavr_admin.R;
import java.util.Arrays;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddItemActivity extends AppCompatActivity {

  @BindView(R.id.addItemImageView)
  ImageView addItemImageView;
  @BindView(R.id.addItemAppbar)
  AppBarLayout addItemAppbar;
  @BindView(R.id.addItemCameraButton)
  FloatingActionButton addItemCameraButton;
  @BindView(R.id.addItemNameEditText)
  TextInputEditText addItemNameEditText;
  @BindView(R.id.addItemCostEditText)
  TextInputEditText addItemCostEditText;
  @BindView(R.id.addItemCategorySpinner)
  Spinner addItemCategorySpinner;
  @BindView(R.id.addItemButton)
  Button addItemButton;

  private static final int RC_IMAGE_PICK_GALLERY = 961;
  private FirebaseAuth firebaseAuth;
  private ApiInterface apiInterface;
  private Unbinder unbinder;
  private String categoryName;
  private Hotel currentHotel;
  private String itemImageUrl;
  private MaterialDialog materialDialog;
  private StorageReference storageReference;
  private boolean editItem;
  private Food foodToBeModified;
  private String[] allCategories;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_item);

    unbinder = ButterKnife.bind(AddItemActivity.this);
    initializeComponents();
    initializeViews();
  }

  private void initializeComponents() {
    firebaseAuth = FirebaseAuth.getInstance();
    apiInterface = ApiClient.getRetrofit(getApplicationContext()).create(ApiInterface.class);
    allCategories = getResources().getStringArray(R.array.categories);
    if (getIntent().getParcelableExtra("CATEGORY")==null) categoryName = allCategories[0];
    else categoryName = ((Category)getIntent().getParcelableExtra("CATEGORY")).getCategoryName();
    editItem = getIntent().getBooleanExtra("EDIT_ITEM", false);
    apiInterface.getHotel(firebaseAuth.getUid()).enqueue(new Callback<Hotel>() {
      @Override
      public void onResponse(@NonNull Call<Hotel> call, @NonNull Response<Hotel> response) {
        if (response.body()!=null) currentHotel = response.body();
      }

      @Override
      public void onFailure(@NonNull Call<Hotel> call, @NonNull Throwable t) {
        notifyMessage(t.getMessage());
      }
    });
    storageReference = FirebaseStorage.getInstance().getReference("hotels/"+Objects.requireNonNull(firebaseAuth.getUid())+"/items");
  }

  @SuppressLint("SetTextI18n")
  private void initializeViews() {
    initializeSpinner();
    if (editItem) {
      foodToBeModified = getIntent().getParcelableExtra("EDIT_FOOD");
      Glide.with(getApplicationContext()).load(foodToBeModified.getImage()).into(addItemImageView);
      addItemNameEditText.setText(foodToBeModified.getName());
      addItemCostEditText.setText(Double.toString(foodToBeModified.getCost()));
      categoryName = foodToBeModified.getCategory();
      addItemCategorySpinner.setSelection(Arrays.asList(allCategories).indexOf(categoryName));
      itemImageUrl = foodToBeModified.getImage();
      addItemButton.setText("UPDATE MENU");
    }
  }

  private void initializeSpinner() {
    ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(AddItemActivity.this, android.R.layout.simple_spinner_item, allCategories);
    stringArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    addItemCategorySpinner.setAdapter(stringArrayAdapter);
    if (categoryName==null) categoryName = allCategories[0];
    addItemCategorySpinner.setSelection(Arrays.asList(allCategories).indexOf(categoryName));
  }

  @OnClick(R.id.addItemButton)
  public void onAddItemButtonPress() {
    if (currentHotel==null) notifyMessage("Please retry in a while");
    else {
      disableUI("Updating menu");
      String itemName = addItemNameEditText.getText().toString();
      double itemCost = Double.parseDouble(addItemCostEditText.getText().toString());
      String categoryName = addItemCategorySpinner.getSelectedItem().toString();
      String hotelId = currentHotel.getId();

      if (editItem) {
        apiInterface.updateFoodItem(foodToBeModified.getId(), itemName, itemCost, categoryName, itemImageUrl==null?"":itemImageUrl).enqueue(
            new Callback<Food>() {
              @Override
              public void onResponse(@NonNull Call<Food> call, @NonNull Response<Food> response) {
                if (response.body()==null) notifyMessage("There has been an error updating your menu.");
                else notifyMessage(itemName+" has been updated in your menu.");
              }

              @Override
              public void onFailure(@NonNull Call<Food> call, @NonNull Throwable t) {
                notifyMessage(t.getMessage());
              }
            });
      } else {
        apiInterface.createNewFoodItem(itemName, itemCost, categoryName, hotelId, itemImageUrl==null?"":itemImageUrl).enqueue(
            new Callback<Food>() {
              @Override
              public void onResponse(@NonNull Call<Food> call, @NonNull Response<Food> response) {
                if (response.body()==null) notifyMessage("There has been an error adding the new item to your menu.");
                else {
                  notifyMessage(itemName+" has been added to your Menu.");
                  clearFields();
                }
              }

              @Override
              public void onFailure(@NonNull Call<Food> call, @NonNull Throwable t) {
                notifyMessage(t.getMessage());
              }
            });
      }
    }
  }

  @OnClick(R.id.addItemCameraButton)
  public void onCameraButtonPress() {
    Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    pickIntent.setType("image/*");
    Intent chooserIntent = Intent.createChooser(pickIntent, "Select Image");
    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
    startActivityForResult(chooserIntent, RC_IMAGE_PICK_GALLERY);
  }

  private void clearFields() {
    addItemNameEditText.setText("");
    addItemCostEditText.setText("");
    itemImageUrl = null;
    Glide.with(getApplicationContext()).load(itemImageUrl).into(addItemImageView);
  }

  private void disableUI(String message) {
    materialDialog = new MaterialDialog.Builder(AddItemActivity.this)
        .title(R.string.app_name)
        .content(message)
        .contentColor(Color.BLACK)
        .progress(true, 0)
        .show();
    addItemCameraButton.setEnabled(false);
    addItemButton.setEnabled(false);
  }

  private void enableUI() {
    if (materialDialog!=null && materialDialog.isShowing()) materialDialog.dismiss();
    if (!addItemCameraButton.isEnabled()) addItemCameraButton.setEnabled(true);
    if (!addItemButton.isEnabled()) addItemButton.setEnabled(true);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode==RC_IMAGE_PICK_GALLERY && resultCode==RESULT_OK && data!=null && data.getData()!=null) uploadImageToFirebase(data.getData());
    else notifyMessage("Operation cancelled by user.");
  }

  private void uploadImageToFirebase(Uri data) {
    disableUI("Uploading image.");
    storageReference.child("/"+data.getLastPathSegment()).putFile(data).addOnCompleteListener(
        task -> {
          enableUI();
          if (task.isSuccessful()) storageReference.child("/"+data.getLastPathSegment()).getDownloadUrl().addOnSuccessListener(
              uri -> {
                Glide.with(getApplicationContext()).load(uri).into(addItemImageView);
                itemImageUrl = uri.toString();
              }).addOnFailureListener(e -> {
                notifyMessage(e.getMessage());
              });
          else notifyMessage("There was an error uploading the image.");
        });
  }

  private void notifyMessage(String message) {
    enableUI();
    Snackbar.make(addItemButton, message, Snackbar.LENGTH_SHORT).show();
  }

  @Override
  protected void onDestroy() {
    unbinder.unbind();
    super.onDestroy();
  }
}
