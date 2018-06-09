package com.example.paarthbhasin.finalmobileapp;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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

/**
 * Created by paarthbhasin on 15/5/18.
 */

/**
 * DESCRIPTION: The adapter which populates the PlacedOrdersActivity screen's ListView, with the
 * respective orders created by the user. It fetches the data from the order list that is given by
 * the PlacedOrdersActivity screen.
 *
 * It uses the Recycler View pattern, to make creation and scrolling of the ListView rows faster and
 * more efficient.
 */

public class PlacedOrdersAdapter extends BaseAdapter implements Filterable {

    private Context mCurrentContext;
    private ArrayList<Order> mOrderList;
    private FirebaseAuth mAuth;
    private DatabaseReference mOrderReference;
    private DatabaseReference mDeliveryReference;
    private DatabaseReference mUserReference;

    public PlacedOrdersAdapter(Context con, ArrayList<Order> orderItems) {
        mCurrentContext = con;
        mOrderList = orderItems;
        mAuth = FirebaseAuth.getInstance();
        mOrderReference = FirebaseDatabase.getInstance().getReference().child("orders");
        mDeliveryReference = FirebaseDatabase.getInstance().getReference().child("deliveries");
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
        final PlacedOrdersAdapter.ViewHolder vh;
        if (convertView == null) { // This will be executed for the first time, when views have not been initialised
            vh = new PlacedOrdersAdapter.ViewHolder();
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

            vh.nameView.setText("DELIVERER: " + mOrderList.get(i).getmStatus());
            if (mOrderList.get(i).getmDelivererNumber() == null)
                vh.numberView.setText("NUMBER: Not Applicable");
            else vh.numberView.setText("NUMBER: " + mOrderList.get(i).getmDelivererNumber());
            vh.priceView.setText("PRICE: $" + Float.toString(mOrderList.get(i).getmPrice()) +
                    " TIP: $" + Float.toString(mOrderList.get(i).getmTip()));
            vh.locationView.setText("ITEM LOCATION: " + mOrderList.get(i).getLocID());
            vh.destView.setText("DELIVERY ADDRESS: " + mOrderList.get(i).getDestID());
            vh.statusView.setText("STATUS: In Process");
            vh.itemDescView.setText("Items: " + mOrderList.get(i).getmItemDesc());
            //Log.i("M_NUMBER", mOrderList.get(i).getmDelivererNumber());
            if (!mOrderList.get(i).getmStatus().equals("Unassigned")) {
                vh.deliveryButton.setText("CONFIRM DELIVERY"); // Order has a deliverer
                vh.deliveryButton.
                        setBackgroundTintList(mCurrentContext.getResources().
                                getColorStateList(R.color.button_tint));
                Log.i("BUTTON COLOR 1", "GREEN");
            } else
                vh.deliveryButton.setText("CANCEL ORDER"); // Order has no deliverer
            setOnClickListener(vh, vh.deliveryButton.getText().toString()); // Set button on click listener based on type
            convertView.setTag(vh);
        } else { // Once views have been initialised, this gets executed everytime. Saving the loading time that occurred before.
            vh = (PlacedOrdersAdapter.ViewHolder) convertView.getTag();
            vh.nameView.setText("DELIVERER: " + mOrderList.get(i).getmStatus());
            if (mOrderList.get(i).getmDelivererNumber() == null)
                vh.numberView.setText("NUMBER: Not Applicable");
            else
                vh.numberView.setText("NUMBER: " + mOrderList.get(i).getmDelivererNumber());
            //Log.i("M_NUMBER", mOrderList.get(i).getmDelivererNumber());
            vh.priceView.setText("PRICE: $" + Float.toString(mOrderList.get(i).getmPrice()) +
                    " TIP: $" + Float.toString(mOrderList.get(i).getmTip()));
            vh.locationView.setText("ITEM LOCATION: " + mOrderList.get(i).getLocID());
            vh.destView.setText("DELIVERY ADDRESS: " + mOrderList.get(i).getDestID());
            vh.statusView.setText("STATUS: In Process");
            vh.itemDescView.setText("Items: " + mOrderList.get(i).getmItemDesc());
            vh.position = i;
            if (!mOrderList.get(i).getmStatus().equals("Unassigned")) {
                vh.deliveryButton.setText("CONFIRM DELIVERY"); // Order has a deliverer
                vh.deliveryButton.
                        setBackgroundTintList(mCurrentContext.getResources()
                                .getColorStateList(R.color.button_tint));
                Log.i("BUTTON COLOR 2", "GREEN");
            } else {
                vh.deliveryButton.setText("CANCEL ORDER"); // Order has no deliverer
            }
            setOnClickListener(vh, vh.deliveryButton.getText().toString()); // Set button on click listener based on type

        }
        return convertView;
    }

    // Set on click listeners for the list view buttons. Displays confirmation alert messages on clicking
    // Its the same button but at different times:
    // - One is when you want to cancel (no deliverer/assignee yet)
    // - Other is when you have a deliverer, and want to confirm the delivery
    private void setOnClickListener(final ViewHolder vh, String type) {
        if (type.equals("CANCEL ORDER")) {
            vh.deliveryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertBuilders(vh.position).get(1).show();
                    notifyDataSetChanged();
                }
            });
        } else {
            vh.deliveryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertBuilders(vh.position).get(0).show();
                    notifyDataSetChanged();
                }
            });
        }
        notifyDataSetChanged(); // Update the ListView
    }

    // Alerts for confirming critical order decisions: Confirm Delivery and Cancel Order
    private ArrayList<AlertDialog.Builder> alertBuilders(final int pos) {
        ArrayList<AlertDialog.Builder> alerts = new ArrayList<>();
        AlertDialog.Builder mOrderConfirm = new AlertDialog.Builder(mCurrentContext)
                .setTitle("Order Confirmation")
                .setMessage("Do you confirm that " +
                        mOrderList.get(pos).getmStatus() + " has delivered your order?")
                .setIcon(android.R.drawable.ic_secure)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Order o = mOrderList.get(pos);
                        completeOrder(o);
                        mOrderList.remove(pos);
                        notifyDataSetChanged();
                    }
                })
                .setNegativeButton(R.string.no, null);

        AlertDialog.Builder mCancelConfirm = new AlertDialog.Builder(mCurrentContext)
                .setTitle("Order Cancellation")
                .setMessage("Are you sure you want to cancel this order?")
                .setIcon(android.R.drawable.ic_menu_delete)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Order o = mOrderList.get(pos);
                        removeOrder(o);
                        mOrderList.remove(pos);
                        notifyDataSetChanged();
                    }
                })
                .setNegativeButton(R.string.no, null);
        alerts.add(mOrderConfirm);
        alerts.add(mCancelConfirm);
        return alerts;
    }

    //When you confirm the delivery, the deliverer's credit amount increases. The order is removed from
    // Firebase Database and the delivery details for it are updated in the Database
    private void completeOrder(final Order order) {
        final DatabaseReference childRef = mUserReference.child(order.getmDelivererID()).child("credits");
        childRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                float value = dataSnapshot.getValue(Float.class);
                value += order.getmPrice() + order.getmTip();
                dataSnapshot.getRef().setValue(value);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        removeOrder(order);
        updateDeliveryStatus(order);
    }

    // When you confirm to remove the order, it is removed from Firebase Database
    private void removeOrder(Order order) {
        String userID = mAuth.getCurrentUser().getUid();
        String orderID = order.getmOrderID();
        DatabaseReference childRef = mOrderReference.child(userID);
        Log.i("ID", orderID);
        childRef.child(orderID).removeValue();
    }

    // Update the delivery status for the deliverer to "Completed" when order is considered complete by the user
    private void updateDeliveryStatus(Order order) {
        String delivererID = order.getmDelivererID();
        String orderID = order.getmOrderID();
        DatabaseReference childRef = mDeliveryReference.child(delivererID).child(orderID).child("status");
        childRef.setValue("Completed");
    }

    @Override
    public Filter getFilter() {
        return null;
    }
}

