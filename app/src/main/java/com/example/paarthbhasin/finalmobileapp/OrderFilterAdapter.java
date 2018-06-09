package com.example.paarthbhasin.finalmobileapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


/**
 * Created by paarthbhasin on 12/5/18.
 */

/**
 * DESCRIPTION:
 * The adapter which populates the DeliverActivity screen's ListView, with the filtered orders that
 * match the journey the user is going to take.
 *
 * It fetches the data from the order list that is given by the DeliverActivity screen, and uses that
 * to populate the ListView of DeliverActivity
 *
 * It uses the Recycler View pattern, to make creation and scrolling of the ListView rows faster and
 * more efficient.
 */

public class OrderFilterAdapter extends BaseAdapter implements Filterable {

    private Context mCurrentContext;
    private ArrayList<Order> mOrderList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDeliveryReference;
    private DatabaseReference mOrderReference;
    private DatabaseReference mUserReference;

    public OrderFilterAdapter(Context con, ArrayList<Order> orderItems) {
        mCurrentContext = con;
        mOrderList = orderItems; // List of filtered orders
        mAuth = FirebaseAuth.getInstance();
        // Reference to all deliveries in Firebase Database
        mDeliveryReference = FirebaseDatabase.getInstance().getReference().child("deliveries");
        // Reference to all orders in Firebase Database
        mOrderReference = FirebaseDatabase.getInstance().getReference().child("orders");
        // Reference to all users in Firebase Database
        mUserReference = FirebaseDatabase.getInstance().getReference().child("users");
    }

    // ViewHolder to hold static references to Views and items
    public static class ViewHolder {
        TextView nameView;
        TextView numberView;
        TextView priceView;
        TextView locationView;
        TextView destView;
        TextView statusView;
        TextView itemDescView;
        Button deliveryButton;
        int position;
    }

    @Override
    public int getCount() {
        return mOrderList.size();
    }

    @Override
    public Order getItem(int i) {
        return mOrderList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    // Get a view at position i. This gets sent to the ListView for being populated
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        final ViewHolder vh;
        if (convertView == null) { // This will be executed for the first time, when views have not been initialised
            vh = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) mCurrentContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // Create a list item based off layout definition
            convertView = inflater.inflate(R.layout.list_order, null);
            vh.nameView = (TextView) convertView.findViewById(R.id.name);
            vh.numberView = (TextView) convertView.findViewById(R.id.mobile);
            vh.locationView = (TextView) convertView.findViewById(R.id.location);
            vh.destView = (TextView) convertView.findViewById(R.id.deliverAdd);
            vh.priceView = (TextView) convertView.findViewById(R.id.price);
            vh.statusView = (TextView) convertView.findViewById(R.id.status);
            vh.itemDescView = (TextView) convertView.findViewById(R.id.items);
            vh.deliveryButton = (Button) convertView.findViewById(R.id.selectDelivery);
            vh.position = i;

            vh.nameView.setTextColor(Color.parseColor("#000000"));
            vh.numberView.setTextColor(Color.parseColor("#000000"));
            vh.priceView.setTextColor(Color.parseColor("#000000"));
            vh.locationView.setTextColor(Color.parseColor("#000000"));
            vh.destView.setTextColor(Color.parseColor("#000000"));
            vh.statusView.setTextColor(Color.parseColor("#000000"));
            vh.itemDescView.setTextColor(Color.parseColor("#000000"));

            vh.nameView.setText("NAME: " + mOrderList.get(i).getmName());
            vh.numberView.setText("NUMBER: " + mOrderList.get(i).getmNumber());
            vh.priceView.setText("PRICE: " + Float.toString(mOrderList.get(i).getmPrice()) +
                    " TIP: " + Float.toString(mOrderList.get(i).getmTip()));
            vh.locationView.setText("ITEM LOCATION: " + mOrderList.get(i).getLocID());
            vh.destView.setText("DELIVERY ADDRESS: " + mOrderList.get(i).getDestID());
            vh.statusView.setText("STATUS: " + mOrderList.get(i).getmStatus());
            vh.itemDescView.setText("Items: " + mOrderList.get(i).getmItemDesc());
            // Set listener for button click of button within ListView. Assign the order to the user
            vh.deliveryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("CLICKED", "BUTTON CLICKED");
                    Order order = mOrderList.get(vh.position);
                    addToAssignedOrders(order);
                    updateOrderDetails(order.getmUserID(), order.getmOrderID());
                    Intent i = new Intent(mCurrentContext, AssignedOrdersActivity.class);
                    mCurrentContext.startActivity(i);
                }
            });
            convertView.setTag(vh);
        } else { // Once views have been initialised, this gets executed everytime. Saving the loading time that occurred before.
            vh = (ViewHolder) convertView.getTag();
            vh.nameView.setText("NAME: " + mOrderList.get(i).getmName());
            vh.numberView.setText("NUMBER: " + mOrderList.get(i).getmNumber());
            vh.priceView.setText("PRICE: $" + Float.toString(mOrderList.get(i).getmPrice()) +
                    " TIP: $" + Float.toString(mOrderList.get(i).getmTip()));
            vh.locationView.setText("ITEM LOCATION: " + mOrderList.get(i).getLocID());
            vh.destView.setText("DELIVERY ADDRESS: " + mOrderList.get(i).getDestID());
            vh.statusView.setText("STATUS: " + mOrderList.get(i).getmStatus());
            vh.itemDescView.setText("Items: " + mOrderList.get(i).getmItemDesc());
            vh.position = i;
            // Set listener for button click of button within ListView. Assign the order to the user
            vh.deliveryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Order order = mOrderList.get(vh.position);
                    addToAssignedOrders(order);
                    updateOrderDetails(order.getmUserID(), order.getmOrderID());
                    Intent i = new Intent(mCurrentContext, AssignedOrdersActivity.class);
                    mCurrentContext.startActivity(i);
                }
            });
        }
        return convertView;
    }

    // Assign the order to the user
    private void addToAssignedOrders(Order order) {
        String orderID = order.getmOrderID();
        final String userID = mAuth.getCurrentUser().getUid();
        final DatabaseReference childRef = mDeliveryReference.child(userID).child(orderID);
        childRef.child("name").setValue(order.getmName());
        childRef.child("number").setValue(order.getmNumber());
        childRef.child("price").setValue(order.getmPrice());
        childRef.child("tip").setValue(order.getmTip());
        childRef.child("orderID").setValue(orderID);
        childRef.child("itemLoc").setValue(order.getLocID());
        childRef.child("destLoc").setValue(order.getDestID());
        childRef.child("itemDesc").setValue(order.getmItemDesc());
        childRef.child("imageUrl").setValue(order.getmImageUrl());
        childRef.child("userID").setValue(order.getmUserID());
        childRef.child("status").setValue("In Process");
    }

    // Update the order details for the user who placed the order
    private void updateOrderDetails(String userID, String orderID) {
        final String delivererID = mAuth.getCurrentUser().getUid();
        final DatabaseReference childRef = mOrderReference.child(userID).child(orderID);
        final DatabaseReference deliveryRef = mUserReference.child(delivererID);
        deliveryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String dName = dataSnapshot.child("name").getValue().toString();
                childRef.child("status").setValue(dName);
                childRef.child("delivererID").setValue(delivererID);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public Filter getFilter() {
        return null;
    }
}
