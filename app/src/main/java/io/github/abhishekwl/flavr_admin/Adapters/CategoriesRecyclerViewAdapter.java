package io.github.abhishekwl.flavr_admin.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import io.github.abhishekwl.flavr_admin.Activities.CategoryItemsActivity;
import io.github.abhishekwl.flavr_admin.Models.Category;
import io.github.abhishekwl.flavr_admin.Models.Food;
import io.github.abhishekwl.flavr_admin.R;
import java.util.ArrayList;

public class CategoriesRecyclerViewAdapter extends RecyclerView.Adapter<CategoriesRecyclerViewAdapter.ItemViewHolder> {

  private ArrayList<Category> categoryArrayList;
  private Context context;
  private LayoutInflater layoutInflater;

  public CategoriesRecyclerViewAdapter(Context context, ArrayList<Category> categoryArrayList) {
    this.context = context;
    this.categoryArrayList = categoryArrayList;
    this.layoutInflater = LayoutInflater.from(context);
  }

  @NonNull
  @Override
  public CategoriesRecyclerViewAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View itemView = layoutInflater.inflate(R.layout.category_list_item, parent, false);
    return new ItemViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(@NonNull CategoriesRecyclerViewAdapter.ItemViewHolder holder, int position) {
    Category category = categoryArrayList.get(position);
    holder.bind(holder.itemView.getContext(), category);
  }

  @Override
  public int getItemCount() {
    return categoryArrayList.size();
  }

  class ItemViewHolder extends ViewHolder {

    @BindView(R.id.categoryListItemImageView)
    ImageView categoryImageView;
    @BindView(R.id.categoryListItemCategoryNameTextView)
    TextView categoryNameTextView;
    @BindView(R.id.categoryListItemCategoryItemsTextView)
    TextView categoryItemsTextView;
    @BindView(R.id.categoryListItemRootLayout)
    LinearLayout categoryRootLayout;

    ItemViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    void bind(Context context, Category category) {
      Glide.with(context).load(category.getCategoryImageUrl()).into(categoryImageView);
      categoryNameTextView.setText(category.getCategoryName());
      StringBuilder items = new StringBuilder();
      for (Food food: category.getFoodArrayList()) items.append(food.getName()).append(", ");
      String categoryItems = items.toString().trim();
      categoryItems = categoryItems.substring(0, categoryItems.length()-1);
      categoryItemsTextView.setText(categoryItems);

      categoryRootLayout.setOnClickListener(v -> {
        Intent categoryItemsIntent = new Intent(context, CategoryItemsActivity.class);
        categoryItemsIntent.putExtra("CATEGORY", category);
        context.startActivity(categoryItemsIntent);
      });
    }
  }
}
