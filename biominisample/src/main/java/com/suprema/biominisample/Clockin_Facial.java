package com.suprema.biominisample;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Clockin_Facial extends AppCompatActivity {

    String cardNumber_,adminId_,cardOwner_,clockinType_,organizationId;
    Context mContext;

    private Bitmap bitmap;
    private int PICK_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 8675309;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clockin_facial);



        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startCam();
            }
        },2000);
    }








    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startCam(){

//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        try {
//            startActivityForResult(takePictureIntent, PICK_IMAGE_REQUEST);
//        } catch (ActivityNotFoundException e) {
//            // display error state to the user
//        }


        int camera = ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.CAMERA);
        if (camera != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mContext,
                    new String[]{android.Manifest.permission.CAMERA},9);
        }
        else{
            Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, CAMERA_PERMISSION_REQUEST_CODE);
        }

        ActivityCompat.requestPermissions((Activity) mContext,
                new String[]{android.Manifest.permission.CAMERA},
                9);
//        String[] permissionRequest = {Manifest.permission.CAMERA};
//        requestPermissions(permissionRequest, CAMERA_PERMISSION_REQUEST_CODE);

    }





    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showFileChooser();
            } else {
                Toast.makeText(Clockin_Facial.this, "cannot take photo without permission", Toast.LENGTH_LONG).show();
            }
        }
    }






    private void showFileChooser() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, 1);


    }







    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                //Getting the Bitmap from Gallery
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                //Setting the Bitmap to ImageView
                Intent myIntent = new Intent(Clockin_Facial.this, Clockin_Card.class);
                myIntent.putExtra("_CardNumber", "718222");
                myIntent.putExtra("_FullName", "718222");
                myIntent.putExtra("_ClockinType", clockinType_);
                startActivity(myIntent);
                finish();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }



}