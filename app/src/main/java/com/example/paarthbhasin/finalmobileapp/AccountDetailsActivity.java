package com.example.paarthbhasin.finalmobileapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by paarthbhasin on 28/4/18.
 */

/**
 * DESCRIPTION: This screen displays all the account information, for the user who is currently
 * logged in to the app. It allows updating the email and password for the user. On updating,
 * the user is logged out and taken to the home screen. (HomePageActivity)
 * <p>
 * The following YouTube videos and URLs were referenced for implementing the changing of user
 * email and password:
 *      - https://www.youtube.com/watch?v=eJ0OFxR4xFw&t=680s
 *      - https://stackoverflow.com/questions/37406617/firebase-9-0-0-firebaseauthrecentloginrequiredexception-and-reauthenticateaut?rq=1
 *      - https://firebase.google.com/docs/auth/android/manage-users#re-authenticate_a_user
 **/


public class AccountDetailsActivity extends AppCompatActivity implements
        FirebaseAuth.AuthStateListener,
        View.OnClickListener {

    private FirebaseAuth mAuth; // FirebaseAuth instance
    private Button mChangePW; // Button to handle Password Change Requests
    private EditText mEmailAddress; // User Email Address
    private EditText mPW1; // Field for changing password
    private EditText mPW2; // Field for confirming password
    private Button mChangeEmail; // Button to handle Email Change Requests
    private String mPassword; // User's current password
    private ProgressBar spinner; // Appears until processing not complete
    private RelativeLayout mLayout; // layout reference for the activity
    // Reference to users in the Firebase Realtime Database
    private DatabaseReference mUserReference = FirebaseDatabase.getInstance().getReference().child("users");
    // All the different alert messages for incorrect input from user.
    private ArrayList<AlertDialog.Builder> alertBuilders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details); // Load the UI from XML file
        // Setup a custom toolbar, with different title and a back arrow.
        Toolbar toolbar = findViewById(R.id.toolbar_top);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        spinner = (ProgressBar) findViewById(R.id.progressBar3);
        spinner.setVisibility(View.GONE);
        mAuth = FirebaseAuth.getInstance(); // Setup Auth instance
        mLayout = findViewById(R.id.rl5);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.bt_white_pressed), PorterDuff.Mode.SRC_ATOP);
        TextView title = findViewById(R.id.toolbarTitle);
        title.setTypeface(null, Typeface.BOLD_ITALIC);
        title.setText(R.string.myAccount);
        final TextView mCredits = findViewById(R.id.credits);
        final TextView mFullName = findViewById(R.id.nameDetail);
        mEmailAddress = findViewById(R.id.emailDetail);
        mPW1 = findViewById(R.id.oldPW);
        mPW2 = findViewById(R.id.newPW);
        mChangeEmail = findViewById(R.id.updateEmail);
        mChangePW = findViewById(R.id.updatePW);
        mChangePW.setOnClickListener(this);
        mChangeEmail.setOnClickListener(this);
        DatabaseReference userRef = mUserReference.child(mAuth.getCurrentUser().getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue(String.class);
                String email = mAuth.getCurrentUser().getEmail();
                Log.i("EMAIL", email);
                Float credits = dataSnapshot.child("credits").getValue(Float.class);
                mPassword = dataSnapshot.child("password").getValue(String.class);
                mEmailAddress.setText(email); // Hello User Name
                mFullName.setText(name);
                mCredits.setText(getResources().getText(R.string.credits) + " $" + Float.toString(credits));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(this);
    }

    public void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(this);
    }

    @Override
    public void onClick(View view) { // Handle button clicks
        switch (view.getId()) {
            case android.R.id.home: // Go back home
                finish();
                break;
            case R.id.updatePW: // Attempt to update user password
                // Check whether password input is correct or not
                if (mPW1.getText().toString().length() == 0 || mPW2.getText().toString().length() == 0)
                    getAlertBuilders().get(2).show();
                else if (!mPW1.getText().toString().equals(mPW2.getText().toString()))
                    getAlertBuilders().get(0).show();
                else {
                    spinner.setVisibility(View.VISIBLE); // Spinner appears until processing finished.
                    updatePassword();
                }
                break;
            case R.id.updateEmail: // Attempt to update user email
                // Check whether email input is correct or not
                if (mEmailAddress.getText().toString().length() == 0)
                    getAlertBuilders().get(1).show();
                else if (mEmailAddress.getText().toString().equals(mAuth.getCurrentUser().getEmail()))
                    createSnackbar("No change. Please enter an email other than your current one.");
                else {
                    spinner.setVisibility(View.VISIBLE); // Spinner appears until processing finished.
                    updateEmail();
                }
                break;
            default:
                break;
        }
    }

    // Update the user password. To get to this activity, some time has passed. So password cannot
    // directly changed. A re-authentication is required, before password can be changed.
    private void updatePassword() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(), mPassword);

        /// Re-authenticate user with their old credentials
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // mPW2 refers to the second "confirm" password field. The text from there is taken
                    // as the user's new password
                    user.updatePassword(mPW2.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            spinner.setVisibility(View.GONE); // Spinner disappears when processing finished.
                            if (task.isSuccessful()) {
                                DatabaseReference userRef = mUserReference.child(mAuth.getCurrentUser().getUid());
                                Log.i("USER", mAuth.getCurrentUser().getUid());
                                userRef.child("password").setValue(mPW2.getText().toString());
                                Intent i = new Intent(AccountDetailsActivity.this, HomePageActivity.class);
                                i.putExtra("success", "Password has been successfully changed");
                                finish();
                                startActivity(i);
                            } else {
                                String ex = task.getException().toString();
                                createSnackbar("Password updating failed: " + ex);
                            }
                        }
                    });
                } else {
                    createSnackbar("Re-authentication failed");
                }
            }
        });
    }

    // Update the user email. To get to this activity, some time has passed. So email cannot
    // directly changed. A re-authentication is required, before email can be changed.
    private void updateEmail() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(), mPassword);

        // Re-authenticate user with their old credentials
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // mEmailAddress refers to the email field. The text from there is taken
                    // as the user's new email
                    user.updateEmail(mEmailAddress.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            spinner.setVisibility(View.GONE); // Spinner disappears when processing finished.
                            if (!task.isSuccessful()) {
                                String ex = task.getException().toString();
                                createSnackbar("Email updating failed: " + ex);
                            } else {
                                Intent i = new Intent(AccountDetailsActivity.this, HomePageActivity.class);
                                i.putExtra("success", "Email has been successfully changed");
                                finish();
                                startActivity(i);
                            }
                        }
                    });
                } else {
                    createSnackbar("Re-authentication failed");
                }
            }
        });
    }

    private void createSnackbar(String message) {
        Snackbar.make(mLayout, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: // Handle back press
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

    }

    // All the different alerts for reacting to incorrect input from user.
    private ArrayList<AlertDialog.Builder> getAlertBuilders() {
        alertBuilders.clear();
        AlertDialog.Builder mPWChange = new AlertDialog.Builder(this)
                .setTitle("Password Update Alert")
                .setMessage("The passwords you entered don't match!")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        AlertDialog.Builder mEmailChange = new AlertDialog.Builder(this)
                .setTitle("Email Update Alert")
                .setMessage("Invalid email. Please enter an email of the type: \"username@example.com\"! ")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        AlertDialog.Builder mIncomplete = new AlertDialog.Builder(this)
                .setTitle("Password Update Alert")
                .setMessage("Please fill both the password fields ")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        alertBuilders.add(mPWChange);
        alertBuilders.add(mEmailChange);
        alertBuilders.add(mIncomplete);
        return alertBuilders;
    }

}
