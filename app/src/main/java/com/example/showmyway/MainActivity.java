package com.example.showmyway;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.showmyway.directionhelpers.FetchURL;
import com.example.showmyway.directionhelpers.TaskLoadedCallback;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.Manifest;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback, LocationListener {
    GoogleMap map;

    private MarkerOptions destination, origin;

    List<MarkerOptions> markers = new ArrayList<MarkerOptions>();


    Marker marker;

    private Polyline currentPolyline;

    final static String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    final static int PERMISSION_ALL = 1;
    LocationManager locationManager;
    private static final int GPS_TIME_INTERVAL = 1000 * 30; // get gps location every 1 min
    private static final int GPS_DISTANCE = 1000; // set the distance value in meter
    private static final int HANDLER_DELAY = 1000 * 10;
    private static final int START_HANDLER_DELAY = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //To get current location


        //For the permission
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(PERMISSIONS, PERMISSION_ALL);
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                //Log.d("check1","check1");
                requestLocation();
                handler.postDelayed(this, HANDLER_DELAY);
            }
        }, START_HANDLER_DELAY);


        //Initializing the map fragment
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFrag);
        mapFragment.getMapAsync(this);





    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
    }


    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
//        if (currentPolyline != null)
//            currentPolyline.remove();
        Log.d("values", String.valueOf(values));
        currentPolyline = map.addPolyline((PolylineOptions) values[0]);
        currentPolyline.setColor(R.color.purple_200);
        currentPolyline.setWidth(10);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (origin == null) {

            origin = new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("origin");


            //To set the marker icon
            int height = 100;
            int width = 100;
            BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.city);
            Bitmap b = bitmapdraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
            marker = map.addMarker(origin.icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
            markers.add(origin);

        } else {

            destination = new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("destination");

            // marker = map.addMarker(destination);
            markers.add(destination);
            //Log.d("destination", String.valueOf(destination));
        }



            for (int i = 0; i < markers.size(); i++) {
                Log.d("marksize", String.valueOf(markers.size()));


                if ((i + 1) >= markers.size()) {
                    break;
                } else {
                    new FetchURL(MainActivity.this).execute(getUrl(markers.get(i).getPosition(), markers.get(i + 1).getPosition(), "driving"), "driving");

                }
            }



//        Log.d("mylog", "Got Location: " + location.getLatitude() + ", " + location.getLongitude());
//        Toast.makeText(MainActivity.this, "Got Coordinates: " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();
        locationManager.removeUpdates(this);
    }


    private void requestLocation() {
        if (locationManager == null)
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        GPS_TIME_INTERVAL, GPS_DISTANCE, this);
            }
        }
    }


}