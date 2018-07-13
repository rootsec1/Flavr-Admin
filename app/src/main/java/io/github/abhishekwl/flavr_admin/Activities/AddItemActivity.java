package io.github.abhishekwl.flavr_admin.Activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
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
import io.github.abhishekwl.flavr_admin.Helpers.ApiClient;
import io.github.abhishekwl.flavr_admin.Helpers.ApiInterface;
import io.github.abhishekwl.flavr_admin.Models.Category;
import io.github.abhishekwl.flavr_admin.Models.Food;
import io.github.abhishekwl.flavr_admin.Models.Hotel;
import io.github.abhishekwl.flavr_admin.R;
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

  private FirebaseAuth firebaseAuth;
  private ApiInterface apiInterface;
  private Unbinder unbinder;
  private String categoryName;
  private Hotel currentHotel;
  private String itemImageUrl;
  private MaterialDialog materialDialog;

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
    categoryName = ((Category)getIntent().getParcelableExtra("CATEGORY")).getCategoryName();
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
  }

  private void initializeViews() {

  }

  @OnClick(R.id.addItemButton)
  public void onAddItemButtonPress() {
    if (currentHotel==null) notifyMessage("Please retry in a while");
    else {
      materialDialog = new MaterialDialog.Builder(AddItemActivity.this)
          .title(R.string.app_name)
          .content("Updating menu")
          .contentColor(Color.BLACK)
          .progress(true, 0)
          .show();
      addItemButton.setEnabled(false);
      String itemName = addItemNameEditText.getText().toString();
      double itemCost = Double.parseDouble(addItemCostEditText.getText().toString());
      String categoryName = addItemCategorySpinner.getSelectedItem().toString();
      String hotelId = currentHotel.getId();
      apiInterface.createNewFoodItem(itemName, itemCost, categoryName, hotelId, itemImageUrl).enqueue(
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

  private void clearFields() {
    addItemNameEditText.setText("");
    addItemCostEditText.setText("");
    itemImageUrl = null;
    Glide.with(getApplicationContext()).load(itemImageUrl).into(addItemImageView);
  }

  private void notifyMessage(String message) {
    if (materialDialog!=null && materialDialog.isShowing()) materialDialog.dismiss();
    if (!addItemButton.isEnabled()) addItemButton.setEnabled(true);
    Snackbar.make(addItemButton, message, Snackbar.LENGTH_SHORT).show();
  }

  @Override
  protected void onDestroy() {
    unbinder.unbind();
    super.onDestroy();
  }
}
