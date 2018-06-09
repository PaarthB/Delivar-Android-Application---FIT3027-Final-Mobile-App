package com.example.paarthbhasin.finalmobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by paarthbhasin on 28/4/18.
 */

/**
 * DESCRIPTION:
 * This screen is the home page, that the user is redirected to when the user first
 * logs in to the app via the Login Activity (MainActivity).
 * This is the central screen for navigation throughout the App.
 * It allows the user to navigate the app and perform several actions, including:
 *      - Delivering Orders
 *      - Placing Orders
 *      - Viewing Placed Orders
 *      - Viewing Assigned Orders (Deliveries)
 *      - Viewing Account Information
 *      - Signing-out from the App.
 */

public class HomePageActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener, View.OnClickListener {

    // Reference to all users in the Database
    private DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users");
    private FloatingActionButton mDetails; // For going to Account details screen
    private FloatingActionButton mOrders; // For going to Placed Orders screen
    private FloatingActionButton mDeliveries; // For going to Assigned Deliveries screen
    private FirebaseAuth mAuth; // Firebase Auth Instance
    private RelativeLayout mLayout; // Activity layout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // Set Activity UI
        // Customize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_top);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mLayout = findViewById(R.id.rl2); // Activity Layout
        final TextView title = findViewById(R.id.toolbarTitle);
        if (getIntent().getExtras() != null)
            createSnackbar(getIntent().getStringExtra("success"));
        mAuth = FirebaseAuth.getInstance();
        // Link UI elements defined in XML to code
        Button orderButton = findViewById(R.id.order);
        Button deliverButton = findViewById(R.id.deliver);
        mDetails = findViewById(R.id.myAccountFAB);
        mOrders = findViewById(R.id.myOrderFAB);
        mDeliveries = findViewById(R.id.myDeliveriesFAB);
        orderButton.setOnClickListener(this);
        mDetails.setOnClickListener(this);
        mOrders.setOnClickListener(this);
        mDeliveries.setOnClickListener(this);
        deliverButton.setOnClickListener(this);
        DatabaseReference userRef = mDatabaseRef.child(mAuth.getCurrentUser().getUid());
        // Set the Toolbar title to display user's name. Need to contact the Firebase database for the same.
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String fName = dataSnapshot.child("name").getValue(String.class);
                String toolBarTitle = getResources().getString(R.string.welcome) + " " + fName + "!";
                title.setText(toolBarTitle); // Hello User Name
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // Handle menu item clicks
        if (item.getItemId() == R.id.signOut) { // Sign out when clicked o sign out button
            mAuth.signOut();
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_main; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(this);
    }

    public void onResume() {
        super.onResume();
    }

    public void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(this);
    }

    @Override
    public void onClick(View view) { // Handle button clicks
        switch (view.getId()) {
            case R.id.order: // Create an Order
                Intent i = new Intent(this, OrderActivity.class);
                startActivity(i);
                break;
            case R.id.deliver: // Deliver orders
                i = new Intent(this, DeliverActivity.class);
                startActivity(i);
                break;
            case R.id.myAccountFAB: // View my account details
                i = new Intent(this, AccountDetailsActivity.class);
                startActivity(i);
                break;
            case R.id.myDeliveriesFAB: // View my assigned deliveries
                Log.i("CLICK", "DELIVERY FAB");
                i = new Intent(this, AssignedOrdersActivity.class);
                startActivity(i);
                break;
            case R.id.myOrderFAB: // View my placed orders
                Log.i("CLICK", "ORDER FAB");
                i = new Intent(this, PlacedOrdersActivity.class);
                startActivity(i);
                break;
            default:
                break;
        }
    }

    // Display Snackbar info messages
    private void createSnackbar(String message) {
        Snackbar.make(mLayout, message, Snackbar.LENGTH_LONG).show();
    }


    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

    }
}
