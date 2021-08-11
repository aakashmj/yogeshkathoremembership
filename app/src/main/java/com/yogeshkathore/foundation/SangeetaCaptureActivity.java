package com.yogeshkathore.foundation;

import static com.yogeshkathore.foundation.PatientDetailsAbstractClass.PhotoPath;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rey.material.app.Dialog;
import com.yogeshkathore.foundation.firebase.NetworkChangeCallback;
import com.yogeshkathore.foundation.firebase.NetworkChangeReceiver;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class SangeetaCaptureActivity extends AppCompatActivity implements NetworkChangeCallback {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int SELECT_PICTURE = 2;
    private final List<Integer> blockedKeys = new ArrayList<>(Arrays.asList(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP));
    ImageView mcapturePic, mivPhoto, mGallery;
    AppCompatButton mbtnNext;
    UploadTask uploadTask;
    StorageReference imagesRef;
    StorageReference riversRef;
    StorageReference storageRef;
    FirebaseStorage storage;
    String currentPhotoPath;
    private Uri fileUri;
    private ProgressDialog progessDialog;
    private BroadcastReceiver mNetworkReceiver;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    Dialog dialog;

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
        setContentView(R.layout.capture_layout);
        final Locale loc = new Locale("hin", "IND");
        mNetworkReceiver = new NetworkChangeReceiver(this);
        // registerNetworkBroadcastForNougat();
        sharedPref = getApplicationContext().getSharedPreferences(String.valueOf(R.string.preference_file_key), MODE_PRIVATE);
        editor = sharedPref.edit();
        dialog = new Dialog(this);
        ImageButton mCloseBtn = findViewById(R.id.closeBtn);
        mcapturePic = findViewById(R.id.btnCapture);
        mbtnNext = findViewById(R.id.btnNext);
        mbtnNext.setEnabled(true);
        mGallery = findViewById(R.id.ivGallery);

        if (isOnline()) {
            mGallery.setEnabled(true);
            mcapturePic.setEnabled(true);
        } else {
            mGallery.setEnabled(false);
            mcapturePic.setEnabled(false);
            dialog.setTitle("No Internet..Please Enable Internet");
            dialog.cornerRadius(10);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.positiveAction("SKIP");
            dialog.positiveActionClickListener(v -> {
                Intent reportintent = new Intent(SangeetaCaptureActivity.this, SangeetaReportActivity.class);
                // reportintent.putExtra("picture", rotatedbmp);
                startActivity(reportintent);
                finish();
            });
            dialog.negativeAction("CANCEL");
            dialog.negativeActionClickListener(v -> dialog.dismiss());
            if (!dialog.isShowing()) {
                dialog.show();
            }


        }
        LinearLayout mLinearBrose = findViewById(R.id.llbrowse);
        mivPhoto = findViewById(R.id.ivPhoto);
        String sphotopath = sharedPref.getString("fbphotopath", PhotoPath);

        // Get a non-default Storage bucket
        Glide.with(getApplicationContext()).load(sphotopath).placeholder(R.drawable.ic_avatar).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable @org.jetbrains.annotations.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                assert e != null;
                Glide.with(getApplicationContext()).load(sphotopath);
                //  Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                return false;
            }
        }).into(mivPhoto);

        storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        storageRef = storage.getReference();
        this.progessDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT);

        // Create a child reference
// imagesRef now points to "images"
        imagesRef = storageRef.child("images");
        mcapturePic.setOnClickListener(view -> dispatchTakePictureIntent());
        mGallery.setOnClickListener(view -> fetchImageFromGallery());
        mbtnNext.setOnClickListener(v -> {
            Intent reportintent = new Intent(SangeetaCaptureActivity.this, SangeetaReportActivity.class);
            reportintent.putExtra("photopath", currentPhotoPath);
            //   reportintent.putExtra("picture", byteArray);
            //  reportintent.putExtra("picture", rotatedbmp);
            startActivity(reportintent);
            finish();
        });
        mCloseBtn.setOnClickListener(view -> {
            Intent i = new Intent(SangeetaCaptureActivity.this, FullScreenActivity.class);
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

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return blockedKeys.contains(event.getKeyCode()) || super.dispatchKeyEvent(event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }

    Uri selectedImageURI;
    Bitmap bitmap;
    byte[] byteArray;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setPic();
            galleryAddPic();
            /*Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageBtn.setImageBitmap(imageBitmap);*/
            PatientDetailsAbstractClass.Gallery = false;
        }
        if (requestCode == SELECT_PICTURE) {
            if (data != null) {
                selectedImageURI = data.getData();
            }

            if (selectedImageURI != null) {
            /*SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(String.valueOf(R.string.preference_file_key),MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("photouri", String.valueOf(selected
            ImageURI));
            editor.apply();*/
                PatientDetailsAbstractClass.Gallery = true;
                PatientDetailsAbstractClass.GalleryPhoto = selectedImageURI;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    try {
                        bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getApplicationContext().getContentResolver(), selectedImageURI));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                    byteArray = stream.toByteArray();
                    bitmap.recycle();
                } else {
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageURI);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                    byteArray = stream.toByteArray();
                    bitmap.recycle();

                }
                riversRef = imagesRef.child(selectedImageURI.getLastPathSegment());
                uploadTask = riversRef.putBytes(byteArray);

// Register observers to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener(exception -> {
                    // Handle unsuccessful uploads
                    Toast.makeText(SangeetaCaptureActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                }).addOnSuccessListener(taskSnapshot -> {
                    riversRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Log.e("Tuts+", "uri: " + uri.toString());
                        PhotoPath = uri.toString();
                        editor.putString("fbphotopath", uri.toString());
                        editor.apply();

                        mbtnNext.setEnabled(true);
                        progessDialog.dismiss();
                        //Handle whatever you're going to do with the URL here
                    });
//                Toast.makeText(getApplicationContext(),taskSnapshot.getMetadata().toString(),Toast.LENGTH_LONG).show();
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.

                }).addOnProgressListener(snapshot -> {
                    double progress
                            = (100.0
                            * snapshot.getBytesTransferred()
                            / snapshot.getTotalByteCount());
                    Log.i("Progress", String.valueOf(progress));
                    if (progessDialog != null && !progessDialog.isShowing()) {
                        progessDialog.setMessage("Please Wait Image is Uploading");
                        progessDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progessDialog.setIndeterminate(true);
                        progessDialog.setCancelable(false);
                        progessDialog.show();
                    }
                    mbtnNext.setEnabled(true);

                    //    Toast.makeText(getApplicationContext(),"Please Wait..",Toast.LENGTH_SHORT).show();
                });
                mivPhoto.setImageURI(selectedImageURI);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNetworkReceiver != null)
            unregisterReceiver(mNetworkReceiver);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save file url in bundle as it will be null on scren orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile;
            try {
                photoFile = createImageFile();

                // Continue only if the File was successfully created
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.yogeshkathore.foundation.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    Bitmap rotatedbmp;

    private void setPic() {
        // Get the dimensions of the View
        int targetW = mivPhoto.getWidth();
        int targetH = mivPhoto.getHeight();
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        // Determine how much to scale down the image
        int scaleFactor = Math.max(1, Math.min(photoW / targetW, photoH / targetH));

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
        rotatedbmp = rotateBitmapOrientation(currentPhotoPath);
        if (rotatedbmp != null) {
            PatientDetailsAbstractClass.Photo = rotatedbmp;
        }

// Points to "images/space.jpg"
// Note that you can use variables to create child values
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PatientDetailsAbstractClass.Photo.compress(Bitmap.CompressFormat.JPEG, 60, baos);
        byte[] data = baos.toByteArray();
        //   CameraURI = getImageUri(this,rotatedbmp);
        riversRef = imagesRef.child(imagesRef.getPath());
        uploadTask = riversRef.putBytes(data);
        uploadTask.addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
        }).addOnSuccessListener(taskSnapshot -> {
            riversRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Log.e("Tuts+", "uri: " + uri.toString());
                PhotoPath = uri.toString();
                editor.putString("fbphotopath", uri.toString());
                editor.apply();
                mbtnNext.setEnabled(true);
                progessDialog.dismiss();
                //Handle whatever you're going to do with the URL here
            });
            //  Toast.makeText(getApplicationContext(),taskSnapshot.getMetadata().toString(),Toast.LENGTH_LONG).show();

        }).addOnProgressListener(snapshot -> {
            double progress
                    = (100.0
                    * snapshot.getBytesTransferred()
                    / snapshot.getTotalByteCount());
            Log.i("Progress", String.valueOf(progress));
            if (progessDialog != null && !progessDialog.isShowing()) {
                progessDialog.setMessage("Please Wait Image is Uploading");
                progessDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progessDialog.setIndeterminate(true);
                progessDialog.setCancelable(false);
                progessDialog.show();
            }
            mbtnNext.setEnabled(false);
            //  Toast.makeText(getApplicationContext(),"Please Wait..",Toast.LENGTH_SHORT).show();
        });

       /* SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(String.valueOf(R.string.preference_file_key),MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("camerauri", String.valueOf(selectedImageURI));
        editor.apply();*/
        //  Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        mivPhoto.setImageBitmap(rotatedbmp);
        PatientDetailsAbstractClass.Photo = rotatedbmp;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkReceiver, intentFilter);
    }

    public Bitmap rotateBitmapOrientation(String photoFilePath) {
        // Create and configure BitmapFactory
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFilePath, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(photoFilePath, opts);
        // Read EXIF Data
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert exif != null;
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        // Rotate Bitmap
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        // Return result
        return Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
    }

    void fetchImageFromGallery() {

        Intent intent = new Intent();
        // Create the File where the photo should go
        try {
            createImageFile();

            // Continue only if the File was successfully created
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);

        } catch (IOException ex) {
            // Error occurred while creating the File
            ex.printStackTrace();
        }

    }

    @Override
    public void onNetworkChanged(boolean status) {
        if (status) {
            mGallery.setEnabled(true);
            mcapturePic.setEnabled(true);
            mGallery.setAlpha(1f);
            mcapturePic.setAlpha(1f);
            if (dialog.isShowing()) {
                dialog.dismiss();
                Toast.makeText(this, "Internet Available...Update Photo", Toast.LENGTH_SHORT).show();
            }

        } else {
            mGallery.setEnabled(false);
            mcapturePic.setEnabled(false);
            mGallery.setAlpha(0.5f);
            mcapturePic.setAlpha(0.5f);

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
    protected void onDestroy() {
        super.onDestroy();
        if (mNetworkReceiver != null) {
            unregisterNetworkChanges();
        }
    }

    private void registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    protected void unregisterNetworkChanges() {
        try {
            unregisterReceiver(mNetworkReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public boolean isOnline() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {
            Toast.makeText(getApplicationContext(), "No Internet !", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


    /*private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }*/
}