package cg.stevendende.deliciousrecipes.ui;

import android.annotation.SuppressLint;
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
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.DebugTextViewHelper;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import cg.stevendende.deliciousrecipes.R;
import cg.stevendende.deliciousrecipes.data.RecipesContract;
import cg.stevendende.deliciousrecipes.ui.customviews.ExpandableTextLayoutMain;
import cg.stevendende.deliciousrecipes.ui.model.RecipeStep;

import static cg.stevendende.deliciousrecipes.ui.MainActivity.EXTRA_RECIPE_ID;
import static cg.stevendende.deliciousrecipes.ui.MainActivity.EXTRA_STEP_ID;
import static cg.stevendende.deliciousrecipes.ui.RecipeStepDetailsActivity.EXTRA_RECIPE_STEP;
import static cg.stevendende.deliciousrecipes.ui.RecipeStepDetailsActivity.EXTRA_RECIPE_STEP_VIDEO_URL;

/**
 * Created by STEVEN on 06/05/2017.
 */

public class StepDetailsFragment extends Fragment
        implements ExoPlayer.EventListener, View.OnClickListener {

    public static final String EXTRA_RECIPE_STEP_OBJECT = "StepDetailsFragment.RecipeStep";

    public static final int LOADER_ID = 4;
    private static final java.lang.String SAVE_KEY_PLAY_BACK_POSITION = "saved_position";
    private static final java.lang.String SAVE_KEY_CURRENT_WINDOW = "saved_window";
    private static final java.lang.String SAVE_KEY_PLAY_WHEN_READY = "saved_autoPlay";
    private StepDetailsFragmentInterface mListener;

    private static final String TAG = StepDetailsFragment.class.getName();
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

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
    @BindView(R.id.shortDescriptionDetails)
    TextView mShortDescTV;

    /*@SuppressWarnings("WeakerAccess")
     @BindView(R.id.controls_root)
     LinearLayout debugRootView;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.debug_text_view)
    TextView debugTextView;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.retry_button)
    Button retryButton;*/

    //private SimpleExoPlayer mExoPlayer;
    private static MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;

    private RecipeStep mRecipeStep;
    private String mStepID;
    private String mRecipeID;
    private String mVideoUrl;
    private long mVideoProgression = 0;


    private DataSource.Factory mediaDataSourceFactory;
    private SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;
    //private TrackSelectionHelper trackSelectionHelper;
    private DebugTextViewHelper debugViewHelper;

    private ComponentListener componentListener;
    private boolean inErrorState;
    private TrackGroupArray lastSeenTrackGroupArray;

    private boolean shouldAutoPlay = true;
    private int resumeWindow;
    private long resumePosition;


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

        componentListener = new ComponentListener();
        if (savedInstanceState == null) {
            //get data from DataBase and display
            Cursor cursor = loadStepData(mRecipeID, mStepID, getActivity());

            if (cursor.moveToFirst()) {
                mRecipeStep = new RecipeStep();
                mRecipeStep.setId(cursor.getInt(RecipesContract.RecipeStepEntry.INDEX_ID));
                mRecipeStep.setShortDesc(cursor.getString(RecipesContract.RecipeStepEntry.INDEX_SHORT_DESC));
                mRecipeStep.setDesc(cursor.getString(RecipesContract.RecipeStepEntry.INDEX_DESCRIPTION));
                mRecipeStep.setVideoUrl(cursor.getString(RecipesContract.RecipeStepEntry.INDEX_VIDEO_URL));
                mRecipeStep.setThumbnailUrl(cursor.getString(RecipesContract.RecipeStepEntry.INDEX_THUMBNAIL_URL));
            }
        } else {
            mRecipeStep = savedInstanceState.getParcelable(EXTRA_RECIPE_STEP);
            mVideoUrl = savedInstanceState.getString(EXTRA_RECIPE_STEP_VIDEO_URL);

            resumePosition = savedInstanceState.getLong(SAVE_KEY_PLAY_BACK_POSITION);
            resumeWindow = savedInstanceState.getInt(SAVE_KEY_CURRENT_WINDOW);
            shouldAutoPlay = savedInstanceState.getBoolean(SAVE_KEY_PLAY_WHEN_READY);
        }

        try {
            populateViews(mRecipeStep);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootview;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // player.seekTo(resumeWindow, resumePosition+ 1);
        Log.e("BALog", "position: " + resumePosition);

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

        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
            Log.e("BALog", "out of memoryerror");
        }
    }

    /**
     * Initialize ExoPlayer.
     *
     * @param mediaUri The URI of the sample to play.
     */
    private void initializePlayer(Uri mediaUri) {

        if (player == null) {
            TrackSelection.Factory adaptiveTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);

            player = ExoPlayerFactory.newSimpleInstance(
                    new DefaultRenderersFactory(getActivity()),
                    new DefaultTrackSelector(adaptiveTrackSelectionFactory),
                    new DefaultLoadControl());

            player.addListener(componentListener);
            player.setVideoDebugListener(componentListener);
            player.setAudioDebugListener(componentListener);

            mPlayerView.setPlayer(player);

            player.setPlayWhenReady(shouldAutoPlay);
            player.seekTo(resumeWindow, resumePosition);

            Log.e("BALog", "player initialized");
        }

        Log.e("BALog", "player not initialized");
        //MediaSource mediaSource = buildMediaSource(mediaUri);
        String userAgent = Util.getUserAgent(getActivity(), "Backing");
        MediaSource mediaSource = new ExtractorMediaSource(mediaUri, new DefaultDataSourceFactory(
                getActivity(), userAgent), new DefaultExtractorsFactory(), null, null);
        player.prepare(mediaSource, true, false);


        player.seekTo(resumeWindow, resumePosition);

        /*
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

            boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
            if (haveResumePosition) {
                mExoPlayer.seekTo(resumeWindow, resumePosition);
            }

            mExoPlayer.prepare(mediaSource);
            mExoPlayer.setPlayWhenReady(true);

            inErrorState = false;
            //updateButtonVisibilities();

        }*/
    }

    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource(uri,
                new DefaultHttpDataSourceFactory("ua"),
                new DefaultExtractorsFactory(), null, null);
    }

    void releasePlayer() {

        if (player != null) {
            if (player.getCurrentPosition() != 0) {
                resumePosition = player.getCurrentPosition();
            }
            resumeWindow = player.getCurrentWindowIndex();
            shouldAutoPlay = player.getPlayWhenReady();
            player.removeListener(componentListener);
            player.stop();
            player.release();
            player = null;
        }

        Log.i("BALog", "position in relase: " + resumePosition);
    }

    private void loadVideo(String videoUrl) throws OutOfMemoryError {
        // Initialize the Media Session.
        initializeMediaSession();

        //mVideoUrl = "http://192.168.43.163/udacity/video.mp4";
        //mVideoUrl = "http://192.168.8.100/udacity/video.mp4";
        mVideoUrl = videoUrl;
        //Initialize the player.
        //initializePlayer(Uri.parse("http://techslides.com/demos/sample-videos/small.mp4"));

        //Log.e("BALog", "loading video, url ="+mVideoUrl);
        initializePlayer(Uri.parse(mVideoUrl));
    }

    private void loadImage(String imageUrl) {
        Glide.with(getActivity())
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
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


        Log.e("BALog", "media session initialized");
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
                    player.getCurrentPosition(), 1f);
        } else if ((playbackState == ExoPlayer.STATE_READY)) {
            mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    player.getCurrentPosition(), 1f);
        }
        mMediaSession.setPlaybackState(mStateBuilder.build());

    }


    /**
     * Called when an error occurs. The playback state will transition to {@link #/STATE_IDLE}
     * immediately after this method is called. The player instance can still be used, and
     * {@link #/release()} must still be called on the player should it no longer be required.
     *
     * @param e The error.
     */
    @Override
    public void onPlayerError(ExoPlaybackException e) {
        String errorString = null;
        if (e.type == ExoPlaybackException.TYPE_RENDERER) {
            Exception cause = e.getRendererException();
            if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                // Special case for decoder initialization failures.
                MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
                        (MediaCodecRenderer.DecoderInitializationException) cause;
                if (decoderInitializationException.decoderName == null) {
                    if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                        errorString = getString(R.string.error_querying_decoders);
                    } else if (decoderInitializationException.secureDecoderRequired) {
                        errorString = getString(R.string.error_no_secure_decoder,
                                decoderInitializationException.mimeType);
                    } else {
                        errorString = getString(R.string.error_no_decoder,
                                decoderInitializationException.mimeType);
                    }
                } else {
                    errorString = getString(R.string.error_instantiating_decoder,
                            decoderInitializationException.decoderName);
                }
            }
        }
        if (errorString != null) {
            showToast(errorString);
        }
        inErrorState = true;
        if (isBehindLiveWindow(e)) {
            clearResumePosition();
            initializePlayer(Uri.parse(mVideoUrl));
        } else {
            updateResumePosition();
            //updateButtonVisibilities();
            //showControls();
        }
    }

    private void clearResumePosition() {
        resumeWindow = C.INDEX_UNSET;
        resumePosition = C.TIME_UNSET;
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
        if (inErrorState) {
            // This will only occur if the user has performed a seek whilst in the error state. Update the
            // resume position so that if the user then retries, playback will resume from the position to
            // which they seeked.
            updateResumePosition();
        }
    }

    /**
     * Called when the current playback parameters change. The playback parameters may change due to
     * a call to {@link ExoPlayer#setPlaybackParameters(PlaybackParameters)}, or the player itself
     * may change them (for example, if audio playback switches to passthrough mode, where speed
     * adjustment is no longer possible).
     *
     * @param playbackParameters The playback parameters.
     */
    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    /**
     * Media Session Callbacks, where all external clients control the player.
     */
    private class MySessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            player.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            player.setPlayWhenReady(false);
        }

        @Override
        public void onSkipToPrevious() {
            player.seekTo(0);
        }
    }

    //**************END EXO-Player CallBacks*****************************************//

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        mPlayerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void updateResumePosition() {
        resumeWindow = player.getCurrentWindowIndex();
        resumePosition = Math.max(0, player.getCurrentPosition());
    }


    private void showToast(int messageId) {
        showToast(getString(messageId));
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    // OnClickListener methods

    @Override
    public void onClick(View view) {

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
        outState.putString(EXTRA_RECIPE_STEP_VIDEO_URL, mVideoUrl);

        outState.putBoolean(SAVE_KEY_PLAY_WHEN_READY, shouldAutoPlay);
        outState.putLong(SAVE_KEY_PLAY_BACK_POSITION, resumePosition);
        outState.putInt(SAVE_KEY_CURRENT_WINDOW, resumeWindow);

        //Get the current position of the Video
        if (player != null) {
            mVideoProgression = player.getCurrentPosition();
        }
        //outState.putLong(EXTRA_RECIPE_STEP_VIDEO_PROGRESSION, mVideoProgression);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(EXTRA_RECIPE_STEP_VIDEO_URL)) {
                mVideoUrl = savedInstanceState.getString(EXTRA_RECIPE_STEP_VIDEO_URL);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        hideSystemUi();
        if ((Util.SDK_INT <= 23 || player == null)) {
            try {
                initializePlayer(Uri.parse(mVideoUrl));
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void onDestroyView() {

        try {
            //release the player (resource)
            releasePlayer();
            mMediaSession.setActive(false);
        } catch (NullPointerException exception) {
            exception.printStackTrace();
        } catch (Exception ex) {
            Log.e("BALog", "exolayer unlnown bug --- " + ex.getMessage());
        }

        super.onDestroyView();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (Util.SDK_INT > 23 && mVideoUrl != null) {
            initializePlayer(Uri.parse(mVideoUrl));
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    private class ComponentListener implements ExoPlayer.EventListener, VideoRendererEventListener,
            AudioRendererEventListener {

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {

        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {

        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

            String stateString;
            switch (playbackState) {
                case ExoPlayer.STATE_IDLE:
                    stateString = "ExoPlayer.STATE_IDLE      -";
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    stateString = "ExoPlayer.STATE_BUFFERING -";
                    break;
                case ExoPlayer.STATE_READY:
                    stateString = "ExoPlayer.STATE_READY     -";
                    break;
                case ExoPlayer.STATE_ENDED:
                    stateString = "ExoPlayer.STATE_ENDED     -";
                    break;
                default:
                    stateString = "UNKNOWN_STATE             -";
                    break;
            }
            Log.d(TAG, "changed state to " + stateString + " playWhenReady: " + playWhenReady);

            if ((playbackState == ExoPlayer.STATE_READY) && playWhenReady) {
                mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                        player.getCurrentPosition(), 1f);
            } else if ((playbackState == ExoPlayer.STATE_READY)) {
                mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                        player.getCurrentPosition(), 1f);
            }
            mMediaSession.setPlaybackState(mStateBuilder.build());
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {

        }

        @Override
        public void onPositionDiscontinuity() {

        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }

        @Override
        public void onAudioEnabled(DecoderCounters counters) {

        }

        @Override
        public void onAudioSessionId(int audioSessionId) {

        }

        @Override
        public void onAudioDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {

        }

        @Override
        public void onAudioInputFormatChanged(Format format) {

        }

        @Override
        public void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {

        }

        @Override
        public void onAudioDisabled(DecoderCounters counters) {

        }

        @Override
        public void onVideoEnabled(DecoderCounters counters) {

        }

        @Override
        public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {

        }

        @Override
        public void onVideoInputFormatChanged(Format format) {

        }

        @Override
        public void onDroppedFrames(int count, long elapsedMs) {

        }

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

        }

        @Override
        public void onRenderedFirstFrame(Surface surface) {

        }

        @Override
        public void onVideoDisabled(DecoderCounters counters) {

        }
    }

    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

}
