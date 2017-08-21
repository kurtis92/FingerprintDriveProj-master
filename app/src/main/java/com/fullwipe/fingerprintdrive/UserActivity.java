package com.fullwipe.fingerprintdrive;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fullwipe.fingerprintdrive.explore.ExploreActivity;
import com.fullwipe.fingerprintdrive.login.LoginActivity;
import com.fullwipe.fingerprintdrive.login.WelcomeActivity;
import com.fullwipe.fingerprintdrive.model.User;
import com.fullwipe.fingerprintdrive.utils.UsersLoggedTracker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class UserActivity extends BaseActivity {
    @BindView(R.id.btnaggcollab) Button addUser;
    @BindView(R.id.btncontenuti) Button exploreDocuments;
    @BindView(R.id.benvenuto) TextView tvWelcome;

    public static final String EXTRA_MULTIUSER = "multi_user";

    private boolean isMultiUser = false;

    private MaterialDialog askExitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
            isMultiUser = extras.getBoolean(EXTRA_MULTIUSER, false);

        Collection<User> usersLogged = UsersLoggedTracker.getInstance().getUsersLogged();
        List<String> usernames = new ArrayList<>();

        for (User userLogged : usersLogged)
            usernames.add(userLogged.email.split("@")[0]);

        String welcome = usernames.size() > 1? "Benvenuti" : "Benvenuto";
        StringBuilder welcomeTextBuilder = new StringBuilder(welcome);
        welcomeTextBuilder.append(" ");

        for (int i = 0; i < usernames.size(); i++) {
            welcomeTextBuilder.append(usernames.get(i));
            if (i < usernames.size() - 1)
                welcomeTextBuilder.append(", ");
        }
        welcomeTextBuilder.append("!");
        tvWelcome.setText(welcomeTextBuilder.toString());

        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (isMultiUser)
                    intent = new Intent(UserActivity.this, WelcomeActivity.class);
                else
                    intent =  new Intent(UserActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        exploreDocuments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserActivity.this, ExploreActivity.class));
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (askExitDialog != null) {
            askExitDialog.dismiss();
            askExitDialog = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (isTaskRoot()) {
            if (askExitDialog == null) {
                askExitDialog = new MaterialDialog.Builder(UserActivity.this)
                        .title(R.string.warning)
                        .content(R.string.ask_exit)
                        .positiveText(R.string.yes)
                        .negativeText(R.string.cancel)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                UsersLoggedTracker.clearSession();
                                UserActivity.super.onBackPressed();
                            }
                        }).build();
            }
            askExitDialog.show();
        } else super.onBackPressed();
    }
}
