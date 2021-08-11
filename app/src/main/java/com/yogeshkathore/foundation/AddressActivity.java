package com.yogeshkathore.foundation;

import static com.yogeshkathore.foundation.PatientDetailsAbstractClass.Address;
import static com.yogeshkathore.foundation.PatientDetailsAbstractClass.District;
import static com.yogeshkathore.foundation.PatientDetailsAbstractClass.PinCode;
import static com.yogeshkathore.foundation.PatientDetailsAbstractClass.Taluka;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import java.util.Locale;


public class AddressActivity extends AppCompatActivity {

    private EditText mETPermAddress, mETDistrict, mETTaluka, mETPinCode;
    AppCompatButton mbtnNext;
    SharedPreferences sharedPref;
    String  sdistrict, saddress, staluka,spincode;
    SharedPreferences.Editor editor ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(View.SYSTEM_UI_FLAG_LAYOUT_STABLE, View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        setContentView(R.layout.address_layout);
        final Locale loc = new Locale("hin", "IND");
        sharedPref = getApplicationContext().getSharedPreferences(String.valueOf(R.string.preference_file_key), Context.MODE_PRIVATE);

        saddress = sharedPref.getString("address", Address);
        sdistrict = sharedPref.getString("district", District);
        staluka = sharedPref.getString("taluka", Taluka);
        spincode = sharedPref.getString("pincode", PinCode);

        mETPermAddress = findViewById(R.id.etPermAddress);
        mETPermAddress.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mETPermAddress, InputMethodManager.SHOW_IMPLICIT);
        mETPermAddress.setText(saddress);
        mETDistrict = findViewById(R.id.etDistrict);
        mETDistrict.setText(sdistrict);
        mETTaluka = findViewById(R.id.etTaluka);
        mETTaluka.setText(staluka);
        mETPinCode = findViewById(R.id.etPinCode);
        mETPinCode.setText(spincode);
        mbtnNext = findViewById(R.id.btnNext);
        ImageButton mCloseBtn = findViewById(R.id.closeBtn);

       // awesomeValidation.addValidation(this, R.id.etPermAddress, "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.nameerror);
        // awesomeValidation.addValidation(this, R.id.etDistrict, "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.nameerror);
        // awesomeValidation.addValidation(this, R.id.etTaluka, "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.nameerror);
        //    awesomeValidation.addValidation(this, R.id.etPinCode, "^[+]?[0-9]{10,13}$", R.string.mobileerror);

        mbtnNext.setOnClickListener(v -> {

                Address = mETPermAddress.getText().toString();
                District = mETDistrict.getText().toString();
                Taluka = mETTaluka.getText().toString();
                PinCode = mETPinCode.getText().toString();
                editor = sharedPref.edit();
                editor.putString("address", Address);
                editor.putString("district", District);
                editor.putString("taluka", Taluka);
                editor.putString("pincode",PinCode);
                editor.apply();
                Intent i = new Intent(AddressActivity.this, SangeetaCaptureActivity.class);
                startActivity(i);
                finish();
        });


        mCloseBtn.setOnClickListener(view -> {

            Intent i = new Intent(AddressActivity.this, FullScreenActivity.class);
            startActivity(i);
            finish();


        });
    }


    @Override
    public void onBackPressed() {
        // super.onBackPressed(); commented this line in order to disable back press
        //Write your code here
        Toast.makeText(getApplicationContext(), "Back press disabled!", Toast.LENGTH_SHORT).show();
    }


}