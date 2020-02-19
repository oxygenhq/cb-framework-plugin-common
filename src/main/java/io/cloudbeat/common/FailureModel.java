package io.cloudbeat.common;

import java.io.PrintWriter;
import java.io.StringWriter;

public class FailureModel {

    public FailureModel() {}

    public FailureModel(String message) {
        type = "TESTNG_ERROR";
        this.message = message;
    }

    public FailureModel(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        String stackTrace = sw.toString();
        this.type = "TESTNG_ERROR";
        this.data = stackTrace;
        this.message = throwable.getMessage();
    }

    public String type;
    public String data;
    public String message;
    public String location;
}
