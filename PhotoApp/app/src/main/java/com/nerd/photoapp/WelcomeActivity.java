package com.nerd.photoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nerd.photoapp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class WelcomeActivity extends AppCompatActivity {

    Button btn_logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        btn_logout = findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. 쉐어드 프리퍼런스에 저장되어 있는 토큰을 가져온다.
                SharedPreferences sp = getSharedPreferences(Utils.PREFERENCES_NAME, MODE_PRIVATE);
                final String token = sp.getString("token", null);
                Log.i("AAA", token);

                final JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.DELETE,
                        Utils.BASEURL + "/api/v1/users/logout",
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    boolean success = response.getBoolean("success");
                                    if (success == true){
                                        // 토큰을 지워줘야 한다.
                                        SharedPreferences token_sp = getSharedPreferences(Utils.PREFERENCES_NAME,MODE_PRIVATE);
                                        SharedPreferences.Editor editor = token_sp.edit();
                                        editor.putString("token", null);
                                        editor.apply();

                                        Intent i = new Intent(WelcomeActivity.this, LoginActivity.class);
                                        startActivity(i);
                                        finish();
                                    }else{
                                        // 토스트 띄운다.
                                        Toast.makeText(WelcomeActivity.this, "서버연결실패", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // 토스트 띄운다. 로그아웃 실패라고 토스트.
                                Toast.makeText(WelcomeActivity.this, "로그아웃에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                ) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("Authorization", "Bearer " + token);
                        return params;
                    }
                };
                Volley.newRequestQueue(WelcomeActivity.this).add(request);
            }
        });
    }
}
