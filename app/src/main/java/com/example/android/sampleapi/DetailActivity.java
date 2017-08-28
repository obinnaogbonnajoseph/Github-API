package com.example.android.sampleapi;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.v4.app.LoaderManager.LoaderCallbacks;

import static com.example.android.sampleapi.R.id.username;

/**
 * A class that shows details of the particular GitHub user that is clicked
 */
public class DetailActivity extends AppCompatActivity implements 
        LoaderCallbacks<Bitmap> {
    // Unique id for loader
    private static final int IMAGE_LOADER_ID = 0;

    // ProgressBar to show progress to users
    private ProgressBar mLoadingIndicator;
    // Contatins a link to user image url
    private String mImageUrl = "";
    // Contains the username
    private String userName = "";
    // Contains a link to user url
    private String mUserUrl = "";
    // Image view to display user profile photo
    private ImageView mImageView;
    // Error image view
    private ImageView mErrorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        //Set up the Action bar for the home button
        ActionBar actionBar = this.getSupportActionBar();
        // Set the action bar back button to look like an up button
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        // Displays the username of the user
        TextView mUserName = (TextView) findViewById(username);
        // Displays the user profile picture
        mImageView = (ImageView) findViewById(R.id.profile_image) ;
        // Get the intent passed to this activity
        Intent intent = getIntent();
        if(intent.hasExtra(Intent.EXTRA_TEXT)){
            String[] profile = intent.getStringArrayExtra(Intent.EXTRA_TEXT);
            userName = profile[0];
            mUserUrl = profile[1];
            mImageUrl = profile[2];
        }
        // The progress bar that will indicate to the user that we are
        // loading data and it is hidden when no data is loading
        mLoadingIndicator = (ProgressBar) findViewById(R.id.loading_indicator);
        // Image view that shows an error image when any error occurs.
        mErrorView = (ImageView) findViewById(R.id.error_view);
        // Get a reference to the ConnectivityManager to check the state of network connectivity 
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Ensures a loader is initialized and active.
            getSupportLoaderManager().initLoader(IMAGE_LOADER_ID, null, DetailActivity.this);
        } else {
            // First hide loading indicator, so error image will be visible
            mLoadingIndicator.setVisibility(View.GONE);
            // Show the error image
            showErrorMessage();
        }
        // Set the view with its value.
        mUserName.setText(userName);
    }

    /**
    * This method is called when the User Details button is clicked. It will open
    * the site specified by the mUserUrl
    */
    public void onClickOpenWebAddress(View v) {
        Uri webPage = Uri.parse(mUserUrl);
        // Create an intent with action of ACTION_VIEW
        Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
        // We perform a check on this implicit intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
    * Instantiate and return a new loader for the given ID
    * @param id The ID whose loader is to be created
    * @param args Any argument supplied by the caller
    * @return a new loader instance that is ready to start loading
    */
    @Override
    public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Bitmap>(this) {

            // We initialize the bitmap here
            Bitmap mBitmap = null;

            /**
            * Subclass of the AsyncTaskLoader that is implemented to take care
            * of the loader at starting.
            */
            @Override
            protected void onStartLoading() {
                if (mBitmap != null) {
                    deliverResult(mBitmap);
                } else {
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            /**
            * This method of the AsyncTaskLoader will load the bitmap
            * image in the background.
            * @return the Bitmap from NetworkUtil.
            */
            @Override
            public Bitmap loadInBackground() {
                return NetworkUtil.getBitmap(mImageUrl);
            }

            /**
            * Sends the result of the load to where needed
            * @param bitmap The result of the load
            */
            public void deliverResult(Bitmap bitmap) {
                mBitmap = bitmap;
                super.deliverResult(bitmap);
            }
        };
    }

    /**
    * Called when a previously created loader has finished its load.
    *
    * @param loader The Loader that has finished.
    * @param data   The data generated by the loader.
    */
    @Override
    public void onLoadFinished(Loader<Bitmap> loader, Bitmap data) {
        mLoadingIndicator.setVisibility(View.GONE);
        mImageView.setImageBitmap(data);
        if (data == null) {
            showErrorMessage();
        } else {
            showDataView();
        }
    }

    /** 
    * Called when a previously created loader is reset, and thus making its
    * data unavailable
    * @param loader The loader that is being reset
    */
    @Override
    public void onLoaderReset(Loader<Bitmap> loader) {
        /*
        * We aren't using this method, but we are required to Override it
        */
    }

    /*
    * This method will make the Image for the user profile visible and
    * hide the error image.
    */
    private void showDataView() {
        /* First, make sure the error image is invisible. */
        mErrorView.setVisibility(View.GONE);
        /* Then, show the profile image */
        mImageView.setVisibility(View.VISIBLE);
    }

    /*
    * This method will make the error image visible and hide the profile image
    */
    private void showErrorMessage() {
        /* First, hide the currently visible profile image */
        mImageView.setVisibility(View.GONE);
        /* Then, show the error image */
        mErrorView.setVisibility(View.VISIBLE);
    }

    /**
    * Uses the ShareCompat Intent Builder to create an intent we share
    * the intent is just regular text
     * @return the intent built
    */
    private Intent createShareIntent() {
        String shareText = "Check out this awesome developer " +
                "@<" + userName + ">, <" + mUserUrl + ">.";
        return ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(shareText)
                .getIntent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setIntent(createShareIntent());
        return true;
    }

    /**
     * This method sets the up button, so that when clicked,
     * it returns one back to the MainActivity.
     * @param item is the MenuItem
     * @return calls the same method on its super class
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // When the home button is pressed, take the user back to the MainActivity
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }
}
