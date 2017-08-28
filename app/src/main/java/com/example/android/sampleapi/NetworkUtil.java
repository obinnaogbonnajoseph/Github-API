package com.example.android.sampleapi;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * This is used to communicate with the GitHub server.
 */

class NetworkUtil {

    private static final String LOG_TAG = NetworkUtil.class.getSimpleName();

    private final static String GITHUB_BASE_URL = "https://api.github.com/search/users";
    private final static String ACCESS_TOKEN = "access_token";

    private final static String PARAM_QUERY = "q";

    private static final String access_token = "404af420aaa93ca43f619a5674b3ff732e8b041d";

	/**
	* Builds the URL used to talk to the gitHub server using a location.
	*
	* @return 			   The URL to use to query the weather server.
     *  The end URl to achieve is:https://api.github.com/search/users?q=location:lagos+sort:repos
	*/
	static URL buildUrl() {
		StringBuilder builtUri = new StringBuilder();
        builtUri.append(GITHUB_BASE_URL);
        builtUri.append("?=");
        builtUri.append(ACCESS_TOKEN+"=");
        builtUri.append(access_token+"&");
        builtUri.append(PARAM_QUERY+"=");
        String param = "location:lagos+sort:repos+language:java";
        builtUri.append(param);
		URL url = null;
		try {
			url = new URL(builtUri.toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
        Log.i(LOG_TAG,String.valueOf(url));
		return url;
	}


	/**
	* This method returns the entire result from the HTTP response.
	*
	* @param url The URL to fetch the HTTP response from.
	* @return 	 The contents of the HTTP response.
	* @throws	 IOException related to network and stream reading
	*/
	static String getJsonResponse(URL url) throws IOException {
		String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
	}

	/**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
    /**
    * Obtains the image from the URL got from jsonResponse
    * @param url is the image URL got from jsonResponse
    * @return the Bitmap is returned
    */
    static Bitmap getBitmap(String url) {
        try {
            URL urlConnection = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlConnection
                    .openConnection();
            connection.setDoInput(true);
                connection.connect();
            InputStream inputStream = connection.getInputStream();
            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
    * Return a list of Developer objects that has been built from 
    * parsing the given JSON response.
    * @param devJson is the jsonResponse.
    */
    static ArrayList<Developer> extractFeatureFromJson(String devJson) {
        if (TextUtils.isEmpty(devJson)) {
            return null;
        }

        // Create an empty ArrayList that Developer objects will be added to
        ArrayList<Developer> developers = new ArrayList<>();

        // Try to parse the JSON response string. A try catch block is used
        // to catch exceptions if any.
        try {
            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(devJson);

            // Extract the JSONArray associated with the key called "items"
            // which represents a list of developers.
            JSONArray developerArray = baseJsonResponse.getJSONArray("items");

            // For each developer in the array, create an object.
            for (int i = 0; i < developerArray.length();i++) {

                // Get a single developer at position i within the list
                JSONObject currentDeveloper = developerArray.getJSONObject(i);

                // Extract the necessary things needed
                String username = currentDeveloper.getString("login");
                String imageUrl = currentDeveloper.getString("avatar_url");
                String userUrl = currentDeveloper.getString("html_url");

                // Load the Bitmap imageUrl
                Bitmap userImage = NetworkUtil.getBitmap(imageUrl);

                // Create a new Developer object.
                Developer developer = new Developer(username, userImage, userUrl, imageUrl);

                // Add the new object to the list of Developers
                developers.add(developer);

            }
        } catch(JSONException e) {
            e.printStackTrace();
        }

        // Return the list of Developers
        return developers;
    }
}
