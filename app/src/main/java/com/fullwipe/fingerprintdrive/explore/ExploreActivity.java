package com.fullwipe.fingerprintdrive.explore;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.fullwipe.fingerprintdrive.BaseActivity;
import com.fullwipe.fingerprintdrive.R;
import com.fullwipe.fingerprintdrive.model.Document;
import com.fullwipe.fingerprintdrive.model.User;
import com.fullwipe.fingerprintdrive.utils.GridItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ExploreActivity extends BaseActivity {
    @BindView(R.id.gridview) RecyclerView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);
        ButterKnife.bind(this);

        List<User> userList1 = new ArrayList<>();
        List<User> userList2 = new ArrayList<>();
        List<User> userList3 = new ArrayList<>();
        List<User> userList4 = new ArrayList<>();

        userList1.add(new User("", "V", null, 1));
        userList1.add(new User("", "G", null, 1));

        userList2.add(new User("", "V", null, 1));
        userList2.add(new User("", "F", null, 1));

        userList3.add(new User("", "G", null, 1));
        userList3.add(new User("", "F", null, 1));

        userList4.add(new User("", "V", null, 1));
        userList4.add(new User("", "G", null, 1));
        userList4.add(new User("", "F", null, 1));

        List<Document> exampleList = new ArrayList<>();
        exampleList.add(new Document("Appunti.pdf", "Raccolta di appunti, A.A. 1984/85", userList1));
        exampleList.add(new Document("Mockup.pdf", "Esportato da Adobe Experience Design", userList4));
        exampleList.add(new Document("Manifest.xml", "Backup del manifest", userList2));
        exampleList.add(new Document("Progetto.zip", "Backup dell'applicazione", userList3));
        exampleList.add(new Document("FPdrive.apk", "FingerPrint Drive per dispositivi Android", userList4));

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        gridView.setLayoutManager(gridLayoutManager);
        gridView.addItemDecoration(new GridItemDecoration(getResources().getDimensionPixelSize(R.dimen.dp4)));
        gridView.setAdapter(new ExploreAdapter(exampleList));
    }
}
