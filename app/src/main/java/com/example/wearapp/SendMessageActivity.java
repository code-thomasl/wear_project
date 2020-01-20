package com.example.wearapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendMessageActivity extends WearableActivity  implements View.OnClickListener {

    private static final String TAG = "SendMessageActivity";
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        Button sendMsgBck = findViewById(R.id.send);
        sendMsgBck.setOnClickListener((View.OnClickListener) this);


        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    public void onClick(View view) {
        postMessage();
    }

    public void postMessage() {
        //post to this https://hmin309-embedded-systems.herokuapp.com/message-exchange/messages/
        // use Retrofit library ?

        OkHttpClient okHttpClient = new OkHttpClient();

        MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");
        String myJson =
                "  {\n" +
                "    \"student_id\": 20130039,\n" +
                "    \"gps_lat\": 36.001,\n" +
                "    \"gps_long\": 3.235,\n" +
                "    \"student_message\": \"message3\"\n" +
                "  }";

        //Création de la requête POST
        Request myGetRequest = new Request.Builder()
                .url("https://hmin309-embedded-systems.herokuapp.com/message-exchange/messages/")
                .post(RequestBody.create(JSON_TYPE, myJson))
                .build();

        okHttpClient.newCall(myGetRequest).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                //le retour est effectué dans un thread différent
                final String text = response.body().string();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //textView.setText(text);
                    }
                });
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }
        });

        Log.d(TAG, "Messsage has been posted");
        //Toast.makeText(this, "Message has been posted", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
