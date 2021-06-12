package com.example.simplechatroom;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private Button mBtnLogin, mBtnRegister;
    private EditText mUser, mPassword, mIP, mPort;
    private String name, password, ip, port;
    // 登录接口
    public static final String login_url="http://chat.chiukiki.cn/api/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        //mUser.setText("kiki");
        //mPassword.setText("111111");
        mIP.setText("192.168.140.65");  // 服务器ip
        mPort.setText("50000");         // 服务器端口
        // 点击登录按钮
        mBtnLogin = (Button) findViewById(R.id.btn_login);
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String message = login();
                            Looper.prepare();
                            if (message.equals("登录成功")) {
                                //跳转页面
                                Intent intent = new Intent(MainActivity.this, ChatRoomActivity.class);
                                intent.putExtra("name", name);
                                intent.putExtra("ip", ip);
                                intent.putExtra("port", port);
                                startActivity(intent);
                            }else{
                                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                            Looper.loop();

                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }).start();
            }
        });

        // 点击注册按钮
        mBtnRegister = (Button) findViewById(R.id.btn_to_register);
        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }


    private void initView() {
        mBtnLogin = findViewById(R.id.btn_login);
        mBtnRegister = (Button) findViewById(R.id.btn_to_register);
        mUser = findViewById(R.id.et_user);
        mPassword = findViewById(R.id.et_password);
        mIP = findViewById(R.id.ip_edit_text);
        mPort = findViewById(R.id.port_edit_text);
    }

    private String login(){
        String returnResult = "";
        name = mUser.getText().toString().trim();
        password = mPassword.getText().toString().trim();
        ip = mIP.getText().toString().trim();
        port = mPort.getText().toString().trim();

        // 检验输入是否为空
        if(TextUtils.isEmpty(name)) return returnResult = "请输入用户名";
        if(TextUtils.isEmpty(password)) return returnResult = "请输入密码";
        if(TextUtils.isEmpty(ip)) return returnResult = "请输入服务器ip地址";
        if(TextUtils.isEmpty(port)) return returnResult = "请输入服务器端口";
        try{
            FormBody.Builder params = new FormBody.Builder();
            params.add("name",name);
            params.add("password",password);

            //创建OkHttpClient实例
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(login_url).post(params.build()).build();
            //创建response，并且接收返回的token
            Response response = client.newCall(request).execute();
            String responseData = response.body().string();
            JSONObject jsonObject= new JSONObject(responseData);
            returnResult = jsonObject.getString("message");//获取JSON数据中message字段值
            return returnResult;
        }catch (Exception e) {
            e.printStackTrace();
            Log.e("err:",e.toString());
            return returnResult = "error:"+e.toString();
        }
    }
}
