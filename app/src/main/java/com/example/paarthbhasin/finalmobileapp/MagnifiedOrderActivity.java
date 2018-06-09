package com.example.paarthbhasin.finalmobileapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by paarthbhasin on 15/5/18.
 */

/**
 * DESCRIPTION: This screen displays the details of the order assigned to a user in a neatly
 * presented format. It contains all the necessary information related to the order, that the
 * deliverer should be aware of. This also includes the image of the assigned order.
 *
 * A deliverer can cancel the delivery if they don't wish to deliver it anymore. Or they can
 * remove a "completed" delivery from stop being displayed, and be removed from the database entirely.
 *
 * The implementation for using Glide for handling images and FirebaseStorage for images,
 * was referred from the following links:
 *      - https://stackoverflow.com/questions/44949432/how-to-integrate-firebase-with-glide-using-method
 *      - https://stackoverflow.com/questions/48762263/using-firebase-storage-image-with-glide
 *      - https://github.com/bumptech/glide
 *      - https://stackoverflow.com/questions/38170940/unable-to-show-image-in-imageview-from-firebase-storage
 *      - https://www.youtube.com/watch?v=7bFiVTpNrl4
 *
 * The following URLs and tutorials were referred for implementing a CollapsingToolbar with Image
 * and customizing it as wanted:
 *      - https://www.journaldev.com/13927/android-collapsingtoolbarlayout-example
 *      - http://www.zoftino.com/collapsing-toolbar-layout-example
 *      - https://stackoverflow.com/questions/34176722/android-how-to-add-a-button-with-text-inside-collapsing-toolbar
 *      - https://stackoverflow.com/questions/30820980/android-overflow-menu-and-back-button-not-showing-in-collapsing-toolbar/41384365
 *      - https://stackoverflow.com/questions/42945007/back-button-not-shown-on-collapsed-state-in-collapsingtoolbarlayout
 *      - https://stackoverflow.com/questions/38700748/how-do-you-change-the-color-of-collapsing-toolbar-when-its-collapsed
 *      - https://stackoverflow.com/questions/3144940/set-imageview-width-and-height-programmatically?rq=1
 */


public class MagnifiedOrderActivity extends AppCompatActivity implements View.OnClickListener {
    private Button cancelDelivery; // To handle delivery cancellation
    private Order displayOrder; // The order that is being displayed
    private FirebaseStorage storage; // Reference to firebase storage to retrieve order image
    private TextView orderInfo; // Succinct Order information
    // Reference to all the deliveriers in the database
    private DatabaseReference mDeliveryReference = FirebaseDatabase.getInstance().getReference().child("deliveries");
    // Reference to all the orders in the database
    private DatabaseReference mOrderReference = FirebaseDatabase.getInstance().getReference().child("orders");
    private AlertDialog.Builder mCancelConfirm; // Alert to confirm delivery cancellation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_magnified); // Set Activity UI
        storage = FirebaseStorage.getInstance(); // Get storage reference
        mCancelConfirm = new AlertDialog.Builder(this) // Set alert  details
                .setTitle("Delivery Cancellation")
                .setMessage("Are you sure you want to cancel this delivery?")
                .setIcon(android.R.drawable.ic_menu_delete)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        updateOrderDetails();
                        removeDelivery();
                    }
                })
                .setNegativeButton(android.R.string.no, null);
        // Customize toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.z_toolbar);
        setSupportActionBar(toolbar);
        orderInfo = (TextView) findViewById(R.id.orderInfo);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.bt_white_pressed), PorterDuff.Mode.SRC_ATOP);
        Intent i = getIntent();
        displayOrder = (Order) i.getParcelableExtra("Order"); // The order received from the AssignedOrders Activity.
        CollapsingToolbarLayout ctl = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbar);
        ctl.setTitle("Order Details");
        // Set button click listeners
        cancelDelivery = (Button) findViewById(R.id.cancelDelivery);
        cancelDelivery.setOnClickListener(this);
        loadToolbarBackground(); // Add image to CollapsingToolbar
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: // Go back home
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Set the Image within CollapsingToolbar
    private void loadToolbarBackground() {
        final ImageView orderImage = findViewById(R.id.order_image);
        orderImage.setScaleType(ImageView.ScaleType.FIT_END);
        Log.i("FILENAME", "ABD " + displayOrder.getmImageUrl());
        StorageReference httpsReference = storage.getReferenceFromUrl(displayOrder.getmImageUrl());
        Glide.with(this).using(new FirebaseImageLoader()).load(httpsReference).into(orderImage);
        orderInfo.setText(displayOrder.getDeliverySummary());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onClick(View view) { // handle button clicks
        switch (view.getId()) {
            case R.id.cancelDelivery:
                mCancelConfirm.show(); // Confirm whether user is sure to remove delivery
                break;
            default:
                break;
        }
    }

    // Remove delivery from Firebase Database based on order ID of the deliverer (delivererID)
    private void removeDelivery() {
        String delivererID = displayOrder.getmDelivererID();
        String orderID = displayOrder.getmOrderID();
        DatabaseReference childRef = mDeliveryReference.child(delivererID).child(orderID);
        childRef.removeValue();
        finish();
    }

    // Update the order details if the user chose to remove delivery.
    private void updateOrderDetails() {
        final DatabaseReference childRef = mOrderReference.child(displayOrder.getmUserID()).child(displayOrder.getmOrderID());
        childRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                childRef.child("delivererID").setValue(null);
                childRef.child("status").setValue("Unassigned");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}


