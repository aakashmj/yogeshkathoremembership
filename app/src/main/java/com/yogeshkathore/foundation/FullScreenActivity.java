package com.yogeshkathore.foundation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FullScreenActivity extends AppCompatActivity {
    ImageView wifiImage;
    AnimationDrawable wifiAnimation;
    private SharedPreferences settings;
    Date todayDate;
    Date expirydate;
    SimpleDateFormat dateFormatter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_full_screen);
        settings = getSharedPreferences(getString(R.string.preference_file_key), 0);
        final String status = settings.getString("opdid", "");
        dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
        try {
            todayDate = dateFormatter.parse(dateFormatter.format(new Date() ));
             expirydate = dateFormatter.parse("12-12-2022");
            if (todayDate.after(expirydate)) {
                Toast.makeText(getApplicationContext(),"App Expired",Toast.LENGTH_LONG).show();
                this.finish();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

    /*    wifiImage = findViewById(R.id.imageViewLogo);
        wifiImage.setBackgroundResource(R.drawable.animation_startup);
        wifiAnimation = (AnimationDrawable)wifiImage.getBackground();
        wifiAnimation.start();
*/

        // Using handler with postDelayed called runnable run method
        new Handler().postDelayed(() -> {
            if(!status.isEmpty()){
                Intent i = new Intent(FullScreenActivity.this, Select_language.class);
                startActivity(i);
                finish();
            }else {
                Intent i = new Intent(FullScreenActivity.this, Select_language.class);
                startActivity(i);
                // log.info("Starting Login Activity");
                finish();
            }


        }, 2 * 2000); // wait for 4 seconds


    }

}
