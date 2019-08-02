package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import com.example.myapplication.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mBinding;

    private CountDownTimer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        String[] messages = getResources().getStringArray(R.array.messages);
        showMessage(messages);

        mBinding.btnRice.setOnClickListener(view -> {
            Intent myIntent = new Intent(MainActivity.this, DetectActivity.class);
            myIntent.putExtra(Constant.Key.TARGET, Constant.TargetType.RICE);
            MainActivity.this.startActivity(myIntent);

        });

        mBinding.btnMungbean.setOnClickListener(view -> {
            Intent myIntent = new Intent(MainActivity.this, DetectActivity.class);
            myIntent.putExtra(Constant.Key.TARGET, Constant.TargetType.MUNG_BEAN);
            MainActivity.this.startActivity(myIntent);

        });
    }

    int count = 0;
    private void showMessage(String[] messages) {

        mTimer = new CountDownTimer(3000, 3000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(count == (messages.length - 1)) count = 0;
                mBinding.tvAppInfo.animateText(messages[count]);
                count++;
            }

            @Override
            public void onFinish() {
                showMessage(messages);
            }
        }.start();
    }
}
