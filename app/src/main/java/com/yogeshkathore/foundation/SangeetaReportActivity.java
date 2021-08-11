package com.yogeshkathore.foundation;

import static com.yogeshkathore.foundation.PatientDetailsAbstractClass.Address;
import static com.yogeshkathore.foundation.PatientDetailsAbstractClass.DOB;
import static com.yogeshkathore.foundation.PatientDetailsAbstractClass.District;
import static com.yogeshkathore.foundation.PatientDetailsAbstractClass.FullName;
import static com.yogeshkathore.foundation.PatientDetailsAbstractClass.Gender;
import static com.yogeshkathore.foundation.PatientDetailsAbstractClass.MemberID;
import static com.yogeshkathore.foundation.PatientDetailsAbstractClass.PhotoPath;
import static com.yogeshkathore.foundation.PatientDetailsAbstractClass.PinCode;
import static com.yogeshkathore.foundation.PatientDetailsAbstractClass.Taluka;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.rey.material.app.Dialog;
import com.yogeshkathore.foundation.firebase.UserDetails;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;

import de.hdodenhof.circleimageview.CircleImageView;

public class SangeetaReportActivity extends AppCompatActivity {
    ImageView mIVQrCode;
    CircleImageView mIVPhoto;
    TextView mTVLocation, mTvMemberID, mNameTextView, mTVDob, mTVGender;
    AppCompatButton mbtnChangeDetails;
    AppCompatButton mbtnShareID;
    DatabaseReference mDatabase, gDatabase;
    String smobilenumber, sdistrict, saddress, staluka, sdob, sfname, sphotopath, spincode, sgender;
    String phtopath = null;
    File image;
    private ProgressDialog progessDialog;
    Dialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
        setContentView(R.layout.finalreport_layout);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(String.valueOf(R.string.preference_file_key), Context.MODE_PRIVATE);
        sfname = sharedPref.getString("name", FullName);
        sgender = sharedPref.getString("gender", Gender);
        smobilenumber = sharedPref.getString("mobnumber", PatientDetailsAbstractClass.Number);
        sdistrict = sharedPref.getString("district", District);
        saddress = sharedPref.getString("address", Address);
        staluka = sharedPref.getString("taluka", Taluka);
        spincode = sharedPref.getString("pincode", PinCode);
        sdob = sharedPref.getString("dob", DOB);
        sphotopath = sharedPref.getString("fbphotopath", PhotoPath);

/*        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            byte[] byteArray = extras.getByteArray("picture");
            profilephotobmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        }*/
        if (smobilenumber.length() < 10) {
            Intent changedetails = new Intent(SangeetaReportActivity.this, MobileNumberActivity.class);
            SangeetaReportActivity.this.startActivity(changedetails);
            SangeetaReportActivity.this.finish();
        }
        this.progessDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT);

        if (!progessDialog.isShowing()) {
            progessDialog.setMessage("Generating ID Card...");
            progessDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progessDialog.setIndeterminate(true);
            progessDialog.setCancelable(true);
            progessDialog.show();
        }

       /* photouri = sharedPref.getString("photouri", String.valueOf(GalleryPhoto));
        camerauri = sharedPref.getString("cameraUri",String.valueOf(CameraURI));
*/
        dialog = new Dialog(this);
        LinearLayout cardView = findViewById(R.id.cvIDCard);
        mIVQrCode = findViewById(R.id.ivqrcode);
        mIVPhoto = findViewById(R.id.ivPhoto);
        mTVLocation = findViewById(R.id.tvLocation);
        mTvMemberID = findViewById(R.id.tvMemberID);
        mbtnChangeDetails = findViewById(R.id.btnChangeDetails);
        mbtnShareID = findViewById(R.id.btnShareID);
        mNameTextView = findViewById(R.id.tvMemberName);
        mTVDob = findViewById(R.id.tvDob);
        mTVGender = findViewById(R.id.tvGender);
        MemberID = getDeviceId(SangeetaReportActivity.this);//task.getResult();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("memberid", MemberID);
        editor.apply();
        mTvMemberID.setText(MemberID.substring(0, 8).toUpperCase());
        mNameTextView.setText(sfname);
        mTVDob.setText(sdob);
        mTVGender.setText(sgender);
        mTVLocation.setText(new StringBuilder().append(staluka.toUpperCase().trim()).append(", ").append(sdistrict.trim()).append(", ").append(spincode.trim()).toString());
        mbtnShareID.setOnClickListener(v -> {
            // Share image
            loadView(cardView);
            if (image != null) {
                galleryAddPic(image.getAbsolutePath());
                assert image != null;
            } else {
                assert false;
            }
            shareImageReport(android.net.Uri.parse(image.getAbsolutePath()));
            //       shareImage(Uri.parse(image.getAbsolutePath()));
        });
        mbtnChangeDetails.setOnClickListener(v -> {
            editor.clear();
            Intent changedetails = new Intent(SangeetaReportActivity.this, RegistrationActivity.class);
            SangeetaReportActivity.this.startActivity(changedetails);
            SangeetaReportActivity.this.finish();
        });

    }

    public Bitmap loadBitmapFromView(View v) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        v.measure(View.MeasureSpec.makeMeasureSpec(dm.widthPixels, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(dm.heightPixels, View.MeasureSpec.EXACTLY));
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        int height = v.getMeasuredHeight() - 300;
        Bitmap returnedBitmap = Bitmap.createBitmap(v.getMeasuredWidth(),
                height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(returnedBitmap);
        v.draw(c);

        return returnedBitmap;
    }

    public void loadView(LinearLayout cardView) {
        try {
            cardView.setDrawingCacheEnabled(true);
            Bitmap bitmap = loadBitmapFromView(cardView);
            cardView.setDrawingCacheEnabled(false);
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                phtopath = bundle.getString("photopath");
            }
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                    .format(System.currentTimeMillis());
            File storageDir = new File(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Camera/");
            if (!storageDir.exists())
                storageDir.mkdirs();
            image = File.createTempFile(
                    "YogeshKathore",                   /* prefix */
                    ".jpeg",                     /* suffix */
                    storageDir                   /* directory */
            );
            //imageFile = new File(phtopath);
            FileOutputStream outputStream = new
                    FileOutputStream(image);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void shareImageReport(Uri imagePath) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setType("image/*");
//set your message
        shareIntent.putExtra(Intent.EXTRA_TEXT, imagePath);

        //    String imgpath = Environment.getExternalStorageDirectory() + File.separator + "shikshankranti.jpg";

        File imageFileToShare = new File(imagePath.getPath());

        Uri uri = Uri.fromFile(imageFileToShare);

        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);

        try { // should you to check Whatsapp is installed or not
            startActivity(shareIntent);
        } catch (android.content.ActivityNotFoundException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        // initializing a variable for default display.
        Display display = manager.getDefaultDisplay();
        // creating a variable for point which
        // is to be displayed in QR Code.
        Point point = new Point();
        display.getSize(point);

        // getting width and
        // height of a point
        int width = point.x;
        int height = point.y;

        // generating dimension from width and height.
        int dimen = Math.min(width, height);
        dimen = dimen * 3 / 8;

        String content = "Member ID " + MemberID.substring(0, 8) + "\n" + " Name " + sfname + "\n" + " Mobile No " + smobilenumber + "\n" + " DOB " + sdob + "\n" + " Address " + saddress + "\n" + " Taluka " + staluka + "\n" + " District " + sdistrict + "\n" + " Pin Code " + spincode + "\n";
        // setting this dimensions inside our qr code
        // encoder to generate our qr code.
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 350, 300);
            int qwidth = bitMatrix.getWidth();
            int qheight = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(qwidth, qheight, Bitmap.Config.RGB_565);
            for (int x = 0; x < qwidth; x++) {
                for (int y = 0; y < qheight; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            mIVQrCode.setImageBitmap(bmp);

        } catch (WriterException e) {
            e.printStackTrace();
        }
        // getting our qrcode in the form of bitmap.
        //   bitmap = qrgEncoder.encodeAsBitmap();
        // the bitmap is set inside our image
        // view using .setimagebitmap method.
        FirebaseDatabase pdatabase = FirebaseDatabase.getInstance();
        mDatabase = pdatabase.getReference();
        gDatabase = pdatabase.getReference();
        mDatabase.keepSynced(true);
        gDatabase.keepSynced(true);
        writeNewUser(smobilenumber, MemberID, sfname,sgender, smobilenumber, saddress, sdob, sdistrict, staluka, spincode, sphotopath);
        gDatabase.child("images").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // getting a DataSnapshot for the location at the specified
                // relative path and getting in the link variable
                String link = dataSnapshot.getValue(String.class);
                // loading that data into rImage
                // variable which is ImageView
                Glide.with(getApplicationContext()).load(sphotopath).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable @org.jetbrains.annotations.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progessDialog.dismiss();
                        assert e != null;
                        //  Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

                        new Handler().postDelayed(() -> Glide.with(getApplicationContext()).load(sphotopath).override(350, 300).dontAnimate().into(mIVPhoto), 1000);
                      /* Intent i = new Intent(SangeetaReportActivity.this, FullscreenActivity.class);
                        SangeetaReportActivity.this.startActivity(i);
                        SangeetaReportActivity.this.finish();*/

                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progessDialog.dismiss();

                        return false;
                    }
                }).override(350, 300).dontAnimate().into(mIVPhoto);

                //    Picasso.with(SangeetaReportActivity.this).load(sphotopath).into(mIVPhoto);

            }

            // this will called when any problem
            // occurs in getting data
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // we are showing that error message in toast
                Toast.makeText(SangeetaReportActivity.this, "Error Loading Image", Toast.LENGTH_SHORT).show();
            }
        });

        Glide.with(getApplicationContext()).load(sphotopath).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable @org.jetbrains.annotations.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                progessDialog.dismiss();
                assert e != null;
                //  Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(() -> Glide.with(getApplicationContext()).load(sphotopath).override(350, 300).dontAnimate().into(mIVPhoto), 1000);
                      /* Intent i = new Intent(SangeetaReportActivity.this, FullscreenActivity.class);
                        SangeetaReportActivity.this.startActivity(i);
                        SangeetaReportActivity.this.finish();*/

                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                progessDialog.dismiss();

                return false;
            }
        }).override(350, 300).dontAnimate().into(mIVPhoto);

    }


    @Override
    protected void onPause() {
        super.onPause();
        //unbindService(usbConnection);
    }

    @Override
    public void onBackPressed() {
     //   super.onBackPressed();
        //Write your code here
        dialog.setTitle("Do You Want To Exit Application?");
        dialog.cornerRadius(10);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.positiveAction("OK");
        dialog.positiveActionClickListener(v -> finish());
        dialog.negativeAction("CANCEL");
        dialog.negativeActionClickListener(v -> dialog.dismiss());
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    public void writeNewUser(String userId, String memberid, String name, String gender,String number, String address, String dob, String dist, String tal, String pincode, String photopath) {
        //   UsersDetails user = new UsersDetails(userId, memberid, name, number, Address, dob, dist, tal, pincode, photopath);
        UserDetails userDetails = new UserDetails(userId, memberid, name, gender,number, address, dob, dist, tal, pincode, photopath);
        try {
            mDatabase.child("members").child(userId).setValue(userDetails);
     //       Toast.makeText(SangeetaReportActivity.this, "Inserted", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(SangeetaReportActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    public String getDeviceId(Context context) {

        String deviceId;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        } else {
            final TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (mTelephony.getDeviceId() != null) {
                deviceId = mTelephony.getDeviceId();
            } else {
                deviceId = Settings.Secure.ANDROID_ID;
            }
        }

        return deviceId;
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void galleryAddPic(String path) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    public boolean isOnline() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {
            Toast.makeText(getApplicationContext(), "No Internet connection!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

}