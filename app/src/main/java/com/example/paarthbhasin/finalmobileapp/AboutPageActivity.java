package com.example.paarthbhasin.finalmobileapp;

import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;


/**
 * Created by paarthbhasin on 8/6/18.
 */

/**
 * DESCRIPTION: The "about" screen of the application. Displays all attributions to any third party
 * resources used, including: Images, APIs and SDKs.
 */

public class AboutPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info); // Load the UI from XML file
        // Setup a custom toolbar, with different title and a back arrow.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_top);
        setSupportActionBar(toolbar);
        TextView title = (TextView) findViewById(R.id.toolbarTitle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.bt_white_pressed), PorterDuff.Mode.SRC_ATOP);
        title.setTypeface(null, Typeface.BOLD);
        title.setText(R.string.app_info);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: // Handle back arrow press
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
