package com.example.paarthbhasin.finalmobileapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by paarthbhasin on 29/4/18.
 */

/**
 * DESCRIPTION:
 * Represents an Order item object (Placed Order/Assigned Delivery).
 *
 * Every order/delivery in the database, is basically an order item. Ordered items without
 * deliverers, have deliverer number and ID set to null and status set to "Unassigned".
 *
 * When an order is chosen for delivery, these fields get populated differently, making
 * the order an "Assigned Order" or a "Delivery" that is is progress.
 *
 * An order is identified by all the fields that are passed in the constructor below.
 */

public class Order implements Parcelable {

    private String mOrderID; // Unique order ID
    private String mUserID; // ID of the User who placed the order.
    private String mLocationID; // Order Location
    private String mDestID; // Order Location
    private float mPrice; // Order Amount paid
    private float mTip; // Order Tip paid
    private String mNumber; // Order owner contact number
    private String mName; // Order owner
    private String mImageUrl; // Order Image URL
    private String mItemDesc; // Order Description
    private String mStatus; // Order Status, assigned or unassigned
    private String mDelivererID; // Person who is delivering the order
    private String mDelivererNumber; // Number of the deliverer

    // Order constructor
    public Order(String name, String user, String start, String destination, float price, float tip, String number, String ID, String url, String desc, String status, String deliverer, String delivererNumber) {
        this.mName = name;
        this.mUserID = user;
        this.mLocationID = start;
        this.mDestID = destination;
        this.mPrice = price;
        this.mTip = tip;
        this.mNumber = number;
        this.mOrderID = ID;
        this.mImageUrl = url;
        this.mItemDesc = desc;
        this.mStatus = status;
        this.mDelivererID = deliverer;
        this.mDelivererNumber = delivererNumber;
    }

    protected Order(Parcel in) {
        mOrderID = in.readString();
        mUserID = in.readString();
        mLocationID = in.readString();
        mDestID = in.readString();
        mPrice = in.readFloat();
        mTip = in.readFloat();
        mNumber = in.readString();
        mName = in.readString();
        mImageUrl = in.readString();
        mItemDesc = in.readString();
        mStatus = in.readString();
        mDelivererID = in.readString();
        mDelivererNumber = in.readString();
    }

    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };

    public String getmOrderID() {
        return mOrderID;
    }

    public void setmOrderID(String orderID) {
        this.mOrderID = orderID;
    }

    public String getmUserID() {
        return mUserID;
    }

    public void setmUserID(String mUserID) {
        this.mUserID = mUserID;
    }

    public String getLocID() {
        return mLocationID;
    }

    public void setLocID(String location) {
        mLocationID = location;
    }

    public String getDestID() {
        return mDestID;
    }

    public void setDestID(String destination) {
        mDestID = destination;
    }

    public float getmTip() {
        return mTip;
    }

    public void setmTip(float tip) {
        this.mTip = tip;
    }

    public float getmPrice() {
        return mPrice;
    }

    public void setmPrice(float price) {
        this.mPrice = price;
    }

    public String getmDelivererNumber() {
        return mDelivererNumber;
    }

    public void setmDelivererNumber(String number) {
        this.mDelivererNumber = number;
    }

    public String getmNumber() {
        return mNumber;
    }

    public void setmNumber(String number) {
        this.mNumber = number;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String name) {
        this.mName = name;
    }

    public String getmImageUrl() {
        return mImageUrl;
    }

    public void setmImageUrl(String url) {
        this.mImageUrl = url;
    }

    public String getmItemDesc() {
        return mItemDesc;
    }

    public void setmItemDesc(String desc) {
        this.mItemDesc = desc;
    }

    public String getmStatus() {
        return mStatus;
    }

    public void setmStatus(String status) {
        this.mStatus = status;
    }

    public String getmDelivererID() {
        return mDelivererID;
    }

    public void setmDelivererID(String delivererID) {
        this.mDelivererID = delivererID;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mOrderID);
        parcel.writeString(mUserID);
        parcel.writeString(mLocationID);
        parcel.writeString(mDestID);
        parcel.writeFloat(mPrice);
        parcel.writeFloat(mTip);
        parcel.writeString(mNumber);
        parcel.writeString(mName);
        parcel.writeString(mImageUrl);
        parcel.writeString(mItemDesc);
        parcel.writeString(mStatus);
        parcel.writeString(mDelivererID);
        parcel.writeString(mDelivererNumber);
    }

    public String getDeliverySummary() {
        return "Items: " + getmItemDesc() + "\n\nOrdered By: " + getmName() + "\n\nContact Number: " +
                getmNumber() + "\n\nItem Location: " + getLocID() + "\n\nDelivery Address: " + getDestID()
                + "\n\nPrice: " + getmPrice() + "\n\nTip: " + getmTip();
    }


}
