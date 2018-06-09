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
import android.widget.AdapterView;
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
 * DESCRIPTION:
 * This screen displays all the orders that a user has decided to deliver. It fetches
 * all the orders selected for delivery from the Firebase Realtime Database. The user has the option
 * to cancel delivery for any of the assigned orders, at any time. That is, they are not bound to a
 * delivery if they chose it before.
 * <p>
 * This app works on the basis of crowd-sourcing. It is up-to the "crowd" at all times to decide
 * whether or not to deliver something, because they are doing this on their own will.
 * It is not their duty or obligation to complete a delivery they started.
 */


public class AssignedOrdersActivity extends AppCompatActivity {

    private DatabaseReference mDeliveryRef = FirebaseDatabase.getInstance().getReference().child("deliveries");
    private RelativeLayout mLayout;
    private FirebaseAuth mAuth;
    private ListView mListView;
    private boolean notified = false;
    private static final String LOG_TAG = "AssignedOrdersActivity";
    private AssignedOrdersAdapter mAssignedOrderAdapter;
    private ArrayList<Order> mOrderList = new ArrayList<>();
    private AlertDialog.Builder noDeliveries;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assigned_orders); // Setup the activity UI
        // Customize Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_top);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.bt_white_pressed), PorterDuff.Mode.SRC_ATOP);
        TextView title = (TextView) findViewById(R.id.toolbarTitle);
        title.setTypeface(null, Typeface.BOLD_ITALIC);
        title.setText(R.string.myDeliveries);
        mLayout = findViewById(R.id.rl8);
        mAuth = FirebaseAuth.getInstance(); // Instantiate Firebase Auth instance
        mAssignedOrderAdapter = new AssignedOrdersAdapter(this, mOrderList); // Initialise Adapter
        mListView = (ListView) findViewById(R.id.list_assigned); // Setup list view
        // Alert message
        noDeliveries = new AlertDialog.Builder(this)
                .setTitle("No Deliveries")
                .setMessage("You have no assigned deliveries.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        mListView.setAdapter(mAssignedOrderAdapter);
    }

    // Get all the assigned orders from the user's userID reference in the Firebase Database.
    // This is stored under the root tag of "deliveries" within Firebase Realtime Database
    private void getAssignedOrders() {
        mDeliveryRef.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userOrders : dataSnapshot.getChildren()) {
                    // Get all the values from the node for each delivery. And create an Order object
                    Order order = new Order(userOrders.child("name").getValue(String.class),
                            userOrders.child("userID").getValue(String.class),
                            userOrders.child("itemLoc").getValue(String.class),
                            userOrders.child("destLoc").getValue(String.class),
                            userOrders.child("price").getValue(Float.class),
                            userOrders.child("tip").getValue(Float.class),
                            userOrders.child("number").getValue(String.class),
                            userOrders.child("orderID").getValue(String.class),
                            userOrders.child("imageUrl").getValue(String.class),
                            userOrders.child("itemDesc").getValue(String.class),
                            userOrders.child("status").getValue(String.class),
                            mAuth.getCurrentUser().getUid(),
                            null
                    );
                    Log.i("FILENAME", userOrders.child("imageUrl").getValue(String.class));
                    mOrderList.add(order); // Populate list for adapter
                }
                Log.i("SIZE", Integer.toString(mOrderList.size()));
                if (mOrderList.size() == 0 && !notified) {
                    noDeliveries.show(); // Show alert if user has no deliveries
                    notified = true;
                }
                mAssignedOrderAdapter.notifyDataSetChanged(); // Update ListView
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
            case android.R.id.home: // Go back Home
                finish();
                return true;
            case R.id.accountMenu: // Go to AccountDetailsActivity
                Intent i = new Intent(this, AccountDetailsActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() { // Rebuild the ListView everytime the activity resumes/restarts
        super.onResume();
        mOrderList.clear();
        getAssignedOrders();
        mAssignedOrderAdapter.notifyDataSetChanged();
    }


}
