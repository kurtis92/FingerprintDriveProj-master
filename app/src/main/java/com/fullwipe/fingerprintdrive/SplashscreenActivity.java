/*
 * Created by Lipari Valentina, Meo Giovanni and Illiano Francesca on 30/7/2017
 */

package com.fullwipe.fingerprintdrive;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.fullwipe.fingerprintdrive.login.LoginActivity;
import com.fullwipe.fingerprintdrive.login.WelcomeActivity;
import com.samsung.android.sdk.pass.Spass;

public class SplashscreenActivity extends BaseActivity {
    private Handler handler;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Spass spass = new Spass();
            try {
                spass.initialize(SplashscreenActivity.this);
                startActivity(new Intent(SplashscreenActivity.this, WelcomeActivity.class));
            } catch (Exception e) {
                e.printStackTrace();
                startActivity(new Intent(SplashscreenActivity.this, LoginActivity.class));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        handler = new Handler();
        handler.postDelayed(runnable, 1000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(runnable);
    }
}
