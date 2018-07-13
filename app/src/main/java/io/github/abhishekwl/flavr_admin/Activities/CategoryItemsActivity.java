package io.github.abhishekwl.flavr_admin.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.google.firebase.auth.FirebaseAuth;
import io.github.abhishekwl.flavr_admin.Adapters.FoodItemsRecyclerViewAdapter;
import io.github.abhishekwl.flavr_admin.Helpers.ApiClient;
import io.github.abhishekwl.flavr_admin.Helpers.ApiInterface;
import io.github.abhishekwl.flavr_admin.Models.Category;
import io.github.abhishekwl.flavr_admin.R;
import java.util.Objects;

public class CategoryItemsActivity extends AppCompatActivity {

  @BindView(R.id.categoryItemsRecyclerView)
  RecyclerView categoryItemsRecyclerView;
  @BindView(R.id.categoryItemsAddButton)
  FloatingActionButton categoryItemsAddButton;

  private Unbinder unbinder;
  private FirebaseAuth firebaseAuth;
  private ApiInterface apiInterface;
  private Category currentCategory;
  private FoodItemsRecyclerViewAdapter foodItemsRecyclerViewAdapter;

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
  }

  private void initializeViews() {
    if (currentCategory != null) {
      Objects.requireNonNull(getSupportActionBar()).setTitle(currentCategory.getCategoryName());
      initializeRecyclerView();
    }
  }

  private void initializeRecyclerView() {
    foodItemsRecyclerViewAdapter = new FoodItemsRecyclerViewAdapter(getApplicationContext(), currentCategory.getFoodArrayList());
    categoryItemsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    categoryItemsRecyclerView.setItemAnimator(new DefaultItemAnimator());
    categoryItemsRecyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
    categoryItemsRecyclerView.setHasFixedSize(true);
    categoryItemsRecyclerView.setAdapter(foodItemsRecyclerViewAdapter);
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
