package cg.stevendende.deliciousrecipes.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import cg.stevendende.deliciousrecipes.R;

/**
 * Created by STEVEN on 06/05/2017.
 */

public class RecipesFragment extends Fragment {

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_recipes, container, false);
        ButterKnife.bind(this, rootview);

        return rootview;
    }
}
