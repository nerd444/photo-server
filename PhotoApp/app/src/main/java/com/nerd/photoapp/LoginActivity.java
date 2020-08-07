package com.nerd.photoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nerd.photoapp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    EditText edit_email;
    EditText edit_passwd;
    Button btn_login;
    Button btn_sign_up;

    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edit_email = findViewById(R.id.edit_email);
        edit_passwd = findViewById(R.id.edit_passwd);
        btn_login = findViewById(R.id.btn_login);
        btn_sign_up = findViewById(R.id.btn_sign_up);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edit_email.getText().toString().trim();
                String passwd = edit_passwd.getText().toString().trim();
                if (email.contains("@") == false){
                    Toast.makeText(LoginActivity.this, "이메일형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (passwd.isEmpty() || passwd.length() < 4 || passwd.length() > 12){
                    Toast.makeText(LoginActivity.this, "비밀번호 규칙에 맞지 않습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 포스트맨에서 body부분 셋팅
                JSONObject object = new JSONObject();
                try {
                    object.put("email", email);
                    object.put("passwd", passwd);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,                            // 메소드
                        Utils.BASEURL + "/api/v1/users/login",      // route
                        object,                                           // 포스트로 보낼 데이터
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.i("로그인",response.toString());
                                try {
                                    String token = response.getString("token");
                                    sp = getSharedPreferences(Utils.PREFERENCES_NAME, MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sp.edit();
                                    editor.putString("token", token);
                                    editor.apply();

                                    Intent i = new Intent(LoginActivity.this, WelcomeActivity.class);
                                    startActivity(i);
                                    finish();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.i("로그인", error.toString());
                            }
                        }
                );
                Volley.newRequestQueue(LoginActivity.this).add(request);
            }
        });

        btn_sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });

    }
}
