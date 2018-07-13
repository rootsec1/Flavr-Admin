package io.github.abhishekwl.flavr_admin.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import io.github.abhishekwl.flavr_admin.Helpers.ApiClient;
import io.github.abhishekwl.flavr_admin.Helpers.ApiInterface;
import io.github.abhishekwl.flavr_admin.Models.Food;
import io.github.abhishekwl.flavr_admin.R;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodItemsRecyclerViewAdapter extends RecyclerView.Adapter<FoodItemsRecyclerViewAdapter.FoodItemViewHolder> {

  private Context rootContext;
  private ArrayList<Food> foodArrayList;
  private String currencyCode;
  private ApiInterface apiInterface;

  public FoodItemsRecyclerViewAdapter(Context context, ArrayList<Food> foodArrayList) {
    this.rootContext = context;
    this.foodArrayList = foodArrayList;
    this.currencyCode = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
    this.apiInterface = ApiClient.getRetrofit(context).create(ApiInterface.class);
  }

  @NonNull
  @Override
  public FoodItemsRecyclerViewAdapter.FoodItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_list_item, parent, false);
    return new FoodItemViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(@NonNull FoodItemsRecyclerViewAdapter.FoodItemViewHolder holder, int position) {
    Food food = foodArrayList.get(position);
    holder.bind(holder.itemView.getContext(), food, position);
  }

  @Override
  public int getItemCount() {
    return foodArrayList.size();
  }

  class FoodItemViewHolder extends ViewHolder {

    @BindView(R.id.foodListItemLinearLayout)
    LinearLayout rootLinearLayout;
    @BindView(R.id.foodListItemNameTextView)
    TextView itemNameTextView;
    @BindView(R.id.foodListItemCostTextView)
    TextView itemCostTextView;
    @BindView(R.id.foodListItemImageView)
    ImageView itemImageView;
    @BindColor(android.R.color.black) int colorBlack;
    @BindColor(R.color.colorAccent) int colorAccent;

    FoodItemViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    void bind(Context context, Food food, int position) {
      Glide.with(context).load(food.getImage()).into(itemImageView);
      itemNameTextView.setText(food.getName());
      itemCostTextView.setText(currencyCode.concat(" ").concat(Double.toString(food.getCost())));

      rootLinearLayout.setOnLongClickListener(v -> {
        Snackbar.make(itemImageView, "Are you sure you want to delete "+food.getName()+" from your menu?", Snackbar.LENGTH_LONG)
            .setActionTextColor(colorAccent)
            .setAction("YES", v1 -> {
              deleteFoodItemFromMenu(food, position, itemImageView);
            }).show();
        return true;
      });
    }
  }

  private void deleteFoodItemFromMenu(Food food, int position, ImageView itemImageView) {
    apiInterface.deleteFoodItem(food.getId()).enqueue(new Callback<Food>() {
      @Override
      public void onResponse(@NonNull Call<Food> call, @NonNull Response<Food> response) {
        if (response.body()!=null) {
          foodArrayList.remove(position);
          notifyDataSetChanged();
          notifyMessage("Menu updated", itemImageView);
        }
      }

      @Override
      public void onFailure(@NonNull Call<Food> call, @NonNull Throwable t) {
        notifyMessage(t.getMessage(), itemImageView);
      }
    });
  }

  private void notifyMessage(String message, View view) {
    Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
  }
}
