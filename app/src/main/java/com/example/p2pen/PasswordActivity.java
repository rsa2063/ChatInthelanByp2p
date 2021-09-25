package com.example.p2pen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class PasswordActivity extends Activity implements OnClickListener {

    LinearLayout set_password=null;
    LinearLayout ver_password=null;

    private EditText setPassword;
    private Button register;

    private EditText verifyPassword;
    private Button logIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        set_password=(LinearLayout)findViewById(R.id.set_password);
        ver_password = (LinearLayout)findViewById(R.id.verify_password);

        setPassword=(EditText) findViewById(R.id.setpassword);
        register=(Button) findViewById(R.id.register);
        verifyPassword=(EditText) findViewById(R.id.verifypassword);
        logIn=(Button) findViewById(R.id.login);

        register.setOnClickListener(this);
        logIn.setOnClickListener(this);


        FileInputStream in = null;
        try {
            in = openFileInput("password");
            set_password.setVisibility(View.GONE);
            ver_password.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            set_password.setVisibility(View.VISIBLE);
            ver_password.setVisibility(View.GONE);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {

                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.register:
                String password=setPassword.getText().toString().trim();
                save(password);
                Toast.makeText(this, "密码设置成功！", Toast.LENGTH_SHORT).show();
                set_password.setVisibility(View.GONE);
                ver_password.setVisibility(View.VISIBLE);
                break;
            case R.id.login:
                login();
                break;
        }
    }

    private void login() {
        String verifypassword=verifyPassword.getText().toString().trim();
        if(check(verifypassword)){
            Toast.makeText(this, "ok", Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(this, KeySettingActivity.class);
            startActivity(intent);
        }else{
            Toast.makeText(this, "密码错误！", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean check(String s) {
        FileInputStream in = null;
        byte[] t = new byte[20];
        try {
            in = openFileInput("password");
            in.read(t);
        } catch (Exception e) {
            return false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {

                }
            }
        }
        Log.d("sockettest", "12   " + HttpRequest.bytesToHexString(t) + " || " + HttpRequest.bytesToHexString(HttpRequest.sign(s.getBytes())));
        return Arrays.equals(t, HttpRequest.sign(s.getBytes()));
    }

    public void save(String inputText) {
        FileOutputStream out = null;
        try {
            out = openFileOutput("password", Context.MODE_PRIVATE);
            out.write(HttpRequest.sign(inputText.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {

                }
            }
        }
    }


}
