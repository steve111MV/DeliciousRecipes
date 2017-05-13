package cg.stevendende.deliciousrecipes.ui.adapters;

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

public class StepsCursorRecyclerAdapter extends RecyclerViewCursorAdapter<RecyclerView.ViewHolder> {

    static private RecipesAdapterInteractionInterface mCallback;

    public StepsCursorRecyclerAdapter() {
        super(null);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe_steps, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(RecyclerView.ViewHolder holder, Cursor cursor) {
        //int itemViewType = getItemViewType(cursor.getPosition());

        MyViewHolder myHolder = (MyViewHolder) holder;

        //id
        myHolder.stepID = cursor.getInt(0) + "";
        //short description
        myHolder.tvName.setText(cursor.getString(1));
        myHolder.tvPosition.setText(myHolder.stepID + "");

    }

    public void setCallbackListener(RecipesAdapterInteractionInterface callbackInterface) {
        mCallback = callbackInterface;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.shortDescription)
        TextView tvName;
        @BindView(R.id.recipeStepPosition)
        TextView tvPosition;

        String stepName, stepID, stepVideo, stepImage;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            if (mCallback != null) mCallback.onItemClick();
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
        void onItemClick();
    }
}
