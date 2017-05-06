package cg.stevendende.deliciousrecipes;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import butterknife.BindView;
import butterknife.ButterKnife;
import cg.stevendende.deliciousrecipes.sync.RecipesSyncAdapter;

public class MainActivity extends AppCompatActivity {

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;


 @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        RecipesSyncAdapter.initializeSyncAdapter(this);

     swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
         @Override
         public void onRefresh() {
             RecipesSyncAdapter.syncImmediately(MainActivity.this);
         }
     });

    }
}
