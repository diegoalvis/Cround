package com.example.cround;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity {

    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_tv);

        videoView = (VideoView) findViewById(R.id.video_view);

        String uriPath2 = "android.resource://com.example.cround/"+R.raw.video_3;
        Uri uri2 = Uri.parse(uriPath2);
        //videoView.hori
        videoView.setVideoURI(uri2);
        videoView.requestFocus();
        videoView.start();

    }
}
