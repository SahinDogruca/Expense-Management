package com.ozandanis.expense.util;

import java.util.HashMap;
import java.util.Map;

public class UserSession{
    private static UserSession instance;
    private Map<String, Object> sessionData;
    private Object currentUser;

    private UserSession() {
        sessionData = new HashMap<>();
    }

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setAttribute(String key, int value) {
        sessionData.put(key, value);
    }

    public Object getAttribute(String key) {
        return sessionData.get(key);
    }

    public void removeAttribute(String key) {
        sessionData.remove(key);
    }

    public void setCurrentUser(Object user) {
        this.currentUser = user;
    }

    public Object getCurrentUser() {
        return currentUser;
    }

    public void clearSession() {
        sessionData.clear();
        currentUser = null;
    }
}