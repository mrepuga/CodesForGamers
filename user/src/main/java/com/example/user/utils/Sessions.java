package com.example.user.utils;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

public class Sessions {
    private static final List<HttpSession> activeSessions = new ArrayList<>();

    public static void addSession(HttpSession session) {
        activeSessions.add(session);
    }

    public static void removeSession(HttpSession session) {
        activeSessions.remove(session);
    }

    public static List<HttpSession> getAllSessions() {
        return new ArrayList<>(activeSessions);
    }
}
