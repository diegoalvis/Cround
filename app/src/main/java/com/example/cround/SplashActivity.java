package com.example.cround;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {

    private final int TIEMPO_PANTALLA   = 3500; // 3 segundos

    ImageView iv_logo, iv_fondo;
    TextView tv_titulo, tv_eslogan;

    IntentLauncher intentLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        iv_fondo = (ImageView) findViewById(R.id.iv_fondo);
        iv_logo = (ImageView) findViewById(R.id.iv_splash_logo);
        tv_titulo = (TextView) findViewById(R.id.tv_splash_titulo);
        tv_eslogan = (TextView) findViewById(R.id.tv_splash_eslogan);

        intentLauncher= new IntentLauncher();
        intentLauncher.start();

    }



    private class IntentLauncher extends Thread {
        @Override
        public void run() {
            try {
                AnimarLogo();
                Thread.sleep(TIEMPO_PANTALLA/2);
                AnimarFondoIN();
                Thread.sleep((TIEMPO_PANTALLA)/2);
                LanzarActivityConnect();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void LanzarActivityConnect() {
        Intent intent = new Intent(this, ConnectActivity.class);
        startActivity(intent);
        finish();
    }

    private void AnimarFondoOUT() {
        runOnUiThread(new Runnable() {      //go count - show interface
            @Override
            public void run() {
                Animation animation = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.fade_out);
                animation.reset();
                iv_fondo.startAnimation(animation);
            }
        });
    }

    private void AnimarFondoIN() {
        runOnUiThread(new Runnable() {      //go count - show interface
            @Override
            public void run() {
                Animation animation = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.fade_in);
                animation.reset();
                iv_fondo.startAnimation(animation);
                tv_titulo.startAnimation(animation);
                tv_eslogan.startAnimation(animation);
            }
        });
    }

    private void AnimarLogo() {
        runOnUiThread(new Runnable() {      //go count - show interface
            @Override
            public void run() {
                Animation animation = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.fade_in);
                animation.reset();
                iv_logo.startAnimation(animation);
            }
        });

    }
}
