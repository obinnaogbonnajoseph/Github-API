package com.example.android.sampleapi;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements
        DevAdapter.DevAdapterOnClickHandler,
        LoaderCallbacks<ArrayList<Developer>> {
	// Create an instance of the Adapter variable called mAdapter
	private DevAdapter mAdapter;
	// Create a RecyclerView variable called mDevelopersList
	private RecyclerView mDevelopersList;
    // Displays when there is no data from gitHub API
    private TextView mErrorMessageDisplay;
    // It indicates to the user that we are loading data
    private ProgressBar mLoadingIndicator;
    // Unique Id for Loader
    public static final int DEVELOPER_LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Using findViewById, I get reference to the RecycleView from xml.
        mDevelopersList = (RecyclerView) findViewById(R.id.rv_dev_list);
        // This TextView is used to display errors and will be hidden if no errors
        mErrorMessageDisplay = (TextView) findViewById(R.id.error_view);
        // A LinearLayoutManager variable called layoutManager is created.
        // It is linear.
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        // setLayoutManager on mDevelopersList with the LinearLayoutManager
        // created above
        mDevelopersList.setLayoutManager(layoutManager);
        // setHasFixedSize(true) is used to designate that the contents of the
        // RecyclerView won't change an item's size
        mDevelopersList.setHasFixedSize(true);
        // The adapter is responsible for linking data with the views that
        // will display the gitHub data.
        mAdapter = new DevAdapter(this);
        // Set the DevAdapter created attaching it to mDevelopersList
        mDevelopersList.setAdapter(mAdapter);
        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.loading_indicator);
        /*
         * From MainActivity, we have implemented the LoaderCallbacks interface with the type of
         * Developer Object. (implements LoaderCallbacks<ArrayList<Developer>>) The variable callback is passed
         * to the call to initLoader below. This means that whenever the loaderManager has
         * something to notify us of, it will do so through this callback.
         */
        LoaderCallbacks<ArrayList<Developer>> callback = MainActivity.this;
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            /*
         * Ensures a loader is initialized and active. If the loader doesn't already exist, one is
         * created and (if the activity/fragment is currently started) starts the loader. Otherwise
         * the last created loader is re-used.
         */
            getSupportLoaderManager().initLoader(DEVELOPER_LOADER_ID, null, callback);

        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            mLoadingIndicator.setVisibility(View.GONE);
            // Show the errorMessageView
            showErrorMessage();
            // Update errorMessageView with no connection error message
            mErrorMessageDisplay.setText(R.string.no_internet_connection);
        }
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id The ID whose loader is to be created.
     * @param loaderArgs Any arguments supplied by the caller.
     *
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<ArrayList<Developer>> onCreateLoader(int id, final Bundle loaderArgs) {

        return new AsyncTaskLoader<ArrayList<Developer>>(this) {
            /* This String array will hold and help cache our weather data */
            ArrayList<Developer> mDeveloper = null;
            /**
             * Subclasses of AsyncTaskLoader must implement this to take care of loading their data.
             */
            @Override
            protected void onStartLoading() {
                if (mDeveloper != null) {
                    deliverResult(mDeveloper);
                } else {
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            /**
             * This is the method of the AsyncTaskLoader that will load and parse the JSON data
             * in the background.
             *
             * @return gitHub data from gitHub API.
             *         null if an error occurs
             */
            @Override
            public ArrayList<Developer> loadInBackground() {
                // URL is built
                URL devRequestUrl = NetworkUtil.buildUrl();

                // A try-catch block is used to catch JSONException response
                try {
                    String jsonResponse = NetworkUtil.getJsonResponse(devRequestUrl);
                    return NetworkUtil.extractFeatureFromJson(jsonResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            /**
             * Sends the result of the load to the registered listener.
             *
             * @param data The result of the load
             */
            public void deliverResult(ArrayList<Developer> data) {
                mDeveloper = data;
                super.deliverResult(data);
            }
        };
    }
    /**
     * Called when a previously created loader has finished its load.
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<ArrayList<Developer>> loader, ArrayList<Developer> data) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mAdapter.setData(data);
        if (data == null) {
            showErrorMessage();
        } else {
            showDataView();
        }
    }
    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<ArrayList<Developer>> loader) {
        /*
         * We aren't using this method, but we are required to Override
         * it to implement the LoaderCallbacks<String> interface
         */
    }
    /**
     * This method is for responding to clicks from our list.
     *
     * @param profile is the developer's information sent as intent to another activity
     */
    @Override
    public void onClick(String[] profile) {
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT,profile);
        startActivity(intentToStartDetailActivity);
    }

    /**
    * This method will make the View for the github data visible and 
    * hide the error message.
    */
    private void showDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the gitHub data is visible */
        mDevelopersList.setVisibility(View.VISIBLE);
    }

    /*
    * This method will make the error message visible and hide the github data
    * view.
    */
    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mDevelopersList.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }
}
