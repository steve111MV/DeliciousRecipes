package cg.stevendende.deliciousrecipes.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import butterknife.BindView;
import butterknife.ButterKnife;
import cg.stevendende.deliciousrecipes.BakingAppUtils;
import cg.stevendende.deliciousrecipes.R;
import cg.stevendende.deliciousrecipes.StepParsingAsyncTask;
import cg.stevendende.deliciousrecipes.data.RecipesContract;
import cg.stevendende.deliciousrecipes.ui.customviews.ExpandableTextLayoutMain;
import cg.stevendende.deliciousrecipes.ui.model.RecipeStep;

import static cg.stevendende.deliciousrecipes.ui.MainActivity.EXTRA_RECIPE_ID;
import static cg.stevendende.deliciousrecipes.ui.MainActivity.EXTRA_STEP_ID;
import static cg.stevendende.deliciousrecipes.ui.RecipeStepDetailsActivity.EXTRA_RECIPE_STEP;

/**
 * Created by STEVEN on 06/05/2017.
 */

public class StepDetailsFragment extends Fragment implements ExoPlayer.EventListener {

    public static final String EXTRA_RECIPE_STEP_OBJECT = "StepDetailsFragment.RecipeStep";

    public static final int LOADER_ID = 4;
    private StepDetailsFragmentInterface mListener;
    private static final String TAG = StepDetailsFragment.class.getName();

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.exoPlayer)
    SimpleExoPlayerView mPlayerView;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.stepImage)
    ImageView mImageView;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.expandable_text)
    ExpandableTextLayoutMain mDesciptionExpandableTV;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.shortDescription)
    TextView mShortDescTV;

    private SimpleExoPlayer mExoPlayer;
    private static MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;

    private RecipeStep mRecipeStep;
    private String mStepID;
    private String mRecipeID;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param stepID Parameter 1.
     * @return A new instance of fragment RecipeDetailsFragment.
     */
    public static StepDetailsFragment newInstance(String recipeID, String stepID) {
        StepDetailsFragment fragment = new StepDetailsFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_RECIPE_ID, recipeID);
        args.putString(EXTRA_STEP_ID, stepID);
        fragment.setArguments(args);
        return fragment;
    }

    public interface StepDetailsFragmentInterface {
        void onRecipeItemClick(String id, String name);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRecipeID = getArguments().getString(EXTRA_RECIPE_ID);
            mStepID = getArguments().getString(EXTRA_STEP_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.activity_recipe_step_details, container, false);
        ButterKnife.bind(this, rootview);

        if (savedInstanceState == null) {
            //get data from DataBase and display
            Cursor cursor = loadStepData(mRecipeID, mStepID, getActivity());

            /** parse the cursor to get a {@link RecipeStep } Object */
            new StepParsingAsyncTask() {
                @Override
                protected void onPostExecute(RecipeStep recipeStep) {
                    mRecipeStep = recipeStep;
                    try {
                        populateViews(recipeStep);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }.execute(cursor);
        } else {
            mRecipeStep = savedInstanceState.getParcelable(EXTRA_RECIPE_STEP);
        }
        return rootview;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            try {
                populateViews(mRecipeStep);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof StepDetailsFragmentInterface) {
            mListener = (StepDetailsFragmentInterface) context;
        } else {
            /*throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        */
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        try {
            //release the player (resource)
            releasePlayer();
            mMediaSession.setActive(false);
        } catch (NullPointerException exception) {
            exception.printStackTrace();
        } catch (Exception ex) {
            Log.e("BALog", "exolayer unlnown bug --- " + ex.getMessage());
        }

    }

    private void populateViews(RecipeStep recipeStep) throws Exception {
        // here the mRecipeStep object has an instance

        if (mRecipeStep == null) {
            Log.i("recipeStep", "null");
            return;
        }

        mShortDescTV.setText(mRecipeStep.getShortDesc());
        mDesciptionExpandableTV.setText(mRecipeStep.getDesc());


        try {
            int MINIMUM_URL_LENGTH = 4;

            if (mRecipeStep.getVideoUrl() != null && !mRecipeStep.getVideoUrl().isEmpty() && mRecipeStep.getVideoUrl().length() > MINIMUM_URL_LENGTH) {

                mImageView.setVisibility(View.GONE);
                mPlayerView.setVisibility(View.VISIBLE);

                loadVideo(mRecipeStep.getVideoUrl());
            } else if (mRecipeStep.getThumbnailUrl() != null && !mRecipeStep.getThumbnailUrl().isEmpty() && mRecipeStep.getThumbnailUrl().length() > MINIMUM_URL_LENGTH) {

                mImageView.setVisibility(View.VISIBLE);
                mPlayerView.setVisibility(View.GONE);

                loadImage(mRecipeStep.getThumbnailUrl());
            } else {
                mImageView.setVisibility(View.GONE);
                mPlayerView.setVisibility(View.GONE);
            }
        } catch (OutOfMemoryError ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Initialize ExoPlayer.
     *
     * @param mediaUri The URI of the sample to play.
     */
    private void initializePlayer(Uri mediaUri) {
        if (mExoPlayer == null) {
            // Create an instance of the ExoPlayer.
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(getActivity(), trackSelector, loadControl);
            mPlayerView.setPlayer(mExoPlayer);

            // Set the ExoPlayer.EventListener to this activity.
            mExoPlayer.addListener(this);

            // Prepare the MediaSource.
            String userAgent = Util.getUserAgent(getActivity(), "ClassicalMusicQuiz");
            MediaSource mediaSource = new ExtractorMediaSource(mediaUri, new DefaultDataSourceFactory(
                    getActivity(), userAgent), new DefaultExtractorsFactory(), null, null);
            mExoPlayer.prepare(mediaSource);
            mExoPlayer.setPlayWhenReady(true);
        }
    }

    void releasePlayer() throws Exception {
        mExoPlayer.stop();
        mExoPlayer.release();
        mExoPlayer = null;
    }

    private void loadVideo(String videoUrl) throws OutOfMemoryError {
        // Initialize the Media Session.
        initializeMediaSession();

        // Initialize the player.
        initializePlayer(Uri.parse("http://192.168.43.163/udacity/test.mp4"));
        //initializePlayer(Uri.parse(videoUrl));
    }

    private void loadImage(String imageUrl) {
        Glide.with(getActivity())
                .load(imageUrl)
                .centerCrop()
                .crossFade()
                .into(mImageView);
    }

    /**
     * Initializes the Media Session to be enabled with media buttons, transport controls, callbacks
     * and media controller.
     */
    private void initializeMediaSession() {

        // Create a MediaSessionCompat.
        mMediaSession = new MediaSessionCompat(getActivity(), TAG);

        // Enable callbacks from MediaButtons and TransportControls.
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Do not let MediaButtons restart the player when the app is not visible.
        mMediaSession.setMediaButtonReceiver(null);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player.
        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);

        mMediaSession.setPlaybackState(mStateBuilder.build());


        // MySessionCallback has methods that handle callbacks from a media controller.
        mMediaSession.setCallback(new MySessionCallback());

        // Start the Media Session since the activity is active.
        mMediaSession.setActive(true);

    }

    public static Cursor loadStepData(String recipeID, String stepID, Context context) {

        //build query with parameters
        String selection = RecipesContract.RecipeStepEntry.COLUMN_RECIPE_ID + " = ? AND "
                + RecipesContract.RecipeStepEntry._ID + " = ?";

        String[] selectionArgs = new String[]{recipeID, stepID};

        //build the query
        return context.getContentResolver()
                .query(RecipesContract.RecipeStepEntry.CONTENT_URI,
                        RecipesContract.RecipeStepEntry.COLUMNS_STEP_DETAILS,
                        selection,
                        selectionArgs, null);

    }

    //**************START EXO PLAYER CALLBACKS*****************************************//

    /**
     * Called when the timeline and/or manifest has been refreshed.
     * <p>
     * Note that if the timeline has changed then a position discontinuity may also have occurred.
     * For example the current period index may have changed as a result of periods being added or
     * removed from the timeline. The will <em>not</em> be reported via a separate call to
     * {@link #onPositionDiscontinuity()}.
     *
     * @param timeline The latest timeline. Never null, but may be empty.
     * @param manifest The latest manifest. May be null.
     */
    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    /**
     * Called when the available or selected tracks change.
     *
     * @param trackGroups     The available tracks. Never null, but may be of length zero.
     * @param trackSelections The track selections for each {@link com.google.android.exoplayer2.Renderer}. Never null and always
     *                        of length {@link /getRendererCount()}, but may contain null elements.
     */
    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    /**
     * Called when the player starts or stops loading the source.
     *
     * @param isLoading Whether the source is currently being loaded.
     */
    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    /**
     * Called when the value returned from either {@link #/getPlayWhenReady()} or
     * {@link #/getPlaybackState()} changes.
     *
     * @param playWhenReady Whether playback will proceed when ready.
     * @param playbackState One of the {@code STATE} constants defined in the {@link ExoPlayer}
     */
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if ((playbackState == ExoPlayer.STATE_READY) && playWhenReady) {
            mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                    mExoPlayer.getCurrentPosition(), 1f);
        } else if ((playbackState == ExoPlayer.STATE_READY)) {
            mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    mExoPlayer.getCurrentPosition(), 1f);
        }
        mMediaSession.setPlaybackState(mStateBuilder.build());

        try {
            //showNotification(mStateBuilder.build());
        } catch (IllegalStateException ex) {
            //ex.printStackTrace();
        }
    }

    /**
     * Called when an error occurs. The playback state will transition to {@link #/STATE_IDLE}
     * immediately after this method is called. The player instance can still be used, and
     * {@link #/release()} must still be called on the player should it no longer be required.
     *
     * @param error The error.
     */
    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    /**
     * Called when a position discontinuity occurs without a change to the timeline. A position
     * discontinuity occurs when the current window or period index changes (as a result of playback
     * transitioning from one period in the timeline to the next), or when the playback position
     * jumps within the period currently being played (as a result of a seek being performed, or
     * when the source introduces a discontinuity internally).
     * <p>
     * When a position discontinuity occurs as a result of a change to the timeline this method is
     * <em>not</em> called. {@link #onTimelineChanged(Timeline, Object)} is called in this case.
     */
    @Override
    public void onPositionDiscontinuity() {

    }
    //**************END*****************************************//


    /**
     * Media Session Callbacks, where all external clients control the player.
     */
    private class MySessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            mExoPlayer.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            mExoPlayer.setPlayWhenReady(false);
        }

        @Override
        public void onSkipToPrevious() {
            mExoPlayer.seekTo(0);
        }
    }


    /**
     * Broadcast Receiver registered to receive the MEDIA_BUTTON intent coming from clients.
     */
    public static class MediaReceiver extends BroadcastReceiver {

        public MediaReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            MediaButtonReceiver.handleIntent(mMediaSession, intent);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putParcelable(EXTRA_RECIPE_STEP, mRecipeStep);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }
}
