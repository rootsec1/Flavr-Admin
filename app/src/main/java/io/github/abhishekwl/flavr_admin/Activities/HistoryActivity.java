package io.github.abhishekwl.flavr_admin.Activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.github.abhishekwl.flavr_admin.R;

public class HistoryActivity extends AppCompatActivity {

  @BindView(R.id.historyRecyclerView)
  RecyclerView historyRecyclerView;
  @BindView(R.id.historyDeleteButton)
  FloatingActionButton historyDeleteButton;

  private Unbinder unbinder;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_history);

    unbinder = ButterKnife.bind(HistoryActivity.this);
    initializeComponents();
    initializeViews();
  }

  private void initializeComponents() {

  }

  private void initializeViews() {

  }

}
