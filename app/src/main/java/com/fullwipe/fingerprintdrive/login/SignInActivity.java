package com.fullwipe.fingerprintdrive.login;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fullwipe.fingerprintdrive.BaseActivity;
import com.fullwipe.fingerprintdrive.R;
import com.fullwipe.fingerprintdrive.model.User;
import com.fullwipe.fingerprintdrive.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.fullwipe.fingerprintdrive.utils.Constants.FIELD_EMAIL;
import static com.fullwipe.fingerprintdrive.utils.Constants.FIELD_PASSWORD;
import static com.fullwipe.fingerprintdrive.utils.Constants.CHILD_USERS;
import static com.fullwipe.fingerprintdrive.utils.Constants.FIELD_PWD_LENGTH;

public class SignInActivity extends BaseActivity {
    @BindView(R.id.etEmail) EditText etEmail;
    @BindView(R.id.etPassword) EditText etPassword;
    @BindView(R.id.etConfPassword) EditText etConfPassword;
    @BindView(R.id.btnSignin) Button signin;
    @BindView(R.id.signin_layout) View goToLogin;
    @BindView(R.id.impronta_layout) View goToFingerprintSettings;

    private static final String TAG = SignInActivity.class.getSimpleName();
    public static final String EXTRA_MULTIUSER = "multi_user";

    private FirebaseDatabase firebaseDatabaseInstance;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private Spass spass;
    private SpassFingerprint spassFingerprint;

    private List<User> users = new ArrayList<>();
    private String cryptedImei;

    private boolean isMultiUser = false;
    private boolean onReadyIdentify = false;
    private boolean onReadyEnroll = false;
    private boolean isFeatureEnabled_fingerprint;
    private boolean isFeatureEnabled_custom;

    private MaterialDialog progressDialog;

    private SpassFingerprint.RegisterListener spassRegisterListener = new SpassFingerprint.RegisterListener() {
        @Override
        public void onFinished() {
            onReadyEnroll = false;
            Log.d(TAG, "RegisterListener.onFinished()");
        }
    };

    private final SpassFingerprint.IdentifyListener spassIdentifyListener =
            new SpassFingerprint.IdentifyListener() {
                @Override
                public void onFinished(int eventStatus) {
                    int fingerprintIndex = 0;
                    onReadyIdentify = false;
                    try {
                        fingerprintIndex = spassFingerprint.getIdentifiedFingerprintIndex();
                    } catch (IllegalStateException ise) {
                        Log.e(TAG, ise.getMessage());
                        return;
                    }

                    boolean isSigninValid = true;
                    String onFinished = "onFinished";
                    StringBuilder msgBuilder = new StringBuilder(onFinished);
                    String fingerprintIndexCrypted = Utils.crypt(String.valueOf(fingerprintIndex));

                    switch (eventStatus) {
                        case SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS:
                            if (isMultiUser) {
                                for (User user : users) {
                                    if (user.fingerprintId.equals(fingerprintIndexCrypted)) {
                                        msgBuilder.append(" - Success, but this fingerprint is already registered. Index: ").append(fingerprintIndex);
                                        isSigninValid = false;
                                        break;
                                    }

                                    if (user.email.equals(etEmail.getText().toString())) {
                                        msgBuilder.append(" - Success, but this email is already registered. Email: ").append(etEmail.getText().toString());
                                        isSigninValid = false;
                                        break;
                                    }
                                }

                                msgBuilder.append(" - Identify authentification Success with fingerprintIndex: ").append(fingerprintIndex);
                            } else
                                msgBuilder.append(" - Identify authentification Success with fingerprintIndex: ").append(fingerprintIndex);
                            break;
                        case SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS:
                            msgBuilder.append(" - Password authentification Success");
                            break;
                        case SpassFingerprint.STATUS_USER_CANCELLED:
                        case SpassFingerprint.STATUS_USER_CANCELLED_BY_TOUCH_OUTSIDE:
                            msgBuilder.append(" - User cancel this identify");
                            break;
                        case SpassFingerprint.STATUS_TIMEOUT_FAILED:
                            msgBuilder.append(" - The time for identify is finished");
                            break;
                        case SpassFingerprint.STATUS_BUTTON_PRESSED:
                            msgBuilder.append(" - User pressed the own button. Please connect own Backup Menu");
                            break;
                    }

                    String msg = msgBuilder.toString();
                    if (msg.length() > onFinished.length()) {
                        Log.d(TAG, msg);
                        Toast.makeText(SignInActivity.this, msg, Toast.LENGTH_SHORT).show();
                        if (isSigninValid)
                            executeSignin(fingerprintIndexCrypted);
                    } else {
                        Log.e(TAG, "onFinished(): Authentification Fail for identify");
                        Toast.makeText(SignInActivity.this, "Fallimento", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onReady() {
                    Log.d(TAG, "identify state is ready");
                }

                @Override
                public void onStarted() {
                    Log.d(TAG, "User touched fingerprint sensor");
                }

                @Override
                public void onCompleted() {
                    Log.d(TAG, "the identify is completed");
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);

        progressDialog = Utils.defaultProgressDialog(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
            isMultiUser = extras.getBoolean(EXTRA_MULTIUSER, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabaseInstance = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabaseInstance.getReference();

        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this, LoginActivity.class));
            }
        });

        goToFingerprintSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMultiUser)
                    registerFingerprint();
                else startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
            }
        });

        if (isMultiUser) {
            progressDialog.show();
            signin.setEnabled(false);
            goToFingerprintSettings.setEnabled(false);

            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            cryptedImei = Utils.crypt(telephonyManager.getDeviceId());

            spass = new Spass();

            try {
                spass.initialize(this);
            } catch (SsdkUnsupportedException | UnsupportedOperationException e) {
                e.printStackTrace();
                finish();
            }
            isFeatureEnabled_fingerprint = spass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT);

            if (isFeatureEnabled_fingerprint) {
                spassFingerprint = new SpassFingerprint(this);
                Log.d(TAG, "Fingerprint Service is supported in the device. SDK version : " + spass.getVersionName());
            } else {
                Log.e(TAG, "Fingerprint Service is not supported in the device.");
                finish();
            }

            isFeatureEnabled_custom = spass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT_CUSTOMIZED_DIALOG);

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(CHILD_USERS)
                            && dataSnapshot.child(CHILD_USERS).hasChild(cryptedImei)) {
                        checkUserData();
                    } else if (isFeatureEnabled_custom) {
                        progressDialog.dismiss();
                        goToFingerprintSettings.setEnabled(true);
                        signin.setEnabled(true);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "database error", databaseError.toException());
                    Toast.makeText(SignInActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

        }

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ensureValidDataForm()) {
                    if (isMultiUser)
                        startIdentifyDialog();
                    else {
                        executeSignin(null);
                    }
                }
            }
        });
    }

    private void checkUserData() {
        // Read from the database
        databaseReference.child(CHILD_USERS).child(cryptedImei)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    users.add(new User(
                            userSnapshot.getKey(),
                            userSnapshot.child(FIELD_EMAIL).getValue(String.class),
                            userSnapshot.child(FIELD_PASSWORD).getValue(String.class),
                            Integer.valueOf(userSnapshot.child(FIELD_PWD_LENGTH).getValue(String.class))));
                }

                if (isFeatureEnabled_custom) {
                    progressDialog.dismiss();
                    goToFingerprintSettings.setEnabled(true);
                    signin.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
                Toast.makeText(SignInActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private boolean ensureValidDataForm() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String confPassword = etConfPassword.getText().toString();

        if (!password.equals(confPassword)) {
            etConfPassword.setError("Le password non sono uguali");
            etConfPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Almeno 6 caratteri");
            etPassword.requestFocus();
            return false;
        }

        if (!Utils.isValidEmail(email)) {
            etEmail.setError("Email non valida");
            etEmail.requestFocus();
            return false;
        }
        return true;
    }

    private void executeSignin(final String fingerprintIndexCrypted) {
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(
                etEmail.getText().toString(),
                Utils.crypt(etPassword.getText().toString()))
                .addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignInActivity.this, "Registrazione fallita",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            if (isMultiUser)
                                saveNewUserData(fingerprintIndexCrypted);
                            else {
                                progressDialog.dismiss();
                                Toast.makeText(SignInActivity.this, "Registrazione ESEGUITA",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    }
                });
    }

    private void saveNewUserData(String fingerprintIndexCrypted) {
        HashMap<String, String> userData = new HashMap<>();
        userData.put(FIELD_EMAIL, etEmail.getText().toString());
        userData.put(FIELD_PASSWORD, Utils.crypt(etPassword.getText().toString()));
        userData.put(FIELD_PWD_LENGTH, String.valueOf(etPassword.getText().toString().length()));

        DatabaseReference userDatabaseRef = firebaseDatabaseInstance.getReference();
        userDatabaseRef.child(CHILD_USERS).child(cryptedImei).child(fingerprintIndexCrypted)
                .setValue(userData, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                progressDialog.dismiss();
                if (databaseError != null) {
                    Log.e(TAG, "save new data error", databaseError.toException());
                    Toast.makeText(SignInActivity.this, "save new data error", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(SignInActivity.this, "Registrazione ESEGUITA",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void startIdentifyDialog() {
        if (!onReadyIdentify) {
            onReadyIdentify = true;
            try {
                if (spassFingerprint != null) {
                    spassFingerprint.startIdentifyWithDialog(SignInActivity.this, spassIdentifyListener, false);
                }
                Log.d(TAG, "Please identify finger to verify you");
            } catch (IllegalStateException e) {
                onReadyIdentify = false;
                Log.e(TAG, "Exception: " + e);
            }
        } else {
            Log.d(TAG, "The previous request is remained. Please finished or cancel first");
        }
    }

    private void registerFingerprint() {
        if (!onReadyIdentify) {
            if (!onReadyEnroll) {
                onReadyEnroll = true;
                if (spassFingerprint != null) {
                    spassFingerprint.registerFinger(SignInActivity.this, spassRegisterListener);
                }
                Log.d(TAG, "Jump to the Enroll screen");
            } else {
                Log.d(TAG, "Please wait and try to register again");
            }
        } else {
            Log.d(TAG, "Please cancel Identify first");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (progressDialog == null)
            progressDialog = Utils.defaultProgressDialog(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
