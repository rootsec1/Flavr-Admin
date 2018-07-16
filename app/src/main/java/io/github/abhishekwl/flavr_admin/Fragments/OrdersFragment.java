package io.github.abhishekwl.flavr_admin.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import io.github.abhishekwl.flavr_admin.Adapters.OrdersRecyclerViewAdapter;
import io.github.abhishekwl.flavr_admin.Helpers.ApiClient;
import io.github.abhishekwl.flavr_admin.Helpers.ApiInterface;
import io.github.abhishekwl.flavr_admin.Models.Hotel;
import io.github.abhishekwl.flavr_admin.Models.Order;
import io.github.abhishekwl.flavr_admin.R;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class OrdersFragment extends Fragment {

  @BindView(R.id.ordersFragmentRecyclerView)
  RecyclerView ordersFragmentRecyclerView;
  @BindView(R.id.ordersFragmentProgressBar)
  ProgressBar progressBar;

  private View rootView;
  private Unbinder unbinder;
  private ApiInterface apiInterface;
  private FirebaseAuth firebaseAuth;
  private DatabaseReference databaseReference;
  private ArrayList<Order> orderArrayList = new ArrayList<>();
  private OrdersRecyclerViewAdapter ordersRecyclerViewAdapter;
  private Vibrator vibrator;

  public OrdersFragment() {
    // Required empty public constructor
  }


  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    rootView = inflater.inflate(R.layout.fragment_orders, container, false);
    unbinder = ButterKnife.bind(this, rootView);
    initializeComponents();
    initializeViews();
    return rootView;
  }

  private void initializeViews() {
    initializeRecyclerView();
    listenForDbChanges();
  }

  private void listenForDbChanges() {
    databaseReference.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        performNetworkRequest();
      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

      }
    });
  }

  private void performNetworkRequest() {
    progressBar.setVisibility(View.VISIBLE);
    apiInterface.getHotel(firebaseAuth.getUid()).enqueue(new Callback<Hotel>() {
      @Override
      public void onResponse(@NonNull Call<Hotel> call, @NonNull Response<Hotel> response) {
        apiInterface.getAllOrderItems(response.body().getId(), firebaseAuth.getUid()).enqueue(
            new Callback<ArrayList<Order>>() {
              @Override
              public void onResponse(@NonNull Call<ArrayList<Order>> call, @NonNull Response<ArrayList<Order>> response) {
                orderArrayList.clear();
                orderArrayList.addAll(response.body());
                ordersRecyclerViewAdapter.notifyDataSetChanged();
                if (orderArrayList.isEmpty()) notifyMessage("No orders yet :|");
                else {
                  notifyMessage("Orders updated");
                  vibrator.vibrate(500);
                }
              }

              @Override
              public void onFailure(@NonNull Call<ArrayList<Order>> call, @NonNull Throwable t) {
                notifyMessage("There has been an error fetching your orders.");
              }
            });
      }

      @Override
      public void onFailure(@NonNull Call<Hotel> call, @NonNull Throwable t) {
        notifyMessage(t.getMessage());
      }
    });
  }

  private void initializeRecyclerView() {
    ordersFragmentRecyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
    ordersFragmentRecyclerView.setItemAnimator(new DefaultItemAnimator());
    ordersFragmentRecyclerView.setHasFixedSize(true);
    ordersRecyclerViewAdapter = new OrdersRecyclerViewAdapter(rootView.getContext(), orderArrayList);
    ordersFragmentRecyclerView.setAdapter(ordersRecyclerViewAdapter);
  }

  private void initializeComponents() {
    firebaseAuth = FirebaseAuth.getInstance();
    apiInterface = ApiClient.getRetrofit(rootView.getContext()).create(ApiInterface.class);
    databaseReference = FirebaseDatabase.getInstance().getReference("hotels/"+firebaseAuth.getUid()+"/count");
    vibrator = (Vibrator) rootView.getContext().getSystemService(Context.VIBRATOR_SERVICE);
  }

  private void notifyMessage(String message) {
    if (progressBar!=null && progressBar.getVisibility()==View.VISIBLE) progressBar.setVisibility(View.GONE);
    Snackbar.make(ordersFragmentRecyclerView, message, Snackbar.LENGTH_SHORT).show();
  }

  @Override
  public void onStart() {
    super.onStart();
    unbinder = ButterKnife.bind(this, rootView);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }
}
