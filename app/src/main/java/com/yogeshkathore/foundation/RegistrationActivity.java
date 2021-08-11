package com.yogeshkathore.foundation;

import static com.yogeshkathore.foundation.PatientDetailsAbstractClass.DOB;
import static com.yogeshkathore.foundation.PatientDetailsAbstractClass.FName;
import static com.yogeshkathore.foundation.PatientDetailsAbstractClass.FullName;
import static com.yogeshkathore.foundation.PatientDetailsAbstractClass.Gender;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;


public class RegistrationActivity extends AppCompatActivity {

    private EditText metName;
    Button mbtnNext;
    EditText mETDOB;
    SharedPreferences sharedPref;
    String sdob, sfname, sgender;
    SharedPreferences.Editor editor;
    ImageView mIVMale, mIVFemale;
    private boolean selectgender = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        sharedPref = getApplicationContext().getSharedPreferences(String.valueOf(R.string.preference_file_key), Context.MODE_PRIVATE);
        sfname = sharedPref.getString("name", FName);
        sgender = sharedPref.getString("gender", Gender);
        sdob = sharedPref.getString("dob", DOB);
        metName = findViewById(R.id.etName);
        metName.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(metName, InputMethodManager.SHOW_IMPLICIT);
        metName.setText(sfname);
        mIVMale = findViewById(R.id.ivMale);
        mIVFemale = findViewById(R.id.ivFemale);
        mIVMale.setOnClickListener(view -> {
            selectgender = true;
            mIVMale.setAlpha(0.4F);
            PatientDetailsAbstractClass.Gender = "MALE ";
            mIVFemale.setAlpha(1.0F);
        });

        mIVFemale.setOnClickListener(view -> {
            selectgender = true;
            mIVFemale.setAlpha(0.4F);
            mIVMale.setAlpha(1.0F);
            PatientDetailsAbstractClass.Gender = "FEMALE ";

        });


        mETDOB = findViewById(R.id.etDob);
        mETDOB.setText(sdob);
        //submit button click event registration
        new DateInputMask(mETDOB);
        mbtnNext = findViewById(R.id.btnSubmit);
        // mPreviousButton=findViewById(R.id.previousButton);
        mbtnNext.setOnClickListener(v -> {
            if (selectgender) {
                FullName = metName.getText().toString().trim();
                DOB = mETDOB.getText().toString().trim();
                editor = sharedPref.edit();
                editor.putString("name", FullName);
                editor.putString("dob", DOB);
                editor.putString("gender", Gender);
                editor.apply();
                Intent addressintent = new Intent(RegistrationActivity.this, AddressActivity.class);
                startActivity(addressintent);
                finish();

            } else {
                Toast.makeText(getApplicationContext(), "Please Select Gender", Toast.LENGTH_LONG).show();
            }

        });

    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed(); commented this line in order to disable back press
        //Write your code here
        Toast.makeText(getApplicationContext(), "Back press disabled!", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }

    public static class CustomViewGroup extends ViewGroup {
        public CustomViewGroup(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            // Intercepted touch!
            return true;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();


    }


    private String getAge(int year, int month, int day) {
        Log.i("getAge", day + "/" + month + "/" + year);
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        int ageInt = age;
        return Integer.toString(ageInt);
    }


    public static class DateInputMask implements TextWatcher {

        private String current = "";
        private final Calendar cal = Calendar.getInstance();
        private final EditText input;

        public DateInputMask(EditText input) {
            this.input = input;
            this.input.addTextChangedListener(this);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().equals(current)) {
                return;
            }

            String clean = s.toString().replaceAll("[^\\d.]|\\.", "");
            String cleanC = current.replaceAll("[^\\d.]|\\.", "");

            int cl = clean.length();
            int sel = cl;
            for (int i = 2; i <= cl && i < 6; i += 2) {
                sel++;
            }
            //Fix for pressing delete next to a forward slash
            if (clean.equals(cleanC)) sel--;

            if (clean.length() < 8) {
                String ddmmyyyy = "DDMMYYYY";
                clean = clean + ddmmyyyy.substring(clean.length());
            } else {
                //This part makes sure that when we finish entering numbers
                //the date is correct, fixing it otherwise
                int day = Integer.parseInt(clean.substring(0, 2));
                int mon = Integer.parseInt(clean.substring(2, 4));
                int year = Integer.parseInt(clean.substring(4, 8));

                mon = mon < 1 ? 1 : Math.min(mon, 12);
                cal.set(Calendar.MONTH, mon - 1);
                year = (year < 1900) ? 1900 : Math.min(year, 2100);
                cal.set(Calendar.YEAR, year);
                // ^ first set year for the line below to work correctly
                //with leap years - otherwise, date e.g. 29/02/2012
                //would be automatically corrected to 28/02/2012

                day = Math.min(day, cal.getActualMaximum(Calendar.DATE));
                clean = String.format("%02d%02d%02d", day, mon, year);
            }

            clean = String.format("%s/%s/%s", clean.substring(0, 2),
                    clean.substring(2, 4),
                    clean.substring(4, 8));

            sel = Math.max(sel, 0);
            current = clean;
            input.setText(current);
            input.setSelection(Math.min(sel, current.length()));
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

}