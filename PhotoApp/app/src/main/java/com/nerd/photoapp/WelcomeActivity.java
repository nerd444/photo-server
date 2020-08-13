package com.nerd.photoapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nerd.photoapp.adapter.RecyclerViewAdapter;
import com.nerd.photoapp.api.NetworkClient;
import com.nerd.photoapp.api.PostApi;
import com.nerd.photoapp.api.UserApi;
import com.nerd.photoapp.model.Item;
import com.nerd.photoapp.model.Post;
import com.nerd.photoapp.model.PostRes;
import com.nerd.photoapp.model.UserRes;
import com.nerd.photoapp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class WelcomeActivity extends AppCompatActivity {

    Button btn_logout;
    Button btn_posting;

    RecyclerView recyclerView;
    RecyclerViewAdapter recyclerViewAdapter;
    List<Item> postArrayList = new ArrayList<>();

    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        btn_logout = findViewById(R.id.btn_logout);
        recyclerView = findViewById(R.id.recyclerView);
        btn_posting = findViewById(R.id.btn_posting);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(WelcomeActivity.this));

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. 쉐어드 프리퍼런스에 저장되어 있는 토큰을 가져온다.
                SharedPreferences sp = getSharedPreferences(Utils.PREFERENCES_NAME, MODE_PRIVATE);
                final String token = sp.getString("token", null);
                Log.i("AAA", token);

                Retrofit retrofit = NetworkClient.getRetrofitClient(WelcomeActivity.this);
                UserApi userApi = retrofit.create(UserApi.class);

                Call<UserRes> call = userApi.logoutUser("Bearer "+token);
                call.enqueue(new Callback<UserRes>() {
                    @Override
                    public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                        if (response.isSuccessful()){
                            if (response.body().isSuccess()){
                                Log.i("AAA", "token : "+token);
                                SharedPreferences sp = getSharedPreferences(Utils.PREFERENCES_NAME,MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("token", null);
                                editor.apply();

                                Intent i = new Intent(WelcomeActivity.this, LoginActivity.class);
                                startActivity(i);
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<UserRes> call, Throwable t) {

                    }
                });

            }
        });

        SharedPreferences sp = getSharedPreferences(Utils.PREFERENCES_NAME, MODE_PRIVATE);
        token = sp.getString("token", null);

        Log.i("AAA", "token : "+ token);

        getNetworkData();

        btn_posting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(WelcomeActivity.this, PostingActivity.class);
                startActivity(i);
            }
        });
    }

    private void getNetworkData() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(WelcomeActivity.this);

        PostApi postsApi = retrofit.create(PostApi.class);

        Call<PostRes> call = postsApi.getPosts("Bearer "+token, 0, 25);
        call.enqueue(new Callback<PostRes>() {
            @Override
            public void onResponse(Call<PostRes> call, Response<PostRes> response) {
                // response.body() => PostRes 클래스
                Log.i("AAA", response.body().getSuccess().toString());
                // response.body().get(0) => List<Item> 의 첫번째 Item 객체.
                // response.body().get(0).getContent() => 위의 Item 객체에 저장되 content 값
                Log.i("AAA", response.body().getItems().get(0).getContent());
                Log.i("AAA", response.body().getCnt().toString());

                postArrayList = response.body().getItems();

                recyclerViewAdapter = new RecyclerViewAdapter(WelcomeActivity.this, postArrayList);
                recyclerView.setAdapter(recyclerViewAdapter);
            }

            @Override
            public void onFailure(Call<PostRes> call, Throwable t) {

            }
        });

    }
}
