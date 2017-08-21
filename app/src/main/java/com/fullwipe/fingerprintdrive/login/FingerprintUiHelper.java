package com.fullwipe.fingerprintdrive.login;

import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fullwipe.fingerprintdrive.R;
import com.samsung.android.sdk.pass.SpassFingerprint;

/**
 * Created by Valentina Lipari, Meo Giovanni and Illiano Francesca on 12/08/17.
 */

public class FingerprintUiHelper implements SpassFingerprint.IdentifyListener {
    private static final String TAG = FingerprintUiHelper.class.getSimpleName();

    private final ImageView icon;
    private final TextView errorTv;

    public FingerprintUiHelper(ImageView icon, TextView errorTv) {
        this.icon = icon;
        this.errorTv = errorTv;
    }

    @Override
    public void onFinished(int eventStatus) {
        Log.d(TAG, "onFinished");

//        log("identify finished : reason =" + getEventStatusName(eventStatus));
//        int FingerprintIndex = 0;
//        String FingerprintGuideText = null;
//        try {
//            FingerprintIndex = mSpassFingerprint.getIdentifiedFingerprintIndex();
//        } catch (IllegalStateException ise) {
//            log(ise.getMessage());
//        }
//        if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS) {
//            log("onFinished() : Identify authentification Success with FingerprintIndex : " + FingerprintIndex);
//        } else if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS) {
//            log("onFinished() : Password authentification Success");
//        } else if (eventStatus == SpassFingerprint.STATUS_OPERATION_DENIED) {
//            log("onFinished() : Authentification is blocked because of fingerprint service internally.");
//        } else if (eventStatus == SpassFingerprint.STATUS_USER_CANCELLED) {
//            log("onFinished() : User cancel this identify.");
//        } else if (eventStatus == SpassFingerprint.STATUS_TIMEOUT_FAILED) {
//            log("onFinished() : The time for identify is finished.");
//        } else if (eventStatus == SpassFingerprint.STATUS_QUALITY_FAILED) {
//            log("onFinished() : Authentification Fail for identify.");
//            needRetryIdentify = true;
//            FingerprintGuideText = mSpassFingerprint.getGuideForPoorQuality();
//            Toast.makeText(mContext, FingerprintGuideText, Toast.LENGTH_SHORT).show();
//        } else {
//            log("onFinished() : Authentification Fail for identify");
//            needRetryIdentify = true;
//        }
//        if (!needRetryIdentify) {
//            resetIdentifyIndex();
//        }

        if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS) {
            icon.setImageResource(R.drawable.ic_fingerprint_success);
            errorTv.setTextColor(
                    errorTv.getResources().getColor(R.color.colorAccent, null));
            errorTv.setText(
                    errorTv.getResources().getString(R.string.fingerprint_success));
        } else {
            showError("fallito");
        }
    }

    @Override
    public void onReady() {
        Log.d(TAG, "onReady");
        icon.setImageResource(R.drawable.ic_fp_40px);
    }

    @Override
    public void onStarted() {
        Log.d(TAG, "onStarted");
    }

    @Override
    public void onCompleted() {
        Log.d(TAG, "onCompleted");
    }

    private void showError(CharSequence error) {
        icon.setImageResource(R.drawable.ic_fingerprint_error);
        errorTv.setText(error);
        errorTv.setTextColor(
                errorTv.getResources().getColor(R.color.colorAccent, null));
    }
}
