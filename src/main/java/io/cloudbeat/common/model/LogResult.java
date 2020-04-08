package io.cloudbeat.common.model;

import org.openqa.selenium.logging.LogEntry;

import java.util.logging.Level;

public class LogResult {
    public long time;
    public String level;
    public String msg;
    public String src;

    public LogResult(LogEntry x, String source) {
        time = x.getTimestamp();
        level = x.getLevel().getName();
        msg = x.getMessage();
        src = source;
    }
}
