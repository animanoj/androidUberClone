package com.example.ani.uberclone;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ViewRequestsActivity extends AppCompatActivity {

    ListView requestListView;
    ArrayList<String> requests = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    ArrayList<Double> requestLatitudes = new ArrayList<>();
    ArrayList<Double> requestLongitudes = new ArrayList<>();
    ArrayList<String> usernames = new ArrayList<>();

    LocationManager locationManager;
    LocationListener locationListener;

    public void updateListView(Location location) {
        requests.clear();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
        final ParseGeoPoint geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());

        if(ParseUser.getCurrentUser().get("location") == null) {
            ParseUser.getCurrentUser().put("location", new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
            ParseUser.getCurrentUser().saveInBackground();
        }

        query.whereNear("location", geoPoint);
        query.whereDoesNotExist("driverUsername");
        query.setLimit(10);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null) {
                    requestLatitudes.clear();
                    requestLongitudes.clear();

                    if(objects.size() > 0) {
                        for(int i = 0; i < objects.size(); i++) {
                            ParseGeoPoint object = (ParseGeoPoint) objects.get(i).get("location");
                            if(object != null) {
                                Double distanceInKms = (double) Math.round(geoPoint.distanceInKilometersTo(object) * 10) / 10;

                                requestLatitudes.add(object.getLatitude());
                                requestLongitudes.add(object.getLongitude());
                                usernames.add(objects.get(i).getString("username"));

                                requests.add(distanceInKms.toString() + " kms");
                                arrayAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                    else
                        Toast.makeText(getApplicationContext(), "No nearby requests", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateListView(lastKnownLocation);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.viewrequestmenu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.menuLogout) {
            ParseUser.logOut();

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1) {
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateListView(lastKnownLocation);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests);

        setTitle("Nearby Requests");

        requests.clear();

        Toast.makeText(getApplicationContext(), "Getting nearby requests", Toast.LENGTH_SHORT).show();

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, requests);

        requestListView = (ListView) findViewById(R.id.requestListView);
        requestListView.setAdapter(arrayAdapter);

        requestListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if (requestLatitudes.size() > position && requestLongitudes.size() > position && usernames.size() > position && lastKnownLocation != null) {
                        Intent intent = new Intent(getApplicationContext(), DriverActivity.class);

                        intent.putExtra("requestLatitude", requestLatitudes.get(position));
                        intent.putExtra("requestLongitude", requestLongitudes.get(position));
                        intent.putExtra("driverLatitude", lastKnownLocation.getLatitude());
                        intent.putExtra("driverLongitude", lastKnownLocation.getLongitude());
                        intent.putExtra("username", usernames.get(position));

                        startActivityForResult(intent, 1);
                    }
                }
            }
        });

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                ParseUser.getCurrentUser().put("location", new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
                ParseUser.getCurrentUser().saveInBackground();

                updateListView(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (Build.VERSION.SDK_INT < 23) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            updateListView(lastKnownLocation);
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateListView(lastKnownLocation);
            }
        }
    }
}
