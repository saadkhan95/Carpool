package com.example.googleautocomplete;


import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends AppCompatActivity {


    GoogleMap map;
    ArrayList<LatLng> markerPoints;
    double lat;
    double lng;
    LatLng location;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        // Initializing
        markerPoints = new ArrayList<LatLng>();

    //    Getting reference to SupportMapFragment of the activity_main
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

     //   Getting Map for the SupportMapFragment
        map = fm.getMap();
        float def=0;
        Intent inten=getIntent();
        double s1=inten.getDoubleExtra("Lat", 0);
        double s2=inten.getDoubleExtra("Long",0);
        final LatLng current = new LatLng(32.578891,71.560526);
        final LatLng destination = new LatLng(s1,s2);
        System.out.println("lat= " + s1 + " long= " + s2);
        Toast.makeText(MapsActivity.this, s1 + " " + s2,
                Toast.LENGTH_LONG)
                .show();
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(30.3753, 69.3451), 5f), 4000, null);
        if (map != null) {

      //      Enable MyLocation Button in the Map
            map.setMyLocationEnabled(true);

            markerPoints.add(current);
            MarkerOptions options = new MarkerOptions();
            options.position(current);
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            map.addMarker(options);
            markerPoints.add(destination);
            options.position(destination);
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            map.addMarker(options);
            LatLng origin = markerPoints.get(0);
            LatLng dest = markerPoints.get(1);

            // Getting URL to the Google Directions API
            String url = getDirectionsUrl(origin, dest);

            DownloadTask downloadTask = new DownloadTask();

            // Start downloading json data from Google Directions API
            downloadTask.execute(url);
            // Setting onclick event listener for the map
//            map.setOnMapClickListener(new OnMapClickListener() {
//
//                @Override
//                public void onMapClick(LatLng point) {
//
//                    // Already two locations
//                    if (markerPoints.size() > 1) {
//                        markerPoints.clear();
//                        map.clear();
//                    }
//
//                    // Adding new item to the ArrayList
//                    if(markerPoints.size() == 1)
//                    {
//                        markerPoints.add(destination);
//                    }
//                    markerPoints.add(point);
//
//                    // Creating MarkerOptions
//                    MarkerOptions options = new MarkerOptions();
//
//                    // Setting the position of the marker
//                    options.position(point);
//
//                    /**
//                     * For the start location, the color of marker is GREEN and
//                     * for the end location, the color of marker is RED.
//                     */
//                    if (markerPoints.size() == 1) {
//                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
//                    } else if (markerPoints.size() == 2) {
//                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
//                    }
//
//                    // Add new marker to the Google Map Android API V2
//                    map.addMarker(options);
//
//                    // Checks, whether start and end locations are captured
//                    if (markerPoints.size() >= 2) {
//                        LatLng origin = markerPoints.get(0);
//                        LatLng dest = markerPoints.get(1);
//
//                        // Getting URL to the Google Directions API
//                        String url = getDirectionsUrl(origin, dest);
//
//                        DownloadTask downloadTask = new DownloadTask();
//
//                        // Start downloading json data from Google Directions API
//                        downloadTask.execute(url);
//                    }
//                }
//            });
        }

    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception while downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.CYAN);
                lineOptions.width(10);
            }

            // Drawing polyline in the Google Map for the i-th route
            map.addPolyline(lineOptions);
        }
    }
}