package com.example.ani.uberclone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class MainActivity extends AppCompatActivity {

    Switch aswitch;
    Button startButton;

    public void moveToRider() {
        Intent intent = new Intent(getApplicationContext(), RiderActivity.class);
        startActivity(intent);
    }

    public void moveToDriver() {
        Intent intent = new Intent(getApplicationContext(), ViewRequestsActivity.class);
        startActivity(intent);
    }

    public void redirectActivity() {
        if(ParseUser.getCurrentUser().get("riderOrDriver").equals("rider"))
            moveToRider();
        else if(ParseUser.getCurrentUser().get("riderOrDriver").equals("driver"))
            moveToDriver();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        aswitch = (Switch) findViewById(R.id.switch1);
        startButton = (Button) findViewById(R.id.startButton);

        if(ParseUser.getCurrentUser() == null) {
            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (e == null)
                        Log.i("LogIn", "successful");
                    else
                        Log.i("LogIn", e.getMessage());
                }
            });
        }
        else if(ParseUser.getCurrentUser().get("riderOrDriver") != null)
                redirectActivity();
    }

    public void start(View view) {
        final String choice = aswitch.isChecked() ? "driver" : "rider";
        ParseUser.getCurrentUser().put("riderOrDriver", choice);
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null)
                    redirectActivity();
                else
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
