package com.tallerrepair.tallerrepair.session;

import com.tallerrepair.tallerrepair.entity.User;

public final class AppSession {
    private static User currentUser;

    private AppSession() {
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void clear() {
        currentUser = null;
    }
}
