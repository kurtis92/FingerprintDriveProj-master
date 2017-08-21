package com.fullwipe.fingerprintdrive;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Valentina Lipari, Meo Giovanni and Illiano Francesca on 14/08/17.
 */

public class BaseActivity extends AppCompatActivity {
    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.enter_one, R.anim.enter_two);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.back_one, R.anim.back_two);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.back_one, R.anim.back_two);
    }
}
