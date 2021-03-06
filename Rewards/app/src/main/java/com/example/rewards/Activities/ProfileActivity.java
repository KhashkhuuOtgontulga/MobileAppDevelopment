package com.example.rewards.Activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rewards.AsyncTasks.DeleteAllProfileAPIAsyncTask;
import com.example.rewards.AsyncTasks.DeleteProfileAPIAsyncTask;
import com.example.rewards.AsyncTasks.ResetPointsAPIAsyncTask;
import com.example.rewards.HighestPoints;
import com.example.rewards.ProfileAdapter;
import com.example.rewards.R;
import com.example.rewards.Reward;
import com.example.rewards.RewardAdapter;
import com.example.rewards.SortByPoints;
import com.example.rewards.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.view.Gravity.CENTER_HORIZONTAL;

public class ProfileActivity extends AppCompatActivity {

    private TextView nameProfile;
    private TextView usernameProfile;
    private TextView passwordProfile;
    private TextView locationProfile;
    private TextView numberPointsAwarded;
    private TextView departmentTextProfile;
    private TextView positionTextProfile;
    private TextView numberPointsProfile;
    private TextView storyTextProfile;
    private ImageView imageView;

    private TextView rewardHistory;
    private final List<Reward> rewardList = new ArrayList<>();
    private RewardAdapter rewardAdapter;
    private RecyclerView rewardRecycler;

    private ProgressBar progressBar;

    private UserProfile dh;
    private int UPDATE_PROFILE = 1;
    private int LEADER_PROFILE = 2;

    private EditText et;
    private String input;

    private static final String TAG = "ProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nameProfile = findViewById(R.id.nameAward);
        usernameProfile = findViewById(R.id.usernameProfile);
        locationProfile = findViewById(R.id.locationProfile);
        numberPointsAwarded = findViewById(R.id.numberPointsAwardedAward);
        departmentTextProfile = findViewById(R.id.departmentTextAward);
        positionTextProfile = findViewById(R.id.positionTextAward);
        numberPointsProfile = findViewById(R.id.numberPointsProfile);
        storyTextProfile = findViewById(R.id.storyTextAward);
        imageView = findViewById(R.id.imageAward);
        rewardHistory = findViewById(R.id.rewardHistory);
        rewardRecycler = findViewById(R.id.rewardRecycler);

        Intent intent = getIntent();

        dh = (UserProfile) intent.getSerializableExtra(CreateProfileActivity.extraName);
        nameProfile.setText(dh.getLast_name() + ", " + dh.getFirst_name());
        usernameProfile.setText("(" + dh.getUsername() + ")");
        locationProfile.setText(dh.getLocation());
        numberPointsAwarded.setText(String.valueOf(dh.getPoints_awarded()));
        departmentTextProfile.setText(dh.getDepartment());
        positionTextProfile.setText(dh.getPosition());
        numberPointsProfile.setText(String.valueOf(dh.getPoints_to_award()));
        Log.d(TAG, "PROFILE TO ADD POINTS: " + String.valueOf(dh.getPoints_to_award()));
        storyTextProfile.setText(dh.getStory());

        rewardRecycler = findViewById(R.id.rewardRecycler);
        rewardAdapter = new RewardAdapter(rewardList);
        // connect the recyclerView to the adapter
        rewardRecycler.setLayoutManager(new LinearLayoutManager(this));
        rewardRecycler.setAdapter(rewardAdapter);
        /* show the recyclerview */
        rewardList.addAll(dh.getRewards());
        Collections.sort(rewardList, Collections.<Reward>reverseOrder(new HighestPoints()));
        rewardAdapter.notifyDataSetChanged();
        rewardHistory.setText("Reward History " + "("+ rewardAdapter.getItemCount() + "):");

        progressBar = findViewById(R.id.progressBar2);
        progressBar.bringToFront();
        progressBar.setVisibility(View.GONE);

        byte[] imageBytes = Base64.decode(dh.getImage(),  Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        imageView.setImageBitmap(bitmap);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.icon);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.editField:
                Intent intent = new Intent(this, EditProfileActivity.class);
                intent.putExtra("EDIT", dh);
                startActivityForResult(intent, UPDATE_PROFILE);
                return true;
            case R.id.leaderField:
                //Toast.makeText(this, "Going to the leaderboard", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.VISIBLE);
                Intent i = new Intent(this, LeaderboardActivity.class);
                i.putExtra("LEADER", dh);
                startActivityForResult(i, LEADER_PROFILE);
                progressBar.setVisibility(View.GONE);
                return true;
            case R.id.deleteField:
                //Toast.makeText(this, "Going to the leaderboard", Toast.LENGTH_LONG).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                et = new EditText(this);
                et.setGravity(CENTER_HORIZONTAL);
                // same thing as putting android:textAllCaps="true"
                // in the editText of an xml file
                builder.setView(et);

                builder.setIcon(R.drawable.logo);
                builder.setTitle("Delete");
                builder.setMessage("Enter a username to delete one user or all users or to reset the points: ");
                builder.setPositiveButton("All Users",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                input = et.getText().toString();
                                Log.d(TAG, "onOptionsItemSelected: " + input);
                                deleteAllUsers(input);
                            }
                        }
                );
                builder.setNegativeButton("One User",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                input = et.getText().toString();
                                Log.d(TAG, "onOptionsItemSelected: " + input);
                                deleteUser(input);
                            }
                        }
                );
                builder.setNeutralButton("Reset Points",
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                resetPoints();
                            }
                        }
                );
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void deleteAllUsers(String input) {
        new DeleteAllProfileAPIAsyncTask(this).execute("A20379665", dh.getUsername(), dh.getPassword(), input);
    }

    public void deleteUser(String input) {
        new DeleteProfileAPIAsyncTask(this).execute("A20379665", dh.getUsername(), dh.getPassword(), input);
    }

    public void resetPoints() {
        new ResetPointsAPIAsyncTask(this).execute("A20379665", dh.getUsername(), dh.getPassword());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == UPDATE_PROFILE) {
            if (resultCode == RESULT_OK) {
                dh = (UserProfile) data.getSerializableExtra(EditProfileActivity.extraName);

                nameProfile.setText(dh.getFirst_name() + ", " + dh.getLast_name());
                usernameProfile.setText("(" + dh.getUsername() + ")");
                locationProfile.setText(dh.getLocation());
                numberPointsAwarded.setText(String.valueOf(dh.getPoints_awarded()));
                departmentTextProfile.setText(dh.getDepartment());
                positionTextProfile.setText(dh.getPosition());
                numberPointsProfile.setText(String.valueOf(dh.getPoints_to_award()));
                storyTextProfile.setText(dh.getStory());
                byte[] imageBytes = Base64.decode(dh.getImage(),  Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imageView.setImageBitmap(bitmap);
            }
        }
        else if (requestCode == LEADER_PROFILE) {
            if (data == null) {
                // do nothing
            }
            else {
                dh = (UserProfile) data.getSerializableExtra(LeaderboardActivity.extraName);
                Log.d(TAG, "PROFILE POINTS TO GIVE: " + String.valueOf(dh.getPoints_to_award()));
                numberPointsProfile.setText(String.valueOf(dh.getPoints_to_award()));
            }
        }
    }

    public static void makeCustomToast(Context context, int time) {
        Toast toast = Toast.makeText(context, "Success!", time);
        View toastView = toast.getView();
        toastView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
        TextView tv = toastView.findViewById(android.R.id.message);
        tv.setPadding(250, 100, 250, 100);
        tv.setTextColor(Color.WHITE);
        toast.show();
    }

    public void sendError(boolean error, String connectionResult) {
        if (error) {
            try {
                JSONObject errorDetails = new JSONObject(connectionResult);
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
            makeCustomToast(this, Toast.LENGTH_LONG);
        }
    }
}