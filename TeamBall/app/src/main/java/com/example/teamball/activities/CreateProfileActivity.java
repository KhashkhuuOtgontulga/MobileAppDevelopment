package com.example.teamball.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teamball.AsyncTasks.CreateProfileAPIAsyncTask;
import com.example.teamball.R;
import com.example.teamball.Reward;
import com.example.teamball.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class CreateProfileActivity extends AppCompatActivity {

    private static final String TAG = "CreateProfileActivity";
    public static final String extraName = "DATA HOLDER";

    private Criteria criteria;
    private LocationManager locationManager;
    private Location currentLocation;
    private String location;

    private static int MY_LOCATION_REQUEST_CODE = 329;
    private int REQUEST_IMAGE_GALLERY = 1;
    private int REQUEST_IMAGE_CAPTURE = 2;
    private static int MY_PHOTO_REQUEST_CODE = 329;

    private EditText username;
    private EditText password;
    private EditText first_name;
    private EditText last_name;

    private ImageView imageView;
    private ImageView add;
    private File currentImageFile;

    private UserProfile up;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createprofile);

        first_name = findViewById(R.id.firstNameEdit);
        last_name = findViewById(R.id.lastName);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        username = findViewById(R.id.usernameProfile);
        password = findViewById(R.id.passProfile);

        imageView = findViewById(R.id.imageProfile2);
        add = findViewById(R.id.imageView3);
        imageView.setImageResource(R.drawable.default_photo);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);

        setLocation();
    }



    @SuppressLint("MissingPermission")
    public void setLocation() {
        //String locationProvider = LocationManager.GPS_PROVIDER;
        String locationProvider = locationManager.getBestProvider(criteria, true);
        Log.d(TAG, "locationProvider: " + locationProvider);
        currentLocation = locationManager.getLastKnownLocation(locationProvider);
        if (currentLocation != null) {
            doLatLon();
        } else {
            Log.d(TAG, "location unavailable: ");
        }
    }

    public void doLatLon() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses;

            String loc = currentLocation.getLatitude() + ", " + currentLocation.getLongitude();
            Log.d(TAG, "doLatLon: " + loc);
            if (loc.trim().isEmpty()) {
                Toast.makeText(this, "Enter Lat & Lon coordinates first!", Toast.LENGTH_LONG).show();
                return;
            }
            String[] latLon = loc.split(",");
            double lat = Double.parseDouble(latLon[0]);
            double lon = Double.parseDouble(latLon[1]);

            addresses = geocoder.getFromLocation(lat, lon, 1);

            StringBuilder sb = new StringBuilder();

            for (Address ad : addresses) {

                location = String.format("%s, %s",
                        (ad.getLocality() == null ? "" : ad.getLocality()),
                        (ad.getAdminArea() == null ? "" : ad.getAdminArea()));

                sb.append("\n");
            }
            Log.d(TAG, "final location: " + location);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull
            String[] permissions, @NonNull
                    int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PHOTO_REQUEST_CODE) {
            if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE) &&
                    grantResults[0] == PERMISSION_GRANTED) {
                getPhoto();
                return;
            }
        }
        else if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PERMISSION_GRANTED) {
                setLocation();
                return;
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.createprofile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                Log.d(TAG, "in home button: ");
                return true;
            case R.id.saveField:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.drawable.logo);
                builder.setTitle("Save Changes?");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Bitmap origBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

                        ByteArrayOutputStream bitmapAsByteArrayStream = new ByteArrayOutputStream();
                        origBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bitmapAsByteArrayStream);
                        String imgString = Base64.encodeToString(bitmapAsByteArrayStream.toByteArray(), Base64.DEFAULT);
                        up = new UserProfile(first_name.getText().toString(),
                                last_name.getText().toString(),
                                location,
                                username.getText().toString(),
                                password.getText().toString(),
                                imgString,
                                        1500);
                        createProfile(up);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createProfile(UserProfile up) {
        Log.d(TAG, "createProfile activity and method: ");
        new CreateProfileAPIAsyncTask(this).execute("A20379665",
                up.getFirst_name(), up.getLast_name(),
                up.getLocation(), up.getUsername(), up.getPassword(),
                up.getImage(), Integer.toString(up.getRating()));
    }

    public void pickOption(final View w) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    MY_PHOTO_REQUEST_CODE);
        } else {
            getPhoto();
        }
    }

    public void getPhoto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.logo);
        builder.setTitle("Profile Picture");
        builder.setMessage("Take picture from: ");
        builder.setPositiveButton("Camera",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        doCamera();
                    }
                }
        );
        builder.setNegativeButton("Gallery",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doGallery();
                    }
                }
        );
        builder.setNeutralButton("Cancel",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.dismiss();
                    }
                }
        );
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void doGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_IMAGE_GALLERY);
    }

    public void doCamera() {
        currentImageFile = new File(getExternalCacheDir(), "appimage_" + System.currentTimeMillis() + ".jpg");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(currentImageFile));
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK) {
            try {
                processGallery(data);
            } catch (Exception e) {
                Toast.makeText(this, "onActivityResult: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                processCamera();
            } catch (Exception e) {
                Toast.makeText(this, "onActivityResult: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    private void processCamera() {
        Uri selectedImage = Uri.fromFile(currentImageFile);
        imageView.setImageURI(selectedImage);
        Bitmap bm = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

        currentImageFile.delete();
    }

    private void processGallery(Intent data) {
        Uri galleryImageUri = data.getData();
        if (galleryImageUri == null)
            return;

        InputStream imageStream = null;
        try {
            imageStream = getContentResolver().openInputStream(galleryImageUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
        imageView.setImageBitmap(selectedImage);
        add.setImageResource(0);
    }

    public static void makeCustomToast(Context context, int time) {
        Toast toast = Toast.makeText(context, "User Create Successful", time);
        View toastView = toast.getView();
        toastView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
        TextView tv = toastView.findViewById(android.R.id.message);
        tv.setPadding(250, 100, 250, 100);
        tv.setTextColor(Color.WHITE);
        toast.show();
    }

    public void errorMessage(boolean error, String connectionResult) {
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
            makeCustomToast(CreateProfileActivity.this, Toast.LENGTH_LONG);
            // add the user profile to the database
            // then start the login activity
            Intent intent = new Intent(CreateProfileActivity.this, ProfileActivity.class);
            intent.putExtra(extraName, up); // Better be Serializable!
            startActivity(intent);
        }
    }
}
