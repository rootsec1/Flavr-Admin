package io.github.abhishekwl.flavr_admin.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.google.firebase.auth.FirebaseAuth;
import io.github.abhishekwl.flavr_admin.Adapters.FoodItemsRecyclerViewAdapter;
import io.github.abhishekwl.flavr_admin.Helpers.ApiClient;
import io.github.abhishekwl.flavr_admin.Helpers.ApiInterface;
import io.github.abhishekwl.flavr_admin.Models.Category;
import io.github.abhishekwl.flavr_admin.Models.Food;
import io.github.abhishekwl.flavr_admin.Models.Hotel;
import io.github.abhishekwl.flavr_admin.R;
import java.util.ArrayList;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryItemsActivity extends AppCompatActivity {

  @BindView(R.id.categoryItemsRecyclerView)
  RecyclerView categoryItemsRecyclerView;
  @BindView(R.id.categoryItemsAddButton)
  FloatingActionButton categoryItemsAddButton;
  @BindView(R.id.categoryItemsSwipeRefreshLayout)
  SwipeRefreshLayout swipeRefreshLayout;
  @BindColor(R.color.colorAccent) int colorAccent;
  @BindColor(R.color.colorPrimary) int colorPrimary;
  @BindColor(R.color.colorPrimaryDark) int colorPrimaryDark;

  private Unbinder unbinder;
  private FirebaseAuth firebaseAuth;
  private ApiInterface apiInterface;
  private Category currentCategory;
  private FoodItemsRecyclerViewAdapter foodItemsRecyclerViewAdapter;
  private ArrayList<Food> foodArrayList = new ArrayList<>();
  private Hotel currentHotel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_category_items);

    unbinder = ButterKnife.bind(CategoryItemsActivity.this);
    initializeComponents();
    initializeViews();
  }

  private void initializeComponents() {
    firebaseAuth = FirebaseAuth.getInstance();
    apiInterface = ApiClient.getRetrofit(getApplicationContext()).create(ApiInterface.class);
    currentCategory = getIntent().getParcelableExtra("CATEGORY");
    currentHotel = currentCategory.getFoodArrayList().get(0).getHotel();
  }

  private void initializeViews() {
    if (currentCategory != null) {
      Objects.requireNonNull(getSupportActionBar()).setTitle(currentCategory.getCategoryName());
      initializeRecyclerView();
    }
    swipeRefreshLayout.setOnRefreshListener(CategoryItemsActivity.this::performNetworkRequest);
  }

  private void performNetworkRequest() {
    disableUI();
    apiInterface.getAllFoodItems(currentHotel.getId(), firebaseAuth.getUid()).enqueue(
        new Callback<ArrayList<Food>>() {
          @Override
          public void onResponse(@NonNull Call<ArrayList<Food>> call, @NonNull Response<ArrayList<Food>> response) {
            foodArrayList.clear();
            for (int i=0; i<response.body().size(); i++) if (!response.body().get(i).getCategory().equalsIgnoreCase(currentCategory.getCategoryName())) response.body().remove(i);
            foodArrayList.addAll(response.body());
            swipeRefreshLayout.setRefreshing(false);
            foodItemsRecyclerViewAdapter.notifyDataSetChanged();
          }

          @Override
          public void onFailure(@NonNull Call<ArrayList<Food>> call, @NonNull Throwable t) {
            Snackbar.make(categoryItemsAddButton, t.getMessage(), Snackbar.LENGTH_SHORT).show();
          }
        });
  }

  private void initializeRecyclerView() {
    foodItemsRecyclerViewAdapter = new FoodItemsRecyclerViewAdapter(getApplicationContext(), foodArrayList);
    categoryItemsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    categoryItemsRecyclerView.setItemAnimator(new DefaultItemAnimator());
    categoryItemsRecyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
    categoryItemsRecyclerView.setHasFixedSize(true);
    categoryItemsRecyclerView.setAdapter(foodItemsRecyclerViewAdapter);
  }

  private void disableUI() {
    swipeRefreshLayout.setColorSchemeColors(colorAccent, colorPrimary, colorPrimaryDark);
    swipeRefreshLayout.setRefreshing(true);
    foodArrayList.clear();
    foodItemsRecyclerViewAdapter.notifyDataSetChanged();
  }

  @Override
  protected void onStart() {
    super.onStart();
    performNetworkRequest();
  }

  @OnClick(R.id.categoryItemsAddButton)
  public void onAddItemButtonPress() {
    Intent addItemIntent = new Intent(CategoryItemsActivity.this, AddItemActivity.class);
    addItemIntent.putExtra("CATEGORY", currentCategory);
    startActivity(addItemIntent);
  }

  @Override
  protected void onDestroy() {
    unbinder.unbind();
    super.onDestroy();
  }
}
