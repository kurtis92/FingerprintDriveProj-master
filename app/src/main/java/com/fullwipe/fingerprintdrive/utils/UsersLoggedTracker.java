package com.fullwipe.fingerprintdrive.utils;

import com.fullwipe.fingerprintdrive.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Valentina Lipari, Meo Giovanni and Illiano Francesca on 19/08/17.
 */

public class UsersLoggedTracker {
    private static UsersLoggedTracker singleton;

    private final List<User> usersLogged;

    private UsersLoggedTracker() {
        usersLogged = new ArrayList<>();
    }

    public static UsersLoggedTracker getInstance() {
        if (singleton == null)
            singleton = new UsersLoggedTracker();
        return singleton;
    }

    public List<User> getUsersLogged() {
        return new ArrayList<>(usersLogged);
    }

    public void addUser(User user) {
        for (User userLogged : usersLogged)
            if (userLogged.email.equals(user.email))
                return;

        usersLogged.add(user);
    }

    public static void clearSession() {
        singleton = null;
    }
}
