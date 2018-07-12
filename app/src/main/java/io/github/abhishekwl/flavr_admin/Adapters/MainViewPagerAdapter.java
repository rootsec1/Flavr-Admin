package io.github.abhishekwl.flavr_admin.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import io.github.abhishekwl.flavr_admin.Fragments.AnalyticsFragment;
import io.github.abhishekwl.flavr_admin.Fragments.MenuFragment;
import io.github.abhishekwl.flavr_admin.Fragments.OrdersFragment;
import io.github.abhishekwl.flavr_admin.Fragments.ProfileFragment;

public class MainViewPagerAdapter extends FragmentPagerAdapter {

  public MainViewPagerAdapter(FragmentManager fm) {
    super(fm);
  }

  @Override
  public Fragment getItem(int i) {
    switch (i) {
      case 0: return new OrdersFragment();
      case 1: return new MenuFragment();
      case 2: return new AnalyticsFragment();
      default: return new ProfileFragment();
    }
  }

  @Override
  public int getCount() {
    return 4;
  }
}
