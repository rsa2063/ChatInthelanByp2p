package com.example.p2pen;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
        private EditText anotherKey;
        private EditText anotherIp;
        private Button anotherOk;
        private ListView msgList;
        private EditText myMsg;
        private Button mySend;

        private List<Msg> list = new ArrayList<>();
        private byte[] anotherKeyBytes;
        MsgAdapter adapter;

        private ServerSocket serverS;
        private int REC_PORT = 8091;

        Thread receiveT;

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        Msg newmsg = (Msg) msg.obj;
                        list.add(newmsg);
                        adapter.notifyDataSetChanged();
                        msgList.setSelection(list.size());
                        break;
                    case 1:
                        Msg newms = (Msg) msg.obj;
                        list.add(newms);
                        adapter.notifyDataSetChanged();
                        myMsg.setText("");
                        msgList.setSelection(list.size());
                        break;
                    case -1:
                        String error = (String) msg.obj;
                        Toast.makeText(ChatActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }
        };



        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_chat);
            Log.d("sockettest", HttpRequest.getPublicKey());

            anotherKey = (EditText) findViewById(R.id.public_key_aite);
            anotherIp = (EditText) findViewById(R.id.ip_aite);
            anotherOk = (Button) findViewById(R.id.key_ip_set);
            anotherOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    anotherKeyBytes = Base64.decode(anotherKey.getText().toString(), Base64.DEFAULT);

                    Toast.makeText(ChatActivity.this, "密钥设置成功", Toast.LENGTH_SHORT).show();
                }
            });

            list.add(new Msg("ex1", Msg.RECEIVE_SIGN));
            list.add(new Msg("ex2", Msg.SEND_SIGN));

            msgList = (ListView) findViewById(R.id.list_view_msg);
            adapter = new MsgAdapter(ChatActivity.this, R.layout.msg_item, list);
            msgList.setAdapter(adapter);

            myMsg = (EditText) findViewById(R.id.edit_text_msg);
            mySend = (Button) findViewById(R.id.button_send);
            mySend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String ip, newsM;
                    if ((ip = anotherIp.getText().toString()).equals("")) {
                        Toast.makeText(ChatActivity.this, "请输入想要通信的用户的IP", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    if ((newsM = myMsg.getText().toString()).equals("")) {
                        Toast.makeText(ChatActivity.this, "请输入信息", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new Thread(new sendMsgT(ip, newsM)).start();
                }
            });

            receiveT = new Thread(new ReceiveThread());
            receiveT.start();

        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
        }

        @Override
        protected void onResume() {
            SharedPreferences s = getSharedPreferences("keyip", MODE_PRIVATE);
            anotherKey.setText(s.getString("key", ""));
            anotherIp.setText(s.getString("ip", "192.168.159."));
            super.onResume();
        }

        @Override
        protected void onPause() {
            super.onPause();
            SharedPreferences.Editor e = getSharedPreferences("keyip", MODE_PRIVATE).edit();
            e.putString("key", anotherKey.getText().toString());
            e.putString("ip", anotherIp.getText().toString());
            e.commit();
        }

        @Override
        protected void onStop() {
            super.onStop();
            if (receiveT != null) {
                receiveT.interrupt();
            }
            try {
                serverS.close();
            } catch (Exception e) {
                e.getMessage();
            }
        }

    class ReceiveThread implements Runnable {           //监听8091端口

        @Override
        public void run() {
            Socket s = null;

            try {
                Log.d("sockettest", "1");
                serverS = new ServerSocket(REC_PORT);
            } catch (Exception e) {
                e.printStackTrace();
            }

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Log.d("sockettest", "2");
                    s = serverS.accept();
                    Log.d("sockettest", "23");
                    new Thread(new chatOne(s)).start();
                    Log.d("sockettest", "234");
                } catch (Exception e) {
                    e.getMessage();
                }
            }
        }
    }


    class chatOne implements Runnable {   //受到请求 处理请求
        Socket so = null;

        public chatOne(Socket s) {
            so = s;
        }
        @Override
        public void run() {
            try {

                BufferedInputStream in = new BufferedInputStream(so.getInputStream());
                byte num = (byte) in.read();
                byte[] data = new byte[64];
                in.read(data);
                byte[] sha1 = new byte[20];
                in.read(sha1);

                if (HttpRequest.verify(num, data, sha1)) {

                    byte[] t;
                    Log.d("sockettest", HttpRequest.bytesToHexString(data));
                    //t = HttpRequest.decryptByPublicKey(data, anotherKeyBytes);
                    t = HttpRequest.decryptByPrivateKey(data, HttpRequest.getBytesFromPriKey());
                    String temp = new String(t);

                    Msg newMsg = new Msg(temp, Msg.RECEIVE_SIGN);

                    Log.d("sockettest", temp + "\n" + new String(t) + "   " + HttpRequest.lastNum);
                    Message m = new Message();
                    m.obj = newMsg;
                    m.what = 0;
                    handler.sendMessage(m);
                }
                else {
                    Log.d("sockettest",  num + "verify   error   " + HttpRequest.lastNum);
                }
            } catch (Exception e) {
                e.printStackTrace();
                //Log.d("sockettest", e.getMessage());
            }
        }
    }


    class sendMsgT implements Runnable {           //发送信息
        String ip;
        String msg;

        public sendMsgT(String ip, String msg) {
            this.ip = ip;
            this.msg = msg;
        }
        @Override
        public void run() {
            try {
                Socket s = new Socket(ip, 8091);
                byte[] endata;

                BufferedOutputStream out = new BufferedOutputStream(s.getOutputStream(), 85);
                out.write(HttpRequest.getLastNum());
                out.write((endata = HttpRequest.encryptByPublicKey(msg.getBytes(), anotherKeyBytes)));
                    out.write(HttpRequest.sign(endata));
                Log.d("sockettest", "2   " + HttpRequest.lastNum);
                out.close();
                s.close();

                Msg newMsg = new Msg(msg, Msg.SEND_SIGN);
                Message m = new Message();
                m.obj = newMsg;
                m.what = 1;
                handler.sendMessage(m);

            } catch (Exception e) {
                e.printStackTrace();
                Message m = new Message();
                m.obj = "可能ip输入错误，请确认后重试";
                m.what = -1;
                handler.sendMessage(m);
            }
        }
    }
}
