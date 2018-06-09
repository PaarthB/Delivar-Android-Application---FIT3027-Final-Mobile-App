package com.example.paarthbhasin.finalmobileapp;

import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by paarthbhasin on 12/5/18.
 */

/**
 * DESCRIPTION: This screen provides the user the functionality, to filter all orders in the database,
 * that match the route/journey the user is about to take.
 *
 * It asks the user for start and end points of the journey, and then constructs a route between the two
 * points. Any orders in the system, that lie on the path, or within 1.5km from it, are displayed.
 *
 * This allows the deliverer, to fetch and deliver items, whilst completing their journey,
 * which isn't affected as the items lie on the journey itself.
 *
 * Once the filtered orders are displayed, the user (deliverer), can select them for delivery.
 *
 * The population of these filtered orders in the ListView, is done by the OrderFilterAdapter. This
 * activity builds the filtered order list, and sends it as the data source to the adapter.
 *
 * The idea of the implementation of AutoCompleteTextView with PlacesArrayAdapter has been taken
 * and modified from the following URL:
 * http://www.truiton.com/2015/04/android-places-api-autocomplete-getplacebyid/
 */


public class DeliverActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    // Reference to all the orders in the Firebase Database. Root tag is "orders"
    private DatabaseReference mOrderReference = FirebaseDatabase.getInstance().getReference().child("orders");
    private RelativeLayout mLayout; // Activity Layout
    private FirebaseAuth mAuth; // Firebase Auth instance
    LatLng start = null; // Starting location. null initially.
    LatLng end = null; // Ending location. null initially.
    boolean pressed = true; // Used to set whether the filtering process should start or not.
    private ListView mListView;
    private ProgressBar spinner; // For until the processing of filtered orders is not complete
    private static final String LOG_TAG = "DeliverActivity";
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private AutoCompleteTextView mStartLocationAutoComplete; // Start location suggestion (autocomplete)
    private AutoCompleteTextView mEndLocationAutoComplete; // End location suggestion (autocomplete)
    private PlacesArrayAdapter mPlaceArrayAdapter; // Populates the AutoCompleteTextView with places
    private OrderFilterAdapter mOrderFilterAdapter; // Displays filtered orders in the ListView
    private GoogleApiClient mGoogleApiClient; // To get data from Google Maps API
    private ArrayList<Order> mOrderList = new ArrayList<>(); // List of filtered orders.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliver); // Inflate Activity UI
        // Customize Activity Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_top);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        spinner = (ProgressBar) findViewById(R.id.progressBar2);
        spinner.setVisibility(View.GONE); // No spinner initially as filtering has not started
        // Setup mGoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(DeliverActivity.this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build();
        mLayout = findViewById(R.id.rl6);
        mAuth = FirebaseAuth.getInstance();
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.bt_white_pressed), PorterDuff.Mode.SRC_ATOP);
        TextView title = (TextView) findViewById(R.id.toolbarTitle);
        title.setTypeface(null, Typeface.BOLD_ITALIC);
        title.setText(R.string.deliver_orders);
        Button filterOrders = (Button) findViewById(R.id.filterOrderButton);
        filterOrders.setOnClickListener(this);
        // Setup AutoCompleteTextView
        mStartLocationAutoComplete = (AutoCompleteTextView) findViewById(R.id.autoCompleteStartLoc);
        mEndLocationAutoComplete = (AutoCompleteTextView) findViewById(R.id.autoCompleteEndLoc);
        setOnClickListeners();
        // Setup adapters. Need two, one for AutoCompleteTextView(s), one for ListView
        mPlaceArrayAdapter = new PlacesArrayAdapter(this, android.R.layout.simple_list_item_1,
                null, null);
        mOrderFilterAdapter = new OrderFilterAdapter(this, mOrderList);
        mStartLocationAutoComplete.setAdapter(mPlaceArrayAdapter);
        mEndLocationAutoComplete.setAdapter(mPlaceArrayAdapter);
        mListView = (ListView) findViewById(R.id.listViewDeliveries);
        mListView.setFocusableInTouchMode(true);
        mListView.setFocusable(true);
        mListView.setAdapter(mOrderFilterAdapter);
    }

    // Set click listeners for AutoCompleteTextView suggestions (places).
    private void setOnClickListeners() {
        mStartLocationAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final PlacesArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(i);
                final String placeId = String.valueOf(item.placeId);
                Log.i(LOG_TAG, "Selected: " + item.description);
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, placeId);
                placeResult.setResultCallback(mSrcUpdatePlaceDetailsCallback);
            }
        });
        mEndLocationAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final PlacesArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(i);
                final String placeId = String.valueOf(item.placeId);
                Log.i(LOG_TAG, "Selected: " + item.description);
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, placeId);
                placeResult.setResultCallback(mDestUpdatePlaceDetailsCallback);
            }
        });
    }

    // get start location back upon clicking
    private ResultCallback<PlaceBuffer> mSrcUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(LOG_TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            if (place != null)
                start = place.getLatLng();
        }
    };

    // get end location back upon clicking
    private ResultCallback<PlaceBuffer> mDestUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(LOG_TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            if (place != null)
                end = place.getLatLng();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    public void onStart() {
        super.onStart();
    }

    public void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View view) { // Handle button clicks
        switch (view.getId()) {
            case android.R.id.home: // Go back home
                finish();
                break;
            case R.id.filterOrderButton: // Match Orders that lie on your journey path
                Log.i("HEY", "WORKS");
                pressed = true;
                if (validDetails()) {
                    mOrderList.clear();
                    spinner.setVisibility(View.VISIBLE);
                    filterOrders();
                }
                break;
            default:
                break;
        }
    }

    // Check whether the user has entered valid start and end locations if they want to filter orders.
    private boolean validDetails() {
        Log.i("HEY3", "WORKS");
        if ((start == null || end == null) && !pressed) {
            return false;
        }
        Log.i("HEY2", "WORKS");
        if ((start == null || end == null) && pressed) {
            Log.i("HEY", "WORKS");
            createSnackbar("Incomplete delivery details. Need both source & destination address.");
            pressed = false;
            return false;
        }
        return true;
    }

    // Display SnackBar alert
    private void createSnackbar(String message) {
        Snackbar.make(mLayout, message, Snackbar.LENGTH_LONG).show();
    }


    // Builds the Google Maps Direction URL, that is used to query Google Maps for getting path
    // between two locations. That is, if you call this URL, you get a JSON response from Google, containing
    // the journey from 'origin' to 'dest'
    private String getMapsApiDirectionsUrl(LatLng origin, LatLng dest) {
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

    // Filter the orders. Make this work in background thread (AsyncTask) so that the current UI does not freeze/hang
    private void filterOrders() {
        Log.i("ALGORITHM", "STARTED");
        new LoadViewTask().execute(getMapsApiDirectionsUrl(start, end));
    }

    // Returns the distance between two points, each having one Latitude and Longitude
    private float getDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] distance = new float[2];
        Location.distanceBetween(lat1, lon1, lat2, lon2, distance);
        return distance[0];
    }

    //To use the AsyncTask, it must be subclassed
    @SuppressLint("StaticFieldLeak")
    public class LoadViewTask extends AsyncTask<String, Integer, Void> {
        @Override
        protected void onPreExecute() {

        }

        @Override // Executes in the background thread to improve User Experience
        protected Void doInBackground(String... url) {
            Log.i("doInBackgground", "REACHED");
            routeMatchingAlgorithm(buildJSON(url[0]));
            return null;
        }

        // Go through all the orders and find out which ones intersect with the journey
        // contained in 'journey'
        private void routeMatchingAlgorithm(final JSONObject journey) {
            mOrderReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.i("DATASNAPSHOT", "REACHED");
                    Log.i("KEY VALUE", Long.toString(dataSnapshot.getChildrenCount()));
                    // All order owners
                    for (DataSnapshot userOrders : dataSnapshot.getChildren()) {
                        Log.i("AUTH CHECK", userOrders.getKey());
                        // All orders owners except the user itself. (We only to filter orders that others have placed)
                        if (!userOrders.getKey().equals(mAuth.getCurrentUser().getUid())) {
                            Log.i("AUTH CHECK", "PASSED");
                            // All orders placed by a specific user
                            for (DataSnapshot orderList : userOrders.getChildren()) {
                                // Get order's source and dest location (latitude and longitude)
                                Double d1 = (Double) orderList.child("itemLat").getValue();
                                Double d2 = (Double) orderList.child("itemLong").getValue();
                                Double d3 = (Double) orderList.child("destLat").getValue();
                                Double d4 = (Double) orderList.child("destLong").getValue();
                                // See if source and dest intersect with journey
                                if (intersect(journey, new LatLng(d1, d2), new LatLng(d3, d4))
                                        && orderList.child("status").getValue().equals("Unassigned")) {
                                    Order order = new Order(orderList.child("name").getValue().toString(),
                                            userOrders.getKey(),
                                            orderList.child("itemLoc").getValue(String.class),
                                            orderList.child("destLoc").getValue(String.class),
                                            orderList.child("price").getValue(Float.class),
                                            orderList.child("tip").getValue(Float.class),
                                            orderList.child("number").getValue(String.class),
                                            orderList.child("orderID").getValue(String.class),
                                            orderList.child("imageUrl").getValue(String.class),
                                            orderList.child("itemDesc").getValue(String.class),
                                            orderList.child("status").getValue(String.class),
                                            mAuth.getCurrentUser().getUid(), // If we choose to deliver, it will have our ID
                                            null // Unassigned Deliveries don't have a Deliverer Number
                                    );
                                    mOrderList.add(order); // Populate the OrderFilterAdapter data source
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        // When filtering is complete, Adapter source is ready, so ListView can be populated
        @Override
        protected void onPostExecute(Void v) {
            mOrderFilterAdapter = new OrderFilterAdapter(getApplicationContext(), mOrderList);
            mListView.setAdapter(mOrderFilterAdapter);
            spinner.setVisibility(View.GONE); // Make the spinner disappear as processing is done
        }

        // Method that determines if "location" points lie on the path "journey"
        // Location points are item source and destination address. To return true, both source and
        // destination should lie on the journey path
        private boolean intersect(JSONObject journey, LatLng... location) {
            boolean startPresent = false; // Whether order can be picked
            boolean endPresent = false; // Whether order can be delivered
            Double sLat;
            Double sLong;
            Double eLat;
            Double eLong;
            LatLng starting = location[0]; // item location
            LatLng ending = location[1]; // Item destination
            try {
                JSONArray routes = journey.getJSONArray("routes");
                outerLoop:
                for (int i = 0; i < routes.length(); i++) {
                    JSONArray legs = ((JSONObject) routes.get(i)).getJSONArray("legs");
                    for (int j = 0; j < legs.length(); j++) {
                        JSONArray steps = ((JSONObject) legs.get(j)).getJSONArray("steps"); // Go over all the nodes
                        for (int k = 0; k < steps.length(); k++) {
                            // Each info node has a prior and current (where you came from and where you are)
                            // See whether the item source & dest intersects with any of them
                            sLat = (Double) ((JSONObject) ((JSONObject) steps.get(k)).get("start_location")).get("lat");
                            sLong = (Double) ((JSONObject) ((JSONObject) steps.get(k)).get("start_location")).get("lng");
                            eLat = (Double) ((JSONObject) ((JSONObject) steps.get(k)).get("end_location")).get("lat");
                            eLong = (Double) ((JSONObject) ((JSONObject) steps.get(k)).get("end_location")).get("lng");
                            if (checkProximity(starting, sLat, sLong, eLat, eLong))
                                startPresent = true;
                            if (startPresent && checkProximity(ending, sLat, sLong, eLat, eLong)) {
                                // End only becomes true (you can deliver), if starting is true (you can pick).
                                // That's because, you can't deliver something if you can't pick it.
                                endPresent = true;
                                break outerLoop;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return startPresent && endPresent; // Returns true if you can both pick and deliver
        }

        // Method that checks whether a location is within 1.5kms of any one of the journey points
        private boolean checkProximity(LatLng location, Double lat1, Double long1, Double lat2, Double long2) {
            if ((location.latitude == lat1 && location.longitude == long1))
                return true;
            else if ((location.latitude == lat2 && location.longitude == long2))
                return true;
            else if (getDistance(lat1, long1, location.latitude, location.longitude) <= 1500)
                return true;
            else if (getDistance(lat2, long2, location.latitude, location.longitude) <= 1500)
                return true;
            return false;
        }

        // Returns the JSON response upon querying the google maps API to get directions (route)
        private JSONObject buildJSON(String url) {
            try {
                URL downloadURL = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) downloadURL.openConnection();
                InputStream input = connection.getInputStream();
                String result = "";
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(input));
                StringBuilder sb = new StringBuilder();
                while ((result = reader.readLine()) != null) {
                    sb.append(result);
                }
                JSONObject routePoints = new JSONObject(sb.toString()); // Points from start to end
                return routePoints;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    // When Google Maps Client is ready, it is sent to mPlacesArrayAdapter as it needs it to fill up
    // AutoCompleteTextView with places
    @Override
    public void onConnected(Bundle bundle) {
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
        Log.i(LOG_TAG, "Google Places API connected.");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlaceArrayAdapter.setGoogleApiClient(null);
        Log.e(LOG_TAG, "Google Places API connection suspended.");
    }

    protected void onResume() { // Clear Filtered order list everytime you start
        super.onResume();
        mOrderList.clear();
    }

}
