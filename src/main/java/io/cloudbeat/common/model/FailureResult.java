package io.cloudbeat.common.model;

import io.cloudbeat.common.Helper;

import java.io.PrintWriter;
import java.io.StringWriter;

public class FailureResult {
    public final static String FAILURE_TYPE = "JAVA_ERROR";
    public FailureResult() {}

    public FailureResult(String message) {
        type = FAILURE_TYPE;
        this.message = message;
    }

    public FailureResult(Throwable throwable) {
        this(throwable, null);
    }

    public FailureResult(Throwable throwable, String testPackageName) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        String stackTrace = sw.toString();
        StackTraceElement[] filteredStackTrace =
                Helper.getStackTraceStartingFromPackage(throwable.getStackTrace(), testPackageName);
        this.subtype = throwable.getClass().getSimpleName();
        this.type = throwable.getClass().getSimpleName();
        this.data = stackTrace;
        this.stacktrace = Helper.stackTraceToStringArray(filteredStackTrace);
        this.message = throwable.getMessage();
        if(this.message == null) {
            this.message = "UNKNOWN_ERROR ";
        }
        // set location attribute
        if (filteredStackTrace.length > 0) {
            this.location = filteredStackTrace[0].toString();
        }
    }

    public String type;
    public String subtype;
    public String data;
    public String[] stacktrace;
    public String message;
    public String location;
}
