package io.github.abhishekwl.flavr_admin.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.OnTabSelectedListener;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.firebase.auth.FirebaseAuth;
import io.github.abhishekwl.flavr_admin.Adapters.MainViewPagerAdapter;
import io.github.abhishekwl.flavr_admin.Helpers.ApiClient;
import io.github.abhishekwl.flavr_admin.Helpers.ApiInterface;
import io.github.abhishekwl.flavr_admin.Models.Hotel;
import io.github.abhishekwl.flavr_admin.R;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

  @BindView(R.id.mainTabLayout) TabLayout mainTabLayout;
  @BindView(R.id.mainViewPager) ViewPager mainViewPager;
  @BindDrawable(R.drawable.baseline_fastfood_24) Drawable foodDrawable;
  @BindDrawable(R.drawable.baseline_restaurant_menu_24) Drawable menuDrawable;
  @BindDrawable(R.drawable.baseline_trending_up_24) Drawable analyticsDrawable;
  @BindDrawable(R.drawable.baseline_person_24) Drawable personDrawable;
  @BindColor(android.R.color.white) int colorWhite;
  @BindColor(R.color.colorBackgroundDark) int colorDarkWhite;
  @BindColor(R.color.colorBackgroundLight) int colorLightWhite;
  @BindColor(R.color.colorAccent) int colorAccent;

  private Unbinder unbinder;
  private FirebaseAuth firebaseAuth;
  private FusedLocationProviderClient fusedLocationProviderClient;
  public Location deviceLocation;
  private ApiInterface apiInterface;
  private Message message;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    initializeComponents();
    initializeViews();
  }

  private void initializeComponents() {
    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
    retrieveDeviceLocation();
    unbinder = ButterKnife.bind(MainActivity.this);
    firebaseAuth = FirebaseAuth.getInstance();
    apiInterface = ApiClient.getRetrofit(getApplicationContext()).create(ApiInterface.class);
    publishHotel();
  }

  @SuppressLint("MissingPermission")
  private void retrieveDeviceLocation() {
    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> deviceLocation = location);
  }

  private void initializeViews() {
    setupTabLayoutAndViewPager();
  }

  private void setupTabLayoutAndViewPager() {
    Objects.requireNonNull(getSupportActionBar()).setElevation(0f);
    mainViewPager.setAdapter(new MainViewPagerAdapter(getSupportFragmentManager()));
    mainTabLayout.setupWithViewPager(mainViewPager);
    Objects.requireNonNull(mainTabLayout.getTabAt(0)).setIcon(foodDrawable);
    Objects.requireNonNull(mainTabLayout.getTabAt(1)).setIcon(menuDrawable);
    Objects.requireNonNull(mainTabLayout.getTabAt(2)).setIcon(analyticsDrawable);
    Objects.requireNonNull(mainTabLayout.getTabAt(3)).setIcon(personDrawable);
    Objects.requireNonNull(Objects.requireNonNull(mainTabLayout.getTabAt(0)).getIcon()).setColorFilter(colorWhite, Mode.SRC_IN);
    Objects.requireNonNull(Objects.requireNonNull(mainTabLayout.getTabAt(1)).getIcon()).setColorFilter(colorDarkWhite, Mode.SRC_IN);
    Objects.requireNonNull(Objects.requireNonNull(mainTabLayout.getTabAt(2)).getIcon()).setColorFilter(colorDarkWhite, Mode.SRC_IN);
    Objects.requireNonNull(Objects.requireNonNull(mainTabLayout.getTabAt(3)).getIcon()).setColorFilter(colorDarkWhite, Mode.SRC_IN);
    mainTabLayout.addOnTabSelectedListener(new OnTabSelectedListener() {
      @Override
      public void onTabSelected(Tab tab) {
        Objects.requireNonNull(tab.getIcon()).setColorFilter(colorWhite, Mode.SRC_IN);
      }

      @Override
      public void onTabUnselected(Tab tab) {
        Objects.requireNonNull(tab.getIcon()).setColorFilter(colorDarkWhite, Mode.SRC_IN);
      }

      @Override
      public void onTabReselected(Tab tab) {

      }
    });
  }

  private void publishHotel() {
    apiInterface.getHotel(firebaseAuth.getUid()).enqueue(new Callback<Hotel>() {
      @Override
      public void onResponse(@NonNull Call<Hotel> call, @NonNull Response<Hotel> response) {
        if (response.body()==null) notifyMessage("There is no place associated with your email address.");
        else {
          String hotelId = Objects.requireNonNull(response.body()).getId();
          String uid = Objects.requireNonNull(response.body()).getUid();
          String publishString = hotelId+":"+uid;
          publishMessage(new Message(publishString.getBytes()));
        }
      }

      @Override
      public void onFailure(@NonNull Call<Hotel> call, @NonNull Throwable t) {
        Snackbar.make(mainTabLayout, t.getMessage(), Snackbar.LENGTH_SHORT).show();
      }
    });
  }

  private void publishMessage(Message message) {
    if (message!=null) {
      Strategy strategy = new Strategy.Builder()
          .setDiscoveryMode(Strategy.DISCOVERY_MODE_BROADCAST)
          .setDistanceType(Strategy.DISTANCE_TYPE_DEFAULT)
          .setTtlSeconds(Strategy.TTL_SECONDS_MAX)
          .build();
      Nearby.getMessagesClient(MainActivity.this).publish(message, new PublishOptions.Builder().setStrategy(strategy).build()).addOnCompleteListener(
          task -> {
            if (task.isSuccessful()) notifyMessage("Broadcasting hotel presence :)");
            else notifyMessage("There was an error broadcasting your place's presence :(");
          });
    }
    else notifyMessage("Error generating message to broadcast");
  }

  public Location getDeviceLocation() {
    return deviceLocation;
  }

  private void notifyMessage(String message) {
    Snackbar.make(mainTabLayout, message, Snackbar.LENGTH_SHORT).show();
  }

  @Override
  protected void onStart() {
    super.onStart();
    retrieveDeviceLocation();
    publishMessage(message);
  }

  @Override
  protected void onStop() {
    if (message!=null) Nearby.getMessagesClient(MainActivity.this).unpublish(message);
    super.onStop();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menuItemSignOut:
        firebaseAuth.signOut();
        startActivity(new Intent(MainActivity.this, SignInActivity.class));
        finish();
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onDestroy() {
    unbinder.unbind();
    super.onDestroy();
  }
}
