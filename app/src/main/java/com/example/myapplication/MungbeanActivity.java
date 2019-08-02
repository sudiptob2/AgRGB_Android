package com.example.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

public class MungbeanActivity extends AppCompatActivity {

    Button captureBtn;
    Button selectFromGallery;
    Button processBtn;
    ImageView mgImageView;
    final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1;
    final static int SELECT_CODE = 10;
    Uri imageUri;
    Bitmap bitmap_capture_mungbean;
    Bitmap bitmap_process;
    private Bitmap operation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mungbean);

        captureBtn = (Button) findViewById(R.id.bt_capture);
        selectFromGallery = (Button) findViewById(R.id.bt_select_from_gallery);
        processBtn = (Button) findViewById(R.id.bt_process_image);
        mgImageView = (ImageView) findViewById(R.id.image_view_mungbean);

        selectFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"),SELECT_CODE);
            }
        });


        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        });
        processBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                processImage();

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode==RESULT_OK)
        {
            bitmap_capture_mungbean = (Bitmap) data.getExtras().get("data");
            mgImageView.setImageBitmap(bitmap_capture_mungbean);
        }
        //For selecting image from gallery
        try {
            if (resultCode == RESULT_OK) {
                if (requestCode == SELECT_CODE) {
                    Uri selectedImageUri = data.getData();
                    // Get the path from the Uri
                    final String path = getPathFromURI(selectedImageUri);
                    if (path != null) {
                        File f = new File(path);
                        selectedImageUri = Uri.fromFile(f);
                    }
                    // Set the image in ImageView
                    mgImageView.setImageURI(selectedImageUri);
                }
            }
        } catch (Exception e) {
            Log.e("FileSelectorActivity", "File select error", e);
        }


    }
    private void openGallery()
    {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, SELECT_CODE);
    }
    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    public void processImage()
    {
        BitmapDrawable abmp = (BitmapDrawable) mgImageView.getDrawable();
        bitmap_process = abmp.getBitmap();

        operation = Bitmap.createBitmap(bitmap_process.getWidth(),bitmap_process.getHeight(), bitmap_process.getConfig());
        double red = 0.33;
        double green = 0.59;
        double blue = 0.11;

        for (int i = 0; i < bitmap_process.getWidth(); i++) {
            for (int j = 0; j < bitmap_process.getHeight(); j++) {
                int p = bitmap_process.getPixel(i, j);
                int r = Color.red(p);
                int g = Color.green(p);
                int b = Color.blue(p);

                r = 100  +  r;
                g = 100  + g;
                b = 100  + b;

                operation.setPixel(i, j, Color.argb(Color.alpha(p), r, g, b));
            }
        }
        mgImageView.setImageBitmap(operation);
    }
}
