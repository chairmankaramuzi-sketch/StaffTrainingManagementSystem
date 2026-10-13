package com.stms.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service for logging notifications, events, and audit logs.
 */
public class NotificationService {
    private final List<String> auditLogs = Collections.synchronizedList(new ArrayList<>());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void logEvent(String message) {
        String entry = "[" + LocalDateTime.now().format(FORMATTER) + "] " + message;
        auditLogs.add(entry);
        System.out.println(entry);
    }

    public List<String> getRecentLogs(int limit) {
        synchronized (auditLogs) {
            int size = auditLogs.size();
            int from = Math.max(0, size - limit);
            return new ArrayList<>(auditLogs.subList(from, size));
        }
    }
}
