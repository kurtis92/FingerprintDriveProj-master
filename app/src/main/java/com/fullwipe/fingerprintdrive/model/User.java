package com.fullwipe.fingerprintdrive.model;

import android.support.annotation.Nullable;

/**
 * Created by Valentina Lipari, Meo Giovanni and Illiano Francesca on 16/08/17.
 */

public class User {
    public String fingerprintId;
    public String email;
    public String password;
    public int pwdLength;

    public User(String email) {
        this(null, email, null, -1);
    }

    public User(@Nullable String fingerprintId, String email, @Nullable String password, int pwdLength) {
        this.fingerprintId = fingerprintId;
        this.email = email;
        this.password = password;
        this.pwdLength = pwdLength;
    }
}
