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
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nerd.photoapp.api.NetworkClient;
import com.nerd.photoapp.api.UserApi;
import com.nerd.photoapp.model.UserReq;
import com.nerd.photoapp.model.UserRes;
import com.nerd.photoapp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

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

                // body 부분 셋팅
                UserReq userReq = new UserReq(email, passwd);

                // 서버로 이메일과 비밀번호를 전송한다.
                Retrofit retrofit = NetworkClient.getRetrofitClient(LoginActivity.this);
                UserApi userApi = retrofit.create(UserApi.class);

                Call<UserRes> call = userApi.loginUser(userReq);

                call.enqueue(new Callback<UserRes>() {
                    @Override
                    public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                        // 상태코드가 200 인지 확인
                        if (response.isSuccessful()){
                            // response.body()가 UserRes 이다.
                            boolean success = response.body().isSuccess();
                            String token = response.body().getToken();
                            Log.i("AAAA", "success : "+success+" token : "+token);

                            SharedPreferences sp = getSharedPreferences(Utils.PREFERENCES_NAME, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("token", token);
                            editor.apply();

                            Intent i = new Intent(LoginActivity.this, WelcomeActivity.class);
                            startActivity(i);
                            finish();

                        }
                    }

                    @Override
                    public void onFailure(Call<UserRes> call, Throwable t) {

                    }
                });
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
