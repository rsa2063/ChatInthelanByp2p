package com.example.p2pen;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class KeySettingActivity extends AppCompatActivity {

    private Button keySetting;
    private TextView pubKey;

    private Button chatStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_setting);

        chatStart = (Button) findViewById(R.id.chat_start);
        keySetting = (Button) findViewById(R.id.key_set);
        pubKey = (TextView) findViewById(R.id.public_key);
        keySetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpRequest.initKeys();
                pubKey.setText(HttpRequest.getPublicKey());
            }
        });

        chatStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                startActivity(intent);
            }
        });
    }
}
