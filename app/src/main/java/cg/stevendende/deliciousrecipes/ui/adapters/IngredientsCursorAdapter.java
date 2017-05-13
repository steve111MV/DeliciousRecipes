package cg.stevendende.deliciousrecipes.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cg.stevendende.deliciousrecipes.R;
import cg.stevendende.deliciousrecipes.data.RecipesContract;

/**
 * Created by STEVEN on 13/05/2017.
 */

public class IngredientsCursorAdapter extends RecyclerViewCursorAdapter<IngredientsCursorAdapter.MyViewHolder> {

    public IngredientsCursorAdapter(Cursor cursor) {
        super(cursor);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ingredient_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(MyViewHolder holder, Cursor cursor) {

        holder.tvQuantity.setText(
                cursor.getDouble(
                        RecipesContract.IngredientEntry.INDEX_QUANTITY) + "");
        holder.tvMeasure.setText(cursor.getString(RecipesContract.IngredientEntry.INDEX_MEASURE));
        holder.tvIngredient.setText(cursor.getString(RecipesContract.IngredientEntry.INDEX_INGREDIENT));

    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.quantity)
        TextView tvQuantity;
        @BindView(R.id.measure)
        TextView tvMeasure;
        @BindView(R.id.ingredient)
        TextView tvIngredient;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
