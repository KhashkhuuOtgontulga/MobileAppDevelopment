package com.example.rewards.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.rewards.AsyncTasks.LoginAPIAsyncTask;
import com.example.rewards.R;
import com.example.rewards.Reward;
import com.example.rewards.SharedPreference;
import com.example.rewards.UserProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private SharedPreference prefs;
    private EditText data1;
    private EditText data2;
    public static final String extraName = "DATA HOLDER";

    private ProgressBar progressBar;

    private static int MY_LOCATION_REQUEST_CODE = 329;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.icon);

        data1 = findViewById(R.id.userText);
        data2 = findViewById(R.id.passText);


        prefs = new SharedPreference(this);
        data1.setText(prefs.getValue(getString(R.string.data1Key)));
        data2.setText(prefs.getValue(getString(R.string.data2Key)));

        progressBar = findViewById(R.id.progressBar);
        progressBar.bringToFront();
        progressBar.setVisibility(View.GONE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION )
                != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    MY_LOCATION_REQUEST_CODE);
        }

    }

    public void itemClicked(View w) {
        CheckBox c1 = (CheckBox) w;
        if (c1.isChecked()) {
            saveAll(w);
        }
        else {
            clearAll();
        }
        Log.d(TAG, "itemClicked: " + Boolean.toString(c1.isChecked()));
        // save the state of the checkbox

        /*// Save
        boolean checkBoxValue = c1.isChecked();
        editor.putBoolean("c1", checkBoxValue);
        editor.commit();;

        // Load
        c1.setChecked(settings.getBoolean("c1", true));*/
    }

    public void login(View w) {
        // if the username and password match in a database
        // access the database to log into the profile activity
        // else say that the username or password is incorrect
        data1.getText().toString();
        data2.getText().toString();
        String sId = "A20379665";
        String uName = ((EditText) findViewById(R.id.userText)).getText().toString();
        String pswd = ((EditText) findViewById(R.id.passText)).getText().toString();
        Log.d(TAG, "login clicked: ");
        progressBar.setVisibility(View.VISIBLE);
        new LoginAPIAsyncTask(this).execute(sId, uName, pswd);
    }

    public void createProfile(View v) {
        Intent intent = new Intent(this, CreateProfileActivity.class);
        startActivity(intent);
    }

    public void saveAll(View v) {
        Log.d(TAG, "saveAll: ");
        save1(v);
        save2(v);
    }

    public void save1(View v) {
        Log.d(TAG, "save1: ");
        String d1 = data1.getText().toString();
        prefs.save(getString(R.string.data1Key), d1);
    }

    public void save2(View v) {
        Log.d(TAG, "save2: ");
        String d2 = data2.getText().toString();
        prefs.save(getString(R.string.data2Key), d2);
    }

    public void clearAll() {
        Log.d(TAG, "clearAll: ");

        prefs.clearAll();
        data1.setText("");
        data2.setText("");

    }

    public void sendResults(boolean error, String s) {
        Log.d(TAG, "sendResults: " + s);
        if (error) {
            try {
                JSONObject errorDetails = new JSONObject(s);
                JSONObject jsonObject = new JSONObject(errorDetails.getString("errordetails"));
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(jsonObject.getString("status"));
                builder.setMessage(jsonObject.getString("message"));
                AlertDialog dialog = builder.create();
                dialog.show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            ArrayList<Reward> temp = new ArrayList<>();

            JSONObject json = null;
            try {
                json = new JSONObject(s);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            int total = 0;
            try{
                JSONArray rewards = new JSONArray(json.getString("rewards"));
                Log.d(TAG, "rewards: " + rewards.toString());
                for (int j = 0; j < rewards.length(); j++) {
                    JSONObject giver = rewards.getJSONObject(j);
                    temp.add(new Reward(giver.getString("date"),
                            giver.getString("name"),
                            giver.getString("notes"),
                            Integer.parseInt(giver.getString("value"))));
                    total += Integer.parseInt(giver.getString("value"));
                }
                Log.d(TAG, "total: " + Integer.toString(total));
            } catch (JSONException e) {
                // do nothing
            }
            UserProfile up = null;
            try {
                up = new UserProfile(json.getString("firstName"),
                        json.getString("lastName"),
                        json.getString("username"),
                        json.getString("password"),
                        json.getString("location"),
                        Boolean.parseBoolean(json.getString("admin")),
                        total,
                        json.getString("department"),
                        json.getString("position"),
                        Integer.parseInt(json.getString("pointsToAward")),
                        json.getString("story"),
                        json.getString("imageBytes"),
                        temp);
                        //json.getString("rewards"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra(extraName, up); // Better be Serializable!
            startActivity(intent);
            Log.d(TAG, "supposed to start the activity: ");
        }
        progressBar.setVisibility(View.GONE);
    }
}
