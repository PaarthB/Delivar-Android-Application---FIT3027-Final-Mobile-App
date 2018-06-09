package com.example.paarthbhasin.finalmobileapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by paarthbhasin on 27/4/18.
 */

/**
 * DESCRIPTION:
 * This screen is the login screen for the app. To login, the user needs to provide correct email
 * and password, that's recognized by Firebase Authentication.
 *
 * If the details are valid, the user is logged in and taken to the HomePageActivity.
 * Otherwise, an appropriate SnackBar message is displayed to the user, stating what went wrong.
 */

public class MainActivity extends AppCompatActivity implements
        FirebaseAuth.AuthStateListener,
        View.OnClickListener {

    private static int REQUEST_CODE = 1;

    private EditText mEmailAddress; // User email
    private EditText mPassword; // User password
    private RelativeLayout mLayout; // Activity layout
    private FirebaseAuth mAuth; // Firebase Auth instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set Activity UI
        mLayout = findViewById(R.id.rl1); // Activity layout
        // Customize toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_top);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView title = (TextView) findViewById(R.id.toolbarTitle);
        title.setText(R.string.app_name);
        // Link XML elements to code
        mEmailAddress = (EditText) findViewById(R.id.userEmail);
        mPassword = (EditText) findViewById(R.id.userPassword);
        Button mRegisterButton = (Button) findViewById(R.id.registerButton);
        Button mLogInButton = (Button) findViewById(R.id.signInButton);
        Log.i("BT", Integer.toString(mRegisterButton.getId()));
        mRegisterButton.setOnClickListener(this);
        mLogInButton.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
    }

    public void onStart() { // Set user authentication state listener when activity starts
        super.onStart();
        mAuth.addAuthStateListener(this);
    }

    public void onStop() { // Remove user authentication state listener, when activity stops
        super.onStop();
        mAuth.removeAuthStateListener(this);
    }

    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View view) { // Handle button clicks
        Log.i("CLICK", view.toString());
        Log.i("ID", Integer.toString(view.getId()));
        switch (view.getId()) {
            case R.id.registerButton: // Go to Registration Screen
                Intent i = new Intent(this, RegistrationActivity.class);
                startActivityForResult(i, REQUEST_CODE);
                break;
            case R.id.signInButton: // Attempt to login the user
                logInAccount();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.info) { // Go to Application Information Screen
            Intent i = new Intent(this, AboutPageActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void logInAccount() { // Attempt to log the user in
        String email = mEmailAddress.getText().toString();
        String password = mPassword.getText().toString();
        if (email.length() > 0 && password.length() > 0) { // check if inputs are correct
            mAuth.signInWithEmailAndPassword(email, password).addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    createSnackbar(e.getMessage());
                }
            });
        } else {
            createSnackbar("Please enter an email address and password");
        }
    }

    @Override // When auth state changes (user becomes logged in), take the user to HomePageActivity
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (firebaseAuth.getCurrentUser() != null) {
            Intent i = new Intent(this, HomePageActivity.class);
            startActivity(i);
            finish();
        }
    }

    // Display SnackBar info messages
    private void createSnackbar(String message) {
        Snackbar.make(mLayout, message, Snackbar.LENGTH_LONG).show();
    }

    // Executed on receiving successful response from RegistrationActivity (user successfully signed up)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String msg = data.getStringExtra("msg");
                createSnackbar(msg);
            }
        }
    }
}

