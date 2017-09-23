package cg.stevendende.deliciousrecipes.ui;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.RelativeLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import cg.stevendende.deliciousrecipes.R;
import cg.stevendende.deliciousrecipes.data.RecipesContract;
import cg.stevendende.deliciousrecipes.ui.adapters.StepsCursorRecyclerAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RecipeDetailsFragment.StepsCallbackInterface} interface
 * to handle interaction events.
 * Use the {@link RecipeDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecipeDetailsFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        StepsCursorRecyclerAdapter.RecipesAdapterInteractionInterface {
    // parameters initialization
    public static final int LOADER_ID = 2;

    private String mRecipeID;
    private String mStepID;
    private String mRecipeName;

    private StepsCallbackInterface mListener;

    GridLayoutManager mGridLayoutManager;
    StepsCursorRecyclerAdapter mCursorAdapter;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.stepsRecyclerView)
    RecyclerView mRecyclerView;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.ingredientsRL)
    RelativeLayout mIngredient;

    public RecipeDetailsFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param stepId Parameter 1.
     * @return A new instance of fragment RecipeDetailsFragment.
     */
    public static RecipeDetailsFragment newInstance(String recipeId, String stepId) {
        RecipeDetailsFragment fragment = new RecipeDetailsFragment();
        Bundle args = new Bundle();
        args.putString(MainActivity.EXTRA_STEP_ID, stepId);
        args.putString(MainActivity.EXTRA_RECIPE_ID, recipeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRecipeID = getArguments().getString(MainActivity.EXTRA_RECIPE_ID);
            mStepID = getArguments().getString(MainActivity.EXTRA_STEP_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview = inflater.inflate(R.layout.fragment_recipe_details, container, false);
        ButterKnife.bind(this, rootview);

        if (savedInstanceState != null) {
            String tmpRecipeID = savedInstanceState.getString(MainActivity.EXTRA_RECIPE_ID);
            if (tmpRecipeID != null) {
                mRecipeID = tmpRecipeID;
                mStepID = savedInstanceState.getString(MainActivity.EXTRA_STEP_ID);
                //mRecipeName = savedInstanceState.getString(EXTRA_RECIPE_NAME);
            }
        }

        final int SPAN_COUNT = 1;
        mGridLayoutManager = new GridLayoutManager(
                getActivity(),
                SPAN_COUNT,
                LinearLayoutManager.VERTICAL,
                false);
        mRecyclerView.setLayoutManager(mGridLayoutManager);

        mCursorAdapter = new StepsCursorRecyclerAdapter();
        mRecyclerView.setAdapter(mCursorAdapter);

        mIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onIngredientsClick(mRecipeID);
            }
        });

        mCursorAdapter.setCallbackListener(this);
        return rootview;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof StepsCallbackInterface) {
            mListener = (StepsCallbackInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //build the query

        String selection = RecipesContract.RecipeStepEntry.TABLE_NAME
                + "." + RecipesContract.RecipeStepEntry.COLUMN_RECIPE_ID + " = ?";
        String[] selectionArgs = new String[]{mRecipeID};

        return new android.support.v4.content.CursorLoader(
                getActivity(),
                RecipesContract.RecipeStepEntry.CONTENT_URI,
                RecipesContract.RecipeStepEntry.COLUMNS_STEPS,
                selection,
                selectionArgs,
                RecipesContract.RecipeStepEntry.TABLE_NAME
                        + "." + RecipesContract.RecipeStepEntry._ID);

    }

    /**
     * Called when a previously created loader has finished its load.  Note
     * that normally an application is <em>not</em> allowed to commit fragment
     * transactions while in this call, since it can happen after an
     * activity's state is saved.  See {@link android.support.v4.app.FragmentManager#beginTransaction()
     * FragmentManager.openTransaction()} for further discussion on this.
     * <p>
     * <p>This function is guaranteed to be called prior to the release of
     * the last data that was supplied for this Loader.  At this point
     * you should remove all use of the old data (since it will be released
     * soon), but should not do your own release of the data since its Loader
     * owns it and will take care of that.  The Loader will take care of
     * management of its data so you don't have to.  In particular:
     * <p>
     * <ul>
     * <li> <p>The Loader will monitor for changes to the data, and report
     * them to you through new calls here.  You should not monitor the
     * data yourself.  For example, if the data is a {@link Cursor}
     * and you place it in a {@link CursorAdapter}, use
     * the {@link CursorAdapter#CursorAdapter(Context,
     * Cursor, int)} constructor <em>without</em> passing
     * in either {@link CursorAdapter#FLAG_AUTO_REQUERY}
     * or {@link CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER}
     * (that is, use 0 for the flags argument).  This prevents the CursorAdapter
     * from doing its own observing of the Cursor, which is not needed since
     * when a change happens you will get a new Cursor throw another call
     * here.
     * <li> The Loader will release the data once it knows the application
     * is no longer using it.  For example, if the data is
     * a {@link Cursor} from a {@link CursorLoader},
     * you should not call close() on it yourself.  If the Cursor is being placed in a
     * {@link CursorAdapter}, you should use the
     * {@link CursorAdapter#swapCursor(Cursor)}
     * method so that the old Cursor is not closed.
     * </ul>
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);

        Log.i("data_size", data.getCount() + "");
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onStepItemClick(String stepID, String stepName) {
        mListener.onStepItemclick(stepID, stepName);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface StepsCallbackInterface {
        void onStepItemclick(String stepId, String stepName);

        void onIngredientsClick(String recipeID);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putString(MainActivity.EXTRA_RECIPE_ID, mRecipeID);
        super.onSaveInstanceState(outState);
    }
}
