package com.example.prizedemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.prizedemo.view.PrizeView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PrizeView prizeView = findViewById(R.id.id_prize);
        Log.d("MainActivity", "设置的文字" + prizeView.getTextContent());

    }
}
