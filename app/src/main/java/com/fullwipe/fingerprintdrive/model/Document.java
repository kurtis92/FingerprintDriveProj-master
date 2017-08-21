package com.fullwipe.fingerprintdrive.model;

import java.util.List;

/**
 * Created by Valentina Lipari, Meo Giovanni and Illiano Francesca on 17/08/17.
 */

public class Document {
    public String title;
    public String description;
    public List<User> users;

    public Document(String title, String description, List<User> users) {
        this.title = title;
        this.description = description;
        this.users = users;
    }
}
