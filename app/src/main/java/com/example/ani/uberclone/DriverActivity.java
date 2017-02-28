package com.example.ani.uberclone;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class DriverActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Intent intent;

    public void acceptRequest(View view) {
        ParseQuery<ParseObject> query = new ParseQuery<>("Request");
        query.whereEqualTo("username", intent.getStringExtra("username"));
        query.setLimit(1);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null) {
                    if(objects.size() > 0) {
                        for(int i = 0; i < objects.size(); i++) {
                            objects.get(i).put("driverUsername", ParseUser.getCurrentUser().getUsername());
                            objects.get(i).saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e == null) {
                                        Intent directionsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr=" + intent.getDoubleExtra("driverLatitude", 0.0) + "," + intent.getDoubleExtra("driverLongitude", 0.0) + "&daddr=" + intent.getDoubleExtra("requestLatitude", 0.0) + "," + intent.getDoubleExtra("requestLongitude", 0.0)));
                                        startActivity(directionsIntent);
                                        finish();
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        intent = getIntent();

        RelativeLayout mapLayout = (RelativeLayout) findViewById(R.id.activity_driveractivity);
        mapLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ArrayList<Marker> markers = new ArrayList<>();

                LatLng driverLocation = new LatLng(intent.getDoubleExtra("driverLatitude", 0.0), intent.getDoubleExtra("driverLongitude", 0.0));
                LatLng requestLocation = new LatLng(intent.getDoubleExtra("requestLatitude", 0.0), intent.getDoubleExtra("requestLongitude", 0.0));
                markers.add(mMap.addMarker(new MarkerOptions().position(driverLocation).title("Your Location")));
                markers.add(mMap.addMarker(new MarkerOptions().position(requestLocation).title("Request Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for(Marker marker : markers) {
                    builder.include(marker.getPosition());
                }
                LatLngBounds bounds = builder.build();

                int padding = 200;
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.animateCamera(cameraUpdate);
            }
        });
    }
}
