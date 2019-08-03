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
import android.widget.TextView;
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

    TextView mTxtDetectionResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_detect);

        mTargetType = getIntent().getStringExtra(Constant.Key.TARGET);
        setTitle(mTargetType);

        mBinding.fabCamera.setOnClickListener(view -> chooseImage());


        mBinding.btnPreprocess.setOnClickListener(view -> {
            mBinding.pb.setVisibility(View.VISIBLE);
            BitmapDrawable target = (BitmapDrawable) mBinding.imgTarget.getDrawable();
            new Thread(() -> {

                switch (mTargetType) {
                    case Constant.TargetType.RICE:
                        mOutput = preProcessRice(target);
                        break;

                    case Constant.TargetType.MUNG_BEAN:
                        mOutput = preProcessMungbean(target);
                        break;
                }

                runOnUiThread(() -> {
                    mBinding.pb.setVisibility(View.INVISIBLE);
                    mBinding.imgTarget.setImageBitmap(mOutput);
                });
            }).start();
        });

        mBinding.btnDetect.setOnClickListener(view -> {
            mBinding.pb.setVisibility(View.VISIBLE);
            BitmapDrawable target = (BitmapDrawable) mBinding.imgTarget.getDrawable();
            new Thread(() -> {

                switch (mTargetType) {
                    case Constant.TargetType.RICE:
                        mOutput = detectMungbean(target); //Rice function will go here
                        break;

                    case Constant.TargetType.MUNG_BEAN:
                        mOutput = detectMungbean(target);
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


    public Bitmap preProcessRice(BitmapDrawable abmp){

        mTemporaryBitmap = abmp.getBitmap();
        mBitmapProcess = Bitmap.createScaledBitmap(mTemporaryBitmap,(int)(mTemporaryBitmap.getWidth()*0.40), (int)(mTemporaryBitmap.getHeight()*0.4), true);

        Bitmap output = Bitmap.createBitmap(mBitmapProcess.getWidth(), mBitmapProcess.getHeight(), mBitmapProcess.getConfig());

        return output;
    }


    public Bitmap preProcessMungbean (BitmapDrawable abmp){
        mTemporaryBitmap = abmp.getBitmap();
        mBitmapProcess = Bitmap.createScaledBitmap(mTemporaryBitmap,(int)(mTemporaryBitmap.getWidth()*0.40), (int)(mTemporaryBitmap.getHeight()*0.4), true);

        Bitmap output = Bitmap.createBitmap(mBitmapProcess.getWidth(), mBitmapProcess.getHeight(), mBitmapProcess.getConfig());

        for (int i = 0; i < mBitmapProcess.getWidth(); i++) {
            for (int j = 0; j < mBitmapProcess.getHeight(); j++) {
                int p = mBitmapProcess.getPixel(i, j);
                int r = Color.red(p);
                int g = Color.green(p);
                int b = Color.blue(p);

                if (g < 160 || r < 140 || b > 210)
                {
                    output.setPixel(i, j, Color.argb(Color.alpha(p), 0, 0, 255));

                }
                else
                 output.setPixel(i, j, Color.argb(Color.alpha(p), r, g, b));
            }
        }

       return output;
    }


    public Bitmap detectMungbean(BitmapDrawable abmp) {
        mTemporaryBitmap = abmp.getBitmap();
        mBitmapProcess = Bitmap.createScaledBitmap(mTemporaryBitmap,(int)(mTemporaryBitmap.getWidth()*0.40), (int)(mTemporaryBitmap.getHeight()*0.4), true);

        Bitmap output = Bitmap.createBitmap(mBitmapProcess.getWidth(), mBitmapProcess.getHeight(), mBitmapProcess.getConfig());
        int totalPix = 0;
        int avg = 0, c = 0, w = 0, yl = 0, black = 0, grey = 0, rg, gb, br;



        for (int i = 0; i < mBitmapProcess.getWidth(); i++) {

            for (int j = 0; j < mBitmapProcess.getHeight(); j++) {

                int p = mBitmapProcess.getPixel(i, j);
                int red = Color.red(p);
                int green = Color.green(p);
                int blue = Color.blue(p);
                //Manupulate pixels for preprocessing



                if (blue > 250 && red == 0 && green == 0) {
                   output.setPixel(i, j, Color.argb(Color.alpha(p), 255, 255, 255));
                } else {
                    totalPix++;
                    rg = Math.abs(red - green);
                    gb = Math.abs(green - blue);
                    br = Math.abs(blue - red);


                    if (rg <= 5 && blue < 100) {
                        black++;
                        output.setPixel(i, j, Color.argb(Color.alpha(p), 0, 0, 0));
                    } else if (red > 140 && green > 140 && blue > 140) {
                        w++;
                        output.setPixel(i, j, Color.argb(Color.alpha(p), 255, 0, 0));
                    } else if (rg > 0 && rg < 10 && red > 98 && green > 98 && blue < 190) {
                        yl++;
                        output.setPixel(i, j, Color.argb(Color.alpha(p),238, 255, 0));
                    } else if (rg > 0 && rg < 150 && blue < 150) {
                        grey++;
                        output.setPixel(i, j, Color.argb(Color.alpha(p),255, 0, 220));
                    }

                }
               //This is for any noise. any pixel remaind after these above condition. We print the pixel as black

            }
        }
        String diseaseName = detectMungbeanDiseaseName(totalPix,black,w,yl,grey);
        runOnUiThread(() -> {
            mBinding.pb.setVisibility(View.INVISIBLE);
            mBinding.tvTargetInfo.setText(diseaseName);
        });

        return output;

    }

    private String detectMungbeanDiseaseName(int totalPix,int black,int w,int yl,int grey){

        Log.d("Variables: ", String.valueOf(black)+" "+w+" "+yl);
        double pw = Math.floor(((double)w / (double)totalPix) * 100);
        double pyl = Math.floor(((double)yl / (double)totalPix) * 100);
        double pBlack = Math.floor(((double)black / (double)totalPix) * 100);
        double pGrey = Math.floor(((double)grey / (double)totalPix) * 100);
        double noise = 100 - (pw + pyl + pBlack + pGrey);

        String diseaseName = "Please Use a good quality Image";
        if (pw > 60 && pyl <= 25)
        {
            diseaseName = "Powdery Mildew";
        }
        else if (pw <= 25 && pGrey >= 60)
        {
            diseaseName = "Yellow Mosaic";
        }
        return diseaseName;

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
