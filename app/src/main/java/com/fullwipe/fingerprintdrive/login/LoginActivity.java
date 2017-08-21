package com.fullwipe.fingerprintdrive.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fullwipe.fingerprintdrive.BaseActivity;
import com.fullwipe.fingerprintdrive.R;
import com.fullwipe.fingerprintdrive.UserActivity;
import com.fullwipe.fingerprintdrive.model.User;
import com.fullwipe.fingerprintdrive.utils.UsersLoggedTracker;
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
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.fullwipe.fingerprintdrive.utils.Constants.FIELD_EMAIL;
import static com.fullwipe.fingerprintdrive.utils.Constants.FIELD_PASSWORD;
import static com.fullwipe.fingerprintdrive.utils.Constants.CHILD_USERS;
import static com.fullwipe.fingerprintdrive.utils.Constants.FIELD_PWD_LENGTH;

public class LoginActivity extends BaseActivity {
    @BindView(R.id.tiEmail) TextInputLayout tiEmail;
    @BindView(R.id.etEmail) EditText etEmail;
    @BindView(R.id.etPassword) EditText etPassword;
    @BindView(R.id.btnLogin) Button login;
    @BindView(R.id.text_ForgotPassword) View forgotPassword;
    @BindView(R.id.signin_layout) View signin;
    @BindView(R.id.divider) View divider;

    private static final String TAG = LoginActivity.class.getSimpleName();
    public static final String EXTRA_MULTIUSER = "multi_user";

    private final List<User> users = new ArrayList<>();
    private String cryptedImei;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    private Spass spass;
    private SpassFingerprint spassFingerprint;

    private FingerprintUiHelper fingerprintUiHelper;

    private boolean isMultiUser = false;
    private boolean onReadyIdentify = false;
    private boolean isFeatureEnabled_fingerprint;
    private boolean isFeatureEnabled_custom;

    private String cryptedPassword;

    private MaterialDialog fingerprintDialog;
    private MaterialDialog progressDialog;

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

            boolean canceledByUser = false;
            String onFinished = "onFinished";
            StringBuilder msgBuilder = new StringBuilder(onFinished);
            String fingerprintIndexCrypted = Utils.crypt(String.valueOf(fingerprintIndex));

            switch (eventStatus) {
                case SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS:
                    msgBuilder.append(" - Identify authentification Success with fingerprintIndex: ").append(fingerprintIndex);

                    if (isMultiUser) {
                        for (User user : users) {
                            if (user.fingerprintId.equals(fingerprintIndexCrypted)) {
                                etEmail.setText(user.email);
                                etPassword.setText(user.password.substring(0, user.pwdLength));
                                cryptedPassword = user.password;
                                break;
                            }
                        }
                    }
                    break;
                case SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS:
                    msgBuilder.append(" - Password authentification Success");
                    break;
                case SpassFingerprint.STATUS_USER_CANCELLED:
                case SpassFingerprint.STATUS_USER_CANCELLED_BY_TOUCH_OUTSIDE:
                    msgBuilder.append(" - User cancel this identify");
                    canceledByUser = true;
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
                Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                if (canceledByUser) finish();
            } else {
                Log.e(TAG, "onFinished(): Authentification Fail for identify");
                Toast.makeText(LoginActivity.this, "Fallimento", Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        progressDialog = Utils.defaultProgressDialog(this);

//        getActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
            isMultiUser = extras.getBoolean(EXTRA_MULTIUSER, false);
        setUI();

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        if (isMultiUser) {
            progressDialog.show();

            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            cryptedImei = Utils.crypt(telephonyManager.getDeviceId());

            spass = new Spass();

            try {
                spass.initialize(this);
            } catch (SsdkUnsupportedException | UnsupportedOperationException e) {
                progressDialog.dismiss();
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

            // Read from the database
            databaseReference.child(CHILD_USERS).child(cryptedImei).addListenerForSingleValueEvent(new ValueEventListener() {
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

                    if (users.isEmpty()) {
                        Toast.makeText(LoginActivity.this, R.string.no_users_found, Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    if (isFeatureEnabled_custom) {
                        progressDialog.dismiss();
                        startIdentifyDialog();
                    }
                    else {
                        Toast.makeText(LoginActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                    Toast.makeText(LoginActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            etEmail.setText("v.lipari93@gmail.com");
            etPassword.setText("finardonenazionale");
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                String password;
                if (isMultiUser) {
                    password = cryptedPassword;
                    cryptedPassword = null;
                } else {
                    if (etPassword.getText().toString().length() < 6) {
                        etPassword.setError("Almeno 6 caratteri");
                        return;
                    }

                    if (!Utils.isValidEmail(etEmail.getText().toString())) {
                        etEmail.setError("Email non valida");
                        return;
                    }

                    password = Utils.crypt(etPassword.getText().toString());
                }

                mAuth.signInWithEmailAndPassword(etEmail.getText().toString(), password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressDialog.dismiss();
                                Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "signInWithEmail:failed", task.getException());
                                    Toast.makeText(LoginActivity.this, "Login fallito",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Login OK",
                                            Toast.LENGTH_SHORT).show();
                                    UsersLoggedTracker.getInstance().addUser(new User(etEmail.getText().toString()));
                                    Intent intent = new Intent(LoginActivity.this, UserActivity.class);
                                    intent.putExtra(UserActivity.EXTRA_MULTIUSER, isMultiUser);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            }
                        });
            }
        });

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignInActivity.class));
            }
        });
    }

    private void setUI() {
        etEmail.setEnabled(!isMultiUser);
        etPassword.setEnabled(!isMultiUser);
        forgotPassword.setVisibility(isMultiUser? View.GONE : View.VISIBLE);
        signin.setVisibility(isMultiUser? View.GONE : View.VISIBLE);
        divider.setVisibility(isMultiUser? View.GONE : View.VISIBLE);
    }

    private void startIdentifyDialog() {
        if (!onReadyIdentify) {
            onReadyIdentify = true;
            try {
                if (spassFingerprint != null) {
                    spassFingerprint.startIdentifyWithDialog(LoginActivity.this, spassIdentifyListener, false);
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

    //    private void boh() {
//        LinearLayout dialogView =
//                (LinearLayout) LayoutInflater.from(this).inflate(R.layout.fingerprint_dialog_container, null);
//        ImageView icon = (ImageView) dialogView.findViewById(R.id.fingerprint_icon);
//        TextView errorTv = (TextView) dialogView.findViewById(R.id.fingerprint_status);
//
//        fingerprintUiHelper = new FingerprintUiHelper(icon, errorTv);
//        fingerprintDialog = new MaterialDialog.Builder(this)
//                .title("Effettua login")
//                .customView(dialogView, false)
//                .cancelable(false)
//                .build();
//        fingerprintDialog.show();
//    }
}