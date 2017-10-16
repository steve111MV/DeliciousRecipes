import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashChunkSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
import com.nanodegree.udacy.backingapp.R;
import com.nanodegree.udacy.backingapp.data.RecipeContract;
import com.nanodegree.udacy.backingapp.model.RecipeStep;
import com.nanodegree.udacy.backingapp.utils.ExpandAndCollapseViewUtil;
import com.nanodegree.udacy.backingapp.utils.StepParsingAsyncTask;
import com.nanodegree.udacy.backingapp.utils.utils;

import java.util.EventListener;

import static com.nanodegree.udacy.backingapp.activities.MainActivity.EXTRA_RECIPE_ID;
import static com.nanodegree.udacy.backingapp.activities.MainActivity.EXTRA_STEP_ID;
import static com.nanodegree.udacy.backingapp.activities.RecipeStepDetailsActivity.EXTRA_RECIPE_STEP;
import static com.nanodegree.udacy.backingapp.utils.Constant.DURATION;
import static com.nanodegree.udacy.backingapp.utils.Constant.STEP_COLUMNS;
import static com.nanodegree.udacy.backingapp.utils.Constant.sBrowniesImage;
import static com.nanodegree.udacy.backingapp.utils.Constant.sCheeseCakeImage;
import static com.nanodegree.udacy.backingapp.utils.Constant.sNutellaImage;
import static com.nanodegree.udacy.backingapp.utils.Constant.sYelloCakeImage;

/**
 * Created by STEVEN on 06/05/2017.
 */

public class StepDetailsFragment extends Fragment implements EventListener,
        PlaybackControlView.VisibilityListener {

    public static final String SAVE_KEY_CURRENT_WINDOW = "currentWindow";
    public static final String SAVE_KEY_PLAY_BACK_POSITION= "playbackPosition";
    public static final String SAVE_KEY_PLAY_WHEN_READY = "playWhenReady";
    public static final String SAVE_KEY_VIDEO_URL = "videoUrl";

    public static final int LOADER_ID = 4;
    private StepDetailsFragmentInterface mListener;
    private static final String TAG = StepDetailsFragment.class.getName();
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    SimpleExoPlayerView mPlayerView;
    ImageView mImageView;
    ImageView imageViewExpand;
    TextView mShortDescTV, textViewDescription;
    private ComponentListener componentListener;
    private SimpleExoPlayer mExoPlayer;
    private RecipeStep mRecipeStep;
    private String mStepID;
    private String mRecipeID;
    private String videoUrl;

    private long playbackPosition;
    private int currentWindow;
    private boolean playWhenReady = true;
    ViewGroup linearLayoutDetails;
    LinearLayout linearLayoutClic;


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

    @Override
    public void onVisibilityChange(int i) {

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
            Log.e(TAG +"mRecipeID", mRecipeID);
            Cursor cursor = loadStepData(mRecipeID, mStepID, getActivity());

            /** parse the cursor to get a {@link RecipeStep } Object */
            new StepParsingAsyncTask() {
                @Override
                protected void onPostExecute(RecipeStep recipeStep) {
                    mRecipeStep = recipeStep;
                    videoUrl = mRecipeStep.getVideoUrl();
                    try {
                        //  populateViews(recipeStep);
                        populateViews(mRecipeStep);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }.execute(cursor);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializePlayer(Uri.parse(videoUrl));
        } else {
            showToast(R.string.storage_permission_denied);
            getActivity().finish();
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.activity_recipe_step_details, container, false);
        mPlayerView = rootview.findViewById(R.id.exoPlayer);
        mImageView = rootview.findViewById(R.id.stepImage);
        textViewDescription = rootview.findViewById(R.id.textview_description);
        mShortDescTV = rootview.findViewById(R.id.shortDescriptionDetails);
        imageViewExpand = rootview.findViewById(R.id.imageViewExpand);
        linearLayoutDetails = rootview.findViewById(R.id.linearLayoutDetails);
        linearLayoutClic = rootview.findViewById(R.id.linear_clic);
        componentListener = new ComponentListener();
        if (savedInstanceState == null) {
            //get data from DataBase and display
//            Cursor cursor = loadStepData(mRecipeID, mStepID, getActivity());
//
//            /** parse the cursor to get a {@link RecipeStep } Object */
//            new StepParsingAsyncTask() {
//                @Override
//                protected void onPostExecute(RecipeStep recipeStep) {
//                    mRecipeStep = recipeStep;
//                    videoUrl = mRecipeStep.getVideoUrl();
//                    try {
//                        //  populateViews(recipeStep);
//                        populateViews(mRecipeStep);
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                }
//            }.execute(cursor);
        } else {
            //when device is rooted or any thing else
            videoUrl = savedInstanceState.getString(SAVE_KEY_VIDEO_URL);
            playbackPosition = savedInstanceState.getLong(SAVE_KEY_PLAY_BACK_POSITION);
            currentWindow = savedInstanceState.getInt(SAVE_KEY_CURRENT_WINDOW);
            playWhenReady = savedInstanceState.getBoolean(SAVE_KEY_PLAY_WHEN_READY);

        }
        linearLayoutClic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expandCardView();
            }
        });

        return rootview;
    }

    //this methode help to expand cardview
    void expandCardView() {
        if (linearLayoutDetails.getVisibility() == View.GONE) {
            ExpandAndCollapseViewUtil.expand(linearLayoutDetails, DURATION);
            imageViewExpand.setImageResource(R.drawable.ic_expand_down);
            rotate(-180.0f);
        } else {
            ExpandAndCollapseViewUtil.collapse(linearLayoutDetails, DURATION);
            imageViewExpand.setImageResource(R.drawable.ic_expand_up);
            rotate(180.0f);
        }
    }

    // root the icon
    private void rotate(float angle) {
        Animation animation = new RotateAnimation(0.0f, angle, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setFillAfter(true);
        animation.setDuration(DURATION);
        imageViewExpand.startAnimation(animation);
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
        } /*else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");

        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    private void populateViews(RecipeStep recipeStep) throws Exception {
        // here the mRecipeStep object has an instance

        if (recipeStep == null) {
            Log.i("recipeStep", "null");
            return;
        }

        mShortDescTV.setText(recipeStep.getShortDesc());
        textViewDescription.setText(recipeStep.getDesc());

        try {
            final int MINIMUM_URL_LENGTH = 4;
            //au cas ou il n'y aucune vidéo ou image charge les images par défaut
            //if there isn't images and videos load default image
            if (recipeStep.getThumbnailUrl()== null && recipeStep.getThumbnailUrl().isEmpty()
                    && recipeStep.getVideoUrl().length() == 0 && recipeStep.getVideoUrl().isEmpty()) {

                Log.e(TAG +"case 1", mRecipeID);

                mImageView.setVisibility(View.VISIBLE);
                mPlayerView.setVisibility(View.GONE);
                loadDefaultImage();

                //if there is a video not image
            } if (recipeStep.getVideoUrl() != null && !recipeStep.getVideoUrl().isEmpty()
                    && recipeStep.getVideoUrl().length() > MINIMUM_URL_LENGTH && recipeStep.getThumbnailUrl().isEmpty()) {

                Log.e(TAG +"case 1", mRecipeID);
                mImageView.setVisibility(View.GONE);
                mPlayerView.setVisibility(View.VISIBLE);

                loadVideo(recipeStep.getVideoUrl());

                //if there is thumbnailURL not videoUrl
            } else if (recipeStep.getThumbnailUrl() != null && !recipeStep.getThumbnailUrl().isEmpty()
                    && recipeStep.getThumbnailUrl().length() > MINIMUM_URL_LENGTH && !recipeStep.getVideoUrl().isEmpty()) {

                Log.e(TAG +"case 2", mRecipeID);
                mImageView.setVisibility(View.GONE);
                mPlayerView.setVisibility(View.VISIBLE);
                loadVideo(recipeStep.getThumbnailUrl());

            } else if(recipeStep.getThumbnailUrl() == null || recipeStep.getThumbnailUrl().isEmpty() ||
                    recipeStep.getThumbnailUrl().length() < MINIMUM_URL_LENGTH){

                Log.e(TAG +"case 3", mRecipeID);

                mImageView.setVisibility(View.VISIBLE);
                mPlayerView.setVisibility(View.GONE);
                loadDefaultImage();

            }

        } catch (OutOfMemoryError ex) {
            ex.printStackTrace();
            Log.e(TAG + "OutOfMemoryError", ex.toString());
        }
    }

    //this method load my personal image when there are'nt
    private void loadDefaultImage(){
        if(mRecipeID.contains("1")){
            utils.downloadImage(getActivity(), sNutellaImage, mImageView);
        }
        if(mRecipeID.contains("2")){
            utils.downloadImage(getActivity(), sBrowniesImage, mImageView);
        }
        if(mRecipeID.contains("3")){
            utils.downloadImage(getActivity(), sYelloCakeImage, mImageView);
        }
        if(mRecipeID.contains("4")){
            utils.downloadImage(getActivity(), sCheeseCakeImage, mImageView);
        }
    }




    /**
     * Initialize ExoPlayer.
     *
     * @param mediaUri The URI of the sample to play.
     */
    private void initializePlayer(Uri mediaUri) {

        if (mExoPlayer == null) {
            Log.e(TAG+"initializePlayer1", String.valueOf(mExoPlayer));
            // a factory to create an AdaptiveVideoTrackSelection
            TrackSelection.Factory adaptiveTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
            // using a DefaultTrackSelector with an adaptive video selection factory
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(getActivity()),
                    new DefaultTrackSelector(adaptiveTrackSelectionFactory), new DefaultLoadControl());
            mExoPlayer.addListener(componentListener);
            mExoPlayer.setVideoDebugListener(componentListener);
            mExoPlayer.setAudioDebugListener(componentListener);
            mPlayerView.setPlayer(mExoPlayer);
            mExoPlayer.setPlayWhenReady(playWhenReady);
            mExoPlayer.seekTo(currentWindow, playbackPosition);
        }

        Log.e(TAG+"initializePlayer2", String.valueOf(mExoPlayer));
        MediaSource mediaSource = buildMediaSource(mediaUri);
        mExoPlayer.prepare(mediaSource, true, false);
        // i use this for testing
      /*   MediaSource mediaSource = buildMediaSource(Uri.parse(getString(R.string.media_url_dash)));
        mExoPlayer.prepare(mediaSource, true, false);*/
    }


    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultHttpDataSourceFactory("ua", BANDWIDTH_METER);
        DashChunkSource.Factory dashChunkSourceFactory = new DefaultDashChunkSource.Factory(
                dataSourceFactory);
        return new DashMediaSource(uri, dataSourceFactory, dashChunkSourceFactory, null, null);
    }


    @Override
    public void onStart() {
        super.onStart();
//        if ((mExoPlayer == null)) {
//            initializePlayer(Uri.parse(videoUrl));
//        }
        try {
            populateViews(mRecipeStep);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
//        if ((mExoPlayer == null)) {
//            initializePlayer(Uri.parse(videoUrl));
//        }
        try {
            populateViews(mRecipeStep);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            releasePlayer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            releasePlayer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        mPlayerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    void releasePlayer() throws Exception {
        playbackPosition = mExoPlayer.getCurrentPosition();
        currentWindow = mExoPlayer.getCurrentWindowIndex();
        playWhenReady = mExoPlayer.getPlayWhenReady();
        mExoPlayer.removeListener(componentListener);
        mExoPlayer.stop();
        mExoPlayer.release();
        mExoPlayer = null;
    }

    private void loadVideo(String videoUrl) throws OutOfMemoryError {
        // Initialize the player.
        initializePlayer(Uri.parse(videoUrl));
    }


    public static Cursor loadStepData(String recipeID, String stepID, Context context) {

        //build query with parameters
        String selection = RecipeContract.StepEntry.COLUMN_RECIPE_ID + " = ? AND "
                + RecipeContract.StepEntry._ID + " = ?";

        String[] selectionArgs = new String[]{recipeID, stepID};

        //build the query
        return context.getContentResolver()
                .query(RecipeContract.StepEntry.CONTENT_URI,
                        STEP_COLUMNS,
                        selection,
                        selectionArgs, null);

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(EXTRA_RECIPE_STEP, mRecipeStep);
        outState.putBoolean(SAVE_KEY_PLAY_WHEN_READY, playWhenReady);
        outState.putLong(SAVE_KEY_PLAY_BACK_POSITION, playbackPosition);
        outState.putInt(SAVE_KEY_CURRENT_WINDOW, currentWindow);
        outState.putString(SAVE_KEY_VIDEO_URL, videoUrl);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    private class ComponentListener implements ExoPlayer.EventListener, VideoRendererEventListener,
            AudioRendererEventListener {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
            // Do nothing.
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            // Do nothing.
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            // Do nothing.
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
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            // Do nothing.
        }

        @Override
        public void onPositionDiscontinuity() {
            // Do nothing.
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

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
        public void onAudioInputFormatChanged(Format format)
        }

        @Override
        public void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {
        }

        @Override
        public void onAudioDisabled(DecoderCounters counters) {
        }
    }
}