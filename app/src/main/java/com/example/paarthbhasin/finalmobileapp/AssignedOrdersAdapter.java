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
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

/**
 * Created by paarthbhasin on 15/5/18.
 */

/**
 * DESCRIPTION: The adapter which populates the AssignedOrdersActivity screen's ListView, with the
 * assigned orders. It fetches the data from the order list that is given by the AssignedOrdersActivity
 * screen.
 *
 * It uses the Recycler View pattern, to make creation and scrolling of the ListView rows faster and
 * more efficient.
 */


public class AssignedOrdersAdapter extends BaseAdapter implements Filterable {

    private Context mCurrentContext;
    private ArrayList<Order> mOrderList;

    public AssignedOrdersAdapter(Context con, ArrayList<Order> orderItems) {
        mCurrentContext = con;
        mOrderList = orderItems;
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
        Log.i("A SIZE", Integer.toString(mOrderList.size()));
    }

    // Get a view at position i. This gets sent to the ListView for being populated
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        final AssignedOrdersAdapter.ViewHolder vh;
        if (convertView == null) { // This will be executed for the first time, when views have not been initialised
            vh = new AssignedOrdersAdapter.ViewHolder();
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
            vh.priceView.setText("PRICE: $" + Float.toString(mOrderList.get(i).getmPrice()) +
                    " TIP: $" + Float.toString(mOrderList.get(i).getmTip()));
            vh.locationView.setText("ITEM LOCATION: " + mOrderList.get(i).getLocID());
            vh.destView.setText("DELIVERY ADDRESS: " + mOrderList.get(i).getDestID());
            vh.statusView.setText("STATUS: " + mOrderList.get(i).getmStatus());
            vh.itemDescView.setText("Items: " + mOrderList.get(i).getmItemDesc());
            vh.deliveryButton.setText("VIEW DELIVERY");
            vh.deliveryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { // Set button click listener to magnify the order
                    Intent orderDetails = new Intent(mCurrentContext, MagnifiedOrderActivity.class);
                    orderDetails.putExtra("Order", mOrderList.get(vh.position));
                    Log.i("CLICKED", "BUTTON CLICKED");
                    mCurrentContext.startActivity(orderDetails);
                }
            });
            convertView.setTag(vh);
        } else { // Once views have been initialised, this gets executed everytime. Saving the loading time that occurred before.
            vh = (AssignedOrdersAdapter.ViewHolder) convertView.getTag();
            vh.nameView.setText("NAME: " + mOrderList.get(i).getmName());
            vh.numberView.setText("NUMBER: " + mOrderList.get(i).getmNumber());
            vh.priceView.setText("PRICE: $" + Float.toString(mOrderList.get(i).getmPrice()) +
                    " TIP: $" + Float.toString(mOrderList.get(i).getmTip()));;
            vh.locationView.setText("ITEM LOCATION: " + mOrderList.get(i).getLocID());
            vh.destView.setText("DELIVERY ADDRESS: " + mOrderList.get(i).getDestID());
            vh.statusView.setText("STATUS: " + mOrderList.get(i).getmStatus());
            vh.itemDescView.setText("Items: " + mOrderList.get(i).getmItemDesc());
            vh.position = i;
            vh.deliveryButton.setText("VIEW DELIVERY");
            vh.deliveryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { // Set button click listener to magnify the order
                    Intent orderDetails = new Intent(mCurrentContext, MagnifiedOrderActivity.class);
                    orderDetails.putExtra("Order", mOrderList.get(vh.position));
                    Log.i("CLICKED", "BUTTON CLICKED");
                    mCurrentContext.startActivity(orderDetails);
                }
            });
        }
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return null;
    }
}
