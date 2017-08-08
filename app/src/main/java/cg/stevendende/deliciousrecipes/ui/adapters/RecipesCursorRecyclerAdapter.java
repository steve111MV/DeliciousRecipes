package cg.stevendende.deliciousrecipes.ui.adapters;

import android.content.res.TypedArray;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cg.stevendende.deliciousrecipes.R;
import cg.stevendende.deliciousrecipes.data.RecipesContract;

/**
 * Created by STEVEN on 07/05/2017.
 */

public class RecipesCursorRecyclerAdapter extends RecyclerViewCursorAdapter<RecyclerView.ViewHolder> {

    static private RecipesAdapterInteractionInterface mCallback;

    public RecipesCursorRecyclerAdapter() {
        super(null);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(RecyclerView.ViewHolder holder, Cursor cursor) {
        //int itemViewType = getItemViewType(cursor.getPosition());

        MyViewHolder myHolder = (MyViewHolder) holder;

        myHolder.recipeId = cursor.getInt(RecipesContract.RecipeEntry.INDEX_ID) + "";
        myHolder.recipeName = cursor.getString(RecipesContract.RecipeEntry.INDEX_NAME);
        //if there's a Thumbnail
        if (myHolder.hasThumbnail) {
            myHolder.imageContainer.setVisibility(View.VISIBLE);
            myHolder.tvNameWithImage.setText(cursor.getString(RecipesContract.RecipeEntry.INDEX_NAME));

            //content description for TalkBack (Android Acessibilities)
            myHolder.tvNameWithImage.setContentDescription(cursor.getString(RecipesContract.RecipeEntry.INDEX_NAME));
        } else {
            myHolder.imageContainer.setVisibility(View.GONE);
            myHolder.tvName.setText(cursor.getString(RecipesContract.RecipeEntry.INDEX_NAME));

            myHolder.tvName.setContentDescription(cursor.getString(RecipesContract.RecipeEntry.INDEX_NAME));
        }

    }

    public void setCallbackListener(RecipesAdapterInteractionInterface callbackInterface) {
        mCallback = callbackInterface;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.recipeImageContainer)
        RelativeLayout imageContainer;

        @BindView(R.id.nameWithImage)
        TextView tvNameWithImage;

        @BindView(R.id.name)
        TextView tvName;

        @BindView(R.id.image)
        ImageView image;

        String recipeName, recipeId;

        //presntly using test URL
        String imageUrl = "https://d17h27t6h515a5.cloudfront.net/topher/2017/April/58ffd974_-intro-creampie/-intro-creampie.mp4";
        boolean hasThumbnail = false;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            if (mCallback != null) mCallback.onItemClick(recipeId, recipeName);
        }
    }

    /*
    private Bitmap getVieoThumb(String url){
        final int FRAMES = 2000000;

        FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
        mmr.setDataSource( url);
        mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM);
        mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST);
        Bitmap b = mmr.getFrameAtTime(FRAMES, FFmpegMediaMetadataRetriever.OPTION_CLOSEST); // frame at 2 seconds
        //byte [] artwork = mmr.getEmbeddedPicture();
        //mmr.release();

        return b;
    } */

    public interface RecipesAdapterInteractionInterface {
        void onItemClick(String itemId, String itemName);
    }
}
