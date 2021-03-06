package com.example.stockwatch;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;


public class NameDownloaderAsyncTask extends AsyncTask<String, Integer, String> {

    private MainActivity mainActivity;
    private static final String API_KEY = "sk_a9a77a68d61544529b8b142b6f247f6e";
    private static final String DATA_URL = "https://cloud.iexapis.com/stable/ref-data/symbols/?token=" + API_KEY;
    private static final String TAG = "NameDownloaderAsyncTask";
    public HashMap<String, String> sData = new HashMap<>();


    public NameDownloaderAsyncTask(MainActivity ma) {
        mainActivity = ma;
    }

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "onPreExecute: Loading Stock Data...");
        Toast.makeText(mainActivity, "Loading Stock Data...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostExecute(String s) {
        mainActivity.initiateData(sData);
        Log.d(TAG, "onPostExecute: Stock Data Loaded.");
        Toast.makeText(mainActivity, "Stock Data Loaded.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected String doInBackground(String... params) {

        // Connect to data provider via API
        Uri dataUri = Uri.parse(DATA_URL);
        String urlToUse = dataUri.toString();
        Log.d(TAG, "doInBackground: " + urlToUse);

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //Log.d(TAG, "doInBackground: ResponseCode: " + conn.getResponseCode());

            // Download stock symbols & names JSON
            conn.setRequestMethod("GET");

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            Log.d(TAG, "doInBackground: " + sb.toString());

        } catch (Exception e) {
            Log.e(TAG, "doInBackground: ", e);
            return null;
        }

        Log.d(TAG, "doInBackground: " + sb.toString());

        parseJSON(sb.toString());

        return null;
    }


    private void parseJSON(String s) {

        try {
            JSONArray jObjMain = new JSONArray(s);

            for (int i = 0; i < jObjMain.length(); i++) {
                JSONObject jStock = (JSONObject) jObjMain.get(i);

                // Parse symbol & name data
                String symbol = jStock.getString("symbol");
                String name = jStock.getString("name").toUpperCase();
                // Add symbol & names to symbol:name HashMap

                sData.put(symbol, name);
                Log.d(TAG, "parseJSON: " + symbol + " " + name);
            }
            // Done

        } catch (Exception e) {
            Log.d(TAG, "parseJSON: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
