package com.example.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button mungbeanBtn = (Button) findViewById(R.id.bt_mungbean);
        final Button riceBtn = (Button) findViewById(R.id.bt_rice);

        riceBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(MainActivity.this, RiceActivity.class);
                MainActivity.this.startActivity(myIntent);

            }
        });
        mungbeanBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(MainActivity.this, MungbeanActivity.class);
                MainActivity.this.startActivity(myIntent);

            }
        });




    }
}
