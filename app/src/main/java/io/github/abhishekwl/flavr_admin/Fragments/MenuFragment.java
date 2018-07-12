package io.github.abhishekwl.flavr_admin.Fragments;


import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
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
import io.github.abhishekwl.flavr_admin.Adapters.CategoriesRecyclerViewAdapter;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class MenuFragment extends Fragment {

  @BindView(R.id.menuFragmentRecyclerView)
  RecyclerView menuFragmentRecyclerView;
  @BindView(R.id.menuFragmentAddItemButton)
  FloatingActionButton menuFragmentAddItemButton;
  @BindView(R.id.menuFragmentProgressBar)
  ProgressBar menuFragmentProgressBar;

  private View rootView;
  private Unbinder unbinder;
  private CategoriesRecyclerViewAdapter categoriesRecyclerViewAdapter;
  private ArrayList<Category> categoryArrayList = new ArrayList<>();
  private ApiInterface apiInterface;
  private FirebaseAuth firebaseAuth;

  public MenuFragment() {
    // Required empty public constructor
  }


  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    rootView = inflater.inflate(R.layout.fragment_menu, container, false);
    unbinder = ButterKnife.bind(this, rootView);
    initializeComponents();
    initializeViews();
    return rootView;
  }

  private void initializeComponents() {
    firebaseAuth = FirebaseAuth.getInstance();
    apiInterface = ApiClient.getRetrofit(rootView.getContext()).create(ApiInterface.class);
  }

  private void initializeViews() {
    initializeRecyclerView();
    fetchHotel();
  }

  private void fetchHotel() {
    apiInterface.getHotel(firebaseAuth.getUid()).enqueue(new Callback<Hotel>() {
      @Override
      public void onResponse(@NonNull Call<Hotel> call, @NonNull Response<Hotel> response) {
        if (response.body()!=null) fetchFoodItemsFromHotel(Objects.requireNonNull(response.body()));
        else notifyMessage("There has been an error contacting our servers.");
      }

      @Override
      public void onFailure(@NonNull Call<Hotel> call, @NonNull Throwable t) {
        notifyMessage(t.getMessage());
      }
    });
  }

  private void fetchFoodItemsFromHotel(Hotel hotel) {
    apiInterface.getAllFoodItems(hotel.getId(), firebaseAuth.getUid()).enqueue(new Callback<ArrayList<Food>>() {
          @SuppressWarnings("unchecked")
          @Override
          public void onResponse(@NonNull Call<ArrayList<Food>> call, @NonNull Response<ArrayList<Food>> response) {
            if (response.body()!=null) new ComputeCategoriesArrayList().execute(response.body());
            else notifyMessage("Error fetching food items belonging to your place :(");
          }

          @Override
          public void onFailure(@NonNull Call<ArrayList<Food>> call, @NonNull Throwable t) {
            notifyMessage(t.getMessage());
          }
        });
  }

  private void initializeRecyclerView() {
    categoriesRecyclerViewAdapter = new CategoriesRecyclerViewAdapter(rootView.getContext(), categoryArrayList);
    menuFragmentRecyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
    menuFragmentRecyclerView.setItemAnimator(new DefaultItemAnimator());
    menuFragmentRecyclerView.addItemDecoration(new DividerItemDecoration(rootView.getContext(), DividerItemDecoration.VERTICAL));
    menuFragmentRecyclerView.setHasFixedSize(true);
    menuFragmentRecyclerView.setAdapter(categoriesRecyclerViewAdapter);
  }

  @SuppressLint("StaticFieldLeak")
  private class ComputeCategoriesArrayList extends AsyncTask<ArrayList<Food>, Void, ArrayList<Category>> {

    private String[] categoryNamesArray;

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      categoryNamesArray = getResources().getStringArray(R.array.categories);
    }

    @Override
    protected ArrayList<Category> doInBackground(ArrayList<Food>... arrayLists) {
      ArrayList<Category> categoryArrayList = new ArrayList<>();
      for (String categoryName: categoryNamesArray) {
        Category category = getItemThatBelongToCategory(categoryName, arrayLists[0]);
        if (category!=null) categoryArrayList.add(category);
      }
      return categoryArrayList;
    }

    @Override
    protected void onPostExecute(ArrayList<Category> categoryArrayListFiltered) {
      super.onPostExecute(categoryArrayListFiltered);
      categoryArrayList.clear();
      categoryArrayList.addAll(categoryArrayListFiltered);
      notifyMessage("Fetched food categories.");
      categoriesRecyclerViewAdapter.notifyDataSetChanged();
    }

    private Category getItemThatBelongToCategory(String categoryName, ArrayList<Food> arrayList) {
      ArrayList<Food> foodArrayList = new ArrayList<>();
      for (Food food: arrayList) if (food.getCategory().equalsIgnoreCase(categoryName)) foodArrayList.add(food);
      if (foodArrayList.isEmpty()) return null;
      else return new Category(foodArrayList);
    }
  }

  private void notifyMessage(String message) {
    if (menuFragmentProgressBar.getVisibility()==View.VISIBLE) menuFragmentProgressBar.setVisibility(View.GONE);
    //Snackbar.make(menuFragmentAddItemButton, message, Snackbar.LENGTH_SHORT).show();
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
