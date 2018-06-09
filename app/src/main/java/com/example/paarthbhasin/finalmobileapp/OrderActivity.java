package com.example.paarthbhasin.finalmobileapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;

/**
 * Created by paarthbhasin on 28/4/18.
 */

/**
 * DESCRIPTION:
 * This screen allows the user to create different orders.
 * Each order requires the following information in order to be processed correctly:
 * - Order Description
 * - Order Image
 * - Order Location
 * - Order Destination
 * - Order Price
 * - Extra Tip
 *
 * If you don't provide all the above fields, the order will not be created and an error message
 * will be displayed.
 *
 * To process the order and its payment, Sandbox Braintree_PayPal SDK has been used.
 * It gives the feel of a real transaction occurring (requiring Credit Card Details), whilst ensuring
 * you can test it with mock values.
 *
 * The app communicates with the backend Braintree Server (in PHP) , which runs as a XAMPP server
 * on 127.0.0.1 (localhost loopback). The emulator is connected to the localhost loopback on the
 * address 10.0.2.2. This was determined from the Android documentation
 * as follows:
 *              https://developer.android.com/studio/run/emulator-networking
 *
 * Once the payment goes through, the order is created under user's account ID, on the Firebase
 * Realtime Database.
 *
 * The idea of the implementation of AutoCompleteTextView with PlacesArrayAdapter has been taken
 * and modified from the following tutorial and StackOverflow link:
 *          - http://www.truiton.com/2015/04/android-places-api-autocomplete-getplacebyid/
 *          - https://stackoverflow.com/questions/25928948/get-lat-lang-from-a-place-id-returned-by-autocomplete-place-api
 *
 * The implementation of Braintree_Paypal SDK for Android was studied and modified from the following
 * URLs:
 *          - https://inducesmile.com/android/using-braintree-payment-in-android-to-accept-payment/
 *          - https://developers.braintreepayments.com/guides/client-sdk/setup/android/v2
 *          - https://developers.braintreepayments.com/guides/drop-in/setup-and-integration/android/v2
 */

public class OrderActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    // Reference to all orders.
    private DatabaseReference mOrderReference = FirebaseDatabase.getInstance().getReference().child("orders");
    // Reference to all users.
    private DatabaseReference mUserReference = FirebaseDatabase.getInstance().getReference().child("users");
    // Reference to Firebase storage. To upload images accordingly.
    private StorageReference mStorageReference = FirebaseStorage.getInstance().getReference();
    private RelativeLayout mLayout; // Activity layout
    private FirebaseAuth mAuth; // FirebaseAuth instance for handling authentication
    private EditText mAmount; // Item amount field
    private EditText mTip; // Item tip field
    private Float amount = 0f; // item amount
    private Float tip = 0f; // Item tip
    private String itemLoc = ""; // Item location address
    private String deliverAdd = ""; // Item destination address
    private ArrayList<AlertDialog.Builder> alertBuilders = new ArrayList<AlertDialog.Builder>(); // All the alerts for responding to incorrect input
    private static final String LOG_TAG = "OrderActivity";
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private static final int PICK_IMAGE_REQUEST = 71;
    private String orderID; // Each order has a unique Order ID (timestamp)
    private String mFullName; // Name of person who ordered
    private LatLng srcLoc; // Source latitude & Longitude of item
    private LatLng dstLoc; // Destination latitude & Longitude of item
    private String mNumber; // Mobile number of the user who is going to place the order
    private EditText mItemDesc; // Item Description
    private AutoCompleteTextView mItemLocationAutoComplete; // Item Location Autocomplete
    private AutoCompleteTextView mDeliveryAddressAutoComplete; // Item Destination Autocomplete
    private GoogleApiClient mGoogleApiClient; // Google Maps Client. For querying Google Maps Server
    private static final String TAG = OrderActivity.class.getSimpleName();
    // To communicate with Braintree_PayPal server
    private static final String PATH_TO_SERVER = "http://10.0.2.2/braintree_paypal/index.php";
    private String clientToken; // Token sent to the server
    private Uri filePath; // Location of item image
    private Bitmap itemImage = null; // Initially item image is null
    private Drawable upload = null; // For changing colour of upload button
    private ProgressBar spinner; // Used when order is being processed
    private static final int BRAINTREE_REQUEST_CODE = 4949;
    private PlacesArrayAdapter mPlaceArrayAdapter; // For populating AutoCompleteTextView with places


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order); // Setup activity UI
        // customize toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_top);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.bt_white_pressed), PorterDuff.Mode.SRC_ATOP);
        TextView title = (TextView) findViewById(R.id.toolbarTitle);
        title.setTypeface(null, Typeface.BOLD_ITALIC);
        title.setText(R.string.place_order);
        mGoogleApiClient = new GoogleApiClient.Builder(OrderActivity.this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build();
        mLayout = findViewById(R.id.rl3); // Activity Layout
        mAuth = FirebaseAuth.getInstance();
        spinner = (ProgressBar) findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);
        Button paymentButton = (Button) findViewById(R.id.paymentButton); // To process payment requests
        paymentButton.setOnClickListener(this);
        // Setup links to Views
        mAmount = (EditText) findViewById(R.id.paymentAmount);
        mTip = findViewById(R.id.tip);
        mItemDesc = (EditText) findViewById(R.id.itemName);
        mItemLocationAutoComplete = (AutoCompleteTextView) findViewById(R.id.itemLocation);
        mDeliveryAddressAutoComplete = (AutoCompleteTextView) findViewById(R.id.deliveryAddress);
        setOnClickListeners();
        mPlaceArrayAdapter = new PlacesArrayAdapter(this, android.R.layout.simple_list_item_1,
                null, null);
        mItemLocationAutoComplete.setAdapter(mPlaceArrayAdapter);
        mDeliveryAddressAutoComplete.setAdapter(mPlaceArrayAdapter);
        getClientTokenFromServer(); // Initialise connection to Braintree_PayPal backend server
        DatabaseReference userRef = mUserReference.child(mAuth.getCurrentUser().getUid());
        userRef.addValueEventListener(new ValueEventListener() { // Setup user information
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mFullName = dataSnapshot.child("name").getValue(String.class);
                mNumber = dataSnapshot.child("number").getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Set click listeners for AutoCompleteTextView suggestions (places).
    private void setOnClickListeners() {
        mItemLocationAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final PlacesArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(i);
                final String placeId = String.valueOf(item.placeId);
                Log.i(LOG_TAG, "Selected: " + item.description);
                itemLoc = item.description.toString();
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, placeId);
                placeResult.setResultCallback(mSrcUpdatePlaceDetailsCallback);
            }
        });
        mDeliveryAddressAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final PlacesArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(i);
                final String placeId = String.valueOf(item.placeId);
                Log.i(LOG_TAG, "Selected: " + item.description);
                deliverAdd = item.description.toString();
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
                srcLoc = place.getLatLng();
        }
    };

    // get destination location back upon clicking
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
                dstLoc = place.getLatLng();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_main; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_order, menu);
        upload = menu.findItem(R.id.upload).getIcon();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.upload) {
            chooseImage();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) { // handle button clicks
        switch (view.getId()) {
            case android.R.id.home: // Go back home
                finish();
                break;
            case R.id.paymentButton: // Braintree_PayPal payment
                if (detailsValid()) {
                    spinner.setVisibility(View.VISIBLE);
                    processPayment();
                }
                break;
            case R.id.upload: // Upload item image
                chooseImage();
                break;
            default:
                break;
        }
    }

    private boolean detailsValid() { // Check whether user has supplied all info, before processing order
        if (mAmount.getText().toString().length() == 0 || mTip.getText().toString().length() == 0 || dstLoc == null ||
                srcLoc == null || itemImage == null || mItemDesc.getText().toString().length() == 0) {
            createSnackbar("Incomplete order details. Need item description, source & destination address, amount, tip and image");
            return false;
        }
        return true;
    }

    // Process the payment. Start the Braintree Money transaction via Credit/Debit card.
    // This happens in activities define by Braintree_PayPal Drop-in UI, which is 3rd Party Android Activity
    private void processPayment() {
        tip = Float.parseFloat(mTip.getText().toString());
        amount = Float.parseFloat(mAmount.getText().toString()) + tip;
        DropInRequest dropInRequest = new DropInRequest().clientToken(clientToken).requestThreeDSecureVerification(true);
        dropInRequest.amount(amount.toString()).disablePayPal();
        startActivityForResult(dropInRequest.getIntent(this), BRAINTREE_REQUEST_CODE);
    }

    // Select image to upload from phone/emulate gallery
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    // Get response from Braintree Server whether connection was successful
    private void getClientTokenFromServer() {
        AsyncHttpClient androidClient = new AsyncHttpClient();
        androidClient.get(PATH_TO_SERVER, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, getString(R.string.token_failed) + responseString);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseToken) {
                Log.d(TAG, "Client token: " + responseToken);
                clientToken = responseToken;
            }
        });
    }

    @Override
    // Get response from Braintree Drop-in UI after credit card details were submitted
    // Get response from Image Gallery after image was selected
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BRAINTREE_REQUEST_CODE) {
            if (RESULT_OK == resultCode) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                String paymentNonce = result.getPaymentMethodNonce().getNonce();
                //send to your server
                Log.d(TAG, "Testing the app here");
                sendPaymentNonceToServer(paymentNonce, amount.toString());
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d(TAG, "User cancelled payment");
            } else {
                Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
                Log.d(TAG, " error exception");
            }
        } else if (requestCode == PICK_IMAGE_REQUEST)
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                filePath = data.getData();
                try {
                    itemImage = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                    upload.mutate();
                    upload.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);
                } catch (IOException e) {
                    itemImage = null;
                    upload.mutate();
                    upload.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                    e.printStackTrace();
                    Log.e("ERROR CHOOSING IMAGE FROM GALLERY", e.toString());
                }
            }
    }

    // Send payment data over to server via HTTP Post Request
    private void sendPaymentNonceToServer(String paymentNonce, String amt) {
        RequestParams params = new RequestParams();
        params.put("NONCE", paymentNonce);
        params.put("AMOUNT", amt);

        AsyncHttpClient androidClient = new AsyncHttpClient();
        androidClient.post(PATH_TO_SERVER, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "Error: Failed to create a transaction");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.d(TAG, "Output " + responseString);
                getAlertBuilders();
                saveOrderDetails();
                alertBuilders.get(0).show();
            }
        });
    }

    // Save the order details to Firebase Database, if order processing was successful
    private void saveOrderDetails() {
        // Save the order to Firebase database and image to firebase storage.
        orderID = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + "";
        final String userID = mAuth.getCurrentUser().getUid();
        final DatabaseReference childRef = mOrderReference.child(userID).child(orderID);
        childRef.child("name").setValue(mFullName);
        childRef.child("number").setValue(mNumber);
        childRef.child("price").setValue(amount);
        childRef.child("orderID").setValue(orderID);
        childRef.child("tip").setValue(tip);
        childRef.child("itemLoc").setValue(itemLoc);
        childRef.child("destLoc").setValue(deliverAdd);
        childRef.child("itemLong").setValue(srcLoc.longitude);
        childRef.child("itemLat").setValue(srcLoc.latitude);
        childRef.child("destLat").setValue(dstLoc.latitude);
        childRef.child("destLong").setValue(dstLoc.longitude);
        childRef.child("status").setValue("Unassigned");
        childRef.child("itemDesc").setValue(mItemDesc.getText().toString());
        final long date = Calendar.getInstance().getTimeInMillis();
        StorageReference fileReference = mStorageReference.child(userID + "/" + date + ".jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        itemImage.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        UploadTask upload = fileReference.putBytes(baos.toByteArray());
        upload.addOnCompleteListener(this, new
                OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            UploadTask.TaskSnapshot taskSnapshot = task.getResult();
                            createSnackbar("Image uploaded!");
                            childRef.child("imageUrl")
                                    .setValue(taskSnapshot.getDownloadUrl().toString());
                        } else {
                            createSnackbar("Image failed to upload");
                        }
                    }
                });
        emptyFields(); // Reset all the field once transaction is complete
        spinner.setVisibility(View.GONE);
    }

    // Reset all the order data previously supplied
    private void emptyFields() {
        mDeliveryAddressAutoComplete.setText("");
        mItemDesc.setText("");
        mItemLocationAutoComplete.setText("");
        mAmount.setText("");
        mTip.setText("");
        amount=0f;
        tip=0f;
        itemImage = null;
        upload.mutate();
        upload.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        srcLoc = null;
        dstLoc = null;
    }

    // Alert(s) for responding to incorrect input from user
    private ArrayList<AlertDialog.Builder> getAlertBuilders() {
        alertBuilders.clear();
        AlertDialog.Builder mPayment = new AlertDialog.Builder(this)
                .setTitle("Payment Successful")
                .setMessage("Thank you! Your payment was successful. An amount of $" +
                        amount.toString() + " has been debited from your credit/debit card.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        /*Intent placed = new Intent(getApplicationContext(), PlacedOrdersActivity.class);
                        startActivity(placed);*/
                    }
                });
        alertBuilders.add(mPayment);
        return alertBuilders;
    }

    // Display Snackbar info message
    private void createSnackbar(String message) {
        Snackbar.make(mLayout, message, Snackbar.LENGTH_LONG).show();
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

}



