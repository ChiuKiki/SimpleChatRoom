package com.example.simplechatroom;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    // 注册接口
    public static final String register_url="http://chat.chiukiki.cn/api/register";
    // 变量
    String name,gender,tel,password,confirmPassword;
    // 控件
    EditText mName, mGender, mTel, mPassword, mConfirmPassword;
    Button mBtnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 点击注册按钮
        mBtnRegister = (Button) findViewById(R.id.btn_register);
        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String message = register();
                            Looper.prepare();
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                            if (message.equals("注册成功")) {
                                //进入登录页面
                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                            Looper.loop();
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }).start();
            }
        });
    }

    /**
     * 用户注册
     * 用户名、性别、手机号、密码、确认密码
     * @return returnResult
     */
    private String register(){
        String returnResult = "";
        mName = (EditText)findViewById(R.id.et_user);
        mGender = (EditText)findViewById(R.id.et_gender);
        mTel = (EditText)findViewById(R.id.et_tel);
        mPassword = (EditText)findViewById(R.id.et_password);
        mConfirmPassword = (EditText)findViewById(R.id.et_confirm_password);

        name = mName.getText().toString().trim();
        gender = mGender.getText().toString().trim();
        tel = mTel.getText().toString().trim();
        password = mPassword.getText().toString().trim();
        confirmPassword = mConfirmPassword.getText().toString().trim();

        // 检验是否为空
        if(TextUtils.isEmpty(name)) return returnResult = "请输入用户名";
        if(TextUtils.isEmpty(gender)) return returnResult = "请输入性别";
        if(TextUtils.isEmpty(tel)) return returnResult = "请输入手机号";
        if(TextUtils.isEmpty(password)) return returnResult = "请输入密码";
        if(TextUtils.isEmpty(confirmPassword)) return returnResult = "请输入再次输入密码";

        if(!password.equals(confirmPassword)) return returnResult = "两次密码不一致";

        try{
            FormBody.Builder params = new FormBody.Builder();
            params.add("name",name);
            params.add("gender",gender);
            params.add("tel",tel);
            params.add("password",password);
            params.add("confirmPassword",confirmPassword);

            //创建OkHttpClient实例
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(register_url).post(params.build()).build();
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
