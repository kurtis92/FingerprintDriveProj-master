package com.fullwipe.fingerprintdrive.utils;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fullwipe.fingerprintdrive.R;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Valentina Lipari, Meo Giovanni and Illiano Francesca on 18/08/17.
 */

public class Utils {
    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile(
                    "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
                    , Pattern.CASE_INSENSITIVE);

    public static String crypt(String textToCrypt) {
        return Hashing.sha256().hashString(textToCrypt, Charset.defaultCharset())
        .toString();
    }

    public static boolean isValidEmail(String emailAddress) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailAddress);
        return matcher.find();
    }

    public static MaterialDialog defaultProgressDialog(Context context) {
        return new MaterialDialog.Builder(context)
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .content(R.string.wait)
                .cancelable(false)
                .build();
    }
}
