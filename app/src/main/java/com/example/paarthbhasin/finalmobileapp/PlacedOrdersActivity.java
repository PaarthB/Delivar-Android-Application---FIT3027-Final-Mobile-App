package com.example.paarthbhasin.finalmobileapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by paarthbhasin on 15/5/18.
 */

/**
 * DESCRIPTION: This screen displays all the orders that have been placed by the user that has logged in.
 * The user can cancel an order, if it has not been chosen yet by anyone for delivery.
 * The user can confirm an order, once they get the required item. This functionality only appears when
 * some other user has chosen this user's order(s) for delivery.
 */

public class PlacedOrdersActivity extends AppCompatActivity {

    // Reference to all the orders in the Fireabase Database
    private DatabaseReference mOrderReference = FirebaseDatabase.getInstance().getReference().child("orders");
    // Reference to all the users in the Firebase Database
    private DatabaseReference mUserReference = FirebaseDatabase.getInstance().getReference().child("users");
    private FirebaseAuth mAuth; // FirebaseAuth instance
    private ListView mListView; // ListView for containing all the placed orders
    private String mobileNumber; // Mobile number of the user
    private boolean notified = false; // For making sure alert is displayed only once if user goes and comes back here
    private static final String LOG_TAG = "PlacedOrdersActivity";
    private PlacedOrdersAdapter mPlacedOrderAdapter; //
    private AlertDialog.Builder noOrders; // Alert stating no orders placed
    private ArrayList<Order> mOrderList = new ArrayList<>(); // List of orders placed by the user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placed_orders); // Set activity UI
        // Customize toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_top);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mAuth = FirebaseAuth.getInstance(); // get auth instance
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.bt_white_pressed), PorterDuff.Mode.SRC_ATOP);
        TextView title = (TextView) findViewById(R.id.toolbarTitle);
        title.setTypeface(null, Typeface.BOLD_ITALIC);
        title.setText(R.string.myOrder);
        noOrders = new AlertDialog.Builder(this) // Alert when there are no orders
                .setTitle("No Orders")
                .setMessage("You have not placed any orders.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

        // Initialise and setup adapter, list view and connect the two.
        mPlacedOrderAdapter = new PlacedOrdersAdapter(this, mOrderList);
        mListView = (ListView) findViewById(R.id.list_placed_orders);
        mListView.setFocusableInTouchMode(true);
        mListView.setFocusable(true);
        mListView.setAdapter(mPlacedOrderAdapter);
    }

    // Populate mOrderList to contain all the orders placed by the user.
    private void getPlacedOrders() {
        mOrderReference.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("KEY1", Long.toString(dataSnapshot.getChildrenCount()));
                // Go over all the orders for the current user
                for (DataSnapshot userOrders : dataSnapshot.getChildren()) {
                    Log.i("KEY2", userOrders.child("name").getValue().toString());
                    Log.i("KEY3", userOrders.child("itemLoc").getValue().toString());
                    Log.i("KEY4", userOrders.child("orderID").getValue().toString());
                    String delivererID = null;
                    // If order has a deliverer, then display the deliverer's mobile number
                    if (userOrders.child("delivererID").exists()) {
                        delivererID = userOrders.child("delivererID").getValue(String.class);
                        getMobileNumber(delivererID);
                        SharedPreferences editor = getSharedPreferences("mobile", MODE_PRIVATE);
                        mobileNumber = editor.getString("number", "");
                    } else mobileNumber = null; // otherwise keep it null
                    Order order = new Order(userOrders.child("name").getValue().toString(),
                            mAuth.getCurrentUser().getUid(),
                            userOrders.child("itemLoc").getValue(String.class),
                            userOrders.child("destLoc").getValue(String.class),
                            userOrders.child("price").getValue(Float.class),
                            userOrders.child("tip").getValue(Float.class),
                            userOrders.child("number").getValue(String.class),
                            userOrders.child("orderID").getValue(String.class),
                            userOrders.child("imageUrl").getValue(String.class),
                            userOrders.child("itemDesc").getValue(String.class),
                            userOrders.child("status").getValue(String.class),
                            delivererID,
                            mobileNumber
                    );
                    mOrderList.add(order);
                }
                Log.i("SIZE", Integer.toString(mOrderList.size()));
                if (mOrderList.size() == 0 && !notified) { // Show alert if not already shown. Don't show again
                    noOrders.show();
                    notified = true;
                }
                mPlacedOrderAdapter.notifyDataSetChanged(); // Update the List view with the placed orders
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Returns the mobile number for a specific user. Queries the Firebase Database
    private void getMobileNumber(String userID) {
        final DatabaseReference childRef = mUserReference.child(userID);
        childRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Using shared preferences to store mobile number.
                // This is because accessing local variables is not possible from here without
                // declaring them final (which can't be done as it keeps changing)
                String mobile = dataSnapshot.child("number").getValue(String.class);
                Log.i("NUMBER", mobile);
                SharedPreferences.Editor editor = getSharedPreferences("mobile", MODE_PRIVATE).edit();
                editor.putString("number", mobile);
                editor.apply();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_main; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_navigation, menu);
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
        switch (item.getItemId()) {
            case android.R.id.home: // Go back home
                finish();
                return true;
            case R.id.accountMenu: // Go to Account details activity
                Intent i = new Intent(this, AccountDetailsActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() { // Clear the data source and reload ListView every time activity restarts
        super.onResume();
        mOrderList.clear();
        getPlacedOrders();
        mPlacedOrderAdapter.notifyDataSetChanged();
    }

}
