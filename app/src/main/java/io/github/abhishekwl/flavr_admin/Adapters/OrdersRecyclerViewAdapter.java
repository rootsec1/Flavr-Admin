package io.github.abhishekwl.flavr_admin.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import io.github.abhishekwl.flavr_admin.Helpers.ApiClient;
import io.github.abhishekwl.flavr_admin.Helpers.ApiInterface;
import io.github.abhishekwl.flavr_admin.Models.Food;
import io.github.abhishekwl.flavr_admin.Models.Order;
import io.github.abhishekwl.flavr_admin.R;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrdersRecyclerViewAdapter extends RecyclerView.Adapter<OrdersRecyclerViewAdapter.OrderViewHolder> {

  private ArrayList<Order> orderArrayList;
  private Context rootContext;
  private String currencyCode;
  private ApiInterface apiInterface;
  private MaterialDialog materialDialog;

  public OrdersRecyclerViewAdapter(Context rootContext, ArrayList<Order> orderArrayList) {
    this.rootContext = rootContext;
    this.orderArrayList = orderArrayList;
    this.currencyCode = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
    this.apiInterface = ApiClient.getRetrofit(rootContext).create(ApiInterface.class);
  }

  @NonNull
  @Override
  public OrdersRecyclerViewAdapter.OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_list_item, parent, false);
    return new OrderViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(@NonNull OrdersRecyclerViewAdapter.OrderViewHolder holder, int position) {
    Order order = orderArrayList.get(position);
    holder.bind(holder.itemView.getContext(), order);
  }

  @Override
  public int getItemCount() {
    return orderArrayList.size();
  }

  class OrderViewHolder extends ViewHolder {

    @BindView(R.id.orderListItemClientProfilePictureImageView)
    ImageView clientProfilePictureImageView;  //TODO: Store image of client in DB
    @BindView(R.id.orderListItemClientNameTextView)
    TextView clientNameTextView;
    @BindView(R.id.orderListItemGrandTotalTextView)
    TextView clientTotalCostTextView;
    @BindView(R.id.orderListItemFoodItemsRecyclerView)
    RecyclerView foodItemsRecyclerView;
    @BindView(R.id.orderListItemCardView)
    CardView orderItemCardView;

    private OrdersListItemFoodItemsRecyclerViewAdapter ordersListItemFoodItemsRecyclerViewAdapter;

    OrderViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);

      foodItemsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
      foodItemsRecyclerView.setHasFixedSize(true);
      foodItemsRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    public void bind(Context context, Order order) {
      Glide.with(context).load(R.drawable.baseline_person_24).into(clientProfilePictureImageView);
      double totalCost = 0;
      clientNameTextView.setText(order.getUser().getName());
      for (Food food: order.getFoodArrayList()) totalCost+=food.getCost();
      clientTotalCostTextView.setText(currencyCode.concat(" ").concat(Double.toString(totalCost)));
      ordersListItemFoodItemsRecyclerViewAdapter = new OrdersListItemFoodItemsRecyclerViewAdapter(order.getFoodArrayList());
      foodItemsRecyclerView.setAdapter(ordersListItemFoodItemsRecyclerViewAdapter);

      orderItemCardView.setOnClickListener(v -> {
        materialDialog = new MaterialDialog.Builder(rootContext)
            .title(R.string.app_name)
            .items(R.array.order_list_item_menu)
            .titleColor(Color.BLACK)
            .contentColor(Color.BLACK)
            .negativeText("CANCEL")
            .itemsCallbackSingleChoice(0, (dialog, itemView, which, text) -> {
              switch (which) {
                case 0: deleteOrder(order, "Order completed"); break;
                case 1: deleteOrder(order, "Order has been deleted."); break;
                case 2: callUser(order); break;
                case 3: viewProfilePicture(order); break;
              }
              return true;
            })
            .positiveText("OKAY")
            .show();
      });
    }
  }

  private void deleteOrder(Order order, String message) {
    apiInterface.deleteOrder(order.getId()).enqueue(new Callback<Order>() {
      @Override
      public void onResponse(@NonNull Call<Order> call, @NonNull Response<Order> response) {
        if (!TextUtils.isEmpty(response.body().getId())) {
          if (message.equalsIgnoreCase("Order completed. Notifying customer.")) {
            notifyCustomer(order);
            Toast.makeText(rootContext, message, Toast.LENGTH_SHORT).show();
          } else {
            materialDialog = new MaterialDialog.Builder(rootContext)
                .title(R.string.app_name)
                .content(message)
                .positiveText("OKAY")
                .contentColor(Color.BLACK)
                .titleColor(Color.BLACK)
                .show();
          }
        }
      }

      @Override
      public void onFailure(@NonNull Call<Order> call, @NonNull Throwable t) {
        materialDialog = new MaterialDialog.Builder(rootContext)
            .title(R.string.app_name)
            .content(t.getMessage())
            .contentColor(Color.BLACK)
            .titleColor(Color.BLACK)
            .show();
      }
    });
  }

  private void notifyCustomer(Order order) {

  }

  private void viewProfilePicture(Order order) {

  }

  @SuppressLint("MissingPermission")
  private void callUser(Order order) {
    rootContext.startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:".concat(order.getUser().getPhone()))));
  }
}
