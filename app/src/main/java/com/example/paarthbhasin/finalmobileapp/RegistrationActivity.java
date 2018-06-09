package com.example.paarthbhasin.finalmobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by paarthbhasin on 28/4/18.
 */

/**
 * DESCRIPTION: This screen is the registration screen, which allows the user to register and create
 * an account on this app. It requires the user's to enter their name, email, password and phone number.
 *
 * Once registered successfully, the user's account account info is created on Firebase Realtime Database.
 * The account login details are stored and managed by Firebase Authentication.
 */


public class RegistrationActivity extends AppCompatActivity implements
        FirebaseAuth.AuthStateListener, View.OnClickListener {

    // Reference to all users in the Firebase Database
    private DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users");
    private Button mRegisterButton; // To handle registration requests
    private Button mLogInButton; // To handle requests to sign-in, without registration
    private EditText mEmailAddress; // User email
    private EditText mPassword; // User password
    private EditText mPasswordRepeat; // Confirmation Password (needs to be same)
    private EditText mNumber; // User mobile number
    private EditText mFullName; // User Full name
    private RelativeLayout mLayout; // Activity Layout
    private FirebaseAuth mAuth; // Auth instance for handling authentication

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // Setup Activity UI
        // Customize toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_top);
        setSupportActionBar(toolbar);
        TextView title = (TextView) findViewById(R.id.toolbarTitle);
        title.setText(R.string.app_registration);
        mLayout = findViewById(R.id.rl4); // Activity Layout
        // Link views in XML to code
        mEmailAddress = (EditText) findViewById(R.id.userEmailField);
        mPassword = (EditText) findViewById(R.id.userPassword);
        mFullName = (EditText) findViewById(R.id.userNameField);
        mNumber = (EditText) findViewById(R.id.userNumberField);
        mPasswordRepeat = (EditText) findViewById(R.id.userPasswordRetype);
        mRegisterButton = (Button) findViewById(R.id.registration);
        mLogInButton = (Button) findViewById(R.id.sign_in);
        // Set button click listeners
        mRegisterButton.setOnClickListener(this);
        mLogInButton.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance(); //  get Auth instance
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
            case R.id.registration: // Try to register the user
                registerAccount();
                break;
            case R.id.sign_in: // Go to Sign in Activity (Main Activity)
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
                finish();
                break;
            default:
                break;
        }
    }

    // Attempt to register the user
    private void registerAccount() {
        String email = mEmailAddress.getText().toString(); // get email
        String password = mPassword.getText().toString(); // get passworx
        if (hasCorrectData()) { // Registration Data is complete and valid. Then try registering user
            mAuth.createUserWithEmailAndPassword(email, password).
                    addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) { // Registration unsuccessful
                                String ex = task.getException().toString();
                                createSnackbar("Registration Failed" + ex);
                            } else { // Registration successful. Sign out and to Sign-in screen (MainActivity)
                                saveDetails();
                                mAuth.signOut();
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("msg", "Registration Successful");
                                setResult(RESULT_OK, returnIntent);
                                finish();
                            }
                        }
                    });
        }
    }

    // Validate whether user has entered complete and correct registration data
    private boolean hasCorrectData() {
        boolean valid = true;
        if (mNumber.getText().toString().length() == 0 || mFullName.getText().toString().length() == 0
                || mEmailAddress.getText().toString().length() == 0) {
            createSnackbar("Please enter full name, email and mobile number");
            valid = false;
        } else if (mPassword.getText().toString().length() == 0
                || mPasswordRepeat.toString().length() == 0) {
            createSnackbar("Please fill both password fields");
            valid = false;
        } else if (!mPassword.getText().toString().equals(mPasswordRepeat.getText().toString())) {
            createSnackbar("Please enter the same password");
            valid = false;
        }
        return valid;
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

    }

    // Save the details in DB, of a user who has successfully registered
    private void saveDetails() {
        final String userID = mAuth.getCurrentUser().getUid();
        DatabaseReference childRef = mDatabaseRef.child(userID);
        childRef.child("name").setValue(mFullName.getText().toString());
        childRef.child("number").setValue(mNumber.getText().toString());
        childRef.child("credits").setValue(Integer.parseInt("0"));
        childRef.child("password").setValue(mPassword.getText().toString());
    }

    // Display Snackbar info messages
    private void createSnackbar(String message) {
        Snackbar.make(mLayout, message, Snackbar.LENGTH_LONG).show();
    }

}
