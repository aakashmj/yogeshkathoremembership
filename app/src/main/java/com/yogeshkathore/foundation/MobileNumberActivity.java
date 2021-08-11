package com.yogeshkathore.foundation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;


public class MobileNumberActivity extends AppCompatActivity {

    Button mbtnSubmit;
    EditText mETMobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_number);
       /* mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            mAuth.signOut();
            FirebaseAuth.getInstance().signOut();
            // User is signed in
        } else {
            mAuth.signOut();
            FirebaseAuth.getInstance().signOut();
            // No user is signed in
        }
        FirebaseAuth.getInstance().signOut();
*/
        mETMobile = findViewById(R.id.etMobileNumber);

        mbtnSubmit = findViewById(R.id.btnSubmit);
        mbtnSubmit.setOnClickListener(v -> {
            PatientDetailsAbstractClass.Number = mETMobile.getText().toString();


                PatientDetailsAbstractClass.Number = mETMobile.getText().toString();
                Intent pintent = new Intent(MobileNumberActivity.this, VerificationActivity.class);
                pintent.putExtra("phonenumber", PatientDetailsAbstractClass.Number);
                MobileNumberActivity.this.startActivity(pintent);
                MobileNumberActivity.this.finish();


        });


    }
}