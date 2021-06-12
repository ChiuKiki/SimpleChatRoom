package com.example.simplechatroom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.server.ChatBean;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChatRoomActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText editText;
    private TextView numView;
    private Button button;
    private Socket socket;
    private ArrayList<ChatBean> loglist;
    private ChatAdapter chatAdapter;
    private String name, ip, port;
    private String content_rcv, name_rcv, hisPort_rcv, time_rcv;
    private int type_rcv, num_rcv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        init();
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        ip = intent.getStringExtra("ip");
        port = intent.getStringExtra("port");

        final Handler handler = new MyHandler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("ip:" + ip + " port: " + port);
                    // 创建socket，与服务器连接
                    socket = new Socket(ip, Integer.parseInt(port));
                    // 读取服务器响应的信息
                    InputStream is = socket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                    // 一加入聊天室，向服务器发消息，服务器群发上线通知(通过type为-1判断)
                    OutputStream outputStream = socket.getOutputStream();
                    SimpleDateFormat date_format = new SimpleDateFormat("HH:mm:ss");
                    ChatBean chatBean = new ChatBean("online message", name, String.valueOf(socket.getLocalPort()), date_format.format(new Date()), -1, 0);
                    outputStream.write((chatBean.toJsonString() + "\r\n").getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                    // 死循环，聊天室收发消息
                    while (true) {
                        String data_rcv = reader.readLine();
                        Message message = Message.obtain();
                        Log.i("rcv_tag", "data_rcv:" + data_rcv);
                        // 把收到的数据发到主线程中
                        message.what = 1;
                        message.obj = data_rcv;
                        handler.sendMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("err",e.getMessage());
                }
            }
        }).start();

        // 点击”发送“按钮
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String data_send = editText.getText().toString();
                if (data_send == null || data_send.isEmpty()) return; // 判空
                editText.setText("");   // 发送后清空输入框
                // 新建一个子线程向服务器发送信息
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            OutputStream outputStream = socket.getOutputStream();
                            SimpleDateFormat date_format = new SimpleDateFormat("HH:mm:ss");
                            ChatBean chatBean = new ChatBean(data_send, name, String.valueOf(socket.getLocalPort()), date_format.format(new Date()), 0, 0);
                            outputStream.write((chatBean.toJsonString() + "\r\n").getBytes(StandardCharsets.UTF_8));
                            outputStream.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    private void init() {
        recyclerView = findViewById(R.id.recycleview);
        editText = findViewById(R.id.et);
        numView = findViewById(R.id.tv_member);
        button = findViewById(R.id.send_button);
        loglist = new ArrayList<>();
        chatAdapter = new ChatAdapter(this);

    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                String localPort = String.valueOf(socket.getLocalPort());
                try {
                    JSONObject jsb = new JSONObject((String) msg.obj);
                    name_rcv = jsb.getString("name");
                    hisPort_rcv = jsb.getString("hisPort");
                    content_rcv = jsb.getString("content");
                    time_rcv = jsb.getString("time");
                    type_rcv = jsb.getInt("type");
                    num_rcv = jsb.getInt("num");

                    Log.i("num_tag", "num:" + num_rcv);
                    numView.setText("在线人数："+ num_rcv);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // 加载不同布局
                if (localPort.equals(hisPort_rcv)) {
                    // 右
                    ChatBean bean = new ChatBean(content_rcv, name_rcv, hisPort_rcv, time_rcv, 2, 0);
                    loglist.add(bean);
                } else if (hisPort_rcv != null) {
                    // 左
                    ChatBean bean = new ChatBean(content_rcv, name_rcv, hisPort_rcv, time_rcv, 1, 0);//左
                    loglist.add(bean);
                }

                // 向适配器set数据
                chatAdapter.setData(loglist);
                recyclerView.setAdapter(chatAdapter);
                LinearLayoutManager manager = new LinearLayoutManager(ChatRoomActivity.this, LinearLayoutManager.VERTICAL, false);
                recyclerView.setLayoutManager(manager);
            }
        }
    }

}
