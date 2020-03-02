package io.cloudbeat.common.model;

import java.util.Date;

public class LogResult {
    public Date dateTime;
    public LogType logType;
    public String message;
    public enum LogType
    {
        Info,
        Warning,
        Error,
    }
}
