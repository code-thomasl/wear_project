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
import org.json.JSONException;
import org.json.JSONObject;

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

        mTextView = findViewById(R.id.text_view_result);


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
        /*String myJson =
                "  {\n" +
                "    \"student_id\": 20130039,\n" +
                "    \"gps_lat\": 36.001,\n" +
                "    \"gps_long\": 3.235,\n" +
                "    \"student_message\": \"message3\"\n" +
                "  }";


        */

        /*
        String myJson = new StringBuilder()
                .append("{")
                .append("\"student_id\":\"20130039\",")
                .append("\"gps_lat\":\"34.001\",")
                .append("\"gps_long\":\"3.235\",")
                .append("\"student_message\":\"message4\",")
                .append("}").toString();
        */

        JSONObject postdata = new JSONObject();
        try
        {
            postdata.put("student_id", "20130039");
            postdata.put("gps_lat", "34.001");
            postdata.put("gps_long", "3.235");
            postdata.put("student_message", "message5");

        } catch(JSONException e)
        {
            e.printStackTrace();
        }

        //Création de la requête POST
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                postdata.toString()
        );


        Request myPostRequest = new Request.Builder()
                .url("https://hmin309-embedded-systems.herokuapp.com/message-exchange/messages/")
                .post(body)
                .build();


        okHttpClient.newCall(myPostRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                Log.w("failure Response", mMessage);
                //call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //le retour est effectué dans un thread différent
                //final String text = response.body().string();

                String mMessage = response.body().string();
                Log.e(TAG, mMessage);
                Log.d(TAG, "[[Messsage has been posted]]");

                /*
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //mTextView.setText(text);
                    }
                });
                 */
            }


        });

        Log.d(TAG, "Messsage has been posted");
        //Toast.makeText(this, "Message has been posted", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
