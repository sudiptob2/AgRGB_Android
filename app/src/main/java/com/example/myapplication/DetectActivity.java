package com.example.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import com.example.myapplication.databinding.ActivityDetectBinding;

import java.io.File;

public class DetectActivity extends AppCompatActivity {

    private ActivityDetectBinding mBinding;

    private Uri mImageUri;
    private Bitmap mBitmapCaptureMungbean;
    private Bitmap mBitmapProcess;
    private Bitmap mTemporaryBitmap;
    private Bitmap mOutput = null;

    private String mTargetType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_detect);

        mTargetType = getIntent().getStringExtra(Constant.Key.TARGET);
        setTitle(mTargetType);

        mBinding.fabCamera.setOnClickListener(view -> chooseImage());

        mBinding.btnDetect.setOnClickListener(view -> {
            mBinding.pb.setVisibility(View.VISIBLE);
            BitmapDrawable target = (BitmapDrawable) mBinding.imgTarget.getDrawable();
            new Thread(() -> {

                switch (mTargetType) {
                    case Constant.TargetType.RICE:
                        mOutput = processMungBean(target);
                        break;

                    case Constant.TargetType.MUNG_BEAN:
                        mOutput = processMungBean(target);
                        break;
                }

                runOnUiThread(() -> {
                    mBinding.pb.setVisibility(View.INVISIBLE);
                    mBinding.imgTarget.setImageBitmap(mOutput);
                });
            }).start();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Constant.RequestCode.CAMERA && resultCode==RESULT_OK) {
            mBitmapCaptureMungbean = (Bitmap) data.getExtras().get("data");
            Bitmap resized = Bitmap.createScaledBitmap(mBitmapCaptureMungbean,(int)(mBitmapCaptureMungbean.getWidth()*0.40), (int)(mBitmapCaptureMungbean.getHeight()*0.4), true);
            mBinding.imgTarget.setImageBitmap(resized);
        }
        //For selecting image from gallery
        try {
            if (resultCode == RESULT_OK) {
                if (requestCode == Constant.RequestCode.GALLERY) {
                    Uri selectedImageUri = data.getData();
                    // Get the path from the Uri
                    final String path = getPathFromURI(selectedImageUri);
                    if (path != null) {
                        File f = new File(path);
                        selectedImageUri = Uri.fromFile(f);
                    }
                    // Set the image in ImageView
                    mBinding.imgTarget.setImageURI(selectedImageUri);
                }
            }
        } catch (Exception e) {
            Log.e("FileSelectorActivity", "File select error", e);
        }


    }
    private void openGallery()
    {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, Constant.RequestCode.GALLERY);
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

    public Bitmap processMungBean(BitmapDrawable abmp) {
        mTemporaryBitmap = abmp.getBitmap();
        mBitmapProcess = Bitmap.createScaledBitmap(mTemporaryBitmap,(int)(mTemporaryBitmap.getWidth()*0.40), (int)(mTemporaryBitmap.getHeight()*0.4), true);

        Bitmap output = Bitmap.createBitmap(mBitmapProcess.getWidth(), mBitmapProcess.getHeight(), mBitmapProcess.getConfig());
        double red = 0.33;
        double green = 0.59;
        double blue = 0.11;
        int totalPix = 0;
        int rg,gb,br;
        int black=0,w=0,yl=0,grey=0;

        for (int i = 0; i < mBitmapProcess.getWidth(); i++) {
            for (int j = 0; j < mBitmapProcess.getHeight(); j++) {
                int p = mBitmapProcess.getPixel(i, j);
                int r = Color.red(p);
                int g = Color.green(p);
                int b = Color.blue(p);
                //Manupulate pixels for preprocessing

                if (b > 250 && r == 0 && g == 0) {
                    //continue;
                   output.setPixel(i, j, Color.argb(Color.alpha(p), 255, 255, 255));
                } else {
                    totalPix++;
                    rg = (int) Math.abs(r - g);
                    gb = (int) Math.abs(g - b);
                    br = (int) Math.abs(b - r);
                    if (rg <= 5 && blue < 100) {
                        black++;
                        output.setPixel(i, j, Color.argb(Color.alpha(p), 255, 255, 255));
                    } else if (r > 140 && g > 140 && b > 140) {
                        w++;
                        output.setPixel(i, j, Color.argb(Color.alpha(p), 255, 0, 0));
                    } else if (rg > 0 && rg < 10 && r > 98 && g > 98 && b < 190) {
                        yl++;
                        output.setPixel(i, j, Color.argb(Color.alpha(p),238, 255, 0));
                    } else if (rg > 0 && rg < 150 && b < 150) {
                        grey++;
                        output.setPixel(i, j, Color.argb(Color.alpha(p),255, 255, 255));
                    }
                }
               // operation.setPixel(i, j, Color.argb(Color.alpha(p), r, g, b));
            }
        }

        return output;
    }

    private void chooseImage() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.txt_select_image)
                .setMessage(R.string.txt_choose_option)
                .setPositiveButton(R.string.label_gallery, (dialogInterface, i) -> {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constant.RequestCode.GALLERY);
                })
                .setNegativeButton(R.string.label_camera, (dialogInterface, i) -> {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, Constant.RequestCode.CAMERA);
                })
                .create()
                .show();
    }
}
