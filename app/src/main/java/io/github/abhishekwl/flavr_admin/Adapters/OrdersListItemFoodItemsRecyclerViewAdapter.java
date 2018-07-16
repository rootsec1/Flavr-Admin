package io.github.abhishekwl.flavr_admin.Adapters;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.abhishekwl.flavr_admin.Models.Food;
import io.github.abhishekwl.flavr_admin.R;
import java.util.ArrayList;

public class OrdersListItemFoodItemsRecyclerViewAdapter extends RecyclerView.Adapter<OrdersListItemFoodItemsRecyclerViewAdapter.FoodItemViewHolder> {

  private ArrayList<Food> foodArrayList;

  OrdersListItemFoodItemsRecyclerViewAdapter(ArrayList<Food> foodArrayList) {
    this.foodArrayList = foodArrayList;
  }

  @NonNull
  @Override
  public OrdersListItemFoodItemsRecyclerViewAdapter.FoodItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_list_item_food_items_list_item, parent, false);
    return new FoodItemViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(@NonNull OrdersListItemFoodItemsRecyclerViewAdapter.FoodItemViewHolder holder, int position) {
    Food food = foodArrayList.get(position);
    holder.bind(food, position);
  }

  @Override
  public int getItemCount() {
    return foodArrayList.size();
  }

  public class FoodItemViewHolder extends ViewHolder {

    @BindView(R.id.orderListItemFoodItemIndexTextView)
    TextView indexTextView;
    @BindView(R.id.orderListItemFoodItemListItemNameTextView)
    TextView itemNameTextView;

    FoodItemViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    @SuppressLint("SetTextI18n")
    public void bind(Food food, int index) {
      itemNameTextView.setText(food.getName());
      indexTextView.setText(Integer.toString(index+1).concat("."));
    }
  }
}
